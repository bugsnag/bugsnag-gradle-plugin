package com.bugsnag.gradle

import com.android.build.api.variant.HasAndroidResources
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.tasks.ExternalNativeBuildTask
import com.bugsnag.gradle.android.AndroidVariant
import com.bugsnag.gradle.android.CreateBuildTask
import com.bugsnag.gradle.android.ExtractBugsnagJniLibsTask
import com.bugsnag.gradle.android.GenerateBuildIdTask
import com.bugsnag.gradle.android.GenerateResourcesTask
import com.bugsnag.gradle.android.HasAndroidOptions
import com.bugsnag.gradle.android.UploadBundleTask
import com.bugsnag.gradle.android.UploadMappingTask
import com.bugsnag.gradle.android.UploadNativeSymbolsTask
import com.bugsnag.gradle.android.configureFrom
import com.bugsnag.gradle.android.from
import com.bugsnag.gradle.android.onAndroidVariant
import com.bugsnag.gradle.dsl.BugsnagExtension
import com.bugsnag.gradle.dsl.VariantConfiguration
import com.bugsnag.gradle.dsl.debug
import com.bugsnag.gradle.util.wireFinalizer
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.process.ExecOperations
import javax.inject.Inject

internal const val TASK_GROUP = "BugSnag"
internal const val UPLOAD_TASK_PREFIX = "bugsnagUpload"
internal const val CREATE_BUILD_TASK_PREFIX = "bugsnagCreate"
internal const val CLEAN_TASK = "Clean"

class GradlePlugin @Inject constructor(
    private val execOperations: ExecOperations
) : Plugin<Project> {
    override fun apply(target: Project) {
        val bugsnag = target.extensions.create("bugsnag", BugsnagExtension::class.java)
        // turn-off the 'debug' variant by default
        bugsnag.variants.debug.enabled = false

        configurePlugin(bugsnag, target)
    }

    private fun configurePlugin(bugsnag: BugsnagExtension, target: Project) {
        target.afterEvaluate {
            if (bugsnag.enabled && bugsnag.enableLegacyNativeExtraction) {
                registerNdkLibInstallTask(target)
            }
        }

        target.onAndroidVariant { variant: AndroidVariant ->
            val variantConfiguration = VariantConfiguration(bugsnag, bugsnag.variants.findByName(variant.name))

            if (!variantConfiguration.enabled) {
                return@onAndroidVariant
            }

            registerBugsnagTasks(target, variant, variantConfiguration, execOperations)
        }
    }
}

private fun registerBugsnagTasks(
    target: Project,
    variant: AndroidVariant,
    variantConfiguration: VariantConfiguration,
    execOperations: ExecOperations
) {
    val buildUuidProvider = registerBuildIdGenerationTask(target, variant, variantConfiguration)

    val uploadBundleTask = target.tasks.register(
        variant.name.toTaskName(prefix = UPLOAD_TASK_PREFIX, suffix = "Bundle"),
        UploadBundleTask::class.java,
        configureUploadBundleTask(target, variantConfiguration, variant, execOperations)
    )

    if (variantConfiguration.autoUploadBundle) {
        target.wireFinalizer(uploadBundleTask, variant.bundleTaskName)
    }

    val createBuildTask = target.tasks.register(
        variant.name.toTaskName(prefix = CREATE_BUILD_TASK_PREFIX, suffix = "Build"),
        CreateBuildTask::class.java,
        configureCreateBuildTask(target, variantConfiguration, variant, execOperations)
    )
    createBuildTask.configure { it.buildUuid.set(buildUuidProvider) }

    if (variantConfiguration.autoCreateBuild) {
        target.wireFinalizer(createBuildTask, variant.bundleTaskName)
    }

    if (variant.obfuscationMappingFile != null) {
        registerProguardUploadTask(target, variant, variantConfiguration, buildUuidProvider, execOperations)
    }

    if (variant.nativeSymbols != null) {
        target.tasks.register(
            variant.name.toTaskName(prefix = UPLOAD_TASK_PREFIX, suffix = "NativeSymbols"),
            UploadNativeSymbolsTask::class.java,
            configureUploadNativeSymbolsTask(variantConfiguration, variant, target, execOperations)
        )
    }
}

private fun registerBuildIdGenerationTask(
    target: Project,
    variant: AndroidVariant,
    variantConfiguration: VariantConfiguration
): Provider<String> {
    val generateBuildIdTask = target.tasks.register(
        variant.name.toTaskName(prefix = "bugsnagGenerate", suffix = "BuildId"),
        GenerateBuildIdTask::class.java
    ) { task ->
        task.group = TASK_GROUP
        task.buildUuid.set(variantConfiguration.buildUuid)
        val buildIdFile = target.layout.buildDirectory.file(
            "intermediates/bugsnag/build-id-${variant.name}.txt"
        )
        task.outputFile.set(buildIdFile)
    }

    val buildUuidProvider = generateBuildIdTask.flatMap { it.outputFile }
        .map { it.asFile.readText().trim() }

    val generateResourcesTask = target.tasks.register(
        variant.name.toTaskName(prefix = "bugsnagGenerate", suffix = "Resources"),
        GenerateResourcesTask::class.java
    ) { task ->
        task.group = TASK_GROUP
        task.buildUuidFile.set(generateBuildIdTask.flatMap { it.outputFile })
        val resDir = target.layout.buildDirectory.dir("generated/res/bugsnag/${variant.name}")
        task.outputDirectory.set(resDir)
    }

    val variantResources = variant.variant
    if (variantResources is HasAndroidResources) {
        variantResources.sources.res?.addGeneratedSourceDirectory(
            generateResourcesTask,
            GenerateResourcesTask::outputDirectory
        )
    }
    return buildUuidProvider
}

private fun registerProguardUploadTask(
    target: Project,
    variant: AndroidVariant,
    variantConfiguration: VariantConfiguration,
    buildUuidProvider: Provider<String>,
    execOperations: ExecOperations
) {
    target.tasks.register(
        variant.name.toTaskName(prefix = UPLOAD_TASK_PREFIX, suffix = "ProguardMapping"),
        UploadMappingTask::class.java
    ) { task ->
        configureAndroidTask(task, variantConfiguration, variant, execOperations)
        task.mappingFile.set(variant.obfuscationMappingFile)
        task.androidVariantMetadata.configureFrom(variantConfiguration, variant)
        variant.dexClassesDir?.let { task.dexClassesDir.set(it) }
        task.buildUuid.set(buildUuidProvider)
    }
}

private fun registerNdkLibInstallTask(project: Project) {
    val ndkTasks = project.tasks.withType(ExternalNativeBuildTask::class.java)
    val cleanTasks = ndkTasks.filter { it.name.contains(CLEAN_TASK) }.toSet()
    val buildTasks = ndkTasks.filter { !it.name.contains(CLEAN_TASK) }.toSet()

    if (buildTasks.isNotEmpty()) {
        val ndkSetupTask = project.tasks.register(
            "bugsnagInstallJniLibsTask",
            ExtractBugsnagJniLibsTask::class.java
        ) { task ->
            task.group = TASK_GROUP
            task.bugsnagArtifacts.from(ExtractBugsnagJniLibsTask.resolveBugsnagArtifacts(project))
        }

        ndkSetupTask.configure { it.mustRunAfter(cleanTasks) }
        buildTasks.forEach { it.dependsOn(ndkSetupTask) }
    }
}

private fun configureUploadNativeSymbolsTask(
    variantConfiguration: VariantConfiguration,
    variant: AndroidVariant,
    target: Project,
    execOperations: ExecOperations
) = Action<UploadNativeSymbolsTask> { task ->
    configureAndroidTask(task, variantConfiguration, variant, execOperations)
    task.symbolFiles.from(variant.nativeSymbols)

    val projectRoot = variantConfiguration.projectRoot ?: target.rootDir.toString()
    val ndkRoot =
        variantConfiguration.ndkRoot ?: target.extensions.getByType(BaseExtension::class.java).ndkDirectory
    task.projectRoot.set(projectRoot)
    task.ndkRoot.set(ndkRoot)
    task.androidVariantMetadata.configureFrom(variantConfiguration, variant)

    task.dependsOn(variant.name.toTaskName(prefix = "extract", suffix = "NativeSymbolTables"))
}

private fun configureCreateBuildTask(
    target: Project,
    bugsnag: VariantConfiguration,
    variant: AndroidVariant,
    execOperations: ExecOperations
) = Action<CreateBuildTask> { task ->
    task.group = TASK_GROUP
    task.globalOptions.configureFrom(bugsnag, execOperations)
    task.systemMetadata.configureFrom(target, bugsnag)
    task.metadata.set(bugsnag.metadata)
    task.variantMetadata.configureFrom(bugsnag, variant)
    task.androidManifestFile.set(variant.manifestFile)
    task.projectPath.set(task.project.projectDir.toString())
}

private fun configureUploadBundleTask(
    target: Project,
    bugsnag: VariantConfiguration,
    variant: AndroidVariant,
    execOperations: ExecOperations
) = Action<UploadBundleTask> { task ->
    configureBugsnagCliTask(task, bugsnag, execOperations)
    task.bundleFile.set(variant.bundleFile)

    val projectRoot = bugsnag.projectRoot ?: target.rootDir.toString()
    task.projectRoot.set(projectRoot)

    // make sure that the bundle is actually built first
    task.dependsOn(variant.bundleTaskName)
}

private fun configureAndroidTask(
    task: BugsnagCliTask,
    bugsnag: VariantConfiguration,
    variant: AndroidVariant,
    execOperations: ExecOperations
) {
    configureBugsnagCliTask(task, bugsnag, execOperations)

    if (task is HasAndroidOptions) {
        task.androidOptions.from(variant)
    }
}

private fun configureBugsnagCliTask(
    task: BugsnagCliTask,
    bugsnag: VariantConfiguration,
    execOperations: ExecOperations
) {
    task.group = TASK_GROUP
    task.globalOptions.configureFrom(bugsnag, execOperations)

    if (task is AbstractUploadTask) {
        task.uploadOptions.configureFrom(bugsnag)
    }
}

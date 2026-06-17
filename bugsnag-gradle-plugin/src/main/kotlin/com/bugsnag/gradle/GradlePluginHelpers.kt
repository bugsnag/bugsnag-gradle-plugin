package com.bugsnag.gradle

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.tasks.ExternalNativeBuildTask
import com.bugsnag.gradle.android.AndroidVariant
import com.bugsnag.gradle.android.CreateBuildTask
import com.bugsnag.gradle.android.ExtractBugsnagJniLibsTask
import com.bugsnag.gradle.android.UploadBundleTask
import com.bugsnag.gradle.android.UploadMappingTask
import com.bugsnag.gradle.android.UploadNativeSymbolsTask
import com.bugsnag.gradle.android.configureFrom
import com.bugsnag.gradle.dsl.VariantConfiguration
import com.bugsnag.gradle.util.wireFinalizer
import org.gradle.api.Project
import org.gradle.process.ExecOperations
import java.io.File

private fun resolveNdkDir(target: Project): File? {
    val androidExt = try {
        target.extensions.findByType(BaseExtension::class.java)
    } catch (e: NoClassDefFoundError) {
        // AGP is not present — log the caught error, so it isn't swallowed and
        // can be inspected in CI logs.
        target.logger.debug(
            "Android Gradle Plugin not present; cannot resolve ndkDirectory from BaseExtension",
            e
        )
        null
    }
    return androidExt?.ndkDirectory?.takeIf { it.exists() }
        ?: System.getenv("ANDROID_NDK_ROOT")?.let { File(it) }?.takeIf { it.exists() }
}

private fun configureTaskNdkRoot(
    task: UploadNativeSymbolsTask,
    target: Project,
    variantConfiguration: VariantConfiguration
) {
    // prefer explicit configuration from variant
    val ndkRootFromConfig = variantConfiguration.ndkRoot
    if (ndkRootFromConfig != null) {
        task.ndkRoot.set(ndkRootFromConfig)
        return
    }

    // otherwise resolve from Android extension or env var
    val ndkDir = resolveNdkDir(target)
    if (ndkDir != null) {
        task.ndkRoot.set(ndkDir)
        return
    }
    throw BugsnagCliException(
        "[FATAL] environment variable 'ANDROID_NDK_ROOT' not defined and no NDK directory " +
            "found via Android extension. Set ANDROID_NDK_ROOT or configure ndkRoot in the " +
            "bugsnag extension/variant configuration."
    )
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

internal fun registerBundleAndBuildTasks(
    target: Project,
    variantConfiguration: VariantConfiguration,
    variant: AndroidVariant,
    execOperations: ExecOperations
) {
    val uploadBundleTaskName = variant.name.toTaskName(
        prefix = UPLOAD_TASK_PREFIX,
        suffix = "Bundle"
    )
    if (target.tasks.findByName(uploadBundleTaskName) == null) {
        val uploadBundleTask = target.tasks.register(
            uploadBundleTaskName,
            UploadBundleTask::class.java
        ) { task ->
            configureBugsnagCliTask(task, variantConfiguration, execOperations)
            task.bundleFile.set(variant.bundleFile)
            val projectRoot = variantConfiguration.projectRoot ?: target.rootDir.toString()
            task.projectRoot.set(projectRoot)
            task.dependsOn(variant.bundleTaskName)
        }
        if (variantConfiguration.autoUploadBundle) {
            target.wireFinalizer(uploadBundleTask, variant.bundleTaskName)
        }
    }

    val createBuildTaskName = variant.name.toTaskName(
        prefix = CREATE_BUILD_TASK_PREFIX,
        suffix = "Build"
    )
    if (target.tasks.findByName(createBuildTaskName) == null) {
        val createBuildTask = target.tasks.register(
            createBuildTaskName,
            CreateBuildTask::class.java
        ) { task ->
            configureBugsnagCliTask(task, variantConfiguration, execOperations)
            task.metadata.set(variantConfiguration.metadata)
            task.variantMetadata.configureFrom(variantConfiguration, variant)
            task.androidManifestFile.set(variant.manifestFile)
            task.projectPath.set(task.project.projectDir.toString())
        }
        if (variantConfiguration.autoCreateBuild) {
            target.wireFinalizer(createBuildTask, variant.bundleTaskName)
        }
    }
}

internal fun registerProguardMappingTask(
    target: Project,
    variantConfiguration: VariantConfiguration,
    variant: AndroidVariant,
    execOperations: ExecOperations
) {
    if (variant.obfuscationMappingFile != null) {
        val proguardTaskName = variant.name.toTaskName(
            prefix = UPLOAD_TASK_PREFIX,
            suffix = "ProguardMapping"
        )
        if (target.tasks.findByName(proguardTaskName) == null) {
            target.tasks.register(
                proguardTaskName,
                UploadMappingTask::class.java
            ) { task ->
                configureBugsnagCliTask(task, variantConfiguration, execOperations)
                task.mappingFile.set(variant.obfuscationMappingFile)
                task.androidVariantMetadata.configureFrom(variantConfiguration, variant)
                variant.dexClassesDir?.let {
                    task.dexClassesDir.set(it)
                }
                variantConfiguration.buildUuid?.let {
                    task.buildUuid.set(it)
                }
            }
        }
    }
}

internal fun registerNativeSymbolsTask(
    target: Project,
    variantConfiguration: VariantConfiguration,
    variant: AndroidVariant,
    execOperations: ExecOperations
) {
    if (variant.nativeSymbols == null) return

    val nativeSymbolsTaskName = variant.name.toTaskName(
        prefix = UPLOAD_TASK_PREFIX,
        suffix = "NativeSymbols"
    )
    if (target.tasks.findByName(nativeSymbolsTaskName) != null) return

    // resolve values now so we can always set required properties at registration time
    val resolvedProjectRoot = variantConfiguration.projectRoot ?: target.rootDir.toString()
    val resolvedVariantName = variant.name

    target.tasks.register(
        nativeSymbolsTaskName,
        UploadNativeSymbolsTask::class.java
    ) { task ->
        configureBugsnagCliTask(task, variantConfiguration, execOperations)

        // ALWAYS set required non-optional properties so Gradle validation passes
        task.projectRoot.set(resolvedProjectRoot)
        task.androidVariantMetadata.variantName.set(resolvedVariantName)

        // if manifest missing we skip configuring files/ndk/upload but the task remains valid
        if (variant.manifestFile == null) {
            task.logger.warn(
                "Skipping native symbols upload for variant ${variant.name}: no AndroidManifest.xml located"
            )
            task.logger.warn(
                "Skipping native symbols upload for variant ${variant.name}: no AndroidManifest.xml located"
            )
            return@register
        }

        // configure files & metadata
        task.symbolFiles.from(variant.nativeSymbols)
        task.androidVariantMetadata.configureFrom(variantConfiguration, variant)

        // configure ndk root (throws clear error if not resolvable)
        configureTaskNdkRoot(task, target, variantConfiguration)

        task.dependsOn(
            variant.name.toTaskName(
                prefix = "extract",
                suffix = "NativeSymbolTables"
            )
        )
    }
}

fun registerNdkLibInstallTask(project: Project) {
    val ndkTasks = project.tasks.withType(ExternalNativeBuildTask::class.java)
    val cleanTasks = ndkTasks.filter { it.name.contains(CLEAN_TASK) }.toSet()
    val buildTasks = ndkTasks.filter { !it.name.contains(CLEAN_TASK) }.toSet()
    if (buildTasks.isNotEmpty()) {
        val ndkSetupTask = project.tasks.register(
            "bugsnagInstallJniLibsTask",
            ExtractBugsnagJniLibsTask::class.java
        ) { task ->
            task.group = TASK_GROUP
            val artifacts = ExtractBugsnagJniLibsTask.resolveBugsnagArtifacts(project)
            task.bugsnagArtifacts.from(artifacts)
        }
        ndkSetupTask.configure { it.mustRunAfter(cleanTasks) }
        buildTasks.forEach { it.dependsOn(ndkSetupTask) }
    }
}

package com.bugsnag.gradle

import com.bugsnag.gradle.android.AndroidVariant
import com.bugsnag.gradle.android.CreateBuildTask
import com.bugsnag.gradle.android.UploadBundleTask
import com.bugsnag.gradle.android.UploadMappingTask
import com.bugsnag.gradle.android.UploadNativeSymbolsTask
import com.android.build.gradle.tasks.ExternalNativeBuildTask
import com.bugsnag.gradle.android.ExtractBugsnagJniLibsTask
import com.bugsnag.gradle.android.onAndroidVariant
import com.bugsnag.gradle.dsl.BugsnagExtension
import com.bugsnag.gradle.dsl.VariantConfiguration
import com.bugsnag.gradle.util.wireFinalizer
import org.gradle.api.Project
import org.gradle.process.ExecOperations

private fun configurePlugin(bugsnag: BugsnagExtension, target: Project, execOperations: ExecOperations) {
    target.afterEvaluate {
        if (bugsnag.enabled && bugsnag.enableLegacyNativeExtraction) {
            registerNdkLibInstallTask(target)
        }
    }
    target.onAndroidVariant { variant: AndroidVariant ->
        val variantConfiguration = VariantConfiguration(
            bugsnag,
            bugsnag.variants.findByName(variant.name)
        )
        if (!variantConfiguration.enabled) {
            return@onAndroidVariant
        }
        registerBundleAndBuildTasks(target, variantConfiguration, variant, execOperations)
        registerProguardMappingTask(target, variantConfiguration, variant, execOperations)
        registerNativeSymbolsTask(target, variantConfiguration, variant, execOperations)
    }
}

private fun configureBugsnagCliTask(task: BugsnagCliTask, bugsnag: VariantConfiguration, execOperations: ExecOperations) {
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
    val uploadBundleTaskName = variant.name.toTaskName(prefix = UPLOAD_TASK_PREFIX, suffix = "Bundle")
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

    val createBuildTaskName = variant.name.toTaskName(prefix = CREATE_BUILD_TASK_PREFIX, suffix = "Build")
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
        val proguardTaskName = variant.name.toTaskName(prefix = UPLOAD_TASK_PREFIX, suffix = "ProguardMapping")
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
    if (variant.nativeSymbols != null) {
        val nativeSymbolsTaskName = variant.name.toTaskName(
            prefix = UPLOAD_TASK_PREFIX,
            suffix = "NativeSymbols"
        )
        if (target.tasks.findByName(nativeSymbolsTaskName) == null) {
            target.tasks.register(
                nativeSymbolsTaskName,
                UploadNativeSymbolsTask::class.java
            ) { task ->
                configureBugsnagCliTask(task, variantConfiguration, execOperations)
                task.symbolFiles.from(variant.nativeSymbols)
                val projectRoot = variantConfiguration.projectRoot ?: target.rootDir.toString()
                val ndkRoot = variantConfiguration.ndkRoot
                task.projectRoot.set(projectRoot)
                if (ndkRoot != null) {
                    task.ndkRoot.set(ndkRoot)
                }
                task.androidVariantMetadata.configureFrom(variantConfiguration, variant)
                task.dependsOn(
                    variant.name.toTaskName(
                        prefix = "extract",
                        suffix = "NativeSymbolTables"
                    )
                )
            }
        }
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
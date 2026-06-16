package com.bugsnag.gradle

import com.bugsnag.gradle.android.AndroidVariant
import com.bugsnag.gradle.android.CreateBuildTask
import com.bugsnag.gradle.android.UploadBundleTask
import com.bugsnag.gradle.android.UploadMappingTask
import com.bugsnag.gradle.android.UploadNativeSymbolsTask
import com.bugsnag.gradle.dsl.VariantConfiguration
import com.bugsnag.gradle.util.wireFinalizer
import org.gradle.api.Project

internal fun registerBundleAndBuildTasks(
    target: Project,
    variantConfiguration: VariantConfiguration,
    variant: AndroidVariant
) {
    val uploadBundleTaskName = variant.name.toTaskName(prefix = UPLOAD_TASK_PREFIX, suffix = "Bundle")
    if (target.tasks.findByName(uploadBundleTaskName) == null) {
        val uploadBundleTask = target.tasks.register(
            uploadBundleTaskName,
            UploadBundleTask::class.java
        ) { task ->
            configureUploadBundleTask(target, variantConfiguration, variant)(task)
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
            configureCreateBuildTask(target, variantConfiguration, variant)(task)
        }
        if (variantConfiguration.autoCreateBuild) {
            target.wireFinalizer(createBuildTask, variant.bundleTaskName)
        }
    }
}

internal fun registerProguardMappingTask(
    target: Project,
    variantConfiguration: VariantConfiguration,
    variant: AndroidVariant
) {
    if (variant.obfuscationMappingFile != null) {
        val proguardTaskName = variant.name.toTaskName(prefix = UPLOAD_TASK_PREFIX, suffix = "ProguardMapping")
        if (target.tasks.findByName(proguardTaskName) == null) {
            target.tasks.register(
                proguardTaskName,
                UploadMappingTask::class.java
            ) { task ->
                task.mappingFile.set(variant.obfuscationMappingFile)
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
    variant: AndroidVariant
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
                configureUploadNativeSymbolsTask(variantConfiguration, variant, target)(task)
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
internal fun configureUploadNativeSymbolsTask(
    variantConfiguration: VariantConfiguration,
    variant: AndroidVariant,
    target: Project
) = /*Action<UploadNativeSymbolsTask>*/ { task: UploadNativeSymbolsTask ->
    task.symbolFiles.from(variant.nativeSymbols)
    val projectRoot = variantConfiguration.projectRoot ?: target.rootDir.toString()
    val ndkRoot = variantConfiguration.ndkRoot
    task.projectRoot.set(projectRoot)
    if (ndkRoot != null) {
        task.ndkRoot.set(ndkRoot)
    }
    task.dependsOn(
        variant.name.toTaskName(
            prefix = "extract",
            suffix = "NativeSymbolTables"
        )
    )
}

@Suppress("UNUSED_PARAMETER")
internal fun configureCreateBuildTask(
    target: Project,
    bugsnag: VariantConfiguration,
    variant: AndroidVariant
) = /*Action<CreateBuildTask>*/ { task: CreateBuildTask ->
    task.group = TASK_GROUP
    task.metadata.set(bugsnag.metadata)
    task.androidManifestFile.set(variant.manifestFile)
    task.projectPath.set(task.project.projectDir.toString())
}

@Suppress("UNUSED_PARAMETER")
internal fun configureUploadBundleTask(
    target: Project,
    bugsnag: VariantConfiguration,
    variant: AndroidVariant
) = /*Action<UploadBundleTask>*/ { task: UploadBundleTask ->
    task.bundleFile.set(variant.bundleFile)
    val projectRoot = bugsnag.projectRoot ?: target.rootDir.toString()
    task.projectRoot.set(projectRoot)
    task.dependsOn(variant.bundleTaskName)
}
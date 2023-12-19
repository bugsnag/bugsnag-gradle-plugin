package com.bugsnag.gradle

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.bugsnag.gradle.android.*
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

internal const val TASK_GROUP = "BugSnag"
internal const val UPLOAD_TASK_PREFIX = "bugsnagUpload"

class GradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val bugsnag = target.extensions.create("bugsnag", BugsnagExtension::class.java)

        if (!bugsnag.enabled) {
            return
        }

        target.onAndroidVariant { variant: AndroidVariant ->
            target.tasks.register(
                variant.name.toTaskName(prefix = UPLOAD_TASK_PREFIX, suffix = "Bundle"),
                UploadBundleTask::class.java,
                configureUploadBundleTask(bugsnag, variant)
            )

            if (variant.obfuscationMappingFile != null) {
                target.tasks.register(
                    variant.name.toTaskName(prefix = UPLOAD_TASK_PREFIX, suffix = "ProguardMapping"),
                    UploadMappingTask::class.java
                ) { task ->
                    configureAndroidTask(task, bugsnag, variant)
                    task.mappingFile.set(variant.obfuscationMappingFile)
                    task.dexFile.set(variant.dexFile)
                    bugsnag.buildId?.let { task.buildId.set(it) }
                }
            }

            if (variant.nativeSymbols != null) {
                target.tasks.register(
                    variant.name.toTaskName(prefix = UPLOAD_TASK_PREFIX, suffix = "NativeSymbols"),
                    UploadNativeSymbolsTask::class.java
                ) { task ->
                    configureAndroidTask(task, bugsnag, variant)
                    task.symbolFiles.from(variant.nativeSymbols)
                }.dependsOn(variant.name.toTaskName(prefix = "extract", suffix = "NativeSymbolTables"))
            }
        }
    }

    private fun configureUploadBundleTask(bugsnag: BugsnagExtension, variant: AndroidVariant) =
        Action<UploadBundleTask> { task ->
            configureBugsnagCliTask(task, bugsnag)
            task.bundleFile.set(variant.bundleFile)

            // make sure that the bundle is actually built first
            task.dependsOn(variant.bundleTaskName)
        }

    private fun configureAndroidTask(task: AbstractAndroidTask, bugsnag: BugsnagExtension, variant: AndroidVariant) {
        configureBugsnagCliTask(task, bugsnag)
        task.androidOptions.from(variant)
    }

    private fun configureBugsnagCliTask(task: BugsnagCliTask, bugsnag: BugsnagExtension) {
        task.group = TASK_GROUP
        task.globalOptions.from(bugsnag)
    }
}

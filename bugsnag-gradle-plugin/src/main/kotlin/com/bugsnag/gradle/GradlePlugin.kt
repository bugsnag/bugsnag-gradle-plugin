package com.bugsnag.gradle

import com.bugsnag.gradle.android.*
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.process.ExecOperations
import javax.inject.Inject

internal const val TASK_GROUP = "BugSnag"
internal const val UPLOAD_TASK_PREFIX = "bugsnagUpload"
internal const val CREATE_BUILD_TASK_PREFIX = "bugsnagCreate"

class GradlePlugin @Inject constructor(
    private val execOperations: ExecOperations,
) : Plugin<Project> {
    override fun apply(target: Project) {
        val bugsnag = target.extensions.create("bugsnag", BugsnagExtension::class.java)
        if (!bugsnag.enabled) {
            return
        }

        configurePlugin(bugsnag, target)
    }

    private fun configurePlugin(bugsnag: BugsnagExtension, target: Project) {
        target.onAndroidVariant { variant: AndroidVariant ->
            if (!bugsnag.enabled) {
                return@onAndroidVariant
            }

            target.tasks.register(
                variant.name.toTaskName(prefix = UPLOAD_TASK_PREFIX, suffix = "Bundle"),
                UploadBundleTask::class.java,
                configureUploadBundleTask(target, bugsnag, variant)
            )

            target.tasks.register(
                variant.name.toTaskName(prefix = CREATE_BUILD_TASK_PREFIX, suffix = "Build"),
                CreateBuildTask::class.java,
                configureCreateBuildTask(target, bugsnag, variant)
            )

            if (variant.obfuscationMappingFile != null) {
                target.tasks.register(
                    variant.name.toTaskName(prefix = UPLOAD_TASK_PREFIX, suffix = "ProguardMapping"),
                    UploadMappingTask::class.java
                ) { task ->
                    configureAndroidTask(task, bugsnag, variant)
                    task.mappingFile.set(variant.obfuscationMappingFile)
                    task.androidVariantMetadata.configureFrom(bugsnag, variant)
                    variant.dexClassesDir?.let { task.dexClassesDir.set(it) }
                    bugsnag.buildUuid?.let { task.buildUuid.set(it) }
                }
            }

            if (variant.nativeSymbols != null) {
                target.tasks.register(
                    variant.name.toTaskName(prefix = UPLOAD_TASK_PREFIX, suffix = "NativeSymbols"),
                    UploadNativeSymbolsTask::class.java
                ) { task ->
                    configureAndroidTask(task, bugsnag, variant)
                    task.symbolFiles.from(variant.nativeSymbols)

                    val projectRoot = bugsnag.projectRoot ?: target.rootDir.toString()
                    task.projectRoot.set(projectRoot)
                    task.ndkRoot.set(bugsnag.ndkRoot)
                    task.androidVariantMetadata.configureFrom(bugsnag, variant)

                    task.dependsOn(variant.name.toTaskName(prefix = "extract", suffix = "NativeSymbolTables"))
                }
            }
        }
    }

    private fun configureCreateBuildTask(target: Project, bugsnag: BugsnagExtension, variant: AndroidVariant) =
        Action<CreateBuildTask> { task ->
            task.group = TASK_GROUP
            task.globalOptions.configureFrom(bugsnag, execOperations)
            task.systemMetadata.configureFrom(target, bugsnag)
            task.metadata.set(bugsnag.metadata)
            task.variantMetadata.configureFrom(bugsnag, variant)
            task.androidManifestFile.set(variant.manifestFile)
            task.projectPath.set(task.project.projectDir.toString())
        }

    private fun configureUploadBundleTask(target: Project, bugsnag: BugsnagExtension, variant: AndroidVariant) =
        Action<UploadBundleTask> { task ->
            configureBugsnagCliTask(task, bugsnag)
            task.bundleFile.set(variant.bundleFile)

            val projectRoot = bugsnag.projectRoot ?: target.rootDir.toString()
            task.projectRoot.set(projectRoot)

            // make sure that the bundle is actually built first
            task.dependsOn(variant.bundleTaskName)
        }

    private fun configureAndroidTask(task: AbstractAndroidTask, bugsnag: BugsnagExtension, variant: AndroidVariant) {
        configureBugsnagCliTask(task, bugsnag)
        task.androidOptions.from(variant)
    }

    private fun configureBugsnagCliTask(task: BugsnagCliTask, bugsnag: BugsnagExtension) {
        task.group = TASK_GROUP
        task.globalOptions.configureFrom(bugsnag, execOperations)
    }
}

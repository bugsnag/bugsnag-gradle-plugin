package com.bugsnag.gradle

import com.bugsnag.gradle.android.AndroidVariant
import com.bugsnag.gradle.android.UploadBundleTask
import com.bugsnag.gradle.android.UploadMappingTask
import com.bugsnag.gradle.android.onAndroidVariant
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
                configureUploadBundleTask(bugsnag, variant),
            )

            if (variant.obfuscationMappingFile != null) {
                target.tasks.register(
                    variant.name.toTaskName(prefix = UPLOAD_TASK_PREFIX, suffix = "ProguardMapping"),
                    UploadMappingTask::class.java,
                ) { task ->
                    task.group = TASK_GROUP
                    task.globalOptions.from(bugsnag)
                    task.mappingFile.set(variant.obfuscationMappingFile)
                }
            }
        }
    }

    private fun configureUploadBundleTask(bugsnag: BugsnagExtension, variant: AndroidVariant) =
        Action<UploadBundleTask> { task ->
            task.group = TASK_GROUP
            task.globalOptions.from(bugsnag)
            task.bundleFile.set(variant.bundleFile)

            // make sure that the bundle is actually built first
            task.dependsOn(variant.bundleTaskName)
        }
}

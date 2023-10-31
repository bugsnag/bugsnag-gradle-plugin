package com.bugsnag.gradle

import com.bugsnag.gradle.android.onAndroidVariant
import com.bugsnag.gradle.android.toTaskName
import org.gradle.api.Plugin
import org.gradle.api.Project

class GradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.onAndroidVariant { (variantName, bundleFile) ->
            target.tasks.register(
                variantName.toTaskName(prefix = "bugsnagUpload", suffix = "Bundle"),
                UploadBundleTask::class.java
            ) { task ->
                task.bundleFile.set(bundleFile)
            }
        }
    }
}

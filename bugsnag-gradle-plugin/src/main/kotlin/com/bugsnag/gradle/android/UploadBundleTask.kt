package com.bugsnag.gradle.android

import com.bugsnag.gradle.BugsnagCliTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

internal abstract class UploadBundleTask : BugsnagCliTask() {
    @get:InputFile
    abstract val bundleFile: RegularFileProperty

    @TaskAction
    fun performUpload() {
        execUpload("android-aab", bundleFile.get().asFile.toString())
    }
}

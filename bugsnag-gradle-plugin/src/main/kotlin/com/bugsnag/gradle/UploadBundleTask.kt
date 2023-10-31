package com.bugsnag.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

internal abstract class UploadBundleTask : BugsnagCliTask() {
    @get:InputFile
    abstract val bundleFile: RegularFileProperty

    @TaskAction
    fun performUpload() {
        exec("upload", "android-aab", bundleFile.get().asFile.toString())
    }
}

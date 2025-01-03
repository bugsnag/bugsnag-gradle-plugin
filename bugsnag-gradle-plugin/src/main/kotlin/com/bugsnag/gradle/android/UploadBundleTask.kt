package com.bugsnag.gradle.android

import com.bugsnag.gradle.AbstractUploadTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

internal abstract class UploadBundleTask : AbstractUploadTask() {
    @get:InputFile
    abstract val bundleFile: RegularFileProperty

    @get:Input
    abstract val projectRoot: Property<String>

    @TaskAction
    fun performUpload() {
        execUpload("android-aab", bundleFile.get().asFile.toString()) {
            if (globalOptions.uploadApiEndpointRootUrl.isPresent) {
                "upload-api-root-url" `=` globalOptions.uploadApiEndpointRootUrl.get()
            }
            "project-root" `=` projectRoot
        }
    }
}

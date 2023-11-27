package com.bugsnag.gradle.android

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

internal abstract class UploadMappingTask : AbstractAndroidTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val mappingFile: RegularFileProperty

    @TaskAction
    fun uploadFile() {
        execUpload("android-proguard", mappingFile.get().asFile.absolutePath)
    }
}

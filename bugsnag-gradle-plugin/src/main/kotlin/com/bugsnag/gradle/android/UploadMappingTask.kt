package com.bugsnag.gradle.android

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

internal abstract class UploadMappingTask : AbstractAndroidTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val mappingFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dexFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val buildId: Property<String>

    @TaskAction
    fun uploadFile() {
        var id = buildId.orNull
        if (id == null) {
            id = execForOutput {
                it.args("create-android-build-id", dexFile.get().asFile.absolutePath)
            }
        }
        execUpload(
            "android-proguard",
            "--build-uuid=$id",
            mappingFile.get().asFile.absolutePath
        )
    }
}

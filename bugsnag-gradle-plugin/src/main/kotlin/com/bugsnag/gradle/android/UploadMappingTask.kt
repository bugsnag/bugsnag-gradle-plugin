package com.bugsnag.gradle.android

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

internal abstract class UploadMappingTask : AbstractAndroidTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val mappingFile: RegularFileProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dexClassesDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val buildId: Property<String>

    @TaskAction
    fun uploadFile() {
        var id = buildId.orNull
        if (id == null) {
            id = execForOutput {
                it.args("create-android-build-id", dexClassesDir.get().asFile.absolutePath)
            }.trim()
        }

        execUpload(
            "android-proguard",
            "--build-uuid=$id",
            mappingFile.get().asFile.absolutePath
        )
    }
}

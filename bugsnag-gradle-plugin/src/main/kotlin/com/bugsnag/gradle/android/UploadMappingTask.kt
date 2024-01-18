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

    @get:Nested
    abstract val androidVariantMetadata: AndroidVariantMetadata

    @TaskAction
    fun uploadFile() {
        execUpload(
            "android-proguard",
            if (buildId.isPresent) "--build-id=${buildId.get()}"
            else "--dex-files=${dexClassesDir.get().asFile.absolutePath}",
            "--variant=${androidVariantMetadata.variantName.get()}",
            "--version-name=${androidVariantMetadata.versionName.get()}",
            "--version-code=${androidVariantMetadata.versionCode.get()}",
            mappingFile.get().asFile.absolutePath
        )
    }
}

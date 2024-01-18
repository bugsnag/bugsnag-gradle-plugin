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
    abstract val buildUuid: Property<String>

    @get:Nested
    abstract val androidVariantMetadata: AndroidVariantMetadata

    @TaskAction
    fun uploadFile() {
        execUpload("android-proguard", mappingFile.get().asFile.absolutePath) {
            "build-uuid" `=` buildUuid
            "dex-files" `=` dexClassesDir
            "variant" `=` androidVariantMetadata.variantName
            "version-name" `=` androidVariantMetadata.versionName
            "version-code" `=` androidVariantMetadata.versionCode.map { it.toString() }
        }
    }
}

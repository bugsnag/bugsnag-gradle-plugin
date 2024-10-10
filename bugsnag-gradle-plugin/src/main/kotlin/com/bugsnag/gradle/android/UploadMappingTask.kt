package com.bugsnag.gradle.android

import com.bugsnag.gradle.AbstractUploadTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

internal abstract class UploadMappingTask : AbstractUploadTask(), HasAndroidOptions {
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
            if (globalOptions.uploadApiEndpointRootUrl.isPresent) {
                "upload-api-root-url" `=` globalOptions.uploadApiEndpointRootUrl.get()
            }
            "build-uuid" `=` buildUuid
            "dex-files" `=` dexClassesDir
            "variant" `=` androidVariantMetadata.variantName
            "version-name" `=` androidVariantMetadata.versionName
            "version-code" `=` androidVariantMetadata.versionCode.map { it.toString() }

            androidOptions.addToCli(this)
        }
    }
}

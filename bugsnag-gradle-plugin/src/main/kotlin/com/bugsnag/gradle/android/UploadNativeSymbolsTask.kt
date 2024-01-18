package com.bugsnag.gradle.android

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

internal abstract class UploadNativeSymbolsTask : AbstractAndroidTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val symbolFiles: ConfigurableFileCollection

    @get:Input
    abstract val projectRoot: Property<String>

    @get:Nested
    abstract val androidVariantMetadata: AndroidVariantMetadata

    @TaskAction
    fun uploadMappingFiles() {
        symbolFiles.onEach { symFile ->
            execUpload(
                "android-ndk",
                "--project-root=${projectRoot.get()}",
                "--variant=${androidVariantMetadata.variantName.get()}",
                "--version-name=${androidVariantMetadata.versionName.get()}",
                "--version-code=${androidVariantMetadata.versionCode.get()}",
                symFile.absolutePath,
            )
        }
    }
}

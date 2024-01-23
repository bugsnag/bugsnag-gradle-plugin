package com.bugsnag.gradle.android

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.util.PatternSet

internal abstract class UploadNativeSymbolsTask : AbstractAndroidTask() {
    private val symbolFilePattern = PatternSet()
        .include("**/*.so.sym")
        .include("**/*.so")

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val symbolFiles: ConfigurableFileCollection

    @get:Input
    abstract val projectRoot: Property<String>

    @get:Input
    @get:Optional
    abstract val ndkRoot: Property<String>

    @get:Nested
    abstract val androidVariantMetadata: AndroidVariantMetadata

    @TaskAction
    fun uploadMappingFiles() {
        symbolFiles.asFileTree.matching(symbolFilePattern).onEach { symFile ->
            execUpload("android-ndk", symFile.absolutePath) {
                "project-root" `=` projectRoot
                "android-ndk-root" `=` ndkRoot
                "application-id" `=` androidVariantMetadata.applicationId
                "variant" `=` androidVariantMetadata.variantName
                "version-name" `=` androidVariantMetadata.versionName
                "version-code" `=` androidVariantMetadata.versionCode.map { it.toString() }
            }
        }
    }
}

package com.bugsnag.gradle.android

import com.bugsnag.gradle.AbstractUploadTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternSet

internal abstract class UploadNativeSymbolsTask : AbstractUploadTask(), HasAndroidOptions {
    private val symbolFilePattern = PatternSet()
        .include("**/*.so.sym")
        .include("**/*.so")

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val symbolFiles: ConfigurableFileCollection

    @get:Input
    abstract val projectRoot: Property<String>

    @get:Internal
    abstract val ndkRoot: DirectoryProperty

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

                androidOptions.addToCli(this)
            }
        }
    }
}

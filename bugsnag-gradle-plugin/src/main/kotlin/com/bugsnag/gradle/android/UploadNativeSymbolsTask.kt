package com.bugsnag.gradle.android

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.util.PatternSet

internal abstract class UploadNativeSymbolsTask : AbstractAndroidTask() {

    private val symbolFilePattern = PatternSet().include("**/*.so.sym")

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val symbolFiles: ConfigurableFileCollection

    @get:Input
    abstract val projectRoot: Property<String>

    @TaskAction
    fun uploadMappingFiles() {
        symbolFiles.asFileTree.matching(symbolFilePattern).onEach { symFile ->
            execUpload("android-ndk", "--project-root=${projectRoot.get()}", symFile.absolutePath)
        }
    }
}

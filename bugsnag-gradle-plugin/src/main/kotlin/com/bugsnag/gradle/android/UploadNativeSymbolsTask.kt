package com.bugsnag.gradle.android

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternSet

internal abstract class UploadNativeSymbolsTask : AbstractAndroidTask() {

    private val symbolFilePattern = PatternSet().include("**/*.so.sym")

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val symbolFiles: ConfigurableFileCollection

    @TaskAction
    fun uploadMappingFiles() {
        symbolFiles.asFileTree.matching(symbolFilePattern).onEach { symFile ->
            execUpload("android-ndk", symFile.absolutePath)
        }
    }
}

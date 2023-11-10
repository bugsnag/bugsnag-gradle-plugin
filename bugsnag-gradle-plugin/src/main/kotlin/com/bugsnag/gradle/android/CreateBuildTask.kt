package com.bugsnag.gradle.android

import com.bugsnag.gradle.BugsnagCliTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

internal abstract class CreateBuildTask : BugsnagCliTask(){

    @get:InputDirectory
    abstract val projectRoot: DirectoryProperty

    @get:Input
    abstract val versionName: Property<String>

    @get:Input
    abstract val versionCode: Property<Int>

    @TaskAction
    fun createBuild() {
        exec("create-build",
            "--version-name=${versionName.get()}",
            "--version-code=${versionCode.get()})",
            projectRoot.get().asFile.absolutePath)
    }
}
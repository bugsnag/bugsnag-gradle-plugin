package com.bugsnag.gradle.android

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.util.UUID

internal abstract class GenerateBuildIdTask : DefaultTask() {

    @get:Input
    @get:Optional
    abstract val buildUuid: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val uuid = buildUuid.orNull ?: UUID.randomUUID().toString()
        outputFile.get().asFile.writeText(uuid)
    }
}

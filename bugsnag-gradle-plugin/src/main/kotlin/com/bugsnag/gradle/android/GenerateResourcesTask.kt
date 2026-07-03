package com.bugsnag.gradle.android

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

internal abstract class GenerateResourcesTask : DefaultTask() {

    @get:InputFile
    abstract val buildUuidFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val buildUuid = buildUuidFile.get().asFile.readText().trim()
        val valuesDir = File(outputDirectory.get().asFile, "values")
        valuesDir.mkdirs()

        val resourceFile = File(valuesDir, "bugsnag_android_build_uuid.xml")
        resourceFile.writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <string name="com.bugsnag.android.BUILD_UUID" translatable="false">$buildUuid</string>
            </resources>
            """.trimIndent()
        )
    }
}

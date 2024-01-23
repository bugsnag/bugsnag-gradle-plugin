package com.bugsnag.gradle.android

import com.bugsnag.gradle.BugsnagCliTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional

interface AndroidOptions {
    @get:Optional
    @get:InputFile
    val appManifest: RegularFileProperty
}

internal fun AndroidOptions.addToCli(cliOptions: BugsnagCliTask.BugsnagCliBuilder) {
    cliOptions.apply {
        "app-manifest" `=` appManifest
    }
}

internal fun AndroidOptions.from(variant: AndroidVariant) {
    appManifest.set(variant.manifestFile)
}

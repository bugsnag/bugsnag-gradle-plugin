package com.bugsnag.gradle.android

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.process.ExecSpec

interface AndroidOptions {
    @get:Optional
    @get:InputFile
    val appManifest: RegularFileProperty
}

internal fun AndroidOptions.addToExecSpec(execSpec: ExecSpec) {
    if (appManifest.isPresent) {
        execSpec.args("--app-manifest=${appManifest.get().asFile.absolutePath}")
    }
}

internal fun AndroidOptions.from(variant: AndroidVariant) {
    appManifest.set(variant.manifestFile)
}

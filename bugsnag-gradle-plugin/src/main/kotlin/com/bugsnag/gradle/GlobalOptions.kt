package com.bugsnag.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.process.ExecSpec

interface GlobalOptions {
    @get:Optional
    @get:InputFile
    val executableFile: RegularFileProperty

    @get:Input
    @get:Optional
    val apiKey: Property<String>

    @get:Input
    @get:Optional
    val failOnUploadError: Property<Boolean>

    @get:Input
    @get:Optional
    val overwrite: Property<Boolean>

    @get:Input
    @get:Optional
    val timeout: Property<Int>

    @get:Input
    @get:Optional
    val retries: Property<Int>
}

internal fun GlobalOptions.addToExecSpec(execSpec: ExecSpec) {
    if (apiKey.isPresent) {
        execSpec.args("--api-key=${apiKey.get()}")
    }

    if (failOnUploadError.getOrElse(false)) {
        execSpec.args("--fail-on-upload-error")
    }
}

internal fun GlobalOptions.addToUploadExecSpec(execSpec: ExecSpec) {
    addToExecSpec(execSpec)

    if (overwrite.getOrElse(false)) {
        execSpec.args("--overwrite")
    }

    if (timeout.getOrElse(0) > 0) {
        execSpec.args("--timeout=${timeout.get()}")
    }

    if (retries.getOrElse(0) > 0) {
        execSpec.args("--retries=${retries.get()}")
    }
}

internal fun GlobalOptions.from(extension: BugsnagExtension) {
    extension.cliPath?.let { executableFile.set(it) }
    extension.timeout?.let { timeout.set(it) }
    extension.retries?.let { retries.set(it) }
    extension.apiKey?.let { apiKey.set(it) }
    failOnUploadError.set(extension.failOnUploadError)
    overwrite.set(extension.overwrite)
}

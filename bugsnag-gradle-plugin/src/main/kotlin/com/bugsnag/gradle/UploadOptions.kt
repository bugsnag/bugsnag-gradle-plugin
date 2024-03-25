package com.bugsnag.gradle

import com.bugsnag.gradle.dsl.BugsnagExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.process.ExecSpec

interface UploadOptions {
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

internal fun UploadOptions.addToExecSpec(execSpec: ExecSpec) {
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

internal fun UploadOptions.configureFrom(extension: BugsnagExtension) {
    extension.timeout?.let { timeout.set(it) }
    extension.retries?.let { retries.set(it) }
    overwrite.set(extension.overwrite)
}

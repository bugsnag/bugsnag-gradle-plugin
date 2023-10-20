package com.bugsnag.gradle

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.process.ExecSpec

interface GlobalOptions {
    @get:Input
    @get:Optional
    val apiKey: Property<String>

    @get:Input
    @get:Optional
    val uploadEndpoint: Property<String>

    @get:Input
    @get:Optional
    val buildEndpoint: Property<String>

    @get:Input
    @get:Optional
    val port: Property<Int>

    @get:Input
    @get:Optional
    val overwrite: Property<Boolean>
}

internal fun GlobalOptions.addToExecSpec(execSpec: ExecSpec) {
    if (apiKey.isPresent) {
        execSpec.args("--api-key=${apiKey.get()}")
    }

    if (uploadEndpoint.isPresent) {
        execSpec.args("--upload-api-root-url=${uploadEndpoint.get()}")
    }

    if (buildEndpoint.isPresent) {
        execSpec.args("--build-api-root-url=${buildEndpoint.get()}")
    }

    if (port.isPresent) {
        execSpec.args("--port=${port.get()}")
    }

    if (overwrite.getOrElse(false)) {
        execSpec.args("--overwrite")
    }
}

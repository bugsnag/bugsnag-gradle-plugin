package com.bugsnag.gradle

import com.bugsnag.gradle.dsl.BugsnagExtension
import com.bugsnag.gradle.util.NullOutputStream
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec

interface GlobalOptions {
    @get:Input
    val executableFile: Property<String>

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

    @get:Input
    @get:Optional
    val uploadApiEndpointRootUrl: Property<String>

    @get:Input
    @get:Optional
    val buildApiEndpointRootUrl: Property<String>

    @get:Input
    @get:Optional
    val port: Property<Int>
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

    if (uploadApiEndpointRootUrl.isPresent) {
        execSpec.args("--upload-api-root-url=${uploadApiEndpointRootUrl.get()}")
    }

    if (buildApiEndpointRootUrl.isPresent) {
        execSpec.args("--build-api-root-url=${buildApiEndpointRootUrl.get()}")
    }

    if (port.isPresent) {
        execSpec.args("--port=${port.get()}")
    }
}

internal fun GlobalOptions.configureFrom(extension: BugsnagExtension, execOperations: ExecOperations) {
    executableFile.set(extension.getCliExecutable(execOperations))

    extension.timeout?.let { timeout.set(it) }
    extension.retries?.let { retries.set(it) }
    extension.apiKey?.let { apiKey.set(it) }
    extension.uploadApiEndpointRootUrl?.let { uploadApiEndpointRootUrl.set(it) }
    extension.buildApiEndpointRootUrl?.let { buildApiEndpointRootUrl.set(it) }
    failOnUploadError.set(extension.failOnUploadError)
    overwrite.set(extension.overwrite)
}

private fun BugsnagExtension.getCliExecutable(execOperations: ExecOperations): String {
    if (cliPath == SYSTEM_CLI_FILE) {
        return systemCliIfInstalled(execOperations)
            ?: throw BugsnagCliException(
                "systemCli was specified, but no bugsnag-cli was not found on your path. " +
                    "See https://docs.bugsnag.com/build-integrations/bugsnag-cli/#installation " +
                    "for installation instructions."
            )
    }

    return cliPath ?: EmbeddedCliExtractor.embeddedCliPath
}

private fun systemCliIfInstalled(execOperations: ExecOperations): String? {
    return try {
        val exitValue = execOperations
            .exec {
                it.standardOutput = NullOutputStream
                it.errorOutput = NullOutputStream
                it.isIgnoreExitValue = true
                it.commandLine("bugsnag-cli", "--version")
            }
            .exitValue

        "bugsnag-cli".takeIf { exitValue == 0 }
    } catch (ex: Exception) {
        null
    }
}

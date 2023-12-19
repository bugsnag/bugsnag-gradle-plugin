package com.bugsnag.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Nested
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import java.io.ByteArrayOutputStream
import javax.inject.Inject

private const val CLI_LOG_INFO = "[INFO] "
private const val CLI_LOG_WARN = "[WARN] "
private const val CLI_LOG_ERROR = "[ERROR] "

internal const val SYSTEM_CLI_FILE = "\$PATH/bugsnag-cli"

internal abstract class BugsnagCliTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Nested
    abstract val globalOptions: GlobalOptions

    protected open fun exec(vararg args: String) {
        exec {
            it.executable(globalOptions.executableFile.get())
            globalOptions.addToExecSpec(it)
            it.args(*args)
        }
    }

    protected open fun execUpload(uploadType: String, vararg args: String) {
        exec {
            it.executable(globalOptions.executableFile.get())
            it.args("upload", uploadType)
            globalOptions.addToUploadExecSpec(it)
            it.args(*args)
        }
    }

    protected open fun execForOutput(spec: (ExecSpec) -> Unit): String {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val result = execResult(spec, stdout, stderr)
        execResultCheck(result, stdout, stderr)
        return stdout.toString("UTF-8")
    }

    protected open fun exec(spec: (ExecSpec) -> Unit) {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val result = execResult(spec, stdout, stderr)
        execResultCheck(result, stdout, stderr)
    }

    private fun execResultCheck(
        result: ExecResult?,
        stdout: ByteArrayOutputStream,
        stderr: ByteArrayOutputStream
    ) {
        if (result?.exitValue != 0) {
            throw BugsnagCliException(
                extractErrorMessage(
                    stdout.takeIf { it.size() > 0 }?.toByteArray()
                        ?: stderr.toByteArray()
                )
            )
        } else if (stdout.size() > 0) {
            relayCliLogging(stdout.toByteArray())
        }
    }

    private fun execResult(
        spec: (ExecSpec) -> Unit,
        stdout: ByteArrayOutputStream,
        stderr: ByteArrayOutputStream
    ): ExecResult? {
        val result = execOperations
            .exec {
                it.executable(globalOptions.executableFile.get())
                spec(it)

                it.standardOutput = stdout
                it.errorOutput = stderr
                it.isIgnoreExitValue = true
            }
        return result
    }

    private fun extractErrorMessage(bytes: ByteArray): String {
        return bytes
            .inputStream()
            .reader()
            .useLines { lines ->
                lines.filter { it.startsWith(CLI_LOG_ERROR) || it.contains("error:") }
                    .map { it.removePrefix(CLI_LOG_ERROR).substringAfter("error: ").trim() }
                    .joinToString("\n")
            }
    }

    private fun relayCliLogging(bytes: ByteArray) {
        bytes
            .inputStream()
            .reader()
            .forEachLine { line ->
                if (line.startsWith(CLI_LOG_INFO)) {
                    logger.info(line.substring(CLI_LOG_INFO.length))
                } else if (line.startsWith(CLI_LOG_WARN)) {
                    logger.warn(line.substring(CLI_LOG_WARN.length))
                } else if (line.startsWith(CLI_LOG_ERROR)) {
                    logger.error(line.substring(CLI_LOG_ERROR.length))
                }
            }
    }
}

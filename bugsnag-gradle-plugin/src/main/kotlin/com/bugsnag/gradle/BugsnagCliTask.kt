package com.bugsnag.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
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

    /**
     * Run the configured `bugsnag-cli` with the given arguments. The ordering of the arguments is always:
     * `bugsnag-cli <global-options> <args> <options>`.
     */
    protected open fun exec(vararg args: String, cliBuilder: BugsnagCliBuilder.() -> Unit) {
        exec {
            executable(globalOptions.executableFile.get())
            args(*args)
            cliBuilder()
            globalOptions.addToExecSpec(this)
        }
    }

    protected open fun execUpload(uploadType: String, file: String, cliBuilder: BugsnagCliBuilder.() -> Unit) {
        exec("upload", uploadType) {
            cliBuilder()
            +file
        }
    }

    protected open fun execForOutput(spec: BugsnagCliBuilder.() -> Unit): String {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val result = execForResult(spec, stdout, stderr)
        execResultCheck(result, stdout, stderr)
        return stdout.toString("UTF-8")
    }

    protected open fun exec(cliBuilder: BugsnagCliBuilder.() -> Unit) {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val result = execForResult(cliBuilder, stdout, stderr)
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

    private fun execForResult(
        cliBuilder: BugsnagCliBuilder.() -> Unit,
        stdout: ByteArrayOutputStream,
        stderr: ByteArrayOutputStream
    ): ExecResult? {
        val result = execOperations
            .exec {
                it.executable(globalOptions.executableFile.get())
                BugsnagCliBuilder(it).cliBuilder()

                it.standardOutput = stdout
                it.errorOutput = stderr
                it.isIgnoreExitValue = true
            }
        return result
    }

    private fun extractErrorMessage(bytes: ByteArray): String {
        val lines = bytes.inputStream().reader().readLines()

        val result = lines
            .asSequence()
            .filter { it.startsWith(CLI_LOG_ERROR) || it.contains("error:") }
            .map { it.removePrefix(CLI_LOG_ERROR).substringAfter("error: ").trim() }
            .joinToString("\n")

        return result.ifBlank { lines.joinToString("\n") }
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

    /**
     * Extended ExecSpec DSL to make creating option arguments from nullable-strings and `Providers` simpler.
     * Options can be added as:
     * ```kotlin
     * "api-key" `=` apiKeyProperty
     * ```
     * If the value of an argument is null or not present the option is omitted completely.
     */
    class BugsnagCliBuilder(private val delegate: ExecSpec) : ExecSpec by delegate {
        @JvmName("setString")
        @Suppress("FunctionNaming")
        infix fun String.`=`(value: String?) {
            if (value != null) {
                delegate.args("--$this=$value")
            }
        }

        @JvmName("setString")
        infix fun String.`=`(value: Provider<String>?) {
            if (value?.isPresent == true) {
                delegate.args("--$this=${value.get()}")
            }
        }

        @JvmName("setFile")
        infix fun String.`=`(value: Provider<out FileSystemLocation>?) {
            if (value?.isPresent == true) {
                delegate.args("--$this=${value.get().asFile.absolutePath}")
            }
        }

        /**
         * Alias for [args].
         */
        operator fun String.unaryPlus() {
            delegate.args(this)
        }
    }
}

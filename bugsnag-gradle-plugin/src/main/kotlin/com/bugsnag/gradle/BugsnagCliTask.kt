package com.bugsnag.gradle

import com.bugsnag.gradle.util.NullOutputStream
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Nested
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import javax.inject.Inject

internal abstract class BugsnagCliTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Nested
    abstract val globalOptions: GlobalOptions

    private val executable: String = getCliExecutable()

    protected open fun exec(vararg args: String) {
        exec {
            it.commandLine(executable)
            globalOptions.addToExecSpec(it)
            it.args(*args)
        }.assertNormalExitValue()
    }

    protected open fun execUpload(uploadType: String, vararg args: String) {
        exec {
            it.commandLine(executable)
            it.args("upload", uploadType)
            globalOptions.addToUploadExecSpec(it)
            it.args(*args)
        }.assertNormalExitValue()
    }

    protected open fun exec(spec: (ExecSpec) -> Unit): ExecResult {
        return execOperations
            .exec {
                it.commandLine(executable)
                spec(it)
            }
    }

    private fun getCliExecutable(): String {
        return globalOptions.executableFile.orNull?.asFile?.absolutePath
            ?: systemCliIfInstalled()
            ?: EmbeddedCliExtractor.embeddedCliPath
    }

    private fun systemCliIfInstalled(): String? {
        return try {
            val exitValue = execOperations
                .exec {
                    it.standardOutput = NullOutputStream
                    it.errorOutput = NullOutputStream
                    it.commandLine("bugsnag-cli").args("--version")
                }
                .exitValue

            "bugsnag-cli".takeIf { exitValue == 0 }
        } catch (ex: Exception) {
            null
        }
    }
}

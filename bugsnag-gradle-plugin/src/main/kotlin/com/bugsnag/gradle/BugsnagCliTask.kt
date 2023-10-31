package com.bugsnag.gradle

import com.bugsnag.gradle.util.NullOutputStream
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.StopExecutionException
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

internal abstract class BugsnagCliTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Nested
    abstract val globalOptions: GlobalOptions

    private val executable: String = getCliExecutable()

    protected fun exec(vararg args: String) {
        execOperations
            .exec {
                it.commandLine(executable)
                globalOptions.addToExecSpec(it)
                it.args(*args)
            }
            .assertNormalExitValue()
    }

    protected fun execUpload(uploadType: String, vararg args: String) {
        execOperations
            .exec {
                it.commandLine(executable)
                it.args("upload", uploadType)
                globalOptions.addToUploadExecSpec(it)
                it.args(*args)
            }
            .assertNormalExitValue()
    }

    private fun getCliExecutable(): String {
        return globalOptions.executableFile.orNull?.asFile?.absolutePath
            ?: systemCliIfInstalled()
            ?: embeddedCli()
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

    private fun embeddedCli(): String {
        val suffix = ".exe".takeIf { System.getProperty("os.name").contains("win", ignoreCase = true) } ?: ""
        val tmpFile = File.createTempFile("bugsnag-cli", suffix)

        extractPlatformExecutable(tmpFile)

        return tmpFile.absolutePath
    }

    private fun extractPlatformExecutable(destination: File) {
        val resourceName = getCliResourceName(
            System.getProperty("os.name").lowercase(),
            System.getProperty("os.arch").lowercase()
        )

        destination.outputStream().buffered().use { output ->
            BugsnagCliTask::class.java.getResourceAsStream(resourceName).use { it!!.copyTo(output) }
        }

        destination.setExecutable(true, true)
    }

    private fun getCliResourceName(osName: String, archName: String): String {
        val os = when {
            osName.contains("mac") -> "macos"
            osName.contains("linux") -> "linux"
            osName.contains("win") -> "windows"
            else -> throw StopExecutionException("Unsupported OS: $osName")
        }

        val arch = when (archName) {
            "amd64" -> "x86_64"
            "aarch64" -> "arm64"
            else -> archName
        }

        val suffix = when {
            osName.contains("win") -> ".exe"
            else -> ""
        }

        return "/$arch-$os-bugsnag-cli$suffix"
    }
}

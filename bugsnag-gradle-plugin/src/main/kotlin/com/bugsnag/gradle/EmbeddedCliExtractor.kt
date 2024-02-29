package com.bugsnag.gradle

import org.gradle.api.tasks.StopExecutionException
import java.io.File

/**
 * Handles the extraction of an embedded copy of the `bugsnag-cli` when required. This object is singleton to ensure
 * that any such CLI is only extracted once per build (assuming it's required at all).
 */
internal object EmbeddedCliExtractor {
    val embeddedCliPath: String = embeddedCli()

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

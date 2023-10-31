package com.bugsnag.gradle

import org.gradle.api.internal.file.IdentityFileResolver
import org.gradle.process.internal.DefaultExecSpec
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class GlobalOptionsTest {
    @Test
    fun testUploadOptions() {
        val options = TestGlobalOptions()
        options.apiKey.set("abc123")
        options.failOnUploadError.set(false)
        options.overwrite.set(true)
        options.timeout.set(987)
        options.retries.set(42)

        val execSpec = DefaultExecSpec(IdentityFileResolver())
        options.addToUploadExecSpec(execSpec)

        assertEquals(
            listOf("--api-key=abc123", "--overwrite", "--timeout=987", "--retries=42"),
            execSpec.args,
        )
    }

    @Test
    fun testFailOnUploadError() {
        val options = TestGlobalOptions()
        options.failOnUploadError.set(true)

        val execSpec = DefaultExecSpec(IdentityFileResolver())
        options.addToUploadExecSpec(execSpec)

        assertEquals(
            listOf("--fail-on-upload-error"),
            execSpec.args,
        )
    }

    @Test
    fun testFromExtension() {
        val options = TestGlobalOptions()
        options.from(BugsnagExtension().apply {
            cliPath = File("/hello-bugsnag-cli")
            failOnUploadError = false
            overwrite = true
            timeout = 987
            retries = 42
        })

        assertEquals(
            "/hello-bugsnag-cli",
            options.executableFile.get().asFile.absolutePath,
        )

        assertFalse(options.failOnUploadError.get())
        assertTrue(options.overwrite.get())

        assertEquals(987, options.timeout.get())
        assertEquals(42, options.retries.get())
    }
}

package com.bugsnag.gradle

import com.bugsnag.gradle.dsl.BugsnagExtension
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.internal.file.IdentityFileResolver
import org.gradle.api.model.ObjectFactory
import org.gradle.process.ExecOperations
import org.gradle.process.internal.DefaultExecSpec
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as whenever

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
        val objects = mock(ObjectFactory::class.java)
        val execOperations = mock(ExecOperations::class.java)

        whenever(objects.domainObjectContainer(any(Class::class.java)))
            .thenReturn(mock(NamedDomainObjectContainer::class.java))

        val bugsnag = BugsnagExtension(objects).apply {
            cliPath = "/hello-bugsnag-cli"
            failOnUploadError = false
            overwrite = true
            timeout = 987
            retries = 42
        }

        options.configureFrom(bugsnag, execOperations)

        assertEquals("/hello-bugsnag-cli", options.executableFile.get())
        assertFalse(options.failOnUploadError.get())
        assertTrue(options.overwrite.get())

        assertEquals(987, options.timeout.get())
        assertEquals(42, options.retries.get())
    }
}

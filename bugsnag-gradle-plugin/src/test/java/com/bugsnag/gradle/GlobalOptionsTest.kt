package com.bugsnag.gradle

import org.gradle.api.internal.file.IdentityFileResolver
import org.gradle.api.internal.provider.DefaultProperty
import org.gradle.api.internal.provider.PropertyHost
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.ExecOperations
import org.gradle.process.internal.DefaultExecSpec
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
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
        val providerFactory = mock(ProviderFactory::class.java)
        val execOperations = mock(ExecOperations::class.java)

        whenever(providerFactory.systemProperty(anyString()))
            .thenAnswer { DefaultProperty(PropertyHost.NO_OP, String::class.java) }

        whenever(objects.property(any(Class::class.java)))
            .thenAnswer { DefaultProperty(PropertyHost.NO_OP, it.arguments.first() as Class<*>) }

        val bugsnag = BugsnagExtension(objects, providerFactory).apply {
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

package com.bugsnag.gradle

import com.bugsnag.gradle.dsl.BugsnagExtension
import com.bugsnag.gradle.dsl.VariantConfiguration
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.internal.file.IdentityFileResolver
import org.gradle.api.model.ObjectFactory
import org.gradle.process.internal.DefaultExecSpec
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class UploadOptionsTest {
    @Test
    fun testUploadOptions() {
        val options = TestUploadOptions()
        options.overwrite.set(true)
        options.timeout.set(987)
        options.retries.set(42)

        val execSpec = DefaultExecSpec(IdentityFileResolver())
        options.addToExecSpec(execSpec)

        assertEquals(
            listOf("--overwrite", "--timeout=987", "--retries=42"),
            execSpec.args
        )
    }

    @Test
    fun testFromExtension() {
        val options = TestUploadOptions()
        val objects = Mockito.mock(ObjectFactory::class.java)

        Mockito.`when`(objects.domainObjectContainer(Mockito.any(Class::class.java)))
            .thenReturn(Mockito.mock(NamedDomainObjectContainer::class.java))

        val bugsnag = BugsnagExtension(objects).apply {
            cliPath = "/hello-bugsnag-cli"
            overwrite = true
            timeout = 987
            retries = 42
        }

        val config = VariantConfiguration(bugsnag)

        options.configureFrom(config)

        assertTrue(options.overwrite.get())

        assertEquals(987, options.timeout.get())
        assertEquals(42, options.retries.get())
    }
}

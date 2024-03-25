package com.bugsnag.gradle

import com.bugsnag.gradle.dsl.BugsnagExtension
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.internal.file.IdentityFileResolver
import org.gradle.api.model.ObjectFactory
import org.gradle.process.ExecOperations
import org.gradle.process.internal.DefaultExecSpec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as whenever

class GlobalOptionsTest {
    @Test
    fun testGloablOptions() {
        val options = TestGlobalOptions()
        options.apiKey.set("abc123")

        val execSpec = DefaultExecSpec(IdentityFileResolver())
        options.addToExecSpec(execSpec)

        assertEquals(
            listOf("--api-key=abc123"),
            execSpec.args
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
            overwrite = true
            timeout = 987
            retries = 42
        }

        options.configureFrom(bugsnag, execOperations)
        assertEquals("/hello-bugsnag-cli", options.executableFile.get())
    }
}

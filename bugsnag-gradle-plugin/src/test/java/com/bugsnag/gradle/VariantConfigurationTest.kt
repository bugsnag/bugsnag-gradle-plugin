package com.bugsnag.gradle

import com.bugsnag.gradle.dsl.BugsnagExtension
import com.bugsnag.gradle.dsl.BugsnagVariantExtension
import com.bugsnag.gradle.dsl.VariantConfiguration
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class VariantConfigurationTest {
    private fun extension(): BugsnagExtension {
        val objects = Mockito.mock(ObjectFactory::class.java)
        Mockito.`when`(objects.domainObjectContainer(Mockito.any(Class::class.java)))
            .thenReturn(Mockito.mock(NamedDomainObjectContainer::class.java))
        return BugsnagExtension(objects)
    }

    private fun variant(name: String = "release"): BugsnagVariantExtension =
        object : BugsnagVariantExtension() {
            override fun getName(): String = name
        }

    @Test
    fun topLevelValuesApplyWhenVariantBlockExists() {
        val bugsnag = extension().apply {
            releaseStage = "production"
            exclude = mutableListOf("**/*.map")
            sourceControl {
                it.provider = "github"
                it.repository = "https://github.com/example/repo"
                it.revision = "abc123"
            }
        }
        // a variant block that only sets an unrelated option
        val release = variant().apply { retries = 3 }

        val config = VariantConfiguration(bugsnag, release)

        assertEquals("production", config.releaseStage)
        assertEquals(listOf("**/*.map"), config.exclude)
        assertEquals("github", config.sourceControl.provider)
        assertEquals("https://github.com/example/repo", config.sourceControl.repository)
        assertEquals("abc123", config.sourceControl.revision)
    }

    @Test
    fun variantValuesOverrideTopLevelPerField() {
        val bugsnag = extension().apply {
            releaseStage = "production"
            exclude = mutableListOf("**/*.map")
            sourceControl {
                it.provider = "github"
                it.repository = "https://github.com/example/repo"
            }
        }
        val release = variant().apply {
            releaseStage = "beta"
            exclude = mutableListOf("libs/**")
            sourceControl { it.revision = "def456" }
        }

        val config = VariantConfiguration(bugsnag, release)

        assertEquals("beta", config.releaseStage)
        assertEquals(listOf("libs/**"), config.exclude)
        // provider and repository are inherited; revision comes from the variant
        assertEquals("github", config.sourceControl.provider)
        assertEquals("https://github.com/example/repo", config.sourceControl.repository)
        assertEquals("def456", config.sourceControl.revision)
    }

    @Test
    fun unsetValuesStayNull() {
        val config = VariantConfiguration(extension(), variant())

        assertNull(config.releaseStage)
        assertNull(config.sourceControl.provider)
        assertNull(config.sourceControl.repository)
        assertNull(config.sourceControl.revision)
    }
}

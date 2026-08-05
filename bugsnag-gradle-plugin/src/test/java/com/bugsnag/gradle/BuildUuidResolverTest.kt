package com.bugsnag.gradle

import com.bugsnag.gradle.dsl.BugsnagExtension
import com.bugsnag.gradle.dsl.BugsnagVariantExtension
import com.bugsnag.gradle.dsl.VariantConfiguration
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as whenever

class BuildUuidResolverTest {
    @Test
    fun testConfiguredBuildUuidIsUsedWhenGeneratorIsMissing() {
        val bugsnag = bugsnagExtension()
        bugsnag.buildUuid = "configured-uuid"

        val resolver = BuildUuidResolver(VariantConfiguration(bugsnag))

        assertEquals("configured-uuid", resolver.value)
    }

    @Test
    fun testGeneratorIsMemoizedAndOverridesConfiguredBuildUuid() {
        val bugsnag = bugsnagExtension().apply {
            buildUuid = "configured-uuid"
        }
        val variant = object : BugsnagVariantExtension() {
            override fun getName(): String = "release"
        }.apply {
            buildUuid = "variant-uuid"
        }

        var invocations = 0
        variant.buildUuidGenerator = {
            invocations += 1
            "generated-uuid"
        }

        val resolver = BuildUuidResolver(VariantConfiguration(bugsnag, variant))

        assertEquals("generated-uuid", resolver.value)
        assertEquals("generated-uuid", resolver.value)
        assertEquals(1, invocations)
    }

    @Test
    fun testVariantBuildUuidOverridesGlobalGenerator() {
        var globalInvocations = 0
        val bugsnag = bugsnagExtension().apply {
            buildUuidGenerator = {
                globalInvocations += 1
                "global-generated-uuid"
            }
        }
        val variant = object : BugsnagVariantExtension() {
            override fun getName(): String = "release"
        }.apply {
            buildUuid = "variant-uuid"
        }

        val resolver = BuildUuidResolver(VariantConfiguration(bugsnag, variant))

        assertEquals("variant-uuid", resolver.value)
        assertEquals(0, globalInvocations)
    }

    @Test
    fun testGlobalGeneratorIsUsedWhenVariantDoesNotOverrideIt() {
        var globalInvocations = 0
        val bugsnag = bugsnagExtension().apply {
            buildUuidGenerator = {
                globalInvocations += 1
                "global-generated-uuid"
            }
        }

        val resolver = BuildUuidResolver(VariantConfiguration(bugsnag))

        assertEquals("global-generated-uuid", resolver.value)
        assertEquals("global-generated-uuid", resolver.value)
        assertEquals(1, globalInvocations)
    }

    private fun bugsnagExtension(): BugsnagExtension {
        val objects = mock(ObjectFactory::class.java)
        whenever(objects.domainObjectContainer(any(Class::class.java)))
            .thenReturn(mock(NamedDomainObjectContainer::class.java))

        return BugsnagExtension(objects)
    }
}

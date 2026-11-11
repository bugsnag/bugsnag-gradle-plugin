package com.bugsnag.gradle

import com.bugsnag.gradle.android.addBuildOptions
import org.gradle.api.internal.file.IdentityFileResolver
import org.gradle.api.internal.provider.DefaultProperty
import org.gradle.api.internal.provider.PropertyHost
import org.gradle.api.provider.Property
import org.gradle.process.internal.DefaultExecSpec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CreateBuildOptionsTest {
    private fun stringProperty(value: String? = null): Property<String> =
        DefaultProperty(PropertyHost.NO_OP, String::class.java).also { prop ->
            value?.let { prop.set(it) }
        }

    private fun buildArgs(
        releaseStage: String? = null,
        provider: String? = null,
        repository: String? = null,
        revision: String? = null
    ): List<String> {
        val execSpec = DefaultExecSpec(IdentityFileResolver())
        BugsnagCliTask.BugsnagCliBuilder(execSpec).addBuildOptions(
            stringProperty(releaseStage),
            stringProperty(provider),
            stringProperty(repository),
            stringProperty(revision)
        )
        return execSpec.args
    }

    @Test
    fun noOptionsLeavesDefaultsToCliAndServer() {
        // no --release-stage, so the server default ("production") applies rather than the variant name
        assertTrue(buildArgs().isEmpty())
    }

    @Test
    fun allOptionsArePassed() {
        assertEquals(
            listOf(
                "--release-stage=staging",
                "--provider=github",
                "--repository=https://github.com/example/repo",
                "--revision=abc123"
            ),
            buildArgs(
                releaseStage = "staging",
                provider = "github",
                repository = "https://github.com/example/repo",
                revision = "abc123"
            )
        )
    }

    @Test
    fun neverPassesBuildUuid() {
        val args = buildArgs(releaseStage = "production", revision = "abc123")
        assertTrue(args.none { it.startsWith("--build-uuid") })
    }
}

package com.bugsnag.gradle.android

import com.bugsnag.gradle.BugsnagCliTask
import com.bugsnag.gradle.dsl.VariantConfiguration
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

internal abstract class CreateBuildTask : BugsnagCliTask() {
    @get:Optional
    @get:Input
    abstract val metadata: MapProperty<String, String>

    @get:Nested
    abstract val systemMetadata: SystemMetadata

    @get:Nested
    abstract val variantMetadata: AndroidVariantMetadata

    @get:InputFile
    abstract val androidManifestFile: RegularFileProperty

    @get:Input
    abstract val projectPath: Property<String>

    @get:Input
    @get:Optional
    abstract val releaseStage: Property<String>

    @get:Input
    @get:Optional
    abstract val provider: Property<String>

    @get:Input
    @get:Optional
    abstract val repository: Property<String>

    @get:Input
    @get:Optional
    abstract val revision: Property<String>

    @TaskAction
    fun createBuild() {
        val buildMetadata = systemMetadata.toMap() + metadata.orElse(emptyMap()).get()
        val metadataOption = buildMetadata.entries.joinToString(",") { (key, value) -> "$key=$value" }
        exec("create-build") {
            if (globalOptions.buildApiEndpointRootUrl.isPresent) {
                "build-api-root-url" `=` globalOptions.buildApiEndpointRootUrl.get()
            }
            "metadata" `=` metadataOption
            "app-manifest" `=` androidManifestFile
            "builder-name" `=` systemMetadata.builderName
            "version-name" `=` variantMetadata.versionName
            "version-code" `=` variantMetadata.versionCode.map { it.toString() }
            addBuildOptions(releaseStage, provider, repository, revision)

            +projectPath.get().toString()
        }
    }

    interface SystemMetadata {
        @get:Input
        @get:Optional
        val osArch: Property<String>

        @get:Input
        @get:Optional
        val osName: Property<String>

        @get:Input
        @get:Optional
        val osVersion: Property<String>

        @get:Input
        @get:Optional
        val javaVersion: Property<String>

        @get:Input
        @get:Optional
        val gradleVersion: Property<String>

        @get:Input
        @get:Optional
        val gitVersion: Property<String>

        @get:Optional
        @get:Input
        val builderName: Property<String>
    }
}

/**
 * Adds the release-stage and source-control options to a `create-build` command. Options without a value are
 * omitted entirely, so the CLI / server defaults apply (e.g. the `production` release stage and git
 * auto-detection). Note that `create-build` has no `--build-uuid` option, so it must never be passed here.
 */
internal fun BugsnagCliTask.BugsnagCliBuilder.addBuildOptions(
    releaseStage: Provider<String>,
    provider: Provider<String>,
    repository: Provider<String>,
    revision: Provider<String>
) {
    "release-stage" `=` releaseStage
    "provider" `=` provider
    "repository" `=` repository
    "revision" `=` revision
}

private const val PROP_OS_ARCH = "os.arch"
private const val PROP_OS_NAME = "os.name"
private const val PROP_OS_VERSION = "os.version"
private const val PROP_JAVA_VERSION = "java.version"
private const val PROP_USER_NAME = "user.name"

internal fun CreateBuildTask.SystemMetadata.configureFrom(project: Project, bugsnag: VariantConfiguration) {
    val providerFactory = project.providers

    osArch.set(providerFactory.systemProperty(PROP_OS_ARCH))
    osName.set(providerFactory.systemProperty(PROP_OS_NAME))
    osVersion.set(providerFactory.systemProperty(PROP_OS_VERSION))
    javaVersion.set(providerFactory.systemProperty(PROP_JAVA_VERSION))
    gradleVersion.set(project.gradle.gradleVersion)

    builderName.convention(providerFactory.systemProperty(PROP_USER_NAME))
    bugsnag.builderName?.let { builderName.set(it) }
}

internal fun CreateBuildTask.SystemMetadata.toMap(): Map<String, String?> =
    mapOf(
        "os_arch" to osArch.orNull,
        "os_name" to osName.orNull,
        "os_version" to osVersion.orNull,
        "java_version" to javaVersion.orNull,
        "gradle_version" to gradleVersion.orNull,
        "git_version" to gitVersion.orNull
    )

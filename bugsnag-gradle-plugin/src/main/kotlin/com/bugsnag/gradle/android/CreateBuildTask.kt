package com.bugsnag.gradle.android

import com.bugsnag.gradle.BugsnagCliTask
import com.bugsnag.gradle.BugsnagExtension
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
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

    @TaskAction
    fun createBuild() {
        val buildMetadata = systemMetadata.toMap() + metadata.orElse(emptyMap()).get()
        val metadataOption = buildMetadata.entries.joinToString(";") { (key, value) -> "$key=$value" }
        exec(
            "create-build",
            "--metadata=${metadataOption}",
            "--app-manifest=${androidManifestFile.get()}",
            "--builder-name=${systemMetadata.builderName.get()}",
            "--release-stage=${variantMetadata.variantName.get()}",
            "--version-name=${variantMetadata.versionName.get()}",
            "--version-code=${variantMetadata.versionCode.get()}",
            projectPath.get().toString()
        )
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

private const val PROP_OS_ARCH = "os.arch"
private const val PROP_OS_NAME = "os.name"
private const val PROP_OS_VERSION = "os.version"
private const val PROP_JAVA_VERSION = "java.version"

internal fun CreateBuildTask.SystemMetadata.configureFrom(project: Project, bugsnag: BugsnagExtension) {
    val providerFactory = project.providers

    osArch.set(providerFactory.systemProperty(PROP_OS_ARCH))
    osName.set(providerFactory.systemProperty(PROP_OS_NAME))
    osVersion.set(providerFactory.systemProperty(PROP_OS_VERSION))
    javaVersion.set(providerFactory.systemProperty(PROP_JAVA_VERSION))
    gradleVersion.set(project.gradle.gradleVersion)
    builderName.set(bugsnag.builderName)
}

internal fun CreateBuildTask.SystemMetadata.toMap(): Map<String, String?> =
    mapOf(
        "os_arch" to osArch.orNull,
        "os_name" to osName.orNull,
        "os_version" to osVersion.orNull,
        "java_version" to javaVersion.orNull,
        "gradle_version" to gradleVersion.orNull,
        "git_version" to gitVersion.orNull,
    )
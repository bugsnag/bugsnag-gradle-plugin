package com.bugsnag.gradle

import com.bugsnag.gradle.android.AndroidVariant
import com.bugsnag.gradle.android.GenerateBuildIdTask
import com.bugsnag.gradle.android.GenerateResourcesTask
import com.bugsnag.gradle.android.onAndroidVariant
import com.bugsnag.gradle.dsl.BugsnagExtension
import com.bugsnag.gradle.dsl.VariantConfiguration
import com.bugsnag.gradle.dsl.debug
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.process.ExecOperations
import javax.inject.Inject

class GradlePlugin @Inject constructor(
    private val execOperations: ExecOperations
) : Plugin<Project> {
    override fun apply(target: Project) {
        val bugsnag = target.extensions.create("bugsnag", BugsnagExtension::class.java)
        // turn-off the 'debug' variant by default
        bugsnag.variants.debug.enabled = false

        configurePlugin(bugsnag, target)
    }

    private fun configurePlugin(bugsnag: BugsnagExtension, target: Project) {
        // Defer legacy NDK extraction wiring until after evaluation
        target.afterEvaluate {
            if (bugsnag.enabled && bugsnag.enableLegacyNativeExtraction && hasAndroidPluginApplied(target)) {
                registerNdkLibInstallTask(target)
            }
        }

        // Per-variant registration only when Android plugin is present
        if (hasAndroidPluginApplied(target)) {
            target.onAndroidVariant { variant: AndroidVariant ->
                val variantConfiguration =
                    VariantConfiguration(bugsnag, bugsnag.variants.findByName(variant.name))

                if (!variantConfiguration.enabled) return@onAndroidVariant

                // Delegate to helpers (single source of truth)
                registerBundleAndBuildTasks(target, variantConfiguration, variant, execOperations)
                registerProguardMappingTask(target, variantConfiguration, variant, execOperations)
                registerNativeSymbolsTask(target, variantConfiguration, variant, execOperations)

                // Keep build id/resources generation local
                registerBuildIdGenerationTask(target, variant, variantConfiguration)
            }
        }
    }
}

private fun registerBuildIdGenerationTask(
    target: Project,
    variant: AndroidVariant,
    variantConfiguration: VariantConfiguration
): Provider<String> {
    val generateBuildIdTask = target.tasks.register(
        variant.name.toTaskName(prefix = "bugsnagGenerate", suffix = "BuildId"),
        GenerateBuildIdTask::class.java
    ) { task ->
        task.group = TASK_GROUP
        task.buildUuid.set(variantConfiguration.buildUuid)
        val buildIdFile = target.layout.buildDirectory.file(
            "intermediates/bugsnag/build-id-${variant.name}.txt"
        )
        task.outputFile.set(buildIdFile)
    }

    val buildUuidProvider = generateBuildIdTask.flatMap { it.outputFile }
        .map { it.asFile.readText().trim() }

    val variantResources = variant.variant
    // ... existing code ...
    val generateResourcesTask = target.tasks.register(
        variant.name.toTaskName(prefix = "bugsnagGenerate", suffix = "Resources"),
        GenerateResourcesTask::class.java
    ) { task ->
        task.group = TASK_GROUP
        task.buildUuidFile.set(generateBuildIdTask.flatMap { it.outputFile })
        val resDir = target.layout.buildDirectory.dir("generated/res/bugsnag/${variant.name}")
        task.outputDirectory.set(resDir)
    }

    // variant.variant already implements HasAndroidResources, so no need for an is-check
    variantResources.sources.res?.addGeneratedSourceDirectory(
        generateResourcesTask,
        GenerateResourcesTask::outputDirectory
    )
    return buildUuidProvider
}

private fun hasAndroidPluginApplied(project: Project): Boolean {
    return project.plugins.hasPlugin("com.android.application") ||
        project.plugins.hasPlugin("com.android.library")
}

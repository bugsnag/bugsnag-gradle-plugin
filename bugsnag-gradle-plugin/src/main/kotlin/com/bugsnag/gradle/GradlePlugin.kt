package com.bugsnag.gradle

import com.android.build.gradle.tasks.ExternalNativeBuildTask
import com.bugsnag.gradle.android.AndroidVariant
import com.bugsnag.gradle.android.ExtractBugsnagJniLibsTask
import com.bugsnag.gradle.android.onAndroidVariant
import com.bugsnag.gradle.dsl.BugsnagExtension
import com.bugsnag.gradle.dsl.VariantConfiguration
import com.bugsnag.gradle.dsl.debug
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.process.ExecOperations
import javax.inject.Inject

internal const val CLEAN_TASK = "Clean"

class GradlePlugin @Inject constructor(
    private val execOperations: ExecOperations
) : Plugin<Project> {
    override fun apply(target: Project) {
        val bugsnag = target.extensions.create("bugsnag", BugsnagExtension::class.java)
        // turn-off the 'debug' variant by default
        bugsnag.variants.debug.enabled = false
        configurePlugin(bugsnag, target, execOperations)
    }

    private fun configurePlugin(bugsnag: BugsnagExtension, target: Project, execOperations: ExecOperations) {
        target.afterEvaluate {
            if (bugsnag.enabled && bugsnag.enableLegacyNativeExtraction) {
                registerNdkLibInstallTask(target)
            }
        }
        target.onAndroidVariant { variant: AndroidVariant ->
            handleVariant(target, bugsnag, variant, execOperations)
        }
    }

    private fun handleVariant(
        target: Project,
        bugsnag: BugsnagExtension,
        variant: AndroidVariant,
        execOperations: ExecOperations
    ) {
        val variantConfiguration = VariantConfiguration(
            bugsnag,
            bugsnag.variants.findByName(variant.name)
        )
        if (!variantConfiguration.enabled) {
            return
        }
        registerBundleAndBuildTasks(target, variantConfiguration, variant, execOperations)
        registerProguardMappingTask(target, variantConfiguration, variant, execOperations)
        registerNativeSymbolsTask(target, variantConfiguration, variant, execOperations)
    }

    private fun registerNdkLibInstallTask(project: Project) {
        val ndkTasks = project.tasks.withType(ExternalNativeBuildTask::class.java)
        val cleanTasks = ndkTasks.filter { it.name.contains(CLEAN_TASK) }.toSet()
        val buildTasks = ndkTasks.filter { !it.name.contains(CLEAN_TASK) }.toSet()
        if (buildTasks.isNotEmpty()) {
            val ndkSetupTask = project.tasks.register(
                "bugsnagInstallJniLibsTask",
                ExtractBugsnagJniLibsTask::class.java
            ) { task ->
                task.group = TASK_GROUP
                val artifacts = ExtractBugsnagJniLibsTask.resolveBugsnagArtifacts(project)
                task.bugsnagArtifacts.from(artifacts)
            }
            ndkSetupTask.configure { it.mustRunAfter(cleanTasks) }
            buildTasks.forEach { it.dependsOn(ndkSetupTask) }
        }
    }
}

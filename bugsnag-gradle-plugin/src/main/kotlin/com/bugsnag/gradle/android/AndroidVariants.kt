package com.bugsnag.gradle.android

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

data class AndroidVariant(
    val name: String,
    val bundleFile: Provider<RegularFile>,
)

internal fun Project.onAndroidVariant(consumer: (variant: AndroidVariant) -> Unit) {
    val androidExtension = extensions.findByType(AndroidComponentsExtension::class.java)
    androidExtension?.onVariants { variant: Variant ->
        consumer(AndroidVariant(variant.name, variant.artifacts.get(SingleArtifact.BUNDLE)))
    }
}

internal fun String.capitalise(): String = replaceFirstChar { it.uppercase() }

internal fun String.toTaskName(prefix: String, suffix: String = "") = "$prefix${this.capitalise()}$suffix"

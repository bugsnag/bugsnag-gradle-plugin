package com.bugsnag.gradle.android

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.CanMinifyCode
import com.android.build.api.variant.Variant
import com.bugsnag.gradle.toTaskName
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

data class AndroidVariant(
    val name: String,
    val bundleFile: Provider<RegularFile>,
    /**
     * The provider pointing to the obfuscation mapping file (typically `mapping.txt`) or `null` if minification is
     * not enabled for this variant.
     */
    val obfuscationMappingFile: Provider<RegularFile>?,
) {
    val bundleTaskName: String
        get() = name.toTaskName(prefix = "bundle")
}

internal fun Project.isMinifyEnabledFor(variant: Variant): Boolean {
    return variant is CanMinifyCode && variant.isMinifyEnabled || hasDexguardPlugin()
}

internal fun Project.onAndroidVariant(consumer: (variant: AndroidVariant) -> Unit) {
    try {
        val androidExtension = extensions.findByType(AndroidComponentsExtension::class.java)
        androidExtension?.onVariants { variant: Variant ->
            consumer(
                AndroidVariant(
                    variant.name,
                    variant.artifacts.get(SingleArtifact.BUNDLE),
                    variant.artifacts
                        .get(SingleArtifact.OBFUSCATION_MAPPING_FILE)
                        .takeIf { isMinifyEnabledFor(variant) },
                ),
            )
        }
    } catch (ex: NoClassDefFoundError) {
        // ignore these - AGP is not available in this Project
    }
}

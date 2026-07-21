package com.bugsnag.gradle

import com.bugsnag.gradle.dsl.VariantConfiguration

internal class BuildUuidResolver(
    private val variantConfiguration: VariantConfiguration
) {
    val value: String? by lazy(LazyThreadSafetyMode.NONE) {
        variantConfiguration.resolveBuildUuid()
    }
}

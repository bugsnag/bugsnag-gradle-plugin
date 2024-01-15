package com.bugsnag.gradle.android

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

internal interface AndroidVariantMetadata {
    @get:Input
    val variantName: Property<String>

    @get:Input
    val versionName: Property<String>

    @get:Input
    val versionCode: Property<Int>
}

internal fun AndroidVariantMetadata.configureFrom(variant: AndroidVariant) {
    variantName.set(variant.name)
    variant.versionName?.let { versionName.set(it) }
    variant.versionCode?.let { versionCode.set(it) }
}

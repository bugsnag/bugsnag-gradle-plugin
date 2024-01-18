package com.bugsnag.gradle.android

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.process.ExecSpec

internal interface AndroidVariantMetadata {
    @get:Input
    val variantName: Property<String>

    @get:Input
    @get:Optional
    val versionName: Property<String>

    @get:Input
    @get:Optional
    val versionCode: Property<Int>
}

internal fun AndroidVariantMetadata.configureFrom(variant: AndroidVariant) {
    variantName.set(variant.name)
    variant.versionName?.let { versionName.set(it) }
    variant.versionCode?.let { versionCode.set(it) }
}

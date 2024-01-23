package com.bugsnag.gradle.android

import com.bugsnag.gradle.BugsnagExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

internal interface AndroidVariantMetadata {
    @get:Input
    val variantName: Property<String>

    @get:Input
    @get:Optional
    val versionName: Property<String>

    @get:Input
    @get:Optional
    val versionCode: Property<Int>

    @get:Input
    @get:Optional
    val applicationId: Property<String>
}

internal fun AndroidVariantMetadata.configureFrom(bugsnag: BugsnagExtension, variant: AndroidVariant) {
    variantName.set(variant.name)
    variant.applicationId?.let { applicationId.set(it) }

    val versionNameOverride = bugsnag.versionNameOverride
    if (versionNameOverride != null) {
        versionName.set(versionNameOverride)
    } else {
        variant.versionName?.let { versionName.set(it) }
    }

    val versionCodeOverride = bugsnag.versionCodeOverride
    if (versionCodeOverride != null) {
        versionCode.set(versionCodeOverride)
    } else {
        variant.versionCode?.let { versionCode.set(it) }
    }
}

package com.bugsnag.gradle.android

import org.gradle.api.Project

internal fun Project.hasDexguardPlugin(): Boolean {
    return pluginManager.hasPlugin("dexguard")
}

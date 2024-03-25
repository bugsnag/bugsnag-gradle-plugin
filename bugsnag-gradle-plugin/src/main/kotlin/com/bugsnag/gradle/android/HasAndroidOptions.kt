package com.bugsnag.gradle.android

import org.gradle.api.tasks.Nested

interface HasAndroidOptions {
    @get:Nested
    val androidOptions: AndroidOptions
}

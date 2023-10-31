package com.bugsnag.gradle

internal fun String.capitalise(): String = replaceFirstChar { it.uppercase() }

internal fun String.toTaskName(prefix: String, suffix: String = "") = "$prefix${this.capitalise()}$suffix"

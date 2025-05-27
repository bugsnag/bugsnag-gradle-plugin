package com.bugsnag.gradle.util

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

internal fun Project.wireFinalizer(finalizer: TaskProvider<out Task>, taskName: String) {
    afterEvaluate {
        tasks.findByName(taskName)?.finalizedBy(finalizer)
    }
}

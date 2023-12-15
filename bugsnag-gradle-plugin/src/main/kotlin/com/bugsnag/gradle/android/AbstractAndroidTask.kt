package com.bugsnag.gradle.android

import com.bugsnag.gradle.BugsnagCliTask
import org.gradle.api.tasks.Nested
import org.gradle.process.ExecSpec

internal abstract class AbstractAndroidTask : BugsnagCliTask() {
    @get:Nested
    abstract val androidOptions: AndroidOptions

    override fun exec(spec: (ExecSpec) -> Unit) {
        super.exec {
            spec(it)
            androidOptions.addToExecSpec(it)
        }
    }
}

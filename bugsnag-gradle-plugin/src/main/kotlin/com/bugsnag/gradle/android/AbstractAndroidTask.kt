package com.bugsnag.gradle.android

import com.bugsnag.gradle.BugsnagCliTask
import org.gradle.api.tasks.Nested

internal abstract class AbstractAndroidTask : BugsnagCliTask() {
    @get:Nested
    abstract val androidOptions: AndroidOptions

    override fun exec(vararg args: String, cliBuilder: BugsnagCliBuilder.() -> Unit) {
        super.exec(*args) {
            androidOptions.addToCli(this)
            cliBuilder()
        }
    }
}

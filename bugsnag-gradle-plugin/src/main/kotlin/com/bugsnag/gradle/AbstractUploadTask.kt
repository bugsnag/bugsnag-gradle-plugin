package com.bugsnag.gradle

import org.gradle.api.tasks.Nested

internal abstract class AbstractUploadTask : BugsnagCliTask() {
    @get:Nested
    abstract val uploadOptions: UploadOptions

    override fun exec(vararg args: String, cliBuilder: BugsnagCliBuilder.() -> Unit) {
        super.exec(*args) {
            cliBuilder()
            uploadOptions.addToExecSpec(this)
        }
    }
}

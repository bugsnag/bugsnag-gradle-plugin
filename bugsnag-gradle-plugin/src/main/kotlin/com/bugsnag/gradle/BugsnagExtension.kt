package com.bugsnag.gradle

import org.gradle.api.model.ObjectFactory
import java.io.File
import javax.inject.Inject

open class BugsnagExtension @Inject constructor(
    objects: ObjectFactory
) {
    /**
     * Whether the Bugsnag Plugin is enabled, setting this to `false` will deactivate the plugin completely.
     *
     * Defaults to `true`
     */
    var enabled: Boolean = true

    /**
     * Whether the build should fail when an upload fails.
     *
     * Defaults to `true`
     */
    var failOnUploadError: Boolean = true

    /**
     * If `true` overwrite any existing symbol files (`mapping.txt, `*.sym.so`, etc.) already uploaded.
     *
     * Defaults to `false`
     */
    var overwrite: Boolean = false

    /**
     * If a non-null positive value this is the number of seconds timeout for the upload commands to run.
     *
     * Defaults to `null`
     */
    var timeout: Int? = null

    /**
     * The number of retries to perform when attempting an upload. This can be set in cases where network reliability
     * might be a problem.
     *
     * Defaults to `null` (no retries)
     */
    var retries: Int? = null

    /**
     * Optionally the path of the `bugsnag-cli` executable, if not specified then the plugin will attempt to
     * use the packaged CLI tool. If you have a system-wide `bugsnag-cli` installed you can set this to [systemCli]
     * to use it.
     *
     * Defaults to `null`
     */
    var cliPath: String? = null

    /**
     * Optionally override the detected apiKey.
     *
     * Defaults to `null`
     */
    var apiKey: String? = null

    var uploadApiEndpointRootUrl: String? = null

    var buildApiEndpointRootUrl: String? = null

    var buildId: String? = null

    @Deprecated("replaced by uploadApiEndpointRootUrl", replaceWith = ReplaceWith("uploadApiEndpointRootUrl"))
    var endpoint: String? by ::uploadApiEndpointRootUrl

    @Deprecated("replaced by buildApiEndpointRootUrl", replaceWith = ReplaceWith("buildApiEndpointRootUrl"))
    var releasesEndpoint: String? by ::buildApiEndpointRootUrl

    /**
     * The project root to trim from the beginning of the native symbol filenames. This directly corresponds to the
     * `--project-root` option on `bugsnag-cli upload android-ndk` and `bugsnag-cli upload android-aab`.
     *
     * Defaults to the Gradle root-project directory
     */
    var projectRoot: String? = null

    /**
     * When [cliPath] is set to `systemCli` then the system-wide `bugsnag-cli` will be used (whatever is on your
     * `PATH` environment variable).
     */
    fun systemCli(): String = SYSTEM_CLI_FILE
}

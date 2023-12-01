package com.bugsnag.gradle

import java.io.File

open class BugsnagExtension {
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
     * locate a system-wide `bugsnag-cli` installation and if that is not found it will fall back to the packaged CLI
     * tool.
     *
     * Defaults to `null`
     */
    var cliPath: File? = null

    /**
     * Optionally override the detected apiKey.
     *
     * Defaults to `null`
     */
    var apiKey: String? = null

    var uploadApiEndpointRootUrl: String? = null

    var buildApiEndpointRootUrl: String? = null

    @Deprecated("replaced by uploadApiEndpointRootUrl", replaceWith = ReplaceWith("uploadApiEndpointRootUrl"))
    var endpoint: String? by ::uploadApiEndpointRootUrl

    @Deprecated("replaced by buildApiEndpointRootUrl", replaceWith = ReplaceWith("buildApiEndpointRootUrl"))
    var releasesEndpoint: String? by ::buildApiEndpointRootUrl
}

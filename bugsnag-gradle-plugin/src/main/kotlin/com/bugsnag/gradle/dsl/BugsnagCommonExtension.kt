package com.bugsnag.gradle.dsl

import java.io.File

interface BugsnagCommonExtension {
    /**
     * Whether the Bugsnag Plugin is enabled, setting this to `false` will deactivate the plugin completely.
     *
     * Defaults to `true`
     */
    var enabled: Boolean

    /**
     * Whether the build should fail when an upload fails.
     *
     * Defaults to `true`
     */
    var failOnUploadError: Boolean

    /**
     * If `true` overwrite any existing symbol files (`mapping.txt, `*.sym.so`, etc.) already uploaded.
     *
     * Defaults to `false`
     */
    var overwrite: Boolean

    /**
     * If a non-null positive value this is the number of seconds timeout for the upload commands to run.
     *
     * Defaults to `null`
     */
    var timeout: Int?

    /**
     * The number of retries to perform when attempting an upload. This can be set in cases where network reliability
     * might be a problem.
     *
     * Defaults to `null` (no retries)
     */
    var retries: Int?

    /**
     * Optionally override the detected apiKey.
     *
     * Defaults to `null`
     */
    var apiKey: String?

    var buildUuid: String?

    /**
     * Alias for [buildUuid]
     */
    var buildUUID: String?
        get() = buildUuid
        set(value) {
            buildUuid = value
        }

    /**
     * Optionally override the detected versionName. This is useful if you also override `Configuration.appVersion`.
     * @see [Configuration.appVersion](https://docs.bugsnag.com/platforms/android/configuration-options/#appversion)
     */
    var versionNameOverride: String?

    /**
     * Optionally override the detected versionCode. This is useful if you also override `Configuration.versionCode`.
     * @see [Configuration.versionCode](https://docs.bugsnag.com/platforms/android/configuration-options/#versioncode)
     */
    var versionCodeOverride: Int?

    var uploadApiEndpointRootUrl: String?

    var buildApiEndpointRootUrl: String?

    @Deprecated("replaced by uploadApiEndpointRootUrl", replaceWith = ReplaceWith("uploadApiEndpointRootUrl"))
    var endpoint: String?
        get() = uploadApiEndpointRootUrl
        set(value) {
            uploadApiEndpointRootUrl = value
        }

    @Deprecated("replaced by buildApiEndpointRootUrl", replaceWith = ReplaceWith("buildApiEndpointRootUrl"))
    var releasesEndpoint: String?
        get() = buildApiEndpointRootUrl
        set(value) {
            buildApiEndpointRootUrl = value
        }

    /**
     * The project root to trim from the beginning of the native symbol filenames. This directly corresponds to the
     * `--project-root` option on `bugsnag-cli upload android-ndk` and `bugsnag-cli upload android-aab`.
     *
     * Defaults to the Gradle root-project directory
     */
    var projectRoot: String?

    /**
     * Path to Android NDK installation ($ANDROID_NDK_ROOT is used if this is not set).
     */
    var ndkRoot: File?

    /**
     * Metadata to be included in builds / released on BugSnag. This will always include information gathered from
     * the build environment: os name, version and architecture, builder name, Java and Gradle version, and
     * source control information.
     */
    var metadata: MutableMap<String, String>?

    var builderName: String?
}

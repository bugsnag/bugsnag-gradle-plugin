package com.bugsnag.gradle.dsl

import com.bugsnag.gradle.SYSTEM_CLI_FILE
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class BugsnagExtension @Inject constructor(objects: ObjectFactory) : BugsnagCommonExtension {
    override var enabled: Boolean = true
    override var failOnUploadError: Boolean = true
    override var overwrite: Boolean = false
    override var timeout: Int? = null
    override var retries: Int? = null
    override var apiKey: String? = null
    override var buildUuid: String? = null
    override var versionNameOverride: String? = null
    override var versionCodeOverride: Int? = null
    override var uploadApiEndpointRootUrl: String? = null
    override var buildApiEndpointRootUrl: String? = null
    override var projectRoot: String? = null
    override var ndkRoot: String? = null
    override var metadata: MutableMap<String, String>? = LinkedHashMap()
    override var builderName: String? = null

    /**
     * Optionally the path of the `bugsnag-cli` executable, if not specified then the plugin will attempt to
     * use the packaged CLI tool. If you have a system-wide `bugsnag-cli` installed you can set this to [systemCli]
     * to use it.
     *
     * Defaults to `null`
     */
    var cliPath: String? = null

    /**
     * If `true` then the BugSnag NDK headers and libraries will be extracted for apps that require the BugSnag C
     * libraries (and `bugsnag.h`) but are not using
     * [prefabs](https://docs.bugsnag.com/platforms/android/#native-api-configuration).
     *
     * Defaults to `false`
     */
    var enableLegacyNativeExtraction: Boolean = false

    val variants: NamedDomainObjectContainer<BugsnagVariantExtension> =
        objects.domainObjectContainer(BugsnagVariantExtension::class.java)

    /**
     * When [cliPath] is set to `systemCli` then the system-wide `bugsnag-cli` will be used (whatever is on your
     * `PATH` environment variable).
     */
    fun systemCli(): String = SYSTEM_CLI_FILE
}

val NamedDomainObjectContainer<BugsnagVariantExtension>.debug: BugsnagVariantExtension
    get() = maybeCreate("debug")

fun NamedDomainObjectContainer<BugsnagVariantExtension>.debug(builder: BugsnagVariantExtension.() -> Unit) {
    debug.apply(builder)
}

val NamedDomainObjectContainer<BugsnagVariantExtension>.release: BugsnagVariantExtension
    get() = maybeCreate("release")

fun NamedDomainObjectContainer<BugsnagVariantExtension>.release(builder: BugsnagVariantExtension.() -> Unit) {
    release.apply(builder)
}

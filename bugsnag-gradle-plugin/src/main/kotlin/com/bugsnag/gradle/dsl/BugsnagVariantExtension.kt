package com.bugsnag.gradle.dsl

import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory

abstract class BugsnagVariantExtension : BugsnagCommonExtension, Named {
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
}

internal fun BugsnagVariantExtension.mergeWith(objects: ObjectFactory, root: BugsnagExtension): BugsnagExtension {
    return objects.newInstance(BugsnagExtension::class.java).also {
        it.enabled = enabled
        it.failOnUploadError = failOnUploadError
        it.overwrite = overwrite
        it.timeout = timeout ?: root.timeout
        it.retries = retries ?: root.retries
        it.apiKey = apiKey ?: root.apiKey
        it.buildUuid = buildUuid ?: root.buildUuid
        it.versionNameOverride = versionNameOverride ?: root.versionNameOverride
        it.versionCodeOverride = versionCodeOverride ?: root.versionCodeOverride
        it.uploadApiEndpointRootUrl = uploadApiEndpointRootUrl ?: root.uploadApiEndpointRootUrl
        it.buildApiEndpointRootUrl = buildApiEndpointRootUrl ?: root.buildApiEndpointRootUrl
        it.projectRoot = projectRoot ?: root.projectRoot
        it.ndkRoot = ndkRoot ?: root.ndkRoot
        it.metadata = metadata ?: root.metadata
        it.builderName = builderName ?: root.builderName

        it.variants.add(this)

        it.cliPath = root.cliPath
    }
}

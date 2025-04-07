package com.bugsnag.gradle.dsl

import org.gradle.api.Named
import java.io.File

abstract class BugsnagVariantExtension : BugsnagCommonExtension, Named {
    override var enabled: Boolean = true
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
    override var ndkRoot: File? = null
    override var metadata: MutableMap<String, String>? = LinkedHashMap()
    override var builderName: String? = null
}

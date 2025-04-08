package com.bugsnag.gradle.dsl

import java.io.File

internal class VariantConfiguration(
    private val extension: BugsnagExtension,
    private val variantExtension: BugsnagVariantExtension? = null
) {
    val enabled: Boolean get() = variantExtension?.enabled ?: extension.enabled
    val overwrite: Boolean get() = variantExtension?.overwrite ?: extension.overwrite
    val timeout: Int? get() = variantExtension?.timeout ?: extension.timeout
    val retries: Int? get() = variantExtension?.retries ?: extension.retries
    val apiKey: String? get() = variantExtension?.apiKey ?: extension.apiKey
    val buildUuid: String? get() = variantExtension?.buildUuid ?: extension.buildUuid
    val versionNameOverride: String? get() = variantExtension?.versionNameOverride ?: extension.versionNameOverride
    val versionCodeOverride: Int? get() = variantExtension?.versionCodeOverride ?: extension.versionCodeOverride
    val uploadApiEndpointRootUrl: String?
        get() = variantExtension?.uploadApiEndpointRootUrl
            ?: extension.uploadApiEndpointRootUrl
    val buildApiEndpointRootUrl: String?
        get() = variantExtension?.buildApiEndpointRootUrl
            ?: extension.buildApiEndpointRootUrl
    val projectRoot: String? get() = variantExtension?.projectRoot ?: extension.projectRoot
    val ndkRoot: File? get() = variantExtension?.ndkRoot ?: extension.ndkRoot
    val metadata: MutableMap<String, String>? = variantExtension?.metadata ?: extension.metadata
    val builderName: String? get() = variantExtension?.builderName ?: extension.builderName

    val autoUploadBundle: Boolean = variantExtension?.autoUploadBundle == true
    val autoCreateBuild: Boolean = variantExtension?.autoCreateBuild ?: variantExtension?.autoUploadBundle ?: false

    val cliPath: String? get() = extension.cliPath
    val enableLegacyNativeExtraction: Boolean get() = extension.enableLegacyNativeExtraction
}

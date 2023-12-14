package com.bugsnag.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.file.DefaultFilePropertyFactory
import org.gradle.api.internal.file.IdentityFileResolver
import org.gradle.api.internal.provider.DefaultProperty
import org.gradle.api.internal.provider.PropertyHost
import org.gradle.api.provider.Property

@Suppress("UNCHECKED_CAST")
internal class TestGlobalOptions : GlobalOptions, PropertyHost by PropertyHost.NO_OP {
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override val executableFile: RegularFileProperty =
        DefaultFilePropertyFactory(this, IdentityFileResolver(), null)
            .newFileProperty()

    override val apiKey: Property<String> = DefaultProperty(this, String::class.java)
    override val failOnUploadError: Property<Boolean> =
        DefaultProperty(this, java.lang.Boolean::class.java) as Property<Boolean>
    override val overwrite: Property<Boolean> =
        DefaultProperty(this, java.lang.Boolean::class.java) as Property<Boolean>
    override val timeout: Property<Int> =
        DefaultProperty(this, java.lang.Integer::class.java) as Property<Int>
    override val retries: Property<Int> =
        DefaultProperty(this, java.lang.Integer::class.java) as Property<Int>
    override val uploadApiEndpointRootUrl: Property<String> = DefaultProperty(this, String::class.java)
    override val buildApiEndpointRootUrl: Property<String> = DefaultProperty(this, String::class.java)
    override val port: Property<Int> = DefaultProperty(this, java.lang.Integer::class.java) as Property<Int>
}

package com.bugsnag.gradle

import org.gradle.api.internal.provider.DefaultProperty
import org.gradle.api.internal.provider.PropertyHost
import org.gradle.api.provider.Property

@Suppress("UNCHECKED_CAST")
internal class TestGlobalOptions : GlobalOptions, PropertyHost by PropertyHost.NO_OP {
    override val executableFile: Property<String> = DefaultProperty(this, String::class.java)
    override val apiKey: Property<String> = DefaultProperty(this, String::class.java)
    override val uploadApiEndpointRootUrl: Property<String> = DefaultProperty(this, String::class.java)
    override val buildApiEndpointRootUrl: Property<String> = DefaultProperty(this, String::class.java)
    override val port: Property<Int> = DefaultProperty(this, java.lang.Integer::class.java) as Property<Int>
}

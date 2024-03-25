package com.bugsnag.gradle

import org.gradle.api.internal.provider.DefaultProperty
import org.gradle.api.internal.provider.PropertyHost
import org.gradle.api.provider.Property

@Suppress("UNCHECKED_CAST")
internal class TestUploadOptions : UploadOptions, PropertyHost by PropertyHost.NO_OP {
    override val overwrite: Property<Boolean> =
        DefaultProperty(this, java.lang.Boolean::class.java) as Property<Boolean>
    override val timeout: Property<Int> =
        DefaultProperty(this, Integer::class.java) as Property<Int>
    override val retries: Property<Int> =
        DefaultProperty(this, Integer::class.java) as Property<Int>
}

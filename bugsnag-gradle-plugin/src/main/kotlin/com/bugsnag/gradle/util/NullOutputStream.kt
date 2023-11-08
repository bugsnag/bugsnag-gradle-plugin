package com.bugsnag.gradle.util

import java.io.OutputStream

internal object NullOutputStream : OutputStream() {
    override fun write(b: Int) = Unit
    override fun write(b: ByteArray) = Unit
    override fun write(b: ByteArray, off: Int, len: Int) = Unit
}

plugins {
    id("com.android.application") version (System.getenv("AGP_VERSION") ?: "9.2.1") apply false
    id("com.bugsnag.gradle") version "9000.0.0-test" apply false
}

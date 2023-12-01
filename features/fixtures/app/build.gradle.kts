plugins {
    id("com.android.application") version (System.getenv("AGP_VERSION") ?: "8.1.2") apply false
    id("com.bugsnag.gradle") version "9000.0.0-test" apply false
}

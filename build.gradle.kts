plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm") version "1.8.10" apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.2.0" apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

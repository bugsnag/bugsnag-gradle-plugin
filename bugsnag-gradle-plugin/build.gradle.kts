plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
    id("signing")

    id("com.github.hierynomus.license")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

group = "com.bugsnag"
version = project.findProperty("VERSION_NAME") ?: "1.0-SNAPSHOT"

dependencies {
    compileOnly("com.android.tools.build:gradle:8.0.0")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.10")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-all:2.0.2-beta")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

java {
    targetCompatibility = JavaVersion.VERSION_11
}

val bugsnagCliDir = File(rootProject.projectDir, "bugsnag-cli")

/**
 * makeCli builds all of the `bugsnag-cli` binaries allowing them to be directly included in the Gradle plugin
 */
val makeCli = tasks.register<Exec>("makeCli") {
    workingDir(bugsnagCliDir)
    commandLine("make")
    args("build-all")
}

tasks.processResources {
    dependsOn(makeCli)
    from(File(bugsnagCliDir, "bin"))
}

gradlePlugin {
    plugins {
        val bugsnagPlugin by creating {
            id = "com.bugsnag.gradle"
            implementationClass = "com.bugsnag.gradle.GradlePlugin"
        }
    }
}

publishing {
    publications {
    }
}

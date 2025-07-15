plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
    id("signing")
    id("com.gradle.plugin-publish")

    id("com.github.hierynomus.license")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.vanniktech.maven.publish") version "0.32.0"
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
    jvmToolchain(17)
}

java {
    targetCompatibility = JavaVersion.VERSION_17
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
    website.set(project.findProperty("POM_URL")?.toString())
    vcsUrl.set(project.findProperty("POM_SCM_URL")?.toString())

    plugins {
        create("bugsnagPlugin") {
            id = "com.bugsnag.gradle"
            displayName = project.property("POM_NAME").toString()
            description = project.property("POM_DESCRIPTION").toString()
            implementationClass = "com.bugsnag.gradle.GradlePlugin"
            tags.set(listOf("bugsnag", "proguard", "android", "upload"))
        }
    }
}

// license checking
license {
    header = rootProject.file("LICENSE")
    ignoreFailures = true
}

java {
    withJavadocJar()
    withSourcesJar()
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name = "Bugsnag Gradle Plugin"
        description = "Gradle plugin to automatically upload mapping files to BugSnag"
        inceptionYear = "2024"
        url = "https://github.com/bugsnag/bugsnag-gradle-plugin"
        licenses {
            license {
                name = "The MIT License"
                url = "https://opensource.org/licenses/MIT"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "bugsnag"
                name = "Bugsnag Team"
                email = "support@bugsnag.com"
            }
        }
        scm {
            url = "https://github.com/bugsnag/bugsnag-gradle-plugin"
            connection = "scm:git:git://github.com/bugsnag/bugsnag-gradle-plugin.git"
            developerConnection = "scm:git:ssh://git@github.com/bugsnag/bugsnag-gradle-plugin.git"
        }
    }
}

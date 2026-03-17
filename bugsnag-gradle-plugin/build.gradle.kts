plugins {
    id("java-gradle-plugin")
    id("maven-publish")
    id("signing")

    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gradle.pluginPublish)

    alias(libs.plugins.license)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

group = "com.bugsnag"
version = project.findProperty("VERSION_NAME") ?: "1.0-SNAPSHOT"

dependencies {
    compileOnly(libs.android.plugin)

    implementation(libs.kotlin.stdlib)

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.mockito)
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

publishing {
    repositories {
        maven {
            if (project.findProperty("VERSION_NAME")?.toString()?.contains("SNAPSHOT") == true) {
                setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
            } else {
                setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            }

            credentials {
                username = project.findProperty("NEXUS_USERNAME")?.toString() ?: System.getenv("NEXUS_USERNAME")
                password = project.findProperty("NEXUS_PASSWORD")?.toString() ?: System.getenv("NEXUS_PASSWORD")
            }
        }
    }

    afterEvaluate {
        publications {
            publications {
                getByName<MavenPublication>("pluginMaven") {
                    groupId = "com.bugsnag"
                    artifactId = project.property("POM_ARTIFACT_ID").toString()

                    pom {
                        name = project.property("POM_NAME").toString()
                        description = project.property("POM_DESCRIPTION").toString()
                        url = project.property("POM_URL").toString()

                        licenses {
                            license {
                                name = project.property("POM_LICENCE_NAME")?.toString()
                                url = project.property("POM_LICENCE_URL")?.toString()
                            }
                        }

                        developers {
                            developer {
                                id = project.property("POM_DEVELOPER_ID")?.toString()
                                name = project.property("POM_DEVELOPER_NAME")?.toString()
                            }
                        }

                        scm {
                            connection = project.property("POM_SCM_CONNECTION")?.toString()
                            developerConnection = project.property("POM_SCM_DEV_CONNECTION")?.toString()
                            url = project.property("POM_SCM_URL")?.toString()
                        }
                    }
                }
            }
        }
    }
}

afterEvaluate {
    signing {
        sign(publishing.publications["pluginMaven"])
    }
}

// Workaround for https://github.com/gradle/gradle/issues/15568
tasks.withType<AbstractPublishToMaven>().configureEach {
    mustRunAfter(tasks.withType<Sign>())
}

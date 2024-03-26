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
        create("bugsnagPlugin") {
            id = "com.bugsnag.gradle"
            implementationClass = "com.bugsnag.gradle.GradlePlugin"
        }
    }
}

// license checking
license {
    header = rootProject.file("LICENSE")
    ignoreFailures = true
}

project.afterEvaluate {
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

        publications {
            publications {
                create("BugsnagGradlePlugin", MavenPublication::class) {
                    from(components["java"])
                    groupId = "com.bugsnag"
                    artifactId = project.property("artefactId").toString()

                    pom {
                        name = project.property("pomName").toString()
                        description = project.property("POM_DESCRIPTION").toString()
                        uri(project.property("POM_URL").toString())

                        licenses {
                            license {
                                name = project.property("POM_LICENCE_NAME")?.toString()
                                url = project.property("POM_LICENCE_URL")?.toString()
                                description = project.property("POM_LICENCE_DIST")?.toString()
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

plugins {
    id("com.android.application")
    id("com.bugsnag.gradle")
}

android {
    namespace = "com.example.fixture"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.fixture"
        minSdk = 24
        targetSdk = 33
        versionCode = System.getenv("APP_VERSION_CODE")?.toInt() ?: 1
        versionName = System.getenv("APP_VERSION_NAME") ?: "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

val mazeRunnerPort = System.getenv("MAZE_RUNNER_PORT") ?: "9339"

bugsnag {
    uploadApiEndpointRootUrl = "http://localhost:$mazeRunnerPort/builds"
    buildApiEndpointRootUrl = "http://localhost:$mazeRunnerPort/builds"

    builderName = "test_user"

    System.getenv("PROJECT_ROOT")?.let { projectRoot = File(project.rootDir, it).toString() }
    System.getenv("BUILD_UUID")?.let { buildUuid = it }

    System.getenv("VERSION_NAME_OVERRIDE")?.let { versionNameOverride = it }
    System.getenv("VERSION_CODE_OVERRIDE")?.let { versionCodeOverride = it.toInt() }
}

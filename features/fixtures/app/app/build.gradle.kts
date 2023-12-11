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

bugsnag {
    uploadApiEndpointRootUrl = "http://localhost:9339/builds"
    buildApiEndpointRootUrl = "http://localhost:9339/builds"
}

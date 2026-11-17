plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.tvpower"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tvpower"
        minSdk = 24          // 安卓 7.0 起步，兼容安卓 9 盒子
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // 极简、跨版本稳定的 APK 命名：TvPower-v1.0-debug.apk
        setProperty("archivesBaseName", "TvPower-v${versionName}")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
}

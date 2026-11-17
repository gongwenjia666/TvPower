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

    // 自定义输出 APK 文件名：TvPower_debug_v1.0.apk / TvPower_release_v1.0.apk
    applicationVariants.all {
        variant.outputs.all {
            outputFileName = "TvPower_${variant.name}_v${variant.versionName}.apk"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
}

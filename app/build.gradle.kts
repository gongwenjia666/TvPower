plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// ---------- 版本号自动递增 ----------
// CI 上取 GitHub Actions 的 run number，每次构建 +1；本地取 git 提交数作为兜底
val buildNumber: Int =
    System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()
        ?: providers.exec {
            commandLine("git", "rev-list", "--count", "HEAD")
        }.standardOutput.asText.get().trim().toIntOrNull()
        ?: 1

val baseVersion = "1.0"

android {
    namespace = "com.tvpower"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tvpower"
        minSdk = 24          // 安卓 7.0 起步，兼容安卓 9 盒子
        targetSdk = 34

        versionCode = buildNumber                    // 必须是单调递增的整数
        versionName = "$baseVersion.$buildNumber"    // 形如 1.0.12

        setProperty("archivesBaseName", "TvPower")   // 产物名：TvPower-release.apk
    }

    signingConfigs {
        create("release") {
            // 有正式签名材料就用正式的；没有则留空，由下面的 release 构建回退到 debug 签名
            val storePath = System.getenv("RELEASE_KEYSTORE_PATH")
            if (!storePath.isNullOrBlank()) {
                storeFile = file(storePath)
                storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
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
            signingConfig =
                if (!System.getenv("RELEASE_KEYSTORE_PATH").isNullOrBlank()) {
                    signingConfigs.getByName("release")
                } else {
                    signingConfigs.getByName("debug")  // 回退签名，保证产物可直接安装
                }
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

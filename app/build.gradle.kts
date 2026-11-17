import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// ---------- 日期版本号：2026.11.17.v1 ----------
// 版本名由 CI 计算通过环境变量传入（同日递增 v2、v3…，跨日重置）；
// 本地直接构建时退化为「当天日期 + .v1」。
val cnToday: LocalDate = LocalDate.now(ZoneId.of("Asia/Shanghai"))
val datePart = cnToday.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
val fallbackVersionCode = cnToday.format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInt() * 100 + 1

val resolvedVersionName = System.getenv("VERSION_NAME")?.takeIf { it.isNotBlank() } ?: "$datePart.v1"
val resolvedVersionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: fallbackVersionCode

// Android 的 versionCode 是 int，上限 2,100,000,000；超了会在安装/上架时直接报错
require(resolvedVersionCode in 1..2_100_000_000) {
    "versionCode=${resolvedVersionCode} 超出 Android 允许范围"
}

android {
    namespace = "com.tvpower"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tvpower"
        minSdk = 24          // 安卓 7.0 起步，兼容安卓 9 盒子
        targetSdk = 34

        versionCode = resolvedVersionCode            // 2026111701 这种 10 位整数，单调递增
        versionName = resolvedVersionName            // 2026.11.17.v1

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

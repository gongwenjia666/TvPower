import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// ---------- 日期版本号：v2026.11.18_1 ----------
// 版本名由 CI 计算通过环境变量传入（同日递增 _2、_3…，跨日重置为 _1）；
// 本地直接构建时退化为「当天日期 + _1」。
val cnToday: LocalDate = LocalDate.now(ZoneId.of("Asia/Shanghai"))
val datePart = cnToday.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
val fallbackVersionCode = cnToday.format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInt() * 100 + 1

val resolvedVersionName = System.getenv("VERSION_NAME")?.takeIf { it.isNotBlank() } ?: "v${datePart}_1"
val resolvedVersionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: fallbackVersionCode

// Android 的 versionCode 是 int，上限 2,100,000,000；超了会在安装/上架时直接报错
require(resolvedVersionCode in 1..2_100_000_000) {
    "versionCode=${resolvedVersionCode} 超出 Android 允许范围"
}

// ---------- 签名证书 ----------
// 本地：把签名信息写在项目根目录的 keystore.properties（已被 .gitignore 排除）
// CI  ：由 workflow 从仓库 Secrets 注入 RELEASE_KEYSTORE_PATH 等环境变量
// 两者都没有时回退 debug 证书，保证产物仍可直接安装
val keystoreProps = Properties().apply {
    val propsFile = rootProject.file("keystore.properties")
    if (propsFile.exists()) propsFile.inputStream().use { load(it) }
}

fun signingValue(propKey: String, envKey: String): String? =
    keystoreProps.getProperty(propKey)?.takeIf { it.isNotBlank() }
        ?: System.getenv(envKey)?.takeIf { it.isNotBlank() }

val releaseStorePath = signingValue("storeFile", "RELEASE_KEYSTORE_PATH")
val hasReleaseKeystore = !releaseStorePath.isNullOrBlank()
println("签名配置：${if (hasReleaseKeystore) "正式证书（$releaseStorePath）" else "未配置，回退 debug 证书"}")

android {
    namespace = "com.tvpower"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tvpower"
        minSdk = 24          // 安卓 7.0 起步，兼容安卓 9 盒子
        targetSdk = 34

        versionCode = resolvedVersionCode            // 2026111701 这种 10 位整数，单调递增
        versionName = resolvedVersionName            // v2026.11.18_1 这种带 v 前缀、下划线序号的写法

        setProperty("archivesBaseName", "TvPower")   // 产物名：TvPower-release.apk
    }

    signingConfigs {
        create("release") {
            if (hasReleaseKeystore) {
                storeFile = rootProject.file(releaseStorePath!!)
                storePassword = signingValue("storePassword", "RELEASE_KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "RELEASE_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "RELEASE_KEY_PASSWORD")
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
                if (hasReleaseKeystore) {
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

# TvPower 电源

安卓电视盒子的一键 **关机 / 休眠 / 重启** 工具，界面是一块**透明悬浮面板**，浮在盒子桌面上，只有三个按钮可见。

源码托管在 GitHub，APK 由 GitHub Actions 云端构建并自动发布 Release，本地不需要装 Android Studio。

已实测可用的机型：烽火 HG680-KF（Hi3798MV320，安卓 9）；前提是第三方 ATV 固件 + ROOT。

---

## 功能与命令映射

| 按钮 | 执行的 root 命令 | 说明 |
| --- | --- | --- |
| 关机 | `reboot -p` | 软关机；部分海思固件只软停不断电，可改试 `poweroff` |
| 休眠 | `input keyevent KEYCODE_POWER` | 模拟电源键灭屏，遥控器可唤醒，非深度休眠 |
| 重启 | `reboot` | 最稳，第三方 ATV 固件通用 |

三个动作全部通过 `su` 执行 shell 命令。**没有 ROOT 时点击只会弹「执行失败：未获取 ROOT 权限」**，不会真的执行。

## 界面

竖排三个按钮，整体居中：

```
┌──────────┐
│   关机   │  ← 进入界面默认焦点
├──────────┤
│   休眠   │
├──────────┤
│   重启   │
└──────────┘
```

- 窗口全透明，面板之外直接透出盒子桌面
- 焦点态：深蓝底 + 白描边 + 白字，远距离可辨
- **没有二次确认弹窗**：聚焦后按确认键立即执行

### 遥控器操作

| 按键 | 行为 |
| --- | --- |
| 上下方向键 | 在 关机 → 休眠 → 重启 之间移动焦点 |
| OK / 确认键 | 立即执行当前聚焦的动作 |
| 返回键 | 退出界面（系统默认行为） |

注意：默认焦点在「关机」且没有确认框，**打开应用后按一下确认键就会关机**。介意误触可把 `activity_main.xml` 里的 `requestFocus` 挪到「休眠」按钮上。

## ROOT 前提

- 必须先有 `su`，并授权给本应用。可用终端模拟器执行 `su` 验证。
- 关机/重启属系统级操作，普通签名应用无法直接调用，只能借 `su` 执行。
- 部分海思定制固件底层不支持真正断电关机与 S3 深度待机，属固件限制，App 无法绕过。

---

## 下载与安装

到仓库 [Releases](https://github.com/gongwenjia666/TvPower/releases) 页下载最新 APK，文件名形如 `TvPower-v2026.11.18_2.apk`。

```bash
# 盒子开启 ADB 调试后
adb install TvPower-v2026.11.18_2.apk
# 覆盖安装
adb install -r TvPower-v2026.11.18_2.apk
```

报 `INSTALL_FAILED_UPDATE_INCOMPATIBLE` 说明新旧包**签名不一致**，必须先卸载旧版：

```bash
adb uninstall com.tvpower
```

> 当前 CI 默认回退 **debug 证书**签名（仓库 Secrets 中的正式证书尚未配置）。一旦启用正式签名，此后所有版本必须保持同一证书，否则每次升级都要先卸载。

---

## 版本号规则

格式：`v年.月.日_当日序号`，例如 `v2026.11.18_2`

| 规则 | 说明 |
| --- | --- |
| 同一天第一次发布 | `v2026.11.18_1` |
| 同一天第二次发布 | `v2026.11.18_2` |
| 跨天 | 序号重置，回到 `_1` |
| versionCode | `yyyymmdd` + 两位序号，如 `2026111802`，保证单调递增 |
| 兼容历史 tag | 计算序号时同时统计旧格式 `v2026.11.18.v1` 与新格式 `v2026.11.18_1`，避免序号回退 |

版本名由 CI 通过环境变量 `VERSION_NAME` / `VERSION_CODE` 注入；本地直接构建时退化为「当天日期 + `_1`」。

## 发版流程

推送到 `main` 即自动发版，无需手动操作：

1. `push` 到 `main`（或 Actions 页面手动 `Run workflow`）
2. 计算日期与当日序号 → `assembleRelease`
3. 生成更新日志（对比上一个 tag 的提交记录）
4. 创建 Release，tag 与版本名一致，并标记为 Latest

同一分支的构建用 `concurrency` 串行化并 `cancel-in-progress`，避免两次推送并发抢同一个 tag、或 Latest 标记落错。

其他分支（实验用）走 `.github/workflows/build-branch.yml`：只跑 `assembleDebug`、只上传 artifact，**不建 Release、不打 tag**。

## 签名配置

仓库里不含任何证书文件，`.gitignore` 已排除 `signing/`、`keystore.properties`、`*.jks`、`*.keystore`。

**本地构建**：项目根目录放 `keystore.properties`

```properties
storeFile=signing/tvpower-release.jks
storePassword=你的口令
keyAlias=tvpower
keyPassword=你的口令
```

**CI 构建**：仓库 Settings → Secrets and variables → Actions 配置 4 条

| Secret | 含义 |
| --- | --- |
| `RELEASE_KEYSTORE_BASE64` | keystore 文件的单行 base64 |
| `RELEASE_KEYSTORE_PASSWORD` | 库口令 |
| `RELEASE_KEY_ALIAS` | 别名 |
| `RELEASE_KEY_PASSWORD` | 密钥口令 |

两者都没有时自动回退 debug 证书，构建日志会打印「签名配置：未配置，回退 debug 证书」。

---

## 技术参数

| 项 | 值 |
| --- | --- |
| 包名 | `com.tvpower` |
| minSdk / targetSdk | 24（安卓 7.0）/ 34 |
| 构建链 | AGP 8.2.2 / Kotlin 1.9.0 / Gradle 8.2 / JDK 17 |
| 依赖 | `androidx.core:core-ktx`、`androidx.appcompat` |
| 启动入口 | `LAUNCHER` + `LEANBACK_LAUNCHER`，兼容电视桌面与手机桌面 |
| 特性声明 | `android.software.leanback`、`android.hardware.touchscreen` 均为 `required=false` |
| 图标 | `mipmap-*` 五档（48/72/96/144/192）+ `drawable-xhdpi/tv_banner.png`（640×360） |

## 目录结构

```
TvPower/
├── .github/workflows/
│   ├── build.yml              # main 分支：release 构建 + 自动发 Release
│   └── build-branch.yml       # 其他分支：debug 构建，仅上传 artifact
├── app/
│   ├── build.gradle.kts       # 版本号解析 + 双来源签名配置
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── kotlin/com/tvpower/MainActivity.kt
│       └── res/
│           ├── color/btn_tv_text.xml
│           ├── drawable/btn_tv_bg.xml
│           ├── drawable-xhdpi/tv_banner.png
│           ├── layout/activity_main.xml
│           ├── mipmap-*/ic_launcher.png
│           └── values/{strings,colors,themes}.xml
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── .gitignore
```

## 常见问题

**点了没反应，只弹「未获取 ROOT 权限」**
盒子没 root，或 root 管理器没给本应用授权。先用终端模拟器跑 `su` 确认。

**关机后指示灯还亮着**
`reboot -p` 在海思固件上通常只软停系统，不切主板供电。可尝试改用 `poweroff`；仍无效则是固件驱动限制。

**打开应用看到的是纯黑而不是桌面**
透明窗口依赖下层窗口绘制。电视桌面一般能把桌面/壁纸透出来；若某机型显示纯黑，是系统把桌面窗口移出了合成，属固件行为。主题已设 `windowShowWallpaper=true` 兜底，透出壁纸而不是黑。

**打开应用时先闪一下白**
系统启动预览窗的底色。已在主题里用 `windowDisablePreview=true` 关闭。

**休眠后唤不醒**
部分固件对 `KEYCODE_POWER` 只做灭屏，需要再按一次电源键；深度待机（S3）多数盒子不支持。

**手机能装吗**
能装，界面在手机上也能操作。但透明背景在手机桌面上多数看不到效果（桌面窗口会被移出合成），这个设计是为电视盒子做的。

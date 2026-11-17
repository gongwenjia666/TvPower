# TvPower 盒子电源工具

面向安卓电视盒子（如烽火 HG680-KF / Hi3798MV320，安卓 9）的一键 **重启 / 关机 / 睡眠息屏** 小工具，源码托管在 GitHub，使用 GitHub Actions 云端编译 APK，本地无需安装 Android Studio。

## 功能

| 按钮 | 执行的 root 命令 | 说明 |
| --- | --- | --- |
| 🔄 重启盒子 | `reboot` | 最稳，第三方 ATV 固件通用 |
| ⏻ 关机 | `reboot -p` | 部分海思固件仅软停、不断电；可备选 `poweroff` |
| 💤 睡眠息屏 | `input keyevent KEYCODE_POWER` | 模拟电源键灭屏，可遥控唤醒，非深度休眠 |

## 界面布局（横排）

三个按钮水平排列，从左到右：**🔄 重启盒子 ｜ ⏻ 关机 ｜ 💤 睡眠息屏**。

## 遥控器操作（TV 盒子，非触屏）

| 遥控器按键 | 行为 |
| --- | --- |
| 左 / 右方向键 | 在三个按钮之间移动焦点，左右导航已显式指定 |
| OK / 确认键 | 触发当前聚焦的按钮，与触屏点击走同一套逻辑 |
| 返回键 | 退出当前界面（系统默认行为） |
| 弹窗内左右键 | 在“确定 / 取消”之间切换，OK 键确认 |

- 进入界面默认聚焦在左侧「重启盒子」，首键即可操作。
- 聚焦态为深蓝底 + 白色描边 + 白字，远距离可看清选中项。
- 未额外监听 D-pad 按键：标准 Button 焦点导航 + onClick 已覆盖遥控器确认，触屏点击同样可用。

## 前提条件（重要）

- **必须已获取 ROOT（su）**。原厂移动 IPTV 固件无 root，点击按钮只会提示“未获取 ROOT 权限”。
- 关机/重启属于系统级操作，普通签名第三方 App 无法直接调用，只能通过 `su` 执行 shell 命令。
- 部分海思定制固件底层不支持真正断电关机与 S3 深度待机，属于固件驱动限制，App 无法解决。

## 目录结构

```
TvPower/
├── .github/workflows/build.yml      # GitHub Actions 云编译
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── kotlin/com/tvpower/MainActivity.kt
│       └── res/
│           ├── layout/activity_main.xml
│           └── values/strings.xml, themes.xml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## GitHub 部署步骤

1. 在 GitHub 新建仓库，命名 `TvPower`，选 Public，可勾选 `Add a README file`。
2. 按上面目录结构逐个上传本仓库文件（`Add file → Upload files` 或网页新建文件粘贴内容）。
3. 推送后进入仓库 **Actions** 页面，会自动触发 `Build TvPower APK`；也可点 `Run workflow` 手动编译。
4. 编译成功后：
   - 在 Action 运行记录的 **Artifacts** 下载 `TvPower-APK`；
   - 同时自动发布到 Releases（tag: `v1.0`）。
5. 产出文件名：`TvPower_debug_v1.0.apk`。

## 安装到盒子

```bash
# 盒子开启 ADB 调试后，USB / 网络连接执行
adb install TvPower_debug_v1.0.apk
```

## 技术参数

- 包名：`com.tvpower`
- minSdk：24（安卓 7.0），targetSdk：34
- 构建：AGP 8.2.2 / Kotlin 1.9.0 / Gradle 8.2 / JDK 17（Actions 内）

## 常见问题

- **提示“未获取 ROOT 权限”**：盒子未 root，或 su 未授权给本 App。先用终端模拟器验证 `su` 是否可用。
- **关机后指示灯仍亮**：海思固件 `reboot -p` 只软停系统，未切断主板供电，换用 `poweroff` 再试；仍无效则是底层驱动限制。
- **睡眠无效**：部分 TV 固件不响应 `KEYCODE_SLEEP`，当前用 `KEYCODE_POWER` 退而求其次灭屏。
- **Release 包未签名**：当前只编译 debug 包；若要对外发布 release，需在 `app/build.gradle.kts` 配置签名证书，否则部分新版安卓会拒绝安装。

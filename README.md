# Fitness Android App

这是为现有 `fitness` 模块新增的独立安卓工程，原有网页模块保持不变。

## 方案说明

- 安卓端位于 `android/fitness-android`
- 当前实现为 `Kotlin + Jetpack Compose` 原生界面，不再使用 `WebView`
- 数据通过现有后端接口获取：`/api/fitness/*`
- App 内可配置服务端根地址，例如：
  - 模拟器：`http://10.0.2.2:8080`
  - 真机同局域网：`http://你的电脑局域网IP:8080`
- 原有 `src/public/fitness.html` 继续保留，不受影响

## 打开方式

1. 使用 Android Studio 打开 `android/fitness-android`
2. 等待 Gradle 同步完成
3. 直接运行 `app`

## 本地联调

先启动原 Node 服务：

```powershell
Set-Location "D:\code\fund-app"
pnpm install
pnpm exec ts-node src/index.ts
```

如果你用的是 `npm`：

```powershell
Set-Location "D:\code\fund-app"
npm install
npx ts-node src/index.ts
```

## 使用说明

- 打开 App 后默认连接 `http://10.0.2.2:8080`
- 首页为原生 Compose 界面，覆盖网页端健身模块的主要功能
- 右上角可刷新或修改服务端地址
- 安卓端只是新增原生实现，不会删除或替换原有 `src/public/fitness.html`

## 安装包

- 调试安装包默认输出：`app/build/outputs/apk/debug/app-debug.apk`
- 本次整理后的可直接安装包会放在：`dist/fitness-android-debug.apk`

## 后续可选增强

- 补齐更精细的 UI 像素级对齐
- 增加登录态与 token 注入
- 增加正式签名与 `release` 安装包流程

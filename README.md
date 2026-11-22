# FLauncher — Terminal & Floating-Window Android Home Launcher

A lightweight, dark translucent terminal-style Android Home Launcher modeled on the FLauncher workflow: live system wallpaper transparency, notification feed, built-in split-screen browser, and draggable/resizable system-wide floating overlay windows that stay on top of other apps.

## Features

- **Home Screen & Status Bar**: Displays `root@flauncher ~` (switching to `root@flauncher:~/browser` in browser mode), device uptime & battery percentage, live `NotificationListenerService` feed, terminal output log, date/clock widget, right-side quick dock (`> qr`, `> map`, `> notes`, `> weather`, `> files`, `> call`, `> apps`), and bottom command bar.
- **Built-in WebView Browser**: Typing a search term (e.g., `ciao`) or URL opens an integrated dark browser with right-side controls (`> open`, `> float`, `> desktop`, `> reload`, `> back`, `> exit`).
- **System-Wide Floating Windows (`SYSTEM_ALERT_WINDOW`)**: All floating windows can be dragged by their header, resized from the bottom-right corner (`⌟`), and stacked on top of other running apps:
  - **Floating Terminal**: `FLauncher Terminal · Floating v1.8.0` with live filtered suggestion chips (built-in commands + fuzzy-matched installed apps).
  - **Floating Weather**: Live GPS/mock-location weather via Open-Meteo (no API key required) with `24H DAY` hourly and `7D WEEK` forecast tabs.
  - **Floating Maps**: High-contrast dark monochrome street map powered by OpenFreeMap / MapLibre GL JS with a cyan current-location marker, zoom/compass controls, and `map <destination>` routing via Nominatim + OSRM.
  - **Floating Notes**: Quick persistent notes with `[+]` / `SAVE` / `← Back to Notes` and timestamped cards.
  - **Floating Apps Drawer & QR Generator**: Instant searchable app list and offline ZXing QR code generator.

## Build Automatically with GitHub Actions

1. Create a new repository on GitHub and push all files from this folder (including `.github/workflows/build.yml`) to the `main` branch:
   ```bash
   git init
   git add .
   git commit -m "Initial commit: FLauncher Android home launcher"
   git branch -M main
   git remote add origin https://github.com/<your-username>/<your-repo>.git
   git push -u origin main
   ```
2. Open the **Actions** tab in your GitHub repository. The **Build FLauncher APK** workflow runs automatically using JDK 17 and Gradle 8.7.
3. Once the workflow finishes, download **`FLauncher-Debug-APK`** from the run's **Artifacts** section and install `FLauncher-debug.apk` on your Android device (Android 8.0+ / API 26+).
4. Push a git tag like `v1.8.0` to automatically publish a GitHub Release with the APK attached.

## First-Launch Android Setup

- **Set as Default Home App**: Android Settings → Apps → Default apps → Home app → **FLauncher**.
- **Floating Windows**: Tap any floating tool (or Settings → Display over other apps → **FLauncher** → Allow).
- **Live Notifications**: Tap the notifications placeholder on the home screen (or Settings → Notification access → **FLauncher Notifications** → Allow).
- **Location**: Grant location permission when prompted so Weather and Maps detect your current coordinates (compatible with GPS, Network, and Fake GPS mock providers).

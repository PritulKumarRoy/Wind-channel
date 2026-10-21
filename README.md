# 🌤️ Wind Channel — Minimal & Modern Weather Observatory

**Wind Channel** is a modern, clean, and polished weather app for Android crafted with **Kotlin** and **Jetpack Compose (Material Design 3)**. Built upon open, high-precision meteorological telemetry from Open-Meteo, Wind Channel delivers hyper-local forecasts, interactive hourly trend charts, 10-day outlook bars, and custom-rendered Canvas weather instruments—all wrapped in an adaptable dual-mode theme system.

---

## ✨ Features

### 🌓 Dual-Mode Theme Engine
* **Clean White Canvas (Light Mode):** An editorial, magazine-like interface featuring pure white backgrounds (`#FFFFFF`), halite salt surface tints, crisp slate borders, and high-contrast ink-black numerals (`#090D14`).
* **Obsidian Night (Dark Mode):** Deep velvet dark atmosphere (`#090D14`) paired with elevated slate card containers (`#111827`) and luminescent white typography (`#FFFFFF`).
* **One-Tap Instant Toggle:** Dedicated theme switcher icon directly in the top app bar and within Settings. Every numeral, label, gauge, card, and dialog adapts immediately.

### 🌡️ Hero Atmospheric Presentation
* **Prominent Temperature Display:** Large, clean typography (82sp) showing real-time temperature formatted in either Celsius or Fahrenheit.
* **Comprehensive Current Stats:** Real-time condition titles, feels-like temperature, daily High/Low extremes, and an atmospheric condition summary badge.
* **Contextual Advisories:** Severe weather alert banners whenever turbulent weather patterns are detected.

### ⏱️ Interactive Hourly Forecast Ribbon
* **Three Analytical Views:**
  * **Temperature (°C / °F):** Hourly temperature progression with custom weather icons.
  * **Precipitation (%):** Rain probability indicators with dynamic blue droplet styling.
  * **Wind Speed (km/h / mph):** 24-hour wind velocity trend curves.
* **"Now" Indicator:** Highlights the current hour with distinct high-contrast formatting.

### 📅 10-Day Extended Outlook
* **Comparative Range Bars:** Visual horizontal gradient bars comparing each day’s temperature span relative to 10-day global extremes.
* **Live Temperature Indicator:** A dynamic marker showing exactly where current conditions sit on today's span.
* **Precipitation Probability Badges:** Displays rain chances for days with greater than 20% precipitation probability.

### 🧭 Precision Meteorological Instruments (Custom Canvas)
* **Digital Wind Compass:** Custom Canvas 8-point nautical compass with an animated needle pointing to the true wind vector, displaying current wind speed, gusts, and cardinal heading.
* **UV Index Radial Arc Gauge:** Semi-circular color-coded gauge showing current UV levels, risk category (*Low* to *Extreme*), and sun-protection advice.
* **Solar Arc & Sun Tracker:** Displays the sun's trajectory across the sky with sunrise, sunset, dawn, and dusk timestamps.
* **Precipitation Radar (Demo):** An illustrative animation, clearly labelled as demo. It does **not** show real radar data.
* **Barometric Pressure Card:** Real-time atmospheric pressure in both **hPa** and **inHg** with directional trend arrows (*Rising*, *Steady*, *Falling*).
* **Humidity & Dew Point Card:** Relative humidity percentage, calculated dew point, and human comfort index rating.
* **Visibility & Cloud Cover Card:** Atmospheric line-of-sight distance in miles/km and total cloud obscuration percentage.
* **Moon Phase Tracker:** Current lunar cycle phase name and illumination percentage (calculated from the lunar cycle).

### 🌍 Global Location Search & Multi-City Manager
* **Live Geocoding Search:** Search and select any city or region across the globe.
* **Saved Locations Manager:** Store multiple cities with instant switching and deletion capabilities.
* **Quick-Access Metropolises:** Pre-configured shortcuts for fast city switching.
* **Honest Offline Handling:** If a refresh fails, the app shows an error and, if available, the last *real* forecast it downloaded together with its download time. It never shows made-up weather.

### 📐 Measured vs. calculated values
* **From Open-Meteo:** temperature, feels-like, humidity, dew point, pressure, wind speed/direction/gusts, visibility, cloud cover, precipitation probability, UV index, sunrise/sunset and all forecast dates/times (city-local).
* **Calculated in the app:** pressure trend (change over the previous 3 hours, ±1 hPa threshold), moon phase, and *estimated* first light / dusk (civil twilight from latitude and date, labelled “est.”).
* Anything the service does not provide is shown as “—” instead of a guessed number.

---

## 🛠️ Tech Stack & Tooling

| Component | Technology | Badge / Logo |
| :--- | :--- | :--- |
| **Core Language** | Kotlin 2.0+ | ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white) |
| **Operating System** | Android 7.0+ (API 24), targets API 36 | ![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white) |
| **UI Toolkit** | Jetpack Compose | ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white) |
| **Design System** | Material Design 3 (M3) | ![Material 3](https://img.shields.io/badge/Material%20Design%203-757575?style=for-the-badge&logo=materialdesign&logoColor=white) |
| **Concurrency & Flow** | Kotlin Coroutines & StateFlow | ![Coroutines](https://img.shields.io/badge/Kotlin%20Coroutines-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white) |
| **IDE & Build Suite** | Android Studio & Gradle (Kotlin DSL) | ![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white) ![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white) |
| **Networking** | Square Retrofit 2.12 & OkHttp 4 | ![Retrofit](https://img.shields.io/badge/Square%20Retrofit-231F20?style=for-the-badge&logo=square&logoColor=white) ![OkHttp](https://img.shields.io/badge/Square%20OkHttp-231F20?style=for-the-badge&logo=square&logoColor=white) |
| **JSON Serialization** | Square Moshi | ![Moshi](https://img.shields.io/badge/Moshi%20JSON-231F20?style=for-the-badge&logo=json&logoColor=white) |
| **Data Provider** | Open-Meteo REST API | ![Open-Meteo](https://img.shields.io/badge/Open--Meteo-0284C7?style=for-the-badge&logo=icloud&logoColor=white) |
| **Typography** | Google Fonts (Outfit & Inter) | ![Google Fonts](https://img.shields.io/badge/Google%20Fonts-4285F4?style=for-the-badge&logo=googlefonts&logoColor=white) |
| **Unit Testing** | Robolectric Local JVM Testing | ![Robolectric](https://img.shields.io/badge/Robolectric-3DDC84?style=for-the-badge&logo=android&logoColor=white) |

---

## 🚀 Getting Started

### Prerequisites

Ensure your development machine has:
1. **JDK 17 or JDK 21** installed (`java -version`).
2. **Android SDK** with platform tools / `adb` configured in your system `PATH`.
3. An Android device with **USB Debugging enabled** or an active Android Virtual Device (AVD).

Verify connected device:
```bash
adb devices
```

---

### Running via Command Line (`cmd` / `bash` / `zsh`)

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/wind-channel.git
   cd wind-channel
   ```

2. **Build and install the app directly to your device:**
   * **Windows (`cmd` / PowerShell):**
     ```cmd
     gradle installDebug
     ```
     *(or `gradlew.bat installDebug` if using the Gradle wrapper)*
   * **macOS / Linux:**
     ```bash
     gradle installDebug
     ```
     *(or `./gradlew installDebug` if using the Gradle wrapper)*

3. **Launch the application via ADB:**
   ```bash
   adb shell am start -n com.aistudio.baroqueweather.vptm/com.example.MainActivity
   ```
   *(or `adb shell monkey -p com.aistudio.baroqueweather.vptm -c android.intent.category.LAUNCHER 1`)*

> [!TIP]
> If you already had an earlier build installed on your device or emulator with a different signature, uninstall it first:
> ```bash
> adb uninstall com.aistudio.baroqueweather.vptm
> ```

---

### Building the Standalone APK

If you want to generate an installable `.apk` file:

* **Windows (`cmd` / PowerShell):**
  ```cmd
  gradle assembleDebug
  ```
* **macOS / Linux:**
  ```bash
  gradle assembleDebug
  ```

The generated APK will be available at:
```
app/build/outputs/apk/debug/app-debug.apk
```

To install the APK directly:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

### Running via Android Studio

1. Open **Android Studio**.
2. Select **File > Open...** and navigate to the root directory of this repository.
3. Allow Gradle to synchronize dependencies.
4. Select your connected device or emulator in the toolbar.
5. Click the green **Run (▶)** button or press `Shift + F10`.

---

### Running Unit & Robolectric Tests

To execute the test suite (including weather calculations, cardinal wind directions, and string assertions):

* **Windows (`cmd` / PowerShell):**
  ```cmd
  gradle testDebugUnitTest
  ```
* **macOS / Linux:**
  ```bash
  gradle testDebugUnitTest
  ```

---

## 🔒 Privacy & Permissions

Wind Channel values privacy and resource safety:
* **Zero Location Tracking:** Geocoding searches are query-based; the app does not monitor background GPS telemetry.
* **Zero Third-Party Trackers:** No ad networks, analytics SDKs, or background telemetry services.
* **No Account Required:** Completely functional without login, registration, or cloud credentials.

<div align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="120" height="120" alt="Wind Channel app icon"/>
</div>

<h1 align="center">Wind Channel</h1>
<h3 align="center"><em>A modern weather app for Android built with **Kotlin** and **Jetpack Compose (Material Design 3)**. It uses the free [Open-Meteo](https://open-meteo.com/) forecast and geocoding APIs to show current conditions, a 24-hour hourly ribbon, a 10-day outlook and a set of custom Canvas-drawn weather instruments, in a light and a dark theme.</em></h3>

<br>

---

The app is deliberately honest about its data: every number comes either from Open-Meteo or from a clearly labelled calculation. If something is not available, the app shows "—" instead of guessing, and it never displays made-up weather.

---

## ✨ Features

### 🌓 Light & Dark Themes
* **Light mode:** white canvas (`#FFFFFF`), soft slate borders and tints, ink-black numerals (`#090D14`), with a subtle sky gradient at the top that changes with the current condition (clear day/night, cloudy, rain, storm, snow).
* **Dark mode:** deep navy-black background (`#090D14`), elevated slate cards (`#111827`) and white text, with matching condition gradients.
* **System theme on first launch:** the app starts in whatever your device is set to. After that, your own choice wins.
* **Instant toggle:** a sun/moon button in the top bar, plus a switch in Settings.

### 🌡️ Current Conditions
* Large 82sp temperature, condition title with icon and a short summary badge.
* Today's high and low, plus "feels like".
* **°C / °F toggle** (top bar pill or Settings). Choosing °F also switches wind speed to mph and visibility to miles, so one switch selects metric or imperial throughout.
* **Weather notices:** a red banner appears for thunderstorms, strong wind (sustained ≥ 45 km/h or gusts ≥ 70 km/h), very high UV (≥ 8), snow, and fog or visibility below 1 km. These are derived from the forecast data, are worded as such, and are **not official weather warnings**.

### ⏱️ Hourly Forecast
A horizontally scrolling ribbon of the next **24 hours**, starting at the current hour (labelled "Now"). Switch between three views:
* **Temp:** hourly temperature with a weather icon.
* **Rain:** precipitation probability (%).
* **Wind:** hourly wind speed in km/h or mph.

### 📅 10-Day Outlook
* One row per day with icon, low, high and a horizontal range bar.
* Bars are scaled against the coldest and warmest values across the 10 days, so days can be compared at a glance.
* On today's row, a marker shows where the current temperature sits.
* A rain-probability badge appears on days with a probability of 20 % or more.

### 🧭 Instruments
| Card | What it shows |
| :--- | :--- |
| **Wind compass** | Custom Canvas dial with a needle rotated to the wind direction. Shows speed, "From" cardinal direction (8-point: N, NE, E, SE, S, SW, W, NW), gusts (when reported) and a plain-language description. |
| **UV index gauge** | Semi-circular color-coded gauge for **today's forecast maximum UV index**, with the risk level (Low to Extreme) and protection advice. Shows "UV index not available" if the service has no value. |
| **Sun position & daylight** | Sun arc with sunrise and sunset, plus **estimated** first light and dusk (civil twilight, labelled "est."). Handles polar day/night by showing a notice instead of times. |
| **Humidity** | Relative humidity, dew point and a comfort label (Dry below 35 %, Comfortable 35–65 %, Humid & muggy above 65 %). |
| **Pressure** | Surface pressure in both **hPa** and **inHg**, with a Rising / Steady / Falling arrow based on the last 3 hours. |
| **Visibility & cloud cover** | Visibility in km or miles with a short description, plus total cloud cover (%). |
| **Moon phase** | Phase name and illumination percentage, calculated from the average lunar cycle. |
| **Precipitation radar (demo)** | An illustrative animation, clearly labelled **DEMO**. It does **not** show real radar data. |

### 🌍 Locations
* **Live search:** find any city or region worldwide via the Open-Meteo geocoding API (debounced as you type).
* **Saved Locations:** tap the city name in the top bar to switch between saved cities, add new ones or delete them. The last remaining city cannot be removed.
* **Built-in cities:** San Francisco (default), New York, London, Tokyo, Paris, Sydney, Berlin, Singapore, Dubai and Toronto. The first six start out as your saved list.
* **Search shortcuts:** "Popular Metropolises" chips (Tokyo, London, New York) in the search dialog.
* **Search fallback:** if the search service can't be reached, the app searches the built-in city list instead and tells you so.

### 📡 Honest Error & Offline Handling
* A failed load with nothing to show gives a clear error message and a **Try Again** button.
* If a *refresh* fails and the app already downloaded a real forecast for that city during the current session, it keeps showing it with a banner stating that live data is unavailable and when the forecast was downloaded.
* Errors are specific: no connection, server error (with HTTP status), or incomplete/malformed data.
* Refresh with the ↻ button in the top bar. There is no automatic background refresh.

### 📐 Measured vs. Calculated Values

| Source | Values |
| :--- | :--- |
| **From Open-Meteo** | Temperature, feels-like, humidity, dew point, surface pressure, wind speed / direction / gusts, visibility, cloud cover, precipitation probability, weather codes, today's max UV index, sunrise / sunset, and all forecast dates and times (in the city's local time zone) |
| **Calculated in the app** | Pressure trend (change over the previous 3 hours, ±1 hPa threshold), moon phase, estimated first light / dusk, weather notices, wind direction label and description, humidity comfort label, unit conversions. Dew point is calculated (Magnus formula) only if the service omits it. |
| **Not available** | Shown as "—" or a "not available" message, never guessed |

---

## 🛠️ Tech Stack

| Component | Technology |
| :--- | :--- |
| **Language** | Kotlin 2.2.10 |
| **Platform** | Android 7.0+ (minSdk 24), compile/target SDK 36 |
| **UI** | Jetpack Compose (BOM 2024.09.00) with Material Design 3, custom Canvas drawing |
| **Architecture** | Single `ViewModel` exposing a `StateFlow`, repository + parser layers |
| **Concurrency** | Kotlin Coroutines & StateFlow |
| **Networking** | Retrofit 2.12.0, OkHttp 4.10.0 (10 s connect / 15 s read / 25 s total timeouts) |
| **JSON** | Moshi 1.15.2 |
| **Data provider** | Open-Meteo Forecast & Geocoding APIs (no API key required) |
| **Typography** | Outfit (headings, numerals) and Inter (body text), bundled in the app |
| **Build** | Android Gradle Plugin 8.13.1, Gradle 9.3.1, Kotlin DSL, version catalog |
| **Testing** | JUnit 4, Robolectric 4.16.1, Compose UI Test, kotlinx-coroutines-test |

### Project layout
```
app/src/main/java/com/example/
├── MainActivity.kt                 # Entry point, applies system theme once
├── data/
│   ├── api/WeatherApiService.kt    # Retrofit interface + requested Open-Meteo fields
│   ├── model/                      # Domain models, unit conversions, exceptions
│   └── repository/
│       ├── WeatherRepository.kt    # Network calls, error mapping, city search
│       └── OpenMeteoParser.kt      # Validates and converts raw responses
└── ui/
    ├── components/                 # Icons, range bar, UV gauge, compass, sun arc, radar demo
    ├── screens/ModernWeatherScreen.kt
    ├── theme/                      # Colors, M3 schemes, typography
    └── viewmodel/WeatherViewModel.kt
```

---

## 🚀 Getting Started

### Prerequisites
1. **JDK 17 or newer** (`java -version`).
2. **Android SDK** (API 36) with `adb` on your `PATH`.
3. An Android device with **USB debugging** enabled, or an Android Virtual Device (AVD).
4. An internet connection (for dependencies on first build, and for weather data at runtime).

Verify your device is connected:
```bash
adb devices
```

### Running in Android Studio (recommended)
1. Open **Android Studio** (a recent version that supports AGP 8.13).
2. Choose **File > Open…** and select the repository root.
3. Let Gradle sync. Android Studio downloads the required Gradle version automatically.
4. Select a device or emulator and click **Run ▶** (`Shift + F10`).

### Running from the command line

> **Note:** this repository does not include the Gradle wrapper scripts (`gradlew` / `gradlew.bat`), only `gradle/wrapper/gradle-wrapper.properties`, which pins **Gradle 9.3.1**. Either let Android Studio sync the project, install Gradle 9.3.1 yourself, or generate the wrapper once with `gradle wrapper`. After that you can use `./gradlew` (macOS / Linux) or `gradlew.bat` (Windows) in place of `gradle` below.

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/wind-channel.git
   cd wind-channel
   ```
2. **Build and install on the connected device:**
   ```bash
   gradle installDebug
   ```
3. **Launch the app:**
   ```bash
   adb shell am start -n com.aistudio.baroqueweather.vptm/com.example.MainActivity
   ```

> [!TIP]
> If an earlier build with a different signature is already installed, uninstall it first:
> ```bash
> adb uninstall com.aistudio.baroqueweather.vptm
> ```

### Building a debug APK
```bash
gradle assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

Install it with:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

> The debug build is signed with the `debug.keystore` in the project root (it is created automatically if missing). A **release** build expects your own keystore and the environment variables `KEYSTORE_PATH`, `STORE_PASSWORD` and `KEY_PASSWORD`.

### Running the tests
```bash
gradle testDebugUnitTest
```
The suite runs on the local JVM (no device needed) and covers:
* Parsing of Open-Meteo responses, including missing and `null` values
* Unit conversions, wind directions, pressure trend and moon phase
* Repository error mapping (network, HTTP, malformed or incomplete data)
* ViewModel behavior: error states, caching, city search and saved cities, theme handling
* Compose UI checks for the error and "stale forecast" screens

Instrumented tests (`gradle connectedDebugAndroidTest`) currently contain only the default example test.

---

## ⚠️ Current Limitations
* **No persistence.** Saved cities, the selected city, unit and theme choices, and the "last downloaded forecast" are held in memory only. They reset when the app process is closed.
* **Not location-aware.** The app does not request device location. Choose a city manually.
* **UV is a daily maximum**, not a live reading.
* **Weather notices are model-derived**, not official alerts. Always consult your national weather service for warnings.
* **Radar is a demo animation** and does not show real precipitation.
* **Pressure is surface (station) pressure**, not sea-level pressure.

---

## 🔒 Privacy & Permissions

* **Permissions:** only `INTERNET` and `ACCESS_NETWORK_STATE`.
* **No device location:** the app never reads GPS or network location. Forecast requests contain the coordinates of the *city you selected*, and search requests contain the text you typed. Both are sent to Open-Meteo.
* **No trackers:** no ad networks, analytics SDKs or crash-reporting services, and no accounts or sign-in.
* **Logging:** HTTP request lines (which include coordinates) are logged only in debug builds, never in release builds.

---

## 📄 Data & Credits

Weather and geocoding data are provided by [Open-Meteo.com](https://open-meteo.com/) under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/). Please review Open-Meteo's terms of use, especially if you plan to use the app commercially or at high request volumes.

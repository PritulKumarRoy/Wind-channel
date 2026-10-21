package com.example.testing

import com.example.data.api.WeatherApiService
import com.example.data.model.City
import com.example.data.model.CurrentData
import com.example.data.model.DailyData
import com.example.data.model.GeocodingResponse
import com.example.data.model.HourlyData
import com.example.data.model.OpenMeteoResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

object TestData {
  val city = City("test", "Testville", "Testshire", "Testland", 10.0, 20.0)

  /** Hourly series covers 2026-10-20 .. 2026-10-22 (72 entries). Index 0 = 2026-10-20T00:00. */
  val hourTimes: List<String?> = listOf("2026-10-20", "2026-10-21", "2026-10-22").flatMap { day ->
    (0..23).map { h -> "${day}T${h.toString().padStart(2, '0')}:00" }
  }

  /** With the default `now` (2026-10-21T14:15) the current hour is index 24 + 14 = 38. */
  const val NOW_INDEX = 38

  val dailyTimes: List<String?> = listOf("2026-10-20", "2026-10-21", "2026-10-22", "2026-10-23")

  fun current(
    time: String? = "2026-10-21T14:15",
    temperature: Double? = 21.0,
    gusts: Double? = 30.0,
    weatherCode: Int? = 1
  ) = CurrentData(
    time = time,
    temperature = temperature,
    apparentTemperature = 20.0,
    relativeHumidity = 55.0,
    isDay = 1,
    weatherCode = weatherCode,
    surfacePressure = 1015.0,
    windSpeed = 18.0,
    windDirection = 280.0,
    windGusts = gusts,
    cloudCover = 20.0
  )

  fun hourly(
    wind: List<Double?>? = hourTimes.indices.map { it.toDouble() },
    pressure: List<Double?>? = hourTimes.indices.map { 1000.0 + it * 0.5 },
    visibility: List<Double?>? = hourTimes.indices.map { 24140.0 },
    precipitation: List<Double?>? = hourTimes.indices.map { 10.0 },
    dewPoint: List<Double?>? = hourTimes.indices.map { 11.0 }
  ) = HourlyData(
    time = hourTimes,
    temperature = hourTimes.indices.map { 10.0 + it * 0.1 },
    weatherCode = hourTimes.indices.map { 1 },
    precipitationProbability = precipitation,
    surfacePressure = pressure,
    windSpeed = wind,
    visibility = visibility,
    dewPoint = dewPoint,
    isDay = hourTimes.indices.map { if (it % 24 in 6..18) 1 else 0 }
  )

  fun daily(
    uv: List<Double?>? = listOf(5.0, 6.0, 7.0, 8.0),
    precipitation: List<Double?>? = listOf(10.0, 20.0, 65.0, 80.0),
    sunrise: List<String?>? = dailyTimes.map { "${it}T06:30" },
    sunset: List<String?>? = dailyTimes.map { "${it}T18:30" }
  ) = DailyData(
    time = dailyTimes,
    weatherCode = listOf(0, 1, 61, 95),
    temperatureMax = listOf(20.0, 22.0, 24.0, 19.0),
    temperatureMin = listOf(10.0, 11.0, 12.0, 9.0),
    precipitationProbabilityMax = precipitation,
    sunrise = sunrise,
    sunset = sunset,
    uvIndexMax = uv
  )

  fun response(
    current: CurrentData? = current(),
    hourly: HourlyData? = hourly(),
    daily: DailyData? = daily()
  ) = OpenMeteoResponse(latitude = 10.0, longitude = 20.0, current = current, hourly = hourly, daily = daily)
}

/** Hand-written fake: records the query parameters the repository sends. */
class FakeWeatherApi : WeatherApiService {
  var forecast: suspend (Double, Double) -> OpenMeteoResponse = { _, _ -> TestData.response() }
  var search: suspend (String) -> GeocodingResponse = { GeocodingResponse(emptyList()) }

  var forecastCalls = 0
  var searchCalls = 0
  val requestedLatitudes = mutableListOf<Double>()
  var lastCurrent: String? = null
  var lastHourly: String? = null
  var lastDaily: String? = null
  var lastPastDays: Int? = null
  var lastForecastDays: Int? = null

  override suspend fun getForecast(
    latitude: Double,
    longitude: Double,
    current: String,
    hourly: String,
    daily: String,
    timezone: String,
    pastDays: Int,
    forecastDays: Int
  ): OpenMeteoResponse {
    forecastCalls++
    requestedLatitudes += latitude
    lastCurrent = current
    lastHourly = hourly
    lastDaily = daily
    lastPastDays = pastDays
    lastForecastDays = forecastDays
    return forecast(latitude, longitude)
  }

  override suspend fun searchLocations(
    query: String,
    count: Int,
    language: String,
    format: String
  ): GeocodingResponse {
    searchCalls++
    return search(query)
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
  val scheduler: TestCoroutineScheduler = TestCoroutineScheduler()
) : TestWatcher() {
  val dispatcher: TestDispatcher = UnconfinedTestDispatcher(scheduler)
  override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
  override fun finished(description: Description) = Dispatchers.resetMain()
}

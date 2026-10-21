package com.example.data.repository

import com.example.data.model.City
import com.example.data.model.CompleteWeatherData
import com.example.data.model.DailyForecastItem
import com.example.data.model.HourlyForecastItem
import com.example.data.model.ModernUvIndex
import com.example.data.model.OpenMeteoResponse
import com.example.data.model.WeatherAdvisory
import com.example.data.model.WeatherException
import com.example.data.model.calculateModernWind
import com.example.data.model.calculateMoonPhase
import com.example.data.model.calculatePressureTrend
import com.example.data.model.calculateUvRating
import com.example.data.model.mapWeatherCodeToModern
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Converts a raw Open-Meteo response into [CompleteWeatherData].
 *
 * Rules:
 *  - Values that identify "what the weather is right now" (temperature, code, wind, pressure, ...)
 *    are required. If they are missing, an [WeatherException.InvalidData] is thrown instead of
 *    inventing a number.
 *  - Optional values (visibility, gusts, UV, precipitation probability, ...) stay null when the
 *    service did not supply them.
 *  - All dates / times come from the timestamps in the response (city-local), never from the
 *    device clock or device time zone.
 */
internal object OpenMeteoParser {

  private const val HOURS_SHOWN = 24
  private const val DAYS_SHOWN = 10

  fun parse(city: City, response: OpenMeteoResponse, fetchedAtMillis: Long): CompleteWeatherData {
    val current = response.current ?: invalid("current conditions missing")
    val observedAt = current.time ?: invalid("current time missing")
    val currentDate = observedAt.substringBefore('T')
    val currentHourKey = observedAt.take(13) + ":00"
    val localMinutes = minutesOfDay(observedAt.substringAfter('T', "")) ?: invalid("current time malformed")

    val tempC = current.temperature ?: invalid("temperature missing")
    val apparentC = current.apparentTemperature ?: invalid("feels-like temperature missing")
    val humidity = current.relativeHumidity?.roundToInt()?.coerceIn(0, 100) ?: invalid("humidity missing")
    val code = current.weatherCode ?: invalid("weather code missing")
    val isDay = (current.isDay ?: invalid("day/night flag missing")) == 1
    val pressureHpa = current.surfacePressure ?: invalid("pressure missing")
    val windSpeed = current.windSpeed ?: invalid("wind speed missing")
    val windDir = current.windDirection ?: invalid("wind direction missing")

    val hourly = response.hourly ?: invalid("hourly forecast missing")
    val hTimes = hourly.time.orEmpty()
    val hourIdx = hTimes.indexOf(currentHourKey)
    if (hourIdx < 0) invalid("hourly forecast does not cover the current hour")

    val daily = response.daily ?: invalid("daily forecast missing")
    val dTimes = daily.time.orEmpty()
    val todayIdx = dTimes.indexOf(currentDate)
    if (todayIdx < 0) invalid("daily forecast does not cover today")

    // ---- Daily ----
    val dailyItems = mutableListOf<DailyForecastItem>()
    for (d in todayIdx until minOf(todayIdx + DAYS_SHOWN, dTimes.size)) {
      val iso = dTimes[d] ?: continue
      val dMax = daily.temperatureMax?.getOrNull(d) ?: continue
      val dMin = daily.temperatureMin?.getOrNull(d) ?: continue
      val dCode = daily.weatherCode?.getOrNull(d) ?: continue
      dailyItems += DailyForecastItem(
        isoDate = iso,
        dayLabel = if (d == todayIdx) "Today" else formatDate(iso, "EEE") ?: iso,
        fullDate = formatDate(iso, "MMM d") ?: iso,
        maxTempC = dMax,
        minTempC = dMin,
        weatherCode = dCode,
        condition = mapWeatherCodeToModern(dCode, true),
        precipitationProb = daily.precipitationProbabilityMax?.getOrNull(d)?.roundToInt(),
        sunriseTime = timePart(daily.sunrise?.getOrNull(d)),
        sunsetTime = timePart(daily.sunset?.getOrNull(d)),
        maxUv = daily.uvIndexMax?.getOrNull(d)
      )
    }
    val today = dailyItems.firstOrNull()
    if (today == null || today.isoDate != currentDate) invalid("today's forecast is incomplete")

    val sunrise = today.sunriseTime
    val sunset = today.sunsetTime
    val sunriseMin = sunrise?.let { minutesOfDay(it) }
    val sunsetMin = sunset?.let { minutesOfDay(it) }
    val twilight = if (sunriseMin != null && sunsetMin != null) {
      civilTwilightOffsetMinutes(city.latitude, currentDate)
    } else null
    val firstLight = if (sunriseMin != null && twilight != null) formatMinutes(sunriseMin - twilight) else null
    val dusk = if (sunsetMin != null && twilight != null) formatMinutes(sunsetMin + twilight) else null

    // ---- Hourly (starting at the current hour) ----
    val hourlyItems = mutableListOf<HourlyForecastItem>()
    for (i in hourIdx until minOf(hourIdx + HOURS_SHOWN, hTimes.size)) {
      val time = hTimes[i] ?: continue
      val hourInt = time.substringAfter('T', "").take(2).toIntOrNull() ?: continue
      val hTemp = hourly.temperature?.getOrNull(i) ?: continue
      val hCode = hourly.weatherCode?.getOrNull(i) ?: continue
      val isNow = i == hourIdx
      val hIsDay = hourly.isDay?.getOrNull(i)?.let { it == 1 }
        ?: if (isNow) isDay
        else if (sunriseMin != null && sunsetMin != null) hourInt * 60 in sunriseMin..sunsetMin
        else hourInt in 6..18
      hourlyItems += HourlyForecastItem(
        timeLabel = if (isNow) "Now" else formatHour12(hourInt),
        hourInt = hourInt,
        isNow = isNow,
        tempC = hTemp,
        weatherCode = hCode,
        precipitationProb = hourly.precipitationProbability?.getOrNull(i)?.roundToInt(),
        condition = mapWeatherCodeToModern(hCode, hIsDay),
        windSpeedKmh = hourly.windSpeed?.getOrNull(i)
      )
    }

    // ---- Values taken from the current hour of the hourly series ----
    val visibilityKm = hourly.visibility?.getOrNull(hourIdx)?.let { it / 1000.0 }
    val dewPoint = hourly.dewPoint?.getOrNull(hourIdx) ?: dewPointMagnus(tempC, humidity)
    val pressureNow = hourly.surfacePressure?.getOrNull(hourIdx)
    val pressure3hAgo = hourly.surfacePressure?.getOrNull(hourIdx - 3)
    val pressureTrend = calculatePressureTrend(
      if (pressureNow != null && pressure3hAgo != null) pressureNow - pressure3hAgo else null
    )

    val wind = calculateModernWind(windDir, windSpeed, current.windGusts)
    val uv = today.maxUv?.let { calculateUvRating(it) }

    return CompleteWeatherData(
      city = city,
      fetchedAtMillis = fetchedAtMillis,
      localTimeMinutes = localMinutes,
      temperatureC = tempC,
      apparentTempC = apparentC,
      todayMaxC = today.maxTempC,
      todayMinC = today.minTempC,
      relativeHumidity = humidity,
      dewPointC = dewPoint,
      weatherCode = code,
      condition = mapWeatherCodeToModern(code, isDay),
      pressureHpa = pressureHpa,
      pressureTrend = pressureTrend,
      wind = wind,
      visibilityKm = visibilityKm,
      cloudCoverPercent = current.cloudCover?.roundToInt()?.coerceIn(0, 100),
      uv = uv,
      isDay = isDay,
      sunrise = sunrise,
      sunset = sunset,
      firstLight = firstLight,
      dusk = dusk,
      hourly = hourlyItems,
      daily = dailyItems,
      moonPhase = calculateMoonPhase(fetchedAtMillis),
      advisory = buildAdvisory(code, wind.speedKmh, wind.gustSpeedKmh, visibilityKm, uv)
    )
  }

  /**
   * Notices are derived from the forecast model values, NOT from an official weather authority,
   * and are worded accordingly.
   */
  private fun buildAdvisory(
    code: Int,
    windKmh: Double,
    gustKmh: Double?,
    visibilityKm: Double?,
    uv: ModernUvIndex?
  ): WeatherAdvisory? {
    val note = " Based on forecast model data, not an official warning."
    return when {
      code in listOf(95, 96, 99) -> WeatherAdvisory(
        title = "Thunderstorm Conditions",
        level = "Warning",
        description = "Thunderstorms are in the forecast. Lightning and gusty winds are possible.$note",
        icon = "thunder"
      )
      windKmh >= 45.0 || (gustKmh ?: 0.0) >= 70.0 -> WeatherAdvisory(
        title = "Strong Wind Conditions",
        level = "Advisory",
        description = if (gustKmh != null) {
          "Winds of ${windKmh.roundToInt()} km/h with gusts up to ${gustKmh.roundToInt()} km/h.$note"
        } else {
          "Sustained winds of ${windKmh.roundToInt()} km/h.$note"
        },
        icon = "wind"
      )
      (uv?.value ?: 0.0) >= 8.0 -> WeatherAdvisory(
        title = "Very High UV Forecast",
        level = "Advisory",
        description = "Sun protection is advised around midday.$note",
        icon = "sun"
      )
      code in listOf(71, 73, 75, 77, 85, 86) -> WeatherAdvisory(
        title = "Snow Conditions",
        level = "Advisory",
        description = "Snowfall is in the forecast. Surfaces may be icy.$note",
        icon = "snow"
      )
      code in listOf(45, 48) || (visibilityKm != null && visibilityKm < 1.0) -> WeatherAdvisory(
        title = "Fog / Low Visibility",
        level = "Advisory",
        description = "Visibility may be reduced. Drive with caution.$note",
        icon = "fog"
      )
      else -> null
    }
  }

  // ---------- helpers ----------

  private fun invalid(detail: String): Nothing = throw WeatherException.InvalidData(detail)

  private fun timePart(iso: String?): String? =
    iso?.substringAfter('T', "")?.take(5)?.takeIf { minutesOfDay(it) != null }

  /** "HH:mm" -> minutes since midnight, or null if malformed. */
  internal fun minutesOfDay(hhmm: String): Int? {
    val parts = hhmm.trim().split(":")
    if (parts.size < 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].take(2).toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h * 60 + m
  }

  private fun formatMinutes(total: Int): String {
    val n = ((total % 1440) + 1440) % 1440
    return String.format(Locale.US, "%02d:%02d", n / 60, n % 60)
  }

  private fun formatHour12(hour24: Int): String = when (hour24) {
    0 -> "12 AM"
    12 -> "12 PM"
    in 1..11 -> "$hour24 AM"
    else -> "${hour24 - 12} PM"
  }

  /** Parses a date-only ISO string without involving the device time zone. */
  private fun parseDate(iso: String): Date? = try {
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
      timeZone = TimeZone.getTimeZone("UTC")
      isLenient = false
    }.parse(iso)
  } catch (_: Exception) {
    null
  }

  internal fun formatDate(iso: String, pattern: String): String? {
    val date = parseDate(iso) ?: return null
    return SimpleDateFormat(pattern, Locale.getDefault()).apply {
      timeZone = TimeZone.getTimeZone("UTC")
    }.format(date)
  }

  /** Magnus approximation; only used if the service omits the dew point. */
  private fun dewPointMagnus(tempC: Double, humidity: Int): Double {
    val a = 17.27
    val b = 237.7
    val alpha = ((a * tempC) / (b + tempC)) + ln(humidity.coerceIn(1, 100) / 100.0)
    return ((b * alpha) / (a - alpha)).coerceIn(-30.0, 45.0)
  }

  /**
   * Estimated minutes between sunrise and civil dawn (equally sunset and civil dusk) for the
   * given latitude and date. Returns null when the sun does not cross those angles that day
   * (polar regions).
   */
  internal fun civilTwilightOffsetMinutes(latitudeDeg: Double, isoDate: String): Int? {
    val date = parseDate(isoDate) ?: return null
    val dayOfYear = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = date }
      .get(Calendar.DAY_OF_YEAR)
    val declination = Math.toRadians(23.44) * sin(2.0 * PI * (284 + dayOfYear) / 365.0)
    val lat = Math.toRadians(latitudeDeg)

    fun hourAngleDeg(zenithDeg: Double): Double? {
      val c = (cos(Math.toRadians(zenithDeg)) - sin(lat) * sin(declination)) / (cos(lat) * cos(declination))
      return if (c in -1.0..1.0) Math.toDegrees(acos(c)) else null
    }

    val sunHa = hourAngleDeg(90.833) ?: return null
    val civilHa = hourAngleDeg(96.0) ?: return null
    return ((civilHa - sunHa) * 4.0).roundToInt() // 15 degrees of hour angle = 60 minutes
  }
}

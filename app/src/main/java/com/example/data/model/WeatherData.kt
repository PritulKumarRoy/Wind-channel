package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlin.math.cos
import kotlin.math.roundToInt

data class City(
  val id: String,
  val name: String,
  val region: String,
  val country: String,
  val latitude: Double,
  val longitude: Double,
  val isDefault: Boolean = false
)

val DefaultGlobalCities = listOf(
  City("sf", "San Francisco", "California", "United States", 37.7749, -122.4194, isDefault = true),
  City("ny", "New York", "New York", "United States", 40.7128, -74.0060),
  City("london", "London", "Greater London", "United Kingdom", 51.5074, -0.1278),
  City("tokyo", "Tokyo", "Tokyo", "Japan", 35.6762, 139.6503),
  City("paris", "Paris", "Île-de-France", "France", 48.8566, 2.3522),
  City("sydney", "Sydney", "New South Wales", "Australia", -33.8688, 151.2093),
  City("berlin", "Berlin", "Berlin", "Germany", 52.5200, 13.4050),
  City("singapore", "Singapore", "Central", "Singapore", 1.3521, 103.8198),
  City("dubai", "Dubai", "Dubai", "United Arab Emirates", 25.2048, 55.2708),
  City("toronto", "Toronto", "Ontario", "Canada", 43.6532, -79.3832)
)

enum class ModernWeatherIconType {
  CLEAR_DAY,
  CLEAR_NIGHT,
  PARTLY_CLOUDY_DAY,
  PARTLY_CLOUDY_NIGHT,
  CLOUDY,
  RAIN_LIGHT,
  RAIN,
  RAIN_HEAVY,
  THUNDERSTORM,
  SNOW,
  FOG,
  WIND
}

enum class AtmosphereType {
  CLEAR_DAY,
  CLEAR_NIGHT,
  CLOUDY,
  RAIN,
  STORM,
  SNOW
}

data class ModernWeatherCondition(
  val code: Int,
  val title: String,
  val summary: String,
  val iconType: ModernWeatherIconType,
  val atmosphere: AtmosphereType
)

fun mapWeatherCodeToModern(code: Int, isDay: Boolean = true): ModernWeatherCondition {
  return when (code) {
    0 -> if (isDay) {
      ModernWeatherCondition(0, "Clear Sky", "Sunny and bright conditions all day", ModernWeatherIconType.CLEAR_DAY, AtmosphereType.CLEAR_DAY)
    } else {
      ModernWeatherCondition(0, "Clear Night", "Clear starry skies with calm winds", ModernWeatherIconType.CLEAR_NIGHT, AtmosphereType.CLEAR_NIGHT)
    }
    1 -> if (isDay) {
      ModernWeatherCondition(1, "Mainly Clear", "Mostly sunny with a few passing clouds", ModernWeatherIconType.PARTLY_CLOUDY_DAY, AtmosphereType.CLEAR_DAY)
    } else {
      ModernWeatherCondition(1, "Partly Cloudy", "Passing high clouds overnight", ModernWeatherIconType.PARTLY_CLOUDY_NIGHT, AtmosphereType.CLEAR_NIGHT)
    }
    2 -> if (isDay) {
      ModernWeatherCondition(2, "Partly Cloudy", "Sun and scattered cumulus clouds", ModernWeatherIconType.PARTLY_CLOUDY_DAY, AtmosphereType.CLOUDY)
    } else {
      ModernWeatherCondition(2, "Partly Cloudy", "Scattered cloud patches tonight", ModernWeatherIconType.PARTLY_CLOUDY_NIGHT, AtmosphereType.CLEAR_NIGHT)
    }
    3 -> ModernWeatherCondition(3, "Overcast", "Uniform cloud deck covering the sky", ModernWeatherIconType.CLOUDY, AtmosphereType.CLOUDY)
    45, 48 -> ModernWeatherCondition(code, "Foggy", "Low visibility and morning fog patches", ModernWeatherIconType.FOG, AtmosphereType.CLOUDY)
    51, 53 -> ModernWeatherCondition(code, "Light Drizzle", "Intermittent fine drizzle droplets", ModernWeatherIconType.RAIN_LIGHT, AtmosphereType.RAIN)
    55 -> ModernWeatherCondition(55, "Dense Drizzle", "Persistent misty drizzle dampening streets", ModernWeatherIconType.RAIN_LIGHT, AtmosphereType.RAIN)
    61, 63 -> ModernWeatherCondition(code, "Rain Showers", "Steady rain showers expected", ModernWeatherIconType.RAIN, AtmosphereType.RAIN)
    65 -> ModernWeatherCondition(65, "Heavy Rain", "Substantial downpours and wet roads", ModernWeatherIconType.RAIN_HEAVY, AtmosphereType.RAIN)
    71, 73, 75, 77 -> ModernWeatherCondition(code, "Snow Fall", "Cold crisp snowfall blanketing ground", ModernWeatherIconType.SNOW, AtmosphereType.SNOW)
    80, 81 -> ModernWeatherCondition(code, "Scattered Showers", "Passing brief rain showers", ModernWeatherIconType.RAIN, AtmosphereType.RAIN)
    82 -> ModernWeatherCondition(82, "Violent Downpour", "Heavy torrential rain shower bursts", ModernWeatherIconType.RAIN_HEAVY, AtmosphereType.RAIN)
    85, 86 -> ModernWeatherCondition(code, "Snow Flurries", "Blowing snow squalls and gusty winds", ModernWeatherIconType.SNOW, AtmosphereType.SNOW)
    95, 96, 99 -> ModernWeatherCondition(code, "Thunderstorm", "Active lightning and gusty storm front", ModernWeatherIconType.THUNDERSTORM, AtmosphereType.STORM)
    else -> ModernWeatherCondition(code, "Partly Cloudy", "Scattered cloud cover across the horizon", ModernWeatherIconType.CLOUDY, AtmosphereType.CLOUDY)
  }
}

enum class PressureTrend {
  RISING,
  STEADY,
  FALLING,
  /** Not enough pressure readings to judge a trend. */
  UNKNOWN
}

/** Change in station pressure (hPa) over the last 3 hours that counts as "rising" / "falling". */
const val PRESSURE_TREND_THRESHOLD_HPA = 1.0

/** [deltaHpa] is the pressure change over the previous three hours, or null if unavailable. */
fun calculatePressureTrend(deltaHpa: Double?): PressureTrend = when {
  deltaHpa == null -> PressureTrend.UNKNOWN
  deltaHpa >= PRESSURE_TREND_THRESHOLD_HPA -> PressureTrend.RISING
  deltaHpa <= -PRESSURE_TREND_THRESHOLD_HPA -> PressureTrend.FALLING
  else -> PressureTrend.STEADY
}

data class ModernWind(
  val degrees: Double,
  val speedKmh: Double,
  /** Gust speed reported by the weather service, or null when the service did not provide one. */
  val gustSpeedKmh: Double?,
  val directionCardinal: String,
  val description: String
) {
  val speedMph: Double get() = UnitConversions.kmhToMph(speedKmh)
  val gustSpeedMph: Double? get() = gustSpeedKmh?.let { UnitConversions.kmhToMph(it) }
}

fun calculateModernWind(degrees: Double, speedKmh: Double, gustKmh: Double? = null): ModernWind {
  val normalized = ((degrees % 360) + 360) % 360
  val cardinal = when {
    normalized >= 337.5 || normalized < 22.5 -> "N"
    normalized < 67.5 -> "NE"
    normalized < 112.5 -> "E"
    normalized < 157.5 -> "SE"
    normalized < 202.5 -> "S"
    normalized < 247.5 -> "SW"
    normalized < 292.5 -> "W"
    else -> "NW"
  }
  val mph = UnitConversions.kmhToMph(speedKmh)
  val desc = when {
    mph < 4.0 -> "Calm air"
    mph < 12.0 -> "Light gentle breeze"
    mph < 24.0 -> "Moderate steady breeze"
    mph < 38.0 -> "Strong brisk winds"
    else -> "High gale force winds"
  }
  return ModernWind(degrees, speedKmh, gustKmh, cardinal, desc)
}

data class ModernUvIndex(
  val value: Double,
  val rating: String,
  val advice: String,
  val peakHours: String = "11 AM – 4 PM"
)

fun calculateUvRating(uv: Double): ModernUvIndex {
  return when {
    uv < 3.0 -> ModernUvIndex(uv, "Low", "No protection required for ordinary outdoor exposure")
    uv < 6.0 -> ModernUvIndex(uv, "Moderate", "Wear sunscreen and sunglasses when outdoors during midday")
    uv < 8.0 -> ModernUvIndex(uv, "High", "Protection essential: apply SPF 30+ and seek shade near noon")
    uv < 11.0 -> ModernUvIndex(uv, "Very High", "Extra precautions required: minimize direct sun exposure")
    else -> ModernUvIndex(uv, "Extreme", "Take all precautions: skin can burn in minutes")
  }
}

data class WeatherAdvisory(
  val title: String,
  val level: String, // "Info", "Advisory", "Warning"
  val description: String,
  val icon: String
)

data class HourlyForecastItem(
  val timeLabel: String,
  val hourInt: Int,
  val isNow: Boolean,
  val tempC: Double,
  val weatherCode: Int,
  /** Null when the weather service has no probability for this hour. */
  val precipitationProb: Int?,
  val condition: ModernWeatherCondition,
  /** Null when the weather service has no wind reading for this hour. */
  val windSpeedKmh: Double?
)

data class DailyForecastItem(
  /** ISO date (yyyy-MM-dd) exactly as returned by the weather service, in the city's local time zone. */
  val isoDate: String,
  val dayLabel: String,
  val fullDate: String,
  val maxTempC: Double,
  val minTempC: Double,
  val weatherCode: Int,
  val condition: ModernWeatherCondition,
  val precipitationProb: Int?,
  val sunriseTime: String?,
  val sunsetTime: String?,
  val maxUv: Double?
)

data class MoonPhase(
  val name: String,
  val illuminationPercent: Int,
  val phaseQuarterRatio: Float // 0 to 1
)

/** Moon phase is calculated from the average lunar cycle (not supplied by the weather service). */
fun calculateMoonPhase(nowMillis: Long): MoonPhase {
  val refNewMoonMs = 1704974400000L // 2024-01-11 12:00 UTC (new moon)
  val synodicPeriodMs = 29.53058867 * 24 * 60 * 60 * 1000
  val diff = ((nowMillis - refNewMoonMs) % synodicPeriodMs + synodicPeriodMs) % synodicPeriodMs
  val phaseRatio = (diff / synodicPeriodMs).toFloat().coerceIn(0f, 1f)
  val illum = (0.5 * (1 - cos(phaseRatio * 2 * Math.PI)) * 100).roundToInt()

  val name = when {
    phaseRatio < 0.05f || phaseRatio > 0.95f -> "New Moon"
    phaseRatio < 0.22f -> "Waxing Crescent"
    phaseRatio < 0.28f -> "First Quarter"
    phaseRatio < 0.45f -> "Waxing Gibbous"
    phaseRatio < 0.55f -> "Full Moon"
    phaseRatio < 0.72f -> "Waning Gibbous"
    phaseRatio < 0.78f -> "Last Quarter"
    else -> "Waning Crescent"
  }
  return MoonPhase(name, illum, phaseRatio)
}

data class CompleteWeatherData(
  val city: City,
  /** Wall-clock time (epoch millis) at which this forecast was downloaded. */
  val fetchedAtMillis: Long,
  /** Minutes since local midnight in the city's own time zone, taken from the weather service. */
  val localTimeMinutes: Int,
  val temperatureC: Double,
  val apparentTempC: Double,
  val todayMaxC: Double,
  val todayMinC: Double,
  val relativeHumidity: Int,
  val dewPointC: Double,
  val weatherCode: Int,
  val condition: ModernWeatherCondition,
  val pressureHpa: Double,
  val pressureTrend: PressureTrend,
  val wind: ModernWind,
  /** Null when the weather service has no visibility value. */
  val visibilityKm: Double?,
  val cloudCoverPercent: Int?,
  /** Today's forecast maximum UV index; null when unavailable. */
  val uv: ModernUvIndex?,
  val isDay: Boolean,
  /** Local HH:mm. Null during polar day/night. */
  val sunrise: String?,
  val sunset: String?,
  /** ESTIMATED civil dawn/dusk (HH:mm), calculated from sunrise/sunset and latitude. */
  val firstLight: String?,
  val dusk: String?,
  val hourly: List<HourlyForecastItem>,
  val daily: List<DailyForecastItem>,
  /** Calculated from the lunar cycle, not from the weather service. */
  val moonPhase: MoonPhase,
  val advisory: WeatherAdvisory?
)

// Retrofit Raw DTOs.
// Open-Meteo returns `null` for individual array entries it has no data for, so every list
// element is nullable. Numeric fields that may arrive as 55 or 55.0 are read as Double.
@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
  val latitude: Double? = null,
  val longitude: Double? = null,
  val current: CurrentData? = null,
  val hourly: HourlyData? = null,
  val daily: DailyData? = null
)

@JsonClass(generateAdapter = true)
data class CurrentData(
  /** Local ISO-8601 time of the observation in the city's time zone, e.g. 2026-10-21T14:15 */
  val time: String? = null,
  @Json(name = "temperature_2m") val temperature: Double? = null,
  @Json(name = "apparent_temperature") val apparentTemperature: Double? = null,
  @Json(name = "relative_humidity_2m") val relativeHumidity: Double? = null,
  @Json(name = "is_day") val isDay: Int? = null,
  @Json(name = "weather_code") val weatherCode: Int? = null,
  @Json(name = "surface_pressure") val surfacePressure: Double? = null,
  @Json(name = "wind_speed_10m") val windSpeed: Double? = null,
  @Json(name = "wind_direction_10m") val windDirection: Double? = null,
  @Json(name = "wind_gusts_10m") val windGusts: Double? = null,
  @Json(name = "cloud_cover") val cloudCover: Double? = null
)

@JsonClass(generateAdapter = true)
data class HourlyData(
  val time: List<String?>? = null,
  @Json(name = "temperature_2m") val temperature: List<Double?>? = null,
  @Json(name = "weather_code") val weatherCode: List<Int?>? = null,
  @Json(name = "precipitation_probability") val precipitationProbability: List<Double?>? = null,
  @Json(name = "surface_pressure") val surfacePressure: List<Double?>? = null,
  @Json(name = "wind_speed_10m") val windSpeed: List<Double?>? = null,
  /** Metres */
  @Json(name = "visibility") val visibility: List<Double?>? = null,
  @Json(name = "dew_point_2m") val dewPoint: List<Double?>? = null,
  @Json(name = "is_day") val isDay: List<Int?>? = null
)

@JsonClass(generateAdapter = true)
data class DailyData(
  val time: List<String?>? = null,
  @Json(name = "weather_code") val weatherCode: List<Int?>? = null,
  @Json(name = "temperature_2m_max") val temperatureMax: List<Double?>? = null,
  @Json(name = "temperature_2m_min") val temperatureMin: List<Double?>? = null,
  @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Double?>? = null,
  val sunrise: List<String?>? = null,
  val sunset: List<String?>? = null,
  @Json(name = "uv_index_max") val uvIndexMax: List<Double?>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
  val results: List<GeocodingResult>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResult(
  val id: Long? = null,
  val name: String? = null,
  val latitude: Double? = null,
  val longitude: Double? = null,
  val country: String? = null,
  @Json(name = "admin1") val admin1: String? = null
)

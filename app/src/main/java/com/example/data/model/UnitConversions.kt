package com.example.data.model

import kotlin.math.roundToInt

/** All unit conversions used by the UI live here so they can be unit-tested. */
object UnitConversions {
  fun celsiusToFahrenheit(celsius: Double): Double = celsius * 9.0 / 5.0 + 32.0
  fun kmhToMph(kmh: Double): Double = kmh * 0.621371
  fun kmToMiles(km: Double): Double = km * 0.621371
  fun hPaToInHg(hPa: Double): Double = hPa * 0.02953
}

/** Rounded temperature with a degree sign, e.g. "21°" or "70°". */
fun formatTemperature(celsius: Double, fahrenheit: Boolean): String {
  val value = if (fahrenheit) UnitConversions.celsiusToFahrenheit(celsius) else celsius
  return "${value.roundToInt()}°"
}

/** Wind speed rounded to a whole number with unit, e.g. "18 km/h" or "11 mph". */
fun formatWindSpeed(kmh: Double, imperial: Boolean): String =
  if (imperial) "${UnitConversions.kmhToMph(kmh).roundToInt()} mph" else "${kmh.roundToInt()} km/h"

/** Visibility with one decimal below 10 units so that e.g. 0.8 km is not shown as "0 km". */
fun formatVisibility(km: Double, imperial: Boolean): String {
  val value = if (imperial) UnitConversions.kmToMiles(km) else km
  val unit = if (imperial) "mi" else "km"
  return if (value < 10.0) String.format(java.util.Locale.US, "%.1f %s", value, unit)
  else "${value.roundToInt()} $unit"
}

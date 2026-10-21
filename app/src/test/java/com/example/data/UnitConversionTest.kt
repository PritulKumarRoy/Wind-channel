package com.example.data

import com.example.data.model.PressureTrend
import com.example.data.model.UnitConversions
import com.example.data.model.calculateModernWind
import com.example.data.model.calculateMoonPhase
import com.example.data.model.calculatePressureTrend
import com.example.data.model.formatTemperature
import com.example.data.model.formatVisibility
import com.example.data.model.formatWindSpeed
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConversionTest {

  @Test fun `celsius to fahrenheit`() {
    assertEquals(32.0, UnitConversions.celsiusToFahrenheit(0.0), 1e-9)
    assertEquals(212.0, UnitConversions.celsiusToFahrenheit(100.0), 1e-9)
    assertEquals(-40.0, UnitConversions.celsiusToFahrenheit(-40.0), 1e-9)
    assertEquals(68.0, UnitConversions.celsiusToFahrenheit(20.0), 1e-9)
  }

  @Test fun `temperature formatting rounds and respects unit`() {
    assertEquals("21°", formatTemperature(20.6, fahrenheit = false))
    assertEquals("70°", formatTemperature(21.0, fahrenheit = true))
    assertEquals("-4°", formatTemperature(-4.2, fahrenheit = false))
    assertEquals("32°", formatTemperature(0.0, fahrenheit = true))
  }

  @Test fun `wind and distance conversions`() {
    assertEquals(62.1371, UnitConversions.kmhToMph(100.0), 1e-3)
    assertEquals("18 km/h", formatWindSpeed(18.2, imperial = false))
    assertEquals("11 mph", formatWindSpeed(18.2, imperial = true))
    assertEquals("0.8 km", formatVisibility(0.8, imperial = false))
    assertEquals("24 km", formatVisibility(24.14, imperial = false))
    assertEquals("15 mi", formatVisibility(24.14, imperial = true))
  }

  @Test fun `pressure conversion`() {
    assertEquals(29.92, UnitConversions.hPaToInHg(1013.25), 0.01)
  }

  @Test fun `pressure trend thresholds`() {
    assertEquals(PressureTrend.RISING, calculatePressureTrend(1.0))
    assertEquals(PressureTrend.FALLING, calculatePressureTrend(-1.0))
    assertEquals(PressureTrend.STEADY, calculatePressureTrend(0.4))
    assertEquals(PressureTrend.UNKNOWN, calculatePressureTrend(null))
  }

  @Test fun `wind cardinal directions and gust handling`() {
    assertEquals("N", calculateModernWind(0.0, 10.0).directionCardinal)
    assertEquals("N", calculateModernWind(359.0, 10.0).directionCardinal)
    assertEquals("E", calculateModernWind(90.0, 10.0).directionCardinal)
    assertEquals("SW", calculateModernWind(225.0, 10.0).directionCardinal)
    assertEquals(null, calculateModernWind(0.0, 10.0).gustSpeedKmh)
    assertEquals(25.0, calculateModernWind(0.0, 10.0, 25.0).gustSpeedKmh!!, 0.0)
  }

  @Test fun `moon phase at a known new and full moon`() {
    val newMoon = calculateMoonPhase(1704974400000L) // 2024-01-11 12:00 UTC
    assertEquals("New Moon", newMoon.name)
    val full = calculateMoonPhase(1704974400000L + (14.765 * 86_400_000).toLong())
    assertEquals("Full Moon", full.name)
  }
}

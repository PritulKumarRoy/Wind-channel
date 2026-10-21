package com.example.data

import com.example.data.model.PressureTrend
import com.example.data.model.WeatherException
import com.example.data.repository.OpenMeteoParser
import com.example.testing.TestData
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

class OpenMeteoParserTest {

  private val originalTz = TimeZone.getDefault()
  private val originalLocale = Locale.getDefault()

  @Before fun setUp() {
    Locale.setDefault(Locale.US)
  }

  @After fun tearDown() {
    TimeZone.setDefault(originalTz)
    Locale.setDefault(originalLocale)
  }

  private fun parse(r: com.example.data.model.OpenMeteoResponse = TestData.response()) =
    OpenMeteoParser.parse(TestData.city, r, fetchedAtMillis = 1_800_000_000_000L)

  @Test fun `hourly wind comes from the API and starts at the current hour`() {
    val data = parse()
    assertEquals(24, data.hourly.size)
    assertEquals("Now", data.hourly[0].timeLabel)
    assertEquals(14, data.hourly[0].hourInt)
    assertTrue(data.hourly[0].isNow)
    // wind series is index-valued, so the value proves which array slot was used
    assertEquals(38.0, data.hourly[0].windSpeedKmh!!, 0.0)
    assertEquals(39.0, data.hourly[1].windSpeedKmh!!, 0.0)
    assertEquals("3 PM", data.hourly[1].timeLabel)
  }

  @Test fun `missing hourly wind stays null instead of falling back to current wind`() {
    val data = parse(TestData.response(hourly = TestData.hourly(wind = null)))
    assertTrue(data.hourly.all { it.windSpeedKmh == null })
    assertEquals(18.0, data.wind.speedKmh, 0.0) // current wind is unaffected
  }

  @Test fun `null entries inside hourly arrays are tolerated`() {
    val prec = TestData.hourTimes.indices.map { if (it == TestData.NOW_INDEX) null else 40.0 }
    val data = parse(TestData.response(hourly = TestData.hourly(precipitation = prec)))
    assertNull(data.hourly[0].precipitationProb)
    assertEquals(40, data.hourly[1].precipitationProb)
  }

  @Test fun `daily labels come from API dates not the device calendar or time zone`() {
    TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati")) // UTC+14
    val data = parse()
    assertEquals(3, data.daily.size) // yesterday (past_days) is dropped
    assertEquals("2026-10-21", data.daily[0].isoDate)
    assertEquals("Today", data.daily[0].dayLabel)
    assertEquals("Thu", data.daily[1].dayLabel)
    assertEquals("Oct 22", data.daily[1].fullDate)
    assertEquals("Fri", data.daily[2].dayLabel)
    assertEquals("Oct 23", data.daily[2].fullDate)
  }

  @Test fun `daily precipitation and uv come from the API`() {
    val data = parse()
    assertEquals(20, data.daily[0].precipitationProb)
    assertEquals(65, data.daily[1].precipitationProb)
    assertEquals(6.0, data.uv!!.value, 0.0)
  }

  @Test fun `missing uv and precipitation stay null`() {
    val data = parse(TestData.response(daily = TestData.daily(uv = null, precipitation = null)))
    assertNull(data.uv)
    assertNull(data.daily[0].precipitationProb)
  }

  @Test fun `gusts are taken from the API, not calculated`() {
    assertEquals(30.0, parse().wind.gustSpeedKmh!!, 0.0)
    assertNull(parse(TestData.response(current = TestData.current(gusts = null))).wind.gustSpeedKmh)
  }

  @Test fun `visibility comes from hourly data in kilometres`() {
    assertEquals(24.14, parse().visibilityKm!!, 0.001)
    assertNull(parse(TestData.response(hourly = TestData.hourly(visibility = null))).visibilityKm)
  }

  @Test fun `dew point comes from the API when present and is calculated otherwise`() {
    assertEquals(11.0, parse().dewPointC, 0.0)
    val calc = parse(TestData.response(hourly = TestData.hourly(dewPoint = null))).dewPointC
    assertEquals(11.4, calc, 0.5) // 21 C at 55 % RH
  }

  @Test fun `pressure trend uses the previous three hours`() {
    assertEquals(PressureTrend.RISING, parse().pressureTrend) // +1.5 hPa
    val falling = TestData.hourTimes.indices.map { 1020.0 - it * 0.5 }
    assertEquals(PressureTrend.FALLING, parse(TestData.response(hourly = TestData.hourly(pressure = falling))).pressureTrend)
    val flat = TestData.hourTimes.indices.map { 1013.0 }
    assertEquals(PressureTrend.STEADY, parse(TestData.response(hourly = TestData.hourly(pressure = flat))).pressureTrend)
    assertEquals(PressureTrend.UNKNOWN, parse(TestData.response(hourly = TestData.hourly(pressure = null))).pressureTrend)
  }

  @Test fun `local time and sun times come from the city's own timestamps`() {
    val data = parse()
    assertEquals(14 * 60 + 15, data.localTimeMinutes)
    assertEquals("06:30", data.sunrise)
    assertEquals("18:30", data.sunset)
  }

  @Test fun `missing sunrise and sunset yield null instead of default times`() {
    val data = parse(TestData.response(daily = TestData.daily(sunrise = null, sunset = null)))
    assertNull(data.sunrise)
    assertNull(data.sunset)
    assertNull(data.firstLight)
    assertNull(data.dusk)
  }

  @Test fun `first light is earlier than sunrise and dusk later than sunset`() {
    val data = parse()
    assertNotNull(data.firstLight)
    assertTrue(data.firstLight!! < "06:30")
    assertTrue(data.dusk!! > "18:30")
  }

  @Test fun `civil twilight estimate is sensible at the equator and null in polar summer`() {
    val equator = OpenMeteoParser.civilTwilightOffsetMinutes(0.0, "2026-03-20")!!
    assertTrue("was $equator", equator in 20..30)
    assertNull(OpenMeteoParser.civilTwilightOffsetMinutes(80.0, "2026-06-21"))
  }

  @Test fun `missing required current fields raise InvalidData instead of inventing values`() {
    assertInvalid(TestData.response(current = null))
    assertInvalid(TestData.response(current = TestData.current(temperature = null)))
    assertInvalid(TestData.response(current = TestData.current(weatherCode = null)))
    assertInvalid(TestData.response(current = TestData.current(time = null)))
  }

  @Test fun `forecast that does not cover now raises InvalidData`() {
    assertInvalid(TestData.response(current = TestData.current(time = "2030-01-01T10:00")))
    assertInvalid(TestData.response(hourly = null))
    assertInvalid(TestData.response(daily = null))
  }

  @Test fun `advisories are not presented as official warnings`() {
    val storm = parse(TestData.response(current = TestData.current(weatherCode = 95)))
    assertNotNull(storm.advisory)
    assertTrue(storm.advisory!!.description.contains("not an official warning"))
    assertNull(parse().advisory)
  }

  private fun assertInvalid(response: com.example.data.model.OpenMeteoResponse) {
    try {
      parse(response)
      throw AssertionError("expected InvalidData")
    } catch (e: WeatherException.InvalidData) {
      // expected
    }
  }
}

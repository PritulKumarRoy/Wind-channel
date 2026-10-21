package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.calculateModernWind
import com.example.data.model.mapWeatherCodeToModern
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Wind Channel", appName)
  }

  @Test
  fun `verify modern weather mapping`() {
    val sunny = mapWeatherCodeToModern(0, isDay = true)
    assertEquals("Clear Sky", sunny.title)

    val thunder = mapWeatherCodeToModern(95, isDay = true)
    assertEquals("Thunderstorm", thunder.title)

    val wind = calculateModernWind(0.0, 20.0)
    assertEquals("N", wind.directionCardinal)
  }

  @Test
  fun `verify MainActivity launches successfully`() {
    org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
  }
}

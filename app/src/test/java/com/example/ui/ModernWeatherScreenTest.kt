package com.example.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.repository.WeatherRepository
import com.example.testing.FakeWeatherApi
import com.example.testing.MainDispatcherRule
import com.example.ui.screens.ModernWeatherScreen
import com.example.ui.theme.WindChannelTheme
import com.example.ui.viewmodel.WeatherViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

/** Screen-level behaviour for the failure scenarios (runs on the JVM via Robolectric). */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ModernWeatherScreenTest {

  @get:Rule val main = MainDispatcherRule()
  @get:Rule val compose = createComposeRule()

  private val api = FakeWeatherApi()

  private fun viewModel() = WeatherViewModel(
    WeatherRepository(api, UnconfinedTestDispatcher(main.scheduler)) { 1_800_000_000_000L }
  )

  private fun show(vm: WeatherViewModel) = compose.setContent {
    WindChannelTheme(darkTheme = false) { ModernWeatherScreen(viewModel = vm) }
  }

  @Test fun `no connection and no earlier forecast shows an error with retry - not weather`() {
    api.forecast = { _, _ -> throw IOException("offline") }
    show(viewModel())

    compose.onNodeWithText("Unable to load forecast").assertIsDisplayed()
    compose.onNodeWithTag("retry_button").assertIsDisplayed()
  }

  @Test fun `failed refresh labels the old forecast as not live`() {
    val vm = viewModel()
    show(vm)
    api.forecast = { _, _ -> throw IOException("offline") }
    compose.runOnUiThread { vm.refreshWeather() }
    compose.waitForIdle()

    compose.onNodeWithText("Live data unavailable", substring = true).assertIsDisplayed()
  }

  @Test fun `retry recovers from an error`() {
    api.forecast = { _, _ -> throw IOException("offline") }
    show(viewModel())
    compose.onNodeWithText("Unable to load forecast").assertIsDisplayed()

    api.forecast = { _, _ -> com.example.testing.TestData.response() }
    compose.onNodeWithTag("retry_button").performClick()
    compose.waitForIdle()

    compose.onNodeWithText("Unable to load forecast").assertDoesNotExist()
  }
}

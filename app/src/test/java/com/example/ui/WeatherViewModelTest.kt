package com.example.ui

import com.example.data.model.City
import com.example.data.model.DefaultGlobalCities
import com.example.data.model.GeocodingResponse
import com.example.data.model.GeocodingResult
import com.example.data.repository.WeatherRepository
import com.example.testing.FakeWeatherApi
import com.example.testing.MainDispatcherRule
import com.example.testing.TestData
import com.example.ui.viewmodel.WeatherUiState
import com.example.ui.viewmodel.WeatherViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

  @get:Rule val main = MainDispatcherRule()

  private val api = FakeWeatherApi()

  private fun viewModel(): WeatherViewModel {
    val repo = WeatherRepository(api, UnconfinedTestDispatcher(main.scheduler)) { 1_800_000_000_000L }
    return WeatherViewModel(repo)
  }

  @Test fun `successful load shows live data`() {
    val vm = viewModel()
    val ui = vm.state.value.uiState
    assertTrue(ui is WeatherUiState.Success)
    assertEquals(DefaultGlobalCities[0], (ui as WeatherUiState.Success).data.city)
  }

  @Test fun `failed first load is an error with NO data - nothing is fabricated`() {
    api.forecast = { _, _ -> throw IOException("offline") }
    val vm = viewModel()
    val ui = vm.state.value.uiState
    assertTrue(ui is WeatherUiState.Error)
    ui as WeatherUiState.Error
    assertNull(ui.cachedData)
    assertTrue(ui.message.contains("internet", ignoreCase = true))
  }

  @Test fun `failed refresh keeps the last REAL forecast and reports an error`() {
    val vm = viewModel()
    val real = (vm.state.value.uiState as WeatherUiState.Success).data

    api.forecast = { _, _ -> throw IOException("offline") }
    vm.refreshWeather()

    val ui = vm.state.value.uiState as WeatherUiState.Error
    assertSame(real, ui.cachedData)
    assertFalse(vm.state.value.isRefreshing)
  }

  @Test fun `recovers when the connection returns`() {
    api.forecast = { _, _ -> throw IOException("offline") }
    val vm = viewModel()
    assertTrue(vm.state.value.uiState is WeatherUiState.Error)

    api.forecast = { _, _ -> TestData.response() }
    vm.refreshWeather()
    assertTrue(vm.state.value.uiState is WeatherUiState.Success)
  }

  @Test fun `cached forecast of one city is never shown for another city`() {
    val vm = viewModel() // loads default city successfully
    api.forecast = { _, _ -> throw IOException("offline") }
    vm.loadWeatherForCity(DefaultGlobalCities[1])
    val ui = vm.state.value.uiState as WeatherUiState.Error
    assertNull(ui.cachedData)
  }

  @Test fun `a slow older request cannot overwrite a newer city`() {
    val vm = viewModel()
    val gate = CompletableDeferred<Unit>()
    val slowCity = DefaultGlobalCities[1]
    val fastCity = DefaultGlobalCities[2]
    api.forecast = { lat, _ ->
      if (lat == slowCity.latitude) gate.await()
      TestData.response()
    }
    vm.loadWeatherForCity(slowCity)
    vm.loadWeatherForCity(fastCity)
    gate.complete(Unit)
    main.scheduler.advanceUntilIdle()

    assertEquals(fastCity, vm.state.value.selectedCity)
    assertEquals(fastCity, (vm.state.value.uiState as WeatherUiState.Success).data.city)
  }

  @Test fun `empty search result is not an error`() {
    api.search = { GeocodingResponse(results = null) }
    val vm = viewModel()
    vm.onSearchQueryChanged("zzzz")
    main.scheduler.advanceUntilIdle()
    assertTrue(vm.state.value.searchResults.isEmpty())
    assertNull(vm.state.value.searchError)
    assertFalse(vm.state.value.isSearching)
  }

  @Test fun `search results are shown`() {
    api.search = {
      GeocodingResponse(listOf(GeocodingResult(7, "Kyoto", 35.0, 135.7, "Japan", "Kyoto")))
    }
    val vm = viewModel()
    vm.onSearchQueryChanged("Kyoto")
    main.scheduler.advanceUntilIdle()
    assertEquals("Kyoto", vm.state.value.searchResults.single().name)
  }

  @Test fun `search outage falls back to built-in cities and says so`() {
    api.search = { throw IOException("offline") }
    val vm = viewModel()
    vm.onSearchQueryChanged("tok")
    main.scheduler.advanceUntilIdle()
    assertEquals("Tokyo", vm.state.value.searchResults.single().name)
    assertTrue(vm.state.value.searchError!!.contains("built-in"))
  }

  @Test fun `selecting a city that is already saved does not duplicate it`() {
    val vm = viewModel()
    val before = vm.state.value.savedCities.size
    val sameAsSf = City("geo_5391959", "San Francisco", "California", "United States", 37.77, -122.42)
    vm.selectCity(sameAsSf)
    assertEquals(before, vm.state.value.savedCities.size)
  }

  @Test fun `same city name in a different region is kept separately`() {
    val vm = viewModel()
    val before = vm.state.value.savedCities.size
    vm.selectCity(City("geo_1", "Springfield", "Illinois", "United States", 39.8, -89.6))
    vm.selectCity(City("geo_2", "Springfield", "Missouri", "United States", 37.2, -93.3))
    assertEquals(before + 2, vm.state.value.savedCities.size)
  }

  @Test fun `removing the selected city loads the next saved city`() {
    val vm = viewModel()
    val selected = vm.state.value.selectedCity
    val next = vm.state.value.savedCities.first { it.id != selected.id }
    vm.removeCity(selected)
    assertEquals(vm.state.value.savedCities.first(), vm.state.value.selectedCity)
    assertEquals(next, vm.state.value.savedCities.first())
    assertEquals(next.latitude, api.requestedLatitudes.last(), 0.0)
    assertTrue(vm.state.value.uiState is WeatherUiState.Success)
  }

  @Test fun `the last saved city cannot be removed`() {
    val vm = viewModel()
    vm.state.value.savedCities.drop(1).forEach { vm.removeCity(it) }
    val only = vm.state.value.savedCities.single()
    vm.removeCity(only)
    assertEquals(1, vm.state.value.savedCities.size)
  }

  @Test fun `system theme is applied once and then the user's choice wins`() {
    val vm = viewModel()
    vm.applySystemTheme(true)
    assertTrue(vm.state.value.isDarkMode)
    vm.toggleTheme()
    vm.applySystemTheme(true)
    assertFalse(vm.state.value.isDarkMode)
  }
}

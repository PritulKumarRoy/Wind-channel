package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.City
import com.example.data.model.CompleteWeatherData
import com.example.data.model.DefaultGlobalCities
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

sealed interface WeatherUiState {
  data object Loading : WeatherUiState
  data class Success(val data: CompleteWeatherData) : WeatherUiState
  data class Error(val message: String, val cachedData: CompleteWeatherData?) : WeatherUiState
}

data class WeatherScreenState(
  val uiState: WeatherUiState = WeatherUiState.Loading,
  val selectedCity: City = DefaultGlobalCities[0],
  val savedCities: List<City> = DefaultGlobalCities.take(6),
  val isFahrenheit: Boolean = false,
  val selectedHourlyTab: Int = 0, // 0: Temperature, 1: Precipitation %, 2: Wind
  val searchQuery: String = "",
  val searchResults: List<City> = emptyList(),
  val isSearching: Boolean = false,
  /** Set when the search service could not be reached (results then only contain built-in cities). */
  val searchError: String? = null,
  val showSearchDialog: Boolean = false,
  val showCitiesDialog: Boolean = false,
  val showSettingsDialog: Boolean = false,
  val isRefreshing: Boolean = false,
  val isDarkMode: Boolean = false
)

class WeatherViewModel @JvmOverloads constructor(
  private val repository: WeatherRepository = WeatherRepository()
) : ViewModel() {

  private val _state = MutableStateFlow(WeatherScreenState())
  val state: StateFlow<WeatherScreenState> = _state.asStateFlow()

  private var searchJob: Job? = null
  private var loadJob: Job? = null
  private var themeInitialized = false

  /** Last successfully downloaded (real) forecast per city id. Never contains simulated data. */
  private val lastKnownGood = mutableMapOf<String, CompleteWeatherData>()

  init {
    loadWeatherForCity(_state.value.selectedCity)
  }

  /**
   * Follows the system light/dark setting once, on first launch. After that the user's own
   * choice (toggle) wins, also across configuration changes.
   */
  fun applySystemTheme(isSystemDark: Boolean) {
    if (themeInitialized) return
    themeInitialized = true
    _state.update { it.copy(isDarkMode = isSystemDark) }
  }

  fun loadWeatherForCity(city: City) = load(city, showLoading = true)

  private fun load(city: City, showLoading: Boolean) {
    loadJob?.cancel() // a newer request always wins over an older, slower one
    _state.update {
      it.copy(
        selectedCity = city,
        uiState = if (showLoading) WeatherUiState.Loading else it.uiState
      )
    }
    loadJob = viewModelScope.launch {
      try {
        val weather = repository.fetchWeather(city)
        lastKnownGood[city.id] = weather
        _state.update { it.copy(uiState = WeatherUiState.Success(weather), isRefreshing = false) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _state.update {
          it.copy(
            uiState = WeatherUiState.Error(
              message = e.message ?: "Unable to load the forecast.",
              // Only a previously downloaded REAL forecast may be shown; never invented values.
              cachedData = lastKnownGood[city.id]
            ),
            isRefreshing = false
          )
        }
      }
    }
  }

  fun refreshWeather() {
    // Keep showing the current forecast while refreshing instead of flashing a spinner.
    _state.update { it.copy(isRefreshing = true) }
    load(_state.value.selectedCity, showLoading = _state.value.uiState !is WeatherUiState.Success)
  }

  fun toggleUnit() {
    _state.update { it.copy(isFahrenheit = !it.isFahrenheit) }
  }

  fun toggleTheme() {
    _state.update { it.copy(isDarkMode = !it.isDarkMode) }
  }

  fun setHourlyTab(tabIndex: Int) {
    _state.update { it.copy(selectedHourlyTab = tabIndex) }
  }

  fun openSearchDialog() {
    _state.update { it.copy(showSearchDialog = true, searchQuery = "", searchResults = emptyList(), searchError = null) }
  }

  fun closeSearchDialog() {
    searchJob?.cancel()
    _state.update {
      it.copy(showSearchDialog = false, searchQuery = "", searchResults = emptyList(), isSearching = false, searchError = null)
    }
  }

  fun openCitiesDialog() {
    _state.update { it.copy(showCitiesDialog = true) }
  }

  fun closeCitiesDialog() {
    _state.update { it.copy(showCitiesDialog = false) }
  }

  fun openSettingsDialog() {
    _state.update { it.copy(showSettingsDialog = true) }
  }

  fun closeSettingsDialog() {
    _state.update { it.copy(showSettingsDialog = false) }
  }

  fun onSearchQueryChanged(query: String) {
    _state.update { it.copy(searchQuery = query) }
    searchJob?.cancel()
    if (query.isBlank()) {
      _state.update { it.copy(searchResults = emptyList(), isSearching = false, searchError = null) }
      return
    }

    searchJob = viewModelScope.launch {
      delay(250) // Debounce
      _state.update { it.copy(isSearching = true, searchError = null) }
      try {
        val results = repository.searchCities(query)
        _state.update { it.copy(searchResults = results, isSearching = false, searchError = null) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _state.update {
          it.copy(
            searchResults = repository.searchBuiltInCities(query),
            isSearching = false,
            searchError = "Can't reach the search service. Showing built-in cities only."
          )
        }
      }
    }
  }

  fun selectCity(city: City) {
    val currentSaved = _state.value.savedCities
    val updated = if (currentSaved.none { isSameCity(it, city) }) listOf(city) + currentSaved else currentSaved
    _state.update {
      it.copy(
        savedCities = updated,
        showSearchDialog = false,
        showCitiesDialog = false
      )
    }
    loadWeatherForCity(city)
  }

  fun removeCity(city: City) {
    val before = _state.value
    val updated = before.savedCities.filterNot { it.id == city.id }
    if (updated.isEmpty()) return // always keep at least one saved city
    val removedSelected = before.selectedCity.id == city.id
    _state.update { it.copy(savedCities = updated) }
    lastKnownGood.remove(city.id)
    if (removedSelected) loadWeatherForCity(updated[0])
  }

  private fun isSameCity(a: City, b: City): Boolean =
    a.id == b.id || (
      a.name.equals(b.name, ignoreCase = true) &&
        a.region.equals(b.region, ignoreCase = true) &&
        a.country.equals(b.country, ignoreCase = true)
      )
}

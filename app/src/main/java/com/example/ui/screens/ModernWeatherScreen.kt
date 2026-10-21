package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.AtmosphereType
import com.example.data.model.City
import com.example.data.model.CompleteWeatherData
import com.example.data.model.DailyForecastItem
import com.example.data.model.HourlyForecastItem
import com.example.data.model.ModernWeatherIconType
import com.example.data.model.MoonPhase
import com.example.data.model.PressureTrend
import com.example.data.model.UnitConversions
import com.example.data.model.formatTemperature
import com.example.data.model.formatVisibility
import com.example.ui.components.FrostedCard
import com.example.ui.components.ModernWeatherIcon
import com.example.ui.components.ModernWindCompass
import com.example.ui.components.SimulatedRadarCard
import com.example.ui.components.SunArcVisualizer
import com.example.ui.components.TemperatureRangeBar
import com.example.ui.components.UvIndexArcGauge
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.InkTertiary
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.WeatherThemeColors
import com.example.ui.theme.SaltBackground
import com.example.ui.theme.SaltBorder
import com.example.ui.theme.SaltBorderStrong
import com.example.ui.theme.SaltSurface
import com.example.ui.theme.SaltSurfaceElevated
import com.example.ui.theme.SaltSurfaceSubtle
import com.example.ui.theme.SevereRed
import com.example.ui.theme.SevereRedLight
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SkyAccentLight
import com.example.ui.theme.SunGold
import com.example.ui.theme.SunGoldLight
import com.example.ui.theme.SunOrange
import com.example.ui.theme.WhiteClearDayBottom
import com.example.ui.theme.WhiteClearDayTop
import com.example.ui.theme.WhiteClearNightBottom
import com.example.ui.theme.WhiteClearNightTop
import com.example.ui.theme.WhiteCloudyBottom
import com.example.ui.theme.WhiteCloudyTop
import com.example.ui.theme.WhiteRainBottom
import com.example.ui.theme.WhiteRainTop
import com.example.ui.theme.WhiteSnowBottom
import com.example.ui.theme.WhiteSnowTop
import com.example.ui.theme.WhiteStormBottom
import com.example.ui.theme.WhiteStormTop
import com.example.ui.viewmodel.WeatherUiState
import com.example.ui.viewmodel.WeatherViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernWeatherScreen(
  viewModel: WeatherViewModel,
  modifier: Modifier = Modifier
) {
  val state by viewModel.state.collectAsState()

  // Atmospheric clean gradient on pure white canvas
  val currentAtmosphere = when (val ui = state.uiState) {
    is WeatherUiState.Success -> ui.data.condition.atmosphere
    is WeatherUiState.Error -> ui.cachedData?.condition?.atmosphere ?: AtmosphereType.CLEAR_DAY
    else -> AtmosphereType.CLEAR_DAY
  }

  val isDark = state.isDarkMode
  val backgroundBrush = remember(currentAtmosphere, isDark) {
    if (isDark) {
      when (currentAtmosphere) {
        AtmosphereType.CLEAR_DAY -> Brush.verticalGradient(listOf(Color(0xFF0F2642), Color(0xFF090D14)))
        AtmosphereType.CLEAR_NIGHT -> Brush.verticalGradient(listOf(Color(0xFF0A1326), Color(0xFF050810)))
        AtmosphereType.CLOUDY -> Brush.verticalGradient(listOf(Color(0xFF16202E), Color(0xFF090E16)))
        AtmosphereType.RAIN -> Brush.verticalGradient(listOf(Color(0xFF0F1B2B), Color(0xFF060B12)))
        AtmosphereType.STORM -> Brush.verticalGradient(listOf(Color(0xFF191329), Color(0xFF080611)))
        AtmosphereType.SNOW -> Brush.verticalGradient(listOf(Color(0xFF122230), Color(0xFF070E16)))
      }
    } else {
      when (currentAtmosphere) {
        AtmosphereType.CLEAR_DAY -> Brush.verticalGradient(listOf(WhiteClearDayTop, WhiteClearDayBottom))
        AtmosphereType.CLEAR_NIGHT -> Brush.verticalGradient(listOf(WhiteClearNightTop, WhiteClearNightBottom))
        AtmosphereType.CLOUDY -> Brush.verticalGradient(listOf(WhiteCloudyTop, WhiteCloudyBottom))
        AtmosphereType.RAIN -> Brush.verticalGradient(listOf(WhiteRainTop, WhiteRainBottom))
        AtmosphereType.STORM -> Brush.verticalGradient(listOf(WhiteStormTop, WhiteStormBottom))
        AtmosphereType.SNOW -> Brush.verticalGradient(listOf(WhiteSnowTop, WhiteSnowBottom))
      }
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      WindChannelTopBar(
        selectedCity = state.selectedCity,
        isFahrenheit = state.isFahrenheit,
        isDarkMode = state.isDarkMode,
        onCityClick = { viewModel.openCitiesDialog() },
        onSearchClick = { viewModel.openSearchDialog() },
        onToggleUnit = { viewModel.toggleUnit() },
        onToggleTheme = { viewModel.toggleTheme() },
        onRefresh = { viewModel.refreshWeather() },
        onSettingsClick = { viewModel.openSettingsDialog() }
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(backgroundBrush)
        .padding(innerPadding)
    ) {
      when (val ui = state.uiState) {
        is WeatherUiState.Loading -> {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              CircularProgressIndicator(color = SkyAccent, strokeWidth = 3.dp)
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "Connecting to Wind Channel radar...",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontFamily = InterFontFamily,
                  color = WeatherThemeColors.textSecondary
                )
              )
            }
          }
        }

        is WeatherUiState.Success -> {
          WindChannelContent(
            data = ui.data,
            isFahrenheit = state.isFahrenheit,
            selectedHourlyTab = state.selectedHourlyTab,
            onTabSelected = { viewModel.setHourlyTab(it) }
          )
        }

        is WeatherUiState.Error -> {
          if (ui.cachedData != null) {
            WindChannelContent(
              data = ui.cachedData,
              isFahrenheit = state.isFahrenheit,
              selectedHourlyTab = state.selectedHourlyTab,
              onTabSelected = { viewModel.setHourlyTab(it) },
              bannerNotice = "Live data unavailable. Showing the forecast last downloaded on " +
                "${formatFetchedAt(ui.cachedData.fetchedAtMillis)}. ${ui.message}"
            )
          } else {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
              ) {
                Text(
                  text = "Unable to load forecast",
                  style = MaterialTheme.typography.titleLarge.copy(color = WeatherThemeColors.textPrimary)
                )
                Text(
                  text = ui.message,
                  style = MaterialTheme.typography.bodyMedium.copy(color = WeatherThemeColors.textSecondary),
                  modifier = Modifier.padding(vertical = 12.dp),
                  textAlign = TextAlign.Center
                )
                TextButton(
                  onClick = { viewModel.refreshWeather() },
                  modifier = Modifier.testTag("retry_button")
                ) {
                  Text("Try Again", color = SkyAccent)
                }
              }
            }
          }
        }
      }

      // Dialogs
      if (state.showSearchDialog) {
        CitySearchDialog(
          query = state.searchQuery,
          results = state.searchResults,
          isSearching = state.isSearching,
          errorMessage = state.searchError,
          onQueryChange = { viewModel.onSearchQueryChanged(it) },
          onCitySelected = { viewModel.selectCity(it) },
          onDismiss = { viewModel.closeSearchDialog() }
        )
      }

      if (state.showCitiesDialog) {
        SavedCitiesDialog(
          cities = state.savedCities,
          selectedCity = state.selectedCity,
          onSelectCity = { viewModel.selectCity(it) },
          onAddNew = {
            viewModel.closeCitiesDialog()
            viewModel.openSearchDialog()
          },
          onDeleteCity = { viewModel.removeCity(it) },
          onDismiss = { viewModel.closeCitiesDialog() }
        )
      }

      if (state.showSettingsDialog) {
        SettingsDialog(
          isFahrenheit = state.isFahrenheit,
          isDarkMode = state.isDarkMode,
          onToggleUnit = { viewModel.toggleUnit() },
          onToggleTheme = { viewModel.toggleTheme() },
          onDismiss = { viewModel.closeSettingsDialog() }
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WindChannelTopBar(
  selectedCity: City,
  isFahrenheit: Boolean,
  isDarkMode: Boolean,
  onCityClick: () -> Unit,
  onSearchClick: () -> Unit,
  onToggleUnit: () -> Unit,
  onToggleTheme: () -> Unit,
  onRefresh: () -> Unit,
  onSettingsClick: () -> Unit
) {
  TopAppBar(
    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = if (isDarkMode) Color(0xFF090D14) else PureWhite
    ),
    title = {
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .clickable { onCityClick() }
          .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.LocationOn,
          contentDescription = null,
          tint = SkyAccent,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = selectedCity.name,
              style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                color = WeatherThemeColors.textPrimary
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Icon(
              imageVector = Icons.Default.ExpandMore,
              contentDescription = "Switch City",
              tint = WeatherThemeColors.textSecondary,
              modifier = Modifier.size(16.dp).padding(start = 2.dp)
            )
          }
          if (selectedCity.region.isNotBlank() || selectedCity.country.isNotBlank()) {
            Text(
              text = if (selectedCity.region.isNotBlank()) "${selectedCity.region}, ${selectedCity.country}" else selectedCity.country,
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = InterFontFamily,
                color = WeatherThemeColors.textTertiary,
                fontSize = 11.sp
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    },
    actions = {
      // Temperature Unit Pill
      Box(
        modifier = Modifier
          .padding(end = 2.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(WeatherThemeColors.surfaceSubtle)
          .border(1.dp, WeatherThemeColors.border, RoundedCornerShape(16.dp))
          .clickable { onToggleUnit() }
          .padding(horizontal = 9.dp, vertical = 5.dp)
          .testTag("unit_toggle_pill")
      ) {
        Text(
          text = if (isFahrenheit) "°F" else "°C",
          style = MaterialTheme.typography.labelMedium.copy(
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            color = SkyAccent
          )
        )
      }

      // Theme Change Icon Button
      IconButton(
        onClick = onToggleTheme,
        modifier = Modifier.testTag("theme_toggle_button")
      ) {
        Icon(
          imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
          contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
          tint = if (isDarkMode) SunGold else WeatherThemeColors.textSecondary
        )
      }

      IconButton(
        onClick = onSearchClick,
        modifier = Modifier.testTag("search_button")
      ) {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Search",
          tint = WeatherThemeColors.textPrimary
        )
      }

      IconButton(
        onClick = onRefresh,
        modifier = Modifier.testTag("refresh_button")
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = "Refresh",
          tint = WeatherThemeColors.textSecondary
        )
      }

      IconButton(
        onClick = onSettingsClick,
        modifier = Modifier.testTag("settings_button")
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Settings",
          tint = WeatherThemeColors.textSecondary
        )
      }
    }
  )
}

@Composable
private fun WindChannelContent(
  data: CompleteWeatherData,
  isFahrenheit: Boolean,
  selectedHourlyTab: Int,
  onTabSelected: (Int) -> Unit,
  bannerNotice: String? = null
) {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 640.dp)
        .windowInsetsPadding(WindowInsets.navigationBars),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

      if (bannerNotice != null) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(SunGoldLight)
              .border(1.dp, SunGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Text(
              text = bannerNotice,
              style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = InterFontFamily,
                color = SunGold
              )
            )
          }
        }
      }

      // 2. Severe Weather Alert Banner (if any)
      if (data.advisory != null) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(SevereRedLight)
              .border(1.dp, SevereRed.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
              .padding(14.dp)
          ) {
            Row(verticalAlignment = Alignment.Top) {
              Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = SevereRed,
                modifier = Modifier.size(20.dp).padding(top = 1.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = data.advisory.title,
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = SevereRed
                  )
                )
                Text(
                  text = data.advisory.description,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = InterFontFamily,
                    color = WeatherThemeColors.textPrimary
                  ),
                  modifier = Modifier.padding(top = 2.dp)
                )
              }
            }
          }
        }
      }

      // 3. Hero Weather Focus (Clean Large Numerals Adapting to Theme)
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = formatTempOnly(data.temperatureC, isFahrenheit),
            style = MaterialTheme.typography.displayLarge.copy(
              fontFamily = OutfitFontFamily,
              fontWeight = FontWeight.SemiBold,
              color = WeatherThemeColors.textPrimary,
              fontSize = 82.sp
            )
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            ModernWeatherIcon(
              type = data.condition.iconType,
              size = 24.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = data.condition.title,
              style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Medium,
                color = WeatherThemeColors.textPrimary
              )
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          val maxStr = formatTemp(data.todayMaxC, isFahrenheit)
          val minStr = formatTemp(data.todayMinC, isFahrenheit)
          val feelsStr = formatTemp(data.apparentTempC, isFahrenheit)

          Text(
            text = "H: $maxStr   L: $minStr   ·   Feels like $feelsStr",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = InterFontFamily,
              color = WeatherThemeColors.textSecondary
            )
          )

          Box(
            modifier = Modifier
              .padding(top = 10.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(WeatherThemeColors.surfaceSubtle)
              .border(1.dp, WeatherThemeColors.border, RoundedCornerShape(16.dp))
              .padding(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Text(
              text = data.condition.summary,
              style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = InterFontFamily,
                color = WeatherThemeColors.textPrimary
              )
            )
          }
        }
      }

      // 4. Interactive Hourly Forecast Ribbon
      item {
        FrostedCard(
          title = "Hourly Forecast",
          icon = {
            Icon(Icons.Default.Thermostat, contentDescription = null, tint = SkyAccent, modifier = Modifier.size(16.dp))
          },
          headerAction = {
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(WeatherThemeColors.surfaceSubtle)
                .border(1.dp, WeatherThemeColors.border, RoundedCornerShape(12.dp))
                .padding(2.dp)
            ) {
              val tabs = listOf("Temp", "Rain", "Wind")
              tabs.forEachIndexed { idx, label ->
                val isSelected = selectedHourlyTab == idx
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) SkyAccent else Color.Transparent)
                    .clickable { onTabSelected(idx) }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                  Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontFamily = InterFontFamily,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      color = if (isSelected) PureWhite else WeatherThemeColors.textSecondary,
                      fontSize = 11.sp
                    )
                  )
                }
              }
            }
          }
        ) {
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
          ) {
            items(data.hourly) { hour ->
              HourlyPillItem(
                item = hour,
                tab = selectedHourlyTab,
                isFahrenheit = isFahrenheit
              )
            }
          }
        }
      }

      // 5. 10-Day Extended Forecast Card
      item {
        val allMin = data.daily.minOfOrNull { it.minTempC } ?: 0.0
        val allMax = data.daily.maxOfOrNull { it.maxTempC } ?: 30.0

        FrostedCard(
          title = "10-Day Outlook",
          icon = {
            Icon(Icons.Default.WbSunny, contentDescription = null, tint = SunGold, modifier = Modifier.size(16.dp))
          }
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            data.daily.forEachIndexed { index, day ->
              val isToday = (index == 0)
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = day.dayLabel,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = InterFontFamily,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    color = if (isToday) WeatherThemeColors.textPrimary else WeatherThemeColors.textSecondary
                  ),
                  modifier = Modifier.width(60.dp)
                )

                Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                  ModernWeatherIcon(type = day.condition.iconType, size = 22.dp)
                }

                Box(modifier = Modifier.width(42.dp), contentAlignment = Alignment.CenterStart) {
                  val dayPrecip = day.precipitationProb
                  if (dayPrecip != null && dayPrecip >= 20) {
                    Text(
                      text = "$dayPrecip%",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = SkyAccent,
                        fontSize = 10.sp
                      )
                    )
                  }
                }

                Text(
                  text = formatTempOnly(day.minTempC, isFahrenheit),
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = OutfitFontFamily,
                    color = WeatherThemeColors.textTertiary,
                    textAlign = TextAlign.End
                  ),
                  modifier = Modifier.width(36.dp)
                )

                TemperatureRangeBar(
                  minTempC = day.minTempC,
                  maxTempC = day.maxTempC,
                  weekMinC = allMin,
                  weekMaxC = allMax,
                  isToday = isToday,
                  currentTempC = data.temperatureC,
                  isFahrenheit = isFahrenheit,
                  modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                )

                Text(
                  text = formatTempOnly(day.maxTempC, isFahrenheit),
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = WeatherThemeColors.textPrimary,
                    textAlign = TextAlign.Start
                  ),
                  modifier = Modifier.width(36.dp)
                )
              }
            }
          }
        }
      }

      // 6. Precipitation Radar Loop
      item {
        SimulatedRadarCard()
      }

      // 7. UV Index & Wind Cards
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          FrostedCard(
            title = "UV Index",
            icon = { Icon(Icons.Default.WbSunny, contentDescription = null, tint = SunOrange, modifier = Modifier.size(15.dp)) },
            modifier = Modifier.weight(1f)
          ) {
            val uv = data.uv
            if (uv != null) {
              UvIndexArcGauge(uv = uv)
            } else {
              Text(
                text = "UV index not available",
                style = MaterialTheme.typography.bodySmall.copy(color = WeatherThemeColors.textSecondary)
              )
            }
          }

          FrostedCard(
            title = "Wind",
            icon = { Icon(Icons.Default.Air, contentDescription = null, tint = SkyAccent, modifier = Modifier.size(15.dp)) },
            modifier = Modifier.weight(1f)
          ) {
            ModernWindCompass(wind = data.wind, isFahrenheit = isFahrenheit)
          }
        }
      }

      // 8. Daylight & Solar Arc
      item {
        FrostedCard(
          title = "Sun Position & Daylight",
          icon = { Icon(Icons.Default.LightMode, contentDescription = null, tint = SunGold, modifier = Modifier.size(15.dp)) }
        ) {
          val sunrise = data.sunrise
          val sunset = data.sunset
          if (sunrise != null && sunset != null) {
            SunArcVisualizer(
              sunrise = sunrise,
              sunset = sunset,
              firstLight = data.firstLight,
              dusk = data.dusk,
              currentMinutes = data.localTimeMinutes
            )
          } else {
            Text(
              text = "Sunrise and sunset are not available for this date (polar day or night).",
              style = MaterialTheme.typography.bodySmall.copy(color = WeatherThemeColors.textSecondary)
            )
          }
        }
      }

      // 9. Humidity & Pressure Cards
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          FrostedCard(
            title = "Humidity",
            icon = { Icon(Icons.Default.WaterDrop, contentDescription = null, tint = SkyAccent, modifier = Modifier.size(15.dp)) },
            modifier = Modifier.weight(1f)
          ) {
            Column {
              Text(
                text = "${data.relativeHumidity}%",
                style = MaterialTheme.typography.headlineLarge.copy(
                  fontFamily = OutfitFontFamily,
                  fontWeight = FontWeight.Bold,
                  color = WeatherThemeColors.textPrimary
                )
              )
              val dewStr = formatTemp(data.dewPointC, isFahrenheit)
              Text(
                text = "Dew point: $dewStr",
                style = MaterialTheme.typography.bodySmall.copy(color = WeatherThemeColors.textSecondary),
                modifier = Modifier.padding(top = 4.dp)
              )
              val comfort = if (data.relativeHumidity in 35..65) "Comfortable" else if (data.relativeHumidity > 65) "Humid & muggy" else "Dry"
              Text(
                text = comfort,
                style = MaterialTheme.typography.labelSmall.copy(color = SkyAccent, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 6.dp)
              )
            }
          }

          FrostedCard(
            title = "Pressure",
            icon = { Icon(Icons.Default.Compress, contentDescription = null, tint = WeatherThemeColors.textSecondary, modifier = Modifier.size(15.dp)) },
            modifier = Modifier.weight(1f)
          ) {
            Column {
              Text(
                text = "${data.pressureHpa.toInt()} hPa",
                style = MaterialTheme.typography.headlineLarge.copy(
                  fontFamily = OutfitFontFamily,
                  fontWeight = FontWeight.Bold,
                  color = WeatherThemeColors.textPrimary
                )
              )
              val inHg = String.format(java.util.Locale.US, "%.2f inHg", UnitConversions.hPaToInHg(data.pressureHpa))
              Text(
                text = inHg,
                style = MaterialTheme.typography.bodySmall.copy(color = WeatherThemeColors.textSecondary),
                modifier = Modifier.padding(top = 2.dp)
              )
              Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                val icon = when (data.pressureTrend) {
                  PressureTrend.RISING -> Icons.AutoMirrored.Filled.TrendingUp
                  PressureTrend.FALLING -> Icons.AutoMirrored.Filled.TrendingDown
                  PressureTrend.STEADY, PressureTrend.UNKNOWN -> Icons.AutoMirrored.Filled.TrendingFlat
                }
                val trendLabel = when (data.pressureTrend) {
                  PressureTrend.RISING -> "Rising (last 3 h)"
                  PressureTrend.FALLING -> "Falling (last 3 h)"
                  PressureTrend.STEADY -> "Steady (last 3 h)"
                  PressureTrend.UNKNOWN -> "Trend unavailable"
                }
                Icon(icon, contentDescription = null, tint = SkyAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = trendLabel,
                  style = MaterialTheme.typography.labelSmall.copy(color = SkyAccent, fontWeight = FontWeight.Bold)
                )
              }
            }
          }
        }
      }

      // 10. Visibility & Moon Phase Cards
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          FrostedCard(
            title = "Visibility",
            icon = { Icon(Icons.Default.Visibility, contentDescription = null, tint = WeatherThemeColors.textSecondary, modifier = Modifier.size(15.dp)) },
            modifier = Modifier.weight(1f)
          ) {
            Column {
              val visKm = data.visibilityKm
              val distStr = if (visKm != null) formatVisibility(visKm, imperial = isFahrenheit) else "—"
              Text(
                text = distStr,
                style = MaterialTheme.typography.headlineLarge.copy(
                  fontFamily = OutfitFontFamily,
                  fontWeight = FontWeight.Bold,
                  color = WeatherThemeColors.textPrimary
                )
              )
              val clarity = when {
                visKm == null -> "Not reported"
                visKm >= 10.0 -> "Perfect clear view"
                visKm >= 5.0 -> "Moderate haze"
                else -> "Low visibility"
              }
              Text(
                text = clarity,
                style = MaterialTheme.typography.bodySmall.copy(color = WeatherThemeColors.textSecondary),
                modifier = Modifier.padding(top = 4.dp)
              )
              Text(
                text = "Cloud Cover: ${data.cloudCoverPercent?.let { "$it%" } ?: "—"}",
                style = MaterialTheme.typography.labelSmall.copy(color = WeatherThemeColors.textTertiary),
                modifier = Modifier.padding(top = 4.dp)
              )
            }
          }

          FrostedCard(
            title = "Moon Phase",
            icon = { Icon(Icons.Default.DarkMode, contentDescription = null, tint = WeatherThemeColors.textSecondary, modifier = Modifier.size(15.dp)) },
            modifier = Modifier.weight(1f)
          ) {
            Column {
              Text(
                text = data.moonPhase.name,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontFamily = OutfitFontFamily,
                  fontWeight = FontWeight.Bold,
                  color = WeatherThemeColors.textPrimary
                )
              )
              Text(
                text = "Illumination: ${data.moonPhase.illuminationPercent}%",
                style = MaterialTheme.typography.bodySmall.copy(color = WeatherThemeColors.textSecondary),
                modifier = Modifier.padding(top = 2.dp)
              )
              Text(
                text = "Calculated from the lunar cycle",
                style = MaterialTheme.typography.labelSmall.copy(color = WeatherThemeColors.textTertiary),
                modifier = Modifier.padding(top = 6.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun HourlyPillItem(
  item: HourlyForecastItem,
  tab: Int,
  isFahrenheit: Boolean
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.width(48.dp)
  ) {
    Text(
      text = item.timeLabel,
      style = MaterialTheme.typography.labelSmall.copy(
        fontFamily = InterFontFamily,
        fontWeight = if (item.isNow) FontWeight.Bold else FontWeight.Normal,
        color = if (item.isNow) WeatherThemeColors.textPrimary else WeatherThemeColors.textSecondary,
        fontSize = 11.sp
      )
    )

    Spacer(modifier = Modifier.height(8.dp))

    when (tab) {
      0 -> {
        ModernWeatherIcon(type = item.condition.iconType, size = 24.dp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = formatTempOnly(item.tempC, isFahrenheit),
          style = MaterialTheme.typography.titleSmall.copy(
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            color = WeatherThemeColors.textPrimary
          )
        )
      }
      1 -> {
        Icon(
          imageVector = Icons.Default.WaterDrop,
          contentDescription = null,
          tint = if ((item.precipitationProb ?: 0) > 0) SkyAccent else WeatherThemeColors.textMuted,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = item.precipitationProb?.let { "$it%" } ?: "—",
          style = MaterialTheme.typography.titleSmall.copy(
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            color = if ((item.precipitationProb ?: 0) > 0) SkyAccent else WeatherThemeColors.textTertiary
          )
        )
      }
      2 -> {
        Icon(
          imageVector = Icons.Default.Air,
          contentDescription = null,
          tint = SkyAccent,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        val windKmh = item.windSpeedKmh
        val spd = when {
          windKmh == null -> "—"
          isFahrenheit -> UnitConversions.kmhToMph(windKmh).roundToInt().toString()
          else -> windKmh.roundToInt().toString()
        }
        Text(
          text = spd,
          style = MaterialTheme.typography.titleSmall.copy(
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            color = WeatherThemeColors.textPrimary
          )
        )
      }
    }
  }
}

@Composable
private fun SavedCitiesDialog(
  cities: List<City>,
  selectedCity: City,
  onSelectCity: (City) -> Unit,
  onAddNew: () -> Unit,
  onDeleteCity: (City) -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = WeatherThemeColors.surface,
      border = androidx.compose.foundation.BorderStroke(1.dp, WeatherThemeColors.border),
      shadowElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 440.dp)
        .padding(8.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Saved Locations",
            style = MaterialTheme.typography.titleLarge.copy(
              fontFamily = OutfitFontFamily,
              fontWeight = FontWeight.Bold,
              color = WeatherThemeColors.textPrimary
            )
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = WeatherThemeColors.textSecondary)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(cities) { city ->
            val isCurrent = city.id == selectedCity.id
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isCurrent) (if (WeatherThemeColors.isDark) Color(0xFF0F2642) else SkyAccentLight) else WeatherThemeColors.surfaceSubtle)
                .border(
                  1.dp,
                  if (isCurrent) SkyAccent else WeatherThemeColors.border,
                  RoundedCornerShape(12.dp)
                )
                .clickable { onSelectCity(city) }
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = city.name,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) SkyAccent else WeatherThemeColors.textPrimary
                  )
                )
                Text(
                  text = if (city.region.isNotBlank()) "${city.region}, ${city.country}" else city.country,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = InterFontFamily,
                    color = WeatherThemeColors.textSecondary
                  )
                )
              }

              if (cities.size > 1) {
                IconButton(
                  onClick = { onDeleteCity(city) },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = WeatherThemeColors.textMuted,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (WeatherThemeColors.isDark) SkyAccent else InkPrimary)
            .clickable { onAddNew() }
            .padding(vertical = 12.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Add City or Region",
              style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                color = PureWhite
              )
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CitySearchDialog(
  query: String,
  results: List<City>,
  isSearching: Boolean,
  errorMessage: String?,
  onQueryChange: (String) -> Unit,
  onCitySelected: (City) -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = WeatherThemeColors.surface,
      border = androidx.compose.foundation.BorderStroke(1.dp, WeatherThemeColors.border),
      shadowElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 440.dp)
        .padding(8.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Search Location",
            style = MaterialTheme.typography.titleLarge.copy(
              fontFamily = OutfitFontFamily,
              fontWeight = FontWeight.Bold,
              color = WeatherThemeColors.textPrimary
            )
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = WeatherThemeColors.textSecondary)
          }
        }

        OutlinedTextField(
          value = query,
          onValueChange = onQueryChange,
          placeholder = { Text("Search city (e.g. Seattle, Kyoto)...", color = WeatherThemeColors.textMuted) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SkyAccent) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SkyAccent,
            unfocusedBorderColor = WeatherThemeColors.border,
            focusedTextColor = WeatherThemeColors.textPrimary,
            unfocusedTextColor = WeatherThemeColors.textPrimary,
            cursorColor = SkyAccent
          ),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .testTag("search_city_input")
        )

        if (errorMessage != null) {
          Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall.copy(color = SevereRed),
            modifier = Modifier
              .padding(bottom = 8.dp)
              .testTag("search_error")
          )
        }

        if (isSearching) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(120.dp),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(color = SkyAccent, strokeWidth = 2.dp)
          }
        } else if (results.isNotEmpty()) {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(240.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            items(results) { city ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(WeatherThemeColors.surfaceSubtle)
                  .clickable { onCitySelected(city) }
                  .padding(12.dp)
              ) {
                Column {
                  Text(
                    text = city.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontFamily = OutfitFontFamily,
                      fontWeight = FontWeight.Bold,
                      color = WeatherThemeColors.textPrimary
                    )
                  )
                  Text(
                    text = if (city.region.isNotBlank()) "${city.region}, ${city.country}" else city.country,
                    style = MaterialTheme.typography.bodySmall.copy(color = WeatherThemeColors.textSecondary)
                  )
                }
              }
            }
          }
        } else if (query.isNotBlank()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(120.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("No locations found", style = MaterialTheme.typography.bodyMedium.copy(color = WeatherThemeColors.textSecondary))
          }
        } else {
          Column(modifier = Modifier.padding(top = 8.dp)) {
            Text(
              text = "Popular Metropolises",
              style = MaterialTheme.typography.labelSmall.copy(color = WeatherThemeColors.textTertiary)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              listOf("Tokyo", "London", "New York").forEach { name ->
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(WeatherThemeColors.surfaceSubtle)
                    .clickable { onQueryChange(name) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Text(text = name, style = MaterialTheme.typography.bodySmall.copy(color = SkyAccent))
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SettingsDialog(
  isFahrenheit: Boolean,
  isDarkMode: Boolean = false,
  onToggleUnit: () -> Unit,
  onToggleTheme: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = WeatherThemeColors.surface,
      border = androidx.compose.foundation.BorderStroke(1.dp, WeatherThemeColors.border),
      shadowElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 420.dp)
        .padding(8.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge.copy(
              fontFamily = OutfitFontFamily,
              fontWeight = FontWeight.Bold,
              color = WeatherThemeColors.textPrimary
            )
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = WeatherThemeColors.textSecondary)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Temperature Unit Switch
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WeatherThemeColors.surfaceSubtle)
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = "Temperature Unit",
              style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                color = WeatherThemeColors.textPrimary
              )
            )
            Text(
              text = if (isFahrenheit) "Fahrenheit (°F)" else "Celsius (°C)",
              style = MaterialTheme.typography.bodySmall.copy(color = WeatherThemeColors.textSecondary)
            )
          }

          Switch(
            checked = isFahrenheit,
            onCheckedChange = { onToggleUnit() },
            colors = SwitchDefaults.colors(
              checkedThumbColor = PureWhite,
              checkedTrackColor = SkyAccent,
              uncheckedThumbColor = WeatherThemeColors.textMuted,
              uncheckedTrackColor = WeatherThemeColors.border
            )
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dark Mode Theme Switch
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WeatherThemeColors.surfaceSubtle)
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = "Dark Mode Theme",
              style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                color = WeatherThemeColors.textPrimary
              )
            )
            Text(
              text = if (isDarkMode) "Dark obsidian atmosphere" else "Clean white canvas",
              style = MaterialTheme.typography.bodySmall.copy(color = WeatherThemeColors.textSecondary)
            )
          }

          Switch(
            checked = isDarkMode,
            onCheckedChange = { onToggleTheme() },
            colors = SwitchDefaults.colors(
              checkedThumbColor = PureWhite,
              checkedTrackColor = SkyAccent,
              uncheckedThumbColor = WeatherThemeColors.textMuted,
              uncheckedTrackColor = WeatherThemeColors.border
            )
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WeatherThemeColors.surfaceSubtle)
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Info, contentDescription = null, tint = SkyAccent, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Wind Channel v2.5",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = WeatherThemeColors.textPrimary)
            )
            Text(
              text = "Precision open meteorological data via Open-Meteo",
              style = MaterialTheme.typography.bodySmall.copy(color = WeatherThemeColors.textSecondary)
            )
          }
        }
      }
    }
  }
}

private fun formatTemp(celsius: Double, isFahrenheit: Boolean): String =
  formatTemperature(celsius, isFahrenheit)

private fun formatFetchedAt(millis: Long): String =
  java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.getDefault()).format(java.util.Date(millis))

private fun formatTempOnly(celsius: Double, isFahrenheit: Boolean): String {
  return formatTemp(celsius, isFahrenheit)
}

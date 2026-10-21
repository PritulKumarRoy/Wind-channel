package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ModernWeatherScreen
import com.example.ui.theme.WindChannelTheme
import com.example.ui.viewmodel.WeatherViewModel

open class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    try {
      enableEdgeToEdge()
    } catch (t: Throwable) {
      Log.w("MainActivity", "Edge to edge not available on this platform", t)
    }

    setContent {
      val viewModel: WeatherViewModel = viewModel()
      val state by viewModel.state.collectAsState()
      val systemDark = isSystemInDarkTheme()
      LaunchedEffect(systemDark) { viewModel.applySystemTheme(systemDark) }

      WindChannelTheme(darkTheme = state.isDarkMode) {
        ModernWeatherScreen(viewModel = viewModel)
      }
    }
  }
}

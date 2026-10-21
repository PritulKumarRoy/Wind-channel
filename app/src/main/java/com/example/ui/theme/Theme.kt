package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WindChannelLightColorScheme = lightColorScheme(
  primary = InkPrimary,
  onPrimary = PureWhite,
  primaryContainer = SaltSurfaceSubtle,
  onPrimaryContainer = InkPrimary,

  secondary = SkyAccent,
  onSecondary = PureWhite,
  secondaryContainer = SkyAccentLight,
  onSecondaryContainer = SkyAccent,

  tertiary = SunGold,
  onTertiary = PureWhite,
  tertiaryContainer = SunGoldLight,
  onTertiaryContainer = SunGold,

  background = PureWhite,
  onBackground = InkPrimary,

  surface = SaltSurface,
  onSurface = InkPrimary,
  surfaceVariant = SaltSurfaceElevated,
  onSurfaceVariant = InkSecondary,

  outline = SaltBorder,
  outlineVariant = SaltBorderStrong
)

private val WindChannelDarkColorScheme = darkColorScheme(
  primary = PureWhite,
  onPrimary = InkPrimary,
  primaryContainer = Color(0xFF1E293B),
  onPrimaryContainer = PureWhite,

  secondary = SkyAccent,
  onSecondary = PureWhite,
  secondaryContainer = Color(0xFF0F172A),
  onSecondaryContainer = SkyAccentLight,

  tertiary = SunGold,
  onTertiary = PureWhite,
  tertiaryContainer = Color(0xFF1E293B),
  onTertiaryContainer = SunGoldLight,

  background = Color(0xFF090D14),
  onBackground = PureWhite,

  surface = Color(0xFF111827),
  onSurface = PureWhite,
  surfaceVariant = Color(0xFF1E293B),
  onSurfaceVariant = Color(0xFF94A3B8),

  outline = Color(0xFF334155),
  outlineVariant = Color(0xFF475569)
)

@Composable
fun WindChannelTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = if (darkTheme) WindChannelDarkColorScheme else WindChannelLightColorScheme,
    typography = ModernTypography,
    content = content
  )
}

object WeatherThemeColors {
  val textPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface

  val textSecondary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

  val textTertiary: Color
    @Composable get() = if (MaterialTheme.colorScheme.background == PureWhite) InkTertiary else Color(0xFF94A3B8)

  val textMuted: Color
    @Composable get() = if (MaterialTheme.colorScheme.background == PureWhite) InkMuted else Color(0xFF64748B)

  val surface: Color
    @Composable get() = MaterialTheme.colorScheme.surface

  val surfaceElevated: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant

  val surfaceSubtle: Color
    @Composable get() = MaterialTheme.colorScheme.primaryContainer

  val border: Color
    @Composable get() = MaterialTheme.colorScheme.outline

  val borderStrong: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant

  val isDark: Boolean
    @Composable get() = MaterialTheme.colorScheme.background != PureWhite
}


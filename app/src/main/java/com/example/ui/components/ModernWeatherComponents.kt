package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ModernUvIndex
import com.example.data.model.ModernWeatherIconType
import com.example.data.model.ModernWind
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.InkTertiary
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.WeatherThemeColors
import com.example.ui.theme.RainBlue
import com.example.ui.theme.SaltBorder
import com.example.ui.theme.SaltSurface
import com.example.ui.theme.SaltSurfaceElevated
import com.example.ui.theme.SevereRed
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.StormViolet
import com.example.ui.theme.SunGold
import com.example.ui.theme.SunOrange
import com.example.data.model.formatWindSpeed
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FrostedCard(
  modifier: Modifier = Modifier,
  title: String? = null,
  icon: (@Composable () -> Unit)? = null,
  headerAction: (@Composable () -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val surfaceColor = MaterialTheme.colorScheme.surface
  val borderColor = MaterialTheme.colorScheme.outline
  val titleColor = MaterialTheme.colorScheme.onSurfaceVariant
  val isLight = MaterialTheme.colorScheme.background == PureWhite

  Box(
    modifier = modifier
      .shadow(elevation = if (isLight) 2.dp else 0.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0x12000000))
      .clip(RoundedCornerShape(20.dp))
      .background(surfaceColor)
      .border(1.dp, borderColor, RoundedCornerShape(20.dp))
      .padding(16.dp)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      if (title != null || icon != null) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
              Box(modifier = Modifier.padding(end = 8.dp)) {
                icon()
              }
            }
            if (title != null) {
              Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = InterFontFamily,
                  fontWeight = FontWeight.Bold,
                  color = titleColor,
                  letterSpacing = 1.2.sp
                )
              )
            }
          }
          headerAction?.invoke()
        }
      }
      Box(modifier = Modifier.fillMaxWidth()) {
        content()
      }
    }
  }
}

@Composable
fun ModernWeatherIcon(
  type: ModernWeatherIconType,
  modifier: Modifier = Modifier,
  size: Dp = 28.dp
) {
  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(size)) {
      val w = this.size.width
      val h = this.size.height
      val center = Offset(w / 2f, h / 2f)

      when (type) {
        ModernWeatherIconType.CLEAR_DAY -> {
          // Vibrant Sun
          drawCircle(
            brush = Brush.radialGradient(
              listOf(Color(0xFFFDE047), SunGold, SunOrange),
              center = center,
              radius = w * 0.35f
            ),
            radius = w * 0.32f,
            center = center
          )
          val rayLen = w * 0.12f
          val rStart = w * 0.38f
          for (i in 0 until 8) {
            val ang = (i * 45f) * PI / 180f
            val p1 = Offset(center.x + (rStart * cos(ang)).toFloat(), center.y + (rStart * sin(ang)).toFloat())
            val p2 = Offset(center.x + ((rStart + rayLen) * cos(ang)).toFloat(), center.y + ((rStart + rayLen) * sin(ang)).toFloat())
            drawLine(
              color = SunGold,
              start = p1,
              end = p2,
              strokeWidth = 2.dp.toPx(),
              cap = StrokeCap.Round
            )
          }
        }

        ModernWeatherIconType.CLEAR_NIGHT -> {
          val r = w * 0.38f
          val moonPath = Path().apply {
            moveTo(center.x, center.y - r)
            cubicTo(
              center.x + r * 1.15f, center.y - r * 0.5f,
              center.x + r * 1.15f, center.y + r * 0.5f,
              center.x, center.y + r
            )
            cubicTo(
              center.x + r * 0.35f, center.y + r * 0.35f,
              center.x + r * 0.35f, center.y - r * 0.35f,
              center.x, center.y - r
            )
            close()
          }
          drawPath(
            moonPath,
            brush = Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF64748B)))
          )
          drawCircle(SkyAccent, radius = 1.5.dp.toPx(), center = Offset(center.x - w * 0.22f, center.y - h * 0.15f))
        }

        ModernWeatherIconType.PARTLY_CLOUDY_DAY -> {
          drawCircle(
            brush = Brush.radialGradient(listOf(SunGold, SunOrange)),
            radius = w * 0.22f,
            center = Offset(center.x + w * 0.18f, center.y - h * 0.15f)
          )
          drawModernCloud(center = Offset(center.x - w * 0.05f, center.y + h * 0.08f), size = w * 0.72f)
        }

        ModernWeatherIconType.PARTLY_CLOUDY_NIGHT -> {
          drawCircle(
            color = Color(0xFF64748B),
            radius = w * 0.2f,
            center = Offset(center.x + w * 0.18f, center.y - h * 0.15f)
          )
          drawModernCloud(center = Offset(center.x - w * 0.05f, center.y + h * 0.08f), size = w * 0.72f)
        }

        ModernWeatherIconType.CLOUDY -> {
          drawModernCloud(center = center, size = w * 0.85f)
        }

        ModernWeatherIconType.RAIN_LIGHT, ModernWeatherIconType.RAIN, ModernWeatherIconType.RAIN_HEAVY -> {
          val cloudCenter = Offset(center.x, center.y - h * 0.12f)
          drawModernCloud(center = cloudCenter, size = w * 0.75f)

          val dropCount = if (type == ModernWeatherIconType.RAIN_LIGHT) 2 else if (type == ModernWeatherIconType.RAIN) 3 else 4
          val startX = center.x - (w * 0.2f)
          val step = (w * 0.4f) / (dropCount - 1).coerceAtLeast(1)
          val dropLen = if (type == ModernWeatherIconType.RAIN_HEAVY) 8.dp.toPx() else 6.dp.toPx()

          for (i in 0 until dropCount) {
            val dx = startX + (i * step)
            val dy = center.y + (h * 0.15f) + (i % 2) * 2.dp.toPx()
            drawLine(
              color = SkyAccent,
              start = Offset(dx, dy),
              end = Offset(dx - 2.dp.toPx(), dy + dropLen),
              strokeWidth = 2.dp.toPx(),
              cap = StrokeCap.Round
            )
          }
        }

        ModernWeatherIconType.THUNDERSTORM -> {
          val cloudCenter = Offset(center.x, center.y - h * 0.15f)
          drawModernCloud(center = cloudCenter, size = w * 0.75f, color = Color(0xFF334155))

          val bolt = Path().apply {
            moveTo(center.x + 2.dp.toPx(), center.y)
            lineTo(center.x - 5.dp.toPx(), center.y + 9.dp.toPx())
            lineTo(center.x + 1.dp.toPx(), center.y + 9.dp.toPx())
            lineTo(center.x - 4.dp.toPx(), center.y + 18.dp.toPx())
            lineTo(center.x + 6.dp.toPx(), center.y + 6.dp.toPx())
            lineTo(center.x + 1.dp.toPx(), center.y + 6.dp.toPx())
            close()
          }
          drawPath(bolt, SunGold)
        }

        ModernWeatherIconType.SNOW -> {
          val cloudCenter = Offset(center.x, center.y - h * 0.12f)
          drawModernCloud(center = cloudCenter, size = w * 0.75f)
          val dots = listOf(
            Offset(center.x - w * 0.18f, center.y + h * 0.22f),
            Offset(center.x, center.y + h * 0.28f),
            Offset(center.x + w * 0.18f, center.y + h * 0.22f)
          )
          for (dot in dots) {
            drawCircle(SkyAccent, radius = 2.dp.toPx(), center = dot)
          }
        }

        ModernWeatherIconType.FOG -> {
          for (i in 0..2) {
            val y = center.y - h * 0.15f + (i * h * 0.15f)
            drawLine(
              brush = Brush.horizontalGradient(
                listOf(Color.Transparent, Color(0xFF94A3B8), Color.Transparent)
              ),
              start = Offset(center.x - w * 0.4f, y),
              end = Offset(center.x + w * 0.4f, y),
              strokeWidth = 2.5.dp.toPx(),
              cap = StrokeCap.Round
            )
          }
        }

        ModernWeatherIconType.WIND -> {
          for (i in 0..2) {
            val y = center.y - h * 0.18f + (i * h * 0.18f)
            drawLine(
              color = SkyAccent,
              start = Offset(center.x - w * 0.35f, y),
              end = Offset(center.x + w * 0.35f - (i * 4.dp.toPx()), y),
              strokeWidth = 2.dp.toPx(),
              cap = StrokeCap.Round
            )
          }
        }
      }
    }
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawModernCloud(
  center: Offset,
  size: Float,
  color: Color = Color(0xFFCBD5E1)
) {
  val w = size
  val h = size * 0.6f
  val r = h * 0.4f
  val baseLeft = center.x - (w / 2f)

  drawRoundRect(
    color = color,
    topLeft = Offset(baseLeft, center.y - r),
    size = Size(w, r * 2),
    cornerRadius = CornerRadius(r, r)
  )
  drawCircle(
    color = color,
    radius = r * 1.15f,
    center = Offset(center.x - (w * 0.14f), center.y - (r * 0.4f))
  )
  drawCircle(
    color = color,
    radius = r * 0.85f,
    center = Offset(center.x + (w * 0.18f), center.y - (r * 0.2f))
  )
}

@Composable
fun TemperatureRangeBar(
  minTempC: Double,
  maxTempC: Double,
  weekMinC: Double,
  weekMaxC: Double,
  isToday: Boolean,
  currentTempC: Double,
  isFahrenheit: Boolean,
  modifier: Modifier = Modifier
) {
  val totalRange = (weekMaxC - weekMinC).coerceAtLeast(1.0)
  val startFraction = ((minTempC - weekMinC) / totalRange).toFloat().coerceIn(0f, 1f)
  val endFraction = ((maxTempC - weekMinC) / totalRange).toFloat().coerceIn(0f, 1f)
  val currentFraction = ((currentTempC - weekMinC) / totalRange).toFloat().coerceIn(0f, 1f)

  Canvas(
    modifier = modifier
      .height(6.dp)
      .fillMaxWidth()
  ) {
    val w = size.width
    val h = size.height
    val r = h / 2f

    // Background track on white canvas
    drawRoundRect(
      color = Color(0xFFE2E8F0),
      topLeft = Offset(0f, 0f),
      size = Size(w, h),
      cornerRadius = CornerRadius(r, r)
    )

    val barStart = w * startFraction
    val barEnd = w * endFraction
    val barWidth = (barEnd - barStart).coerceAtLeast(4.dp.toPx())

    val barGradient = Brush.horizontalGradient(
      colors = listOf(
        SkyAccent,
        SunGold,
        SunOrange
      ),
      startX = barStart,
      endX = barEnd
    )

    drawRoundRect(
      brush = barGradient,
      topLeft = Offset(barStart, 0f),
      size = Size(barWidth, h),
      cornerRadius = CornerRadius(r, r)
    )

    if (isToday) {
      val dotX = w * currentFraction
      drawCircle(
        color = Color(0x33000000),
        radius = 5.dp.toPx(),
        center = Offset(dotX, h / 2f)
      )
      drawCircle(
        color = PureWhite,
        radius = 4.dp.toPx(),
        center = Offset(dotX, h / 2f)
      )
      drawCircle(
        color = InkPrimary,
        radius = 2.5.dp.toPx(),
        center = Offset(dotX, h / 2f)
      )
    }
  }
}

@Composable
fun UvIndexArcGauge(
  uv: ModernUvIndex,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(140.dp)
        .padding(top = 8.dp),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.size(130.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width / 2f - 10.dp.toPx()
        val strokeW = 8.dp.toPx()

        drawArc(
          color = Color(0xFFE2E8F0),
          startAngle = 150f,
          sweepAngle = 240f,
          useCenter = false,
          topLeft = Offset(center.x - radius, center.y - radius),
          size = Size(radius * 2, radius * 2),
          style = Stroke(strokeW, cap = StrokeCap.Round)
        )

        val progressFrac = (uv.value / 12.0).toFloat().coerceIn(0f, 1f)
        val sweep = progressFrac * 240f

        val arcGradient = Brush.sweepGradient(
          listOf(
            Color(0xFF10B981),
            Color(0xFFFBBF24),
            Color(0xFFF97316),
            Color(0xFFEF4444),
            Color(0xFF8B5CF6)
          ),
          center = center
        )

        drawArc(
          brush = arcGradient,
          startAngle = 150f,
          sweepAngle = sweep.coerceAtLeast(2f),
          useCenter = false,
          topLeft = Offset(center.x - radius, center.y - radius),
          size = Size(radius * 2, radius * 2),
          style = Stroke(strokeW, cap = StrokeCap.Round)
        )
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = String.format("%.0f", uv.value),
          style = MaterialTheme.typography.displaySmall.copy(
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            color = WeatherThemeColors.textPrimary
          )
        )
        Text(
          text = uv.rating.uppercase(),
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            color = when (uv.rating) {
              "Low" -> Color(0xFF059669)
              "Moderate" -> SunGold
              "High" -> SunOrange
              "Very High" -> SevereRed
              else -> Color(0xFF7C3AED)
            }
          )
        )
      }
    }

    Text(
      text = uv.advice,
      style = MaterialTheme.typography.bodySmall.copy(
        fontFamily = InterFontFamily,
        color = WeatherThemeColors.textSecondary,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      ),
      modifier = Modifier.padding(top = 4.dp)
    )
  }
}

@Composable
fun ModernWindCompass(
  wind: ModernWind,
  isFahrenheit: Boolean,
  modifier: Modifier = Modifier
) {
  val speedDisplay = formatWindSpeed(wind.speedKmh, imperial = isFahrenheit)
  val gustDisplay = wind.gustSpeedKmh?.let { formatWindSpeed(it, imperial = isFahrenheit) }
  val isDark = WeatherThemeColors.isDark
  val primaryTextColor = WeatherThemeColors.textPrimary
  val secondaryTextColor = WeatherThemeColors.textSecondary
  val mutedTextColor = WeatherThemeColors.textMuted

  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = speedDisplay,
        style = MaterialTheme.typography.headlineMedium.copy(
          fontFamily = OutfitFontFamily,
          fontWeight = FontWeight.Bold,
          color = primaryTextColor
        )
      )
      Text(
        text = "Wind · From ${wind.directionCardinal}",
        style = MaterialTheme.typography.bodySmall.copy(
          fontFamily = InterFontFamily,
          color = secondaryTextColor
        )
      )
      if (gustDisplay != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Gusts to $gustDisplay",
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            color = SkyAccent
          )
        )
      }
      Text(
        text = wind.description,
        style = MaterialTheme.typography.bodySmall.copy(
          fontFamily = InterFontFamily,
          color = mutedTextColor
        ),
        modifier = Modifier.padding(top = 2.dp)
      )
    }

    Box(
      modifier = Modifier.size(90.dp),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.size(80.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val r = size.width / 2f - 4.dp.toPx()

        drawCircle(
          color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
          radius = r,
          center = center,
          style = Stroke(2.dp.toPx())
        )

        val cardAngs = listOf(0f, 90f, 180f, 270f)
        for (a in cardAngs) {
          val rad = (a - 90f) * PI / 180f
          val p1 = Offset(center.x + (r * cos(rad)).toFloat(), center.y + (r * sin(rad)).toFloat())
          val p2 = Offset(center.x + ((r - 4.dp.toPx()) * cos(rad)).toFloat(), center.y + ((r - 4.dp.toPx()) * sin(rad)).toFloat())
          drawLine(
            color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
            start = p1,
            end = p2,
            strokeWidth = 1.5.dp.toPx()
          )
        }

        rotate(degrees = wind.degrees.toFloat(), pivot = center) {
          val needlePath = Path().apply {
            moveTo(center.x, center.y - r + 3.dp.toPx())
            lineTo(center.x + 4.dp.toPx(), center.y)
            lineTo(center.x - 4.dp.toPx(), center.y)
            close()
          }
          drawPath(needlePath, SkyAccent)

          drawLine(
            color = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1),
            start = center,
            end = Offset(center.x, center.y + (r * 0.7f)),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
          )
        }

        drawCircle(if (isDark) PureWhite else InkPrimary, radius = 3.dp.toPx(), center = center)
      }

      Text(
        text = "N",
        style = MaterialTheme.typography.labelSmall.copy(
          fontFamily = InterFontFamily,
          fontWeight = FontWeight.Bold,
          color = primaryTextColor,
          fontSize = 9.sp
        ),
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 2.dp)
      )
    }
  }
}

@Composable
fun SunArcVisualizer(
  sunrise: String,
  sunset: String,
  firstLight: String?,
  dusk: String?,
  /** Minutes since midnight in the CITY's local time (not the device's). */
  currentMinutes: Int,
  modifier: Modifier = Modifier
) {
  val curMinutes = currentMinutes
  val riseMinutes = parseTimeToMinutes(sunrise, 6 * 60 + 30)
  val setMinutes = parseTimeToMinutes(sunset, 18 * 60 + 45)
  val dayLen = (setMinutes - riseMinutes).coerceAtLeast(60)

  val sunFrac = ((curMinutes - riseMinutes).toFloat() / dayLen.toFloat()).coerceIn(0f, 1f)
  val isDay = curMinutes in riseMinutes..setMinutes

  Column(modifier = modifier.fillMaxWidth()) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(100.dp),
      contentAlignment = Alignment.BottomCenter
    ) {
      Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h)
        val arcR = (w / 2f) - 16.dp.toPx()

        drawLine(
          color = Color(0xFFE2E8F0),
          start = Offset(0f, h - 2.dp.toPx()),
          end = Offset(w, h - 2.dp.toPx()),
          strokeWidth = 1.dp.toPx()
        )

        drawArc(
          color = if (isDay) SunGold else Color(0xFFCBD5E1),
          startAngle = 180f,
          sweepAngle = 180f,
          useCenter = false,
          topLeft = Offset(center.x - arcR, center.y - arcR),
          size = Size(arcR * 2, arcR * 2),
          style = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round
          )
        )

        val sunAngDeg = 180f + (sunFrac * 180f)
        val sunAngRad = sunAngDeg * PI / 180.0
        val sunPos = Offset(
          center.x + (arcR * cos(sunAngRad)).toFloat(),
          center.y + (arcR * sin(sunAngRad)).toFloat()
        )

        drawCircle(
          color = SunGold.copy(alpha = 0.2f),
          radius = 12.dp.toPx(),
          center = sunPos
        )
        drawCircle(
          color = PureWhite,
          radius = 5.dp.toPx(),
          center = sunPos
        )
        drawCircle(
          color = SunGold,
          radius = 3.dp.toPx(),
          center = sunPos
        )
      }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(horizontalAlignment = Alignment.Start) {
        Text("Sunrise", style = MaterialTheme.typography.labelSmall.copy(color = WeatherThemeColors.textTertiary))
        Text(sunrise, style = MaterialTheme.typography.titleMedium.copy(color = WeatherThemeColors.textPrimary, fontWeight = FontWeight.Bold))
        if (firstLight != null) Text("First light (est.): $firstLight", style = MaterialTheme.typography.labelSmall.copy(color = WeatherThemeColors.textMuted, fontSize = 10.sp))
      }

      Column(horizontalAlignment = Alignment.End) {
        Text("Sunset", style = MaterialTheme.typography.labelSmall.copy(color = WeatherThemeColors.textTertiary))
        Text(sunset, style = MaterialTheme.typography.titleMedium.copy(color = WeatherThemeColors.textPrimary, fontWeight = FontWeight.Bold))
        if (dusk != null) Text("Dusk (est.): $dusk", style = MaterialTheme.typography.labelSmall.copy(color = WeatherThemeColors.textMuted, fontSize = 10.sp))
      }
    }
  }
}

@Composable
fun SimulatedRadarCard(
  modifier: Modifier = Modifier
) {
  var isPlaying by remember { mutableStateOf(true) }
  val isDark = WeatherThemeColors.isDark

  val transition = rememberInfiniteTransition(label = "RadarTransition")
  val scanProgress by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 3000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "RadarScan"
  )

  FrostedCard(
    title = "Precipitation Radar (Demo)",
    headerAction = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isPlaying) (if (isDark) Color(0xFF0F2642) else Color(0xFFE0F2FE)) else WeatherThemeColors.surfaceSubtle)
            .clickable { isPlaying = !isPlaying }
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (isPlaying) "Pause" else "Play",
              tint = if (isPlaying) SkyAccent else WeatherThemeColors.textTertiary,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isPlaying) "DEMO" else "PAUSED",
              style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                color = if (isPlaying) SkyAccent else WeatherThemeColors.textTertiary,
                fontSize = 9.sp
              )
            )
          }
        }
      }
    },
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(150.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF0F172A))
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          val center = Offset(w / 2f, h / 2f)

          for (x in 1..4) {
            drawLine(
              color = Color(0x15FFFFFF),
              start = Offset(w * (x / 5f), 0f),
              end = Offset(w * (x / 5f), h),
              strokeWidth = 1.dp.toPx()
            )
          }
          for (y in 1..3) {
            drawLine(
              color = Color(0x15FFFFFF),
              start = Offset(0f, h * (y / 4f)),
              end = Offset(w, h * (y / 4f)),
              strokeWidth = 1.dp.toPx()
            )
          }

          drawCircle(Color(0x1AFFFFFF), radius = h * 0.35f, center = center, style = Stroke(1.dp.toPx()))
          drawCircle(Color(0x1AFFFFFF), radius = h * 0.65f, center = center, style = Stroke(1.dp.toPx()))

          val animOffset = if (isPlaying) scanProgress * 30.dp.toPx() else 0f

          drawCircle(
            brush = Brush.radialGradient(
              listOf(Color(0x7734D399), Color(0x3338BDF8), Color.Transparent),
              center = Offset(center.x - 40.dp.toPx() + animOffset, center.y - 20.dp.toPx()),
              radius = 45.dp.toPx()
            ),
            radius = 45.dp.toPx(),
            center = Offset(center.x - 40.dp.toPx() + animOffset, center.y - 20.dp.toPx())
          )

          drawCircle(
            brush = Brush.radialGradient(
              listOf(Color(0x8860A5FA), Color(0x4438BDF8), Color.Transparent),
              center = Offset(center.x + 30.dp.toPx() + (animOffset * 0.8f), center.y + 15.dp.toPx()),
              radius = 50.dp.toPx()
            ),
            radius = 50.dp.toPx(),
            center = Offset(center.x + 30.dp.toPx() + (animOffset * 0.8f), center.y + 15.dp.toPx())
          )

          drawCircle(
            brush = Brush.radialGradient(
              listOf(Color(0xAAFB923C), Color(0x55FBBF24), Color.Transparent),
              center = Offset(center.x + 25.dp.toPx() + (animOffset * 0.8f), center.y + 12.dp.toPx()),
              radius = 20.dp.toPx()
            ),
            radius = 20.dp.toPx(),
            center = Offset(center.x + 25.dp.toPx() + (animOffset * 0.8f), center.y + 12.dp.toPx())
          )

          drawCircle(PureWhite, radius = 4.dp.toPx(), center = center)
          drawCircle(SkyAccent, radius = 2.dp.toPx(), center = center)
        }

        Text(
          text = if (isPlaying) "Illustrative animation · not real radar data" else "Illustrative animation (paused)",
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = InterFontFamily,
            color = Color(0xAAFFFFFF),
            fontSize = 9.sp
          ),
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(8.dp)
        )
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Light", style = MaterialTheme.typography.labelSmall.copy(color = WeatherThemeColors.textTertiary, fontSize = 10.sp))

        Canvas(modifier = Modifier.width(140.dp).height(4.dp)) {
          drawRoundRect(
            brush = Brush.horizontalGradient(
              listOf(
                Color(0xFF38BDF8),
                Color(0xFF34D399),
                Color(0xFFFBBF24),
                Color(0xFFFB923C),
                Color(0xFFF87171)
              )
            ),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
          )
        }

        Text("Heavy", style = MaterialTheme.typography.labelSmall.copy(color = WeatherThemeColors.textTertiary, fontSize = 10.sp))
      }
    }
  }
}

private fun parseTimeToMinutes(timeStr: String, fallback: Int): Int {
  return try {
    val parts = timeStr.trim().split(":")
    parts[0].toInt() * 60 + parts[1].toInt()
  } catch (e: Exception) {
    fallback
  }
}

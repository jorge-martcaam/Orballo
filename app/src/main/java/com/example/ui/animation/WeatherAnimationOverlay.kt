package com.example.ui.animation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

enum class WeatherVisualType {
    RAIN,
    HEAVY_RAIN,
    DRIZZLE,
    FOG,
    SUNNY,
    CLOUDY,
    NEUTRAL
}

object WeatherAnimationHelper {
    fun determineVisualType(weatherCode: Int, precipitation: Double = 0.0): WeatherVisualType {
        return when (weatherCode) {
            0, 1 -> WeatherVisualType.SUNNY
            2, 3 -> if (precipitation > 0.1) WeatherVisualType.DRIZZLE else WeatherVisualType.CLOUDY
            45, 48 -> WeatherVisualType.FOG // Brétema
            51, 53, 55, 56, 57 -> WeatherVisualType.DRIZZLE // Orballo
            61, 80 -> WeatherVisualType.RAIN
            63, 65, 81, 82 -> WeatherVisualType.HEAVY_RAIN
            95, 96, 99 -> WeatherVisualType.HEAVY_RAIN // Thunderstorm
            71, 73, 75, 77, 85, 86 -> WeatherVisualType.RAIN
            else -> if (precipitation > 0.5) WeatherVisualType.RAIN else WeatherVisualType.NEUTRAL
        }
    }
}

/**
 * Static atmospheric overlay for weather card backdrops.
 * Uses single-pass static Canvas drawing without continuous 60fps frame loops
 * to preserve CPU efficiency and prevent software-rasterizer stalling.
 */
@Composable
fun WeatherAnimationOverlay(
    visualType: WeatherVisualType,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w == 0f || h == 0f) return@Canvas

        when (visualType) {
            WeatherVisualType.SUNNY -> {
                // Static radiant solar glow near the top-right
                val sunCenter = Offset(w * 0.88f, h * 0.22f)
                val radius = 90f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFEF08A).copy(alpha = 0.35f),
                            Color(0xFFFDE047).copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = sunCenter,
                        radius = radius * 2.2f
                    ),
                    center = sunCenter,
                    radius = radius * 2.2f
                )
            }
            WeatherVisualType.RAIN,
            WeatherVisualType.HEAVY_RAIN,
            WeatherVisualType.DRIZZLE -> {
                // Static subtle rain slant lines
                val slantX = if (visualType == WeatherVisualType.HEAVY_RAIN) 12f else 7f
                val count = if (visualType == WeatherVisualType.HEAVY_RAIN) 20 else 10
                for (i in 0 until count) {
                    val x = (w * (i + 1) / (count + 1))
                    val y = (h * ((i * 37) % 100) / 100f)
                    drawLine(
                        color = Color(0xFFBAE6FD).copy(alpha = 0.25f),
                        start = Offset(x, y),
                        end = Offset(x - slantX, y + 24f),
                        strokeWidth = 1.5f,
                        cap = StrokeCap.Round
                    )
                }
            }
            WeatherVisualType.FOG -> {
                // Soft horizontal atmospheric haze bands
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        startY = h * 0.3f,
                        endY = h * 0.7f
                    )
                )
            }
            WeatherVisualType.CLOUDY,
            WeatherVisualType.NEUTRAL -> {
                // Neutral soft radial ambient vignette
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.6f
                    )
                )
            }
        }
    }
}

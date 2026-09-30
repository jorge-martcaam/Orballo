package com.example.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertLevel
import com.example.data.model.GaliciaLocation
import com.example.data.model.UvIndexUtils
import com.example.data.model.WeatherAlert
import com.example.data.model.WeatherTimeUtils
import com.example.data.repository.FullWeatherData
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertYellow
import com.example.ui.theme.OceanSkyBlue
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WeatherHeroCard(
    data: FullWeatherData,
    defaultLocation: GaliciaLocation,
    onSetDefault: (GaliciaLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    val hour = remember(data.timezone) { WeatherTimeUtils.getCurrentHourInZone(data.timezone) }
    val isNight = hour < 7 || hour >= 22
    val code = data.current.weatherCode ?: 0
    val precip = data.current.precipitation ?: 0.0

    val isRain = precip > 0.1 || (code in 51..67) || (code in 80..82) || (code in 95..99)
    val isFogOrCloudy = (code in 45..48) || (code == 3)

    val gradientColors = remember(isRain, isNight, isFogOrCloudy) {
        when {
            isRain -> listOf(Color(0xFF1E293B), Color(0xFF0F172A))
            isNight -> listOf(Color(0xFF090D16), Color(0xFF0F172A))
            isFogOrCloudy -> listOf(Color(0xFF334155), Color(0xFF1E293B))
            else -> listOf(OceanSkyBlue, Color(0xFF0369A1))
        }
    }

    val gradientBrush = Brush.verticalGradient(colors = gradientColors)

    val topColor = gradientColors.first()
    val bgLuminance = 0.2126f * topColor.red + 0.7152f * topColor.green + 0.0722f * topColor.blue
    val isLightBg = bgLuminance > 0.45f

    val heroPrimaryTextColor = if (isLightBg) Color(0xFF0F172A) else Color.White
    val heroSecondaryTextColor = if (isLightBg) Color(0xFF334155) else Color.White.copy(alpha = 0.85f)
    val heroSubtleTextColor = if (isLightBg) Color(0xFF475569) else Color.White.copy(alpha = 0.8f)
    val heroDividerColor = if (isLightBg) Color(0x33000000) else Color.White.copy(alpha = 0.25f)
    val heroPillBg = if (isLightBg) Color(0x1F000000) else Color.White.copy(alpha = 0.22f)

    val isDefault = data.location.name == defaultLocation.name

    val badgeLabel = when {
        !data.location.isGalicia -> "🌍 Open-Meteo (ECMWF Global)"
        data.isFallback -> "📡 Open-Meteo (Alternativo)"
        else -> "🛰️ MeteoGalicia (0-48h WRF)"
    }

    val uvVal = data.current.uvIndex ?: 0.0
    val uvSafety = remember(uvVal) { UvIndexUtils.getSafetyLevel(uvVal) }
    val tempText = data.current.temperature?.let { "${it.roundToInt()}°" } ?: "--°"
    val apparentTempStr = data.current.apparentTemperature?.let { "${it.roundToInt()}°" } ?: "--°"
    val localTime = remember(data.timezone) { WeatherTimeUtils.formatLocationTime(data.timezone) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(gradientBrush)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // 1. Header: Location Name & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = data.location.name,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = heroPrimaryTextColor
                            )
                            if (isDefault) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Localización predeterminada",
                                    tint = Color(0xFFFEF08A),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (data.location.isGalicia) {
                                "${data.location.province} • $localTime"
                            } else {
                                "${data.location.province} · ${data.location.country} • $localTime"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = heroSecondaryTextColor
                        )

                        // Default location selector control
                        Spacer(modifier = Modifier.height(6.dp))
                        if (isDefault) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(heroPillBg)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                    .testTag("badge_default_location")
                            ) {
                                Text(
                                    text = "★ Predeterminada (Widget e App)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = heroPrimaryTextColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(heroPillBg)
                                    .clickable { onSetDefault(data.location) }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                    .testTag("btn_set_as_default")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = heroPrimaryTextColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Fixar como predeterminada",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = heroPrimaryTextColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = data.iconEmoji,
                        fontSize = 38.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Central Temperature & Condition
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = tempText,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 50.sp
                        ),
                        color = heroPrimaryTextColor
                    )

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = data.conditionDescription,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = heroPrimaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Sensación $apparentTempStr",
                                style = MaterialTheme.typography.bodySmall,
                                color = heroSecondaryTextColor
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.testTag("hero_uv_index")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(uvSafety.hexColor))
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "UV ${String.format(Locale.US, "%.1f", uvVal)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = heroPrimaryTextColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    color = heroDividerColor,
                    thickness = 1.dp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 3. Compact Horizontal Metrics Row (Vento, Humidade, Precipitación)
                val humidityStr = data.current.relativeHumidity?.let { "${it.roundToInt()}%" } ?: "--%"
                val windSpeedVal = data.current.windSpeed?.roundToInt()
                val windGustsVal = data.current.windGusts?.roundToInt()
                val windDisplay = if (windSpeedVal != null) {
                    if (windGustsVal != null && windGustsVal > windSpeedVal) {
                        "$windSpeedVal km/h (r. $windGustsVal)"
                    } else {
                        "$windSpeedVal km/h"
                    }
                } else {
                    "-- km/h"
                }
                val precipVal = data.current.precipitation
                val precipStr = precipVal?.let { "${String.format(Locale.US, "%.1f", it)} mm" } ?: "-- mm"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Metric 1: Vento
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Vento",
                            style = MaterialTheme.typography.labelSmall,
                            color = heroSubtleTextColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = windDisplay,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = heroPrimaryTextColor
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color = heroDividerColor
                    )

                    // Metric 2: Humidade
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Humidade",
                            style = MaterialTheme.typography.labelSmall,
                            color = heroSubtleTextColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = humidityStr,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = heroPrimaryTextColor
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color = heroDividerColor
                    )

                    // Metric 3: Precipitación
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Precipitación",
                            style = MaterialTheme.typography.labelSmall,
                            color = heroSubtleTextColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = precipStr,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = heroPrimaryTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Subtle Footer: Source Provenance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = badgeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = heroSubtleTextColor
                    )
                }
            }
        }
    }
}



@Composable
fun HeroMetricCell(
    icon: ImageVector,
    label: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

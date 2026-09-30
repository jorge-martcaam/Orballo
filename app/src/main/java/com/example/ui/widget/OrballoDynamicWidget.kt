package com.example.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalOrballoColors
import com.example.ui.theme.OrballoTheme
import com.example.ui.theme.OrballoThemeTokens

data class OrballoHourlyPoint(
    val timeLabel: String,
    val temp: String,
    val rainProb: String
)

data class OrballoDayShift(
    val name: String,
    val summary: String,
    val temp: String,
    val rainProb: String,
    val icon: ImageVector
)

data class OrballoWidgetData(
    val concello: String = "Santiago de Compostela",
    val currentTemp: String = "14°",
    val weatherCondition: String = "Orballo constante • Sensación 13°C",
    val rainProb: String = "85%",
    val tempRange: String = "11° / 16°",
    val windSpeed: String = "24 km/h S",
    val humidity: String = "92%",
    val pressure: String = "1014 hPa",
    val precipitationMm: String = "2.4 mm",
    val aqiLabel: String = "Boa (22 ICA)",
    val stationStamp: String = "MeteoGalicia • Est. 10145",
    val hourlyTimeline: List<OrballoHourlyPoint> = listOf(
        OrballoHourlyPoint("+1h", "14°", "75%"),
        OrballoHourlyPoint("+2h", "14°", "80%"),
        OrballoHourlyPoint("+3h", "13°", "85%"),
        OrballoHourlyPoint("+4h", "12°", "60%")
    ),
    val dayShifts: List<OrballoDayShift> = listOf(
        OrballoDayShift("Mañá", "Orballo suave", "12°", "70%", Icons.Filled.WbCloudy),
        OrballoDayShift("Tarde", "Orballo constante", "15°", "85%", Icons.Filled.WaterDrop),
        OrballoDayShift("Noite", "Chuvia feble", "13°", "60%", Icons.Filled.WbCloudy)
    )
)

val SampleOrballoMockData = OrballoWidgetData()

@Composable
fun OrballoDynamicWidget(
    data: OrballoWidgetData = SampleOrballoMockData,
    isDark: Boolean = true,
    modifier: Modifier = Modifier
) {
    OrballoTheme(isDark = isDark) {
        val colors = LocalOrballoColors.current
        val outerShape = RoundedCornerShape(OrballoThemeTokens.OuterRadius)

        BoxWithConstraints(
            modifier = modifier
                .clip(outerShape)
                .background(colors.surfaceContainer)
                .border(OrballoThemeTokens.SubtleBorderWidth, colors.outlineVariant, outerShape)
                .padding(12.dp)
                .testTag("orballo_dynamic_widget")
        ) {
            val width = maxWidth
            val height = maxHeight

            when {
                // 4x3: Full Desktop Suite (height >= 220.dp)
                height >= 220.dp -> {
                    Widget4x3FullSuiteContent(data = data)
                }
                // 4x2: Standard Hero with 3 shifts (width >= 240.dp && height >= 130.dp)
                width >= 240.dp && height >= 130.dp -> {
                    Widget4x2StandardShiftContent(data = data)
                }
                // 2x2: Essential Square (height >= 120.dp)
                height >= 120.dp -> {
                    Widget2x2SquareContent(data = data)
                }
                // 4x1: Horizontal Row (width >= 240.dp)
                width >= 240.dp -> {
                    Widget4x1HorizontalRowContent(data = data)
                }
                // 2x1: Minimal Glance
                else -> {
                    Widget2x1CompactPillContent(data = data)
                }
            }
        }
    }
}

@Composable
private fun Widget2x1CompactPillContent(data: OrballoWidgetData) {
    val colors = LocalOrballoColors.current
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Filled.WaterDrop,
                contentDescription = "Estado do tempo",
                tint = colors.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = data.concello,
                    color = colors.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Chuvia: ${data.rainProb}",
                    color = colors.secondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Text(
            text = data.currentTemp,
            color = colors.onSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun Widget2x2SquareContent(data: OrballoWidgetData) {
    val colors = LocalOrballoColors.current
    val panelShape = RoundedCornerShape(OrballoThemeTokens.InnerRadius)
    val pillShape = RoundedCornerShape(OrballoThemeTokens.PillRadius)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data.concello,
                color = colors.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(pillShape)
                    .background(colors.surfaceContainerHigh)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "MeteoGalicia",
                    color = colors.primary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = data.currentTemp,
                color = colors.onSurface,
                fontSize = 36.sp,
                fontWeight = FontWeight.Light
            )
            Icon(
                imageVector = Icons.Filled.WaterDrop,
                contentDescription = "Icona",
                tint = colors.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(panelShape)
                .background(colors.surfaceContainerLow)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Rango", color = colors.onSurfaceVariant, fontSize = 9.sp)
                    Text(data.tempRange, color = colors.onSurface, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Vento", color = colors.onSurfaceVariant, fontSize = 9.sp)
                    Text(data.windSpeed, color = colors.onSurface, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun Widget4x1HorizontalRowContent(data: OrballoWidgetData) {
    val colors = LocalOrballoColors.current
    val panelShape = RoundedCornerShape(OrballoThemeTokens.InnerRadius)

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Concello e Temp actual
        Column(modifier = Modifier.width(110.dp)) {
            Text(
                text = data.concello,
                color = colors.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = data.currentTemp,
                    color = colors.onSurface,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = data.rainProb,
                    color = colors.secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Liña horaria ECMWF IFS
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(panelShape)
                .background(colors.surfaceContainerLow)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            data.hourlyTimeline.take(4).forEach { item ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(item.timeLabel, color = colors.onSurfaceVariant, fontSize = 9.sp)
                    Text(item.temp, color = colors.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(item.rainProb, color = colors.primary, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun Widget4x2StandardShiftContent(data: OrballoWidgetData) {
    val colors = LocalOrballoColors.current
    val panelShape = RoundedCornerShape(OrballoThemeTokens.InnerRadius)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Cabeceira
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = data.concello,
                    color = colors.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = data.weatherCondition,
                    color = colors.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = data.currentTemp,
                    color = colors.onSurface,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }

        // 3 Franxas oficiais de MeteoGalicia (Mañá, Tarde, Noite)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            data.dayShifts.forEach { shift ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(panelShape)
                        .background(colors.surfaceContainerLow)
                        .border(OrballoThemeTokens.SubtleBorderWidth, colors.outlineVariant, panelShape)
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = shift.name,
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Icon(
                            imageVector = shift.icon,
                            contentDescription = shift.name,
                            tint = colors.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = shift.temp,
                            color = colors.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = shift.rainProb,
                            color = colors.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Widget4x3FullSuiteContent(data: OrballoWidgetData) {
    val colors = LocalOrballoColors.current
    val panelShape = RoundedCornerShape(OrballoThemeTokens.InnerRadius)
    val pillShape = RoundedCornerShape(OrballoThemeTokens.PillRadius)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Cabeceira con Concello, Fonte e Temp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.concello,
                    color = colors.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = data.weatherCondition,
                    color = colors.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Text(
                    text = data.stationStamp,
                    color = colors.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
            Text(
                text = data.currentTemp,
                color = colors.onSurface,
                fontSize = 42.sp,
                fontWeight = FontWeight.Light
            )
        }

        // 3 Franxas oficiais de MeteoGalicia (Mañá, Tarde, Noite)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            data.dayShifts.forEach { shift ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(panelShape)
                        .background(colors.surfaceContainerLow)
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = shift.name,
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Icon(
                            imageVector = shift.icon,
                            contentDescription = shift.name,
                            tint = colors.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = shift.temp,
                            color = colors.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Grella de 4 métricas e Calidade do Aire (ICA)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(panelShape)
                .background(colors.surfaceContainerLow)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetricBadge(icon = Icons.Filled.WaterDrop, label = "Chuvia", value = data.precipitationMm)
            MetricBadge(icon = Icons.Filled.Air, label = "Vento", value = data.windSpeed)
            MetricBadge(icon = Icons.Filled.Compress, label = "Presión", value = data.pressure)
            
            Box(
                modifier = Modifier
                    .clip(pillShape)
                    .background(colors.surfaceContainerHigh)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = data.aqiLabel,
                    color = colors.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MetricBadge(
    icon: ImageVector,
    label: String,
    value: String
) {
    val colors = LocalOrballoColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = colors.secondary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Column {
            Text(label, color = colors.onSurfaceVariant, fontSize = 8.sp)
            Text(value, color = colors.onSurface, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

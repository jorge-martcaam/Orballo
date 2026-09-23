package com.example.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import com.example.ui.animation.WeatherAnimationHelper
import com.example.ui.animation.WeatherAnimationOverlay
import com.example.ui.animation.WeatherVisualType
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.location.LocationHelper
import com.example.data.model.AlertLevel
import com.example.data.model.DayForecast
import com.example.data.model.GaliciaLocation
import com.example.data.model.HourlyForecast
import com.example.data.model.HourlyForecastUtils
import com.example.data.model.UvIndexUtils
import com.example.data.model.UvSafetyLevel
import com.example.data.model.WeatherAlert
import com.example.data.model.WeatherDataSource
import com.example.data.repository.AirQualityData
import com.example.data.repository.FullWeatherData
import com.example.data.repository.HistoricalDayData
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertYellow
import com.example.ui.theme.AtlanticTeal
import com.example.ui.theme.OceanSkyBlue
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }

    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val defaultLocation by viewModel.defaultLocation.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()
    val aqiState by viewModel.airQualityState.collectAsState()
    val historicalState by viewModel.historicalState.collectAsState()
    val currentTab by viewModel.selectedTab.collectAsState()
    val historicalDate by viewModel.historicalDate.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.tryGpsLocation(locationHelper)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Tempo en Galicia",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "MeteoGalicia e as Rías",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (locationHelper.hasLocationPermission()) {
                                viewModel.tryGpsLocation(locationHelper)
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("gps_location_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Detectar localización GPS",
                            tint = if (selectedLocation.isGps) OceanSkyBlue else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { viewModel.refresh() },
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar datos"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Galicia Location Selector Chips
            item {
                LocationSelectorSection(
                    selectedLocation = selectedLocation,
                    defaultLocation = defaultLocation,
                    onLocationSelected = { viewModel.selectLocation(it) },
                    onSetDefault = { viewModel.setDefaultLocation(it) }
                )
            }

            // 2. Weather Content or Loading/Error
            when (val state = weatherState) {
                is WeatherUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = OceanSkyBlue)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Cargando o tempo en Galicia en tempo real...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                is WeatherUiState.Error -> {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = AlertRed.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Non se puideron cargar os datos do tempo",
                                    color = AlertRed,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = state.message, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.refresh() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                                ) {
                                    Text("Tentar de novo")
                                }
                            }
                        }
                    }
                }
                is WeatherUiState.Success -> {
                    // Hero Card & Severe Alerts
                    item {
                        WeatherHeroCard(
                            data = state.data,
                            defaultLocation = defaultLocation,
                            onSetDefault = { viewModel.setDefaultLocation(it) }
                        )
                    }

                    // Severe Alerts Banner (if active)
                    if (state.data.alerts.isNotEmpty()) {
                        item {
                            SevereAlertsSection(alerts = state.data.alerts)
                        }
                    }

                    // Current Day Forecast (24-Hour Timeline from MeteoGalicia WRF)
                    if (state.data.todayHourlyForecast.isNotEmpty()) {
                        item {
                            TodayHourlyForecastSection(hourlyList = state.data.todayHourlyForecast)
                        }
                    }

                    // Essential Weather Metrics Grid (Humidity, Wind, Gusts, Pressure, UV)
                    item {
                        EssentialMetricsSection(data = state.data)
                    }
                }
            }

            // 3. Section Tabs (7-Day Forecast, Air Quality AQI, Historical)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                TabRow(
                    selectedTabIndex = currentTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Tab(
                        selected = currentTab == WeatherTab.FORECAST,
                        onClick = { viewModel.selectTab(WeatherTab.FORECAST) },
                        text = { Text("Predición 7 días") },
                        modifier = Modifier.testTag("tab_forecast")
                    )
                    Tab(
                        selected = currentTab == WeatherTab.AIR_QUALITY,
                        onClick = { viewModel.selectTab(WeatherTab.AIR_QUALITY) },
                        text = { Text("Calidade do aire") },
                        modifier = Modifier.testTag("tab_air_quality")
                    )
                    Tab(
                        selected = currentTab == WeatherTab.HISTORICAL,
                        onClick = { viewModel.selectTab(WeatherTab.HISTORICAL) },
                        text = { Text("Histórico") },
                        modifier = Modifier.testTag("tab_historical")
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 4. Tab Content
            when (currentTab) {
                WeatherTab.FORECAST -> {
                    val currentSuccess = weatherState as? WeatherUiState.Success
                    if (currentSuccess != null) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Predición a 7 días",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Días 1-2 MeteoGalicia • Días 3-7 ECMWF",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        items(currentSuccess.data.sevenDayForecast) { forecast ->
                            DayForecastCard(forecast = forecast)
                        }
                    }
                }
                WeatherTab.AIR_QUALITY -> {
                    item {
                        AirQualitySection(
                            aqiState = aqiState,
                            locationName = selectedLocation.name,
                            onRetry = { viewModel.refresh() }
                        )
                    }
                }
                WeatherTab.HISTORICAL -> {
                    item {
                        HistoricalWeatherSection(
                            historicalState = historicalState,
                            selectedDate = historicalDate,
                            locationName = selectedLocation.name,
                            onQueryDate = { viewModel.queryHistoricalDate(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocationSelectorSection(
    selectedLocation: GaliciaLocation,
    defaultLocation: GaliciaLocation,
    onLocationSelected: (GaliciaLocation) -> Unit,
    onSetDefault: (GaliciaLocation) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GaliciaLocation.PRESETS.forEach { location ->
            val isSelected = !selectedLocation.isGps && selectedLocation.name == location.name
            val isDefault = location.name == defaultLocation.name
            val containerColor = if (isSelected) OceanSkyBlue else MaterialTheme.colorScheme.surfaceVariant
            val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(containerColor)
                    .clickable { onLocationSelected(location) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("location_chip_${location.name.replace(" ", "_")}")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDefault) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Localización predeterminada",
                            tint = if (isSelected) Color(0xFFFEF08A) else Color(0xFFEAB308),
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherHeroCard(
    data: FullWeatherData,
    defaultLocation: GaliciaLocation,
    onSetDefault: (GaliciaLocation) -> Unit
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            OceanSkyBlue,
            Color(0xFF0369A1)
        )
    )

    val visualType = remember(data.current.weatherCode, data.current.precipitation) {
        WeatherAnimationHelper.determineVisualType(
            data.current.weatherCode ?: 0,
            data.current.precipitation ?: 0.0
        )
    }

    val isDefault = !data.location.isGps && data.location.name == defaultLocation.name

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(gradientBrush)
                .fillMaxWidth()
        ) {
            // Dynamic animation overlay based on Galician conditions (Rain/Orballo, Fog/Brétema, Sun)
            WeatherAnimationOverlay(
                visualType = visualType,
                modifier = Modifier.matchParentSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = data.location.name,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            if (isDefault) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Localización predeterminada",
                                    tint = Color(0xFFFEF08A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "${data.location.province} • En tempo real",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Provenance Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (data.isFallback) Color(0xFFF59E0B).copy(alpha = 0.35f) else Color(0xFF0D9488).copy(alpha = 0.40f))
                                .border(1.dp, if (data.isFallback) Color(0xFFFDE047) else Color(0xFF5EEAD4), RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (data.isFallback) "📡 Open-Meteo (Alternativo)" else "🛰️ MeteoGalicia (0-48h WRF)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Default location selector control
                        if (!data.location.isGps) {
                            if (isDefault) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.22f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "★ Predeterminada (Widget e App)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.18f))
                                        .clickable { onSetDefault(data.location) }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                        .testTag("btn_set_as_default")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.StarBorder,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Fixar como predeterminada",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = data.iconEmoji,
                        fontSize = 42.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${data.current.temperature?.roundToInt() ?: 0}°C",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        val uvVal = data.current.uvIndex ?: 0.0
                        val uvSafety = remember(uvVal) { UvIndexUtils.getSafetyLevel(uvVal) }
                        Column(
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .testTag("hero_uv_index")
                        ) {
                            Text(
                                text = "UV ${String.format(java.util.Locale.US, "%.1f", uvVal)}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(uvSafety.hexColor).copy(alpha = 0.35f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = uvSafety.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f, fill = false),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = data.conditionDescription,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Sensación térmica ${data.current.apparentTemperature?.roundToInt() ?: 0}°C",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SevereAlertsSection(alerts: List<WeatherAlert>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        alerts.forEach { alert ->
            val (badgeColor, borderCol) = when (alert.level) {
                AlertLevel.RED -> AlertRed to AlertRed
                AlertLevel.ORANGE -> AlertOrange to AlertOrange
                AlertLevel.YELLOW -> AlertYellow to AlertYellow
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, borderCol, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = badgeColor.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Aviso meteorolóxico",
                        tint = badgeColor,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = alert.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = badgeColor,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = alert.source.badgeLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                    color = badgeColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = alert.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Recomendación: ${alert.instruction}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EssentialMetricsSection(data: FullWeatherData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Métricas esenciais do tempo",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))

        // 2x2 or 2x3 Grid of parameters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Humidade",
                value = "${data.current.relativeHumidity?.roundToInt() ?: 0}%",
                subtitle = "Condensación atlántica",
                icon = Icons.Default.WaterDrop,
                iconColor = OceanSkyBlue,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Velocidade do vento",
                value = "${data.current.windSpeed?.roundToInt() ?: 0} km/h",
                subtitle = "Rachas: ${data.current.windGusts?.roundToInt() ?: 0} km/h",
                icon = Icons.Default.Air,
                iconColor = AtlanticTeal,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Presión superficial",
                value = "${data.current.surfacePressure?.roundToInt() ?: 1013} hPa",
                subtitle = if ((data.current.surfacePressure ?: 1013.0) < 1010.0) "Baixa (Borrasca)" else "Estable (Anticiclón)",
                icon = Icons.Default.WbSunny,
                iconColor = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Precipitación",
                value = "${data.current.precipitation ?: 0.0} mm",
                subtitle = "Chuvia actual",
                icon = Icons.Default.WaterDrop,
                iconColor = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

@Composable
fun DayForecastCard(forecast: DayForecast) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Date & Condition
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = forecast.iconEmoji,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = forecast.date,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val isMg = forecast.source == WeatherDataSource.METEOGALICIA
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isMg) Color(0xFF0D9488).copy(alpha = 0.15f) else Color(0xFF4F46E5).copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (isMg) "MeteoGalicia" else "ECMWF",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = if (isMg) Color(0xFF0D9488) else Color(0xFF4F46E5)
                            )
                        }
                    }
                    Text(
                        text = forecast.conditionDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // Rain & Temp
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (forecast.precipitationProbability > 0) {
                    Text(
                        text = "💧 ${forecast.precipitationProbability}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = OceanSkyBlue,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
                Text(
                    text = "${forecast.tempMax.roundToInt()}° / ${forecast.tempMin.roundToInt()}°",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun AirQualitySection(
    aqiState: AirQualityUiState,
    locationName: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        when (aqiState) {
            is AirQualityUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AtlanticTeal)
                }
            }
            is AirQualityUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AlertRed.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Información de calidade do aire non dispoñible", color = AlertRed, fontWeight = FontWeight.Bold)
                        Text(aqiState.message, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onRetry) { Text("Tentar de novo") }
                    }
                }
            }
            is AirQualityUiState.Success -> {
                val data = aqiState.data
                val aqiColor = Color(data.aqiColorHex)

                // Main AQI Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Índice europeo de calidade do aire",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = data.aqiCategory,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = aqiColor
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(aqiColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${data.aqi?.roundToInt() ?: "--"}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = aqiColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { ((data.aqi ?: 0.0) / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = aqiColor,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "A brisa atlántica en $locationName axuda a dispersar os contaminantes atmosféricos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Desagregación dos principais contaminantes",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Pollutant details
                PollutantRow("Partículas finas en suspensión (PM2.5)", "${data.pm25 ?: "--"} µg/m³", (data.pm25 ?: 0.0) / 50.0)
                PollutantRow("Partículas grosas en suspensión (PM10)", "${data.pm10 ?: "--"} µg/m³", (data.pm10 ?: 0.0) / 100.0)
                PollutantRow("Dióxido de nitróxeno (NO₂)", "${data.no2 ?: "--"} µg/m³", (data.no2 ?: 0.0) / 200.0)
                PollutantRow("Ozono (O₃)", "${data.o3 ?: "--"} µg/m³", (data.o3 ?: 0.0) / 180.0)
                PollutantRow("Dióxido de xofre (SO₂)", "${data.so2 ?: "--"} µg/m³", (data.so2 ?: 0.0) / 350.0)
                PollutantRow("Monóxido de carbono (CO)", "${data.co ?: "--"} µg/m³", (data.co ?: 0.0) / 10000.0)
            }
        }
    }
}

@Composable
fun PollutantRow(name: String, concentration: String, ratio: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(concentration, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { ratio.toFloat().coerceIn(0.05f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (ratio > 0.7) AlertOrange else AtlanticTeal,
                trackColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
fun HistoricalWeatherSection(
    historicalState: HistoricalUiState,
    selectedDate: String,
    locationName: String,
    onQueryDate: (String) -> Unit
) {
    var inputDate by remember(selectedDate) { mutableStateOf(selectedDate) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Arquivo meteorolóxico histórico de Galicia",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Seleccione calquera data pasada para consultar os rexistros meteorolóxicos en $locationName.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = inputDate,
                    onValueChange = { inputDate = it },
                    label = { Text("Data (AAAA-MM-DD)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("historical_date_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick presets
                Text(
                    text = "Accesos rápidos:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        "Hai 1 ano" to "2025-09-21",
                        "Hai 2 anos" to "2024-09-21",
                        "Solsticio de inverno" to "2024-12-21"
                    )
                    presets.forEach { (label, date) ->
                        Button(
                            onClick = {
                                inputDate = date
                                onQueryDate(date)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onQueryDate(inputDate) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("historical_search_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanSkyBlue)
                ) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Consultar arquivo histórico")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (historicalState) {
            is HistoricalUiState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Introduza unha data e prema en consultar para ver os rexistros.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is HistoricalUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OceanSkyBlue)
                }
            }
            is HistoricalUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AlertRed.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Erro na consulta do arquivo", color = AlertRed, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(historicalState.message, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            is HistoricalUiState.Success -> {
                val data = historicalState.data
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Rexistro histórico do ${data.date}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "$locationName, Galicia",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(text = data.iconEmoji, fontSize = 36.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Temperaturas", style = MaterialTheme.typography.labelSmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${data.tempMax?.roundToInt() ?: "--"}°C / ${data.tempMin?.roundToInt() ?: "--"}°C",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text("Máx. / Mín.", style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Precipitación", style = MaterialTheme.typography.labelSmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${data.precipitationSum ?: 0.0} mm",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = OceanSkyBlue
                                    )
                                    Text("Chuvia acumulada", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Vento atlántico máximo", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "${data.maxWindSpeed?.roundToInt() ?: "--"} km/h",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = AtlanticTeal
                                    )
                                }
                                Text(
                                    text = data.conditionDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UvIndexCircularCard(
    uvIndex: Double,
    modifier: Modifier = Modifier
) {
    val safetyLevel = remember(uvIndex) { UvIndexUtils.getSafetyLevel(uvIndex) }
    val levelColor = remember(safetyLevel) { Color(safetyLevel.hexColor) }
    val normalizedProgress = remember(uvIndex) { (uvIndex.toFloat() / 11f).coerceIn(0f, 1f) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("uv_index_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Índice UV",
                        tint = levelColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Índice UV actual",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Safety badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(levelColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("uv_safety_badge")
                ) {
                    Text(
                        text = safetyLevel.label,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = levelColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Progress Indicator with centered UV number
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(88.dp)
                        .testTag("uv_circular_indicator")
                ) {
                    CircularProgressIndicator(
                        progress = normalizedProgress,
                        modifier = Modifier.fillMaxSize(),
                        color = levelColor,
                        trackColor = levelColor.copy(alpha = 0.15f),
                        strokeWidth = 8.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f", uvIndex),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("uv_value_text")
                        )
                        Text(
                            text = "Índice",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Safety guidelines and context
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = safetyLevel.advice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Escala: 0 (Baixo) ata 11+ (Extremo)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TodayHourlyForecastSection(
    hourlyList: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    val remainingHours = remember(hourlyList) {
        HourlyForecastUtils.filterRemainingHoursToday(hourlyList)
    }

    if (remainingHours.isEmpty()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("today_hourly_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Predición para hoxe",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val firstSource = hourlyList.firstOrNull()?.source ?: WeatherDataSource.METEOGALICIA
                    val isMg = firstSource == WeatherDataSource.METEOGALICIA
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isMg) Color(0xFF0D9488).copy(alpha = 0.15f) else Color(0xFF4F46E5).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isMg) "MeteoGalicia 0-48h" else "Open-Meteo",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = if (isMg) Color(0xFF0D9488) else Color(0xFF4F46E5)
                        )
                    }
                }
                Text(
                    text = "Horas restantes de hoxe",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hourly_forecast_row")
            ) {
                items(remainingHours) { hourly ->
                    HourlyForecastItem(hourly = hourly)
                }
            }
        }
    }
}

@Composable
fun HourlyForecastItem(
    hourly: HourlyForecast,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("hourly_item_${hourly.time}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = hourly.time,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = hourly.iconEmoji,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${hourly.temperature.roundToInt()}°",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (hourly.precipitationProbability > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💧${hourly.precipitationProbability}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = OceanSkyBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Text(
                    text = "-",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

package com.example.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
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
import com.example.data.repository.MultiYearHistoricalSummary
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertYellow
import com.example.ui.theme.AtlanticTeal
import com.example.ui.theme.OceanSkyBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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
    val favorites by viewModel.favorites.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()
    val aqiState by viewModel.airQualityState.collectAsState()
    val historicalState by viewModel.historicalState.collectAsState()
    val currentTab by viewModel.selectedTab.collectAsState()
    val historicalDate by viewModel.historicalDate.collectAsState()
    val appPage by viewModel.appPage.collectAsState()

    var showLocationSearchSheet by remember { mutableStateOf(false) }
    var showWidgetThemeDialog by remember { mutableStateOf(false) }

    if (showLocationSearchSheet) {
        LocationSearchBottomSheet(
            selectedLocation = selectedLocation,
            defaultLocation = defaultLocation,
            favorites = favorites,
            onLocationSelected = { viewModel.selectLocation(it) },
            onSetDefault = { viewModel.setDefaultLocation(it) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onDismissRequest = { showLocationSearchSheet = false }
        )
    }

    if (showWidgetThemeDialog) {
        WidgetThemeDialog(onDismiss = { showWidgetThemeDialog = false })
    }

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
                            text = if (appPage == AppPage.ACTUAL) "Tempo en Galicia" else "Arquivo Histórico",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (appPage == AppPage.ACTUAL) "MeteoGalicia e as Rías" else "Rexistros meteorolóxicos de Galicia",
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
                    IconButton(
                        onClick = { showWidgetThemeDialog = true },
                        modifier = Modifier.testTag("btn_widget_theme")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Personalizar aspecto do widget"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = appPage == AppPage.ACTUAL,
                    onClick = { viewModel.selectAppPage(AppPage.ACTUAL) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Tempo actual"
                        )
                    },
                    label = { Text("Actual") },
                    modifier = Modifier.testTag("nav_actual")
                )
                NavigationBarItem(
                    selected = appPage == AppPage.HISTORICO,
                    onClick = { viewModel.selectAppPage(AppPage.HISTORICO) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Arquivo histórico"
                        )
                    },
                    label = { Text("Histórico") },
                    modifier = Modifier.testTag("nav_historico")
                )
            }
        }
    ) { paddingValues ->
        when (appPage) {
            AppPage.ACTUAL -> {
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
                            favorites = favorites,
                            onLocationSelected = { viewModel.selectLocation(it) },
                            onSetDefault = { viewModel.setDefaultLocation(it) },
                            onOpenSearchSheet = { showLocationSearchSheet = true }
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
                        }
                    }

                    // 3. Section Tabs (7-Day Forecast, Air Quality AQI)
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
                                            text = if (currentSuccess.data.location.isGalicia) "Días 1-2 MeteoGalicia • Días 3-7 ECMWF" else "Modelo ECMWF IFS (Global)",
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
                    }
                }
            }
            AppPage.HISTORICO -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // 1. Location Selector (defaults to same location, allows searching other concellos)
                    item {
                        LocationSelectorSection(
                            selectedLocation = selectedLocation,
                            defaultLocation = defaultLocation,
                            favorites = favorites,
                            onLocationSelected = { viewModel.selectLocation(it) },
                            onSetDefault = { viewModel.setDefaultLocation(it) },
                            onOpenSearchSheet = { showLocationSearchSheet = true }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 2. Historical archive search and records
                    item {
                        HistoricalWeatherSection(
                            historicalState = historicalState,
                            selectedDate = historicalDate,
                            locationName = selectedLocation.name,
                            onQueryDate = { viewModel.queryHistoricalDate(it) },
                            onQueryMultiYear = { date, years -> viewModel.queryMultiYearHistorical(date, years) }
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
    favorites: List<GaliciaLocation>,
    onLocationSelected: (GaliciaLocation) -> Unit,
    onSetDefault: (GaliciaLocation) -> Unit,
    onOpenSearchSheet: () -> Unit
) {
    val isDefault = !selectedLocation.isGps && selectedLocation.name == defaultLocation.name

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Location header with dropdown arrow to search and change location
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onOpenSearchSheet)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("btn_open_location_search"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(OceanSkyBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = OceanSkyBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (selectedLocation.isGps) "A túa posición (GPS)" else selectedLocation.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isDefault) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Predeterminada",
                                tint = Color(0xFFEAB308),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Text(
                        text = if (selectedLocation.isGps) "Galicia • Localización detectada" else "${selectedLocation.province} • Toca para buscar outros concellos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar concello",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Despregar lista de concellos",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal row of up to 6 favorites
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            favorites.take(6).forEach { location ->
                val isSelected = !selectedLocation.isGps && selectedLocation.name == location.name
                val isLocDefault = location.name == defaultLocation.name
                val containerColor = if (isSelected) OceanSkyBlue else MaterialTheme.colorScheme.surfaceVariant
                val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(containerColor)
                        .clickable { onLocationSelected(location) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("location_chip_${location.name.replace(" ", "_")}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isLocDefault) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Localización predeterminada",
                                tint = if (isSelected) Color(0xFFFEF08A) else Color(0xFFEAB308),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = location.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor
                        )
                    }
                }
            }

            // Quick "+ Outro" chip that opens search sheet
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .clickable(onClick = onOpenSearchSheet)
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("btn_more_locations_chip")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Buscar outro concello",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Máis...",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
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

    val badgeLabel = when {
        !data.location.isGalicia -> "🌍 Open-Meteo (ECMWF Global)"
        data.isFallback -> "📡 Open-Meteo (Alternativo)"
        else -> "🛰️ MeteoGalicia (0-48h WRF)"
    }

    val uvVal = data.current.uvIndex ?: 0.0
    val uvSafety = remember(uvVal) { UvIndexUtils.getSafetyLevel(uvVal) }
    val tempText = data.current.temperature?.let { "${it.roundToInt()}°" } ?: "--°"
    val apparentTempStr = data.current.apparentTemperature?.let { "${it.roundToInt()}°" } ?: "--°"

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
            // Dynamic animation overlay across entire card
            WeatherAnimationOverlay(
                visualType = visualType,
                modifier = Modifier.matchParentSize()
            )

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
                                color = Color.White
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
                                "${data.location.province} • En tempo real"
                            } else {
                                "${data.location.province} · ${data.location.country} • En tempo real"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        // Default location selector control
                        if (!data.location.isGps) {
                            Spacer(modifier = Modifier.height(6.dp))
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
                        color = Color.White
                    )

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = data.conditionDescription,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
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
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            // UV Index indicator with testTag
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
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.25f),
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
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = windDisplay,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color = Color.White.copy(alpha = 0.25f)
                    )

                    // Metric 2: Humidade
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Humidade",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = humidityStr,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color = Color.White.copy(alpha = 0.25f)
                    )

                    // Metric 3: Precipitación
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Precipitación",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = precipStr,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
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
                        color = Color.White.copy(alpha = 0.75f)
                    )
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
fun HeroMetricCell(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricalWeatherSection(
    historicalState: HistoricalUiState,
    selectedDate: String,
    locationName: String,
    onQueryDate: (String) -> Unit,
    onQueryMultiYear: (String, Int) -> Unit
) {
    var inputDate by remember(selectedDate) { mutableStateOf(selectedDate) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Only allow past dates up to today/yesterday (archive API doesn't have future dates)
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            inputDate = formatter.format(Date(millis))
                        }
                        showDatePickerDialog = false
                    },
                    modifier = Modifier.testTag("date_picker_confirm")
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePickerDialog = false },
                    modifier = Modifier.testTag("date_picker_cancel")
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

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
                    text = "Consulte unha data concreta ou as medias históricas para $locationName.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = inputDate,
                    onValueChange = { inputDate = it },
                    label = { Text("Data de referencia (AAAA-MM-DD)") },
                    trailingIcon = {
                        IconButton(
                            onClick = { showDatePickerDialog = true },
                            modifier = Modifier.testTag("historical_calendar_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Abrir calendario para escoller data",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("historical_date_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Multi-year averages quick presets
                Text(
                    text = "Calcular medias para esta data:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val multiYearPresets = listOf(
                        "Últimos 2 anos" to 2,
                        "Últimos 5 anos" to 5,
                        "Últimos 10 anos" to 10
                    )
                    multiYearPresets.forEach { (label, years) ->
                        Button(
                            onClick = {
                                onQueryMultiYear(inputDate, years)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_years_$years"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Single date query button
                OutlinedButton(
                    onClick = { onQueryDate(inputDate) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("historical_search_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Consultar esta data concreta")
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
                        text = "Seleccione un dos accesos rápidos ou introduza unha data para consultar.",
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
            is HistoricalUiState.MultiYearSuccess -> {
                MultiYearHistoricalCard(
                    summary = historicalState.summary,
                    locationName = locationName
                )
            }
        }
    }
}

@Composable
fun MultiYearHistoricalCard(
    summary: MultiYearHistoricalSummary,
    locationName: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("multi_year_historical_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // 1. Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Medias dos últimos ${summary.yearsCount} anos",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Día ${summary.targetMonthDay} en $locationName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val oldestYr = summary.records.lastOrNull()?.date?.take(4) ?: ""
                val newestYr = summary.records.firstOrNull()?.date?.take(4) ?: ""
                if (oldestYr.isNotEmpty() && newestYr.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "$oldestYr-$newestYr",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. 2x2 Grid of Essential Averages
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🌡️ Máx. media", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = summary.avgTempMax?.let { String.format(Locale.US, "%.1f°C", it) } ?: "--",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text("Temperatura diúrna", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("❄️ Mín. media", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = summary.avgTempMin?.let { String.format(Locale.US, "%.1f°C", it) } ?: "--",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text("Temperatura nocturna", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🌧️ Chuvia media", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = summary.avgPrecipitation?.let { String.format(Locale.US, "%.1f mm", it) } ?: "--",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = OceanSkyBlue
                        )
                        Text("Acumulación diaria", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("💨 Vento medio", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = summary.avgWindSpeed?.let { String.format(Locale.US, "%.1f km/h", it) } ?: "--",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AtlanticTeal
                        )
                        Text("Rachas máximas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Frecuencia e Probabilidade de Choiva
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = OceanSkyBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Probabilidade de choiva histórica",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        Text(
                            text = "${summary.rainProbabilityPercent}%",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = OceanSkyBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (summary.rainProbabilityPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = OceanSkyBlue,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Choveu en ${summary.rainDaysCount} dos ${summary.yearsCount} anos nesta data en $locationName.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Rexistros Extremos do Período
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Rexistros extremos nesta data",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        summary.hottestYear?.let { (yr, temp) ->
                            Column(modifier = Modifier.weight(1f)) {
                                Text("🔥 Máis cálido", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "$yr (${String.format(Locale.US, "%.1f°C", temp)})",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        summary.coldestYear?.let { (yr, temp) ->
                            Column(modifier = Modifier.weight(1f)) {
                                Text("🧊 Máis frío", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "$yr (${String.format(Locale.US, "%.1f°C", temp)})",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        summary.rainiestYear?.let { (yr, p) ->
                            Column(modifier = Modifier.weight(1f)) {
                                Text("🌧️ Máis chuvia", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "$yr (${String.format(Locale.US, "%.1f mm", p)})",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Desglose ano a ano
            Text(
                text = "Desglose ano a ano:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            summary.records.forEach { rec ->
                val year = rec.date.take(4)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = year,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.width(50.dp)
                        )
                        Text(text = rec.iconEmoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${rec.tempMax?.roundToInt() ?: "--"}° / ${rec.tempMin?.roundToInt() ?: "--"}°",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = rec.precipitationSum?.let { String.format(Locale.US, "%.1f mm", it) } ?: "--",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if ((rec.precipitationSum ?: 0.0) > 0.1) OceanSkyBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${rec.maxWindSpeed?.roundToInt() ?: "--"} km/h",
                            style = MaterialTheme.typography.bodySmall,
                            color = AtlanticTeal
                        )
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

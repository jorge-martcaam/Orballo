package com.example.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertRed
import com.example.ui.theme.OceanSkyBlue
import com.example.ui.weather.AirQualitySection
import com.example.ui.weather.DayForecastCard
import com.example.ui.weather.HistoricalWeatherSection
import com.example.ui.weather.LocationSelectorSection
import com.example.ui.weather.SevereAlertsSection
import com.example.ui.weather.TodayHourlyForecastSection
import com.example.ui.weather.WeatherHeroCard
import com.example.ui.weather.WeatherTopAppBar
import com.example.ui.weather.WeatherUiFormatters

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val defaultLocation by viewModel.defaultLocation.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()
    val aqiState by viewModel.airQualityState.collectAsState()
    val historicalState by viewModel.historicalState.collectAsState()
    val currentTab by viewModel.selectedTab.collectAsState()
    val historicalDate by viewModel.historicalDate.collectAsState()
    val appPage by viewModel.appPage.collectAsState()

    BackHandler(enabled = appPage != AppPage.ACTUAL) {
        viewModel.selectAppPage(AppPage.ACTUAL)
    }

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WeatherTopAppBar(
                appPage = appPage,
                onRefresh = { viewModel.refresh() }
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
                            contentDescription = "Predición"
                        )
                    },
                    label = { Text("Predición") },
                    modifier = Modifier.testTag("nav_actual")
                )
                NavigationBarItem(
                    selected = appPage == AppPage.HISTORICO,
                    onClick = { viewModel.selectAppPage(AppPage.HISTORICO) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Histórico"
                        )
                    },
                    label = { Text("Histórico") },
                    modifier = Modifier.testTag("nav_historico")
                )
                NavigationBarItem(
                    selected = appPage == AppPage.AXUSTES,
                    onClick = { viewModel.selectAppPage(AppPage.AXUSTES) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Axustes"
                        )
                    },
                    label = { Text("Axustes") },
                    modifier = Modifier.testTag("nav_axustes")
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
                            if (state.data.isFromCache) {
                                item {
                                    val timeStr = remember(state.data.cachedAtEpochMs) {
                                        WeatherUiFormatters.formatEpochToTime(state.data.cachedAtEpochMs)
                                    }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("💾", fontSize = 14.sp)
                                            Text(
                                                text = "Modo sen conexión • Predición gardada ás $timeStr",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                WeatherHeroCard(
                                    data = state.data,
                                    defaultLocation = defaultLocation,
                                    onSetDefault = { viewModel.setDefaultLocation(it) }
                                )
                            }

                            if (state.data.alerts.isNotEmpty()) {
                                item {
                                    SevereAlertsSection(alerts = state.data.alerts)
                                }
                            }

                            if (state.data.todayHourlyForecast.isNotEmpty()) {
                                item {
                                    TodayHourlyForecastSection(
                                        hourlyList = state.data.todayHourlyForecast,
                                        timezone = state.data.timezone
                                    )
                                }
                            }
                        }
                    }

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
            AppPage.AXUSTES -> {
                SettingsTabScreen(
                    paddingValues = paddingValues,
                    viewModel = viewModel,
                    onOpenWidgetManager = { showWidgetThemeDialog = true }
                )
            }
        }
    }
}

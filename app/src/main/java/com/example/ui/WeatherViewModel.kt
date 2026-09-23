package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.location.LocationHelper
import com.example.data.location.LocationPreferences
import com.example.data.model.GaliciaLocation
import com.example.data.repository.AirQualityData
import com.example.data.repository.FullWeatherData
import com.example.data.repository.HistoricalDayData
import com.example.data.repository.WeatherRepository
import com.example.widget.GaliciaWeatherWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val data: FullWeatherData) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

sealed interface AirQualityUiState {
    data object Loading : AirQualityUiState
    data class Success(val data: AirQualityData) : AirQualityUiState
    data class Error(val message: String) : AirQualityUiState
}

sealed interface HistoricalUiState {
    data object Idle : HistoricalUiState
    data object Loading : HistoricalUiState
    data class Success(val data: HistoricalDayData) : HistoricalUiState
    data class Error(val message: String) : HistoricalUiState
}

enum class WeatherTab {
    FORECAST,
    AIR_QUALITY,
    HISTORICAL
}

class WeatherViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: WeatherRepository = WeatherRepository()
) : AndroidViewModel(application) {

    private val locationPreferences = LocationPreferences(application)

    private val _defaultLocation = MutableStateFlow(locationPreferences.getDefaultLocation())
    val defaultLocation: StateFlow<GaliciaLocation> = _defaultLocation.asStateFlow()

    private val _selectedLocation = MutableStateFlow(locationPreferences.getDefaultLocation())
    val selectedLocation: StateFlow<GaliciaLocation> = _selectedLocation.asStateFlow()

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    private val _airQualityState = MutableStateFlow<AirQualityUiState>(AirQualityUiState.Loading)
    val airQualityState: StateFlow<AirQualityUiState> = _airQualityState.asStateFlow()

    private val _historicalState = MutableStateFlow<HistoricalUiState>(HistoricalUiState.Idle)
    val historicalState: StateFlow<HistoricalUiState> = _historicalState.asStateFlow()

    private val _selectedTab = MutableStateFlow(WeatherTab.FORECAST)
    val selectedTab: StateFlow<WeatherTab> = _selectedTab.asStateFlow()

    // Default sample past date (1 year ago)
    private val defaultPastDate: String = run {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -1)
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    private val _historicalDate = MutableStateFlow(defaultPastDate)
    val historicalDate: StateFlow<String> = _historicalDate.asStateFlow()

    init {
        loadDataForLocation(_selectedLocation.value)
    }

    fun selectTab(tab: WeatherTab) {
        _selectedTab.value = tab
        if (tab == WeatherTab.HISTORICAL && _historicalState.value is HistoricalUiState.Idle) {
            queryHistoricalDate(_historicalDate.value)
        }
    }

    fun selectLocation(location: GaliciaLocation) {
        _selectedLocation.value = location
        loadDataForLocation(location)
    }

    fun setDefaultLocation(location: GaliciaLocation) {
        if (!location.isGps) {
            locationPreferences.setDefaultLocation(location)
            _defaultLocation.value = location
            try {
                val intent = Intent(getApplication(), GaliciaWeatherWidgetProvider::class.java).apply {
                    action = GaliciaWeatherWidgetProvider.ACTION_WIDGET_REFRESH
                }
                getApplication<Application>().sendBroadcast(intent)
            } catch (_: Exception) {}
        }
    }

    fun tryGpsLocation(locationHelper: LocationHelper) {
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading
            val gpsLocation = locationHelper.getCurrentLocation()
            if (gpsLocation != null) {
                _selectedLocation.value = gpsLocation
                loadDataForLocation(gpsLocation)
            } else {
                // Keep current and reload
                loadDataForLocation(_selectedLocation.value)
            }
        }
    }

    fun refresh() {
        loadDataForLocation(_selectedLocation.value)
        if (_selectedTab.value == WeatherTab.HISTORICAL) {
            queryHistoricalDate(_historicalDate.value)
        }
    }

    private fun loadDataForLocation(location: GaliciaLocation) {
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading
            _airQualityState.value = AirQualityUiState.Loading

            try {
                val weather = repository.fetchWeather(location)
                _weatherState.value = WeatherUiState.Success(weather)
            } catch (e: Exception) {
                _weatherState.value = WeatherUiState.Error(
                    e.localizedMessage ?: "Erro ao obter os datos meteorolóxicos en tempo real"
                )
            }

            try {
                val aqi = repository.fetchAirQuality(location)
                _airQualityState.value = AirQualityUiState.Success(aqi)
            } catch (e: Exception) {
                _airQualityState.value = AirQualityUiState.Error(
                    e.localizedMessage ?: "Erro ao cargar o índice de calidade do aire"
                )
            }
        }
    }

    fun queryHistoricalDate(dateString: String) {
        _historicalDate.value = dateString
        viewModelScope.launch {
            _historicalState.value = HistoricalUiState.Loading
            try {
                val hist = repository.fetchHistoricalWeather(_selectedLocation.value, dateString)
                _historicalState.value = HistoricalUiState.Success(hist)
            } catch (e: Exception) {
                _historicalState.value = HistoricalUiState.Error(
                    e.localizedMessage ?: "Non se puideron obter datos históricos para $dateString"
                )
            }
        }
    }
}

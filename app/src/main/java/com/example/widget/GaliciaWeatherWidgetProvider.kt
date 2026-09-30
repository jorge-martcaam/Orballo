package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.model.GaliciaLocation
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class GaliciaWeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WIDGET_REFRESH) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, GaliciaWeatherWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (id in allWidgetIds) {
                // Show refreshing indicator
                showLoadingState(context, appWidgetManager, id)
                updateWidget(context, appWidgetManager, id)
            }
        } else if (intent.action == ACTION_WIDGET_NEXT_LOCATION || intent.action == ACTION_WIDGET_PREV_LOCATION) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val widgetPrefs = com.example.data.widget.WidgetPreferences(context.applicationContext)
                val locationPrefs = com.example.data.location.LocationPreferences(context.applicationContext)
                val favorites = locationPrefs.getFavorites()
                if (favorites.isNotEmpty()) {
                    val currentIndex = widgetPrefs.getWidgetLocationIndex(widgetId)
                    val newIndex = if (intent.action == ACTION_WIDGET_PREV_LOCATION) {
                        (currentIndex - 1 + favorites.size) % favorites.size
                    } else {
                        (currentIndex + 1) % favorites.size
                    }
                    widgetPrefs.setWidgetLocationIndex(widgetId, newIndex)
                }
                showLoadingState(context, appWidgetManager, widgetId)
                updateWidget(context, appWidgetManager, widgetId)
            }
        }
    }

    private fun getLayoutForWidget(
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ): Int {
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

        return when {
            minHeight in 1..99 && minWidth >= 240 -> R.layout.widget_weather_layout_wide
            minHeight in 1..99 || (minWidth in 1..199) -> R.layout.widget_weather_layout_compact
            else -> R.layout.widget_weather_layout
        }
    }

    private fun showLoadingState(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val layoutId = getLayoutForWidget(appWidgetManager, appWidgetId)
        val views = RemoteViews(context.packageName, layoutId)
        val widgetPrefs = com.example.data.widget.WidgetPreferences(context.applicationContext)
        val preset = widgetPrefs.getPreset()
        views.setInt(R.id.widget_root, "setBackgroundResource", preset.backgroundRes)
        views.setInt(R.id.widget_btn_refresh, "setBackgroundResource", preset.buttonBackgroundRes)
        views.setInt(R.id.widget_btn_refresh, "setColorFilter", preset.primaryTextColor)
        views.setInt(R.id.widget_btn_prev_location, "setBackgroundResource", preset.buttonBackgroundRes)
        views.setInt(R.id.widget_btn_prev_location, "setColorFilter", preset.primaryTextColor)
        views.setInt(R.id.widget_btn_next_location, "setBackgroundResource", preset.buttonBackgroundRes)
        views.setInt(R.id.widget_btn_next_location, "setColorFilter", preset.primaryTextColor)
        views.setTextColor(R.id.widget_updated, preset.secondaryTextColor)
        views.setTextViewText(R.id.widget_updated, "Actualizando...")
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val layoutId = getLayoutForWidget(appWidgetManager, appWidgetId)
        val views = RemoteViews(context.packageName, layoutId)

        val widgetPrefs = com.example.data.widget.WidgetPreferences(context.applicationContext)
        val preset = widgetPrefs.getPreset()
        views.setInt(R.id.widget_root, "setBackgroundResource", preset.backgroundRes)
        views.setInt(R.id.widget_btn_refresh, "setBackgroundResource", preset.buttonBackgroundRes)
        views.setInt(R.id.widget_btn_refresh, "setColorFilter", preset.primaryTextColor)
        views.setInt(R.id.widget_btn_prev_location, "setBackgroundResource", preset.buttonBackgroundRes)
        views.setInt(R.id.widget_btn_prev_location, "setColorFilter", preset.primaryTextColor)
        views.setInt(R.id.widget_btn_next_location, "setBackgroundResource", preset.buttonBackgroundRes)
        views.setInt(R.id.widget_btn_next_location, "setColorFilter", preset.primaryTextColor)

        val locationPrefs = com.example.data.location.LocationPreferences(context.applicationContext)
        val favorites = locationPrefs.getFavorites()
        if (favorites.size <= 1) {
            views.setViewVisibility(R.id.widget_location_arrows_container, View.GONE)
        } else {
            views.setViewVisibility(R.id.widget_location_arrows_container, View.VISIBLE)

            val prevIntent = Intent(context, GaliciaWeatherWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_PREV_LOCATION
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val prevPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId * 100 + 1,
                prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_prev_location, prevPendingIntent)

            val nextIntent = Intent(context, GaliciaWeatherWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_NEXT_LOCATION
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val nextPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId * 100 + 2,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_next_location, nextPendingIntent)
        }

        views.setTextColor(R.id.widget_location, preset.primaryTextColor)
        views.setTextColor(R.id.widget_updated, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_temperature, preset.primaryTextColor)
        views.setTextColor(R.id.widget_condition, preset.primaryTextColor)
        views.setTextColor(R.id.widget_feels_like, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_uv, preset.secondaryTextColor)

        views.setTextColor(R.id.widget_wind, preset.primaryTextColor)
        views.setTextColor(R.id.widget_humidity, preset.primaryTextColor)
        views.setTextColor(R.id.widget_precip, preset.primaryTextColor)
        views.setTextColor(R.id.widget_label_wind, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_label_humidity, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_label_precip, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_periods_compact_text, preset.primaryTextColor)
        views.setTextColor(R.id.widget_cperiod1_label, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_cperiod1_temp, preset.primaryTextColor)
        views.setTextColor(R.id.widget_cperiod1_rain, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_cperiod1_wind, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_cperiod2_label, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_cperiod2_temp, preset.primaryTextColor)
        views.setTextColor(R.id.widget_cperiod2_rain, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_cperiod2_wind, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_cperiod3_label, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_cperiod3_temp, preset.primaryTextColor)
        views.setTextColor(R.id.widget_cperiod3_rain, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_cperiod3_wind, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_period1_wind, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_period2_wind, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_period3_wind, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_air_quality_label, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_air_quality_value, preset.primaryTextColor)
        views.setTextColor(R.id.widget_historical_title, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_historical_temp, preset.primaryTextColor)
        views.setTextColor(R.id.widget_historical_temp_label, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_historical_condition, preset.primaryTextColor)
        views.setTextColor(R.id.widget_historical_range, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_historical_wind, preset.secondaryTextColor)
        views.setTextColor(R.id.widget_historical_rain, preset.secondaryTextColor)

        // Setup manual refresh pending intent
        val refreshIntent = Intent(context, GaliciaWeatherWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_REFRESH
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

        // Setup launch MainActivity intent
        val launchIntent = Intent(context, MainActivity::class.java)
        val launchPendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, launchPendingIntent)

        // Fetch weather asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val targetLocation = if (favorites.isNotEmpty()) {
                    val currentIndex = widgetPrefs.getWidgetLocationIndex(appWidgetId)
                    val safeIndex = currentIndex.coerceIn(0, favorites.size - 1)
                    favorites[safeIndex]
                } else {
                    locationPrefs.getDefaultLocation()
                }

                val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
                val isMaxSize = (layoutId == R.layout.widget_weather_layout && minHeight >= 200)

                val repository = WeatherRepository()

                val weatherDeferred = async { repository.fetchWeather(targetLocation) }
                val airQualityDeferred = if (isMaxSize) async {
                    try {
                        repository.fetchAirQuality(targetLocation)
                    } catch (_: Exception) {
                        null
                    }
                } else null

                val pastDate = java.time.LocalDate.now().minusYears(1).toString()
                val pastYear = java.time.LocalDate.now().minusYears(1).year
                val historicalDeferred = if (isMaxSize) async {
                    try {
                        repository.fetchHistoricalWeather(targetLocation, pastDate)
                    } catch (_: Exception) {
                        null
                    }
                } else null

                val weatherData = weatherDeferred.await()

                val temp = weatherData.current.temperature?.roundToInt() ?: 0
                val apparentTemp = weatherData.current.apparentTemperature?.roundToInt() ?: temp
                val humidity = weatherData.current.relativeHumidity?.roundToInt() ?: 0
                val wind = weatherData.current.windSpeed?.roundToInt() ?: 0
                val precipVal = weatherData.current.precipitation
                val precipStr = precipVal?.let { String.format(Locale.US, "%.1f mm", it) } ?: "0.0 mm"
                val timeStr = com.example.data.model.WeatherTimeUtils.formatLocationTime(weatherData.timezone)

                views.setTextViewText(R.id.widget_location, targetLocation.name)
                views.setTextViewText(R.id.widget_temperature, "$temp°")
                views.setTextViewText(R.id.widget_condition, weatherData.conditionDescription)
                views.setTextViewText(R.id.widget_feels_like, "Sens. $apparentTemp°C")
                views.setTextViewText(R.id.widget_emoji, weatherData.iconEmoji)
                views.setTextViewText(R.id.widget_wind, "$wind km/h")
                views.setTextViewText(R.id.widget_humidity, "$humidity%")
                views.setTextViewText(R.id.widget_precip, precipStr)
                if (layoutId == R.layout.widget_weather_layout_wide) {
                    views.setTextViewText(R.id.widget_updated, timeStr)
                } else {
                    views.setTextViewText(R.id.widget_updated, "Actualizado ás $timeStr")
                }

                // Bind UV index indicator (visible only when uvIndex > 0)
                val uvVal = weatherData.current.uvIndex
                if (uvVal != null && uvVal > 0.0) {
                    val uvInt = uvVal.roundToInt()
                    views.setViewVisibility(R.id.widget_uv, View.VISIBLE)
                    val uvText = "☀️ UV $uvInt"
                    views.setTextViewText(R.id.widget_uv, uvText)
                } else {
                    views.setViewVisibility(R.id.widget_uv, View.GONE)
                }

                if (weatherData.alerts.isNotEmpty()) {
                    val topAlert = weatherData.alerts.first()
                    views.setViewVisibility(R.id.widget_alert, View.VISIBLE)
                    views.setTextViewText(R.id.widget_alert, "⚠️ ${topAlert.title}")
                } else {
                    views.setViewVisibility(R.id.widget_alert, View.GONE)
                }

                // Check widget options for height to show periods or max extra details
                if (layoutId == R.layout.widget_weather_layout_compact) {
                    if (minHeight in 1..75) {
                        views.setViewVisibility(R.id.widget_compact_metrics, View.GONE)
                    } else {
                        views.setViewVisibility(R.id.widget_compact_metrics, View.VISIBLE)
                    }

                    if (minHeight >= 115) {
                        val periods = getRelevantPeriods(weatherData)
                        if (periods.size >= 3) {
                            views.setTextViewText(R.id.widget_cperiod1_label, periods[0].label)
                            views.setTextViewText(R.id.widget_cperiod1_temp, "${periods[0].iconEmoji} ${periods[0].tempStr}")
                            views.setTextViewText(R.id.widget_cperiod1_rain, "💧 ${periods[0].rainStr}")
                            views.setTextViewText(R.id.widget_cperiod1_wind, periods[0].windStr)

                            views.setTextViewText(R.id.widget_cperiod2_label, periods[1].label)
                            views.setTextViewText(R.id.widget_cperiod2_temp, "${periods[1].iconEmoji} ${periods[1].tempStr}")
                            views.setTextViewText(R.id.widget_cperiod2_rain, "💧 ${periods[1].rainStr}")
                            views.setTextViewText(R.id.widget_cperiod2_wind, periods[1].windStr)

                            views.setTextViewText(R.id.widget_cperiod3_label, periods[2].label)
                            views.setTextViewText(R.id.widget_cperiod3_temp, "${periods[2].iconEmoji} ${periods[2].tempStr}")
                            views.setTextViewText(R.id.widget_cperiod3_rain, "💧 ${periods[2].rainStr}")
                            views.setTextViewText(R.id.widget_cperiod3_wind, periods[2].windStr)
                        }
                        views.setViewVisibility(R.id.widget_periods_compact_row, View.VISIBLE)
                    } else {
                        views.setViewVisibility(R.id.widget_periods_compact_row, View.GONE)
                    }
                }

                if (layoutId == R.layout.widget_weather_layout) {
                    val periods = getRelevantPeriods(weatherData)
                    if (minHeight >= 100 && periods.size >= 3) {
                        views.setViewVisibility(R.id.widget_periods_container, View.VISIBLE)
                        views.setViewVisibility(R.id.widget_periods_compact_row, View.GONE)

                        views.setTextViewText(R.id.widget_period1_label, periods[0].label)
                        views.setTextViewText(R.id.widget_period1_icon, periods[0].iconEmoji)
                        views.setTextViewText(R.id.widget_period1_temp, periods[0].tempStr)
                        views.setTextViewText(R.id.widget_period1_rain, "💧 ${periods[0].rainStr}")
                        views.setTextViewText(R.id.widget_period1_wind, periods[0].windStr)

                        views.setTextViewText(R.id.widget_period2_label, periods[1].label)
                        views.setTextViewText(R.id.widget_period2_icon, periods[1].iconEmoji)
                        views.setTextViewText(R.id.widget_period2_temp, periods[1].tempStr)
                        views.setTextViewText(R.id.widget_period2_rain, "💧 ${periods[1].rainStr}")
                        views.setTextViewText(R.id.widget_period2_wind, periods[1].windStr)

                        views.setTextViewText(R.id.widget_period3_label, periods[2].label)
                        views.setTextViewText(R.id.widget_period3_icon, periods[2].iconEmoji)
                        views.setTextViewText(R.id.widget_period3_temp, periods[2].tempStr)
                        views.setTextViewText(R.id.widget_period3_rain, "💧 ${periods[2].rainStr}")
                        views.setTextViewText(R.id.widget_period3_wind, periods[2].windStr)
                    } else {
                        views.setViewVisibility(R.id.widget_periods_container, View.GONE)
                        views.setViewVisibility(R.id.widget_periods_compact_row, View.GONE)
                    }

                    if (isMaxSize) {
                        views.setViewVisibility(R.id.widget_max_extra_container, View.VISIBLE)
                        val aqiData = airQualityDeferred?.await()
                        if (aqiData?.aqi != null) {
                            views.setViewVisibility(R.id.widget_air_quality_card, View.VISIBLE)
                            val aqiVal = aqiData.aqi.roundToInt()
                            val aqiCategory = aqiData.aqiCategory
                            val pm25Str = aqiData.pm25?.let { " • PM2.5: ${it.roundToInt()} µg/m³" } ?: ""
                            views.setTextViewText(R.id.widget_air_quality_value, "$aqiCategory (ICA $aqiVal)$pm25Str")
                        } else {
                            views.setViewVisibility(R.id.widget_air_quality_card, View.GONE)
                        }

                        val histData = historicalDeferred?.await()
                        if (histData != null) {
                            views.setViewVisibility(R.id.widget_historical_card, View.VISIBLE)
                            views.setTextViewText(R.id.widget_historical_title, "📅 Tal día coma hoxe ($pastYear)")

                            val tMax = histData.tempMax
                            val tMin = histData.tempMin
                            val meanTemp = if (tMax != null && tMin != null) {
                                ((tMax + tMin) / 2.0).roundToInt()
                            } else {
                                tMax?.roundToInt() ?: tMin?.roundToInt() ?: 0
                            }

                            views.setTextViewText(R.id.widget_historical_temp, "$meanTemp°")
                            views.setTextViewText(R.id.widget_historical_condition, histData.conditionDescription)
                            views.setTextViewText(R.id.widget_historical_emoji, histData.iconEmoji)

                            val rangeStr = if (tMax != null && tMin != null) {
                                "↕ ${tMax.roundToInt()}° / ${tMin.roundToInt()}°"
                            } else {
                                "--"
                            }
                            views.setTextViewText(R.id.widget_historical_range, rangeStr)

                            val histWindStr = histData.maxWindSpeed?.let { "💨 ${it.roundToInt()} km/h" } ?: "💨 --"
                            views.setTextViewText(R.id.widget_historical_wind, histWindStr)

                            val histRainStr = histData.precipitationSum?.let { String.format(Locale.US, "🌧️ %.1f mm", it) } ?: "🌧️ 0.0 mm"
                            views.setTextViewText(R.id.widget_historical_rain, histRainStr)
                        } else {
                            views.setViewVisibility(R.id.widget_historical_card, View.GONE)
                        }
                    } else {
                        views.setViewVisibility(R.id.widget_max_extra_container, View.GONE)
                    }
                }

                if (preset == com.example.data.widget.WidgetThemePreset.DYNAMIC) {
                    val hour = com.example.data.model.WeatherTimeUtils.getCurrentHourInZone(weatherData.timezone)
                    val isNight = hour < 7 || hour >= 22
                    val code = weatherData.current.weatherCode ?: 0
                    val precip = weatherData.current.precipitation ?: 0.0

                    val isRain = precip > 0.1 || (code in 51..67) || (code in 80..82) || (code in 95..99)
                    val isFogOrCloudy = (code in 45..48) || (code == 3)

                    val bgRes = when {
                        isRain -> R.drawable.bg_widget_rain
                        isNight -> R.drawable.bg_widget_night
                        isFogOrCloudy -> R.drawable.bg_widget_cloudy
                        else -> R.drawable.bg_widget_sunny
                    }
                    val secColor = when {
                        isRain -> 0xFF94A3B8.toInt()
                        isNight -> 0xFFC7D2FE.toInt()
                        isFogOrCloudy -> 0xFFCBD5E1.toInt()
                        else -> 0xFFBAE6FD.toInt()
                    }
                    val primColor = 0xFFFFFFFF.toInt()

                    views.setInt(R.id.widget_root, "setBackgroundResource", bgRes)
                    views.setTextColor(R.id.widget_location, primColor)
                    views.setTextColor(R.id.widget_updated, secColor)
                    views.setTextColor(R.id.widget_temperature, primColor)
                    views.setTextColor(R.id.widget_condition, primColor)
                    views.setTextColor(R.id.widget_feels_like, secColor)
                    views.setTextColor(R.id.widget_uv, secColor)

                    views.setTextColor(R.id.widget_wind, primColor)
                    views.setTextColor(R.id.widget_humidity, primColor)
                    views.setTextColor(R.id.widget_precip, primColor)
                    views.setTextColor(R.id.widget_label_wind, secColor)
                    views.setTextColor(R.id.widget_label_humidity, secColor)
                    views.setTextColor(R.id.widget_label_precip, secColor)

                    if (layoutId == R.layout.widget_weather_layout) {
                        views.setTextColor(R.id.widget_period1_label, secColor)
                        views.setTextColor(R.id.widget_period1_temp, primColor)
                        views.setTextColor(R.id.widget_period1_rain, secColor)
                        views.setTextColor(R.id.widget_period1_wind, secColor)
                        views.setTextColor(R.id.widget_period2_label, secColor)
                        views.setTextColor(R.id.widget_period2_temp, primColor)
                        views.setTextColor(R.id.widget_period2_rain, secColor)
                        views.setTextColor(R.id.widget_period2_wind, secColor)
                        views.setTextColor(R.id.widget_period3_label, secColor)
                        views.setTextColor(R.id.widget_period3_temp, primColor)
                        views.setTextColor(R.id.widget_period3_rain, secColor)
                        views.setTextColor(R.id.widget_period3_wind, secColor)
                        views.setTextColor(R.id.widget_air_quality_label, secColor)
                        views.setTextColor(R.id.widget_air_quality_value, primColor)
                        views.setTextColor(R.id.widget_historical_title, secColor)
                        views.setTextColor(R.id.widget_historical_temp, primColor)
                        views.setTextColor(R.id.widget_historical_temp_label, secColor)
                        views.setTextColor(R.id.widget_historical_condition, primColor)
                        views.setTextColor(R.id.widget_historical_range, secColor)
                        views.setTextColor(R.id.widget_historical_wind, secColor)
                        views.setTextColor(R.id.widget_historical_rain, secColor)
                    } else if (layoutId == R.layout.widget_weather_layout_compact) {
                        views.setTextColor(R.id.widget_periods_compact_text, primColor)
                        views.setTextColor(R.id.widget_cperiod1_label, secColor)
                        views.setTextColor(R.id.widget_cperiod1_temp, primColor)
                        views.setTextColor(R.id.widget_cperiod1_rain, secColor)
                        views.setTextColor(R.id.widget_cperiod1_wind, secColor)
                        views.setTextColor(R.id.widget_cperiod2_label, secColor)
                        views.setTextColor(R.id.widget_cperiod2_temp, primColor)
                        views.setTextColor(R.id.widget_cperiod2_rain, secColor)
                        views.setTextColor(R.id.widget_cperiod2_wind, secColor)
                        views.setTextColor(R.id.widget_cperiod3_label, secColor)
                        views.setTextColor(R.id.widget_cperiod3_temp, primColor)
                        views.setTextColor(R.id.widget_cperiod3_rain, secColor)
                        views.setTextColor(R.id.widget_cperiod3_wind, secColor)
                    }

                    views.setInt(R.id.widget_btn_prev_location, "setColorFilter", primColor)
                    views.setInt(R.id.widget_btn_next_location, "setColorFilter", primColor)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                views.setTextViewText(R.id.widget_updated, "Erro ao actualizar (toca 🔄)")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private data class WidgetPeriodDisplay(
        val label: String,
        val iconEmoji: String,
        val tempStr: String,
        val rainStr: String,
        val windStr: String = ""
    )

    private fun getRelevantPeriods(weatherData: com.example.data.repository.FullWeatherData): List<WidgetPeriodDisplay> {
        val hour = com.example.data.model.WeatherTimeUtils.getCurrentHourInZone(weatherData.timezone)
        val todayPeriods = weatherData.sevenDayForecast.firstOrNull()?.periods.orEmpty()
        val tomorrowPeriods = weatherData.sevenDayForecast.getOrNull(1)?.periods.orEmpty()
        val baseWind = (weatherData.current.windSpeed ?: 10.0).roundToInt().coerceAtLeast(5)
        val maxWind = (weatherData.sevenDayForecast.firstOrNull()?.maxWindSpeed ?: (weatherData.current.windSpeed ?: 15.0)).roundToInt().coerceAtLeast(baseWind)
        val list = mutableListOf<WidgetPeriodDisplay>()

        if (hour >= 21) {
            // Night shift: Esta Noite, Mañá (M), Mañá (T)
            val tonight = todayPeriods.firstOrNull { it.periodName.contains("Noite", ignoreCase = true) }
                ?: todayPeriods.lastOrNull()
            if (tonight != null) {
                val temp = tonight.estimatedTemp?.roundToInt()?.let { "$it°" } ?: "--°"
                list.add(WidgetPeriodDisplay("Esta Noite", tonight.iconEmoji, temp, "${tonight.precipitationProbability}%", "💨 ${(baseWind * 0.8).roundToInt()}"))
            }
            val tomorrowMorning = tomorrowPeriods.firstOrNull { it.periodName.contains("Mañá", ignoreCase = true) }
                ?: tomorrowPeriods.getOrNull(0)
            if (tomorrowMorning != null) {
                val temp = tomorrowMorning.estimatedTemp?.roundToInt()?.let { "$it°" } ?: "--°"
                list.add(WidgetPeriodDisplay("Mañá (M)", tomorrowMorning.iconEmoji, temp, "${tomorrowMorning.precipitationProbability}%", "💨 $baseWind"))
            }
            val tomorrowAfternoon = tomorrowPeriods.firstOrNull { it.periodName.contains("Tarde", ignoreCase = true) }
                ?: tomorrowPeriods.getOrNull(1)
            if (tomorrowAfternoon != null) {
                val temp = tomorrowAfternoon.estimatedTemp?.roundToInt()?.let { "$it°" } ?: "--°"
                list.add(WidgetPeriodDisplay("Mañá (T)", tomorrowAfternoon.iconEmoji, temp, "${tomorrowAfternoon.precipitationProbability}%", "💨 $maxWind"))
            }
        } else {
            // Standard daytime: Mañá, Tarde, Noite
            val periodWinds = listOf(
                "💨 $baseWind",
                "💨 $maxWind",
                "💨 ${(maxWind * 0.75).roundToInt().coerceAtLeast(5)}"
            )
            for ((idx, p) in todayPeriods.take(3).withIndex()) {
                val temp = p.estimatedTemp?.roundToInt()?.let { "$it°" } ?: "--°"
                val w = periodWinds.getOrElse(idx) { "💨 $baseWind" }
                list.add(WidgetPeriodDisplay(p.periodName, p.iconEmoji, temp, "${p.precipitationProbability}%", w))
            }
        }

        // Resilient fallback if period breakdowns are missing
        if (list.size < 3) {
            val days = weatherData.sevenDayForecast.take(3)
            list.clear()
            for ((idx, day) in days.withIndex()) {
                val label = when (idx) {
                    0 -> "Hoxe"
                    1 -> "Mañá"
                    else -> day.date.takeLast(5)
                }
                val w = "💨 ${day.maxWindSpeed.roundToInt().coerceAtLeast(5)}"
                list.add(
                    WidgetPeriodDisplay(
                        label = label,
                        iconEmoji = day.iconEmoji,
                        tempStr = "${day.tempMax.roundToInt()}° / ${day.tempMin.roundToInt()}°",
                        rainStr = "${day.precipitationProbability}%",
                        windStr = w
                    )
                )
            }
        }

        return list
    }

    companion object {
        const val ACTION_WIDGET_REFRESH = "com.example.ACTION_WIDGET_REFRESH"
        const val ACTION_WIDGET_NEXT_LOCATION = "com.example.ACTION_WIDGET_NEXT_LOCATION"
        const val ACTION_WIDGET_PREV_LOCATION = "com.example.ACTION_WIDGET_PREV_LOCATION"
    }
}

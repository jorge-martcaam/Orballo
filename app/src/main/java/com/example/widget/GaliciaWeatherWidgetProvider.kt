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
import com.example.data.location.LocationHelper
import com.example.data.model.GaliciaLocation
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        views.setTextColor(R.id.widget_uv, preset.primaryTextColor)
        views.setInt(R.id.widget_uv, "setBackgroundResource", preset.buttonBackgroundRes)

        if (layoutId == R.layout.widget_weather_layout || layoutId == R.layout.widget_weather_layout_wide) {
            views.setTextColor(R.id.widget_wind, preset.primaryTextColor)
            views.setTextColor(R.id.widget_humidity, preset.primaryTextColor)
            views.setTextColor(R.id.widget_precip, preset.primaryTextColor)
            views.setTextColor(R.id.widget_label_wind, preset.secondaryTextColor)
            views.setTextColor(R.id.widget_label_humidity, preset.secondaryTextColor)
            views.setTextColor(R.id.widget_label_precip, preset.secondaryTextColor)
        }

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

                val repository = WeatherRepository()
                val weatherData = repository.fetchWeather(targetLocation)

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
                views.setTextViewText(R.id.widget_feels_like, "Sensación $apparentTemp°C")
                views.setTextViewText(R.id.widget_emoji, weatherData.iconEmoji)
                views.setTextViewText(R.id.widget_wind, "$wind km/h")
                views.setTextViewText(R.id.widget_humidity, "$humidity%")
                views.setTextViewText(R.id.widget_precip, precipStr)
                views.setTextViewText(R.id.widget_updated, "Actualizado ás $timeStr")

                // Bind UV index badge next to emoji (visible only when uvIndex > 0)
                val uvVal = weatherData.current.uvIndex
                if (uvVal != null && uvVal > 0.0) {
                    val uvInt = uvVal.roundToInt()
                    views.setViewVisibility(R.id.widget_uv, View.VISIBLE)
                    views.setTextViewText(R.id.widget_uv, "☀️ $uvInt")
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
                    views.setTextColor(R.id.widget_uv, primColor)

                    if (layoutId == R.layout.widget_weather_layout || layoutId == R.layout.widget_weather_layout_wide) {
                        views.setTextColor(R.id.widget_wind, primColor)
                        views.setTextColor(R.id.widget_humidity, primColor)
                        views.setTextColor(R.id.widget_precip, primColor)
                        views.setTextColor(R.id.widget_label_wind, secColor)
                        views.setTextColor(R.id.widget_label_humidity, secColor)
                        views.setTextColor(R.id.widget_label_precip, secColor)
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

    companion object {
        const val ACTION_WIDGET_REFRESH = "com.example.ACTION_WIDGET_REFRESH"
        const val ACTION_WIDGET_NEXT_LOCATION = "com.example.ACTION_WIDGET_NEXT_LOCATION"
        const val ACTION_WIDGET_PREV_LOCATION = "com.example.ACTION_WIDGET_PREV_LOCATION"
    }
}

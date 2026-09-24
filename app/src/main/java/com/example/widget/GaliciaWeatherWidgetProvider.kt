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
                val locationPrefs = com.example.data.location.LocationPreferences(context.applicationContext)
                val targetLocation = locationPrefs.getDefaultLocation()

                val repository = WeatherRepository()
                val weatherData = repository.fetchWeather(targetLocation)

                val temp = weatherData.current.temperature?.roundToInt() ?: 0
                val apparentTemp = weatherData.current.apparentTemperature?.roundToInt() ?: temp
                val humidity = weatherData.current.relativeHumidity?.roundToInt() ?: 0
                val wind = weatherData.current.windSpeed?.roundToInt() ?: 0
                val precipVal = weatherData.current.precipitation
                val precipStr = precipVal?.let { String.format(Locale.US, "%.1f mm", it) } ?: "0.0 mm"
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

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

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                views.setTextViewText(R.id.widget_updated, "Erro ao actualizar (toca 🔄)")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_REFRESH = "com.example.ACTION_WIDGET_REFRESH"
    }
}

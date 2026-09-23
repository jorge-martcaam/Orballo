package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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

    private fun showLoadingState(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_weather_layout)
        views.setTextViewText(R.id.widget_updated, "Actualizando...")
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_weather_layout)

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
                val locationHelper = LocationHelper(context.applicationContext)
                val locationPrefs = com.example.data.location.LocationPreferences(context.applicationContext)
                val userDefault = locationPrefs.getDefaultLocation()
                val targetLocation = if (locationHelper.hasLocationPermission()) {
                    locationHelper.getCurrentLocation() ?: userDefault
                } else {
                    userDefault
                }

                val repository = WeatherRepository()
                val weatherData = repository.fetchWeather(targetLocation)

                val temp = weatherData.current.temperature?.roundToInt() ?: 0
                val humidity = weatherData.current.relativeHumidity?.roundToInt() ?: 0
                val wind = weatherData.current.windSpeed?.roundToInt() ?: 0
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

                views.setTextViewText(R.id.widget_location, targetLocation.name)
                views.setTextViewText(R.id.widget_temperature, "$temp°C")
                views.setTextViewText(
                    R.id.widget_condition,
                    "${weatherData.iconEmoji} ${weatherData.conditionDescription}"
                )
                views.setTextViewText(R.id.widget_humidity, "💧 $humidity%")
                views.setTextViewText(R.id.widget_wind, "💨 $wind km/h")
                views.setTextViewText(R.id.widget_updated, "Actualizado ás $timeStr • Toca 🔄")

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

package com.example.data.location

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.GaliciaLocation

class LocationPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getDefaultLocation(): GaliciaLocation {
        val name = prefs.getString(KEY_DEFAULT_NAME, null) ?: return GaliciaLocation.DEFAULT
        return GaliciaLocation.PRESETS.find { it.name == name } ?: GaliciaLocation.DEFAULT
    }

    fun setDefaultLocation(location: GaliciaLocation) {
        prefs.edit()
            .putString(KEY_DEFAULT_NAME, location.name)
            .apply()
    }

    fun isDefaultLocation(location: GaliciaLocation): Boolean {
        val defaultLoc = getDefaultLocation()
        return !location.isGps && location.name == defaultLoc.name
    }

    companion object {
        private const val PREFS_NAME = "galicia_weather_prefs"
        private const val KEY_DEFAULT_NAME = "default_location_name"
    }
}

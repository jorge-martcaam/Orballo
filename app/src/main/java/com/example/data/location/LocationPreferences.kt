package com.example.data.location

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.GaliciaLocation

enum class ToggleFavoriteResult {
    ADDED,
    REMOVED,
    LIMIT_REACHED
}

class LocationPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getDefaultLocation(): GaliciaLocation {
        val raw = prefs.getString(KEY_DEFAULT_NAME, null) ?: return GaliciaLocation.DEFAULT
        return decodeLocation(raw) ?: GaliciaLocation.DEFAULT
    }

    fun setDefaultLocation(location: GaliciaLocation) {
        prefs.edit()
            .putString(KEY_DEFAULT_NAME, encodeLocation(location))
            .apply()
    }

    fun isDefaultLocation(location: GaliciaLocation): Boolean {
        val defaultLoc = getDefaultLocation()
        return !location.isGps && location.name == defaultLoc.name
    }

    fun getFavorites(): List<GaliciaLocation> {
        val raw = prefs.getString(KEY_FAVORITES, null)
        if (raw.isNullOrBlank()) {
            return listOf(
                "Santiago de Compostela",
                "A Coruña",
                "Vigo",
                "Ourense",
                "Lugo",
                "Pontevedra"
            ).mapNotNull { name -> GaliciaLocation.ALL_LOCATIONS.find { it.name == name } }
        }
        val serializedItems = raw.split(";;").map { it.trim() }.filter { it.isNotEmpty() }.take(6)
        return serializedItems.mapNotNull { decodeLocation(it) }
    }

    fun toggleFavorite(location: GaliciaLocation): ToggleFavoriteResult {
        if (location.isGps) return ToggleFavoriteResult.REMOVED
        val currentFavorites = getFavorites().toMutableList()
        val existingIndex = currentFavorites.indexOfFirst { it.name == location.name }
        if (existingIndex >= 0) {
            currentFavorites.removeAt(existingIndex)
            val joined = currentFavorites.joinToString(";;") { encodeLocation(it) }
            prefs.edit().putString(KEY_FAVORITES, joined).apply()
            return ToggleFavoriteResult.REMOVED
        } else {
            if (currentFavorites.size >= 6) {
                return ToggleFavoriteResult.LIMIT_REACHED
            }
            currentFavorites.add(location)
            val joined = currentFavorites.joinToString(";;") { encodeLocation(it) }
            prefs.edit().putString(KEY_FAVORITES, joined).apply()
            return ToggleFavoriteResult.ADDED
        }
    }

    private fun encodeLocation(location: GaliciaLocation): String {
        return if (location.isGalicia && GaliciaLocation.ALL_LOCATIONS.any { it.name == location.name }) {
            location.name
        } else {
            "EXT#${location.name}#${location.province}#${location.latitude}#${location.longitude}#${location.country}"
        }
    }

    private fun decodeLocation(raw: String): GaliciaLocation? {
        if (raw.startsWith("EXT#")) {
            val parts = raw.split("#")
            if (parts.size >= 6) {
                val name = parts[1]
                val province = parts[2]
                val lat = parts[3].toDoubleOrNull() ?: return null
                val lon = parts[4].toDoubleOrNull() ?: return null
                val country = parts[5]
                return GaliciaLocation(
                    name = name,
                    province = province,
                    latitude = lat,
                    longitude = lon,
                    isGps = false,
                    concelloId = -1,
                    country = country,
                    isGalicia = false
                )
            }
        }
        return GaliciaLocation.ALL_LOCATIONS.find { it.name == raw }
    }


    fun isFavorite(location: GaliciaLocation): Boolean {
        if (location.isGps) return false
        return getFavorites().any { it.name == location.name }
    }

    companion object {
        private const val PREFS_NAME = "galicia_weather_prefs"
        private const val KEY_DEFAULT_NAME = "default_location_name"
        private const val KEY_FAVORITES = "favorite_locations_list"
    }
}

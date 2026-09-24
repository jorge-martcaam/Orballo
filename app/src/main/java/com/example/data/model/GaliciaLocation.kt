package com.example.data.model

import java.text.Collator
import java.text.Normalizer
import java.util.Locale

data class GaliciaLocation(
    val name: String,
    val province: String,
    val latitude: Double,
    val longitude: Double,
    val isGps: Boolean = false,
    val concelloId: Int = 15078,
    val country: String = "España",
    val isGalicia: Boolean = true
) {
    fun resolveConcelloId(): Int {
        if (!isGalicia) return -1
        if (!isGps && concelloId > 0) return concelloId
        return ALL_LOCATIONS.minByOrNull { preset ->
            val dLat = preset.latitude - latitude
            val dLon = preset.longitude - longitude
            dLat * dLat + dLon * dLon
        }?.concelloId ?: 15078
    }

    companion object {
        val PRESETS = listOf(
            GaliciaLocation("Santiago de Compostela", "A Coruña", 42.8782, -8.5448, concelloId = 15077),
            GaliciaLocation("A Coruña", "A Coruña", 43.3623, -8.4115, concelloId = 15030),
            GaliciaLocation("Vigo", "Pontevedra", 42.2406, -8.7207, concelloId = 36057),
            GaliciaLocation("Ourense", "Ourense", 42.3358, -7.8639, concelloId = 32054),
            GaliciaLocation("Lugo", "Lugo", 43.0097, -7.5568, concelloId = 27028),
            GaliciaLocation("Pontevedra", "Pontevedra", 42.4310, -8.6444, concelloId = 36038),
            GaliciaLocation("Ferrol", "A Coruña", 43.4832, -8.2369, concelloId = 15036),
            GaliciaLocation("Fisterra (Costa da Morte)", "A Coruña", 42.9090, -9.2628, concelloId = 15037),
            GaliciaLocation("Ribadeo (Mariña)", "Lugo", 43.5369, -7.0406, concelloId = 27051)
        )

        val ALL_LOCATIONS: List<GaliciaLocation> = GaliciaConcellosCatalog.ALL_313_CONCELLOS

        val DEFAULT = PRESETS[0]

        val PROVINCES = listOf("Todas", "A Coruña", "Lugo", "Ourense", "Pontevedra")

        fun search(query: String, provinceFilter: String? = null): List<GaliciaLocation> {
            val normalizedQuery = query.trim().unaccent()
            val collator = Collator.getInstance(Locale("gl", "ES")).apply { strength = Collator.SECONDARY }
            return ALL_LOCATIONS.filter { location ->
                val matchesProvince = provinceFilter == null || provinceFilter == "Todas" || location.province.equals(provinceFilter, ignoreCase = true)
                val matchesName = normalizedQuery.isEmpty() || location.name.unaccent().contains(normalizedQuery) || location.province.unaccent().contains(normalizedQuery)
                matchesProvince && matchesName
            }.sortedWith { a, b -> collator.compare(a.name, b.name) }
        }

        fun isInsideGaliciaBoundingBox(lat: Double, lon: Double): Boolean {
            return lat in 41.80..43.85 && lon in -9.35..-6.70
        }

        fun normalizeStem(text: String): String {
            val unaccented = text.unaccent().trim()
            // Strip leading articles
            return unaccented
                .replaceFirst(Regex("^(a|o|as|os|la|el|las|los|the)\\s+"), "")
                .trim()
        }

        /**
         * Checks if a geocoded remote location belongs to Galicia and matches an existing concello.
         * If matched, returns the official GaliciaLocation (ensuring MeteoGalicia WRF concelloId is used).
         */
        fun findMatchingGalicianConcello(
            name: String,
            lat: Double,
            lon: Double,
            admin1: String? = null,
            admin2: String? = null
        ): GaliciaLocation? {
            val isGaliciaRegion = admin1?.contains("Galicia", ignoreCase = true) == true ||
                admin2?.let { it.contains("Coruña", true) || it.contains("Lugo", true) || it.contains("Ourense", true) || it.contains("Pontevedra", true) } == true ||
                isInsideGaliciaBoundingBox(lat, lon)

            if (!isGaliciaRegion) return null

            val remoteStem = normalizeStem(name)

            // 1. Try exact stem match against 313 concellos
            val stemMatch = ALL_LOCATIONS.firstOrNull { concello ->
                val localStem = normalizeStem(concello.name)
                localStem == remoteStem || localStem.contains(remoteStem) || remoteStem.contains(localStem)
            }
            if (stemMatch != null) return stemMatch

            // 2. Proximity check within 12km in Galicia
            val nearest = ALL_LOCATIONS.minByOrNull { concello ->
                val dLat = concello.latitude - lat
                val dLon = (concello.longitude - lon) * Math.cos(Math.toRadians(lat))
                dLat * dLat + dLon * dLon
            }
            if (nearest != null) {
                val dLatKm = (nearest.latitude - lat) * 111.0
                val dLonKm = (nearest.longitude - lon) * 111.0 * Math.cos(Math.toRadians(lat))
                val distKm = Math.sqrt(dLatKm * dLatKm + dLonKm * dLonKm)
                if (distKm <= 12.0) return nearest
            }

            return null
        }

        private fun String.unaccent(): String {
            val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
            return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(temp, "").lowercase()
        }
    }
}


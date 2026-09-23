package com.example.data.model

data class GaliciaLocation(
    val name: String,
    val province: String,
    val latitude: Double,
    val longitude: Double,
    val isGps: Boolean = false,
    val concelloId: Int = 15078
) {
    fun resolveConcelloId(): Int {
        if (!isGps && concelloId > 0) return concelloId
        // Find nearest concello among presets for GPS or custom coordinates
        return PRESETS.minByOrNull { preset ->
            val dLat = preset.latitude - latitude
            val dLon = preset.longitude - longitude
            dLat * dLat + dLon * dLon
        }?.concelloId ?: 15078
    }

    companion object {
        val PRESETS = listOf(
            GaliciaLocation("Santiago de Compostela", "A Coruña", 42.8782, -8.5448, concelloId = 15078),
            GaliciaLocation("A Coruña", "A Coruña", 43.3623, -8.4115, concelloId = 15030),
            GaliciaLocation("Vigo", "Pontevedra", 42.2406, -8.7207, concelloId = 36057),
            GaliciaLocation("Ourense", "Ourense", 42.3358, -7.8639, concelloId = 32054),
            GaliciaLocation("Lugo", "Lugo", 43.0097, -7.5568, concelloId = 27028),
            GaliciaLocation("Pontevedra", "Pontevedra", 42.4310, -8.6444, concelloId = 36038),
            GaliciaLocation("Ferrol", "A Coruña", 43.4832, -8.2369, concelloId = 15036),
            GaliciaLocation("Fisterra (Costa da Morte)", "A Coruña", 42.9090, -9.2628, concelloId = 15037),
            GaliciaLocation("Ribadeo (Mariña)", "Lugo", 43.5369, -7.0406, concelloId = 27051)
        )
        val DEFAULT = PRESETS[0]
    }
}

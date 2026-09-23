package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeatherDto?,
    val hourly: HourlyWeatherDto? = null,
    val daily: DailyWeatherDto?
)

@JsonClass(generateAdapter = true)
data class HourlyWeatherDto(
    val time: List<String>?,
    @Json(name = "temperature_2m") val temperature: List<Double>?,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>?,
    @Json(name = "weather_code") val weatherCode: List<Int>?
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    @Json(name = "temperature_2m") val temperature: Double?,
    @Json(name = "relative_humidity_2m") val relativeHumidity: Double?,
    @Json(name = "apparent_temperature") val apparentTemperature: Double?,
    @Json(name = "precipitation") val precipitation: Double?,
    @Json(name = "weather_code") val weatherCode: Int?,
    @Json(name = "surface_pressure") val surfacePressure: Double?,
    @Json(name = "wind_speed_10m") val windSpeed: Double?,
    @Json(name = "wind_direction_10m") val windDirection: Double?,
    @Json(name = "wind_gusts_10m") val windGusts: Double?,
    @Json(name = "uv_index") val uvIndex: Double?
)

@JsonClass(generateAdapter = true)
data class DailyWeatherDto(
    val time: List<String>?,
    @Json(name = "weather_code") val weatherCode: List<Int>?,
    @Json(name = "temperature_2m_max") val temperatureMax: List<Double>?,
    @Json(name = "temperature_2m_min") val temperatureMin: List<Double>?,
    @Json(name = "precipitation_sum") val precipitationSum: List<Double>?,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>?,
    @Json(name = "wind_speed_10m_max") val windSpeedMax: List<Double>?
)

@JsonClass(generateAdapter = true)
data class AirQualityResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentAirQualityDto?
)

@JsonClass(generateAdapter = true)
data class CurrentAirQualityDto(
    @Json(name = "european_aqi") val europeanAqi: Double?,
    val pm10: Double?,
    @Json(name = "pm2_5") val pm25: Double?,
    @Json(name = "carbon_monoxide") val carbonMonoxide: Double?,
    @Json(name = "nitrogen_dioxide") val nitrogenDioxide: Double?,
    @Json(name = "sulphur_dioxide") val sulphurDioxide: Double?,
    val ozone: Double?
)

@JsonClass(generateAdapter = true)
data class HistoricalWeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val daily: HistoricalDailyDto?
)

@JsonClass(generateAdapter = true)
data class HistoricalDailyDto(
    val time: List<String>?,
    @Json(name = "weather_code") val weatherCode: List<Int>?,
    @Json(name = "temperature_2m_max") val temperatureMax: List<Double>?,
    @Json(name = "temperature_2m_min") val temperatureMin: List<Double>?,
    @Json(name = "precipitation_sum") val precipitationSum: List<Double>?,
    @Json(name = "wind_speed_10m_max") val windSpeedMax: List<Double>?
)

enum class WeatherDataSource(
    val badgeLabel: String,
    val modelDescription: String,
    val providerName: String
) {
    METEOGALICIA(
        badgeLabel = "MeteoGalicia WRF",
        modelDescription = "Modelo rexional WRF de MeteoGalicia (1-4 km)",
        providerName = "MeteoGalicia"
    ),
    OPEN_METEO_ECMWF(
        badgeLabel = "ECMWF Global",
        modelDescription = "Modelo global ECMWF IFS de Open-Meteo",
        providerName = "Open-Meteo"
    )
}

data class DayForecast(
    val date: String,
    val weatherCode: Int,
    val conditionDescription: String,
    val iconEmoji: String,
    val tempMax: Double,
    val tempMin: Double,
    val precipitationProbability: Int,
    val precipitationSum: Double,
    val maxWindSpeed: Double,
    val source: WeatherDataSource = WeatherDataSource.METEOGALICIA
)

data class HourlyForecast(
    val time: String,
    val fullDateTime: String,
    val temperature: Double,
    val precipitationProbability: Int,
    val weatherCode: Int,
    val conditionDescription: String,
    val iconEmoji: String,
    val source: WeatherDataSource = WeatherDataSource.METEOGALICIA
)

enum class UvSafetyLevel(
    val label: String,
    val hexColor: Long,
    val advice: String
) {
    LOW("Baixo", 0xFF10B981, "Non se precisa protección. Pode permanecer fóra con seguridade."),
    MODERATE("Moderado", 0xFFF59E0B, "Use lentes de sol e crema protectora se sae ao exterior."),
    HIGH("Alto", 0xFFF97316, "Protección necesaria. Busque a sombra nas horas centrais."),
    VERY_HIGH("Moi alto", 0xFFEF4444, "Protección extra imprescindible. Evite o sol do mediodía."),
    EXTREME("Extremo", 0xFF8B5CF6, "Tome todas as precaucións. A pel desprotexida quéimase en poucos minutos.")
}

object UvIndexUtils {
    fun getSafetyLevel(uvIndex: Double): UvSafetyLevel {
        return when {
            uvIndex < 3.0 -> UvSafetyLevel.LOW
            uvIndex < 6.0 -> UvSafetyLevel.MODERATE
            uvIndex < 8.0 -> UvSafetyLevel.HIGH
            uvIndex < 11.0 -> UvSafetyLevel.VERY_HIGH
            else -> UvSafetyLevel.EXTREME
        }
    }
}

object HourlyForecastUtils {
    fun filterRemainingHoursToday(
        hourlyList: List<HourlyForecast>,
        currentHour: Int = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    ): List<HourlyForecast> {
        if (hourlyList.isEmpty()) return emptyList()

        // Isolate today's items if multiple dates are present
        val firstDate = hourlyList.first().fullDateTime.substringBefore("T")
        val todayItems = if (firstDate.isNotBlank() && firstDate.contains("-")) {
            hourlyList.filter { it.fullDateTime.startsWith(firstDate) }
        } else {
            hourlyList
        }

        val filtered = todayItems.filter { item ->
            val itemHour = item.time.substringBefore(":").toIntOrNull() ?: 0
            itemHour >= currentHour
        }
        return if (filtered.isNotEmpty()) filtered else todayItems.takeLast(1)
    }
}

data class WeatherAlert(
    val level: AlertLevel,
    val title: String,
    val description: String,
    val instruction: String,
    val source: WeatherDataSource = WeatherDataSource.METEOGALICIA
)

enum class AlertLevel {
    YELLOW,
    ORANGE,
    RED
}

object WeatherConditionUtils {
    fun getConditionInfo(code: Int): Pair<String, String> {
        return when (code) {
            0 -> "Ceo despexado" to "☀️"
            1 -> "Pouco anubrado" to "🌤️"
            2 -> "Parcialmente anubrado" to "⛅"
            3 -> "Cuberto" to "☁️"
            45, 48 -> "Néboa (Brétema)" to "🌫️"
            51, 53, 55 -> "Orballo (Chuvisca)" to "🌦️"
            56, 57 -> "Orballo conxelante" to "🌧️"
            61 -> "Chuvia feble" to "🌦️"
            63 -> "Chuvia moderada" to "🌧️"
            65 -> "Chuvia forte atlántica" to "🌧️"
            66, 67 -> "Chuvia conxelante" to "🧊"
            71, 73, 75 -> "Nevada" to "❄️"
            77 -> "Cinzarrio" to "🌨️"
            80, 81, 82 -> "Chuvascos" to "🌧️"
            85, 86 -> "Chuvascos de neve" to "🌨️"
            95 -> "Treboada" to "⛈️"
            96, 99 -> "Treboada forte con sarabia" to "⛈️"
            else -> "Tempo variable" to "🌤️"
        }
    }

    fun evaluateGalicianAlerts(
        windSpeed: Double,
        windGusts: Double,
        precipitation: Double,
        temperature: Double,
        locationName: String
    ): List<WeatherAlert> {
        val alerts = mutableListOf<WeatherAlert>()

        // 1. Gales / Temporal de Vento (Frequent in Atlantic Galicia, Costa da Morte, Estaca de Bares)
        if (windGusts >= 100.0 || windSpeed >= 80.0) {
            alerts.add(
                WeatherAlert(
                    level = AlertLevel.RED,
                    title = "Aviso vermello: Temporal atlántico extremo",
                    description = "Rachas de vento superiores a ${windGusts.toInt()} km/h rexistradas na zona de $locationName.",
                    instruction = "Evite paseos marítimos, actividades ao aire libre e asegure obxectos soltos."
                )
            )
        } else if (windGusts >= 80.0 || windSpeed >= 60.0) {
            alerts.add(
                WeatherAlert(
                    level = AlertLevel.ORANGE,
                    title = "Aviso laranxa: Vento moi forte",
                    description = "Rachas perigosas de ata ${windGusts.toInt()} km/h con forte corrente atlántica.",
                    instruction = "Extreme a precaución en cantís costeiros e estradas expostas."
                )
            )
        } else if (windGusts >= 65.0 || windSpeed >= 45.0) {
            alerts.add(
                WeatherAlert(
                    level = AlertLevel.YELLOW,
                    title = "Aviso amarelo: Ventos costeiros fortes",
                    description = "Velocidade do vento de ${windSpeed.toInt()} km/h con rachas de ata ${windGusts.toInt()} km/h.",
                    instruction = "Estea atento aos avisos marítimos e ao estado do mar."
                )
            )
        }

        // 2. Heavy Rainfall / Ciclogénese Chuvia
        if (precipitation >= 40.0) {
            alerts.add(
                WeatherAlert(
                    level = AlertLevel.ORANGE,
                    title = "Aviso laranxa: Precipitación torrencial",
                    description = "Fortes precipitacións con chuvia acumulada superior a ${precipitation.toInt()} mm.",
                    instruction = "Risco de inundacións locais preto dos ríos (Ulla, Miño, Lérez) e vías urbanas."
                )
            )
        } else if (precipitation >= 20.0) {
            alerts.add(
                WeatherAlert(
                    level = AlertLevel.YELLOW,
                    title = "Aviso amarelo: Chuvia intensa e persistente",
                    description = "Chuvia persistente polo paso de fronte atlántica.",
                    instruction = "Conduza con precaución por risco de aquaplaning e visibilidade reducida."
                )
            )
        }

        // 3. Extreme Temperatures (e.g. Ourense heatwaves or Serra do Xurés/Manzaneda frost)
        if (temperature >= 38.0) {
            alerts.add(
                WeatherAlert(
                    level = AlertLevel.ORANGE,
                    title = "Aviso laranxa: Onda de calor",
                    description = "Temperaturas infrecuentemente altas de ${temperature.toInt()}°C no val interior.",
                    instruction = "Mantéñase hidratado e evite a exposición solar entre as 12:00 e as 18:00."
                )
            )
        } else if (temperature <= -3.0) {
            alerts.add(
                WeatherAlert(
                    level = AlertLevel.YELLOW,
                    title = "Aviso amarelo: Baixas temperaturas e xeadas",
                    description = "Condicións de conxelación a ${temperature.toInt()}°C en zonas de montaña.",
                    instruction = "Precaución con placas de xeo nas estradas comarcais."
                )
            )
        }

        return alerts
    }
}

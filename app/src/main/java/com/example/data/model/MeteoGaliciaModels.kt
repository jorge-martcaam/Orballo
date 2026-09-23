package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MeteoGaliciaDailyResponse(
    @Json(name = "predConcello") val predConcello: MeteoGaliciaPredConcello? = null
)

@JsonClass(generateAdapter = true)
data class MeteoGaliciaPredConcello(
    @Json(name = "idConcello") val idConcello: Int? = null,
    @Json(name = "nome") val nome: String? = null,
    @Json(name = "listaPredDiaConcello") val listaPredDiaConcello: List<MeteoGaliciaDiaForecast>? = null
)

@JsonClass(generateAdapter = true)
data class MeteoGaliciaDiaForecast(
    @Json(name = "dataPredicion") val dataPredicion: String? = null,
    @Json(name = "tMax") val tMax: Double? = null,
    @Json(name = "tMin") val tMin: Double? = null,
    @Json(name = "ceoDia") val ceoDia: Int? = null,
    @Json(name = "uvMax") val uvMax: Double? = null,
    @Json(name = "nivelAviso") val nivelAviso: String? = null,
    @Json(name = "pchoiva") val pchoiva: MeteoGaliciaPeriodValue? = null,
    @Json(name = "ceo") val ceo: MeteoGaliciaPeriodValue? = null,
    @Json(name = "vento") val vento: MeteoGaliciaPeriodValue? = null
)

@JsonClass(generateAdapter = true)
data class MeteoGaliciaPeriodValue(
    val manha: Int? = null,
    val tarde: Int? = null,
    val noite: Int? = null
) {
    fun maxProbability(): Int {
        val vals = listOfNotNull(manha, tarde, noite).filter { it in 0..100 }
        return if (vals.isNotEmpty()) vals.maxOrNull() ?: 0 else 0
    }
}

@JsonClass(generateAdapter = true)
data class MeteoGaliciaHourlyResponse(
    @Json(name = "predHoraria") val predHoraria: MeteoGaliciaPredHoraria? = null
)

@JsonClass(generateAdapter = true)
data class MeteoGaliciaPredHoraria(
    @Json(name = "idConcello") val idConcello: Int? = null,
    @Json(name = "nome") val nome: String? = null,
    @Json(name = "listaPredDiaHoraria") val listaPredDiaHoraria: List<MeteoGaliciaDiaHoraria>? = null
)

@JsonClass(generateAdapter = true)
data class MeteoGaliciaDiaHoraria(
    val dia: Int? = null,
    val listaPredHora: List<MeteoGaliciaHoraItem>? = null
)

@JsonClass(generateAdapter = true)
data class MeteoGaliciaHoraItem(
    @Json(name = "dataPredicion") val dataPredicion: String? = null,
    @Json(name = "icoCeo") val icoCeo: Int? = null,
    @Json(name = "icoVento") val icoVento: Int? = null,
    @Json(name = "tMedia") val tMedia: Double? = null
)

@JsonClass(generateAdapter = true)
data class MeteoGaliciaObservacionResponse(
    val listaObservacionConcellos: List<MeteoGaliciaObservacionItem>? = null
)

@JsonClass(generateAdapter = true)
data class MeteoGaliciaObservacionItem(
    val dataLocal: String? = null,
    val dataUTC: String? = null,
    val icoEstadoCeo: Int? = null,
    val icoVento: Int? = null,
    val idConcello: Int? = null,
    val nomeConcello: String? = null,
    val sensacionTermica: Double? = null,
    val temperatura: Double? = null
)

object MeteoGaliciaConditionUtils {
    fun getConditionInfo(code: Int): Pair<String, String> {
        val baseCode = if (code > 200) code - 100 else code
        val isNight = code > 200
        return when (baseCode) {
            101 -> (if (isNight) "Ceo despexado" else "Ceo despexado") to (if (isNight) "🌙" else "☀️")
            102 -> "Pouco anubrado" to (if (isNight) "☁️" else "🌤️")
            103 -> "Anubrado" to "⛅"
            104, 105 -> "Cuberto" to "☁️"
            106 -> "Néboa / Brétema" to "🌫️"
            107 -> "Orballo / Chuvisca" to "🌦️"
            108 -> "Chuvia" to "🌧️"
            109 -> "Neve" to "❄️"
            110 -> "Treboada" to "⛈️"
            111 -> "Chuvascos" to "🌧️"
            112 -> "Sarabia" to "🌨️"
            else -> "Parcialmente anubrado" to "⛅"
        }
    }

    fun mapToWmoCode(meteoGaliciaCode: Int): Int {
        val baseCode = if (meteoGaliciaCode > 200) meteoGaliciaCode - 100 else meteoGaliciaCode
        return when (baseCode) {
            101 -> 0
            102 -> 1
            103 -> 2
            104, 105 -> 3
            106 -> 45
            107 -> 51
            108 -> 61
            109 -> 71
            110 -> 95
            111 -> 80
            112 -> 85
            else -> 2
        }
    }
}

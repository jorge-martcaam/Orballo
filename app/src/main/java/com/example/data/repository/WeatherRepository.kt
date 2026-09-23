package com.example.data.repository

import com.example.data.api.MeteoGaliciaApiService
import com.example.data.api.NetworkClient
import com.example.data.api.WeatherApiService
import com.example.data.model.CurrentAirQualityDto
import com.example.data.model.CurrentWeatherDto
import com.example.data.model.DayForecast
import com.example.data.model.GaliciaLocation
import com.example.data.model.HourlyForecast
import com.example.data.model.MeteoGaliciaConditionUtils
import com.example.data.model.MeteoGaliciaDiaForecast
import com.example.data.model.MeteoGaliciaHourlyResponse
import com.example.data.model.MeteoGaliciaObservacionResponse
import com.example.data.model.WeatherAlert
import com.example.data.model.WeatherConditionUtils
import com.example.data.model.WeatherDataSource
import com.example.data.model.WeatherResponse
import com.example.data.model.AlertLevel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class FullWeatherData(
    val location: GaliciaLocation,
    val current: CurrentWeatherDto,
    val conditionDescription: String,
    val iconEmoji: String,
    val todayHourlyForecast: List<HourlyForecast> = emptyList(),
    val sevenDayForecast: List<DayForecast>,
    val alerts: List<WeatherAlert>,
    val primarySource: WeatherDataSource = WeatherDataSource.METEOGALICIA,
    val isFallback: Boolean = false
)

data class AirQualityData(
    val aqi: Double?,
    val aqiCategory: String,
    val aqiColorHex: Long,
    val pm25: Double?,
    val pm10: Double?,
    val no2: Double?,
    val o3: Double?,
    val so2: Double?,
    val co: Double?
)

data class HistoricalDayData(
    val date: String,
    val conditionDescription: String,
    val iconEmoji: String,
    val tempMax: Double?,
    val tempMin: Double?,
    val precipitationSum: Double?,
    val maxWindSpeed: Double?
)

class WeatherRepository(
    private val api: WeatherApiService = NetworkClient.weatherApi,
    private val meteoGaliciaApi: MeteoGaliciaApiService = NetworkClient.meteoGaliciaApi
) {

    suspend fun fetchWeather(location: GaliciaLocation): FullWeatherData = coroutineScope {
        val concelloId = location.resolveConcelloId()

        // Fetch Open-Meteo ECMWF (for medium-range days 3-7 and as graceful fallback)
        val openMeteoDeferred = async {
            runCatching {
                api.getForecast(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    models = "ecmwf_ifs"
                )
            }.getOrNull()
        }

        // Fetch MeteoGalicia 0-48h daily, hourly, and observation
        val mgDailyDeferred = async {
            runCatching { meteoGaliciaApi.getDailyForecast(idConc = concelloId) }.getOrNull()
        }
        val mgHourlyDeferred = async {
            runCatching { meteoGaliciaApi.getHourlyForecast(idConc = concelloId) }.getOrNull()
        }
        val mgObsDeferred = async {
            runCatching { meteoGaliciaApi.getObservation(idConcello = concelloId) }.getOrNull()
        }

        val openMeteoResponse = openMeteoDeferred.await()
        val mgDailyResponse = mgDailyDeferred.await()
        val mgHourlyResponse = mgHourlyDeferred.await()
        val mgObsResponse = mgObsDeferred.await()

        val mgDailyList = mgDailyResponse?.predConcello?.listaPredDiaConcello
        val isMeteoGaliciaAvailable = !mgDailyList.isNullOrEmpty()

        if (isMeteoGaliciaAvailable) {
            buildHybridMeteoGaliciaWeather(
                location = location,
                mgDailyList = mgDailyList!!,
                mgHourlyResponse = mgHourlyResponse,
                mgObsResponse = mgObsResponse,
                openMeteoResponse = openMeteoResponse
            )
        } else {
            buildOpenMeteoFallbackWeather(
                location = location,
                openMeteoResponse = openMeteoResponse
            )
        }
    }

    private fun buildHybridMeteoGaliciaWeather(
        location: GaliciaLocation,
        mgDailyList: List<MeteoGaliciaDiaForecast>,
        mgHourlyResponse: MeteoGaliciaHourlyResponse?,
        mgObsResponse: MeteoGaliciaObservacionResponse?,
        openMeteoResponse: WeatherResponse?
    ): FullWeatherData {
        val obs = mgObsResponse?.listaObservacionConcellos?.firstOrNull()
        val omCurrent = openMeteoResponse?.current
        val day0 = mgDailyList[0]

        // Temperature & Conditions from MeteoGalicia Observation or Day 0
        val obsTemp = obs?.temperatura ?: day0.tMax ?: omCurrent?.temperature ?: 16.0
        val obsFeelsLike = obs?.sensacionTermica ?: omCurrent?.apparentTemperature ?: obsTemp
        val obsSkyCode = obs?.icoEstadoCeo ?: day0.ceoDia ?: 101
        val (conditionDesc, iconEmoji) = MeteoGaliciaConditionUtils.getConditionInfo(obsSkyCode)
        val wmoCode = MeteoGaliciaConditionUtils.mapToWmoCode(obsSkyCode)

        val currentDto = CurrentWeatherDto(
            temperature = obsTemp,
            relativeHumidity = omCurrent?.relativeHumidity ?: 76.0,
            apparentTemperature = obsFeelsLike,
            precipitation = omCurrent?.precipitation ?: 0.0,
            weatherCode = wmoCode,
            surfacePressure = omCurrent?.surfacePressure ?: 1018.0,
            windSpeed = omCurrent?.windSpeed ?: 18.0,
            windDirection = omCurrent?.windDirection ?: 220.0,
            windGusts = omCurrent?.windGusts ?: 28.0,
            uvIndex = day0.uvMax ?: omCurrent?.uvIndex ?: 3.0
        )

        // 1. Alerts: Process official MeteoGalicia civil alerts for the concello
        val alerts = mutableListOf<WeatherAlert>()
        for (dia in mgDailyList.take(2)) {
            val aviso = dia.nivelAviso
            if (!aviso.isNullOrBlank() && !aviso.equals("null", ignoreCase = true) && !aviso.equals("verde", ignoreCase = true)) {
                val level = when {
                    aviso.contains("vermello", ignoreCase = true) || aviso.contains("rojo", ignoreCase = true) -> AlertLevel.RED
                    aviso.contains("laranxa", ignoreCase = true) || aviso.contains("naranja", ignoreCase = true) -> AlertLevel.ORANGE
                    else -> AlertLevel.YELLOW
                }
                val diaDate = dia.dataPredicion?.substringBefore("T") ?: "Hoxe"
                alerts.add(
                    WeatherAlert(
                        level = level,
                        title = "Aviso oficial de MeteoGalicia ($diaDate)",
                        description = "Aviso meteorolóxico oficial de MeteoGalicia (${aviso.uppercase()}) activo para ${location.name}.",
                        instruction = "Siga as indicacións de protección civil para ${location.name} e as zonas costeiras atlánticas.",
                        source = WeatherDataSource.METEOGALICIA
                    )
                )
            }
        }

        // If no official alert was issued, evaluate Galician Atlantic physical thresholds
        if (alerts.isEmpty()) {
            val physicalAlerts = WeatherConditionUtils.evaluateGalicianAlerts(
                windSpeed = currentDto.windSpeed ?: 0.0,
                windGusts = currentDto.windGusts ?: (currentDto.windSpeed ?: 0.0),
                precipitation = currentDto.precipitation ?: 0.0,
                temperature = currentDto.temperature ?: 15.0,
                locationName = location.name
            )
            alerts.addAll(physicalAlerts.map { it.copy(source = WeatherDataSource.METEOGALICIA) })
        }

        // 2. Hourly Timeline (Today: Day 0 from MeteoGalicia)
        val hourlyList = mutableListOf<HourlyForecast>()
        val daysHoraria = mgHourlyResponse?.predHoraria?.listaPredDiaHoraria
        val todayDiaHoraria = daysHoraria?.firstOrNull()
        if (todayDiaHoraria != null) {
            for (hora in todayDiaHoraria.listaPredHora.orEmpty()) {
                val rawTime = hora.dataPredicion ?: continue
                val hourLabel = if (rawTime.contains("T")) {
                    val timePart = rawTime.substringAfter("T")
                    if (timePart.count { it == ':' } >= 2) timePart.substringBeforeLast(":") else timePart
                } else rawTime
                val icoCeo = hora.icoCeo ?: 101
                val (desc, emoji) = MeteoGaliciaConditionUtils.getConditionInfo(icoCeo)
                val wCode = MeteoGaliciaConditionUtils.mapToWmoCode(icoCeo)
                val temp = hora.tMedia ?: 15.0

                hourlyList.add(
                    HourlyForecast(
                        time = hourLabel,
                        fullDateTime = rawTime,
                        temperature = temp,
                        precipitationProbability = 0,
                        weatherCode = wCode,
                        conditionDescription = desc,
                        iconEmoji = emoji,
                        source = WeatherDataSource.METEOGALICIA
                    )
                )
            }
        }

        // If MeteoGalicia hourly wasn't populated, fallback to Open-Meteo hourly
        if (hourlyList.isEmpty()) {
            val omHourly = openMeteoResponse?.hourly
            if (omHourly?.time != null) {
                val count = minOf(omHourly.time.size, 24)
                for (i in 0 until count) {
                    val rawTime = omHourly.time[i]
                    val hourLabel = if (rawTime.contains("T")) rawTime.substringAfter("T") else rawTime
                    val code = omHourly.weatherCode?.getOrNull(i) ?: 0
                    val (desc, emoji) = WeatherConditionUtils.getConditionInfo(code)
                    val temp = omHourly.temperature?.getOrNull(i) ?: 15.0
                    val precipProb = omHourly.precipitationProbability?.getOrNull(i) ?: 0

                    hourlyList.add(
                        HourlyForecast(
                            time = hourLabel,
                            fullDateTime = rawTime,
                            temperature = temp,
                            precipitationProbability = precipProb,
                            weatherCode = code,
                            conditionDescription = desc,
                            iconEmoji = emoji,
                            source = WeatherDataSource.OPEN_METEO_ECMWF
                        )
                    )
                }
            }
        }

        // 3. 7-Day Forecast: 0-48h (Days 0, 1, 2) from MeteoGalicia, Days 3-7 from Open-Meteo ECMWF
        val forecastList = mutableListOf<DayForecast>()
        val mgDaysToTake = minOf(mgDailyList.size, 3)
        for (i in 0 until mgDaysToTake) {
            val mgDay = mgDailyList[i]
            val date = mgDay.dataPredicion?.substringBefore("T") ?: "Day ${i + 1}"
            val ceo = mgDay.ceoDia ?: 101
            val (desc, emoji) = MeteoGaliciaConditionUtils.getConditionInfo(ceo)
            val wCode = MeteoGaliciaConditionUtils.mapToWmoCode(ceo)
            val tMax = mgDay.tMax ?: 20.0
            val tMin = mgDay.tMin ?: 12.0
            val precipProb = mgDay.pchoiva?.maxProbability() ?: 0

            forecastList.add(
                DayForecast(
                    date = date,
                    weatherCode = wCode,
                    conditionDescription = desc,
                    iconEmoji = emoji,
                    tempMax = tMax,
                    tempMin = tMin,
                    precipitationProbability = precipProb,
                    precipitationSum = 0.0,
                    maxWindSpeed = 18.0,
                    source = WeatherDataSource.METEOGALICIA
                )
            )
        }

        // Append Days 3 to 7 from ECMWF (Open-Meteo)
        val omDaily = openMeteoResponse?.daily
        if (omDaily?.time != null) {
            val lastMgDate = forecastList.lastOrNull()?.date
            for (i in 0 until omDaily.time.size) {
                val omDate = omDaily.time[i]
                if ((lastMgDate == null || omDate > lastMgDate) && forecastList.size < 7) {
                    val code = omDaily.weatherCode?.getOrNull(i) ?: 0
                    val (cDesc, emoji) = WeatherConditionUtils.getConditionInfo(code)
                    val tMax = omDaily.temperatureMax?.getOrNull(i) ?: 18.0
                    val tMin = omDaily.temperatureMin?.getOrNull(i) ?: 11.0
                    val precipProb = omDaily.precipitationProbabilityMax?.getOrNull(i) ?: 0
                    val precipSum = omDaily.precipitationSum?.getOrNull(i) ?: 0.0
                    val windMax = omDaily.windSpeedMax?.getOrNull(i) ?: 16.0

                    forecastList.add(
                        DayForecast(
                            date = omDate,
                            weatherCode = code,
                            conditionDescription = cDesc,
                            iconEmoji = emoji,
                            tempMax = tMax,
                            tempMin = tMin,
                            precipitationProbability = precipProb,
                            precipitationSum = precipSum,
                            maxWindSpeed = windMax,
                            source = WeatherDataSource.OPEN_METEO_ECMWF
                        )
                    )
                }
            }
        }

        return FullWeatherData(
            location = location,
            current = currentDto,
            conditionDescription = conditionDesc,
            iconEmoji = iconEmoji,
            todayHourlyForecast = hourlyList,
            sevenDayForecast = forecastList,
            alerts = alerts,
            primarySource = WeatherDataSource.METEOGALICIA,
            isFallback = false
        )
    }

    private fun buildOpenMeteoFallbackWeather(
        location: GaliciaLocation,
        openMeteoResponse: WeatherResponse?
    ): FullWeatherData {
        val currentDto = openMeteoResponse?.current ?: CurrentWeatherDto(
            temperature = 16.0,
            relativeHumidity = 78.0,
            apparentTemperature = 15.0,
            precipitation = 0.0,
            weatherCode = 2,
            surfacePressure = 1018.0,
            windSpeed = 22.0,
            windDirection = 240.0,
            windGusts = 35.0,
            uvIndex = 3.0
        )

        val (conditionDesc, iconEmoji) = WeatherConditionUtils.getConditionInfo(currentDto.weatherCode ?: 0)

        val alerts = WeatherConditionUtils.evaluateGalicianAlerts(
            windSpeed = currentDto.windSpeed ?: 0.0,
            windGusts = currentDto.windGusts ?: (currentDto.windSpeed ?: 0.0),
            precipitation = currentDto.precipitation ?: 0.0,
            temperature = currentDto.temperature ?: 15.0,
            locationName = location.name
        ).map { it.copy(source = WeatherDataSource.OPEN_METEO_ECMWF) }

        val forecastList = mutableListOf<DayForecast>()
        val daily = openMeteoResponse?.daily
        if (daily?.time != null) {
            val count = minOf(daily.time.size, 7)
            for (i in 0 until count) {
                val date = daily.time[i]
                val code = daily.weatherCode?.getOrNull(i) ?: 0
                val (cDesc, emoji) = WeatherConditionUtils.getConditionInfo(code)
                val tMax = daily.temperatureMax?.getOrNull(i) ?: 18.0
                val tMin = daily.temperatureMin?.getOrNull(i) ?: 11.0
                val precipProb = daily.precipitationProbabilityMax?.getOrNull(i) ?: 0
                val precipSum = daily.precipitationSum?.getOrNull(i) ?: 0.0
                val windMax = daily.windSpeedMax?.getOrNull(i) ?: 18.0

                forecastList.add(
                    DayForecast(
                        date = date,
                        weatherCode = code,
                        conditionDescription = cDesc,
                        iconEmoji = emoji,
                        tempMax = tMax,
                        tempMin = tMin,
                        precipitationProbability = precipProb,
                        precipitationSum = precipSum,
                        maxWindSpeed = windMax,
                        source = WeatherDataSource.OPEN_METEO_ECMWF
                    )
                )
            }
        }

        val hourlyList = mutableListOf<HourlyForecast>()
        val hourly = openMeteoResponse?.hourly
        if (hourly?.time != null) {
            val count = minOf(hourly.time.size, 24)
            for (i in 0 until count) {
                val rawTime = hourly.time[i]
                val hourLabel = if (rawTime.contains("T")) rawTime.substringAfter("T") else rawTime
                val code = hourly.weatherCode?.getOrNull(i) ?: 0
                val (cDesc, emoji) = WeatherConditionUtils.getConditionInfo(code)
                val temp = hourly.temperature?.getOrNull(i) ?: 15.0
                val precipProb = hourly.precipitationProbability?.getOrNull(i) ?: 0

                hourlyList.add(
                    HourlyForecast(
                        time = hourLabel,
                        fullDateTime = rawTime,
                        temperature = temp,
                        precipitationProbability = precipProb,
                        weatherCode = code,
                        conditionDescription = cDesc,
                        iconEmoji = emoji,
                        source = WeatherDataSource.OPEN_METEO_ECMWF
                    )
                )
            }
        }

        return FullWeatherData(
            location = location,
            current = currentDto,
            conditionDescription = conditionDesc,
            iconEmoji = iconEmoji,
            todayHourlyForecast = hourlyList,
            sevenDayForecast = forecastList,
            alerts = alerts,
            primarySource = WeatherDataSource.OPEN_METEO_ECMWF,
            isFallback = true
        )
    }

    suspend fun fetchAirQuality(location: GaliciaLocation): AirQualityData {
        val response = api.getAirQuality(
            latitude = location.latitude,
            longitude = location.longitude
        )

        val current: CurrentAirQualityDto? = response.current
        val aqiValue = current?.europeanAqi

        val (category, colorHex) = when {
            aqiValue == null -> "Descoñecida" to 0xFF94A3B8
            aqiValue <= 20.0 -> "Boa (Óptima)" to 0xFF10B981
            aqiValue <= 40.0 -> "Razoable" to 0xFF3B82F6
            aqiValue <= 60.0 -> "Moderada" to 0xFFF59E0B
            aqiValue <= 80.0 -> "Mala" to 0xFFF97316
            else -> "Moi mala" to 0xFFEF4444
        }

        return AirQualityData(
            aqi = aqiValue,
            aqiCategory = category,
            aqiColorHex = colorHex,
            pm25 = current?.pm25,
            pm10 = current?.pm10,
            no2 = current?.nitrogenDioxide,
            o3 = current?.ozone,
            so2 = current?.sulphurDioxide,
            co = current?.carbonMonoxide
        )
    }

    suspend fun fetchHistoricalWeather(location: GaliciaLocation, dateString: String): HistoricalDayData {
        val response = api.getHistoricalWeather(
            latitude = location.latitude,
            longitude = location.longitude,
            startDate = dateString,
            endDate = dateString
        )

        val daily = response.daily
        val code = daily?.weatherCode?.firstOrNull() ?: 0
        val (desc, emoji) = WeatherConditionUtils.getConditionInfo(code)

        return HistoricalDayData(
            date = dateString,
            conditionDescription = desc,
            iconEmoji = emoji,
            tempMax = daily?.temperatureMax?.firstOrNull(),
            tempMin = daily?.temperatureMin?.firstOrNull(),
            precipitationSum = daily?.precipitationSum?.firstOrNull(),
            maxWindSpeed = daily?.windSpeedMax?.firstOrNull()
        )
    }
}

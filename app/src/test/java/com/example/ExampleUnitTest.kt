package com.example

import com.example.data.model.HourlyForecast
import com.example.data.model.HourlyForecastUtils
import com.example.data.model.UvIndexUtils
import com.example.data.model.UvSafetyLevel
import com.example.data.model.WeatherConditionUtils
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testUvIndexSafetyLevels() {
    assertEquals(UvSafetyLevel.LOW, UvIndexUtils.getSafetyLevel(0.0))
    assertEquals(UvSafetyLevel.LOW, UvIndexUtils.getSafetyLevel(2.5))
    assertEquals(UvSafetyLevel.LOW, UvIndexUtils.getSafetyLevel(2.9))

    assertEquals(UvSafetyLevel.MODERATE, UvIndexUtils.getSafetyLevel(3.0))
    assertEquals(UvSafetyLevel.MODERATE, UvIndexUtils.getSafetyLevel(4.5))
    assertEquals(UvSafetyLevel.MODERATE, UvIndexUtils.getSafetyLevel(5.9))

    assertEquals(UvSafetyLevel.HIGH, UvIndexUtils.getSafetyLevel(6.0))
    assertEquals(UvSafetyLevel.HIGH, UvIndexUtils.getSafetyLevel(7.5))
    assertEquals(UvSafetyLevel.HIGH, UvIndexUtils.getSafetyLevel(7.9))

    assertEquals(UvSafetyLevel.VERY_HIGH, UvIndexUtils.getSafetyLevel(8.0))
    assertEquals(UvSafetyLevel.VERY_HIGH, UvIndexUtils.getSafetyLevel(9.5))
    assertEquals(UvSafetyLevel.VERY_HIGH, UvIndexUtils.getSafetyLevel(10.9))

    assertEquals(UvSafetyLevel.EXTREME, UvIndexUtils.getSafetyLevel(11.0))
    assertEquals(UvSafetyLevel.EXTREME, UvIndexUtils.getSafetyLevel(14.0))
  }

  @Test
  fun testHourlyForecastModel() {
    val (desc, emoji) = WeatherConditionUtils.getConditionInfo(61)
    val hourly = HourlyForecast(
      time = "14:00",
      fullDateTime = "2026-09-22T14:00",
      temperature = 18.2,
      precipitationProbability = 60,
      weatherCode = 61,
      conditionDescription = desc,
      iconEmoji = emoji
    )

    assertEquals("14:00", hourly.time)
    assertEquals(18.2, hourly.temperature, 0.01)
    assertEquals(60, hourly.precipitationProbability)
    assertEquals("Chuvia feble", hourly.conditionDescription)
    assertEquals("🌦️", hourly.iconEmoji)
  }

  @Test
  fun testFilterRemainingHoursToday() {
    val sampleList = (0..23).map { hour ->
      val hourStr = String.format(java.util.Locale.US, "%02d:00", hour)
      HourlyForecast(
        time = hourStr,
        fullDateTime = "2026-09-22T$hourStr",
        temperature = 15.0 + hour,
        precipitationProbability = 10,
        weatherCode = 1,
        conditionDescription = "Mainly Clear",
        iconEmoji = "🌤️"
      )
    }

    // At hour 0, all 24 hours remain
    val atMidnight = HourlyForecastUtils.filterRemainingHoursToday(sampleList, currentHour = 0)
    assertEquals(24, atMidnight.size)
    assertEquals("00:00", atMidnight.first().time)

    // At hour 12 (midday), hours 00:00 - 11:00 are filtered out (12 hours remain: 12 to 23)
    val atMidday = HourlyForecastUtils.filterRemainingHoursToday(sampleList, currentHour = 12)
    assertEquals(12, atMidday.size)
    assertEquals("12:00", atMidday.first().time)
    assertEquals("23:00", atMidday.last().time)

    // Multi-day isolation: list with Day 0 and Day 1 should only return Day 0
    val tomorrowList = (0..23).map { hour ->
      val hourStr = String.format(java.util.Locale.US, "%02d:00", hour)
      HourlyForecast(
        time = hourStr,
        fullDateTime = "2026-09-23T$hourStr",
        temperature = 20.0,
        precipitationProbability = 0,
        weatherCode = 1,
        conditionDescription = "Clear",
        iconEmoji = "☀️"
      )
    }
    val combinedTwoDays = sampleList + tomorrowList
    val isolatedToday = HourlyForecastUtils.filterRemainingHoursToday(combinedTwoDays, currentHour = 0)
    assertEquals(24, isolatedToday.size)
    assertTrue(isolatedToday.all { it.fullDateTime.startsWith("2026-09-22") })

    // At hour 14 (2 PM), hours 00:00 - 13:00 are filtered out (10 hours remain: 14 to 23)
    val atTwoPm = HourlyForecastUtils.filterRemainingHoursToday(sampleList, currentHour = 14)
    assertEquals(10, atTwoPm.size)
    assertEquals("14:00", atTwoPm.first().time)
    assertEquals("23:00", atTwoPm.last().time)
    assertTrue(atTwoPm.none { it.time == "13:00" || it.time == "09:00" })

    // At hour 23 (11 PM), exactly 1 hour remains (23:00)
    val atElevenPm = HourlyForecastUtils.filterRemainingHoursToday(sampleList, currentHour = 23)
    assertEquals(1, atElevenPm.size)
    assertEquals("23:00", atElevenPm.first().time)
  }

  @Test
  fun testMeteoGaliciaConcelloResolution() {
    val santiago = com.example.data.model.GaliciaLocation.DEFAULT
    assertEquals(15077, santiago.resolveConcelloId())

    // GPS location near Vigo should resolve to Vigo concello (36057)
    val nearVigoGps = com.example.data.model.GaliciaLocation(
      name = "Current Location",
      province = "Pontevedra",
      latitude = 42.23,
      longitude = -8.71,
      isGps = true
    )
    assertEquals(36057, nearVigoGps.resolveConcelloId())
  }

  @Test
  fun testMeteoGaliciaSkyConditionMapping() {
    val (clearDesc, clearEmoji) = com.example.data.model.MeteoGaliciaConditionUtils.getConditionInfo(101)
    assertTrue(clearDesc.contains("despexado") || clearDesc.contains("Despexado"))
    assertEquals("☀️", clearEmoji)

    val (rainDesc, rainEmoji) = com.example.data.model.MeteoGaliciaConditionUtils.getConditionInfo(108)
    assertTrue(rainDesc.contains("chuvia") || rainDesc.contains("Chuvia"))
    assertEquals("🌧️", rainEmoji)

    val (fogDesc, fogEmoji) = com.example.data.model.MeteoGaliciaConditionUtils.getConditionInfo(106)
    assertTrue(fogDesc.contains("Néboa") || fogDesc.contains("Brétema"))
    assertEquals("🌫️", fogEmoji)
  }

  @Test
  fun testWeatherDataSourceProperties() {
    val mg = com.example.data.model.WeatherDataSource.METEOGALICIA
    val om = com.example.data.model.WeatherDataSource.OPEN_METEO_ECMWF
    assertNotEquals(mg.badgeLabel, om.badgeLabel)
    assertEquals("MeteoGalicia", mg.providerName)
    assertEquals("Open-Meteo", om.providerName)
  }
}

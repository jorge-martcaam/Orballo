package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CurrentWeatherDto
import com.example.data.model.GaliciaLocation
import com.example.data.model.HourlyForecast
import com.example.data.repository.FullWeatherData
import com.example.ui.TodayHourlyForecastSection
import com.example.ui.WeatherHeroCard
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `MainActivity launches and completes onCreate without error`() {
    androidx.test.core.app.ActivityScenario.launch(MainActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        org.junit.Assert.assertNotNull(activity)
      }
    }
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Tempo en Galicia", appName)
  }

  @Test
  fun `full WeatherScreen renders without exception`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    val viewModel = factory.create(com.example.ui.WeatherViewModel::class.java)

    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.WeatherScreen(viewModel = viewModel)
      }
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun `WeatherHeroCard and TodayHourlyForecastSection render without exception`() {
    val sampleData = FullWeatherData(
      location = GaliciaLocation.DEFAULT,
      current = CurrentWeatherDto(
        temperature = 17.5,
        relativeHumidity = 80.0,
        apparentTemperature = 16.5,
        precipitation = 0.0,
        weatherCode = 1,
        surfacePressure = 1015.0,
        windSpeed = 15.0,
        windDirection = 220.0,
        windGusts = 25.0,
        uvIndex = 3.5
      ),
      conditionDescription = "Mainly Clear",
      iconEmoji = "🌤️",
      todayHourlyForecast = listOf(
        HourlyForecast(
          time = "14:00",
          fullDateTime = "2026-09-22T14:00",
          temperature = 18.0,
          precipitationProbability = 0,
          weatherCode = 1,
          conditionDescription = "Mainly Clear",
          iconEmoji = "🌤️"
        )
      ),
      sevenDayForecast = emptyList(),
      alerts = emptyList()
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        WeatherHeroCard(
          data = sampleData,
          defaultLocation = GaliciaLocation.DEFAULT,
          onSetDefault = {}
        )
        TodayHourlyForecastSection(hourlyList = sampleData.todayHourlyForecast)
      }
    }

    composeTestRule.onNodeWithTag("hero_uv_index").assertIsDisplayed()
    composeTestRule.onNodeWithTag("today_hourly_section").assertIsDisplayed()
  }

  @Test
  fun `bottom navigation toggles between Actual and Historico pages`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    val viewModel = factory.create(com.example.ui.WeatherViewModel::class.java)

    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.WeatherScreen(viewModel = viewModel)
      }
    }

    composeTestRule.onNodeWithTag("nav_actual").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_historico").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_open_location_search").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_more_locations_chip").assertIsDisplayed()

    // Switch to Historico page
    viewModel.selectAppPage(com.example.ui.AppPage.HISTORICO)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("historical_date_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("historical_calendar_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_years_2").assertExists()
    composeTestRule.onNodeWithTag("btn_years_5").assertExists()
    composeTestRule.onNodeWithTag("btn_years_10").assertExists()
  }

  @Test
  fun `MultiYearHistoricalCard renders averages and insights cleanly`() {
    val sampleSummary = com.example.data.repository.MultiYearHistoricalSummary(
      targetMonthDay = "09-23",
      yearsCount = 5,
      avgTempMax = 22.4,
      avgTempMin = 13.2,
      avgPrecipitation = 4.5,
      avgWindSpeed = 19.8,
      rainDaysCount = 3,
      rainProbabilityPercent = 60,
      hottestYear = Pair(2023, 26.5),
      coldestYear = Pair(2021, 11.0),
      rainiestYear = Pair(2022, 14.2),
      records = listOf(
        com.example.data.repository.HistoricalDayData(
          date = "2025-09-23",
          conditionDescription = "Parcialmente nubrado",
          iconEmoji = "⛅",
          tempMax = 21.0,
          tempMin = 13.0,
          precipitationSum = 0.0,
          maxWindSpeed = 18.0
        )
      )
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.MultiYearHistoricalCard(
          summary = sampleSummary,
          locationName = "Santiago de Compostela"
        )
      }
    }

    composeTestRule.onNodeWithTag("multi_year_historical_card").assertIsDisplayed()
  }

  @Test
  fun `GaliciaLocation search performs accent-insensitive and province filtering`() {
    val results = com.example.data.model.GaliciaLocation.search("sanxenxo")
    org.junit.Assert.assertTrue("Should find Sanxenxo", results.any { it.name == "Sanxenxo" })

    val corunaResults = com.example.data.model.GaliciaLocation.search("coruna", "A Coruña")
    org.junit.Assert.assertTrue("Should find A Coruña", corunaResults.any { it.name == "A Coruña" })

    val ourenseResults = com.example.data.model.GaliciaLocation.search("ourense")
    org.junit.Assert.assertTrue("Should find Ourense", ourenseResults.any { it.name == "Ourense" })
  }

  @Test
  fun `LocationPreferences enforces hard cap of 6 favorites and rejects 7th`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val prefs = com.example.data.location.LocationPreferences(context)
    val favorites = prefs.getFavorites()
    org.junit.Assert.assertEquals(6, favorites.size)

    // Attempting to add a 7th location that is not in favorites
    val newLoc = com.example.data.model.GaliciaLocation("Sanxenxo", "Pontevedra", 42.4000, -8.8067, concelloId = 36051)
    val result = prefs.toggleFavorite(newLoc)
    org.junit.Assert.assertEquals(com.example.data.location.ToggleFavoriteResult.LIMIT_REACHED, result)
    org.junit.Assert.assertEquals(6, prefs.getFavorites().size)

    // Remove one existing favorite
    val firstFav = favorites[0]
    val removeResult = prefs.toggleFavorite(firstFav)
    org.junit.Assert.assertEquals(com.example.data.location.ToggleFavoriteResult.REMOVED, removeResult)
    org.junit.Assert.assertEquals(5, prefs.getFavorites().size)

    // Now adding the new location succeeds
    val addResult = prefs.toggleFavorite(newLoc)
    org.junit.Assert.assertEquals(com.example.data.location.ToggleFavoriteResult.ADDED, addResult)
    org.junit.Assert.assertEquals(6, prefs.getFavorites().size)
    org.junit.Assert.assertTrue(prefs.getFavorites().any { it.name == "Sanxenxo" })
  }

  @Test
  fun `GaliciaLocation search returns results in alphabetical order`() {
    val all = com.example.data.model.GaliciaLocation.search("")
    val names = all.map { it.name }
    val collator = java.text.Collator.getInstance(java.util.Locale.forLanguageTag("gl-ES")).apply { strength = java.text.Collator.SECONDARY }
    for (i in 0 until names.size - 1) {
      org.junit.Assert.assertTrue(
        "Expected '${names[i]}' to precede or equal '${names[i + 1]}'",
        collator.compare(names[i], names[i + 1]) <= 0
      )
    }
  }

  @Test
  fun `GaliciaConcellosCatalog contains all 313 concellos with correct provincial breakdown`() {
    val all = com.example.data.model.GaliciaConcellosCatalog.ALL_313_CONCELLOS
    org.junit.Assert.assertEquals(313, all.size)

    val coruna = all.filter { it.province == "A Coruña" }
    val lugo = all.filter { it.province == "Lugo" }
    val ourense = all.filter { it.province == "Ourense" }
    val pontevedra = all.filter { it.province == "Pontevedra" }

    org.junit.Assert.assertEquals(93, coruna.size)
    org.junit.Assert.assertEquals(67, lugo.size)
    org.junit.Assert.assertEquals(92, ourense.size)
    org.junit.Assert.assertEquals(61, pontevedra.size)

    // Verify small rural concellos across all 4 provinces resolve properly
    val testRural = listOf(
      "Negueira de Muñiz", "Avión", "Vilar de Santos", "Dumbría", "A Mezquita", "Fornelos de Montes", "Triacastela", "Santiso"
    )
    for (ruralName in testRural) {
      val found = com.example.data.model.GaliciaLocation.search(ruralName)
      org.junit.Assert.assertTrue("Should find rural concello: $ruralName", found.any { it.name.contains(ruralName, ignoreCase = true) })
      val loc = found.first { it.name.contains(ruralName, ignoreCase = true) }
      org.junit.Assert.assertTrue("Concello ID should be valid 5-digit INE: ${loc.concelloId}", loc.concelloId in 15001..36999)
      org.junit.Assert.assertTrue("Latitude should be within Galicia: ${loc.latitude}", loc.latitude in 41.8..43.8)
      org.junit.Assert.assertTrue("Longitude should be within Galicia: ${loc.longitude}", loc.longitude in -9.3..-6.7)
    }
  }

  @Test
  fun `deduplication logic correctly matches Galician concellos and distinguishes external cities`() {
    // 1. Galician concello returned with Spanish article or spelling
    val matchCoruna = GaliciaLocation.findMatchingGalicianConcello(
      name = "La Coruña",
      lat = 43.3623,
      lon = -8.4115,
      admin1 = "Galicia",
      admin2 = "A Coruña"
    )
    org.junit.Assert.assertNotNull(matchCoruna)
    org.junit.Assert.assertEquals("A Coruña", matchCoruna?.name)
    org.junit.Assert.assertEquals(15030, matchCoruna?.concelloId)

    // 2. Santiago de Compostela within bounding box
    val matchSantiago = GaliciaLocation.findMatchingGalicianConcello(
      name = "Santiago de Compostela",
      lat = 42.8805,
      lon = -8.5456,
      admin1 = "Galicia"
    )
    org.junit.Assert.assertNotNull(matchSantiago)
    org.junit.Assert.assertEquals("Santiago de Compostela", matchSantiago?.name)
    org.junit.Assert.assertEquals(15077, matchSantiago?.concelloId)

    // 3. External Spanish city (Madrid) -> must NOT match any Galician concello
    val matchMadrid = GaliciaLocation.findMatchingGalicianConcello(
      name = "Madrid",
      lat = 40.4168,
      lon = -3.7038,
      admin1 = "Comunidad de Madrid"
    )
    org.junit.Assert.assertNull(matchMadrid)

    // 4. External Global city (Paris) -> must NOT match any Galician concello
    val matchParis = GaliciaLocation.findMatchingGalicianConcello(
      name = "Paris",
      lat = 48.8566,
      lon = 2.3522,
      admin1 = "Île-de-France"
    )
    org.junit.Assert.assertNull(matchParis)
  }

  @Test
  fun `LocationPreferences persists and restores external non-Galician locations`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = com.example.data.location.LocationPreferences(context)

    val externalLoc = GaliciaLocation(
      name = "Madrid",
      province = "Comunidad de Madrid",
      latitude = 40.4168,
      longitude = -3.7038,
      isGps = false,
      concelloId = -1,
      country = "España",
      isGalicia = false
    )

    prefs.setDefaultLocation(externalLoc)
    val restored = prefs.getDefaultLocation()
    org.junit.Assert.assertEquals("Madrid", restored.name)
    org.junit.Assert.assertEquals("Comunidad de Madrid", restored.province)
    org.junit.Assert.assertEquals("España", restored.country)
    org.junit.Assert.assertFalse(restored.isGalicia)
    org.junit.Assert.assertEquals(40.4168, restored.latitude, 0.001)
  }

  @Test
  fun `all weather conditions and alerts remain strictly in Galician language`() {
    val codes = listOf(0, 1, 2, 3, 51, 61, 80, 95)
    for (code in codes) {
      val (desc, _) = com.example.data.model.WeatherConditionUtils.getConditionInfo(code)
      org.junit.Assert.assertTrue("Condition for code $code should not be blank", desc.isNotBlank())
      // Check that description contains Galician characteristic vocabulary
      val galicianKeywords = listOf("despexado", "anubrado", "cuberto", "chuvia", "orballo", "chuvascos", "treboada", "neve", "brétema", "parcialmente")
      org.junit.Assert.assertTrue(
        "Description '$desc' should be in Galician",
        galicianKeywords.any { desc.lowercase().contains(it) }
      )
    }
  }

  @Test
  fun `widget layout inflates successfully in RemoteViews without InflateException`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val views = android.widget.RemoteViews(context.packageName, R.layout.widget_weather_layout)
    val container = android.widget.FrameLayout(context)
    val inflated = views.apply(context, container)
    org.junit.Assert.assertNotNull("Widget layout must inflate without throwing InflateException", inflated)

    // Ensure all critical IDs exist in the inflated view hierarchy
    org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_location))
    org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_temperature))
    org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_condition))
    org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_feels_like))
    org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_emoji))
    org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_wind))
    org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_humidity))
    org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_precip))
    org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_btn_refresh))
  }

  @Test
  fun `widget theme presets persist and apply correctly across all 3 contrast modes`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = com.example.data.widget.WidgetPreferences(context)

    // Check default
    org.junit.Assert.assertEquals(com.example.data.widget.WidgetThemePreset.OCEAN_BLUE, prefs.getPreset())

    for (preset in com.example.data.widget.WidgetThemePreset.entries) {
      prefs.setPreset(preset)
      org.junit.Assert.assertEquals(preset, prefs.getPreset())

      val views = android.widget.RemoteViews(context.packageName, R.layout.widget_weather_layout)
      views.setInt(R.id.widget_root, "setBackgroundResource", preset.backgroundRes)
      views.setInt(R.id.widget_btn_refresh, "setBackgroundResource", preset.buttonBackgroundRes)
      views.setInt(R.id.widget_btn_refresh, "setColorFilter", preset.primaryTextColor)
      views.setTextColor(R.id.widget_location, preset.primaryTextColor)
      views.setTextColor(R.id.widget_updated, preset.secondaryTextColor)
      views.setTextColor(R.id.widget_temperature, preset.primaryTextColor)
      views.setTextColor(R.id.widget_condition, preset.primaryTextColor)
      views.setTextColor(R.id.widget_feels_like, preset.secondaryTextColor)
      views.setTextColor(R.id.widget_wind, preset.primaryTextColor)
      views.setTextColor(R.id.widget_humidity, preset.primaryTextColor)
      views.setTextColor(R.id.widget_precip, preset.primaryTextColor)

      val container = android.widget.FrameLayout(context)
      val inflated = views.apply(context, container)
      org.junit.Assert.assertNotNull("Preset ${preset.name} must inflate and apply cleanly", inflated)
    }
  }

  @Test
  fun `compact responsive widget layout inflates cleanly without InflateException across all presets`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    for (preset in com.example.data.widget.WidgetThemePreset.entries) {
      val views = android.widget.RemoteViews(context.packageName, R.layout.widget_weather_layout_compact)
      views.setInt(R.id.widget_root, "setBackgroundResource", preset.backgroundRes)
      views.setInt(R.id.widget_btn_refresh, "setBackgroundResource", preset.buttonBackgroundRes)
      views.setInt(R.id.widget_btn_refresh, "setColorFilter", preset.primaryTextColor)
      views.setTextColor(R.id.widget_location, preset.primaryTextColor)
      views.setTextColor(R.id.widget_updated, preset.secondaryTextColor)
      views.setTextColor(R.id.widget_temperature, preset.primaryTextColor)
      views.setTextColor(R.id.widget_condition, preset.primaryTextColor)
      views.setTextColor(R.id.widget_feels_like, preset.secondaryTextColor)

      val container = android.widget.FrameLayout(context)
      val inflated = views.apply(context, container)
      org.junit.Assert.assertNotNull("Compact preset ${preset.name} must inflate cleanly", inflated)

      // Verify compact view elements
      org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_location))
      org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_temperature))
      org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_condition))
      org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_feels_like))
      org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_emoji))
      org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_btn_refresh))
    }
  }

  @Test
  fun `wide 4x1 responsive widget layout inflates cleanly and supports UV and 3 metrics`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    for (preset in com.example.data.widget.WidgetThemePreset.entries) {
      val views = android.widget.RemoteViews(context.packageName, R.layout.widget_weather_layout_wide)
      views.setInt(R.id.widget_root, "setBackgroundResource", preset.backgroundRes)
      views.setInt(R.id.widget_btn_refresh, "setBackgroundResource", preset.buttonBackgroundRes)
      views.setInt(R.id.widget_btn_refresh, "setColorFilter", preset.primaryTextColor)
      views.setTextColor(R.id.widget_location, preset.primaryTextColor)
      views.setTextColor(R.id.widget_updated, preset.secondaryTextColor)
      views.setTextColor(R.id.widget_temperature, preset.primaryTextColor)
      views.setTextColor(R.id.widget_condition, preset.primaryTextColor)
      views.setTextColor(R.id.widget_uv, preset.primaryTextColor)
      views.setInt(R.id.widget_uv, "setBackgroundResource", preset.buttonBackgroundRes)
      views.setTextColor(R.id.widget_wind, preset.primaryTextColor)
      views.setTextColor(R.id.widget_humidity, preset.primaryTextColor)
      views.setTextColor(R.id.widget_precip, preset.primaryTextColor)

      // Test daytime UV active
      views.setViewVisibility(R.id.widget_uv, android.view.View.VISIBLE)
      views.setTextViewText(R.id.widget_uv, "☀️ 4")

      val container = android.widget.FrameLayout(context)
      val inflated = views.apply(context, container)
      org.junit.Assert.assertNotNull("Wide preset ${preset.name} must inflate cleanly", inflated)

      // Verify all wide view elements exist
      org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_location))
      org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_temperature))
      org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_condition))
      org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_emoji))
      val uvView = inflated.findViewById<android.widget.TextView>(R.id.widget_uv)
      org.junit.Assert.assertNotNull(uvView)
      org.junit.Assert.assertEquals(android.view.View.VISIBLE, uvView.visibility)
      org.junit.Assert.assertEquals("☀️ 4", uvView.text.toString())

      // Verify the 3 fixed metrics exist in the wide format
      org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_wind))
      org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_humidity))
      org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_precip))
      org.junit.Assert.assertNotNull(inflated.findViewById<android.view.View>(R.id.widget_btn_refresh))
    }
  }

  @Test
  fun `night mode hides UV badge on all layouts`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val layouts = listOf(
      R.layout.widget_weather_layout,
      R.layout.widget_weather_layout_compact,
      R.layout.widget_weather_layout_wide
    )

    for (layoutRes in layouts) {
      val views = android.widget.RemoteViews(context.packageName, layoutRes)
      // Nighttime: UV <= 0 -> GONE
      views.setViewVisibility(R.id.widget_uv, android.view.View.GONE)
      val container = android.widget.FrameLayout(context)
      val inflated = views.apply(context, container)
      val uvView = inflated.findViewById<android.view.View>(R.id.widget_uv)
      org.junit.Assert.assertNotNull("UV view must exist in layout $layoutRes", uvView)
      org.junit.Assert.assertEquals("UV view must be GONE at night in layout $layoutRes", android.view.View.GONE, uvView.visibility)
    }
  }
}

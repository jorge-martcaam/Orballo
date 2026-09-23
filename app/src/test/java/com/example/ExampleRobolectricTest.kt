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
}

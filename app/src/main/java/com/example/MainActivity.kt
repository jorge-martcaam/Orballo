package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ThemeMode
import com.example.ui.WeatherScreen
import com.example.ui.WeatherViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: WeatherViewModel = viewModel()
      val themeMode by viewModel.themeMode.collectAsState()
      val systemDark = isSystemInDarkTheme()
      val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.BRETEMA -> false
        ThemeMode.NOITE -> true
      }

      MyApplicationTheme(darkTheme = isDark) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          WeatherScreen(viewModel = viewModel)
        }
      }
    }
  }
}

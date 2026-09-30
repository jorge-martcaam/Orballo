package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = OrballoNoiteTokens.primary,
    secondary = OrballoNoiteTokens.secondary,
    tertiary = OrballoNoiteTokens.primary,
    background = OrballoNoiteTokens.surface,
    surface = OrballoNoiteTokens.surfaceContainerLow,
    surfaceVariant = OrballoNoiteTokens.surfaceContainer,
    surfaceContainer = OrballoNoiteTokens.surfaceContainer,
    surfaceContainerHigh = OrballoNoiteTokens.surfaceContainerHigh,
    surfaceContainerLow = OrballoNoiteTokens.surfaceContainerLow,
    surfaceContainerLowest = OrballoNoiteTokens.surfaceContainerLowest,
    surfaceBright = OrballoNoiteTokens.surfaceBright,
    onPrimary = Color(0xFF0F172A),
    onSecondary = Color(0xFF0F172A),
    onBackground = OrballoNoiteTokens.onSurface,
    onSurface = OrballoNoiteTokens.onSurface,
    onSurfaceVariant = OrballoNoiteTokens.onSurfaceVariant,
    outline = OrballoNoiteTokens.outlineVariant,
    outlineVariant = OrballoNoiteTokens.outlineVariant
  )

private val LightColorScheme =
  lightColorScheme(
    primary = OrballoBretemaTokens.primary,
    secondary = OrballoBretemaTokens.secondary,
    tertiary = OrballoBretemaTokens.primary,
    background = OrballoBretemaTokens.surface,
    surface = OrballoBretemaTokens.surfaceContainerLow,
    surfaceVariant = OrballoBretemaTokens.surfaceContainer,
    surfaceContainer = OrballoBretemaTokens.surfaceContainer,
    surfaceContainerHigh = OrballoBretemaTokens.surfaceContainerHigh,
    surfaceContainerLow = OrballoBretemaTokens.surfaceContainerLow,
    surfaceContainerLowest = OrballoBretemaTokens.surfaceContainerLowest,
    surfaceBright = OrballoBretemaTokens.surfaceBright,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = OrballoBretemaTokens.onSurface,
    onSurface = OrballoBretemaTokens.onSurface,
    onSurfaceVariant = OrballoBretemaTokens.onSurfaceVariant,
    outline = OrballoBretemaTokens.outlineVariant,
    outlineVariant = OrballoBretemaTokens.outlineVariant
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val tokens = if (darkTheme) OrballoNoiteTokens else OrballoBretemaTokens

  CompositionLocalProvider(LocalOrballoColors provides tokens) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}

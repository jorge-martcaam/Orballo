package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class OrballoColorTokens(
    val surface: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceBright: Color,
    val primary: Color,
    val secondary: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val glassTint: Color,
    val outlineVariant: Color
)

val OrballoNoiteTokens = OrballoColorTokens(
    surface = Color(0xFF0F131C),
    surfaceContainerLowest = Color(0xFF0A0E17),
    surfaceContainerLow = Color(0xFF181B25),
    surfaceContainer = Color(0xFF1E222D),
    surfaceContainerHigh = Color(0xFF282C38),
    surfaceBright = Color(0xFF353943),
    primary = Color(0xFF38BDF8),
    secondary = Color(0xFF58A6A6),
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF94A3B8),
    glassTint = Color(0xB30F172A),
    outlineVariant = Color(0x14FFFFFF)
)

val OrballoBretemaTokens = OrballoColorTokens(
    surface = Color(0xFFF7F9FC),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2F4F7),
    surfaceContainer = Color(0xFFE8ECF1),
    surfaceContainerHigh = Color(0xFFDDE2E8),
    surfaceBright = Color(0xFFFFFFFF),
    primary = Color(0xFF0284C7),
    secondary = Color(0xFF0EA5E9),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF64748B),
    glassTint = Color(0xD9FFFFFF),
    outlineVariant = Color(0x140F172A)
)

val LocalOrballoColors = staticCompositionLocalOf { OrballoNoiteTokens }

object OrballoThemeTokens {
    val OuterRadius = 28.dp
    val InnerRadius = 16.dp
    val PillRadius = 999.dp
    val SubtleBorderWidth = 1.dp
}

@Composable
fun OrballoTheme(
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    val tokens = if (isDark) OrballoNoiteTokens else OrballoBretemaTokens
    CompositionLocalProvider(LocalOrballoColors provides tokens) {
        content()
    }
}

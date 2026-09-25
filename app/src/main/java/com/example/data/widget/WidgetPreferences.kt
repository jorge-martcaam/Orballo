package com.example.data.widget

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.DrawableRes
import com.example.R

enum class WidgetThemePreset(
    val key: String,
    val title: String,
    val description: String,
    @DrawableRes val backgroundRes: Int,
    @DrawableRes val buttonBackgroundRes: Int,
    val primaryTextColor: Int,
    val secondaryTextColor: Int,
    val isDarkText: Boolean
) {
    OCEAN_BLUE(
        key = "ocean_blue",
        title = "Azul Océano",
        description = "Degradado hero da app con texto branco",
        backgroundRes = R.drawable.bg_widget_ocean,
        buttonBackgroundRes = R.drawable.bg_widget_button,
        primaryTextColor = 0xFFFFFFFF.toInt(),
        secondaryTextColor = 0xFFE0F2FE.toInt(),
        isDarkText = false
    ),
    DARK_GLASS(
        key = "dark_glass",
        title = "Vidro Escuro",
        description = "Translúcido escuro (60%) con texto branco",
        backgroundRes = R.drawable.bg_widget_dark_glass,
        buttonBackgroundRes = R.drawable.bg_widget_button,
        primaryTextColor = 0xFFFFFFFF.toInt(),
        secondaryTextColor = 0xFFCBD5E1.toInt(),
        isDarkText = false
    ),
    LIGHT_GLASS(
        key = "light_glass",
        title = "Vidro Claro",
        description = "Translúcido claro (60%) con texto negro",
        backgroundRes = R.drawable.bg_widget_light_glass,
        buttonBackgroundRes = R.drawable.bg_widget_button_light,
        primaryTextColor = 0xFF000000.toInt(),
        secondaryTextColor = 0xFF000000.toInt(),
        isDarkText = true
    ),
    DYNAMIC(
        key = "dynamic",
        title = "Dinámico (Meteo)",
        description = "Muda de cor segundo o ceo e a hora",
        backgroundRes = R.drawable.bg_widget_sunny,
        buttonBackgroundRes = R.drawable.bg_widget_button,
        primaryTextColor = 0xFFFFFFFF.toInt(),
        secondaryTextColor = 0xFFBAE6FD.toInt(),
        isDarkText = false
    );

    companion object {
        fun fromKey(key: String?): WidgetThemePreset {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: OCEAN_BLUE
        }
    }
}

class WidgetPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("galicia_weather_widget_prefs", Context.MODE_PRIVATE)

    fun getPreset(): WidgetThemePreset {
        val key = prefs.getString(KEY_WIDGET_PRESET, WidgetThemePreset.OCEAN_BLUE.key)
        return WidgetThemePreset.fromKey(key)
    }

    fun setPreset(preset: WidgetThemePreset) {
        prefs.edit().putString(KEY_WIDGET_PRESET, preset.key).apply()
    }

    fun getWidgetLocationIndex(appWidgetId: Int): Int {
        return prefs.getInt("widget_loc_idx_$appWidgetId", 0)
    }

    fun setWidgetLocationIndex(appWidgetId: Int, index: Int) {
        prefs.edit().putInt("widget_loc_idx_$appWidgetId", index).apply()
    }

    companion object {
        private const val KEY_WIDGET_PRESET = "key_widget_preset"
    }
}

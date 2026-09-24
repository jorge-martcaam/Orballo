package com.example.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.widget.WidgetPreferences
import com.example.data.widget.WidgetThemePreset
import com.example.widget.GaliciaWeatherWidgetProvider

@Composable
fun WidgetThemeDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val widgetPrefs = remember { WidgetPreferences(context.applicationContext) }
    var selectedPreset by remember { mutableStateOf(widgetPrefs.getPreset()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Aspecto do Widget",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Selecciona un preaxuste contrastado para o escritorio:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WidgetThemePreset.entries.forEach { preset ->
                    PresetSelectionCard(
                        preset = preset,
                        isSelected = selectedPreset == preset,
                        onClick = { selectedPreset = preset }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    widgetPrefs.setPreset(selectedPreset)
                    // Trigger immediate refresh broadcast
                    val intent = Intent(context, GaliciaWeatherWidgetProvider::class.java).apply {
                        action = GaliciaWeatherWidgetProvider.ACTION_WIDGET_REFRESH
                    }
                    context.sendBroadcast(intent)
                    Toast.makeText(context, "Aspecto do widget actualizado", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                modifier = Modifier.testTag("btn_save_widget_preset")
            ) {
                Text("Aplicar ao Widget")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun PresetSelectionCard(
    preset: WidgetThemePreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val cardBackground = when (preset) {
        WidgetThemePreset.OCEAN_BLUE -> Brush.verticalGradient(
            listOf(Color(0xFF0284C7), Color(0xFF0369A1))
        )
        WidgetThemePreset.DARK_GLASS -> Brush.verticalGradient(
            listOf(Color(0xFF0F172A), Color(0xFF1E293B))
        )
        WidgetThemePreset.LIGHT_GLASS -> Brush.verticalGradient(
            listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))
        )
    }

    val textColor = if (preset.isDarkText) Color(0xFF0F172A) else Color.White
    val subTextColor = if (preset.isDarkText) Color(0xFF475569) else Color(0xFFE0F2FE)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Miniature preview pill
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(cardBackground)
                    .border(1.dp, if (preset.isDarkText) Color(0x330F172A) else Color(0x33FFFFFF), RoundedCornerShape(10.dp))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "19°",
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "⛅",
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preset.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = preset.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                modifier = Modifier.testTag("radio_preset_${preset.key}")
            )
        }
    }
}

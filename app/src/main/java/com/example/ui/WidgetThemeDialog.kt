package com.example.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
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
import com.example.ui.widget.OrballoDynamicWidget
import com.example.ui.widget.SampleOrballoMockData
import com.example.widget.GaliciaWeatherWidgetProvider

@Composable
fun WidgetThemeDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val widgetPrefs = remember { WidgetPreferences(context.applicationContext) }
    var selectedPreset by remember { mutableStateOf(widgetPrefs.getPreset()) }
    var previewSizeIndex by remember { mutableStateOf(3) } // default: 4x2
    var previewIsDark by remember { mutableStateOf(true) }

    val sizes = listOf("2×1", "2×2", "4×1", "4×2", "4×3")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Aspecto e Deseño de Widget",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Deseño Orballo Atlántico e preaxustes para escritorio:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Selector de tamaño do widget dinámico
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Previsualización Orballo Atlántico",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = previewIsDark,
                            onClick = { previewIsDark = true },
                            label = { Text("Noite", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = !previewIsDark,
                            onClick = { previewIsDark = false },
                            label = { Text("Brétema", fontSize = 11.sp) }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sizes.forEachIndexed { index, label ->
                            FilterChip(
                                selected = previewSizeIndex == index,
                                onClick = { previewSizeIndex = index },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Renderizado do widget dinámico Orballo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (previewIsDark) Color(0xFF0F131C) else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val widgetModifier = when (previewSizeIndex) {
                            0 -> Modifier.width(180.dp).height(65.dp) // 2x1
                            1 -> Modifier.width(180.dp).height(150.dp) // 2x2
                            2 -> Modifier.fillMaxWidth().height(65.dp) // 4x1
                            3 -> Modifier.fillMaxWidth().height(150.dp) // 4x2
                            else -> Modifier.fillMaxWidth().height(230.dp) // 4x3
                        }
                        OrballoDynamicWidget(
                            data = SampleOrballoMockData,
                            isDark = previewIsDark,
                            modifier = widgetModifier
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Preaxuste para o Widget de Escritorio Android",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

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
                    val intent = Intent(context, GaliciaWeatherWidgetProvider::class.java).apply {
                        action = GaliciaWeatherWidgetProvider.ACTION_WIDGET_REFRESH
                    }
                    context.sendBroadcast(intent)
                    Toast.makeText(context, "Aspecto do widget actualizado", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                modifier = Modifier.testTag("btn_save_widget_preset")
            ) {
                Text("Gardar Preaxuste")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Pechar")
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
            listOf(Color(0xA60F172A), Color(0xA61E293B))
        )
        WidgetThemePreset.LIGHT_GLASS -> Brush.verticalGradient(
            listOf(Color(0xA6FFFFFF), Color(0xA6E2E8F0))
        )
        WidgetThemePreset.DYNAMIC -> Brush.verticalGradient(
            listOf(Color(0xFF0284C7), Color(0xFF1E293B))
        )
    }

    val textColor = if (preset.isDarkText) Color.Black else Color.White
    val subTextColor = if (preset.isDarkText) Color.Black else Color(0xFFE0F2FE)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(cardBackground)
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("widget_preset_${preset.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = preset.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Seleccionado",
                            tint = textColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = preset.description,
                    fontSize = 12.sp,
                    color = subTextColor
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
        }
    }
}

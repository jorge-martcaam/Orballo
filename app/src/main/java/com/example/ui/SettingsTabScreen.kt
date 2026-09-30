package com.example.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalOrballoColors
import com.example.ui.theme.OrballoThemeTokens
import com.example.ui.widget.OrballoDynamicWidget
import com.example.widget.GaliciaWeatherWidgetProvider

enum class WidgetPreviewSize(
    val label: String,
    val width: Dp,
    val height: Dp,
    val grid: String
) {
    SIZE_2X1("2×1 Pílula", 200.dp, 85.dp, "130x70 dp"),
    SIZE_2X2("2×2 Cadrado", 200.dp, 160.dp, "160x160 dp"),
    SIZE_4X1("4×1 Fila", 320.dp, 85.dp, "280x70 dp"),
    SIZE_4X2("4×2 Estándar", 320.dp, 165.dp, "280x160 dp"),
    SIZE_4X3("4×3 Completa", 320.dp, 255.dp, "280x250 dp")
}

@Composable
fun SettingsTabScreen(
    paddingValues: PaddingValues,
    viewModel: WeatherViewModel? = null,
    onOpenWidgetManager: () -> Unit = {}
) {
    val colors = LocalOrballoColors.current
    val context = LocalContext.current
    val currentThemeMode = viewModel?.themeMode?.collectAsState()?.value ?: ThemeMode.SYSTEM
    var selectedSize by remember { mutableStateOf(WidgetPreviewSize.SIZE_4X2) }
    var isDarkWidget by remember(currentThemeMode) {
        mutableStateOf(currentThemeMode != ThemeMode.BRETEMA)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .testTag("screen_settings"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Header Card - Orballo Atlántico
        item {
            Card(
                shape = RoundedCornerShape(OrballoThemeTokens.OuterRadius),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        OrballoThemeTokens.SubtleBorderWidth,
                        colors.outlineVariant,
                        RoundedCornerShape(OrballoThemeTokens.OuterRadius)
                    )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Orballo Atlántico",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Text(
                                text = "Deseño sereno, funcional e atlántico",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 1.1 Theme Selection Card (Sistema / Brétema / Noite)
        item {
            Card(
                shape = RoundedCornerShape(OrballoThemeTokens.OuterRadius),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        OrballoThemeTokens.SubtleBorderWidth,
                        colors.outlineVariant,
                        RoundedCornerShape(OrballoThemeTokens.OuterRadius)
                    )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Tema da Aplicación",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Conmutación dinámica entre os modos inspirados no clima galego",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.values().forEach { mode ->
                            val isSelected = currentThemeMode == mode
                            val label = when (mode) {
                                ThemeMode.SYSTEM -> "Sistema"
                                ThemeMode.BRETEMA -> "Brétema (Claro)"
                                ThemeMode.NOITE -> "Noite (Escuro)"
                            }
                            val icon = when (mode) {
                                ThemeMode.SYSTEM -> Icons.Default.CloudSync
                                ThemeMode.BRETEMA -> Icons.Default.LightMode
                                ThemeMode.NOITE -> Icons.Default.DarkMode
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel?.setThemeMode(mode)
                                    isDarkWidget = (mode != ThemeMode.BRETEMA)
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = colors.primary,
                                    selectedLeadingIconColor = colors.primary,
                                    containerColor = colors.surfaceContainer,
                                    labelColor = colors.onSurfaceVariant,
                                    iconColor = colors.onSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) colors.primary else colors.outlineVariant
                                )
                            )
                        }
                    }
                }
            }
        }

        // 2. Widget Preview Section
        item {
            Card(
                shape = RoundedCornerShape(OrballoThemeTokens.OuterRadius),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        OrballoThemeTokens.SubtleBorderWidth,
                        colors.outlineVariant,
                        RoundedCornerShape(OrballoThemeTokens.OuterRadius)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Previsualizador de Widgets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "Widgets dinámicos e adaptativos aos 5 tamaños canónicos",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Size selector chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WidgetPreviewSize.values().forEach { size ->
                            FilterChip(
                                selected = selectedSize == size,
                                onClick = { selectedSize = size },
                                label = { Text(size.label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primary,
                                    selectedLabelColor = Color(0xFF0F172A),
                                    containerColor = colors.surfaceContainer,
                                    labelColor = colors.onSurface
                                ),
                                shape = RoundedCornerShape(OrballoThemeTokens.PillRadius),
                                modifier = Modifier.testTag("chip_size_${size.name}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Theme Mode Selector (Noite / Brétema)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isDarkWidget,
                            onClick = { isDarkWidget = true },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("Noite (Escuro)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = Color(0xFF0F172A),
                                containerColor = colors.surfaceContainer,
                                labelColor = colors.onSurface
                            ),
                            shape = RoundedCornerShape(OrballoThemeTokens.PillRadius),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isDarkWidget,
                            onClick = { isDarkWidget = false },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LightMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("Brétema (Claro)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = Color(0xFF0F172A),
                                containerColor = colors.surfaceContainer,
                                labelColor = colors.onSurface
                            ),
                            shape = RoundedCornerShape(OrballoThemeTokens.PillRadius),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Widget preview stage
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isDarkWidget) Color(0xFF070B12) else Color(0xFFE2E8F0))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OrballoDynamicWidget(
                            isDark = isDarkWidget,
                            modifier = Modifier
                                .width(selectedSize.width)
                                .height(selectedSize.height)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Explanatory note about adaptive resizing
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(OrballoThemeTokens.InnerRadius))
                            .background(colors.surfaceContainer)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "O widget de Orballo é único e adaptativo. Para activar os diferentes formatos (2×1, 2×2, 4×1, 4×2 ou 4×3 coas franxas de mañá, tarde e noite), mantén premido o widget na pantalla de inicio de Android e arrastra os seus tiradores para redimensionalo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val appWidgetManager = AppWidgetManager.getInstance(context)
                                val provider = ComponentName(context, GaliciaWeatherWidgetProvider::class.java)
                                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                                    appWidgetManager.requestPinAppWidget(provider, null, null)
                                } else {
                                    Toast.makeText(context, "O teu lanzador non admite ancorar directamente. Mantén premido na pantalla de inicio para engadilo.", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(context, "Mantén premido na pantalla de inicio para engadir o widget.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_pin_widget"),
                        shape = RoundedCornerShape(OrballoThemeTokens.PillRadius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = if (currentThemeMode == ThemeMode.BRETEMA) Color.White else Color(0xFF0F172A)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Engadir Widget á Pantalla de Inicio",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onOpenWidgetManager,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_open_widget_manager"),
                        shape = RoundedCornerShape(OrballoThemeTokens.PillRadius)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Personalizar Cores e Tema do Widget",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 3. Data Source Auditor Section
        item {
            Card(
                shape = RoundedCornerShape(OrballoThemeTokens.OuterRadius),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        OrballoThemeTokens.SubtleBorderWidth,
                        colors.outlineVariant,
                        RoundedCornerShape(OrballoThemeTokens.OuterRadius)
                    )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = colors.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Auditoría de Fontes Oficiais",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    DataSourceItem(
                        name = "MeteoGalicia Open Data",
                        details = "313 concellos de Galicia • Franxas Mañá, Tarde, Noite • Índice de Calidade do Aire (ICA)",
                        status = "Operativo • Rede oficial da Xunta",
                        statusColor = Color(0xFF22C55E)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DataSourceItem(
                        name = "ECMWF IFS (Integrated Forecasting System)",
                        details = "Modelo numérico europeo de alta resolución • Proxección horaria a 4h e 72h",
                        status = "Operativo • Resolución 0.25°",
                        statusColor = Color(0xFF22C55E)
                    )
                }
            }
        }

        // 4. Units & Standards Section
        item {
            Card(
                shape = RoundedCornerShape(OrballoThemeTokens.OuterRadius),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        OrballoThemeTokens.SubtleBorderWidth,
                        colors.outlineVariant,
                        RoundedCornerShape(OrballoThemeTokens.OuterRadius)
                    )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Unidades de Medida",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    UnitMetricItem(
                        icon = Icons.Default.Thermostat,
                        label = "Temperatura",
                        value = "Graos Celsius (°C)"
                    )
                    UnitMetricItem(
                        icon = Icons.Default.WaterDrop,
                        label = "Precipitación",
                        value = "Milímetros (mm / l·m²)"
                    )
                    UnitMetricItem(
                        icon = Icons.Default.Speed,
                        label = "Vento e refachos",
                        value = "Quilómetros por hora (km/h)"
                    )
                }
            }
        }
    }
}

@Composable
private fun DataSourceItem(
    name: String,
    details: String,
    status: String,
    statusColor: Color
) {
    val colors = LocalOrballoColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OrballoThemeTokens.InnerRadius))
            .background(colors.surfaceContainer)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "En liña",
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = details,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = colors.secondary
        )
    }
}

@Composable
private fun UnitMetricItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    val colors = LocalOrballoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

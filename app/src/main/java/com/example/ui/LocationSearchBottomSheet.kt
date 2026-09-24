package com.example.ui

import java.text.Collator
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.api.NetworkClient
import com.example.data.location.ToggleFavoriteResult
import com.example.data.model.GaliciaLocation
import com.example.ui.theme.OceanSkyBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchBottomSheet(
    selectedLocation: GaliciaLocation,
    defaultLocation: GaliciaLocation,
    favorites: List<GaliciaLocation>,
    onLocationSelected: (GaliciaLocation) -> Unit,
    onSetDefault: (GaliciaLocation) -> Unit,
    onToggleFavorite: (GaliciaLocation) -> ToggleFavoriteResult,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    var selectedProvince by remember { mutableStateOf("Todas") }
    var limitReachedMessage by remember { mutableStateOf<String?>(null) }
    var remoteLocations by remember { mutableStateOf<List<GaliciaLocation>>(emptyList()) }
    var isSearchingRemote by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(searchQuery, selectedProvince) {
        val trimmed = searchQuery.trim()
        if (trimmed.length < 2 || selectedProvince != "Todas") {
            remoteLocations = emptyList()
            isSearchingRemote = false
            return@LaunchedEffect
        }
        delay(350)
        isSearchingRemote = true
        try {
            val response = NetworkClient.geocodingApi.searchLocations(
                name = trimmed,
                count = 10,
                language = "gl,es"
            )
            val rawResults = response.results.orEmpty()
            val deduplicated = mutableListOf<GaliciaLocation>()
            for (res in rawResults) {
                // If it is in Galicia, skip it to avoid duplication with the 313 Concellos catalog
                val galicianMatch = GaliciaLocation.findMatchingGalicianConcello(
                    name = res.name,
                    lat = res.latitude,
                    lon = res.longitude,
                    admin1 = res.admin1,
                    admin2 = res.admin2
                )
                if (galicianMatch != null) {
                    continue
                }
                val prov = res.admin1 ?: res.country ?: "España"
                val country = res.country ?: "España"
                if (deduplicated.none { it.name.equals(res.name, ignoreCase = true) && it.province.equals(prov, ignoreCase = true) }) {
                    deduplicated.add(
                        GaliciaLocation(
                            name = res.name,
                            province = prov,
                            latitude = res.latitude,
                            longitude = res.longitude,
                            isGps = false,
                            concelloId = -1,
                            country = country,
                            isGalicia = false
                        )
                    )
                }
            }
            remoteLocations = deduplicated
        } catch (_: Exception) {
            remoteLocations = emptyList()
        } finally {
            isSearchingRemote = false
        }
    }

    val collator = remember { Collator.getInstance(Locale("gl", "ES")).apply { strength = Collator.SECONDARY } }
    val filteredLocations = remember(searchQuery, selectedProvince, favorites) {
        val base = GaliciaLocation.search(searchQuery, selectedProvince)
        base.sortedWith { a, b ->
            val aFav = favorites.any { it.name == a.name }
            val bFav = favorites.any { it.name == b.name }
            if (aFav && !bFav) {
                -1
            } else if (!aFav && bFav) {
                1
            } else {
                collator.compare(a.name, b.name)
            }
        }
    }

    val favoritesInList = remember(filteredLocations, favorites) {
        filteredLocations.filter { loc -> favorites.any { it.name == loc.name } }
    }
    val nonFavoritesInList = remember(filteredLocations, favorites) {
        filteredLocations.filter { loc -> favorites.none { it.name == loc.name } }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = Modifier.testTag("location_search_bottom_sheet"),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Buscar localización",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "313 concellos de Galicia (offline) e busca global de cidades",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.testTag("btn_close_location_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Pechar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search text field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    limitReachedMessage = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("location_search_input"),
                placeholder = { Text("Buscar concello ou cidade (ex: Vigo, Madrid, París...)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpar busca"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Province filter chips
            val provinceScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(provinceScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GaliciaLocation.PROVINCES.forEach { province ->
                    val isSelected = selectedProvince == province
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedProvince = province
                            limitReachedMessage = null
                        },
                        label = { Text(province) },
                        modifier = Modifier.testTag("chip_province_$province"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OceanSkyBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Limit Reached Warning Banner
            if (limitReachedMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("banner_limit_reached"),
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = limitReachedMessage ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF991B1B),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { limitReachedMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Pechar aviso",
                                tint = Color(0xFF991B1B),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Favorites quick bar inside sheet
            if (favorites.isNotEmpty() && searchQuery.isEmpty() && selectedProvince == "Todas") {
                Text(
                    text = "Favoritos gardados (${favorites.size}/6):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                val favScroll = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(favScroll),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    favorites.forEach { favLoc ->
                        val isCurrent = !selectedLocation.isGps && selectedLocation.name == favLoc.name
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCurrent) OceanSkyBlue else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable {
                                    onLocationSelected(favLoc)
                                    onDismissRequest()
                                }
                                .testTag("sheet_fav_chip_${favLoc.name.replace(" ", "_")}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (isCurrent) Color(0xFFFEF08A) else Color(0xFFEAB308),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = favLoc.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider()

            // List of concellos and external cities
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .testTag("location_results_list"),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                if (filteredLocations.isEmpty() && remoteLocations.isEmpty() && !isSearchingRemote) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Non se atoparon localizacións con «$searchQuery»",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    if (favoritesInList.isNotEmpty()) {
                        item {
                            Text(
                                text = "⭐ Favoritos gardados (${favoritesInList.size})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                            )
                        }
                        items(favoritesInList, key = { "fav_${it.name}" }) { loc ->
                            val isSelected = !selectedLocation.isGps && selectedLocation.name == loc.name
                            val isDefault = !loc.isGps && defaultLocation.name == loc.name

                            ConcelloResultRow(
                                location = loc,
                                isSelected = isSelected,
                                isFavorite = true,
                                isDefault = isDefault,
                                onSelect = {
                                    onLocationSelected(loc)
                                    onDismissRequest()
                                },
                                onToggleFav = {
                                    val result = onToggleFavorite(loc)
                                    if (result == ToggleFavoriteResult.LIMIT_REACHED) {
                                        val msg = "Acadaches o límite de 6 favoritos. Desmarca algún concello primeiro."
                                        limitReachedMessage = msg
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                    } else {
                                        limitReachedMessage = null
                                    }
                                },
                                onMakeDefault = { onSetDefault(loc) }
                            )
                        }
                    }

                    if (nonFavoritesInList.isNotEmpty()) {
                        item {
                            if (favoritesInList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Text(
                                text = if (favoritesInList.isNotEmpty()) "Concellos de Galicia (A-Z)" else "Concellos de Galicia (313)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp, top = 6.dp, bottom = 4.dp)
                            )
                        }
                        items(nonFavoritesInList, key = { "concello_${it.name}" }) { loc ->
                            val isSelected = !selectedLocation.isGps && selectedLocation.name == loc.name
                            val isDefault = !loc.isGps && defaultLocation.name == loc.name

                            ConcelloResultRow(
                                location = loc,
                                isSelected = isSelected,
                                isFavorite = false,
                                isDefault = isDefault,
                                onSelect = {
                                    onLocationSelected(loc)
                                    onDismissRequest()
                                },
                                onToggleFav = {
                                    val result = onToggleFavorite(loc)
                                    if (result == ToggleFavoriteResult.LIMIT_REACHED) {
                                        val msg = "Acadaches o límite de 6 favoritos. Desmarca algún concello primeiro."
                                        limitReachedMessage = msg
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                    } else {
                                        limitReachedMessage = null
                                    }
                                },
                                onMakeDefault = { onSetDefault(loc) }
                            )
                        }
                    }

                    if (isSearchingRemote) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Buscando noutras cidades de España e do mundo...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (remoteLocations.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "🌍 Outras cidades (España e Global)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 8.dp, top = 6.dp, bottom = 4.dp)
                            )
                        }
                        items(remoteLocations, key = { "remote_${it.name}_${it.latitude}_${it.longitude}" }) { loc ->
                            val isSelected = !selectedLocation.isGps && selectedLocation.name == loc.name
                            val isDefault = !loc.isGps && defaultLocation.name == loc.name
                            val isFav = favorites.any { it.name == loc.name }

                            ConcelloResultRow(
                                location = loc,
                                isSelected = isSelected,
                                isFavorite = isFav,
                                isDefault = isDefault,
                                onSelect = {
                                    onLocationSelected(loc)
                                    onDismissRequest()
                                },
                                onToggleFav = {
                                    val result = onToggleFavorite(loc)
                                    if (result == ToggleFavoriteResult.LIMIT_REACHED) {
                                        val msg = "Acadaches o límite de 6 favoritos. Desmarca algún concello primeiro."
                                        limitReachedMessage = msg
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                    } else {
                                        limitReachedMessage = null
                                    }
                                },
                                onMakeDefault = { onSetDefault(loc) }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.testTag("snackbar_limit_reached")
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ConcelloResultRow(
    location: GaliciaLocation,
    isSelected: Boolean,
    isFavorite: Boolean,
    isDefault: Boolean,
    onSelect: () -> Unit,
    onToggleFav: () -> Unit,
    onMakeDefault: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
            .background(
                if (isSelected) OceanSkyBlue.copy(alpha = 0.12f) else Color.Transparent
            )
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .testTag("concello_item_${location.name.replace(" ", "_")}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) OceanSkyBlue else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.Check else if (location.isGalicia) Icons.Default.LocationOn else Icons.Default.Public,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isDefault) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFEF08A)
                    ) {
                        Text(
                            text = "Predeterminada",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF713F12),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                if (!location.isGalicia) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE0F2FE)
                    ) {
                        Text(
                            text = if (location.country == "España") "España" else location.country,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF0369A1),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            Text(
                text = if (location.isGalicia) location.province else "${location.province} · ${location.country}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Default location shortcut
        if (!isDefault) {
            IconButton(
                onClick = onMakeDefault,
                modifier = Modifier.testTag("btn_set_default_${location.name.replace(" ", "_")}")
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Fixar como predeterminada",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Favorite toggle button
        IconButton(
            onClick = onToggleFav,
            modifier = Modifier.testTag("star_favorite_${location.name.replace(" ", "_")}")
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = if (isFavorite) "Quitar de favoritos" else "Engadir a favoritos",
                tint = if (isFavorite) Color(0xFFEAB308) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

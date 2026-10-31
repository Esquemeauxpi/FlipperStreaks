package com.flipperstreaks.app.ui.nearby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flipperstreaks.app.ui.components.LocationRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(
    onLocationClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NearbyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val fallbackLat = 45.5231
    val fallbackLon = -122.6765

    LaunchedEffect(Unit) {
        viewModel.loadNearby(fallbackLat, fallbackLon)
    }

    val sortedLocations = when (uiState.sort) {
        NearbySort.DISTANCE ->
            uiState.locations.sortedBy { it.distanceMiles ?: Double.MAX_VALUE }
        NearbySort.NEW_FOR_YOU -> uiState.locations.sortedByDescending {
            val played = uiState.playedByLocation[it.id]?.size ?: 0
            (it.machineCount - played)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Nearby") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Sort chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Sort,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                FilterChip(
                    selected = uiState.sort == NearbySort.DISTANCE,
                    onClick = { viewModel.setSort(NearbySort.DISTANCE) },
                    label = { Text("Nearest") }
                )
                FilterChip(
                    selected = uiState.sort == NearbySort.NEW_FOR_YOU,
                    onClick = { viewModel.setSort(NearbySort.NEW_FOR_YOU) },
                    label = { Text("Most new to you") }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.locations.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    uiState.error != null && uiState.locations.isEmpty() -> {
                        EmptyState(
                            icon = Icons.Filled.LocationOff,
                            title = "Can't find nearby spots",
                            message = uiState.error ?: "Check your connection and try again."
                        )
                    }
                    uiState.locations.isEmpty() -> {
                        EmptyState(
                            icon = Icons.Filled.LocationOn,
                            title = "No locations yet",
                            message = "Try searching a different city from the Search tab."
                        )
                    }
                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(sortedLocations, key = { it.id }) { location ->
                                LocationRow(
                                    location = location,
                                    onClick = { onLocationClick(location.id, location.name) },
                                    playedMachineCount =
                                        uiState.playedByLocation[location.id]?.size ?: 0
                                )
                            }
                            item { Spacer(Modifier.height(16.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String
) {
    Column(
        modifier = Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}


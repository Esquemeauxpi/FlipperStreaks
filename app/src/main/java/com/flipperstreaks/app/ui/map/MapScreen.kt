package com.flipperstreaks.app.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onLocationClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    val annotationManager = remember { mutableStateOf<PointAnnotationManager?>(null) }
    val clickTargets = remember { mutableMapOf<String, Pair<Int, String>>() }
    // Labels appear only when zoomed in past this threshold; below it the map
    // shows just colored dots so dense areas (downtown) stay legible.
    val labelZoomThreshold = 14.0
    var showLabels by remember { mutableStateOf(false) }

    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(uiState.center.lon, uiState.center.lat))
                            .zoom(12.0)
                            .build()
                    )
                    mapboxMap.subscribeCameraChanged {
                        val z = mapboxMap.cameraState.zoom
                        val next = z >= labelZoomThreshold
                        if (next != showLabels) showLabels = next
                    }
                    mapboxMap.loadStyle(Style.MAPBOX_STREETS) { style ->
                        val playedBitmap = makeCircle(0xFF4CAF50.toInt(), 36)
                        val unplayedBitmap = makeCircle(0xFFE53935.toInt(), 36)
                        style.addImage("pin-played", playedBitmap)
                        style.addImage("pin-unplayed", unplayedBitmap)

                        val mgr = annotations.createPointAnnotationManager()
                        // Keep every dot visible, but let Mapbox cull labels that would collide.
                        // Result: at wide zooms only the labels with room to breathe show;
                        // as you zoom in, more names appear as space opens up.
                        mgr.iconAllowOverlap = true
                        mgr.textAllowOverlap = false
                        mgr.textOptional = true
                        mgr.addClickListener(OnPointAnnotationClickListener { annotation ->
                            val target = clickTargets[annotation.id]
                            if (target != null) onLocationClick(target.first, target.second)
                            true
                        })
                        annotationManager.value = mgr
                    }
                    mapViewRef.value = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Search bar overlay
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 4.dp,
            shadowElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search a city…") },
                    leadingIcon = {
                        if (uiState.isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(12.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        }
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        viewModel.searchCity(query)
                        focusManager.clearFocus()
                    }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (uiState.searchError != null) {
                    Text(
                        uiState.searchError ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Zoom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { mapViewRef.value?.zoomBy(1.0) },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) { Icon(Icons.Filled.Add, contentDescription = "Zoom in") }
            FloatingActionButton(
                onClick = { mapViewRef.value?.zoomBy(-1.0) },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) { Icon(Icons.Filled.Remove, contentDescription = "Zoom out") }
        }

        // Refresh-this-area pill button, bottom-center
        Button(
            onClick = {
                val center = mapViewRef.value?.mapboxMap?.cameraState?.center ?: return@Button
                viewModel.refreshArea(center.latitude(), center.longitude())
            },
            enabled = !uiState.isSearching,
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            if (uiState.isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text("Refresh this area")
        }
    }

    // Move camera when a search picks a new center
    LaunchedEffect(uiState.cameraMoveNonce) {
        if (uiState.cameraMoveNonce == 0) return@LaunchedEffect
        mapViewRef.value?.mapboxMap?.setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(uiState.center.lon, uiState.center.lat))
                .zoom(12.0)
                .build()
        )
    }

    // Redraw pins whenever locations, played map, label visibility, or the manager becomes ready
    LaunchedEffect(uiState.locations, uiState.playedByLocation, showLabels, annotationManager.value) {
        val mgr = annotationManager.value ?: return@LaunchedEffect
        mgr.deleteAll()
        clickTargets.clear()

        uiState.locations.forEach { location ->
            val playedCount = uiState.playedByLocation[location.id]?.size ?: 0
            val played = playedCount > 0
            val opts = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(location.longitude, location.latitude))
                .withIconImage(if (played) "pin-played" else "pin-unplayed")
                .withIconSize(1.0)
            if (showLabels) {
                opts.withTextField(location.name)
                    .withTextSize(11.0)
                    .withTextOffset(listOf(0.0, 1.6))
                    .withTextColor("#212121")
                    .withTextHaloColor("#FFFFFF")
                    .withTextHaloWidth(1.5)
            }
            val annotation = mgr.create(opts)
            clickTargets[annotation.id] = Pair(location.id, location.name)
        }
    }
}

private fun MapView.zoomBy(delta: Double) {
    val currentZoom = mapboxMap.cameraState.zoom
    val target = (currentZoom + delta).coerceIn(2.0, 20.0)
    mapboxMap.setCamera(CameraOptions.Builder().zoom(target).build())
}

private fun makeCircle(@ColorInt color: Int, sizePx: Int): Bitmap {
    val bm = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bm)
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    val r = sizePx / 2f
    canvas.drawCircle(r, r, r, fill)
    canvas.drawCircle(r, r, r - 2f, border)
    return bm
}

package com.flipperstreaks.app.ui.map

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flipperstreaks.app.data.repository.PlayLogRepository
import com.flipperstreaks.app.domain.model.Location
import com.flipperstreaks.app.domain.usecase.GetNearbyLocationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

data class MapCenter(val lat: Double, val lon: Double, val label: String? = null)

data class MapUiState(
    val locations: List<Location> = emptyList(),
    val playedByLocation: Map<Int, Set<Int>> = emptyMap(),
    val center: MapCenter = MapCenter(45.5231, -122.6765, "Portland"),
    val isSearching: Boolean = false,
    val searchError: String? = null,
    // Increments each time center is changed by search — screen listens to know when to move camera
    val cameraMoveNonce: Int = 0
) {
    val playedLocationIds: Set<Int> get() = playedByLocation.keys
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getNearbyLocationsUseCase: GetNearbyLocationsUseCase,
    private val playLogRepository: PlayLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val centerFlow = MutableStateFlow(_uiState.value.center)

    init {
        // Locations flow reacts to center changes
        viewModelScope.launch {
            combine(
                centerFlow.flatMapLatest { c ->
                    getNearbyLocationsUseCase.observe(c.lat, c.lon)
                },
                playLogRepository.observePlayedMachineIdsByLocation()
            ) { locations, playedMap ->
                _uiState.value = _uiState.value.copy(
                    locations = locations,
                    playedByLocation = playedMap
                )
            }.collect { }
        }
        viewModelScope.launch {
            val c = centerFlow.value
            runCatching { getNearbyLocationsUseCase.refresh(c.lat, c.lon) }
        }
    }

    fun searchCity(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, searchError = null)
            val coords = geocodeQuery(trimmed)
            if (coords == null) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    searchError = "Couldn't find \"$trimmed\"."
                )
                return@launch
            }
            val (lat, lon) = coords
            runCatching { getNearbyLocationsUseCase.refresh(lat, lon) }
            val newCenter = MapCenter(lat, lon, trimmed)
            centerFlow.value = newCenter
            _uiState.value = _uiState.value.copy(
                center = newCenter,
                cameraMoveNonce = _uiState.value.cameraMoveNonce + 1,
                isSearching = false
            )
        }
    }

    fun clearSearchError() {
        _uiState.value = _uiState.value.copy(searchError = null)
    }

    fun refreshArea(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, searchError = null)
            runCatching { getNearbyLocationsUseCase.refresh(lat, lon) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        searchError = "Couldn't refresh this area."
                    )
                }
            centerFlow.value = MapCenter(lat, lon, null)
            _uiState.value = _uiState.value.copy(
                center = MapCenter(lat, lon, null),
                isSearching = false
            )
        }
    }

    private suspend fun geocodeQuery(query: String): Pair<Double, Double>? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context)
        return if (Build.VERSION.SDK_INT >= 33) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocationName(query, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<android.location.Address>) {
                        cont.resume(
                            addresses.firstOrNull()?.let { Pair(it.latitude, it.longitude) }
                        )
                    }
                    override fun onError(errorMessage: String?) { cont.resume(null) }
                })
            }
        } else {
            withContext(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(query, 1)
                    ?.firstOrNull()
                    ?.let { Pair(it.latitude, it.longitude) }
            }
        }
    }
}

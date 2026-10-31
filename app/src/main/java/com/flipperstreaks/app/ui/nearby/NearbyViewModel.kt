package com.flipperstreaks.app.ui.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flipperstreaks.app.data.repository.PlayLogRepository
import com.flipperstreaks.app.domain.model.Location
import com.flipperstreaks.app.domain.usecase.GetNearbyLocationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NearbySort { DISTANCE, NEW_FOR_YOU }

data class NearbyUiState(
    val locations: List<Location> = emptyList(),
    val playedByLocation: Map<Int, Set<Int>> = emptyMap(),
    val sort: NearbySort = NearbySort.DISTANCE,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NearbyViewModel @Inject constructor(
    private val getNearbyLocations: GetNearbyLocationsUseCase,
    private val playLogRepository: PlayLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NearbyUiState())
    val uiState: StateFlow<NearbyUiState> = _uiState.asStateFlow()

    fun loadNearby(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                getNearbyLocations.refresh(lat, lon)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Couldn't load nearby locations."
                )
            }
            combine(
                getNearbyLocations.observe(lat, lon),
                playLogRepository.observePlayedMachineIdsByLocation()
            ) { locations, playedMap ->
                _uiState.value.copy(
                    locations = locations,
                    playedByLocation = playedMap,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    fun setSort(sort: NearbySort) {
        _uiState.value = _uiState.value.copy(sort = sort)
    }
}

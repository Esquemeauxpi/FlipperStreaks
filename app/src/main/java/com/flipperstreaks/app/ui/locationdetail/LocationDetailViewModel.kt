package com.flipperstreaks.app.ui.locationdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flipperstreaks.app.data.repository.AuthRepository
import com.flipperstreaks.app.data.repository.LocationRepository
import com.flipperstreaks.app.data.repository.PlayLogRepository
import com.flipperstreaks.app.domain.model.Location
import com.flipperstreaks.app.domain.model.Machine
import com.flipperstreaks.app.domain.usecase.LogPlayUseCase
import com.flipperstreaks.app.domain.usecase.SubmitMachineCommentUseCase
import com.flipperstreaks.app.domain.usecase.WriteResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocationDetailUiState(
    val locationName: String = "",
    val location: Location? = null,
    val machines: List<Machine> = emptyList(),
    val isLoading: Boolean = false,
    // Machines the user has EVER played (persisted). Session toggles land here too.
    val playedMachineIds: Set<Int> = emptySet(),
    // machineId -> most recent play epoch millis (across all locations)
    val lastPlayedByMachine: Map<Int, Long> = emptyMap(),
    // Machine currently having its "Leave a note" sheet open, or null.
    val commentingMachine: Machine? = null,
    val isSubmittingComment: Boolean = false
)

@HiltViewModel
class LocationDetailViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val playLogRepository: PlayLogRepository,
    private val logPlayUseCase: LogPlayUseCase,
    private val submitMachineComment: SubmitMachineCommentUseCase,
    authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationDetailUiState())
    val uiState: StateFlow<LocationDetailUiState> = _uiState.asStateFlow()

    val isConnected: StateFlow<Boolean> = authRepository.isConnected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Session-scoped unselects: machine IDs the user tapped to "unplay" during
    // this visit. We don't delete DB rows, but visually we honor the toggle
    // for this session so the +/checkmark responds to the tap.
    private val sessionUnselected = mutableSetOf<Int>()

    fun load(locationId: Int, locationName: String) {
        _uiState.value = _uiState.value.copy(locationName = locationName, isLoading = true)
        viewModelScope.launch {
            val cached = locationRepository.getCachedLocation(locationId)
            if (cached != null) _uiState.value = _uiState.value.copy(location = cached)
            locationRepository.refreshMachinesForLocation(locationId)
            combine(
                locationRepository.observeMachinesFor(locationId),
                playLogRepository.observePlayedMachineIdsByLocation(),
                playLogRepository.observeLastPlayedByMachine()
            ) { machines, playedByLocation, lastPlayed ->
                val persistedPlayed = playedByLocation[locationId] ?: emptySet()
                val effectivePlayed = persistedPlayed - sessionUnselected
                _uiState.value.copy(
                    machines = machines,
                    playedMachineIds = effectivePlayed,
                    lastPlayedByMachine = lastPlayed,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    fun onPlayLogged(machine: Machine) {
        val alreadyPlayed = machine.id in _uiState.value.playedMachineIds
        viewModelScope.launch {
            if (alreadyPlayed) {
                sessionUnselected += machine.id
                _uiState.value = _uiState.value.copy(
                    playedMachineIds = _uiState.value.playedMachineIds - machine.id
                )
                _toastEvent.emit("${machine.name} unselected as 'played'")
            } else {
                sessionUnselected -= machine.id
                logPlayUseCase(machine, _uiState.value.locationName)
                _toastEvent.emit("${machine.name} has been played")
            }
        }
    }

    fun openCommentSheet(machine: Machine) {
        _uiState.value = _uiState.value.copy(commentingMachine = machine)
    }

    fun dismissCommentSheet() {
        _uiState.value = _uiState.value.copy(commentingMachine = null, isSubmittingComment = false)
    }

    fun submitComment(comment: String) {
        val locationId = _uiState.value.location?.id ?: return
        val machine = _uiState.value.commentingMachine ?: return
        val trimmed = comment.trim()
        if (trimmed.isEmpty()) return
        _uiState.value = _uiState.value.copy(isSubmittingComment = true)
        viewModelScope.launch {
            val result = submitMachineComment(locationId, machine.id, trimmed)
            _uiState.value = _uiState.value.copy(isSubmittingComment = false)
            when (result) {
                is WriteResult.Success -> {
                    _uiState.value = _uiState.value.copy(commentingMachine = null)
                    _toastEvent.emit("Comment posted to ${machine.name}.")
                }
                is WriteResult.RequiresAuth ->
                    _toastEvent.emit("Log in to Pinball Map to leave a comment.")
                is WriteResult.Error ->
                    _toastEvent.emit(result.message)
            }
        }
    }
}

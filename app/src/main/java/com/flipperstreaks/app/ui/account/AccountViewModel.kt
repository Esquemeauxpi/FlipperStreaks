package com.flipperstreaks.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flipperstreaks.app.data.local.datastore.PinballMapToken
import com.flipperstreaks.app.data.repository.AuthRepository
import com.flipperstreaks.app.domain.model.StreakSummary
import com.flipperstreaks.app.domain.usecase.CalculateStreaksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    calculateStreaks: CalculateStreaksUseCase
) : ViewModel() {

    val identity: StateFlow<PinballMapToken?> = authRepository.identity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val summary: StateFlow<StreakSummary?> = calculateStreaks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun disconnect() {
        viewModelScope.launch { authRepository.logout() }
    }
}

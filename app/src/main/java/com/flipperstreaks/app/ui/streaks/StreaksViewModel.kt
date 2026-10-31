package com.flipperstreaks.app.ui.streaks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flipperstreaks.app.domain.model.StreakSummary
import com.flipperstreaks.app.domain.usecase.CalculateStreaksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StreaksViewModel @Inject constructor(
    calculateStreaks: CalculateStreaksUseCase
) : ViewModel() {

    val streakSummary: StateFlow<StreakSummary?> = calculateStreaks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

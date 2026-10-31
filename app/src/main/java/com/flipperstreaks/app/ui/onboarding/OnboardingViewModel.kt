package com.flipperstreaks.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flipperstreaks.app.data.local.datastore.OnboardingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {

    // null = still checking DataStore. true/false once known. Kept nullable
    // so the screen can avoid flashing the landing UI for returning users
    // (see OnboardingScreen's LaunchedEffect).
    private val _hasCompletedOnboarding = MutableStateFlow<Boolean?>(null)
    val hasCompletedOnboarding: StateFlow<Boolean?> = _hasCompletedOnboarding.asStateFlow()

    init {
        viewModelScope.launch {
            onboardingPreferences.hasCompletedOnboarding.collect { completed ->
                _hasCompletedOnboarding.value = completed
            }
        }
    }

    fun markCompleted() {
        viewModelScope.launch { onboardingPreferences.markCompleted() }
    }
}

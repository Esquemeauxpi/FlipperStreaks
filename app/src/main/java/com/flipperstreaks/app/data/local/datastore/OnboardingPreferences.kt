package com.flipperstreaks.app.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding_prefs")

// Tracks whether the user has been through the "Find pinball nearby" landing
// screen once. Separate from AuthTokenStore on purpose — this is a plain,
// non-sensitive UI flag, not a credential, so it doesn't need encryption.
@Singleton
class OnboardingPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val hasCompletedKey = booleanPreferencesKey("has_completed_onboarding")

    val hasCompletedOnboarding: Flow<Boolean> = context.onboardingDataStore.data
        .map { prefs -> prefs[hasCompletedKey] ?: false }

    suspend fun markCompleted() {
        context.onboardingDataStore.edit { prefs -> prefs[hasCompletedKey] = true }
    }
}

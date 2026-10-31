package com.flipperstreaks.app.data.local.datastore

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class PinballMapToken(val email: String, val authToken: String, val username: String?)

// Stores the Pinball Map auth token in EncryptedSharedPreferences.
// Absence of a token IS a valid, expected app state — most users
// will never populate this. See ui/login and domain/usecase/WriteResult.
@Singleton
class AuthTokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "flipper_streaks_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Reactive mirror of the stored token — collectors get updates on save()/clear().
    private val _token = MutableStateFlow(readFromPrefs())
    val token: StateFlow<PinballMapToken?> = _token.asStateFlow()

    private fun readFromPrefs(): PinballMapToken? {
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val authToken = prefs.getString(KEY_TOKEN, null) ?: return null
        val username = prefs.getString(KEY_USERNAME, null)
        return PinballMapToken(email, authToken, username)
    }

    suspend fun currentToken(): PinballMapToken? = _token.value

    suspend fun save(token: PinballMapToken) {
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putString(KEY_EMAIL, token.email)
                .putString(KEY_TOKEN, token.authToken)
                .putString(KEY_USERNAME, token.username)
                .apply()
        }
        _token.value = token
    }

    suspend fun clear() {
        withContext(Dispatchers.IO) {
            prefs.edit().clear().apply()
        }
        _token.value = null
    }

    private companion object {
        const val KEY_EMAIL = "pbm_email"
        const val KEY_TOKEN = "pbm_token"
        const val KEY_USERNAME = "pbm_username"
    }
}

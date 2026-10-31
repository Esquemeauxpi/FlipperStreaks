package com.flipperstreaks.app.data.repository

import com.flipperstreaks.app.data.local.datastore.AuthTokenStore
import com.flipperstreaks.app.data.local.datastore.PinballMapToken
import com.flipperstreaks.app.data.remote.PinballMapApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

sealed interface LoginResult {
    data object Success : LoginResult
    data class Failure(val message: String) : LoginResult
}

// Owns the optional connect/disconnect lifecycle for a Pinball Map account.
// This is entirely separate from app usage — see PlayLogRepository, which
// has no dependency on this class at all.
@Singleton
class AuthRepository @Inject constructor(
    private val api: PinballMapApi,
    private val tokenStore: AuthTokenStore
) {
    val identity: Flow<PinballMapToken?> = tokenStore.token
    val isConnected: Flow<Boolean> = tokenStore.token.map { it != null }

    suspend fun login(email: String, password: String): LoginResult {
        return try {
            val details = api.getAuthDetails(email, password)
            tokenStore.save(
                PinballMapToken(
                    email = details.userEmail,
                    authToken = details.authenticationToken,
                    username = details.username
                )
            )
            LoginResult.Success
        } catch (e: retrofit2.HttpException) {
            LoginResult.Failure(
                if (e.code() == 401 || e.code() == 404) "Wrong email or password."
                else "Login failed (${e.code()}). Try again in a moment."
            )
        } catch (e: Exception) {
            LoginResult.Failure(e.message ?: "Couldn't reach Pinball Map. Check your connection.")
        }
    }

    suspend fun logout() = tokenStore.clear()

    suspend fun connectedUsername(): String? = tokenStore.currentToken()?.username
}

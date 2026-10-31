package com.flipperstreaks.app.domain.usecase

import com.flipperstreaks.app.data.remote.PinballMapApi
import com.flipperstreaks.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import javax.inject.Inject

// A write action that requires an opted-in Pinball Map connection.
// The sealed WriteResult lets the UI distinguish "needs login" from
// a genuine network/server error, so it can show the landing sheet
// from ui/login rather than a generic error toast.
sealed interface WriteResult {
    data object Success : WriteResult
    data object RequiresAuth : WriteResult
    data class Error(val message: String) : WriteResult
}

class SubmitMachineCommentUseCase @Inject constructor(
    private val api: PinballMapApi,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(locationId: Int, machineId: Int, comment: String): WriteResult {
        if (!authRepository.isConnected.first()) {
            return WriteResult.RequiresAuth
        }
        return try {
            api.submitMachineComment(locationId, machineId, comment)
            WriteResult.Success
        } catch (e: HttpException) {
            if (e.code() == 401) WriteResult.RequiresAuth
            else WriteResult.Error(e.message ?: "Something went wrong submitting that comment.")
        } catch (e: Exception) {
            WriteResult.Error(e.message ?: "Something went wrong submitting that comment.")
        }
    }
}

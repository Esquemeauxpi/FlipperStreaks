package com.flipperstreaks.app.domain.usecase

import com.flipperstreaks.app.data.repository.PlayLogRepository
import com.flipperstreaks.app.domain.model.StreakSummary
import com.flipperstreaks.app.domain.streaks.StreakEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CalculateStreaksUseCase @Inject constructor(
    private val playLogRepository: PlayLogRepository
) {
    operator fun invoke(): Flow<StreakSummary> =
        playLogRepository.observePlayLogs().map { StreakEngine.compute(it) }
}

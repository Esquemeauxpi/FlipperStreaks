package com.flipperstreaks.app.domain.usecase

import com.flipperstreaks.app.data.repository.PlayLogRepository
import com.flipperstreaks.app.domain.model.Machine
import javax.inject.Inject

class LogPlayUseCase @Inject constructor(
    private val playLogRepository: PlayLogRepository
) {
    suspend operator fun invoke(machine: Machine, locationName: String) {
        playLogRepository.logPlay(
            machineId = machine.id,
            machineName = machine.name,
            manufacturer = machine.manufacturer,
            year = machine.year,
            locationId = machine.locationId,
            locationName = locationName
        )
    }
}

package com.flipperstreaks.app.data.repository

import com.flipperstreaks.app.data.local.db.dao.PlayLogDao
import com.flipperstreaks.app.data.local.db.entity.PlayLogEntity
import com.flipperstreaks.app.domain.model.PlayLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// IMPORTANT: this repository never touches PinballMapApi or AuthTokenStore.
// Logging a play is a pure local write — this is what makes login optional
// at the architecture level, not just in the UI. See ui/login for the
// separate, opt-in flow that unlocks write access to the real map.
@Singleton
class PlayLogRepository @Inject constructor(
    private val playLogDao: PlayLogDao
) {
    suspend fun logPlay(
        machineId: Int,
        machineName: String,
        manufacturer: String?,
        year: Int?,
        locationId: Int,
        locationName: String
    ) {
        playLogDao.insert(
            PlayLogEntity(
                machineId = machineId,
                machineName = machineName,
                manufacturer = manufacturer,
                year = year,
                locationId = locationId,
                locationName = locationName,
                timestampEpochMillis = System.currentTimeMillis()
            )
        )
    }

    fun observePlayLogs(): Flow<List<PlayLog>> =
        playLogDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    fun observeUniqueMachineCount(): Flow<Int> = playLogDao.observeUniqueMachineCount()

    fun observeUniqueLocationCount(): Flow<Int> = playLogDao.observeUniqueLocationCount()

    // locationId -> set of machineIds ever played at that location
    fun observePlayedMachineIdsByLocation(): Flow<Map<Int, Set<Int>>> =
        playLogDao.observeAll().map { entities ->
            entities.groupBy { it.locationId }
                .mapValues { (_, rows) -> rows.map { it.machineId }.toSet() }
        }

    // machineId -> most recent play epoch millis (across any location)
    fun observeLastPlayedByMachine(): Flow<Map<Int, Long>> =
        playLogDao.observeAll().map { entities ->
            entities.groupBy { it.machineId }
                .mapValues { (_, rows) -> rows.maxOf { it.timestampEpochMillis } }
        }

    private fun PlayLogEntity.toDomain() = PlayLog(
        id = id,
        machineId = machineId,
        machineName = machineName,
        manufacturer = manufacturer,
        year = year,
        locationId = locationId,
        locationName = locationName,
        timestampEpochMillis = timestampEpochMillis
    )
}

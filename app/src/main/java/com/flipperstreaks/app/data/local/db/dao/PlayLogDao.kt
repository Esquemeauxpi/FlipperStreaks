package com.flipperstreaks.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.flipperstreaks.app.data.local.db.entity.PlayLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayLogDao {

    @Insert
    suspend fun insert(entry: PlayLogEntity)

    @Query("SELECT * FROM play_logs ORDER BY timestampEpochMillis DESC")
    fun observeAll(): Flow<List<PlayLogEntity>>

    @Query("SELECT COUNT(*) FROM play_logs")
    suspend fun totalPlayCount(): Int

    @Query("SELECT COUNT(DISTINCT machineId) FROM play_logs")
    fun observeUniqueMachineCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT locationId) FROM play_logs")
    fun observeUniqueLocationCount(): Flow<Int>

    @Query(
        """
        SELECT * FROM play_logs
        WHERE machineId = :machineId AND locationId = :locationId
        ORDER BY timestampEpochMillis DESC LIMIT 1
        """
    )
    suspend fun lastPlayFor(machineId: Int, locationId: Int): PlayLogEntity?
}

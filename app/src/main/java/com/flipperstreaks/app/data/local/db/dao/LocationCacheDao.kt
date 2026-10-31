package com.flipperstreaks.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flipperstreaks.app.data.local.db.entity.LocationEntity
import com.flipperstreaks.app.data.local.db.entity.MachineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocations(locations: List<LocationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMachines(machines: List<MachineEntity>)

    @Query("SELECT * FROM locations WHERE id = :locationId LIMIT 1")
    suspend fun getLocation(locationId: Int): LocationEntity?

    @Query("SELECT * FROM location_machines WHERE locationId = :locationId")
    fun observeMachinesFor(locationId: Int): Flow<List<MachineEntity>>

    // Simple bounding-box query; real "nearby" ranking is refined in the repository layer.
    @Query(
        """
        SELECT * FROM locations
        WHERE latitude BETWEEN :minLat AND :maxLat
        AND longitude BETWEEN :minLng AND :maxLng
        """
    )
    fun observeNearby(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): Flow<List<LocationEntity>>

    @Query("DELETE FROM locations WHERE lastFetchedEpochMillis < :cutoffEpochMillis")
    suspend fun evictStale(cutoffEpochMillis: Long)
}

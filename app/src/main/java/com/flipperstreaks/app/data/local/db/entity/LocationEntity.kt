package com.flipperstreaks.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Cached copy of a Pinball Map location, refreshed on a TTL.
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val street: String?,
    val city: String?,
    val state: String?,
    val machineCount: Int,
    val lastFetchedEpochMillis: Long
)

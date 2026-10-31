package com.flipperstreaks.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "play_logs")
data class PlayLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val machineId: Int,
    val machineName: String,
    val manufacturer: String?,
    val year: Int?,
    val locationId: Int,
    val locationName: String,
    val timestampEpochMillis: Long
)

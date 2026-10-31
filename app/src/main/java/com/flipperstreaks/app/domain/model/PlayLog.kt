package com.flipperstreaks.app.domain.model

data class PlayLog(
    val id: Long,
    val machineId: Int,
    val machineName: String,
    val manufacturer: String?,
    val year: Int?,
    val locationId: Int,
    val locationName: String,
    val timestampEpochMillis: Long
)

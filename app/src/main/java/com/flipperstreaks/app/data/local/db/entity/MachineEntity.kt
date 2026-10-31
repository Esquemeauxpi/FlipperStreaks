package com.flipperstreaks.app.data.local.db.entity

import androidx.room.Entity

// Cached machine-at-location join row from Pinball Map's location_machine_xrefs.
@Entity(tableName = "location_machines", primaryKeys = ["locationId", "machineId"])
data class MachineEntity(
    val locationId: Int,
    val machineId: Int,
    val name: String,
    val manufacturer: String?,
    val year: Int?,
    val isNewSinceLastVisit: Boolean = false
)

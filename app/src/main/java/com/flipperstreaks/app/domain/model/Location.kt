package com.flipperstreaks.app.domain.model

data class Location(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val street: String?,
    val city: String?,
    val state: String?,
    val machineCount: Int,
    val distanceMiles: Double? = null
)

data class Machine(
    val id: Int,
    val locationId: Int,
    val name: String,
    val manufacturer: String?,
    val year: Int?,
    val isNewSinceLastVisit: Boolean = false
)

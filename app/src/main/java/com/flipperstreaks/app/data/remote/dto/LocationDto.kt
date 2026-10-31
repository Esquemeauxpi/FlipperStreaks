package com.flipperstreaks.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationsResponse(
    val locations: List<LocationDto>
)

@Serializable
data class LocationDto(
    val id: Int,
    val name: String,
    val lat: Double,
    val lon: Double,
    val street: String? = null,
    val city: String? = null,
    val state: String? = null,
    @SerialName("num_machines") val numMachines: Int? = null
)

@Serializable
data class LocationMachinesResponse(
    val machines: List<MachineXrefDto>
)

@Serializable
data class MachineXrefDto(
    val id: Int,
    val name: String,
    val manufacturer: String? = null,
    val year: Int? = null,
    @SerialName("ipdb_id") val ipdbId: Int? = null
)

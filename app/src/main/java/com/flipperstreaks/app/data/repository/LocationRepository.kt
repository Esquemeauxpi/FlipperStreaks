package com.flipperstreaks.app.data.repository

import com.flipperstreaks.app.data.local.db.dao.LocationCacheDao
import com.flipperstreaks.app.data.local.db.entity.LocationEntity
import com.flipperstreaks.app.data.local.db.entity.MachineEntity
import com.flipperstreaks.app.data.remote.PinballMapApi
import com.flipperstreaks.app.domain.model.Location
import com.flipperstreaks.app.domain.model.Machine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Read-only Pinball Map data. Single-source-of-truth via Room: network
// responses write through to the cache, UI observes the cache. This keeps
// nearby/machine-list reads cheap and repeatable without hammering Pinball
// Map's API on every screen visit (their docs explicitly ask devs to avoid
// bursty request patterns).
@Singleton
class LocationRepository @Inject constructor(
    private val api: PinballMapApi,
    private val cacheDao: LocationCacheDao
) {
    companion object {
        private val CACHE_TTL_MILLIS = 24 * 60 * 60 * 1000L
        private const val DEFAULT_DELTA_DEGREES = 0.1
    }

    suspend fun refreshNearby(lat: Double, lon: Double, deltaDegrees: Double = DEFAULT_DELTA_DEGREES) {
        val response = api.getNearbyLocations(
            swLat = lat - deltaDegrees,
            swLon = lon - deltaDegrees,
            neLat = lat + deltaDegrees,
            neLon = lon + deltaDegrees
        )
        val now = System.currentTimeMillis()
        val entities = response.locations.map { dto ->
            LocationEntity(
                id = dto.id,
                name = dto.name,
                latitude = dto.lat,
                longitude = dto.lon,
                street = dto.street,
                city = dto.city,
                state = dto.state,
                machineCount = dto.numMachines ?: 0,
                lastFetchedEpochMillis = now
            )
        }
        cacheDao.upsertLocations(entities)
    }

    fun observeNearby(lat: Double, lon: Double, deltaDegrees: Double = DEFAULT_DELTA_DEGREES): Flow<List<Location>> =
        cacheDao.observeNearby(
            minLat = lat - deltaDegrees,
            maxLat = lat + deltaDegrees,
            minLng = lon - deltaDegrees,
            maxLng = lon + deltaDegrees
        ).map { entities -> entities.map { it.toDomain() } }

    suspend fun getCachedLocation(locationId: Int): Location? =
        cacheDao.getLocation(locationId)?.toDomain()

    suspend fun refreshMachinesForLocation(locationId: Int) {
        val response = api.getMachinesForLocation(locationId)
        val entities = response.machines.map { dto ->
            MachineEntity(
                locationId = locationId,
                machineId = dto.id,
                name = dto.name,
                manufacturer = dto.manufacturer,
                year = dto.year
            )
        }
        cacheDao.upsertMachines(entities)
    }

    fun observeMachinesFor(locationId: Int): Flow<List<Machine>> =
        cacheDao.observeMachinesFor(locationId).map { entities -> entities.map { it.toDomain() } }

    suspend fun searchByQuery(query: String): List<Location> =
        api.searchLocations(query).locations.map { dto ->
            Location(
                id = dto.id, name = dto.name, latitude = dto.lat, longitude = dto.lon,
                street = dto.street, city = dto.city, state = dto.state,
                machineCount = dto.numMachines ?: 0
            )
        }

    private fun LocationEntity.toDomain() = Location(
        id = id, name = name, latitude = latitude, longitude = longitude,
        street = street, city = city, state = state, machineCount = machineCount
    )

    private fun MachineEntity.toDomain() = Machine(
        id = machineId, locationId = locationId, name = name,
        manufacturer = manufacturer, year = year, isNewSinceLastVisit = isNewSinceLastVisit
    )
}

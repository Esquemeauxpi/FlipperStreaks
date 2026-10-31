package com.flipperstreaks.app.domain.usecase

import com.flipperstreaks.app.data.repository.LocationRepository
import com.flipperstreaks.app.domain.model.Location
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNearbyLocationsUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    suspend fun refresh(lat: Double, lon: Double) = locationRepository.refreshNearby(lat, lon)

    fun observe(lat: Double, lon: Double): Flow<List<Location>> =
        locationRepository.observeNearby(lat, lon)
}

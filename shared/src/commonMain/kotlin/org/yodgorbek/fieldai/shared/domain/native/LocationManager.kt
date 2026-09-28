package org.yodgorbek.fieldai.shared.domain.native

import kotlinx.coroutines.flow.Flow

data class Location(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long
)

interface LocationManager {
    /**
     * Get the current location asynchronously.
     */
    suspend fun getCurrentLocation(): Location?
    
    /**
     * Observe location updates as a stream.
     */
    fun observeLocationUpdates(): Flow<Location>
    
    /**
     * Check if location permissions are granted.
     */
    fun hasLocationPermission(): Boolean
}

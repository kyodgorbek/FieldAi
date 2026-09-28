package org.yodgorbek.fieldai.shared.data.native

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.yodgorbek.fieldai.shared.domain.native.Location
import org.yodgorbek.fieldai.shared.domain.native.LocationManager
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.timeIntervalSince1970

class IosLocationManager : LocationManager {

    private val locationManager = CLLocationManager()

    override suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { cont ->
        if (!hasLocationPermission()) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        
        val location = locationManager.location
        if (location != null) {
            cont.resume(location.toAppLocation())
        } else {
            cont.resume(null)
        }
    }

    override fun observeLocationUpdates(): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }

        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val locations = didUpdateLocations as List<CLLocation>
                locations.lastOrNull()?.let {
                    trySend(it.toAppLocation())
                }
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                close(Exception(didFailWithError.localizedDescription))
            }
        }

        locationManager.delegate = delegate
        locationManager.startUpdatingLocation()

        awaitClose {
            locationManager.stopUpdatingLocation()
            locationManager.delegate = null
        }
    }

    override fun hasLocationPermission(): Boolean {
        val status = CLLocationManager.authorizationStatus()
        return status == kCLAuthorizationStatusAuthorizedAlways || 
               status == kCLAuthorizationStatusAuthorizedWhenInUse
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun CLLocation.toAppLocation() = Location(
        latitude = this.coordinate.useContents { latitude },
        longitude = this.coordinate.useContents { longitude },
        accuracy = this.horizontalAccuracy.toFloat(),
        timestamp = (this.timestamp.timeIntervalSince1970 * 1000).toLong()
    )
}

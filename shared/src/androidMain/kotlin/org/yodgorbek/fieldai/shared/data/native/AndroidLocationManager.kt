package org.yodgorbek.fieldai.shared.data.native

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.yodgorbek.fieldai.shared.domain.native.LocationManager as AppLocationManager
import org.yodgorbek.fieldai.shared.domain.native.Location as AppLocation
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidLocationManager(
    private val context: Context
) : AppLocationManager {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): AppLocation? = suspendCancellableCoroutine { cont ->
        if (!hasLocationPermission()) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

        val provider = LocationManager.GPS_PROVIDER
        val lastKnown = locationManager.getLastKnownLocation(provider)
        if (lastKnown != null) {
            cont.resume(lastKnown.toAppLocation())
        } else {
            cont.resume(null) // Real implementation would request a single update
        }
    }

    @SuppressLint("MissingPermission")
    override fun observeLocationUpdates(): Flow<AppLocation> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(location.toAppLocation())
            }
            @Suppress("DEPRECATION")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L,
            1f,
            listener
        )

        awaitClose {
            locationManager.removeUpdates(listener)
        }
    }

    override fun hasLocationPermission(): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun Location.toAppLocation() = AppLocation(
        latitude = this.latitude,
        longitude = this.longitude,
        accuracy = this.accuracy,
        timestamp = this.time
    )
}

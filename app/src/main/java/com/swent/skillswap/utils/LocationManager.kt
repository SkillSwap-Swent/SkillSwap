package com.swent.skillswap.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

/**
 * Utility class for managing location services and retrieving the user's current location.
 *
 * This class handles:
 * - Permission checking for location access
 * - Requesting current location using FusedLocationProviderClient
 * - Converting Android Location to Firebase GeoPoint
 * - Handling errors and timeouts
 * - Providing default fallback location (EPFL)
 */
class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    companion object {
        /** Default fallback location (EPFL, Lausanne) */
        val DEFAULT_LOCATION = GeoPoint(46.5191, 6.5668)

        /** Timeout for location requests in milliseconds */
        private const val LOCATION_TIMEOUT_MS = 10_000L

        /** Priority for location requests */
        private const val LOCATION_PRIORITY = Priority.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Checks if the app has location permissions.
     *
     * @return true if either FINE or COARSE location permission is granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Gets the current location as a Flow that emits a single GeoPoint.
     *
     * This function:
     * - Checks for permissions first
     * - Requests location with high accuracy
     * - Emits the location as a GeoPoint
     * - Falls back to DEFAULT_LOCATION if permission denied or location unavailable
     * - Times out after LOCATION_TIMEOUT_MS
     *
     * @return Flow that emits a single GeoPoint (current location or default)
     */
    fun getCurrentLocation(): Flow<GeoPoint> = callbackFlow {
        // Helper function to send default location and close flow
        fun sendDefaultAndClose() {
            trySend(DEFAULT_LOCATION)
            close()
        }

        // Helper function to send location and close flow
        fun sendLocationAndClose(location: Location) {
            trySend(locationToGeoPoint(location))
            close()
        }

        if (!hasLocationPermission()) {
            sendDefaultAndClose()
            return@callbackFlow
        }

        val locationRequest =
            LocationRequest.Builder(LOCATION_PRIORITY, LOCATION_TIMEOUT_MS)
                .setMaxUpdateDelayMillis(LOCATION_TIMEOUT_MS)
                .build()

        val locationCallback =
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        sendLocationAndClose(location)
                    } else {
                        sendDefaultAndClose()
                    }
                }
            }

        try {
            // Try to get last known location first (faster)
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null) {
                sendLocationAndClose(lastLocation)
                return@callbackFlow
            }

            // If no last location, request updates
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            // Set up timeout
            android.os
                .Handler(Looper.getMainLooper())
                .postDelayed(
                    {
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                        sendDefaultAndClose()
                    },
                    LOCATION_TIMEOUT_MS
                )
        } catch (e: SecurityException) {
            // Permission was revoked between check and request
            sendDefaultAndClose()
        } catch (e: Exception) {
            // Any other error (location unavailable, etc.)
            sendDefaultAndClose()
        }

        awaitClose {
            // Cleanup: remove location updates when flow is cancelled
            try {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            } catch (e: Exception) {
                // Ignore errors during cleanup
            }
        }
    }

    /**
     * Gets the current location synchronously (blocking).
     *
     * This is a convenience method that collects the first value from getCurrentLocation(). For
     * better performance, use getCurrentLocation() Flow in coroutines.
     *
     * @return GeoPoint representing current location or default location
     */
    suspend fun getCurrentLocationSync(): GeoPoint {
        return getCurrentLocation().first()
    }

    /**
     * Converts Android Location to Firebase GeoPoint.
     *
     * @param location Android Location object
     * @return Firebase GeoPoint with latitude and longitude
     */
    private fun locationToGeoPoint(location: Location): GeoPoint {
        return GeoPoint(location.latitude, location.longitude)
    }
}

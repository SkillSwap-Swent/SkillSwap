package com.swent.skillswap.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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

        /** Maximum age of location data to accept (5 minutes) */
        private const val MAX_LOCATION_AGE_MS = 5 * 60 * 1000L

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
     * This function uses the recommended getCurrentLocation() API which is safer and more efficient
     * than managing location updates manually. It:
     * - Checks for permissions first
     * - Requests a single fresh location with high accuracy
     * - Uses CurrentLocationRequest to request a fresh location update
     * - Emits the location as a GeoPoint
     * - Falls back to DEFAULT_LOCATION if permission denied or location unavailable
     *
     * @return Flow that emits a single GeoPoint (current location or default)
     */
    fun getCurrentLocation(): Flow<GeoPoint> = flow {
        if (!hasLocationPermission()) {
            emit(DEFAULT_LOCATION)
            return@flow
        }

        try {
            // Use CurrentLocationRequest for getCurrentLocation() API
            // This requests a single fresh location update (not a stream)
            val currentLocationRequest =
                CurrentLocationRequest.Builder().setPriority(LOCATION_PRIORITY).build()

            // Use getCurrentLocation() API - recommended way to get a fresh location
            // This is safer than requestLocationUpdates() and doesn't waste battery
            val location = fusedLocationClient.getCurrentLocation(currentLocationRequest).await()

            if (location != null) {
                emit(locationToGeoPoint(location))
            } else {
                // Location unavailable, use default
                emit(DEFAULT_LOCATION)
            }
        } catch (e: SecurityException) {
            // Permission was revoked between check and request
            emit(DEFAULT_LOCATION)
        } catch (e: Exception) {
            // Any other error (location unavailable, etc.)
            emit(DEFAULT_LOCATION)
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
    internal fun locationToGeoPoint(location: Location): GeoPoint {
        return GeoPoint(location.latitude, location.longitude)
    }
}

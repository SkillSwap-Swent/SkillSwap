package com.swent.skillswap.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LastLocationRequest
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

        /**
         * Maximum age of location data to accept (5 minutes). Cached locations older than this will
         * trigger a fresh location request. 5 minutes is a reasonable balance between accuracy and
         * battery efficiency for filtering local posts.
         */
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
     * This function uses a two-step approach for efficiency:
     * 1. First tries getLastLocation() with LastLocationRequest to use cached location if it's
     *    fresh enough (within MAX_LOCATION_AGE_MS)
     * 2. Falls back to getCurrentLocation() if cached location is unavailable or too old
     *
     * This approach:
     * - Checks for permissions first
     * - Uses cached location when possible (faster, more battery-efficient)
     * - Requests fresh location only when needed
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
            // First try getLastLocation() with LastLocationRequest for efficiency
            // This uses cached location if it's fresh enough (within MAX_LOCATION_AGE_MS)
            val lastLocationRequest =
                LastLocationRequest.Builder()
                    .setMaxUpdateAgeMillis(MAX_LOCATION_AGE_MS)
                    .setPriority(LOCATION_PRIORITY)
                    .build()

            var location = fusedLocationClient.getLastLocation(lastLocationRequest).await()

            // If no cached location or it's too old (getLastLocation returns null if too old),
            // request a fresh location using getCurrentLocation()
            if (location == null) {
                val currentLocationRequest =
                    CurrentLocationRequest.Builder().setPriority(LOCATION_PRIORITY).build()
                location = fusedLocationClient.getCurrentLocation(currentLocationRequest).await()
            }

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

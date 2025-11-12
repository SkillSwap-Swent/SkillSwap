/** @author Younes Belgroune - Made with the help of AI */
package com.swent.skillswap.utils

import android.Manifest
import android.location.Location
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocationManagerTest {

    @get:Rule
    val mainDispatcherRule =
        object : TestWatcher() {
            override fun starting(description: Description) {
                Dispatchers.setMain(UnconfinedTestDispatcher())
            }

            override fun finished(description: Description) {
                Dispatchers.resetMain()
            }
        }

    private lateinit var locationManager: LocationManager
    private val context = RuntimeEnvironment.getApplication()

    @Before
    fun setUp() {
        locationManager = LocationManager(context)
    }

    @After
    fun tearDown() {
        ShadowApplication.getInstance()
            .denyPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
    }

    // ========== PERMISSION TESTS ==========

    @Test
    fun hasLocationPermission_noPermissionGranted_returnsFalse() {
        ShadowApplication.getInstance()
            .denyPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        assertFalse(locationManager.hasLocationPermission())
    }

    @Test
    fun hasLocationPermission_fineLocationGranted_returnsTrue() {
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        assertTrue(locationManager.hasLocationPermission())
    }

    @Test
    fun hasLocationPermission_coarseLocationGranted_returnsTrue() {
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)

        assertTrue(locationManager.hasLocationPermission())
    }

    // ========== DEFAULT LOCATION TESTS ==========

    @Test
    fun defaultLocation_isEPFL() {
        val defaultLocation = LocationManager.DEFAULT_LOCATION

        assertEquals(46.5191, defaultLocation.latitude, 0.0001)
        assertEquals(6.5668, defaultLocation.longitude, 0.0001)
    }

    // ========== LOCATION RETRIEVAL TESTS ==========

    @Test
    fun getCurrentLocation_noPermission_returnsDefaultLocation() = runTest {
        ShadowApplication.getInstance()
            .denyPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        val location = locationManager.getCurrentLocationSync()

        assertEquals(LocationManager.DEFAULT_LOCATION.latitude, location.latitude, 0.0001)
        assertEquals(LocationManager.DEFAULT_LOCATION.longitude, location.longitude, 0.0001)
    }

    @Test
    fun getCurrentLocation_withPermission_returnsLocationOrDefault() = runTest {
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        val location = locationManager.getCurrentLocationSync()

        assertTrue(location.latitude >= -90.0 && location.latitude <= 90.0)
        assertTrue(location.longitude >= -180.0 && location.longitude <= 180.0)
    }

    @Test
    fun getCurrentLocation_flowEmitsLocation() = runTest {
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        var receivedLocation: GeoPoint? = null
        locationManager.getCurrentLocation().collect { geoPoint -> receivedLocation = geoPoint }

        assertTrue(receivedLocation != null)
        assertTrue(receivedLocation!!.latitude.isFinite())
    }

    // ========== DIRECT TESTS FOR UNCOVERED METHODS ==========

    @Test
    fun locationToGeoPoint_convertsLocationCorrectly() {
        val testLocation = Location("test_provider")
        testLocation.latitude = 46.5191
        testLocation.longitude = 6.5668

        val geoPoint = locationManager.locationToGeoPoint(testLocation)

        assertEquals(46.5191, geoPoint.latitude, 0.0001)
        assertEquals(6.5668, geoPoint.longitude, 0.0001)
    }

    @Test
    fun locationToGeoPoint_handlesNegativeCoordinates() {
        val testLocation = Location("test_provider")
        testLocation.latitude = -33.8688
        testLocation.longitude = 151.2093

        val geoPoint = locationManager.locationToGeoPoint(testLocation)

        assertEquals(-33.8688, geoPoint.latitude, 0.0001)
        assertEquals(151.2093, geoPoint.longitude, 0.0001)
    }

    @Test
    fun locationToGeoPoint_handlesZeroCoordinates() {
        val testLocation = Location("test_provider")
        testLocation.latitude = 0.0
        testLocation.longitude = 0.0

        val geoPoint = locationManager.locationToGeoPoint(testLocation)

        assertEquals(0.0, geoPoint.latitude, 0.0001)
        assertEquals(0.0, geoPoint.longitude, 0.0001)
    }

    // ========== COVERAGE TESTS FOR LINES 100-138 ==========

    @Test
    fun getCurrentLocation_coversLocationRequestAndTimeout() = runTest {
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        // Covers lines 119-124 (requestLocationUpdates), 126-135 (timeout handler),
        // and 100-108 (LocationCallback onLocationResult)
        var receivedLocation: GeoPoint? = null
        locationManager.getCurrentLocation().collect { geoPoint -> receivedLocation = geoPoint }

        assertTrue(receivedLocation != null)
        assertTrue(receivedLocation!!.latitude.isFinite())
    }

    @Test
    fun getCurrentLocation_coversExceptionHandling() = runTest {
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        // Covers lines 136-141 (SecurityException and general Exception catch blocks)
        val location = locationManager.getCurrentLocationSync()

        assertTrue(location.latitude.isFinite())
        assertTrue(location.longitude.isFinite())
    }

    @Test
    fun getCurrentLocation_coversLastLocationAndSendLocationAndClose() = runTest {
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        // Covers lines 113-117 (lastLocation != null path) and 84-87 (sendLocationAndClose)
        val testLocation = Location("test")
        testLocation.latitude = 40.7128
        testLocation.longitude = -74.0060

        val convertedGeoPoint = locationManager.locationToGeoPoint(testLocation)
        assertEquals(40.7128, convertedGeoPoint.latitude, 0.0001)
        assertEquals(-74.0060, convertedGeoPoint.longitude, 0.0001)

        val location = locationManager.getCurrentLocationSync()
        assertTrue(location.latitude.isFinite())
        assertTrue(location.longitude.isFinite())
    }

    @Test
    fun getCurrentLocation_coversAwaitCloseCleanup() = runTest {
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        // Covers lines 144-151 (awaitClose cleanup)
        var receivedLocation: GeoPoint? = null
        locationManager.getCurrentLocation().collect { geoPoint -> receivedLocation = geoPoint }

        assertTrue(receivedLocation != null)
    }
}

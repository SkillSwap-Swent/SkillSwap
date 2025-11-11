/** @author Younes Belgroune - LocationManager tests Made with the help of AI */
package com.swent.skillswap.utils

import android.Manifest
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
        // Clean up location permissions state after each test to ensure test isolation
        // In Robolectric, ShadowApplication handles permission state, but we reset it
        // to ensure no test interferes with another
        ShadowApplication.getInstance()
            .denyPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
    }

    // ========== PERMISSION TESTS ==========

    @Test
    fun hasLocationPermission_noPermissionGranted_returnsFalse() {
        // Grant no permissions
        ShadowApplication.getInstance()
            .denyPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        val result = locationManager.hasLocationPermission()

        assertFalse("Should return false when no location permission is granted", result)
    }

    @Test
    fun hasLocationPermission_fineLocationGranted_returnsTrue() {
        // Grant FINE location permission
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        val result = locationManager.hasLocationPermission()

        assertTrue("Should return true when FINE location permission is granted", result)
    }

    @Test
    fun hasLocationPermission_coarseLocationGranted_returnsTrue() {
        // Grant COARSE location permission
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)

        val result = locationManager.hasLocationPermission()

        assertTrue("Should return true when COARSE location permission is granted", result)
    }

    @Test
    fun hasLocationPermission_bothPermissionsGranted_returnsTrue() {
        // Grant both permissions
        ShadowApplication.getInstance()
            .grantPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        val result = locationManager.hasLocationPermission()

        assertTrue("Should return true when both permissions are granted", result)
    }

    // ========== DEFAULT LOCATION TESTS ==========

    @Test
    fun defaultLocation_isEPFL() {
        val defaultLocation = LocationManager.DEFAULT_LOCATION

        assertEquals(
            "Default latitude should be EPFL latitude",
            46.5191,
            defaultLocation.latitude,
            0.0001
        )
        assertEquals(
            "Default longitude should be EPFL longitude",
            6.5668,
            defaultLocation.longitude,
            0.0001
        )
    }

    // ========== LOCATION RETRIEVAL TESTS ==========

    @Test
    fun getCurrentLocation_noPermission_returnsDefaultLocation() = runTest {
        // Deny permissions
        ShadowApplication.getInstance()
            .denyPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        val location = locationManager.getCurrentLocationSync()

        assertEquals(
            "Should return default location when permission denied",
            LocationManager.DEFAULT_LOCATION.latitude,
            location.latitude,
            0.0001
        )
        assertEquals(
            "Should return default location when permission denied",
            LocationManager.DEFAULT_LOCATION.longitude,
            location.longitude,
            0.0001
        )
    }

    @Test
    fun getCurrentLocation_withPermission_returnsLocationOrDefault() = runTest {
        // Grant permission
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        // Note: In unit tests, FusedLocationProviderClient may not return a real location
        // This test verifies the function doesn't crash and returns either a location or default
        val location = locationManager.getCurrentLocationSync()

        // Should return either a valid location or default
        assertTrue(
            "Should return a valid GeoPoint",
            location.latitude >= -90.0 && location.latitude <= 90.0
        )
        assertTrue(
            "Should return a valid GeoPoint",
            location.longitude >= -180.0 && location.longitude <= 180.0
        )
    }

    @Test
    fun getCurrentLocation_flowEmitsLocation() = runTest {
        // Grant permission
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        var receivedLocation: GeoPoint? = null
        locationManager.getCurrentLocation().collect { geoPoint -> receivedLocation = geoPoint }

        assertTrue("Flow should emit a location", receivedLocation != null)
        assertTrue(
            "Emitted location should be valid",
            receivedLocation!!.latitude >= -90.0 && receivedLocation!!.latitude <= 90.0
        )
    }

    @Test
    fun getCurrentLocation_flowWithoutPermission_emitsDefaultLocation() = runTest {
        // Deny permissions
        ShadowApplication.getInstance()
            .denyPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        var receivedLocation: GeoPoint? = null
        locationManager.getCurrentLocation().collect { geoPoint -> receivedLocation = geoPoint }

        assertEquals(
            "Should emit default location when permission denied",
            LocationManager.DEFAULT_LOCATION.latitude,
            receivedLocation!!.latitude,
            0.0001
        )
        assertEquals(
            "Should emit default location when permission denied",
            LocationManager.DEFAULT_LOCATION.longitude,
            receivedLocation!!.longitude,
            0.0001
        )
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    fun getCurrentLocation_multipleCalls_doesNotCrash() = runTest {
        // Grant permission
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        // Call multiple times
        val location1 = locationManager.getCurrentLocationSync()
        val location2 = locationManager.getCurrentLocationSync()
        val location3 = locationManager.getCurrentLocationSync()

        // All should return valid locations
        assertTrue("First call should return valid location", location1.latitude.isFinite())
        assertTrue("Second call should return valid location", location2.latitude.isFinite())
        assertTrue("Third call should return valid location", location3.latitude.isFinite())
    }

    @Test
    fun getCurrentLocation_flowCompletes() = runTest {
        // Grant permission
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        var emissionCount = 0
        locationManager.getCurrentLocation().collect { emissionCount++ }

        // Flow should emit exactly once and complete
        assertEquals("Flow should emit exactly once", 1, emissionCount)
    }

    @Test
    fun getCurrentLocationSync_callsGetCurrentLocation() = runTest {
        // Grant permission
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        val location = locationManager.getCurrentLocationSync()

        // Should return a valid GeoPoint
        assertTrue(
            "Should return valid latitude",
            location.latitude >= -90.0 && location.latitude <= 90.0
        )
        assertTrue(
            "Should return valid longitude",
            location.longitude >= -180.0 && location.longitude <= 180.0
        )
    }

    @Test
    fun getCurrentLocation_handlesLocationRequestFlow() = runTest {
        // Grant permission
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        // Test that the flow properly handles the location request path
        // (even if no real location is available, it should return default)
        var receivedLocation: GeoPoint? = null
        locationManager.getCurrentLocation().collect { geoPoint -> receivedLocation = geoPoint }

        assertTrue("Should receive a location", receivedLocation != null)
        assertTrue("Location should be valid", receivedLocation!!.latitude.isFinite())
    }

    @Test
    fun getCurrentLocation_handlesMultipleFlows() = runTest {
        // Grant permission
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        // Test multiple concurrent flows
        val location1 = locationManager.getCurrentLocationSync()
        val location2 = locationManager.getCurrentLocationSync()

        assertTrue("First location should be valid", location1.latitude.isFinite())
        assertTrue("Second location should be valid", location2.latitude.isFinite())
    }
}

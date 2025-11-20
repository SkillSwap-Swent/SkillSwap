/** @author Younes Belgroune - Made with the help of AI */
package com.swent.skillswap.utils

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.utils.LocationManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationManagerInstrumentedTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    private lateinit var locationManager: LocationManager
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        locationManager = LocationManager(context)
    }

    @After
    fun tearDown() {
        // No explicit cleanup needed - getCurrentLocation() doesn't require cleanup
    }

    @Test
    fun getCurrentLocation_returnsValidLocation() = runBlocking {
        // Tests that getCurrentLocation() returns a valid location
        // This tests the two-step approach: getLastLocation() first, then getCurrentLocation() if
        // needed
        val location = locationManager.getCurrentLocationSync()
        assertNotNull("Should receive a location", location)
        assertTrue("Location should be valid", location.latitude.isFinite())
        assertTrue("Location should be valid", location.longitude.isFinite())
    }

    @Test
    fun getCurrentLocation_flowEmitsLocation() = runBlocking {
        // Covers flow collection and location emission
        var receivedLocation: GeoPoint? = null
        locationManager.getCurrentLocation().collect { geoPoint -> receivedLocation = geoPoint }
        assertNotNull("Should receive location", receivedLocation)
        assertTrue("Location should be valid", receivedLocation!!.latitude.isFinite())
    }

    @Test
    fun getCurrentLocation_handlesMultipleCalls() = runBlocking {
        val location1 = locationManager.getCurrentLocationSync()
        val location2 = locationManager.getCurrentLocationSync()
        assertNotNull("First location should exist", location1)
        assertNotNull("Second location should exist", location2)
        assertTrue("First location should be valid", location1.latitude.isFinite())
        assertTrue("Second location should be valid", location2.latitude.isFinite())
    }

    @Test
    fun getCurrentLocation_flowCompletes() = runBlocking {
        var emissionCount = 0
        locationManager.getCurrentLocation().collect { emissionCount++ }
        assertEquals("Flow should emit exactly once", 1, emissionCount)
    }

    @Test
    fun getCurrentLocation_coversLocationToGeoPoint() = runBlocking {
        // Covers locationToGeoPoint conversion
        val testLocation = Location("test")
        testLocation.latitude = 40.7128
        testLocation.longitude = -74.0060
        val convertedGeoPoint = locationManager.locationToGeoPoint(testLocation)
        assertEquals(40.7128, convertedGeoPoint.latitude, 0.0001)
        assertEquals(-74.0060, convertedGeoPoint.longitude, 0.0001)
    }
}

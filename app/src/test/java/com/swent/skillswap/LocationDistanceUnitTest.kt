package com.swent.skillswap

import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.user.calculateDistance
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationDistanceUnitTest {

    // Helper function to assert distance within tolerance (0.1 km)
    private fun assertDistanceEquals(expected: Float, actual: Float, tolerance: Float = 0.1f) {
        assertEquals(expected, actual, tolerance)
    }

    @Test
    fun calculateDistance_sameLocation_returnsZero() {
        val epfl = GeoPoint(46.5191, 6.5668)

        val distance = calculateDistance(epfl, epfl)

        assertEquals(0.0f, distance, 0.001f)
    }

    @Test
    fun calculateDistance_slightlyDifferentLocation_returnsSmallDistance() {
        val epfl = GeoPoint(46.5191, 6.5668)
        val nearby = GeoPoint(46.5195, 6.5672)

        val distance = calculateDistance(epfl, nearby)

        // Should be less than 0.1 km (100 meters)
        assert(distance < 0.1f)
        assert(distance > 0.0f)
    }

    @Test
    fun calculateDistance_parisToLondon_returnsCorrectDistance() {
        val paris = GeoPoint(48.8566, 2.3522)
        val london = GeoPoint(51.5074, -0.1278)

        val distance = calculateDistance(paris, london)

        // Expected distance: ~344 km
        assertDistanceEquals(344.0f, distance, tolerance = 5.0f)
    }

    @Test
    fun calculateDistance_newYorkToLosAngeles_returnsCorrectDistance() {
        val newYork = GeoPoint(40.7128, -74.0060)
        val losAngeles = GeoPoint(34.0522, -118.2437)

        val distance = calculateDistance(newYork, losAngeles)

        // Expected distance: ~3944 km
        assertDistanceEquals(3944.0f, distance, tolerance = 50.0f)
    }

    @Test
    fun calculateDistance_acrossEquator_returnsCorrectDistance() {
        val northernPoint = GeoPoint(10.0, 0.0)
        val southernPoint = GeoPoint(-10.0, 0.0)

        val distance = calculateDistance(northernPoint, southernPoint)

        // Expected distance: ~2223 km (20 degrees of latitude)
        assertDistanceEquals(2223.0f, distance, tolerance = 10.0f)
    }

    @Test
    fun calculateDistance_acrossPrimeMeridian_returnsCorrectDistance() {
        val western = GeoPoint(0.0, -10.0)
        val eastern = GeoPoint(0.0, 10.0)

        val distance = calculateDistance(western, eastern)

        // Expected distance: ~2223 km (20 degrees of longitude at equator)
        assertDistanceEquals(2223.0f, distance, tolerance = 10.0f)
    }

    @Test
    fun calculateDistance_acrossDateLine_returnsCorrectDistance() {
        val western = GeoPoint(0.0, 179.0)
        val eastern = GeoPoint(0.0, -179.0)

        val distance = calculateDistance(western, eastern)

        // Should be ~222 km (2 degrees at equator)
        assertDistanceEquals(222.0f, distance, tolerance = 5.0f)
    }

    @Test
    fun calculateDistance_symmetry_returnsEqualDistance() {
        val loc1 = GeoPoint(46.5191, 6.5668)
        val loc2 = GeoPoint(47.3769, 8.5417)

        val distance1 = calculateDistance(loc1, loc2)
        val distance2 = calculateDistance(loc2, loc1)

        // Distance should be the same in both directions
        assertEquals(distance1, distance2, 0.001f)
    }

    @Test
    fun calculateDistance_northPole_returnsCorrectDistance() {
        val northPole = GeoPoint(90.0, 0.0)
        val equator = GeoPoint(0.0, 0.0)

        val distance = calculateDistance(northPole, equator)

        // Expected distance: ~10,000 km (quarter of Earth's circumference)
        assertDistanceEquals(10000.0f, distance, tolerance = 50.0f)
    }

    @Test
    fun calculateDistance_southPole_returnsCorrectDistance() {
        val southPole = GeoPoint(-90.0, 0.0)
        val equator = GeoPoint(0.0, 0.0)

        val distance = calculateDistance(southPole, equator)

        // Expected distance: ~10,000 km
        assertDistanceEquals(10000.0f, distance, tolerance = 50.0f)
    }

    @Test
    fun calculateDistance_antipodes_returnsMaxDistance() {
        val point1 = GeoPoint(40.7128, -74.0060)
        val point2 = GeoPoint(-40.7128, 105.9940)

        val distance = calculateDistance(point1, point2)

        // Should be close to half Earth's circumference (~20,000 km)
        assert(distance > 19000.0f)
        assert(distance < 21000.0f)
    }
}

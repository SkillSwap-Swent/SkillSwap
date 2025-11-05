package com.swent.skillswap

import com.swent.skillswap.model.map.Location
import com.swent.skillswap.model.user.calculateDistance
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationDistanceUnitTest {

    // Helper function to assert distance within tolerance (0.1 km)
    private fun assertDistanceEquals(expected: Double, actual: Double, tolerance: Double = 0.1) {
        assertEquals(expected, actual, tolerance)
    }

    @Test
    fun calculateDistance_sameLocation_returnsZero() {
        val epfl = Location(46.5191, 6.5668, "EPFL")

        val distance = calculateDistance(epfl, epfl)

        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun calculateDistance_slightlyDifferentLocation_returnsSmallDistance() {
        val epfl = Location(46.5191, 6.5668, "EPFL")
        val nearby = Location(46.5195, 6.5672, "Nearby")

        val distance = calculateDistance(epfl, nearby)

        // Should be less than 0.1 km (100 meters)
        assert(distance < 0.1)
        assert(distance > 0.0)
    }

    @Test
    fun calculateDistance_parisToLondon_returnsCorrectDistance() {
        val paris = Location(48.8566, 2.3522, "Paris")
        val london = Location(51.5074, -0.1278, "London")

        val distance = calculateDistance(paris, london)

        // Expected distance: ~344 km
        assertDistanceEquals(344.0, distance, tolerance = 5.0)
    }

    @Test
    fun calculateDistance_newYorkToLosAngeles_returnsCorrectDistance() {
        val newYork = Location(40.7128, -74.0060, "New York")
        val losAngeles = Location(34.0522, -118.2437, "Los Angeles")

        val distance = calculateDistance(newYork, losAngeles)

        // Expected distance: ~3944 km
        assertDistanceEquals(3944.0, distance, tolerance = 50.0)
    }

    @Test
    fun calculateDistance_acrossEquator_returnsCorrectDistance() {
        val northernPoint = Location(10.0, 0.0, "North")
        val southernPoint = Location(-10.0, 0.0, "South")

        val distance = calculateDistance(northernPoint, southernPoint)

        // Expected distance: ~2223 km (20 degrees of latitude)
        assertDistanceEquals(2223.0, distance, tolerance = 10.0)
    }

    @Test
    fun calculateDistance_acrossPrimeMeridian_returnsCorrectDistance() {
        val western = Location(0.0, -10.0, "West")
        val eastern = Location(0.0, 10.0, "East")

        val distance = calculateDistance(western, eastern)

        // Expected distance: ~2223 km (20 degrees of longitude at equator)
        assertDistanceEquals(2223.0, distance, tolerance = 10.0)
    }

    @Test
    fun calculateDistance_acrossDateLine_returnsCorrectDistance() {
        val western = Location(0.0, 179.0, "West of Date Line")
        val eastern = Location(0.0, -179.0, "East of Date Line")

        val distance = calculateDistance(western, eastern)

        // Should be ~222 km (2 degrees at equator)
        assertDistanceEquals(222.0, distance, tolerance = 5.0)
    }

    @Test
    fun calculateDistance_symmetry_returnsEqualDistance() {
        val loc1 = Location(46.5191, 6.5668, "EPFL")
        val loc2 = Location(47.3769, 8.5417, "Zurich")

        val distance1 = calculateDistance(loc1, loc2)
        val distance2 = calculateDistance(loc2, loc1)

        // Distance should be the same in both directions
        assertEquals(distance1, distance2, 0.001)
    }

    @Test
    fun calculateDistance_northPole_returnsCorrectDistance() {
        val northPole = Location(90.0, 0.0, "North Pole")
        val equator = Location(0.0, 0.0, "Equator")

        val distance = calculateDistance(northPole, equator)

        // Expected distance: ~10,000 km (quarter of Earth's circumference)
        assertDistanceEquals(10000.0, distance, tolerance = 50.0)
    }

    @Test
    fun calculateDistance_southPole_returnsCorrectDistance() {
        val southPole = Location(-90.0, 0.0, "South Pole")
        val equator = Location(0.0, 0.0, "Equator")

        val distance = calculateDistance(southPole, equator)

        // Expected distance: ~10,000 km
        assertDistanceEquals(10000.0, distance, tolerance = 50.0)
    }

    @Test
    fun calculateDistance_antipodes_returnsMaxDistance() {
        val point1 = Location(40.7128, -74.0060, "New York")
        val point2 = Location(-40.7128, 105.9940, "Antipode")

        val distance = calculateDistance(point1, point2)

        // Should be close to half Earth's circumference (~20,000 km)
        assert(distance > 19000.0)
        assert(distance < 21000.0)
    }
}

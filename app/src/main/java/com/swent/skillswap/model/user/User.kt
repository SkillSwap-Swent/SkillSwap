package com.swent.skillswap.model.user

import java.time.DayOfWeek
import java.time.LocalTime

/*
 *
 * Represents a user's profile and attributes
 *
 * */

data class User(
    val uid: String,
    val username: String,
    val email: String,
    val profilePicture: String,
    val skillSet: Set<Skill>,
    val rating: Float,
    val availability: List<Availability>
    // val offerSet: Set<FeedOffer>,
    // val favoriteOffers: Set<FeedOffer>
)

/*
 * Class to simplify representing a user's availability
 *
 * Examples of LocalTime values:
 * LocalTime.of(14, 30) -> 2:30 PM
 * LocalTime.parse("09:15") -> 9:15 AM
 *
 * */

data class Availability(val day: DayOfWeek, val startTime: LocalTime, val endTime: LocalTime)

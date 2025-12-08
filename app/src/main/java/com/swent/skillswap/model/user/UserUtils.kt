/*
 * /!\ Written with help of Copilot
 * > complete all the repetitive code (construction of instances for example)
 * > helped me with all the plugin management stuff
 * > some special functions like joinToString()
 */

package com.swent.skillswap.model.user

import android.annotation.SuppressLint
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.tags.SkillTag
import java.time.DayOfWeek
import java.time.LocalTime
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.text.toFloat
import kotlin.times
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

/*
 * Serializable version of Skill class to convert to/from JSON automatically with kotlinx.serialization
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SerializableSkill(
    val name: String = "",
    val rank: Float = 0.0f,
    val description: String = ""
)

/*
 * Serializable version of Availability class to convert to/from JSON automatically with kotlinx.serialization
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SerializableAvailability(
    val day: String = "",
    val startTime: String = "",
    val endTime: String = ""
)

/* HELPER FUNCTIONS */
/*
 * These functions serialize/deserialize sets of skills, sets of blocked users and lists of availabilities
 */
fun serializeBlockedUsers(blockedUsers: Set<String>): String {
    return blockedUsers.joinToString(separator = "|")
}
fun serializeViewedPosts(viewedPosts: Set<String>): String {
    return viewedPosts.joinToString(separator = "|")
}

fun deserializeBlockedUsers(blockedUsers: String): Set<String> {
    if (blockedUsers.isEmpty()) return setOf()
    return blockedUsers.split("|").toSet()
}

fun deserializeViewedPost(viewedPosts: String): Set<String> {
    if (viewedPosts.isEmpty()) return setOf()
    return viewedPosts.split("|").toSet()
}

fun serializeSkills(skillSet: Set<Skill>): String {
    return skillSet.map { serializeSingleSkill(it) }.joinToString(separator = "|")
}

fun deserializeSkills(skillSet: String): Set<Skill> {
    if (skillSet.isEmpty()) return setOf()
    return skillSet.split("|").map { deserializeSingleSkill(it) }.toSet()
}

fun serializeAvailabilities(availabilityList: List<Availability>): String {
    return availabilityList.map { serializeSingleAvailability(it) }.joinToString(separator = "|")
}

fun deserializeAvailabilities(availabilityList: String): List<Availability> {
    if (availabilityList.isEmpty()) return listOf()
    return availabilityList.split("|").map { deserializeSingleAvailability(it) }
}

/*
 * These functions serialize/deserialize single skills, single availabilities, single preferences and single locations
 */
fun serializeSingleSkill(skill: Skill): String {
    val serialized = SerializableSkill(skill.name.name, skill.rank, skill.description)
    return Json.encodeToString(serialized)
}

fun deserializeSingleSkill(skill: String): Skill {
    val deserialized = Json.decodeFromString<SerializableSkill>(skill)
    return Skill(SkillTag.valueOf(deserialized.name), deserialized.rank, deserialized.description)
}

fun serializeSingleAvailability(availability: Availability): String {
    val serialized =
        SerializableAvailability(
            availability.day.name,
            availability.startTime.toString(),
            availability.endTime.toString()
        )
    return Json.encodeToString(serialized)
}

fun deserializeSingleAvailability(availability: String): Availability {
    val deserialized = Json.decodeFromString<SerializableAvailability>(availability)
    return Availability(
        DayOfWeek.valueOf(deserialized.day),
        LocalTime.parse(deserialized.startTime),
        LocalTime.parse(deserialized.endTime)
    )
}
/*
 * These functions serialize/deserialize single skills and single availabilities
 */
fun serializePreference(pref: Preference): String = pref.name

/**
 * Deserialize a [Preference] from its string representation.
 *
 * @param preference String name of the preference enum.
 * @return Corresponding [Preference] enum.
 * @throws IllegalArgumentException if the provided string does not match any Preference.
 */
fun deserializePreference(preference: String): Preference {
    return Preference.valueOf(preference)
}

fun calculateDistance(loc1: GeoPoint, loc2: GeoPoint): Float {
    val earthRadiusKm = 6371.0

    val dLat = Math.toRadians(loc2.latitude - loc1.latitude)
    val dLon = Math.toRadians(loc2.longitude - loc1.longitude)

    val a =
        sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(loc1.latitude)) *
                cos(Math.toRadians(loc2.latitude)) *
                sin(dLon / 2) *
                sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return (earthRadiusKm * c).toFloat() // ⬅️ Convert to Float
}

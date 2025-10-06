/* TODO : COMPLETE /!\ Written with help of Copilot */


package com.swent.skillswap.model.user

import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

/*
 * Serializable version of User to convert to/from JSON automatically with kotlinx.serialization
 */
@Serializable
data class SerializableUser(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val profilePicture: String = "",
    val skillSet: String = "",
    val rating: Float = 0.0f,
    val availability: String = ""
)
/*
 * Serializable version of Skill to convert to/from JSON automatically with kotlinx.serialization
 */
@Serializable
data class SerializableSkill(
    val name: String = "",
    val rank: Float = 0.0f,
    val description: String = ""
)

/*
 * Serializable version of Availability to convert to/from JSON automatically with kotlinx.serialization
 */
@Serializable
data class SerializableAvailability(
    val day: String = "",
    val startTime: String = "",
    val endTime: String = ""
)

/* Main pair of function to serialize/deserialize User objects to/from JSON strings */

fun serializeUser(user: User): String {
    val serialized =
        SerializableUser(
            user.uid,
            user.username,
            user.email,
            user.profilePicture,
            user.skillSet.map { serializeSkill(it) }.joinToString { "|" },
            user.rating,
            user.availability.map { serializeAvailability(it) }.joinToString { "|" }
        )
    return Json.encodeToString(serialized)
}

fun deserializeUser(user: String): User {
    val deserialized = Json.decodeFromString<SerializableUser>(user)
    return User(
        deserialized.uid,
        deserialized.username,
        deserialized.email,
        deserialized.profilePicture,
        if (deserialized.skillSet.isEmpty()) setOf()
        else deserialized.skillSet.split("|").map { deserializeSkill(it) }.toSet(),
        deserialized.rating,
        if (deserialized.availability.isEmpty()) listOf()
        else deserialized.availability.split("|").map { deserializeAvailability(it) }
    )
}

/* HELPER FUNCTIONS */
fun serializeSkill(skill: Skill): String {
    val serialized = SerializableSkill(skill.name.name, skill.rank, skill.description)
    return Json.encodeToString(serialized)
}

fun deserializeSkill(skill: String): Skill {
    val deserialized = Json.decodeFromString<SerializableSkill>(skill)
    return Skill(SkillName.valueOf(deserialized.name), deserialized.rank, deserialized.description)
}

fun serializeAvailability(availability: Availability): String {
    val serialized =
        SerializableAvailability(
            availability.day.name,
            availability.startTime.toString(),
            availability.endTime.toString()
        )
    return Json.encodeToString(serialized)
}

fun deserializeAvailability(availability: String): Availability {
    val deserialized = Json.decodeFromString<SerializableAvailability>(availability)
    return Availability(
        DayOfWeek.valueOf(deserialized.day),
        LocalTime.parse(deserialized.startTime),
        LocalTime.parse(deserialized.endTime)
    )
}

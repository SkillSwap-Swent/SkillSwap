/*
 * /!\ Written with help of Copilot
 * > complete all the repetitive code (construction of instances for example)
 * > helped me with all the plugin management stuff
 * > some special functions like joinToString()
 */

package com.swent.skillswap.model.user

import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.serialization.Serializable
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
 * Serializable version of Skill class to convert to/from JSON automatically with kotlinx.serialization
 */
@Serializable
data class SerializableSkill(
    val name: String = "",
    val rank: Float = 0.0f,
    val description: String = ""
)

/*
 * Serializable version of Availability class to convert to/from JSON automatically with kotlinx.serialization
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
            serializeSkills(user.skillSet),
            user.rating,
            serializeAvailabilities(user.availability)
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
        deserializeSkills(deserialized.skillSet),
        deserialized.rating,
        deserializeAvailabilities(deserialized.availability)
    )
}

/* HELPER FUNCTIONS */
/*
 * These functions serialize/deserialize sets of skills and lists of availabilities
 */
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
 * These functions serialize/deserialize single skills and single availabilities
 */
fun serializeSingleSkill(skill: Skill): String {
    val serialized = SerializableSkill(skill.name.name, skill.rank, skill.description)
    return Json.encodeToString(serialized)
}

fun deserializeSingleSkill(skill: String): Skill {
    val deserialized = Json.decodeFromString<SerializableSkill>(skill)
    return Skill(SkillName.valueOf(deserialized.name), deserialized.rank, deserialized.description)
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

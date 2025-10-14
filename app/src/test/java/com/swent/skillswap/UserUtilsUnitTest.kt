package com.swent.skillswap

import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.*
import java.time.DayOfWeek
import java.time.LocalTime
import org.junit.Assert.*
import org.junit.Test

/** User utility unit test */
class UserUtilsUnitTest {

    @Test
    fun correctSerializationOfSkill() {
        val skill = Skill(SkillTag.LINEAR_ALGEBRA, 3f, "")
        val serialized = serializeSingleSkill(skill)
        assertEquals(skill, deserializeSingleSkill(serialized))
    }

    @Test
    fun correctSerializationOfAvailability() {
        val availability = Availability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0))
        val serialized = serializeSingleAvailability(availability)
        assertEquals(availability, deserializeSingleAvailability(serialized))
    }

    @Test
    fun correctSerializationOfSetOfSkills() {
        val skill1 = Skill(SkillTag.LINEAR_ALGEBRA, 3f, "qnfj3of")
        val skill2 = Skill(SkillTag.CHEMISTRY, 2f, "qnededefj3of")
        val skills = setOf(skill1, skill2)
        val serialized = serializeSkills(skills)
        assertEquals(skills, deserializeSkills(serialized))
    }

    @Test
    fun correctSerializationOfListOfAvailabilities() {
        val availability1 = Availability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0))
        val availability2 =
            Availability(DayOfWeek.WEDNESDAY, LocalTime.of(10, 0), LocalTime.of(15, 0))
        val availabilities = listOf(availability1, availability2)
        val serialized = serializeAvailabilities(availabilities)
        assertEquals(availabilities, deserializeAvailabilities(serialized))
    }

    @Test
    fun illegalSerializableInstantiations_doNotThrow() {
        SerializableSkill("", -1f, "desc")
        SerializableSkill("qejndb", Float.NaN, "desc")
        SerializableAvailability("", "09:00", "17:00")
        SerializableAvailability("MONDAY", "", "17:00")
        SerializableAvailability("MONDAY", "09:00", "")
    }
}

package com.swent.skillswap.model.user

import com.swent.skillswap.model.tags.SkillTag
import java.time.DayOfWeek
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class UserUtilsSerializationTest {

    @Test
    fun serializeDeserializeSkills_roundTrip() {
        val skills = setOf(
            Skill(SkillTag.CALCULUS, 3.5f, "Good at calc"),
            Skill(SkillTag.COMPUTER_PROGRAMMING, 5.0f, "Kotlin/Compose")
        )
        val encoded = serializeSkills(skills)
        val decoded = deserializeSkills(encoded)
        assertEquals(skills.size, decoded.size)
        assertEquals(skills.map { it.name }.toSet(), decoded.map { it.name }.toSet())
    }

    @Test
    fun serializeDeserializeAvailability_roundTrip() {
        val avail = listOf(
            Availability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0)),
            Availability(DayOfWeek.WEDNESDAY, LocalTime.of(14, 0), LocalTime.of(18, 0))
        )
        val encoded = serializeAvailabilities(avail)
        val decoded = deserializeAvailabilities(encoded)
        assertEquals(avail, decoded)
    }
}



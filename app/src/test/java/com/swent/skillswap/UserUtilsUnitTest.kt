package com.swent.skillswap

import com.swent.skillswap.model.user.*
import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

/** User utility unit test */
class UserUtilsTest {

    @Test
    fun correctSerializationOfSkill() {
        val skill = Skill(SkillName.LINEAR_ALGEBRA, 3f, "")
        val serialized = serializeSkill(skill)
        assertEquals(skill, deserializeSkill(serialized))
    }

    @Test
    fun correctSerializationOfAvailability() {
        val availability = Availability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0))
        val serialized = serializeAvailability(availability)
        assertEquals("MONDAY,09:00,17:00", serialized)
    }
}
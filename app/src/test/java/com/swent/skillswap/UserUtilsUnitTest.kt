package com.swent.skillswap

import com.swent.skillswap.model.user.*
import java.time.DayOfWeek
import java.time.LocalTime
import org.junit.Assert.*
import org.junit.Test

/** User utility unit test */
class UserUtilsUnitTest {

    @Test
    fun correctSerializationOfSkill() {
        val skill = Skill(SkillName.LINEAR_ALGEBRA, 3f, "")
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
        val skill1 = Skill(SkillName.LINEAR_ALGEBRA, 3f, "qnfj3of")
        val skill2 = Skill(SkillName.CHEMISTRY, 2f, "qnededefj3of")
        val skills = setOf(skill1, skill2)
        val serialized = serializeSkills(skills)
        print(serialized + "\n")
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
    fun correctSerializationOfUser() {
        val skill1 = Skill(SkillName.LINEAR_ALGEBRA, 3f, "qnfj3of")
        val skill2 = Skill(SkillName.CHEMISTRY, 2f, "qnededefj3of")

        val availability = Availability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0))
        val user =
            User(
                uid = "123",
                username = "testuser",
                email = "test@example.com",
                profilePicture = "pic_url",
                skillSet = setOf(skill1, skill2),
                rating = 4.5f,
                availability = listOf(availability)
            )

        val serialized = serializeUser(user)
        assertEquals(user, deserializeUser(serialized))
    }
}

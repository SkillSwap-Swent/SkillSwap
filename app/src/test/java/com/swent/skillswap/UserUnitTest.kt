package com.swent.skillswap

import com.swent.skillswap.model.user.Availability
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.SkillName
import com.swent.skillswap.model.user.User
import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * User local unit test
 */
class UserUnitTest {

  @Test
  fun createUserAndCheckProperties() {
    val skill = Skill(SkillName.LINEAR_ALGEBRA,3f,"")
    val availability = Availability(
      DayOfWeek.MONDAY,
      LocalTime.of(9, 0),
      LocalTime.of(17, 0)
    )
    val user = User(
      uid = "123",
      username = "testuser",
      email = "test@example.com",
      profilePicture = "pic_url",
      skillSet = setOf(skill),
      rating = 4.5f,
      availability = listOf(availability)
    )

    assertEquals("123", user.uid)
    assertEquals("testuser", user.username)
    assertEquals("test@example.com", user.email)
    assertEquals("pic_url", user.profilePicture)
    assertTrue(user.skillSet.contains(skill))
    assertEquals(4.5f, user.rating)
    assertEquals(1, user.availability.size)
    assertEquals(DayOfWeek.MONDAY, user.availability[0].day)
  }

  @Test
  fun testUserEqualityAndCopy() {
    val skill = Skill(SkillName.LINEAR_ALGEBRA,3f,"")
    val availability = Availability(
      DayOfWeek.TUESDAY,
      LocalTime.of(10, 0),
      LocalTime.of(18, 0)
    )
    val user1 = User(
      uid = "abc",
      username = "user1",
      email = "user1@example.com",
      profilePicture = "url1",
      skillSet = setOf(skill),
      rating = 5.0f,
      availability = listOf(availability)
    )
    val user2 = user1.copy()
    assertEquals(user1, user2)
    val user3 = user1.copy(username = "user2")
    assertNotEquals(user1, user3)
  }
}
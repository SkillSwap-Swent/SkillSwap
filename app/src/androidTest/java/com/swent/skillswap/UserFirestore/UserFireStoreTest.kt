/*
 * @author: Léo. MARTI
 * /!\ Written with help of Copilot
 * > Help me initilaize the firebase emulator for testing
 * > complete all the repetitive code (construction of instances for example)
 */

package com.swent.skillswap.UserFirestore

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.skillswap.model.map.Location
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.Preference
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.utils.FirebaseEmulator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserFireStoreTest {

    lateinit var repo: UserRepoFirestore
    lateinit var db: FirebaseFirestore

    init {
        FirebaseEmulator.startEmulator()
        db = FirebaseEmulator.firestore
        repo = UserRepoFirestore(db)
    }

    @Before
    fun setUp() = runBlocking {
        val users = FirebaseEmulator.firestore.collection("users").get().await()
        for (doc in users.documents) {
            FirebaseEmulator.firestore.collection("users").document(doc.id).delete().await()
        }
    }

    @Test
    fun addAndRetrieveUserwithemptySkillAndAvaibility() = runBlocking {
        val uid = repo.getNewUid()
        val user =
            User(
                uid = uid,
                username = "testuser",
                email = "test@example.com",
                profilePicture = "",
                skillSet = setOf(),
                rating = 5.0f,
                availability = listOf(),
                preference = Preference.SKILLS,
                location = Location(0.0, 0.0, "test_location")
            )

        repo.addUser(user)
        val retrievedUser = repo.getUser(uid)

        assertNotNull(retrievedUser)
        assertEquals(user.uid, retrievedUser.uid)
        assertEquals(user.username, retrievedUser.username)
        assertEquals(user.email, retrievedUser.email)
        assertEquals(user.rating, retrievedUser.rating)
        assertEquals(user.skillSet, retrievedUser.skillSet)
        assertEquals(user.availability, retrievedUser.availability)
        assertEquals(user.preference, retrievedUser.preference)
        assertEquals(user.location, retrievedUser.location)
    }

    @Test
    fun addAndRetrieveUserwithSkillAndAvaibility() = runBlocking {
        val skill1 =
            Skill(
                name = SkillTag.COMPUTER_PROGRAMMING,
                rank = 4.5f,
                description = "I love programming"
            )
        val skill2 = Skill(name = SkillTag.DATABASES, rank = 3.0f, description = "database is ez")

        val availability1 =
            com.swent.skillswap.model.user.Availability(
                day = java.time.DayOfWeek.MONDAY,
                startTime = java.time.LocalTime.of(9, 0),
                endTime = java.time.LocalTime.of(11, 0)
            )
        val availability2 =
            com.swent.skillswap.model.user.Availability(
                day = java.time.DayOfWeek.WEDNESDAY,
                startTime = java.time.LocalTime.of(14, 0),
                endTime = java.time.LocalTime.of(16, 0)
            )

        val uid = repo.getNewUid()
        val user =
            User(
                uid = uid,
                username = "testuser2",
                email = "endenejnd@mdek.ch",
                profilePicture = "",
                skillSet = setOf(skill1, skill2),
                rating = 4.0f,
                availability = listOf(availability1, availability2),
                preference = Preference.MONEY,
                location = Location(46.5191, 6.5668, "EPFL")
            )

        repo.addUser(user)

        assertEquals(user, repo.getUser(uid))
    }

    @Test
    fun editUser() = runBlocking {
        val uid = repo.getNewUid()
        val basicUser =
            User(
                uid = uid,
                username = "testuser",
                email = "test@example.com",
                profilePicture = "",
                skillSet = setOf(),
                rating = 5.0f,
                availability = listOf(),
                preference = Preference.SKILLS,
                location = Location(0.0, 0.0, "initial_location")
            )

        repo.addUser(basicUser)
        assertEquals(basicUser, repo.getUser(uid))

        val skill1 =
            Skill(
                name = SkillTag.COMPUTER_PROGRAMMING,
                rank = 4.5f,
                description = "I love programming"
            )
        val skill2 = Skill(name = SkillTag.DATABASES, rank = 3.0f, description = "database is ez")

        val availability1 =
            com.swent.skillswap.model.user.Availability(
                day = java.time.DayOfWeek.MONDAY,
                startTime = java.time.LocalTime.of(9, 0),
                endTime = java.time.LocalTime.of(11, 0)
            )
        val availability2 =
            com.swent.skillswap.model.user.Availability(
                day = java.time.DayOfWeek.WEDNESDAY,
                startTime = java.time.LocalTime.of(14, 0),
                endTime = java.time.LocalTime.of(16, 0)
            )

        val editedUser =
            User(
                uid = uid,
                username = "testuser2",
                email = "endenejnd@mdek.ch",
                profilePicture = "pic.url",
                skillSet = setOf(skill1, skill2),
                rating = 4.0f,
                availability = listOf(availability1, availability2),
                preference = Preference.MONEY,
                location = Location(48.8566, 2.3522, "Paris")
            )

        repo.editUser(basicUser.uid, editedUser)
        assertEquals(editedUser, repo.getUser(uid))
    }

    @Test
    fun deleteSingleUserFromMultiple() = runBlocking {
        val uid1 = repo.getNewUid()
        val user1 =
            User(
                uid = uid1,
                username = "testuser",
                email = "j'ailadalle@gmail.com",
                profilePicture = "",
                skillSet = setOf(),
                rating = 5.0f,
                availability = listOf(),
                preference = Preference.SKILLS,
                location = Location(40.7128, -74.0060, "New York")
            )

        val skill1 =
            Skill(
                name = SkillTag.COMPUTER_PROGRAMMING,
                rank = 4.5f,
                description = "I love programming"
            )
        val skill2 = Skill(name = SkillTag.DATABASES, rank = 3.0f, description = "database is ez")

        val availability1 =
            com.swent.skillswap.model.user.Availability(
                day = java.time.DayOfWeek.MONDAY,
                startTime = java.time.LocalTime.of(9, 0),
                endTime = java.time.LocalTime.of(11, 0)
            )
        val availability2 =
            com.swent.skillswap.model.user.Availability(
                day = java.time.DayOfWeek.WEDNESDAY,
                startTime = java.time.LocalTime.of(14, 0),
                endTime = java.time.LocalTime.of(16, 0)
            )

        val uid2 = repo.getNewUid()
        val user2 =
            User(
                uid = uid2,
                username = "testuser2",
                email = "endenejnd@mdek.ch",
                profilePicture = "pic.url",
                skillSet = setOf(skill1, skill2),
                rating = 4.0f,
                availability = listOf(availability1, availability2),
                preference = Preference.MONEY,
                location = Location(51.5074, -0.1278, "London")
            )

        repo.addUser(user1)
        repo.addUser(user2)

        repo.deleteUser(user1.uid)

        assertThrows(Exception::class.java) { runBlocking { repo.getUser(user1.uid) } }

        assertEquals(user2, repo.getUser(user2.uid))
    }

    @Test
    fun editUserLocation() = runBlocking {
        val uid = repo.getNewUid()
        val initialLocation = Location(46.5191, 6.5668, "EPFL")
        val user =
            User(
                uid = uid,
                username = "locationtester",
                email = "location@test.com",
                profilePicture = "",
                skillSet = setOf(),
                rating = 5.0f,
                availability = listOf(),
                preference = Preference.SKILLS,
                location = initialLocation
            )

        repo.addUser(user)
        assertEquals(initialLocation, repo.getUser(uid).location)

        val newLocation = Location(48.8566, 2.3522, "Paris, France")
        val updatedUser = user.copy(location = newLocation)

        repo.editUser(uid, updatedUser)
        val retrievedUser = repo.getUser(uid)

        assertEquals(newLocation.latitude, retrievedUser.location.latitude, 0.0001)
        assertEquals(newLocation.longitude, retrievedUser.location.longitude, 0.0001)
        assertEquals(newLocation.name, retrievedUser.location.name)
    }

    @Test
    fun editUserPreference() = runBlocking {
        val uid = repo.getNewUid()
        val user =
            User(
                uid = uid,
                username = "preferencetester",
                email = "preference@test.com",
                profilePicture = "",
                skillSet = setOf(),
                rating = 5.0f,
                availability = listOf(),
                preference = Preference.SKILLS,
                location = Location(0.0, 0.0, "test")
            )

        repo.addUser(user)
        assertEquals(Preference.SKILLS, repo.getUser(uid).preference)

        val updatedUser = user.copy(preference = Preference.MONEY)
        repo.editUser(uid, updatedUser)

        assertEquals(Preference.MONEY, repo.getUser(uid).preference)
    }

    @Test
    fun testMultipleLocationsWithDifferentCoordinates() = runBlocking {
        val uid1 = repo.getNewUid()
        val user1 =
            User(
                uid = uid1,
                username = "user1",
                email = "user1@test.com",
                profilePicture = "",
                skillSet = setOf(),
                rating = 5.0f,
                availability = listOf(),
                preference = Preference.SKILLS,
                location = Location(46.5191, 6.5668, "EPFL, Switzerland")
            )

        val uid2 = repo.getNewUid()
        val user2 =
            User(
                uid = uid2,
                username = "user2",
                email = "user2@test.com",
                profilePicture = "",
                skillSet = setOf(),
                rating = 5.0f,
                availability = listOf(),
                preference = Preference.MONEY,
                location = Location(35.6762, 139.6503, "Tokyo, Japan")
            )

        repo.addUser(user1)
        repo.addUser(user2)

        val retrievedUser1 = repo.getUser(uid1)
        val retrievedUser2 = repo.getUser(uid2)

        assertEquals(user1.location, retrievedUser1.location)
        assertEquals(user2.location, retrievedUser2.location)
        assertNotEquals(retrievedUser1.location, retrievedUser2.location)
    }

    @Test
    fun testBothPreferenceValues() = runBlocking {
        val uid1 = repo.getNewUid()
        val userWithSkillsPreference =
            User(
                uid = uid1,
                username = "skillsuser",
                email = "skills@test.com",
                profilePicture = "",
                skillSet = setOf(),
                rating = 5.0f,
                availability = listOf(),
                preference = Preference.SKILLS,
                location = Location(0.0, 0.0, "test")
            )

        val uid2 = repo.getNewUid()
        val userWithMoneyPreference =
            User(
                uid = uid2,
                username = "moneyuser",
                email = "money@test.com",
                profilePicture = "",
                skillSet = setOf(),
                rating = 5.0f,
                availability = listOf(),
                preference = Preference.MONEY,
                location = Location(0.0, 0.0, "test")
            )

        repo.addUser(userWithSkillsPreference)
        repo.addUser(userWithMoneyPreference)

        assertEquals(Preference.SKILLS, repo.getUser(uid1).preference)
        assertEquals(Preference.MONEY, repo.getUser(uid2).preference)
    }
}

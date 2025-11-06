/*
 * @author: Léo. MARTI
 * /!\ Written with help of Copilot
 * > Help me initilaize the firebase emulator for testing
 * > complete all the repetitive code (construction of instances for example)
 */

package com.swent.skillswap.UserFirestore

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.Preference
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.utils.FirebaseEmulator
import java.time.DayOfWeek
import java.time.LocalTime
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

    // Helper functions
    private fun createAvailability(
        day: DayOfWeek,
        startHour: Int,
        startMin: Int,
        endHour: Int,
        endMin: Int
    ) =
        com.swent.skillswap.model.user.Availability(
            day = day,
            startTime = LocalTime.of(startHour, startMin),
            endTime = LocalTime.of(endHour, endMin)
        )

    private fun createSkill(name: SkillTag, rank: Float, description: String) =
        Skill(name = name, rank = rank, description = description)

    private fun createUser(
        uid: String,
        username: String,
        email: String,
        profilePicture: String = "",
        skillSet: Set<Skill> = setOf(),
        rating: Float = 5.0f,
        availability: List<com.swent.skillswap.model.user.Availability> = listOf(),
        preference: Preference = Preference.SKILLS,
        location: GeoPoint = GeoPoint(0.0, 0.0)
    ) =
        User(
            uid = uid,
            username = username,
            email = email,
            profilePicture = profilePicture,
            skillSet = skillSet,
            rating = rating,
            availability = availability,
            preference = preference,
            location = location
        )

    @Test
    fun addAndRetrieveUserwithemptySkillAndAvaibility() = runBlocking {
        val uid = repo.getNewUid()
        val user =
            createUser(
                uid = uid,
                username = "testuser",
                email = "test@example.com",
                location = GeoPoint(0.0, 0.0)
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
        val skill1 = createSkill(SkillTag.COMPUTER_PROGRAMMING, 4.5f, "I love programming")
        val skill2 = createSkill(SkillTag.DATABASES, 3.0f, "database is ez")

        val availability1 = createAvailability(DayOfWeek.MONDAY, 9, 0, 11, 0)
        val availability2 = createAvailability(DayOfWeek.WEDNESDAY, 14, 0, 16, 0)

        val uid = repo.getNewUid()
        val user =
            createUser(
                uid = uid,
                username = "testuser2",
                email = "endenejnd@mdek.ch",
                skillSet = setOf(skill1, skill2),
                rating = 4.0f,
                availability = listOf(availability1, availability2),
                preference = Preference.MONEY,
                location = GeoPoint(46.5191, 6.5668)
            )

        repo.addUser(user)
        assertEquals(user, repo.getUser(uid))
    }

    @Test
    fun editUser() = runBlocking {
        val uid = repo.getNewUid()
        val basicUser =
            createUser(
                uid = uid,
                username = "testuser",
                email = "test@example.com",
                location = GeoPoint(0.0, 0.0)
            )

        repo.addUser(basicUser)
        assertEquals(basicUser, repo.getUser(uid))

        val skill1 = createSkill(SkillTag.COMPUTER_PROGRAMMING, 4.5f, "I love programming")
        val skill2 = createSkill(SkillTag.DATABASES, 3.0f, "database is ez")

        val availability1 = createAvailability(DayOfWeek.MONDAY, 9, 0, 11, 0)
        val availability2 = createAvailability(DayOfWeek.WEDNESDAY, 14, 0, 16, 0)

        val editedUser =
            createUser(
                uid = uid,
                username = "testuser2",
                email = "endenejnd@mdek.ch",
                profilePicture = "pic.url",
                skillSet = setOf(skill1, skill2),
                rating = 4.0f,
                availability = listOf(availability1, availability2),
                preference = Preference.MONEY,
                location = GeoPoint(48.8566, 2.3522)
            )

        repo.editUser(basicUser.uid, editedUser)
        assertEquals(editedUser, repo.getUser(uid))
    }

    @Test
    fun deleteSingleUserFromMultiple() = runBlocking {
        val uid1 = repo.getNewUid()
        val user1 =
            createUser(
                uid = uid1,
                username = "testuser",
                email = "j'ailadalle@gmail.com",
                location = GeoPoint(40.7128, -74.0060)
            )

        val skill1 = createSkill(SkillTag.COMPUTER_PROGRAMMING, 4.5f, "I love programming")
        val skill2 = createSkill(SkillTag.DATABASES, 3.0f, "database is ez")

        val availability1 = createAvailability(DayOfWeek.MONDAY, 9, 0, 11, 0)
        val availability2 = createAvailability(DayOfWeek.WEDNESDAY, 14, 0, 16, 0)

        val uid2 = repo.getNewUid()
        val user2 =
            createUser(
                uid = uid2,
                username = "testuser2",
                email = "endenejnd@mdek.ch",
                profilePicture = "pic.url",
                skillSet = setOf(skill1, skill2),
                rating = 4.0f,
                availability = listOf(availability1, availability2),
                preference = Preference.MONEY,
                location = GeoPoint(51.5074, -0.1278)
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
        val initialLocation = GeoPoint(46.5191, 6.5668)
        val user =
            createUser(
                uid = uid,
                username = "locationtester",
                email = "location@test.com",
                location = initialLocation
            )

        repo.addUser(user)
        assertEquals(initialLocation, repo.getUser(uid).location)

        val newLocation = GeoPoint(48.8566, 2.3522)
        val updatedUser = user.copy(location = newLocation)

        repo.editUser(uid, updatedUser)
        val retrievedUser = repo.getUser(uid)

        assertEquals(newLocation.latitude, retrievedUser.location.latitude, 0.0001)
        assertEquals(newLocation.longitude, retrievedUser.location.longitude, 0.0001)
    }

    @Test
    fun editUserPreference() = runBlocking {
        val uid = repo.getNewUid()
        val user =
            createUser(
                uid = uid,
                username = "preferencetester",
                email = "preference@test.com",
                location = GeoPoint(0.0, 0.0)
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
            createUser(
                uid = uid1,
                username = "user1",
                email = "user1@test.com",
                location = GeoPoint(46.5191, 6.5668)
            )

        val uid2 = repo.getNewUid()
        val user2 =
            createUser(
                uid = uid2,
                username = "user2",
                email = "user2@test.com",
                preference = Preference.MONEY,
                location = GeoPoint(35.6762, 139.6503)
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
            createUser(
                uid = uid1,
                username = "skillsuser",
                email = "skills@test.com",
                preference = Preference.SKILLS,
                location = GeoPoint(0.0, 0.0)
            )

        val uid2 = repo.getNewUid()
        val userWithMoneyPreference =
            createUser(
                uid = uid2,
                username = "moneyuser",
                email = "money@test.com",
                preference = Preference.MONEY,
                location = GeoPoint(0.0, 0.0)
            )

        repo.addUser(userWithSkillsPreference)
        repo.addUser(userWithMoneyPreference)

        assertEquals(Preference.SKILLS, repo.getUser(uid1).preference)
        assertEquals(Preference.MONEY, repo.getUser(uid2).preference)
    }
}

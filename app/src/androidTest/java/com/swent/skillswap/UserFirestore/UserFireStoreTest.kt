/*
 * @author: Léo. MARTI
 * /!\ Written with help of Copilot
 * > Help me initilaize the firebase emulator for testing
 * > complete all the repetitive code (construction of instances for example)
 */

package com.swent.skillswap.UserFirestore

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.USERS_COLLECTION_PATH
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.utils.FirebaseEmulator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
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
        db = FirebaseEmulator.firestore // get the firestore instance pointing to the emulator
        repo = UserRepoFirestore(db) // initialize the repository
    }

    // Nettoie la collection users avant chaque test
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
                availability = listOf()
            )

        // Ajout de l'utilisateur

        repo.addUser(user)
        // Récupération de l'utilisateur
        val retrievedUser = repo.getUser(uid)

        assertNotNull(retrievedUser)
        assertEquals(user.uid, retrievedUser.uid)
        assertEquals(user.username, retrievedUser.username)
        assertEquals(user.email, retrievedUser.email)
        assertEquals(user.rating, retrievedUser.rating)
        assertEquals(user.skillSet, retrievedUser.skillSet)
        assertEquals(user.availability, retrievedUser.availability)
    }

    @Test
    fun addAndRetrieveUserwithSkillAndAvaibility() = runBlocking {
        val skill1 = Skill(
            name = SkillTag.COMPUTER_PROGRAMMING,
            rank = 4.5f,
            description = "I love programming"
        )
        val skill2 = Skill(
            name = SkillTag.DATABASES,
            rank = 3.0f,
            description = "database is ez"
        )

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
                availability = listOf(availability1, availability2)
            )

        //add user in firestore
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
                availability = listOf()
            )
        //add the basci user
        repo.addUser(basicUser)

        assertEquals(basicUser, repo.getUser(uid))

        //the edited user
        val skill1 = Skill(
            name = SkillTag.COMPUTER_PROGRAMMING,
            rank = 4.5f,
            description = "I love programming"
        )
        val skill2 = Skill(
            name = SkillTag.DATABASES,
            rank = 3.0f,
            description = "database is ez"
        )

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
                availability = listOf(availability1, availability2)
            )

        repo.editUser(basicUser.uid, editedUser)
        assertEquals(editedUser, repo.getUser(uid))
    }

    @Test
    fun deleteSingleUserFromMultiple() = runBlocking {

        //User 1
        val uid1 = repo.getNewUid()
        val user1 =
            User(
                uid = uid1,
                username = "testuser",
                email = "j'ailadalle@gmail.com",
                profilePicture = "",
                skillSet = setOf(),
                rating = 5.0f,
                availability = listOf()
            )

        //User 2
        val skill1 = Skill(
            name = SkillTag.COMPUTER_PROGRAMMING,
            rank = 4.5f,
            description = "I love programming"
        )
        val skill2 = Skill(
            name = SkillTag.DATABASES,
            rank = 3.0f,
            description = "database is ez"
        )

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
                availability = listOf(availability1, availability2)
            )

        repo.addUser(user1)
        repo.addUser(user2)

        //delete user 1
        repo.deleteUser(user1.uid)

        //get user 1 should throw an exception
        assertThrows(Exception::class.java) { runBlocking{repo.getUser(user1.uid)}}

        //get user 2 should work
        assertEquals(user2, repo.getUser(user2.uid))
    }
}

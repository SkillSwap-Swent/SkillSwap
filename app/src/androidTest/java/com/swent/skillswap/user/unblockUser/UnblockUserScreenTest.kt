package com.swent.skillswap.user.unblockUser

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.user.Preference
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.model.user.UserRepositery
import com.swent.skillswap.ui.user.unblockUser.UnblockUserScreen
import com.swent.skillswap.ui.user.unblockUser.UnblockUserScreenTestTag
import com.swent.skillswap.ui.user.unblockUser.UnblockUserViewModel
import com.swent.skillswap.utils.FirebaseEmulator
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UnblockUserScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var viewModel: UnblockUserViewModel
    private lateinit var userRepository: UserRepositery

    private lateinit var currentUserId: String
    private lateinit var blockedUser1: String
    private lateinit var blockedUser2: String
    private lateinit var currentUserNoBlock: User
    private lateinit var currentUser: User

    @Before
    fun setup() = runBlocking {
        FirebaseEmulator.startEmulator()
        val firestore = FirebaseFirestore.getInstance()
        userRepository = UserRepoFirestore(firestore)

        FirebaseEmulator.auth.createUserWithEmailAndPassword("main@test.com", "password123").await()

        val authResult =
            FirebaseEmulator.auth.signInWithEmailAndPassword("main@test.com", "password123").await()
        currentUserId = authResult.user!!.uid
        blockedUser1 = userRepository.getNewUid()
        blockedUser2 = userRepository.getNewUid()
        currentUser =
            User(
                uid = currentUserId,
                username = "MainUser",
                email = "main@test.com",
                profilePicture = "",
                skillSet = emptySet(),
                rating = 0f,
                availability = emptyList(),
                preference = Preference.SKILLS,
                location = GeoPoint(0.0, 0.0),
                blockedUsers = setOf(blockedUser1, blockedUser2),
                fcmToken = null
            )

        currentUserNoBlock = currentUser.copy(blockedUsers = emptySet())

        val user1 =
            User(
                uid = blockedUser1,
                username = "Alice",
                email = "alice@test.com",
                profilePicture = "",
                skillSet = emptySet(),
                rating = 0f,
                availability = emptyList(),
                preference = Preference.SKILLS,
                location = GeoPoint(0.0, 0.0),
                blockedUsers = emptySet(),
                fcmToken = null
            )

        val user2 =
            User(
                uid = blockedUser2,
                username = "Bob",
                email = "bob@test.com",
                profilePicture = "https://avatars.com/bob.png",
                skillSet = emptySet(),
                rating = 0f,
                availability = emptyList(),
                preference = Preference.SKILLS,
                location = GeoPoint(0.0, 0.0),
                blockedUsers = emptySet(),
                fcmToken = null
            )

        // 5. Add users to Firestore
        userRepository.addUser(currentUser)
        userRepository.addUser(user1)
        userRepository.addUser(user2)
        waitForDocumentToExist(
            firestore = firestore,
            collectionPath = "users",
            documentId = currentUserId
        )

        viewModel = UnblockUserViewModel(userRepository)
    }

    suspend fun waitForDocumentToExist(
        firestore: FirebaseFirestore,
        collectionPath: String,
        documentId: String,
        timeoutMillis: Long = 5000
    ) {
        try {
            withTimeout(timeoutMillis) {
                while (true) {
                    val snapshot =
                        firestore.collection(collectionPath).document(documentId).get().await()

                    if (snapshot.exists()) {
                        return@withTimeout
                    }
                    delay(100)
                }
            }
        } catch (e: Exception) {
            println("Wait for document timed out or failed: $e")
        }
    }

    @After
    fun tearDown() = runBlocking {
        FirebaseEmulator.auth.signOut()
        FirebaseEmulator.clearAuthEmulator()
        FirebaseEmulator.clearFirestoreEmulator()
    }

    @Test
    fun emptyScreen_displaysEmptyText() = runBlocking {
        userRepository.editUser(currentUserId, currentUserNoBlock)
        val emptyStateViewModel = UnblockUserViewModel(userRepository)
        composeTestRule.setContent {
            UnblockUserScreen(
                viewModel = emptyStateViewModel, // Use the new specific instance
                onAvatarClick = {},
                onGoBack = {}
            )
        }
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule
                    .onNodeWithTag(UnblockUserScreenTestTag.EMPTY_TEXT)
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        userRepository.editUser(currentUserId, currentUser)
    }

    @Test
    fun screen_displaysAllUserCards() {
        composeTestRule.setContent {
            UnblockUserScreen(viewModel = viewModel, onAvatarClick = {}, onGoBack = {})
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag(UnblockUserScreenTestTag.PROFILE_CARD)
                .fetchSemanticsNodes()
                .size == 2
        }

        composeTestRule.onNodeWithTag(UnblockUserScreenTestTag.UNBLOCK_TITLE).assertIsDisplayed()

        composeTestRule
            .onAllNodesWithTag(UnblockUserScreenTestTag.PROFILE_CARD)
            .assertCountEquals(2)
    }

    @Test
    fun unblockButton_triggersViewModelAndUpdatesRepo() = runBlocking {
        composeTestRule.setContent {
            UnblockUserScreen(viewModel = viewModel, onAvatarClick = {}, onGoBack = {})
        }

        composeTestRule.waitUntil(
            timeoutMillis = 5000,
            condition = {
                composeTestRule
                    .onAllNodesWithTag(
                        UnblockUserScreenTestTag.UNBLOCK_BUTTON,
                        useUnmergedTree = true
                    )
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
        )

        composeTestRule
            .onAllNodesWithTag(UnblockUserScreenTestTag.UNBLOCK_BUTTON, useUnmergedTree = true)[0]
            .performClick()

        // Allow some time for the background coroutine in VM to update Repo
        // (Since VM.unblockUser calls repo.editUser inside a coroutine)
        delay(500)

        val updatedUser = userRepository.getUser(currentUserId)
        assertEquals(1, updatedUser.blockedUsers.size)
    }

    @Test
    fun backButton_isDisplayed() {
        composeTestRule.setContent {
            UnblockUserScreen(viewModel = viewModel, onAvatarClick = {}, onGoBack = {})
        }

        composeTestRule.waitUntil(
            timeoutMillis = 5000,
            condition = {
                composeTestRule
                    .onAllNodesWithTag(UnblockUserScreenTestTag.BACK_BUTTON)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
        )

        composeTestRule.onNodeWithTag(UnblockUserScreenTestTag.BACK_BUTTON).assertIsDisplayed()
    }

    @Test
    fun avatarClick_triggersCallback() {
        var clickCount = 0
        composeTestRule.setContent {
            UnblockUserScreen(
                viewModel = viewModel,
                onAvatarClick = { clickCount++ },
                onGoBack = {}
            )
        }
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag(UnblockUserScreenTestTag.PROFILE_AVATAR, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .size == 2
        }
        composeTestRule
            .onAllNodesWithTag(UnblockUserScreenTestTag.PROFILE_AVATAR, useUnmergedTree = true)[0]
            .performClick()
        assertEquals(1, clickCount)
        composeTestRule
            .onAllNodesWithTag(UnblockUserScreenTestTag.PROFILE_AVATAR, useUnmergedTree = true)[1]
            .performClick()
        assertEquals(2, clickCount)
    }
}

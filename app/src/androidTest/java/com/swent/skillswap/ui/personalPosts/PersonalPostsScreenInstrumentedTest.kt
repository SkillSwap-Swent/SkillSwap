/** @author Younes Belgroune - Made with the help of AI */
package com.swent.skillswap.ui.personalPosts

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.post.*
import com.swent.skillswap.ui.theme.SkillSwapAppTheme
import com.swent.skillswap.utils.FirebaseEmulator
import java.util.Date
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersonalPostsScreenInstrumentedTest {
    @get:Rule val composeRule = createComposeRule()
    private lateinit var testUserId: String
    private lateinit var testLocation: GeoPoint

    @Before
    fun setUp() {
        runBlocking {
            FirebaseEmulator.startEmulator()
            FirebaseEmulator.clearFirestoreEmulator()
            testUserId =
                FirebaseAuth.getInstance().signInAnonymously().await().user?.uid ?: "test-user"
            testLocation = GeoPoint(46.5191, 6.5668)
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            FirebaseAuth.getInstance().signOut()
            FirebaseEmulator.clearAuthEmulator()
            FirebaseEmulator.clearFirestoreEmulator()
        }
    }

    private fun createRequest(id: String, title: String) =
        Request(
            uid = id,
            title = title,
            description = "Description",
            ownerId = testUserId,
            tags = emptySet(),
            paymentMethod = PaymentMethod.SKILLS,
            expiry = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            creation = Timestamp.now(),
            status = PostStatus.POSTED,
            media = emptyList(),
            postReplies = emptySet(),
            location = testLocation
        )

    @Test
    fun displays_title_and_back_button() {
        var backClicked = false
        composeRule.setContent {
            SkillSwapAppTheme { PersonalPostsScreen(onGoBack = { backClicked = true }) }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(PersonalPostsScreenTags.TITLE).assertIsDisplayed()
        composeRule.onAllNodesWithContentDescription("Back")[0].performClick()
        assert(backClicked)
    }

    @Test
    fun displays_loading_indicator_when_loading() {
        composeRule.setContent { SkillSwapAppTheme { PersonalPostsScreen() } }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(PersonalPostsScreenTags.LOADING_INDICATOR).assertExists()
    }

    @Test
    fun displays_empty_state_when_no_posts() {
        composeRule.setContent { SkillSwapAppTheme { PersonalPostsScreen() } }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("No posts found").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("No posts found").assertIsDisplayed()
    }

    @Test
    fun displays_posts_list_and_details() {
        runBlocking {
            PostFirestoreRepository(FirebaseEmulator.firestore)
                .addPost(createRequest("request-1", "Test Post"))
        }
        composeRule.setContent { SkillSwapAppTheme { PersonalPostsScreen() } }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Test Post").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Test Post").assertIsDisplayed()
        composeRule.onNodeWithText("Request").assertIsDisplayed()
        composeRule.onNodeWithText("Description").assertIsDisplayed()
        composeRule.onNodeWithText("Payment: SKILLS").assertIsDisplayed()
    }

    @Test
    fun displays_error_message_and_retry_button() {
        runBlocking { FirebaseAuth.getInstance().signOut() }
        composeRule.setContent { SkillSwapAppTheme { PersonalPostsScreen() } }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodesWithTag(PersonalPostsScreenTags.ERROR_MESSAGE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(PersonalPostsScreenTags.ERROR_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun edit_button_triggers_callback() {
        var editClicked = false
        runBlocking {
            PostFirestoreRepository(FirebaseEmulator.firestore)
                .addPost(createRequest("request-1", "Edit Me"))
        }
        composeRule.setContent {
            SkillSwapAppTheme { PersonalPostsScreen(onEditPost = { editClicked = true }) }
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Edit Me").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithTag(PersonalPostsScreenTags.EDIT_BUTTON)[0].performClick()
        assert(editClicked)
    }

    @Test
    fun delete_button_removes_post() {
        runBlocking {
            PostFirestoreRepository(FirebaseEmulator.firestore)
                .addPost(createRequest("request-1", "To Delete"))
        }
        composeRule.setContent { SkillSwapAppTheme { PersonalPostsScreen() } }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("To Delete").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithTag(PersonalPostsScreenTags.DELETE_BUTTON)[0].performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("To Delete").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun filter_buttons_work() {
        runBlocking {
            val repo = PostFirestoreRepository(FirebaseEmulator.firestore)
            repo.addPost(createRequest("request-1", "My Request"))
        }
        composeRule.setContent { SkillSwapAppTheme { PersonalPostsScreen() } }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("My Request").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(PersonalPostsScreenTags.FILTER_REQUESTS).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("My Request").assertIsDisplayed()
    }
}

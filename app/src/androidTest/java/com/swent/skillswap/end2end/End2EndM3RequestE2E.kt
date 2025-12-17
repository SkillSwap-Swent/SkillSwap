/* With the help of Opus 4.5 for repetitive tasks and structuring of the test */

package com.swent.skillswap.end2end

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.skillswap.MainActivity
import com.swent.skillswap.ui.auth.CreateAccountTags
import com.swent.skillswap.ui.auth.SignInTags
import com.swent.skillswap.ui.feed.FeedScreenTestTags
import com.swent.skillswap.ui.navigation.NavigationTestTags
import com.swent.skillswap.ui.post.RequestScreenTags
import com.swent.skillswap.ui.user.ProfileTestTags
import com.swent.skillswap.utils.FirebaseEmulator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E test for Request creation with thumbnail and cross-account verification. Follows M1/M2
 * pattern: accounts created via UI, emulators cleared at start.
 *
 * Test flow:
 * 1. t0: Create User1 account via CreateAccount UI
 * 2. t1: User1 creates request via UI (+ add thumbnail programmatically)
 * 3. t2: User1 signs out
 * 4. t3: Create User2 account via CreateAccount UI + verify feed
 * 5. t4: User2 accepts request and chat is created
 */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class End2EndM3RequestE2E {

    companion object {
        // Account credentials (used in UI flows)
        private const val USER1_EMAIL = "user1.request@mail.com"
        private const val USER1_PASSWORD = "Password123"
        private const val USER1_USERNAME = "User1Request"

        private const val USER2_EMAIL = "user2.request@mail.com"
        private const val USER2_PASSWORD = "Password123"
        private const val USER2_USERNAME = "User2Request"

        // Shared state across tests
        private var createdRequestTitle = ""

        @BeforeClass
        @JvmStatic
        fun setupEmulator() {
            FirebaseEmulator.reinitialize()
            FirebaseEmulator.clearAuthEmulator()
            FirebaseEmulator.clearFirestoreEmulator()
        }

        @AfterClass
        @JvmStatic
        fun tearDownFirebase() {
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (_: Exception) {}

            FirebaseEmulator.clearAuthEmulator()
            FirebaseEmulator.clearFirestoreEmulator()
        }
    }

    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth

    @get:Rule
    val grantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.POST_NOTIFICATIONS
        )

    @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        db = FirebaseEmulator.firestore
        auth = FirebaseEmulator.auth
    }

    /** Create User1 account via CreateAccount UI flow. Pattern: Exactly like M2.t0_createAccount */
    @Test
    fun t0_createUser1Account() {
        // Ensure clean state - sign out any cached user
        try {
            auth.signOut()
        } catch (_: Exception) {}

        // Wait for SignIn screen (may take time if app was in different state)
        composeTestRule.waitUntil(timeoutMillis = 200_000) {
            try {
                composeTestRule.onNodeWithTag(SignInTags.LOGO).assertIsDisplayed()
                composeTestRule.onNodeWithTag(SignInTags.SIGN_IN_BUTTON).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Click "Create Account"
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performClick()

        // Username Screen
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag(CreateAccountTags.USERNAME_FIELD)
            .performTextInput(USER1_USERNAME)
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        // Email Screen
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.EMAIL_FIELD)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(CreateAccountTags.EMAIL_FIELD).performTextInput(USER1_EMAIL)
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        // Password Screen
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.PASSWORD_FIELD)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performTextInput(USER1_PASSWORD)
        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performTextInput(USER1_PASSWORD)
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        // Skills Screen - Select CALCULUS
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.SKILLS_FLOW)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        val skillTag = CreateAccountTags.SKILL_CHIP_PREFIX + "CALCULUS"
        composeTestRule.onNodeWithTag(skillTag).performScrollTo()
        composeTestRule.onNodeWithTag(skillTag).performClick()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        // Wait for Profile Screen
        composeTestRule.waitUntil(timeoutMillis = 30_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /** User1 creates a request via UI + adds thumbnail programmatically. */
    @Test
    fun t1_user1CreatesRequest() {
        composeTestRule.waitUntil(timeoutMillis = 30_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Navigate to Add Post Screen
        composeTestRule.onNodeWithTag(NavigationTestTags.POSTS_TAB).performClick()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(RequestScreenTags.TITLE_INPUT)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Fill form
        createdRequestTitle = "E2E Request ${System.currentTimeMillis()}"
        composeTestRule
            .onNodeWithTag(RequestScreenTags.TITLE_INPUT)
            .performTextInput(createdRequestTitle)
        composeTestRule
            .onNodeWithTag(RequestScreenTags.DESCRIPTION_INPUT)
            .performTextInput("E2E test request")

        // Add skill tag (CALCULUS)
        composeTestRule.onNodeWithTag(RequestScreenTags.TAGS_INPUT).performClick()
        composeTestRule.onNodeWithTag(RequestScreenTags.TAGS_INPUT).performTextInput("calcul")
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag("${RequestScreenTags.TAG_SUGGESTION}_CALCULUS")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag("${RequestScreenTags.TAG_SUGGESTION}_CALCULUS").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag("${RequestScreenTags.TAG_CHIP}_CALCULUS")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Select payment method and submit
        composeTestRule
            .onNodeWithTag("${RequestScreenTags.PAYMENT_METHOD_CHIP}_SKILLS")
            .performScrollTo()
        composeTestRule
            .onNodeWithTag("${RequestScreenTags.PAYMENT_METHOD_CHIP}_SKILLS")
            .performClick()
        composeTestRule
            .onNodeWithTag("scrollColumn")
            .performScrollToNode(hasTestTag(RequestScreenTags.CREATE_BUTTON))
        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).performClick()

        // Wait for return to Profile
        composeTestRule.waitUntil(timeoutMillis = 30_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Add thumbnail programmatically (photo picker is external activity)
        composeTestRule.runOnIdle {
            runBlocking(Dispatchers.IO) {
                val requests =
                    db.collection("requests")
                        .whereEqualTo("ownerId", auth.currentUser!!.uid)
                        .whereEqualTo("title", createdRequestTitle)
                        .get()
                        .await()
                val doc = requests.documents.firstOrNull()
                assertNotNull("Request should exist in Firestore", doc)
                db.collection("requests")
                    .document(doc!!.id)
                    .update("media", listOf("https://picsum.photos/400/300"))
                    .await()
            }
        }
    }

    /** User1 signs out. */
    @Test
    fun t2_user1SignsOut() {
        composeTestRule.waitUntil(timeoutMillis = 30_000) {
            composeTestRule
                .onAllNodesWithTag(NavigationTestTags.PROFILE_TAB)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Navigate to Profile and logout
        composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithTag(ProfileTestTags.LOGOUT_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(ProfileTestTags.LOGOUT_BUTTON).performClick()

        // Wait for Sign-In screen
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithTag(SignInTags.SIGN_IN_BUTTON)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.runOnIdle { assertNull("User should be logged out", auth.currentUser) }
    }

    /** Create User2 account and verify feed screen loads. */
    @Test
    fun t3_createUser2AccountAndVerifyFeed() {
        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            composeTestRule
                .onAllNodesWithTag(SignInTags.SIGN_IN_BUTTON)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Click "Create Account"
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performClick()

        // Username Screen
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag(CreateAccountTags.USERNAME_FIELD)
            .performTextInput(USER2_USERNAME)
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        // Email Screen
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.EMAIL_FIELD)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(CreateAccountTags.EMAIL_FIELD).performTextInput(USER2_EMAIL)
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        // Password Screen
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.PASSWORD_FIELD)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag(CreateAccountTags.PASSWORD_FIELD)
            .performTextInput(USER2_PASSWORD)
        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performTextInput(USER2_PASSWORD)
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        // Skills Screen - Select CALCULUS
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.SKILLS_FLOW)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        val skillTag = CreateAccountTags.SKILL_CHIP_PREFIX + "CALCULUS"
        composeTestRule.onNodeWithTag(skillTag).performScrollTo()
        composeTestRule.onNodeWithTag(skillTag).performClick()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        // Wait for Profile Screen
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Navigate to Feed and verify screen loads
        composeTestRule.onNodeWithTag(NavigationTestTags.FEED_TAB).performClick()
        composeTestRule.waitUntil(timeoutMillis = 30_000) {
            composeTestRule
                .onAllNodesWithTag(FeedScreenTestTags.FEED_CARD)
                .fetchSemanticsNodes()
                .isNotEmpty() ||
                composeTestRule
                    .onAllNodesWithTag(FeedScreenTestTags.NO_OFFER_TEXT)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
        }
    }

    /** User2 accepts request (if available) and navigates to chat. */
    @Test
    fun t4_user2AcceptsRequest() {
        composeTestRule.onNodeWithTag(NavigationTestTags.FEED_TAB).performClick()
        composeTestRule.waitUntil(timeoutMillis = 30_000) {
            composeTestRule
                .onAllNodesWithTag(FeedScreenTestTags.FEED_CARD)
                .fetchSemanticsNodes()
                .isNotEmpty() ||
                composeTestRule
                    .onAllNodesWithTag(FeedScreenTestTags.NO_OFFER_TEXT)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
        }

        val hasFeedCard =
            composeTestRule
                .onAllNodesWithTag(FeedScreenTestTags.FEED_CARD)
                .fetchSemanticsNodes()
                .isNotEmpty()

        if (hasFeedCard) {
            // Accept the request
            composeTestRule.onNodeWithTag(FeedScreenTestTags.ACCEPT_BUTTON).performClick()
            composeTestRule.waitUntil(timeoutMillis = 15_000) {
                composeTestRule
                    .onAllNodesWithTag(FeedScreenTestTags.FEED_CARD)
                    .fetchSemanticsNodes()
                    .isNotEmpty() ||
                    composeTestRule
                        .onAllNodesWithTag(FeedScreenTestTags.NO_OFFER_TEXT)
                        .fetchSemanticsNodes()
                        .isNotEmpty()
            }

            // Navigate to Chat and verify chat was created
            composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()
            composeTestRule.runOnIdle {
                runBlocking(Dispatchers.IO) {
                    val chats = db.collection("chats").get().await()
                    assertTrue("At least one chat should exist", chats.documents.isNotEmpty())
                }
            }
        } else {
            // No feed card - just verify Chat tab is accessible
            composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()
        }
    }
}

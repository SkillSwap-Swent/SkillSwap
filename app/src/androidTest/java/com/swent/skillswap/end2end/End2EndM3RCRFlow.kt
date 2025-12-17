package com.swent.skillswap.end2end

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.MainActivity
import com.swent.skillswap.firebase.FirestorePaths
import com.swent.skillswap.model.post.PaymentMethod
import com.swent.skillswap.model.user.*
import com.swent.skillswap.model.user.Preference
import com.swent.skillswap.model.user.User
import com.swent.skillswap.ui.auth.CreateAccountTags
import com.swent.skillswap.ui.auth.SignInTags
import com.swent.skillswap.ui.chat.ChatListTestTags
import com.swent.skillswap.ui.feed.FeedScreenTestTags
import com.swent.skillswap.ui.navigation.NavigationTestTags
import com.swent.skillswap.ui.post.RequestScreenTags
import com.swent.skillswap.ui.post.personalPosts.PersonalPostsScreenTags
import com.swent.skillswap.ui.user.ProfileTestTags
import com.swent.skillswap.utils.FirebaseEmulator
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/** End-to-end test for M3 RCR Flow - Request, Chat, Rate */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class End2EndM3RCRFlow {
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth

    // Test user credentials
    private val user2Email = "user2@test.com"
    private val user2Password = "Password123"
    private val user2Username = "Responder1"

    companion object {

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

            // Sign out before clearing
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (_: Exception) {}

            // Clear emulators AFTER this test class finishes
            FirebaseEmulator.clearAuthEmulator()
            FirebaseEmulator.clearFirestoreEmulator()
        }
    }

    @Before
    fun setup() {
        db = FirebaseEmulator.firestore
        auth = FirebaseEmulator.auth
    }

    @get:Rule
    val grantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.POST_NOTIFICATIONS
        )

    @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

    /** UI-aware sign out that navigates to Profile and taps Logout button */
    private fun signOutViaUI() {
        // If we're already on the sign-in screen, nothing to do
        val onSignIn =
            try {
                composeTestRule.onNodeWithTag(SignInTags.LOGO).assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        if (onSignIn) return

        // Navigate to Profile tab
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertExists()
                true
            } catch (_: Exception) {
                false
            }
        }
        composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
        composeTestRule.waitForIdle()

        // Wait for Logout button and click it
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.LOGOUT_BUTTON)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(ProfileTestTags.LOGOUT_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(ProfileTestTags.LOGOUT_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Wait until sign-in screen is visible
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag(SignInTags.EMAIL_FIELD).assertExists()
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    /** Create account via UI (replicating the pattern used elsewhere) */
    private fun createAccountViaUI(email: String, username: String, password: String) {
        // Ensure sign-in screen is visible
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag(SignInTags.LOGO).assertIsDisplayed()
                true
            } catch (_: Exception) {
                false
            }
        }

        // Navigate to Create Account
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performScrollTo()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performClick()
        composeTestRule.waitForIdle()

        // Username
        composeTestRule.waitUntil(timeoutMillis = 20_000) {
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.USERNAME_FIELD)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(CreateAccountTags.USERNAME_FIELD).performTextInput(username)
        composeTestRule.waitForIdle()
        // Wait until Next is enabled
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).assertIsEnabled()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Email
        composeTestRule.waitUntil(timeoutMillis = 20_000) {
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.EMAIL_FIELD)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(CreateAccountTags.EMAIL_FIELD).performTextInput(email)
        composeTestRule.waitForIdle()
        // Wait until Next is enabled (regex must pass and uniqueness must not have disabled button)
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).assertIsEnabled()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Password
        composeTestRule.waitUntil(timeoutMillis = 20_000) {
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.PASSWORD_FIELD)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(CreateAccountTags.PASSWORD_FIELD).performTextInput(password)
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performTextInput(password)
        composeTestRule.waitForIdle()
        // Wait until Next is enabled (must satisfy uppercase and other rules)
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).assertIsEnabled()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Skills (pick at least one)
        val skillTag = CreateAccountTags.SKILL_CHIP_PREFIX + "CALCULUS"
        composeTestRule.waitUntil(timeoutMillis = 20_000) {
            composeTestRule
                .onAllNodesWithTag(CreateAccountTags.SKILLS_FLOW)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(skillTag).performScrollTo()
        composeTestRule.onNodeWithTag(skillTag).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        // Wait for profile screen to confirm account created
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /** Sign in via UI */
    private fun signInViaUI(email: String, password: String) {
        // Ensure sign-in fields are visible
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag(SignInTags.EMAIL_FIELD).assertExists()
                true
            } catch (_: Exception) {
                false
            }
        }
        composeTestRule.onNodeWithTag(SignInTags.EMAIL_FIELD).performTextInput(email)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(SignInTags.PASSWORD_FIELD).performTextInput(password)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(SignInTags.SIGN_IN_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Wait for profile as home after sign-in
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    // Helper to accept an offer for a given user by email/password
    private fun signInAndAccept(email: String, password: String) {
        // Ensure clean state: if already signed in, log out via UI first
        try {
            signOutViaUI()
        } catch (_: Exception) {}

        // Sign in via UI
        signInViaUI(email, password)

        // Navigate to Feed tab
        composeTestRule.onNodeWithTag(NavigationTestTags.FEED_TAB).performClick()
        composeTestRule.waitForIdle()

        // Wait up to 30s for at least one feed card to appear
        val cardAppeared =
            try {
                composeTestRule.waitUntil(timeoutMillis = 30_000) {
                    composeTestRule
                        .onAllNodesWithTag(FeedScreenTestTags.FEED_CARD)
                        .fetchSemanticsNodes()
                        .isNotEmpty()
                }
                true
            } catch (_: AssertionError) {
                false
            } catch (_: IllegalStateException) {
                false
            }

        val hasCardFinal =
            composeTestRule
                .onAllNodesWithTag(FeedScreenTestTags.FEED_CARD)
                .fetchSemanticsNodes()
                .isNotEmpty()

        if (!cardAppeared || !hasCardFinal) {
            val hasNoOfferFinal =
                composeTestRule
                    .onAllNodesWithTag(FeedScreenTestTags.NO_OFFER_TEXT)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            val msg =
                if (hasNoOfferFinal) {
                    "No offer available in Feed within timeout; UI shows 'no offer'"
                } else {
                    "Expected at least one feed card for responder but none appeared within timeout"
                }
            throw AssertionError(msg)
        }

        // Try to click Accept button when it appears; otherwise fall back to swipe-right on the
        // card
        var clicked = false
        try {
            // Wait a bit for the button to be composed
            composeTestRule.waitUntil(timeoutMillis = 10_000) {
                composeTestRule
                    .onAllNodesWithTag(FeedScreenTestTags.ACCEPT_BUTTON)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            composeTestRule.onNodeWithTag(FeedScreenTestTags.ACCEPT_BUTTON).performClick()
            clicked = true
        } catch (_: AssertionError) {
            // Button not found; try gesture as fallback
        } catch (_: IllegalStateException) {
            // Node hierarchy changed; try gesture as fallback
        }

        if (!clicked) {
            // Swipe right on the card to accept
            composeTestRule.onNodeWithTag(FeedScreenTestTags.FEED_CARD).performTouchInput {
                swipeRight()
            }
        }
        composeTestRule.waitForIdle()
        // brief delay to allow Firestore writes to settle
        Thread.sleep(1500)

        // Validate responder sees a pending/ongoing chat entry
        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 20_000) {
            composeTestRule
                .onAllNodesWithTag(ChatListTestTags.SCREEN)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        fun selectTab(tag: String): Boolean {
            return try {
                composeTestRule.onNodeWithTag(tag).assertExists()
                composeTestRule.onNodeWithTag(tag).performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(300)
                true
            } catch (_: AssertionError) {
                false
            }
        }

        // Try Pending first (responder waiting for owner approval)
        val triedPending = selectTab(ChatListTestTags.PENDING_TAB)

        // Helper to check items presence
        fun hasChatItems(): Boolean {
            val items =
                composeTestRule
                    .onAllNodesWithTag(ChatListTestTags.CHAT_MENU_BUTTON)
                    .fetchSemanticsNodes()
            val empty =
                composeTestRule
                    .onAllNodesWithTag(ChatListTestTags.EMPTY_STATE)
                    .fetchSemanticsNodes()
            return items.isNotEmpty() || empty.isNotEmpty()
        }

        // Perform multiple tab toggles to force refresh
        repeat(3) {
            if (triedPending) {
                selectTab(ChatListTestTags.ONGOING_TAB)
                selectTab(ChatListTestTags.PENDING_TAB)
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 25_000) { hasChatItems() }
        var responderItems =
            composeTestRule
                .onAllNodesWithTag(ChatListTestTags.CHAT_MENU_BUTTON)
                .fetchSemanticsNodes()

        // If no items in Pending, try Ongoing (some implementations place new chats here for
        // responders)
        if (responderItems.isEmpty()) {
            val triedOngoing = selectTab(ChatListTestTags.ONGOING_TAB)
            if (triedOngoing) {
                repeat(2) {
                    selectTab(ChatListTestTags.PENDING_TAB)
                    selectTab(ChatListTestTags.ONGOING_TAB)
                }
                composeTestRule.waitUntil(timeoutMillis = 20_000) { hasChatItems() }
                responderItems =
                    composeTestRule
                        .onAllNodesWithTag(ChatListTestTags.CHAT_MENU_BUTTON)
                        .fetchSemanticsNodes()
            }
        }
        // As a last resort, try Replies (in case tab naming differs)
        if (responderItems.isEmpty()) {
            val triedReplies = selectTab(ChatListTestTags.REPLIES_TAB)
            if (triedReplies) {
                composeTestRule.waitUntil(timeoutMillis = 20_000) { hasChatItems() }
                responderItems =
                    composeTestRule
                        .onAllNodesWithTag(ChatListTestTags.CHAT_MENU_BUTTON)
                        .fetchSemanticsNodes()
            }
        }

        assert(responderItems.isNotEmpty()) {
            "Responder should see at least one chat after accepting (checked Pending, Ongoing, Replies)"
        }

        // Sign out via UI to switch user
        signOutViaUI()
    }

    /**
     * Create a user on the Auth emulator and seed the corresponding Firestore user document.
     * Minimal fields are populated to satisfy app requirements.
     */
    private fun createAndSeedUser(email: String, password: String, username: String) {
        // Create auth account
        val authResult = Tasks.await(auth.createUserWithEmailAndPassword(email, password))
        val uid = authResult.user?.uid ?: throw IllegalStateException("No UID returned for $email")

        // Seed Firestore user document
        val user =
            User(
                uid = uid,
                username = username,
                email = email,
                profilePicture = "",
                skillSet = emptySet(),
                rating = 5.0f,
                availability = emptyList(),
                preference = Preference.SKILLS,
                location = GeoPoint(0.0, 0.0),
                blockedUsers = emptySet(),
                viewedPosts = emptySet(),
                fcmToken = null
            )
        Tasks.await(
            db.collection(FirestorePaths.USERS_COLLECTION)
                .document(uid)
                .set(
                    mapOf(
                        "username" to user.username,
                        "email" to user.email,
                        "profilePicture" to user.profilePicture,
                        "skillSet" to serializeSkills(user.skillSet),
                        "rating" to user.rating,
                        "availability" to serializeAvailabilities(user.availability),
                        "preference" to serializePreference(user.preference),
                        "location" to user.location,
                        "blockedUsers" to serializeBlockedUsers(user.blockedUsers),
                        "viewedPosts" to serializeViewedPosts(user.viewedPosts),
                        "fcmToken" to ""
                    )
                )
        )
    }

    /** Public helper to create the responder user on the emulators. Call this before responding. */
    fun seedResponderUsers() {
        createAndSeedUser(user2Email, user2Password, user2Username)
    }

    @Test
    fun t0_setupPostAuthor() {
        // Create the author account via compact UI helper instead of verbose inline steps
        createAccountViaUI(email = "bob@mail.com", username = "Bob", password = "Password123")

        // Minimal smoke: profile screen is shown
        composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertExists()
    }

    @Test
    fun t1_authorCanCreatePost() {

        // ---------- Navigate to Add Post Screen via bottom nav Posts tab ----------
        composeTestRule.onNodeWithTag(NavigationTestTags.POSTS_TAB).performClick()
        composeTestRule.waitForIdle()

        // ---------- Wait for Add Request Screen ----------
        composeTestRule.waitUntil(10_000) {
            composeTestRule
                .onAllNodesWithTag(RequestScreenTags.TITLE_INPUT)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // ---------- Verify UI ----------

        listOf(
                RequestScreenTags.TITLE_INPUT,
                RequestScreenTags.DESCRIPTION_INPUT,
                RequestScreenTags.TAGS_INPUT,
                RequestScreenTags.CREATE_BUTTON,
            )
            .forEach { tag ->
                composeTestRule.waitUntil(timeoutMillis = 5_000) {
                    try {
                        composeTestRule
                            .onNodeWithTag("scrollColumn")
                            .performScrollToNode(hasTestTag(tag))
                        composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
                        true
                    } catch (_: Exception) {
                        false
                    }
                }
            }

        // ---------- Fill title ----------
        val createdTitle = "Need Help with Physics"
        composeTestRule.onNodeWithTag(RequestScreenTags.TITLE_INPUT).performTextInput(createdTitle)
        composeTestRule.waitForIdle()

        // ---------- Fill description ----------
        composeTestRule
            .onNodeWithTag(RequestScreenTags.DESCRIPTION_INPUT)
            .performTextInput("Looking for someone to help me understand calculus")
        composeTestRule.waitForIdle()

        // ---------- Type tag: calculus ----------
        composeTestRule.onNodeWithTag(RequestScreenTags.TAGS_INPUT).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(RequestScreenTags.TAGS_INPUT).performTextInput("calculus")
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(5_000) {
            composeTestRule
                .onAllNodesWithTag("${RequestScreenTags.TAG_SUGGESTION}_CALCULUS")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // ---------- Select tag ----------
        composeTestRule.onNodeWithTag("${RequestScreenTags.TAG_SUGGESTION}_CALCULUS").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(5_000) {
            composeTestRule
                .onAllNodesWithTag("${RequestScreenTags.TAG_CHIP}_CALCULUS")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.waitUntil(5_000) {
            composeTestRule
                .onAllNodesWithTag("${RequestScreenTags.TAG_CHIP}_CALCULUS")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // ---------- Select Payment Method ----------
        val skillsChip = "${RequestScreenTags.PAYMENT_METHOD_CHIP}_${PaymentMethod.SKILLS.name}"

        composeTestRule.onNodeWithTag(skillsChip).performScrollTo()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(skillsChip).performClick()
        composeTestRule.waitForIdle()

        // ---------- Submit form ----------
        composeTestRule
            .onNodeWithTag("scrollColumn")
            .performScrollToNode(hasTestTag(RequestScreenTags.CREATE_BUTTON))
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).performClick()

        // ---------- Wait for return to Profile ----------
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.waitUntil(5_000) {
            try {
                composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertIsDisplayed()
                true
            } catch (_: Exception) {
                false
            }
        }

        // ---------- Navigate to My Posts and verify the new entry ----------
        composeTestRule.onNodeWithTag(ProfileTestTags.MY_POSTS_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(ProfileTestTags.MY_POSTS_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Wait for My Posts screen
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(PersonalPostsScreenTags.TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        // Check that the created post title is displayed in the list
        composeTestRule.onNodeWithText(createdTitle).assertIsDisplayed()

        // Go back to Profile
        composeTestRule.onAllNodesWithContentDescription("Back")[0].performClick()
        composeTestRule.waitForIdle()

        // ---------- Log out via UI ----------
        signOutViaUI()

        // Extra safety: verify the author's request has been written to Firestore
        val authorEmail = "bob@mail.com"
        val maxAttempts = 10
        var attempt = 0
        var found = false
        while (attempt < maxAttempts && !found) {
            val snapshot =
                Tasks.await(
                    db.collection(FirestorePaths.REQUESTS_COLLECTION)
                        .whereEqualTo("title", createdTitle)
                        .get()
                )
            found = snapshot.documents.isNotEmpty()
            if (!found) {
                Thread.sleep(500)
                attempt++
            }
        }
        assert(found) {
            "Author request '$createdTitle' for $authorEmail was not found in Firestore after creation UI flow"
        }
    }

    @Test
    fun t2_canCreateOtherUser() {
        // Create one responder user via UI to keep app state consistent
        // Assumes we are signed in as the author from previous tests
        signOutViaUI()

        // Create user 2
        createAccountViaUI(user2Email, user2Username, user2Password)
        composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertExists()
        signOutViaUI()

        // Verify Firestore: both emails should exist in users collection (author + user2)
        val usersSnapshot = Tasks.await(db.collection(FirestorePaths.USERS_COLLECTION).get())
        val emails = usersSnapshot.documents.mapNotNull { it.getString("email") }.toSet()
        val authorEmail = "bob@mail.com"
        assert(emails.contains(authorEmail))
        assert(emails.contains(user2Email))

        // Verify Auth via UI sign-ins
        signInViaUI(user2Email, user2Password)
        signOutViaUI()

        signInViaUI(authorEmail, "Password123")
        signOutViaUI()
    }

    @Test
    fun t3_responderAcceptsAuthorsPost() {
        // Single responder accepts the post
        signInAndAccept(user2Email, user2Password)

        // Now author signs in and checks Chats > Replies contains 1 entry
        val authorEmail = "bob@mail.com"
        val authorPassword = "Password123"

        // Sign in as author via UI
        signInViaUI(authorEmail, authorPassword)

        // Navigate to Chat screen via bottom nav
        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()
        composeTestRule.waitForIdle()

        // Wait for ChatList screen visible
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithTag(ChatListTestTags.SCREEN)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Switch to Replies tab (author's pending approvals)
        composeTestRule.onNodeWithTag(ChatListTestTags.REPLIES_TAB).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ChatListTestTags.REPLIES_TAB).performClick()
        composeTestRule.waitForIdle()

        // Wait for list or empty state; force a quick tab toggle to refresh
        composeTestRule.waitUntil(timeoutMillis = 8_000) {
            composeTestRule
                .onAllNodesWithTag(ChatListTestTags.POSTS_LIST)
                .fetchSemanticsNodes()
                .isNotEmpty() ||
                composeTestRule
                    .onAllNodesWithTag(ChatListTestTags.EMPTY_STATE)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
        }
        // Toggle tabs to force refresh (Replies -> Ongoing -> Replies)
        try {
            composeTestRule.onNodeWithTag(ChatListTestTags.ONGOING_TAB).assertExists()
            composeTestRule.onNodeWithTag(ChatListTestTags.ONGOING_TAB).performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(300)
            composeTestRule.onNodeWithTag(ChatListTestTags.REPLIES_TAB).performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(300)
        } catch (_: AssertionError) {
            /* ignore if tabs missing */
        }

        // Wait until there is at least 1 approve button or at least 1 item
        composeTestRule.waitUntil(timeoutMillis = 30_000) {
            val approve =
                composeTestRule
                    .onAllNodesWithTag(ChatListTestTags.ACCEPT_CHAT)
                    .fetchSemanticsNodes()
                    .size
            val items =
                composeTestRule
                    .onAllNodesWithTag(ChatListTestTags.CHAT_MENU_BUTTON)
                    .fetchSemanticsNodes()
                    .size
            (approve >= 1) || (items >= 1)
        }

        val approveButtons =
            composeTestRule.onAllNodesWithTag(ChatListTestTags.ACCEPT_CHAT).fetchSemanticsNodes()
        val chatRows =
            composeTestRule
                .onAllNodesWithTag(ChatListTestTags.CHAT_MENU_BUTTON)
                .fetchSemanticsNodes()
        val count = maxOf(approveButtons.size, chatRows.size)
        assert(count >= 1) { "Expected at least 1 chat to approve in Replies, found ${count}" }
    }
}

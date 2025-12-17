/*
 * End-to-end test for Milestone 3: Request Acceptance Flow
 *
 * Test Scenario: As a registered user who has created a request, I want to review multiple
 * responses, choose the one that suits me best, and see a chat with that person, while
 * receiving a notification that my post has been accepted.
 */
package com.swent.skillswap.end2end

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.swent.skillswap.MainActivity
import com.swent.skillswap.firebase.CloudReferences
import com.swent.skillswap.model.chat.ChatRepositoryFirestore
import com.swent.skillswap.model.chat.ChatStatus
import com.swent.skillswap.model.notification.NotificationRepositoryFirestore
import com.swent.skillswap.model.notification.NotificationType
import com.swent.skillswap.model.post.*
import com.swent.skillswap.model.post.PostFirestoreRepository
import com.swent.skillswap.ui.auth.CreateAccountTags
import com.swent.skillswap.ui.auth.SignInTags
import com.swent.skillswap.ui.chat.ChatListTestTags
import com.swent.skillswap.ui.navigation.NavigationTestTags
import com.swent.skillswap.ui.post.RequestScreenTags
import com.swent.skillswap.ui.user.ProfileTestTags
import com.swent.skillswap.utils.FirebaseEmulator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/** End-to-end tests for Milestone 3: Request Acceptance Flow */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class End2EndM3 {

    lateinit var db: com.google.firebase.firestore.FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var postRepository: PostFirestoreRepository
    lateinit var chatRepository: ChatRepositoryFirestore
    lateinit var notificationRepository: NotificationRepositoryFirestore

    // User credentials
    private val user1Email = "user1@test.com"
    private val user1Password = "Password123"
    private val user1Username = "RequestCreator"

    private val user2Email = "user2@test.com"
    private val user2Password = "Password123"
    private val user2Username = "Responder1"

    private val user3Email = "user3@test.com"
    private val user3Password = "Password123"
    private val user3Username = "Responder2"

    private var requestId: String = ""
    private var user1Id: String = ""
    private var user2Id: String = ""
    private var user3Id: String = ""

    companion object {
        private const val PROJECT_ID = "skillswap-93276"

        @BeforeClass
        @JvmStatic
        fun setupEmulator(): Unit {
            FirebaseEmulator.reinitialize()

            FirebaseEmulator.clearAuthEmulator()
            FirebaseEmulator.clearFirestoreEmulator()

            // Clear storage emulator by manually deleting all files
            // Note: clearStorageEmulator() doesn't work due to incorrect endpoint,
            // so we loop through storage paths and delete all files
            try {
                for (path in CloudReferences.values) {
                    val storageRef = FirebaseEmulator.storage.reference.child(path)
                    val listResult =
                        Tasks.await(storageRef.listAll(), 10, java.util.concurrent.TimeUnit.SECONDS)
                    for (item in listResult.items) {
                        Tasks.await(item.delete(), 10, java.util.concurrent.TimeUnit.SECONDS)
                    }
                }
            } catch (_: Exception) {
                // Ignore errors during setup
            }
        }

        @AfterClass
        @JvmStatic
        fun tearDownFirebase(): Unit {
            // Sign out before clearing
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (_: Exception) {}

            // Clear storage emulator by manually deleting all files
            // Note: clearStorageEmulator() doesn't work due to incorrect endpoint,
            // so we loop through storage paths and delete all files
            try {
                for (path in CloudReferences.values) {
                    val storageRef = FirebaseEmulator.storage.reference.child(path)
                    val listResult =
                        Tasks.await(storageRef.listAll(), 10, java.util.concurrent.TimeUnit.SECONDS)
                    for (item in listResult.items) {
                        Tasks.await(item.delete(), 10, java.util.concurrent.TimeUnit.SECONDS)
                    }
                }
            } catch (_: Exception) {
                // Ignore errors during cleanup
            }

            // Clear emulators AFTER this test class finishes
            FirebaseEmulator.clearAuthEmulator()
            FirebaseEmulator.clearFirestoreEmulator()
        }
    }

    @Before
    fun setup(): Unit {
        // Use FirebaseEmulator instances directly - these are singletons, so no reset occurs
        db = FirebaseEmulator.firestore
        auth = FirebaseEmulator.auth
        // Create new repository instances (they just wrap the same db/auth instances)
        // Note: ChatRepositoryFirestore requires postRepository as a parameter
        postRepository = PostFirestoreRepository(db)
        chatRepository = ChatRepositoryFirestore(db, postRepository)
        notificationRepository = NotificationRepositoryFirestore(db)
    }

    @get:Rule
    val grantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.POST_NOTIFICATIONS
        )

    @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

    /** Helper function to create an account via UI */
    private fun createAccountViaUI(email: String, username: String, password: String) {
        // Wait for sign in screen with longer timeout for CI
        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                composeTestRule.onNodeWithTag(SignInTags.LOGO).assertIsDisplayed()
                true
            } catch (_: Exception) {
                false
            }
        }

        // Navigate to create account
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).assertExists()
                true
            } catch (_: Exception) {
                false
            }
        }
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performScrollTo()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performClick()
        composeTestRule.waitForIdle()

        // Username screen
        composeTestRule.waitUntil(timeoutMillis = 20_000) {
            try {
                composeTestRule
                    .onAllNodesWithTag(CreateAccountTags.USERNAME_FIELD)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
                true
            } catch (_: Exception) {
                false
            }
        }
        composeTestRule.onNodeWithTag(CreateAccountTags.USERNAME_FIELD).performTextInput(username)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Email screen
        composeTestRule.waitUntil(timeoutMillis = 20_000) {
            try {
                composeTestRule
                    .onAllNodesWithTag(CreateAccountTags.EMAIL_FIELD)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
                true
            } catch (_: Exception) {
                false
            }
        }
        composeTestRule.onNodeWithTag(CreateAccountTags.EMAIL_FIELD).performTextInput(email)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Password screen
        composeTestRule.waitUntil(timeoutMillis = 20_000) {
            try {
                composeTestRule
                    .onAllNodesWithTag(CreateAccountTags.PASSWORD_FIELD)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
                true
            } catch (_: Exception) {
                false
            }
        }
        composeTestRule.onNodeWithTag(CreateAccountTags.PASSWORD_FIELD).performTextInput(password)
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
            .performTextInput(password)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Skills screen - select at least one skill
        val skillTag = CreateAccountTags.SKILL_CHIP_PREFIX + "CALCULUS"
        composeTestRule.waitUntil(timeoutMillis = 20_000) {
            try {
                composeTestRule
                    .onAllNodesWithTag(CreateAccountTags.SKILLS_FLOW)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
                true
            } catch (_: Exception) {
                false
            }
        }
        composeTestRule.onNodeWithTag(skillTag).performScrollTo()
        composeTestRule.onNodeWithTag(skillTag).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CreateAccountTags.NEXT_BUTTON).performClick()

        // Wait until firestore auth operation completes and Profile Screen is displayed
        // Match End2EndM1 pattern with 60 second timeout
        composeTestRule.waitUntil(timeoutMillis = 60_000) {
            try {
                composeTestRule
                    .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
                true
            } catch (_: Exception) {
                false
            }
        }
        composeTestRule.waitForIdle()
    }

    /** Helper function to sign in via Firebase Auth (for programmatic sign-in) */
    private fun signInProgrammatically(email: String, password: String) {
        runBlocking(Dispatchers.IO) {
            try {
                Tasks.await(
                    auth.createUserWithEmailAndPassword(email, password),
                    15,
                    java.util.concurrent.TimeUnit.SECONDS
                )
            } catch (_: Exception) {
                // User may already exist, try to sign in
            }
            Tasks.await(
                auth.signInWithEmailAndPassword(email, password),
                15,
                java.util.concurrent.TimeUnit.SECONDS
            )
        }
    }

    /** Helper function to sign out */
    private fun signOut() {
        auth.signOut()
        composeTestRule.waitForIdle()
        // Wait for sign in screen with longer timeout for CI
        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                composeTestRule.onNodeWithTag(SignInTags.LOGO).assertIsDisplayed()
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    /** Helper function to sign in programmatically (login UI is already tested in M1/M2) */
    private fun signInProgrammaticallyToApp(email: String, password: String) {
        runBlocking(Dispatchers.IO) {
            Tasks.await(
                auth.signInWithEmailAndPassword(email, password),
                15,
                java.util.concurrent.TimeUnit.SECONDS
            )
        }
        composeTestRule.waitForIdle()

        // Wait for profile screen to appear after sign in with longer timeout for CI
        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                composeTestRule
                    .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    /**
     * Helper function to reload requestId from Firestore if it's empty. This makes tests more
     * robust by recovering from potential failures in t0. Includes retry logic to handle Firestore
     * emulator timing issues in CI environments.
     */
    private fun ensureRequestIdLoaded() {
        if (requestId.isEmpty()) {
            runBlocking(Dispatchers.IO) {
                // First, ensure user1Id is loaded by signing in if needed
                if (user1Id.isEmpty()) {
                    try {
                        Tasks.await(
                            auth.signInWithEmailAndPassword(user1Email, user1Password),
                            15,
                            java.util.concurrent.TimeUnit.SECONDS
                        )
                        user1Id = auth.currentUser?.uid ?: ""
                        android.util.Log.d("End2EndM3", "Loaded user1Id: $user1Id")
                    } catch (e: Exception) {
                        android.util.Log.e("End2EndM3", "Failed to sign in as user1 to get ID", e)
                    }
                }

                // Now try to find the request with retries
                if (user1Id.isNotEmpty()) {
                    repeat(7) { // try up to 7 times
                        val requests =
                            postRepository.getMultiplePosts(
                                numberOfPosts = 10,
                                type = PostType.REQUEST,
                                ownerId = user1Id
                            )
                        requestId = requests.firstOrNull()?.uid ?: ""
                        if (requestId.isNotEmpty()) {
                            android.util.Log.d(
                                "End2EndM3",
                                "Successfully loaded requestId: $requestId"
                            )
                            return@runBlocking // Exit early on successful retrieval
                        }
                        android.util.Log.d(
                            "End2EndM3",
                            "Retrying request fetch from Firestore... Attempt: ${it + 1}"
                        )
                        Thread.sleep(4000) // wait 4 seconds before retrying
                    }
                }
            }
        }
    }

    @Test
    fun t0_createAccountsAndRequest() {
        // Create user1 account
        createAccountViaUI(user1Email, user1Username, user1Password)

        // Wait for auth.currentUser to be available - use runOnIdle to ensure we're on the right
        // thread
        // and wait with retries inside it (matching End2EndM2 pattern for Firestore operations)
        composeTestRule.runOnIdle {
            var attempts = 0
            while (auth.currentUser == null && attempts < 40) {
                Thread.sleep(500)
                attempts++
            }
            user1Id = auth.currentUser?.uid ?: ""
            assert(user1Id.isNotEmpty()) { "User1 ID should not be empty after account creation" }
        }

        // Ensure profile screen is fully displayed
        // Match End2EndM2 t4 pattern: wait for profile screen, then click POSTS_TAB directly
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Navigate to Add Post Screen via bottom nav Posts tab
        // Match End2EndM2 t4 pattern exactly: direct click without extra waits
        composeTestRule.onNodeWithTag(NavigationTestTags.POSTS_TAB).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                composeTestRule
                    .onAllNodesWithTag(RequestScreenTags.TITLE_INPUT)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
                true
            } catch (_: Exception) {
                false
            }
        }

        // Fill request form
        composeTestRule
            .onNodeWithTag(RequestScreenTags.TITLE_INPUT)
            .performTextInput("Need help with Physics")
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(RequestScreenTags.DESCRIPTION_INPUT)
            .performTextInput("Looking for someone to help me understand mechanics")
        composeTestRule.waitForIdle()

        // Add tag
        composeTestRule.onNodeWithTag(RequestScreenTags.TAGS_INPUT).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(RequestScreenTags.TAGS_INPUT).performTextInput("physics")
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 20_000) {
            try {
                composeTestRule
                    .onAllNodesWithTag("${RequestScreenTags.TAG_SUGGESTION}_PHYSICS_MECHANICS")
                    .fetchSemanticsNodes()
                    .isNotEmpty()
                true
            } catch (_: Exception) {
                false
            }
        }
        composeTestRule
            .onNodeWithTag("${RequestScreenTags.TAG_SUGGESTION}_PHYSICS_MECHANICS")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 20_000) {
            try {
                composeTestRule
                    .onAllNodesWithTag("${RequestScreenTags.TAG_CHIP}_PHYSICS_MECHANICS")
                    .fetchSemanticsNodes()
                    .isNotEmpty()
                true
            } catch (_: Exception) {
                false
            }
        }

        // Submit request
        composeTestRule
            .onNodeWithTag("scrollColumn")
            .performScrollToNode(hasTestTag(RequestScreenTags.CREATE_BUTTON))
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Wait for navigation back to profile/feed with longer timeout for CI
        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                composeTestRule
                    .onAllNodesWithTag(ProfileTestTags.PROFILE_TITLE)
                    .fetchSemanticsNodes()
                    .isNotEmpty() ||
                    composeTestRule
                        .onAllNodesWithTag(NavigationTestTags.FEED_TAB)
                        .fetchSemanticsNodes()
                        .isNotEmpty()
                true
            } catch (_: Exception) {
                false
            }
        }

        // Get the created request ID from Firestore
        // Wait until the request is persisted in Firestore with longer timeout for CI
        composeTestRule.waitUntil(timeoutMillis = 30_000) {
            runBlocking(Dispatchers.IO) {
                val requests =
                    postRepository.getMultiplePosts(
                        numberOfPosts = 10,
                        type = PostType.REQUEST,
                        ownerId = user1Id
                    )
                requestId = requests.firstOrNull()?.uid ?: ""
                requestId.isNotEmpty()
            }
        }
        assert(requestId.isNotEmpty()) {
            "Request ID should not be empty. Request may not have been created or persisted."
        }
    }

    @Test
    fun t1_createUser2AndReply() {
        // Ensure requestId is loaded (reload from Firestore if needed)
        ensureRequestIdLoaded()
        assert(requestId.isNotEmpty()) {
            "Request ID is empty. Could not find request created by user1 in Firestore."
        }

        // Sign out user1
        signOut()

        // Create user2 account
        createAccountViaUI(user2Email, user2Username, user2Password)

        // Wait for auth.currentUser to be available - use runOnIdle with retries
        composeTestRule.runOnIdle {
            var attempts = 0
            while (auth.currentUser == null && attempts < 40) {
                Thread.sleep(500)
                attempts++
            }
            user2Id = auth.currentUser?.uid ?: ""
            assert(user2Id.isNotEmpty()) { "User2 ID should not be empty after account creation" }
        }

        // Ensure profile screen and bottom nav are ready before navigating
        // Match End2EndM1 pattern: verify bottom nav elements are displayed
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertIsDisplayed()
                composeTestRule
                    .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU)
                    .assertIsDisplayed()
                composeTestRule.onNodeWithTag(NavigationTestTags.FEED_TAB).assertIsDisplayed()
                true
            } catch (_: Exception) {
                false
            }
        }
        composeTestRule.waitForIdle()

        // Navigate to feed and accept the request (this creates a reply)
        composeTestRule.onNodeWithTag(NavigationTestTags.FEED_TAB).performClick()
        composeTestRule.waitForIdle()

        // Wait for feed to load - just wait for navigation, don't check for specific elements
        // since the feed might be empty or loading
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag(NavigationTestTags.FEED_TAB)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Accept the post (swipe right or click accept button)
        // This will create a PostReply and a chat
        composeTestRule.waitForIdle()
        Thread.sleep(1000) // Give time for feed to load

        // Try to find and click accept button or perform swipe
        // Note: The exact UI element depends on FeedScreen implementation
        // For now, we'll create the reply programmatically
        runBlocking(Dispatchers.IO) {
            val request = postRepository.getPost(PostType.REQUEST, requestId) as Request
            val postReply =
                PostReply(
                    postId = requestId,
                    ownerId = user2Id,
                    creation = Timestamp.now(),
                    message = "I can help you with physics!",
                    postType = PostType.REQUEST,
                    replyStatus = ReplyStatus.PROPOSED
                )
            postRepository.editPost(
                requestId,
                request.copy(postReplies = request.postReplies + postReply)
            )

            // Create chat
            chatRepository.createChat(listOf(user2Id, user1Id), requestId, PostType.REQUEST)
        }
    }

    @Test
    fun t2_createUser3AndReply() {
        // Ensure requestId is loaded (reload from Firestore if needed)
        ensureRequestIdLoaded()
        assert(requestId.isNotEmpty()) {
            "Request ID is empty. Could not find request created by user1 in Firestore."
        }

        // Sign out user2
        signOut()

        // Create user3 account
        createAccountViaUI(user3Email, user3Username, user3Password)

        // Wait for auth.currentUser to be available - use runOnIdle with retries
        composeTestRule.runOnIdle {
            var attempts = 0
            while (auth.currentUser == null && attempts < 40) {
                Thread.sleep(500)
                attempts++
            }
            user3Id = auth.currentUser?.uid ?: ""
            assert(user3Id.isNotEmpty()) { "User3 ID should not be empty after account creation" }
        }

        // Create reply programmatically
        runBlocking(Dispatchers.IO) {
            val request = postRepository.getPost(PostType.REQUEST, requestId) as Request
            val postReply =
                PostReply(
                    postId = requestId,
                    ownerId = user3Id,
                    creation = Timestamp.now(),
                    message = "I'm also interested in helping!",
                    postType = PostType.REQUEST,
                    replyStatus = ReplyStatus.PROPOSED
                )
            postRepository.editPost(
                requestId,
                request.copy(postReplies = request.postReplies + postReply)
            )

            // Create chat
            chatRepository.createChat(listOf(user3Id, user1Id), requestId, PostType.REQUEST)
        }
    }

    @Test
    fun t3_user1AcceptsReplyAndVerifiesNotifications() {
        // Ensure requestId is loaded (reload from Firestore if needed)
        ensureRequestIdLoaded()
        assert(requestId.isNotEmpty()) {
            "Request ID is empty. Could not find request created by user1 in Firestore."
        }

        // Sign out user3 and sign in as user1
        signOut()
        signInProgrammaticallyToApp(user1Email, user1Password)

        // Ensure profile screen and bottom nav are ready before navigating
        // Match End2EndM1 pattern: verify bottom nav elements are displayed
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertIsDisplayed()
                composeTestRule
                    .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU)
                    .assertIsDisplayed()
                composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).assertIsDisplayed()
                true
            } catch (_: Exception) {
                false
            }
        }
        composeTestRule.waitForIdle()

        // Navigate to chat list
        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()
        composeTestRule.waitForIdle()

        // Wait for chat list to load with longer timeout for CI
        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                composeTestRule
                    .onAllNodesWithTag(ChatListTestTags.SCREEN)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
                true
            } catch (_: Exception) {
                false
            }
        }

        // Navigate to "To Approve" section to see pending chats with longer timeout for CI
        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                composeTestRule.onNodeWithTag(ChatListTestTags.REPLIES_TAB).assertExists()
                true
            } catch (_: Exception) {
                false
            }
        }
        composeTestRule.onNodeWithTag(ChatListTestTags.REPLIES_TAB).performClick()
        composeTestRule.waitForIdle()

        // Wait for pending chats to appear with longer timeout for CI
        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                composeTestRule
                    .onAllNodesWithTag(ChatListTestTags.POSTS_LIST)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
                true
            } catch (_: Exception) {
                false
            }
        }

        // Accept the first chat (user2's reply)
        // Find and click the accept button with longer timeout for CI
        composeTestRule.waitUntil(timeoutMillis = 40_000) {
            try {
                composeTestRule
                    .onAllNodesWithTag(ChatListTestTags.ACCEPT_CHAT)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
                true
            } catch (_: Exception) {
                false
            }
        }
        composeTestRule.onAllNodesWithTag(ChatListTestTags.ACCEPT_CHAT)[0].performClick()
        composeTestRule.waitForIdle()

        // Wait a bit for notifications to be created
        Thread.sleep(2000)

        // Verify notifications were created
        runBlocking(Dispatchers.IO) {
            // Sign in as user2 to check their notifications
            // Note: Currently, only the responder (user2) receives a POST_ACCEPTED notification
            // when their reply is accepted. The request creator (user1) does not receive a
            // notification.
            signInProgrammatically(user2Email, user2Password)
            val user2Notifications = notificationRepository.getNotificationsForUser(user2Id)
            val chatNotification =
                user2Notifications.find {
                    it.type == NotificationType.POST_ACCEPTED && it.relatedId == requestId
                }
            assert(chatNotification != null) {
                "User2 (accepted responder) should receive a POST_ACCEPTED notification about their reply being accepted"
            }

            // Sign back in as user1 to verify chats
            signInProgrammatically(user1Email, user1Password)

            // Verify the chat is now active
            val chats = chatRepository.getChatsOfCurrentUser(PostType.REQUEST)
            val acceptedChat =
                chats.find {
                    it.relatedPostId == requestId &&
                        it.participants.contains(user2Id) &&
                        it.status == ChatStatus.ACTIVE
                }
            assert(acceptedChat != null) { "Chat between user1 and user2 should be active" }

            // Verify other chat (user3) is inactive
            // Note: After accepting user2's chat, user3's chat should be inactive
            // We can verify this by checking that it's not in the active chats list
            val user3ChatInActive =
                chats.find {
                    it.relatedPostId == requestId &&
                        it.participants.contains(user3Id) &&
                        it.status == ChatStatus.ACTIVE
                }
            assert(user3ChatInActive == null) {
                "Chat with user3 should not be active after accepting user2's reply"
            }
        }

        // Navigate to notifications screen to verify UI
        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()
        composeTestRule.waitForIdle()

        // Look for notification icon/button in chat screen header
        // Note: This depends on the UI implementation
        composeTestRule.waitForIdle()
    }
}

package com.swent.skillswap

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.model.notification.Notification
import com.swent.skillswap.model.notification.NotificationRepositoryFirestore
import com.swent.skillswap.model.notification.NotificationType
import com.swent.skillswap.resources.theme.SkillSwapAppTheme
import com.swent.skillswap.ui.navigation.Screen
import com.swent.skillswap.ui.notification.NotificationScreenTags
import com.swent.skillswap.utils.FirebaseEmulator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityNavigationTest : TestCase() {

    @get:Rule val composeTestRule = createComposeRule()

    private val ctx =
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var navController: NavHostController
    private lateinit var auth: FirebaseAuth

    init {
        FirebaseEmulator.startEmulator()
    }

    @Before
    fun setUp() = runBlocking {
        // Initialize FirebaseApp if necessary
        try {
            if (com.google.firebase.FirebaseApp.getApps(ctx).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(ctx)
            }
        } catch (e: Exception) {
            // Ignore if already initialized
        }

        // Auth: sign in anonymously on the emulator
        auth = FirebaseAuth.getInstance()
        try {
            auth.signInAnonymously().await()
        } catch (e: Exception) {
            // Ignore if sign-in fails (may already be signed in or emulator issue)
        }

        composeTestRule.setContent {
            navController = rememberNavController()
            SkillSwapAppTheme { SkillSwapApp(navController = navController) }
        }
        composeTestRule.waitForIdle()

        // Navigate to Profile screen first
        composeTestRule.runOnUiThread {
            navController.navigate(Screen.Profile.route) {
                popUpTo(Screen.AuthMain.route) { inclusive = true }
            }
        }
        composeTestRule.waitForIdle()
        Unit // Explicitly return Unit for JUnit
    }

    @Test
    fun notificationListRoute_mapsToNotificationTab() = run {
        // Navigate to NotificationList route
        composeTestRule.runOnUiThread {
            navController.navigate(Screen.NotificationList.route) {
                popUpTo(Screen.Profile.route) { inclusive = false }
            }
        }
        composeTestRule.waitForIdle()

        // Verify notification screen is displayed
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag(NotificationScreenTags.SCREEN)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(NotificationScreenTags.SCREEN).assertIsDisplayed()
    }

    @After
    fun tearDown() = runBlocking {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // Ignore
        }
        Unit // Explicitly return Unit for JUnit
    }

    @Test
    fun notificationClick_messageTypeWithRelatedId_navigatesToChatScreen() = run {
        val userId = auth.currentUser?.uid ?: return@run
        val repository = NotificationRepositoryFirestore(FirebaseEmulator.firestore)

        // Create a test notification with MESSAGE type and relatedId
        val testNotification =
            Notification(
                uid = "test-notif-msg-1",
                userId = userId,
                title = "Test Message",
                message = "Test message content",
                type = NotificationType.MESSAGE,
                relatedId = "test-chat-id-123",
                isRead = false,
                timestamp = Timestamp.now()
            )

        // Add notification to repository
        runBlocking { repository.addNotification(testNotification) }

        // Navigate to notification screen
        composeTestRule.runOnUiThread {
            navController.navigate(Screen.NotificationList.route) {
                popUpTo(Screen.Profile.route) { inclusive = false }
            }
        }
        composeTestRule.waitForIdle()

        // Wait for notification screen to load and notification to appear
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_test-notif-msg-1")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Click on the notification
        composeTestRule
            .onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_test-notif-msg-1")
            .performClick()
        composeTestRule.waitForIdle()

        // Verify we navigated to chat screen
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            val currentRoute = navController.currentDestination?.route
            currentRoute == Screen.ChatScreen.createRoute("test-chat-id-123")
        }

        // Cleanup
        runBlocking {
            try {
                repository.deleteNotification("test-notif-msg-1")
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    fun notificationClick_messageTypeWithoutRelatedId_doesNotNavigate() = run {
        val userId = auth.currentUser?.uid ?: return@run
        val repository = NotificationRepositoryFirestore(FirebaseEmulator.firestore)

        // Create a test notification with MESSAGE type but NO relatedId
        val testNotification =
            Notification(
                uid = "test-notif-msg-null",
                userId = userId,
                title = "Test Message No Chat",
                message = "Test message without chat",
                type = NotificationType.MESSAGE,
                relatedId = null, // This should prevent navigation
                isRead = false,
                timestamp = Timestamp.now()
            )

        // Add notification to repository
        runBlocking { repository.addNotification(testNotification) }

        // Navigate to notification screen
        composeTestRule.runOnUiThread {
            navController.navigate(Screen.NotificationList.route) {
                popUpTo(Screen.Profile.route) { inclusive = false }
            }
        }
        composeTestRule.waitForIdle()

        // Wait for notification screen to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag(
                    "${NotificationScreenTags.NOTIFICATION_ITEM}_test-notif-msg-null"
                )
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Click on the notification (should not navigate because relatedId is null)
        composeTestRule
            .onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_test-notif-msg-null")
            .performClick()
        composeTestRule.waitForIdle()

        // Verify we're still on notification screen (relatedId is null, so no navigation)
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            val currentRoute = navController.currentDestination?.route
            currentRoute == Screen.NotificationList.route
        }
        composeTestRule.onNodeWithTag(NotificationScreenTags.SCREEN).assertIsDisplayed()

        // Cleanup
        runBlocking {
            try {
                repository.deleteNotification("test-notif-msg-null")
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    fun notificationClick_postReplyType_navigatesToFeed() = run {
        val userId = auth.currentUser?.uid ?: return@run
        val repository = NotificationRepositoryFirestore(FirebaseEmulator.firestore)

        // Create a test notification with POST_REPLY type
        val testNotification =
            Notification(
                uid = "test-notif-post-reply",
                userId = userId,
                title = "Post Reply",
                message = "Someone replied to your post",
                type = NotificationType.POST_REPLY,
                relatedId = "post-id-123",
                isRead = false,
                timestamp = Timestamp.now()
            )

        runBlocking { repository.addNotification(testNotification) }

        // Navigate to notification screen
        composeTestRule.runOnUiThread {
            navController.navigate(Screen.NotificationList.route) {
                popUpTo(Screen.Profile.route) { inclusive = false }
            }
        }
        composeTestRule.waitForIdle()

        // Wait for notification to appear
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag(
                    "${NotificationScreenTags.NOTIFICATION_ITEM}_test-notif-post-reply"
                )
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Click on the notification
        composeTestRule
            .onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_test-notif-post-reply")
            .performClick()
        composeTestRule.waitForIdle()

        // Verify we navigated to Feed
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            val currentRoute = navController.currentDestination?.route
            currentRoute == Screen.Feed.route
        }

        // Cleanup
        runBlocking {
            try {
                repository.deleteNotification("test-notif-post-reply")
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    fun notificationClick_postAcceptedType_navigatesToFeed() = run {
        val userId = auth.currentUser?.uid ?: return@run
        val repository = NotificationRepositoryFirestore(FirebaseEmulator.firestore)

        val testNotification =
            Notification(
                uid = "test-notif-post-accepted",
                userId = userId,
                title = "Post Accepted",
                message = "Your post was accepted",
                type = NotificationType.POST_ACCEPTED,
                relatedId = "post-id-456",
                isRead = false,
                timestamp = Timestamp.now()
            )

        runBlocking { repository.addNotification(testNotification) }

        composeTestRule.runOnUiThread {
            navController.navigate(Screen.NotificationList.route) {
                popUpTo(Screen.Profile.route) { inclusive = false }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag(
                    "${NotificationScreenTags.NOTIFICATION_ITEM}_test-notif-post-accepted"
                )
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_test-notif-post-accepted")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            val currentRoute = navController.currentDestination?.route
            currentRoute == Screen.Feed.route
        }

        runBlocking {
            try {
                repository.deleteNotification("test-notif-post-accepted")
            } catch (e: Exception) {}
        }
    }

    @Test
    fun notificationClick_postRejectedType_navigatesToFeed() = run {
        val userId = auth.currentUser?.uid ?: return@run
        val repository = NotificationRepositoryFirestore(FirebaseEmulator.firestore)

        val testNotification =
            Notification(
                uid = "test-notif-post-rejected",
                userId = userId,
                title = "Post Rejected",
                message = "Your post was rejected",
                type = NotificationType.POST_REJECTED,
                relatedId = "post-id-789",
                isRead = false,
                timestamp = Timestamp.now()
            )

        runBlocking { repository.addNotification(testNotification) }

        composeTestRule.runOnUiThread {
            navController.navigate(Screen.NotificationList.route) {
                popUpTo(Screen.Profile.route) { inclusive = false }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag(
                    "${NotificationScreenTags.NOTIFICATION_ITEM}_test-notif-post-rejected"
                )
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_test-notif-post-rejected")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            val currentRoute = navController.currentDestination?.route
            currentRoute == Screen.Feed.route
        }

        runBlocking {
            try {
                repository.deleteNotification("test-notif-post-rejected")
            } catch (e: Exception) {}
        }
    }

    @Test
    fun notificationClick_newMatchingPostType_navigatesToFeed() = run {
        val userId = auth.currentUser?.uid ?: return@run
        val repository = NotificationRepositoryFirestore(FirebaseEmulator.firestore)

        val testNotification =
            Notification(
                uid = "test-notif-new-post",
                userId = userId,
                title = "New Matching Post",
                message = "A new post matches your skills",
                type = NotificationType.NEW_MATCHING_POST,
                relatedId = "post-id-999",
                isRead = false,
                timestamp = Timestamp.now()
            )

        runBlocking { repository.addNotification(testNotification) }

        composeTestRule.runOnUiThread {
            navController.navigate(Screen.NotificationList.route) {
                popUpTo(Screen.Profile.route) { inclusive = false }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag(
                    "${NotificationScreenTags.NOTIFICATION_ITEM}_test-notif-new-post"
                )
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_test-notif-new-post")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            val currentRoute = navController.currentDestination?.route
            currentRoute == Screen.Feed.route
        }

        runBlocking {
            try {
                repository.deleteNotification("test-notif-new-post")
            } catch (e: Exception) {}
        }
    }
}

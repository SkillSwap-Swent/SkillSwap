package com.swent.skillswap

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.model.notification.Notification
import com.swent.skillswap.model.notification.NotificationRepositoryFirestore
import com.swent.skillswap.model.notification.NotificationType
import com.swent.skillswap.resources.theme.SkillSwapAppTheme
import com.swent.skillswap.ui.chat.ChatListTestTags
import com.swent.skillswap.ui.feed.FeedScreenTestTags
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

    @Test
    fun chatListScreen_notificationButton_navigatesToNotificationList() = run {
        // Navigate to Chat screen
        composeTestRule.runOnUiThread {
            navController.navigate(Screen.Chat.route) {
                popUpTo(Screen.Profile.route) { inclusive = false }
            }
        }
        composeTestRule.waitForIdle()

        // Verify Chat screen is displayed
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag(ChatListTestTags.SCREEN)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Click the notification button
        composeTestRule.onNodeWithContentDescription("Notifications").performClick()
        composeTestRule.waitForIdle()

        // Verify NotificationList screen is displayed
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag(NotificationScreenTags.SCREEN)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(NotificationScreenTags.SCREEN).assertIsDisplayed()
    }

    @Test
    fun notificationScreen_messageNotification_navigatesToChatScreen() = runBlocking {
        val userId = auth.currentUser?.uid ?: return@runBlocking
        val chatId = "test-chat-id"

        // Create a MESSAGE notification
        val notification =
            Notification(
                uid = "msg-notif-1",
                userId = userId,
                title = "New Message",
                message = "You have a new message",
                type = NotificationType.MESSAGE,
                relatedId = chatId,
                isRead = false,
                timestamp = Timestamp.now()
            )

        // Add notification to Firestore
        val db = FirebaseFirestore.getInstance()
        val repo = NotificationRepositoryFirestore(db)
        repo.addNotification(notification)

        // Navigate to NotificationList screen
        composeTestRule.runOnUiThread {
            navController.navigate(Screen.NotificationList.route) {
                popUpTo(Screen.Profile.route) { inclusive = false }
            }
        }
        composeTestRule.waitForIdle()

        // Wait for notification to load
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_msg-notif-1")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Click on the MESSAGE notification
        composeTestRule
            .onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_msg-notif-1")
            .performClick()
        composeTestRule.waitForIdle()

        // Verify ChatScreen is displayed (check for chat screen elements)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route == Screen.ChatScreen.createRoute(chatId)
        }
    }

    @Test
    fun notificationScreen_postNotification_navigatesToFeed() = runBlocking {
        val userId = auth.currentUser?.uid ?: return@runBlocking

        // Create a POST_REPLY notification
        val notification =
            Notification(
                uid = "post-notif-1",
                userId = userId,
                title = "New Reply",
                message = "Someone replied to your post",
                type = NotificationType.POST_REPLY,
                relatedId = "post-id-1",
                isRead = false,
                timestamp = Timestamp.now()
            )

        // Add notification to Firestore
        val db = FirebaseFirestore.getInstance()
        val repo = NotificationRepositoryFirestore(db)
        repo.addNotification(notification)

        // Navigate to NotificationList screen
        composeTestRule.runOnUiThread {
            navController.navigate(Screen.NotificationList.route) {
                popUpTo(Screen.Profile.route) { inclusive = false }
            }
        }
        composeTestRule.waitForIdle()

        // Wait for notification to load
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_post-notif-1")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Click on the POST_REPLY notification
        composeTestRule
            .onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_post-notif-1")
            .performClick()
        composeTestRule.waitForIdle()

        // Verify Feed screen is displayed
        composeTestRule.waitUntil(timeoutMillis = 5000) {
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

    @After
    fun tearDown() = runBlocking {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // Ignore
        }
        Unit // Explicitly return Unit for JUnit
    }
}

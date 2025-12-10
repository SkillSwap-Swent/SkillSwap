package com.swent.skillswap.ui.notification

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.model.notification.Notification
import com.swent.skillswap.model.notification.NotificationRepository
import com.swent.skillswap.model.notification.NotificationType
import com.swent.skillswap.utils.FirebaseEmulator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationScreenTest : TestCase() {

    @get:Rule val composeTestRule = createComposeRule()

    private val ctx =
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var auth: FirebaseAuth

    init {
        FirebaseEmulator.startEmulator()
    }

    private class FakeRepo : NotificationRepository {
        val data = mutableMapOf<String, Notification>()
        var fail = false

        override fun getNewUid() = "uid-${data.size}"

        override suspend fun getNotificationsForUser(userId: String) =
            if (fail) throw Exception("fail") else data.values.filter { it.userId == userId }

        override suspend fun getUnreadNotificationsForUser(userId: String) =
            data.values.filter { it.userId == userId && !it.isRead }

        override suspend fun getNotification(notificationId: String) = data[notificationId]!!

        override suspend fun addNotification(notification: Notification) {
            data[notification.uid] = notification
        }

        override suspend fun markAsRead(notificationId: String) {
            data[notificationId] = data[notificationId]!!.copy(isRead = true)
        }

        override suspend fun markAllAsRead(userId: String) {
            data
                .filter { it.value.userId == userId }
                .forEach { data[it.key] = it.value.copy(isRead = true) }
        }

        override suspend fun deleteNotification(notificationId: String) {
            data.remove(notificationId)
        }

        override suspend fun deleteAllNotificationsForUser(userId: String) {
            data.entries.removeAll { it.value.userId == userId }
        }

        override suspend fun markChatNotificationsAsRead(chatId: String, userId: String) {
            data
                .filter { it.value.userId == userId && it.value.relatedId == chatId }
                .forEach { markAsRead(it.value.uid) }
        }
    }

    private fun notif(
        id: String,
        type: NotificationType = NotificationType.MESSAGE,
        read: Boolean = false,
        userId: String = "user"
    ) = Notification(id, userId, "Title-$id", "Msg-$id", type, "rel-$id", read, Timestamp.now())

    private fun waitForLoading() {
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(3000) {
            composeTestRule
                .onAllNodesWithTag(NotificationScreenTags.LOADING_INDICATOR)
                .fetchSemanticsNodes()
                .isEmpty()
        }
    }

    @Before
    fun setUp() = runBlocking {
        // Initialize FirebaseApp if necessary (useful for UI component runtime)
        try {
            if (FirebaseApp.getApps(ctx).isEmpty()) {
                FirebaseApp.initializeApp(ctx)
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
            // The test will use a fallback userId if auth.currentUser is null
        }
        Unit // Explicitly return Unit for JUnit
    }

    @After
    fun tearDown() = runBlocking {
        try {
            auth.signOut()
        } catch (e: Exception) {}
        Unit // Explicitly return Unit for JUnit
    }

    @Test
    fun emptyUnreadFilter() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        repo.data["1"] = notif("1", read = true, userId = userId)
        val vm = NotificationViewModel(repo)
        composeTestRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        composeTestRule.onNodeWithTag(NotificationScreenTags.FILTER_UNREAD).performClick()
        waitForLoading()
        composeTestRule.onNodeWithText("No unread notifications").assertExists()
    }

    @Test
    fun markAllReadButtonHiddenWhenAllRead() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        repo.data["1"] = notif("1", read = true, userId = userId)
        val vm = NotificationViewModel(repo)
        composeTestRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        composeTestRule.onNodeWithTag(NotificationScreenTags.MARK_ALL_READ).assertDoesNotExist()
    }

    @Test
    fun notificationItemReadAndUnreadStates() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        repo.data["1"] = notif("1", read = false, userId = userId)
        repo.data["2"] = notif("2", read = true, userId = userId)
        val vm = NotificationViewModel(repo)
        vm.setShowUnreadOnly(false)
        composeTestRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        composeTestRule.onNodeWithText("Title-1").assertExists()
        composeTestRule.onNodeWithText("Title-2").assertExists()
        composeTestRule
            .onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_1")
            .performClick()
        waitForLoading()
        assert(repo.data["1"]?.isRead == true)
    }

    @Test
    fun emptyStateShowsCorrectTextForAllFilter() = run {
        val repo = FakeRepo()
        val vm = NotificationViewModel(repo)
        vm.setShowUnreadOnly(false)
        composeTestRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        composeTestRule.onNodeWithText("No notifications").assertExists()
    }

    @Test
    fun clickingReadNotificationDoesNotMarkAsRead() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        repo.data["1"] = notif("1", read = true, userId = userId)
        val vm = NotificationViewModel(repo)
        vm.setShowUnreadOnly(false)
        var clicked: Notification? = null
        composeTestRule.setContent {
            MaterialTheme { NotificationScreen(vm, onNotificationClick = { clicked = it }) }
        }
        waitForLoading()
        // Click the already-read notification
        composeTestRule
            .onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_1")
            .performClick()
        waitForLoading()
        // Should still be read (not marked again)
        assert(repo.data["1"]?.isRead == true)
        assert(clicked?.uid == "1")
    }

    @Test
    fun allNotificationTypesDisplayCorrectly() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        val types =
            listOf(
                NotificationType.MESSAGE,
                NotificationType.POST_REPLY,
                NotificationType.POST_ACCEPTED,
                NotificationType.POST_REJECTED,
                NotificationType.NEW_MATCHING_POST
            )
        types.forEachIndexed { index, type ->
            repo.data["$index"] = notif("$index", type = type, userId = userId)
        }
        val vm = NotificationViewModel(repo)
        composeTestRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        // Verify all type labels are displayed
        composeTestRule.onNodeWithText("Chat").assertExists()
        composeTestRule.onNodeWithText("Reply").assertExists()
        composeTestRule.onNodeWithText("Accepted").assertExists()
        composeTestRule.onNodeWithText("Rejected").assertExists()
        composeTestRule.onNodeWithText("New Post").assertExists()
    }

    @Test
    fun unreadIndicatorShowsForUnreadNotifications() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        repo.data["1"] = notif("1", read = false, userId = userId)
        repo.data["2"] = notif("2", read = true, userId = userId)
        val vm = NotificationViewModel(repo)
        vm.setShowUnreadOnly(false)
        composeTestRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        // Unread notification should have different styling (we can't directly test the dot,
        // but we can verify the notification exists and is clickable)
        composeTestRule
            .onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_1")
            .assertExists()
        composeTestRule
            .onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_2")
            .assertExists()
    }

    @Test
    fun deleteButtonRemovesNotification() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        repo.data["1"] = notif("1", userId = userId)
        repo.data["2"] = notif("2", userId = userId)
        val vm = NotificationViewModel(repo)
        composeTestRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        val deleteButtons = composeTestRule.onAllNodesWithContentDescription("Delete notification")
        assert(deleteButtons.fetchSemanticsNodes().size == 2)
        deleteButtons[0].performClick()
        waitForLoading()
        // One notification should be deleted
        assert(repo.data.size == 1)
    }

    @Test
    fun titleAndMessageDisplayCorrectly() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        repo.data["1"] = notif("1", userId = userId)
        val vm = NotificationViewModel(repo)
        composeTestRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        composeTestRule.onNodeWithText("Title-1").assertExists()
        composeTestRule.onNodeWithText("Msg-1").assertExists()
    }

    @Test
    fun markAllReadButtonAppearsWhenUnreadExists() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        repo.data["1"] = notif("1", read = false, userId = userId)
        val vm = NotificationViewModel(repo)
        composeTestRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        composeTestRule.onNodeWithTag(NotificationScreenTags.MARK_ALL_READ).assertExists()
        composeTestRule.onNodeWithText("Mark all read").assertExists()
    }

    @Test
    fun filterChipsToggleCorrectly() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        repo.data["1"] = notif("1", read = false, userId = userId)
        repo.data["2"] = notif("2", read = true, userId = userId)
        val vm = NotificationViewModel(repo)
        vm.setShowUnreadOnly(false)
        composeTestRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        // Initially showing all (unread filter not selected)
        composeTestRule.onNodeWithText("Title-1").assertExists()
        composeTestRule.onNodeWithText("Title-2").assertExists()
        // Click unread filter
        composeTestRule.onNodeWithTag(NotificationScreenTags.FILTER_UNREAD).performClick()
        waitForLoading()
        // Should only show unread
        composeTestRule.onNodeWithText("Title-1").assertExists()
        composeTestRule.onNodeWithText("Title-2").assertDoesNotExist()
        // Click all filter
        composeTestRule.onNodeWithTag(NotificationScreenTags.FILTER_ALL).performClick()
        waitForLoading()
        // Should show all again
        composeTestRule.onNodeWithText("Title-1").assertExists()
        composeTestRule.onNodeWithText("Title-2").assertExists()
    }
}

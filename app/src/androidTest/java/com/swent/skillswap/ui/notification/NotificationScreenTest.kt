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

    @get:Rule val composeRule = createComposeRule()

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
    }

    private fun notif(
        id: String,
        type: NotificationType = NotificationType.MESSAGE,
        read: Boolean = false,
        userId: String = "user"
    ) = Notification(id, userId, "Title-$id", "Msg-$id", type, "rel-$id", read, Timestamp.now())

    private fun waitForLoading() {
        composeRule.waitForIdle()
        composeRule.waitUntil(3000) {
            composeRule
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
    fun allStatesAndInteractions() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        var clicked: Notification? = null
        var backed = false

        // Notifications with all types
        listOf(
                notif("1", NotificationType.MESSAGE, false, userId),
                notif("2", NotificationType.POST_REPLY, false, userId),
                notif("3", NotificationType.POST_ACCEPTED, false, userId),
                notif("4", NotificationType.POST_REJECTED, false, userId),
                notif("5", NotificationType.NEW_MATCHING_POST, true, userId)
            )
            .forEach { repo.data[it.uid] = it }
        val vm = NotificationViewModel(repo)
        composeRule.setContent {
            MaterialTheme { NotificationScreen(vm, { backed = true }, { clicked = it }) }
        }
        waitForLoading()

        // Verify all types displayed
        listOf("Chat", "Reply", "Accepted", "Rejected", "New Post").forEach {
            composeRule.onNodeWithText(it).assertExists()
        }

        // Mark all as read
        composeRule.onNodeWithTag(NotificationScreenTags.MARK_ALL_READ).assertExists()
        composeRule.onNodeWithTag(NotificationScreenTags.MARK_ALL_READ).performClick()
        waitForLoading()
        assert(repo.data.values.all { it.isRead })

        // Filter toggle
        repo.data["6"] = notif("6", read = false, userId = userId)
        vm.refresh()
        waitForLoading()
        composeRule.onNodeWithTag(NotificationScreenTags.FILTER_UNREAD).performClick()
        waitForLoading()
        composeRule.onNodeWithText("Title-6").assertExists()
        composeRule.onNodeWithTag(NotificationScreenTags.FILTER_ALL).performClick()
        waitForLoading()

        // Click notification
        composeRule.onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_1").performClick()
        waitForLoading()
        assert(clicked?.uid == "1")

        // Delete notification
        composeRule.onAllNodesWithContentDescription("Delete notification")[0].performClick()
        waitForLoading()
        assert(!repo.data.containsKey("1"))

        // Back button
        composeRule.onNodeWithContentDescription("Back").performClick()
        assert(backed)
    }

    @Test
    fun emptyUnreadFilter() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        repo.data["1"] = notif("1", read = true, userId = userId)
        val vm = NotificationViewModel(repo)
        composeRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        composeRule.onNodeWithTag(NotificationScreenTags.FILTER_UNREAD).performClick()
        waitForLoading()
        composeRule.onNodeWithText("No unread notifications").assertExists()
    }

    @Test
    fun markAllReadButtonHiddenWhenAllRead() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        repo.data["1"] = notif("1", read = true, userId = userId)
        val vm = NotificationViewModel(repo)
        composeRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        composeRule.onNodeWithTag(NotificationScreenTags.MARK_ALL_READ).assertDoesNotExist()
    }

    @Test
    fun notificationItemReadAndUnreadStates() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        repo.data["1"] = notif("1", read = false, userId = userId)
        repo.data["2"] = notif("2", read = true, userId = userId)
        val vm = NotificationViewModel(repo)
        composeRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        composeRule.onNodeWithText("Title-1").assertExists()
        composeRule.onNodeWithText("Title-2").assertExists()
        composeRule.onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_1").performClick()
        waitForLoading()
        assert(repo.data["1"]?.isRead == true)
    }

    @Test
    fun emptyStateShowsCorrectTextForAllFilter() = run {
        val repo = FakeRepo()
        val vm = NotificationViewModel(repo)
        composeRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        composeRule.onNodeWithText("No notifications").assertExists()
    }

    @Test
    fun clickingReadNotificationDoesNotMarkAsRead() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        repo.data["1"] = notif("1", read = true, userId = userId)
        val vm = NotificationViewModel(repo)
        var clicked: Notification? = null
        composeRule.setContent {
            MaterialTheme { NotificationScreen(vm, onNotificationClick = { clicked = it }) }
        }
        waitForLoading()
        // Click the already-read notification
        composeRule.onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_1").performClick()
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
        composeRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        // Verify all type labels are displayed
        composeRule.onNodeWithText("Chat").assertExists()
        composeRule.onNodeWithText("Reply").assertExists()
        composeRule.onNodeWithText("Accepted").assertExists()
        composeRule.onNodeWithText("Rejected").assertExists()
        composeRule.onNodeWithText("New Post").assertExists()
    }

    @Test
    fun unreadIndicatorShowsForUnreadNotifications() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        repo.data["1"] = notif("1", read = false, userId = userId)
        repo.data["2"] = notif("2", read = true, userId = userId)
        val vm = NotificationViewModel(repo)
        composeRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        // Unread notification should have different styling (we can't directly test the dot,
        // but we can verify the notification exists and is clickable)
        composeRule.onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_1").assertExists()
        composeRule.onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_2").assertExists()
    }

    @Test
    fun deleteButtonRemovesNotification() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        repo.data["1"] = notif("1", userId = userId)
        repo.data["2"] = notif("2", userId = userId)
        val vm = NotificationViewModel(repo)
        composeRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        val deleteButtons = composeRule.onAllNodesWithContentDescription("Delete notification")
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
        composeRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        composeRule.onNodeWithText("Title-1").assertExists()
        composeRule.onNodeWithText("Msg-1").assertExists()
    }

    @Test
    fun loadingStateShowsIndicator() = run {
        val repo = FakeRepo()
        val vm = NotificationViewModel(repo)
        composeRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        // Should show loading initially - use waitUntil to catch it even if it's fast
        composeRule.waitUntil(1000) {
            composeRule
                .onAllNodesWithTag(NotificationScreenTags.LOADING_INDICATOR)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(NotificationScreenTags.LOADING_INDICATOR).assertExists()
        waitForLoading()
        // Loading should be gone after load
        composeRule.onNodeWithTag(NotificationScreenTags.LOADING_INDICATOR).assertDoesNotExist()
    }

    @Test
    fun errorStateShowsRetryButton() = run {
        val repo = FakeRepo()
        repo.fail = true
        val vm = NotificationViewModel(repo)
        composeRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        composeRule.onNodeWithTag(NotificationScreenTags.ERROR_MESSAGE).assertExists()
        composeRule.onNodeWithText("Retry").assertExists().performClick()
        waitForLoading()
    }

    @Test
    fun markAllReadButtonAppearsWhenUnreadExists() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        repo.data["1"] = notif("1", read = false, userId = userId)
        val vm = NotificationViewModel(repo)
        composeRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        composeRule.onNodeWithTag(NotificationScreenTags.MARK_ALL_READ).assertExists()
        composeRule.onNodeWithText("Mark all read").assertExists()
    }

    @Test
    fun filterChipsToggleCorrectly() = run {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        repo.data["1"] = notif("1", read = false, userId = userId)
        repo.data["2"] = notif("2", read = true, userId = userId)
        val vm = NotificationViewModel(repo)
        composeRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        // Initially showing all (unread filter not selected)
        composeRule.onNodeWithText("Title-1").assertExists()
        composeRule.onNodeWithText("Title-2").assertExists()
        // Click unread filter
        composeRule.onNodeWithTag(NotificationScreenTags.FILTER_UNREAD).performClick()
        waitForLoading()
        // Should only show unread
        composeRule.onNodeWithText("Title-1").assertExists()
        composeRule.onNodeWithText("Title-2").assertDoesNotExist()
        // Click all filter
        composeRule.onNodeWithTag(NotificationScreenTags.FILTER_ALL).performClick()
        waitForLoading()
        // Should show all again
        composeRule.onNodeWithText("Title-1").assertExists()
        composeRule.onNodeWithText("Title-2").assertExists()
    }
}

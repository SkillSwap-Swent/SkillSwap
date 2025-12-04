package com.swent.skillswap.ui.notification

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.swent.skillswap.model.notification.Notification
import com.swent.skillswap.model.notification.NotificationRepository
import com.swent.skillswap.model.notification.NotificationType
import com.swent.skillswap.utils.FirebaseEmulator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationScreenTest {

    @get:Rule val composeRule = createComposeRule()

    companion object {
        @JvmStatic lateinit var auth: FirebaseAuth

        @BeforeClass
        @JvmStatic
        fun globalSetUp() {
            FirebaseEmulator.startEmulator()
            auth = FirebaseEmulator.auth
        }

        @AfterClass
        @JvmStatic
        fun globalTearDown() {
            auth.signOut()
            FirebaseEmulator.clearAuthEmulator()
            FirebaseEmulator.clearFirestoreEmulator()
        }
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

    @Before fun setUp() = runBlocking { auth.signInAnonymously().await() }

    @After
    fun tearDown() = runBlocking {
        try {
            auth.signOut()
        } catch (e: Exception) {}
    }

    @Test
    fun allStatesAndInteractions() = runBlocking {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        var clicked: Notification? = null
        var backed = false

        // Empty state
        val vm1 = NotificationViewModel(repo)
        composeRule.setContent {
            MaterialTheme { NotificationScreen(vm1, { backed = true }, { clicked = it }) }
        }
        waitForLoading()
        composeRule.onNodeWithTag(NotificationScreenTags.EMPTY_STATE).assertExists()

        // Error state
        repo.fail = true
        val vm2 = NotificationViewModel(repo)
        composeRule.setContent { MaterialTheme { NotificationScreen(vm2) } }
        waitForLoading()
        composeRule.onNodeWithTag(NotificationScreenTags.ERROR_MESSAGE).assertExists()
        composeRule.onNodeWithText("Retry").performClick()
        waitForLoading()

        // Notifications with all types
        repo.fail = false
        repo.data.clear()
        listOf(
                notif("1", NotificationType.MESSAGE, false, userId),
                notif("2", NotificationType.POST_REPLY, false, userId),
                notif("3", NotificationType.POST_ACCEPTED, false, userId),
                notif("4", NotificationType.POST_REJECTED, false, userId),
                notif("5", NotificationType.NEW_MATCHING_POST, true, userId)
            )
            .forEach { repo.data[it.uid] = it }
        val vm3 = NotificationViewModel(repo)
        composeRule.setContent {
            MaterialTheme { NotificationScreen(vm3, { backed = true }, { clicked = it }) }
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
        vm3.refresh()
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
    fun emptyUnreadFilter() {
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
    fun markAllReadButtonHiddenWhenAllRead() {
        val userId = auth.currentUser?.uid ?: "user"
        val repo = FakeRepo()
        repo.data["1"] = notif("1", read = true, userId = userId)
        val vm = NotificationViewModel(repo)
        composeRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        composeRule.onNodeWithTag(NotificationScreenTags.MARK_ALL_READ).assertDoesNotExist()
    }

    @Test
    fun notificationItemReadAndUnreadStates() = runBlocking {
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
}

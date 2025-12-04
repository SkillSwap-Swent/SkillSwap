package com.swent.skillswap.ui.notification

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.swent.skillswap.model.notification.Notification
import com.swent.skillswap.model.notification.NotificationRepository
import com.swent.skillswap.model.notification.NotificationType
import com.swent.skillswap.ui.navigation.BottomNavigationMenu
import com.swent.skillswap.ui.navigation.NavigationTestTags
import com.swent.skillswap.ui.navigation.Tab
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationScreenTest {

    @get:Rule val composeRule = createComposeRule()

    private class FakeRepo : NotificationRepository {
        val data = mutableMapOf<String, Notification>()
        var fail = false

        override fun getNewUid() = "uid-${data.size}"

        override suspend fun getNotificationsForUser(userId: String) =
            if (fail) throw Exception("fail") else data.values.filter { it.userId == userId }

        override suspend fun getUnreadNotificationsForUser(userId: String) =
            data.values.filter { it.userId == userId && !it.isRead }

        override suspend fun getNotification(id: String) = data[id]!!

        override suspend fun addNotification(n: Notification) {
            data[n.uid] = n
        }

        override suspend fun markAsRead(id: String) {
            data[id] = data[id]!!.copy(isRead = true)
        }

        override suspend fun markAllAsRead(userId: String) {
            data
                .filter { it.value.userId == userId }
                .forEach { data[it.key] = it.value.copy(isRead = true) }
        }

        override suspend fun deleteNotification(id: String) {
            data.remove(id)
        }

        override suspend fun deleteAllNotificationsForUser(userId: String) {
            data.entries.removeAll { it.value.userId == userId }
        }
    }

    private fun notif(
        id: String,
        type: NotificationType = NotificationType.MESSAGE,
        read: Boolean = false
    ) = Notification(id, "user", "Title-$id", "Msg", type, "rel-$id", read, Timestamp.now())

    private fun waitForLoading() {
        composeRule.waitForIdle()
        composeRule.waitUntil(3000) {
            composeRule
                .onAllNodesWithTag(NotificationScreenTags.LOADING_INDICATOR)
                .fetchSemanticsNodes()
                .isEmpty()
        }
    }

    @Test
    fun allStatesAndTypes() {
        val repo = FakeRepo()
        var vm = NotificationViewModel(repo)
        var clicked: Notification? = null
        var backed = false

        // Test empty state
        composeRule.setContent {
            MaterialTheme { NotificationScreen(vm, { backed = true }, { clicked = it }) }
        }
        waitForLoading()
        composeRule.onNodeWithTag(NotificationScreenTags.EMPTY_STATE).assertExists()
        composeRule.onNodeWithText("No notifications").assertExists()

        // Test error state
        repo.fail = true
        vm = NotificationViewModel(repo)
        composeRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        composeRule.onNodeWithTag(NotificationScreenTags.ERROR_MESSAGE).assertExists()
        composeRule.onNodeWithText("Retry").assertExists()

        // Test all notification types displayed
        repo.fail = false
        repo.data.clear()
        listOf(
                notif("1", NotificationType.MESSAGE),
                notif("2", NotificationType.POST_REPLY),
                notif("3", NotificationType.POST_ACCEPTED),
                notif("4", NotificationType.POST_REJECTED),
                notif("5", NotificationType.NEW_MATCHING_POST, true)
            )
            .forEach { repo.data[it.uid] = it }
        vm = NotificationViewModel(repo)
        composeRule.setContent {
            MaterialTheme { NotificationScreen(vm, { backed = true }, { clicked = it }) }
        }
        waitForLoading()
        listOf("Chat", "Reply", "Accepted", "Rejected", "New Post").forEach {
            composeRule.onNodeWithText(it).assertExists()
        }

        // Test mark all read button (has unread)
        composeRule.onNodeWithTag(NotificationScreenTags.MARK_ALL_READ).assertExists()
        composeRule.onNodeWithTag(NotificationScreenTags.MARK_ALL_READ).performClick()
        waitForLoading()
        assert(repo.data.values.all { it.isRead })

        // Test filters
        repo.data["6"] = notif("6", read = false)
        vm.refresh()
        waitForLoading()
        composeRule.onNodeWithTag(NotificationScreenTags.FILTER_UNREAD).performClick()
        waitForLoading()
        composeRule.onNodeWithText("Title-6").assertExists()
        composeRule.onNodeWithTag(NotificationScreenTags.FILTER_ALL).performClick()
        waitForLoading()

        // Test click notification + relatedId in callback
        composeRule.onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_6").performClick()
        waitForLoading()
        assert(clicked?.uid == "6" && clicked?.relatedId == "rel-6")
        assert(repo.data["6"]?.isRead == true)

        // Test delete
        composeRule.onAllNodesWithContentDescription("Delete notification")[0].performClick()
        waitForLoading()
        Thread.sleep(100)
        assert(repo.data.size < 6)

        // Test back
        composeRule.onNodeWithContentDescription("Back").performClick()
        assert(backed)
    }

    @Test
    fun emptyUnreadFilter() {
        val repo = FakeRepo()
        repo.data["1"] = notif("1", read = true)
        val vm = NotificationViewModel(repo)
        composeRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        composeRule.onNodeWithTag(NotificationScreenTags.FILTER_UNREAD).performClick()
        waitForLoading()
        composeRule.onNodeWithText("No unread notifications").assertExists()
    }

    @Test
    fun retryLoadsData() {
        val repo = FakeRepo()
        repo.fail = true
        val vm = NotificationViewModel(repo)
        composeRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        composeRule.onNodeWithTag(NotificationScreenTags.ERROR_MESSAGE).assertExists()
        repo.fail = false
        repo.data["1"] = notif("1")
        composeRule.onNodeWithText("Retry").performClick()
        waitForLoading()
        composeRule.onNodeWithText("Title-1").assertExists()
    }

    @Test
    fun markAllReadHiddenWhenAllRead() {
        val repo = FakeRepo()
        repo.data["1"] = notif("1", read = true)
        val vm = NotificationViewModel(repo)
        composeRule.setContent { MaterialTheme { NotificationScreen(vm) } }
        waitForLoading()
        composeRule.onNodeWithTag(NotificationScreenTags.MARK_ALL_READ).assertDoesNotExist()
    }

    @Test
    fun clickAlreadyReadNotification() {
        val repo = FakeRepo()
        repo.data["r"] = notif("r", read = true)
        var clicked: Notification? = null
        composeRule.setContent {
            MaterialTheme { NotificationScreen(NotificationViewModel(repo), {}, { clicked = it }) }
        }
        waitForLoading()
        composeRule.onNodeWithTag("${NotificationScreenTags.NOTIFICATION_ITEM}_r").performClick()
        waitForLoading()
        assert(clicked?.uid == "r")
    }

    @Test
    fun multipleSameTypeNotifications() {
        val repo = FakeRepo()
        (1..3).forEach { repo.data["m$it"] = notif("m$it", NotificationType.MESSAGE) }
        composeRule.setContent { MaterialTheme { NotificationScreen(NotificationViewModel(repo)) } }
        waitForLoading()
        composeRule.onAllNodesWithText("Chat").assertCountEquals(3)
    }

    @Test
    fun screenDisplaysTitleAndTags() {
        val repo = FakeRepo()
        repo.data["1"] = notif("1")
        composeRule.setContent { MaterialTheme { NotificationScreen(NotificationViewModel(repo)) } }
        waitForLoading()
        composeRule.onNodeWithTag(NotificationScreenTags.TITLE).assertExists()
        composeRule.onNodeWithText("Notifications").assertExists()
        composeRule.onNodeWithTag(NotificationScreenTags.SCREEN).assertExists()
        composeRule.onNodeWithTag(NotificationScreenTags.NOTIFICATIONS_LIST).assertExists()
    }

    @Test
    fun notificationDisplaysMessageAndTimestamp() {
        val repo = FakeRepo()
        repo.data["1"] =
            Notification(
                "1",
                "user",
                "MyTitle",
                "MyMessage",
                NotificationType.MESSAGE,
                null,
                false,
                Timestamp.now()
            )
        composeRule.setContent { MaterialTheme { NotificationScreen(NotificationViewModel(repo)) } }
        waitForLoading()
        composeRule.onNodeWithText("MyTitle").assertExists()
        composeRule.onNodeWithText("MyMessage").assertExists()
    }

    @Test
    fun bottomNavBadgeDisplaysAndClicks() {
        val repo = FakeRepo()
        repo.data["1"] = notif("1")
        repo.data["2"] = notif("2")
        var tabClicked: Tab? = null
        var badgeClicked = false
        composeRule.setContent {
            MaterialTheme {
                BottomNavigationMenu(
                    selectedTab = Tab.Profile,
                    onTabSelected = { tabClicked = it },
                    notificationViewModel = NotificationViewModel(repo),
                    onNotificationBadgeClick = { badgeClicked = true }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("2").assertExists()
        composeRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()
        assert(tabClicked == Tab.Chat)
        composeRule.onNodeWithTag(NavigationTestTags.FEED_TAB).performClick()
        assert(tabClicked == Tab.Feed)
        composeRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
        assert(tabClicked == Tab.Profile)
    }

    @Test
    fun bottomNavNoBadgeWhenEmpty() {
        val repo = FakeRepo()
        composeRule.setContent {
            MaterialTheme {
                BottomNavigationMenu(
                    selectedTab = Tab.Profile,
                    onTabSelected = {},
                    notificationViewModel = NotificationViewModel(repo)
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertExists()
        composeRule.onAllNodes(hasText("0")).assertCountEquals(0)
    }
}

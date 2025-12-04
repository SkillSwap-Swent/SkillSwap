package com.swent.skillswap.ui.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.swent.skillswap.model.notification.Notification
import com.swent.skillswap.model.notification.NotificationRepository
import com.swent.skillswap.model.notification.NotificationType
import com.swent.skillswap.ui.notification.NotificationViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BottomNavigationMenuTest {

    @get:Rule val composeRule = createComposeRule()

    private class FakeRepo(private val notifications: List<Notification> = emptyList()) : NotificationRepository {
        override fun getNewUid() = "uid"
        override suspend fun getNotificationsForUser(userId: String) = notifications
        override suspend fun getUnreadNotificationsForUser(userId: String) = notifications.filter { !it.isRead }
        override suspend fun getNotification(id: String) = notifications.first { it.uid == id }
        override suspend fun addNotification(n: Notification) {}
        override suspend fun markAsRead(id: String) {}
        override suspend fun markAllAsRead(userId: String) {}
        override suspend fun deleteNotification(id: String) {}
        override suspend fun deleteAllNotificationsForUser(userId: String) {}
    }

    @Test
    fun badgeShowsAndClickable() {
        val notif = Notification("1", "u", "T", "M", NotificationType.MESSAGE, null, false, Timestamp.now())
        val vm = NotificationViewModel(FakeRepo(listOf(notif)))
        var tabClicked: Tab? = null
        var badgeClicked = false

        composeRule.setContent {
            MaterialTheme {
                BottomNavigationMenu(
                    selectedTab = Tab.Profile,
                    onTabSelected = { tabClicked = it },
                    notificationViewModel = vm,
                    onNotificationBadgeClick = { badgeClicked = true }
                )
            }
        }
        composeRule.waitForIdle()

        // Badge should show count
        composeRule.onNodeWithText("1").assertExists()

        // Tab selection works
        composeRule.onNodeWithTag(NavigationTestTags.FEED_TAB).performClick()
        assert(tabClicked == Tab.Feed)

        composeRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()
        assert(tabClicked == Tab.Chat)
    }

    @Test
    fun noBadgeWhenNoNotifications() {
        val vm = NotificationViewModel(FakeRepo())

        composeRule.setContent {
            MaterialTheme {
                BottomNavigationMenu(Tab.Profile, {}, notificationViewModel = vm)
            }
        }
        composeRule.waitForIdle()

        // No badge number should exist
        composeRule.onAllNodesWithText("0").assertCountEquals(0)
    }
}


/** Created with the help of Cursor */
package com.swent.skillswap.model.notification

import com.google.firebase.Timestamp
import com.swent.skillswap.model.chat.Chat
import com.swent.skillswap.model.post.PostType
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FakeNotificationRepositoryTest {

    private lateinit var repository: FakeNotificationRepository
    private val testUserId = "test-user-123"
    private val testUserId2 = "test-user-456"

    private val sampleNotification1 =
        Notification(
            "notification-1",
            testUserId,
            "New Message",
            "You have a new message",
            NotificationType.MESSAGE,
            "chat-1",
            false,
            Timestamp(Date(1000))
        )
    private val sampleNotification2 =
        Notification(
            "notification-2",
            testUserId,
            "Post Reply",
            "Someone replied to your post",
            NotificationType.POST_REPLY,
            "post-1",
            true,
            Timestamp(Date(2000))
        )
    private val sampleNotification3 =
        Notification(
            "notification-3",
            testUserId2,
            "New Match",
            "A new post matches your skills",
            NotificationType.NEW_MATCHING_POST,
            "post-2",
            false,
            Timestamp(Date(3000))
        )

    @Before
    fun setUp() {
        repository = FakeNotificationRepository()
    }

    @Test
    fun getNewUid_generatesUniqueIds() = runTest {
        val uid1 = repository.getNewUid()
        val uid2 = repository.getNewUid()
        assertNotEquals(uid1, uid2)
        assertTrue(uid1.startsWith("test-notification-"))
    }

    @Test
    fun addNotification_addsAndRetrievesCorrectly() = runTest {
        repository.addNotification(sampleNotification1)
        assertEquals(sampleNotification1, repository.getNotification(sampleNotification1.uid))
        repository.addNotification(sampleNotification2)
        repository.addNotification(sampleNotification3)
        assertEquals(3, repository.getAddedNotifications().size)
    }

    @Test
    fun getNotification_handlesExistingAndNonExistent() = runTest {
        repository.addNotification(sampleNotification1)
        assertEquals(sampleNotification1, repository.getNotification(sampleNotification1.uid))
        try {
            repository.getNotification("non-existent")
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("Notification not found"))
        }
    }

    @Test
    fun getNotificationsForUser_filtersCorrectly() = runTest {
        repository.addNotification(sampleNotification1)
        repository.addNotification(sampleNotification2)
        repository.addNotification(sampleNotification3)
        val userNotifications = repository.getNotificationsForUser(testUserId)
        assertEquals(2, userNotifications.size)
        assertTrue(userNotifications.all { it.userId == testUserId })
    }

    @Test
    fun getUnreadNotificationsForUser_filtersCorrectly() = runTest {
        repository.addNotification(sampleNotification1)
        repository.addNotification(sampleNotification2)
        val unread = repository.getUnreadNotificationsForUser(testUserId)
        assertEquals(1, unread.size)
        assertFalse(unread[0].isRead)
    }

    @Test
    fun markAsRead_handlesAllCases() = runTest {
        repository.addNotification(sampleNotification1)
        repository.markAsRead(sampleNotification1.uid)
        assertTrue(repository.getNotification(sampleNotification1.uid).isRead)
        try {
            repository.markAsRead("non-existent")
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("Cannot mark non-existent notification"))
        }
    }

    @Test
    fun markAllAsRead_marksAllUserNotificationsAsRead() = runTest {
        repository.addNotification(sampleNotification1)
        repository.addNotification(sampleNotification2)
        repository.markAllAsRead(testUserId)
        assertTrue(repository.getNotificationsForUser(testUserId).all { it.isRead })
    }

    @Test
    fun deleteNotification_deletesSuccessfully() = runTest {
        repository.addNotification(sampleNotification1)
        repository.deleteNotification(sampleNotification1.uid)
        try {
            repository.getNotification(sampleNotification1.uid)
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("Notification not found"))
        }
    }

    @Test
    fun deleteAllNotificationsForUser_deletesOnlyUserNotifications() = runTest {
        repository.addNotification(sampleNotification1)
        repository.addNotification(sampleNotification3)
        repository.deleteAllNotificationsForUser(testUserId)
        assertTrue(repository.getNotificationsForUser(testUserId).isEmpty())
    }

    @Test
    fun setShouldFail_controlsAllFailureFlags() = runTest {
        repository.setShouldFail(true)
        repository.setShouldFailOnAdd(false)
        repository.addNotification(sampleNotification1)
        repository.setShouldFailOnGet(true)
        try {
            repository.getNotification(sampleNotification1.uid)
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("Simulated get failure"))
        }
    }

    @Test
    fun helperMethods_workCorrectly() = runTest {
        repository.addNotification(sampleNotification1)
        assertEquals(sampleNotification1, repository.getNotificationById(sampleNotification1.uid))
        assertNull(repository.getNotificationById("non-existent"))
        repository.clear()
        assertEquals(0, repository.getAddedNotifications().size)
        assertTrue(repository.getNewUid().startsWith("test-notification-0"))
    }

    @Test
    fun markChatNotificationsAsRead_correctly_marks_chatNotifications_as_read() = runTest {
        // Add notifications: two for chat-1 (one for each user), one for another chat
        val chatNotificationUser1 = sampleNotification1
        val chatNotificationUser2 = sampleNotification1.copy(
            uid = "notification-4",
            userId = testUserId2,
            isRead = false
        ) // chat-1, testUserId2
        val otherChatNotification = sampleNotification1.copy(
            uid = "notification-5",
            relatedId = "chat-2",
            isRead = false
        ) // chat-2, testUserId

        repository.addNotification(chatNotificationUser1)
        repository.addNotification(chatNotificationUser2)
        repository.addNotification(otherChatNotification)

        // Mark chat-1 notifications as read for testUserId
        repository.markChatNotificationsAsRead("chat-1", testUserId)

        // Only chatNotificationUser1 should be marked as read
        assertTrue(repository.getNotification(chatNotificationUser1.uid).isRead)
        assertFalse(repository.getNotification(chatNotificationUser2.uid).isRead)
        assertFalse(repository.getNotification(otherChatNotification.uid).isRead)
    }

}

/** Created with the help of Cursor */
package com.swent.skillswap.model.notification

import com.google.firebase.Timestamp
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
            uid = "notification-1",
            userId = testUserId,
            title = "New Message",
            message = "You have a new message",
            type = NotificationType.MESSAGE,
            relatedId = "chat-1",
            isRead = false,
            timestamp = Timestamp(Date(1000))
        )

    private val sampleNotification2 =
        Notification(
            uid = "notification-2",
            userId = testUserId,
            title = "Post Reply",
            message = "Someone replied to your post",
            type = NotificationType.POST_REPLY,
            relatedId = "post-1",
            isRead = true,
            timestamp = Timestamp(Date(2000))
        )

    private val sampleNotification3 =
        Notification(
            uid = "notification-3",
            userId = testUserId2,
            title = "New Match",
            message = "A new post matches your skills",
            type = NotificationType.NEW_MATCHING_POST,
            relatedId = "post-2",
            isRead = false,
            timestamp = Timestamp(Date(3000))
        )

    @Before
    fun setUp() {
        repository = FakeNotificationRepository()
    }

    @Test
    fun getNewUid_generatesUniqueIds() = runTest {
        val uid1 = repository.getNewUid()
        val uid2 = repository.getNewUid()
        val uid3 = repository.getNewUid()

        assertNotEquals(uid1, uid2)
        assertNotEquals(uid2, uid3)
        assertNotEquals(uid1, uid3)
        assertTrue(uid1.startsWith("test-notification-"))
    }

    @Test
    fun addNotification_addsNotificationSuccessfully() = runTest {
        repository.addNotification(sampleNotification1)

        val retrieved = repository.getNotification(sampleNotification1.uid)
        assertEquals(sampleNotification1, retrieved)
    }

    @Test
    fun addNotification_multipleNotifications_storedCorrectly() = runTest {
        repository.addNotification(sampleNotification1)
        repository.addNotification(sampleNotification2)
        repository.addNotification(sampleNotification3)

        assertEquals(3, repository.getAddedNotifications().size)
    }

    @Test
    fun addNotification_withFailureFlag_throwsException() = runTest {
        repository.setShouldFailOnAdd(true)

        try {
            repository.addNotification(sampleNotification1)
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertEquals("Simulated add failure", e.message)
        }
    }

    @Test
    fun getNotification_existingNotification_returnsCorrectNotification() = runTest {
        repository.addNotification(sampleNotification1)
        val retrieved = repository.getNotification(sampleNotification1.uid)
        assertEquals(sampleNotification1, retrieved)
    }

    @Test
    fun getNotification_nonExistentNotification_throwsException() = runTest {
        try {
            repository.getNotification("non-existent")
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("Notification not found"))
        }
    }

    @Test
    fun getNotification_withFailureFlag_throwsException() = runTest {
        repository.addNotification(sampleNotification1)
        repository.setShouldFailOnGet(true)

        try {
            repository.getNotification(sampleNotification1.uid)
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertEquals("Simulated get failure", e.message)
        }
    }

    @Test
    fun getNotificationsForUser_returnsOnlyUserNotifications() = runTest {
        repository.addNotification(sampleNotification1)
        repository.addNotification(sampleNotification2)
        repository.addNotification(sampleNotification3)

        val userNotifications = repository.getNotificationsForUser(testUserId)

        assertEquals(2, userNotifications.size)
        assertTrue(userNotifications.all { it.userId == testUserId })
    }

    @Test
    fun getNotificationsForUser_returnsSortedByTimestampDescending() = runTest {
        repository.addNotification(sampleNotification1)
        repository.addNotification(sampleNotification2)
        val userNotifications = repository.getNotificationsForUser(testUserId)
        assertEquals(2, userNotifications.size)
        assertTrue(
            userNotifications[0].timestamp.toDate().time >=
                userNotifications[1].timestamp.toDate().time
        )
    }

    @Test
    fun getNotificationsForUser_noNotifications_returnsEmptyList() = runTest {
        val notifications = repository.getNotificationsForUser(testUserId)
        assertTrue(notifications.isEmpty())
    }

    @Test
    fun getUnreadNotificationsForUser_returnsOnlyUnreadNotifications() = runTest {
        repository.addNotification(sampleNotification1)
        repository.addNotification(sampleNotification2)
        repository.addNotification(sampleNotification3)
        val unreadNotifications = repository.getUnreadNotificationsForUser(testUserId)
        assertEquals(1, unreadNotifications.size)
        assertEquals(sampleNotification1.uid, unreadNotifications[0].uid)
        assertFalse(unreadNotifications[0].isRead)
    }

    @Test
    fun getUnreadNotificationsForUser_allRead_returnsEmptyList() = runTest {
        repository.addNotification(sampleNotification2)
        assertTrue(repository.getUnreadNotificationsForUser(testUserId).isEmpty())
    }

    @Test
    fun markAsRead_existingNotification_marksAsRead() = runTest {
        repository.addNotification(sampleNotification1)

        repository.markAsRead(sampleNotification1.uid)

        val retrieved = repository.getNotification(sampleNotification1.uid)
        assertTrue(retrieved.isRead)
    }

    @Test
    fun markAsRead_nonExistentNotification_throwsException() = runTest {
        try {
            repository.markAsRead("non-existent")
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("Cannot mark non-existent notification"))
        }
    }

    @Test
    fun markAsRead_withFailureFlag_throwsException() = runTest {
        repository.addNotification(sampleNotification1)
        repository.setShouldFailOnUpdate(true)

        try {
            repository.markAsRead(sampleNotification1.uid)
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertEquals("Simulated update failure", e.message)
        }
    }

    @Test
    fun markAllAsRead_marksAllUserNotificationsAsRead() = runTest {
        repository.addNotification(sampleNotification1)
        repository.addNotification(sampleNotification2)
        repository.addNotification(sampleNotification3)
        repository.markAllAsRead(testUserId)
        assertTrue(repository.getNotificationsForUser(testUserId).all { it.isRead })
        assertFalse(repository.getNotificationsForUser(testUserId2)[0].isRead)
    }

    @Test
    fun markAllAsRead_withFailureFlag_throwsException() = runTest {
        repository.addNotification(sampleNotification1)
        repository.setShouldFailOnUpdate(true)

        try {
            repository.markAllAsRead(testUserId)
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertEquals("Simulated update failure", e.message)
        }
    }

    @Test
    fun deleteNotification_existingNotification_deletesSuccessfully() = runTest {
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
    fun deleteNotification_nonExistentNotification_noException() = runTest {
        repository.deleteNotification("non-existent")
    }

    @Test
    fun deleteAllNotificationsForUser_deletesOnlyUserNotifications() = runTest {
        repository.addNotification(sampleNotification1)
        repository.addNotification(sampleNotification2)
        repository.addNotification(sampleNotification3)
        repository.deleteAllNotificationsForUser(testUserId)
        assertTrue(repository.getNotificationsForUser(testUserId).isEmpty())
        assertEquals(1, repository.getNotificationsForUser(testUserId2).size)
    }

    @Test
    fun operations_withDelay_canBeSet() = runTest {
        repository.setDelay(100)
        repository.addNotification(sampleNotification1)
        val retrieved = repository.getNotification(sampleNotification1.uid)
        assertEquals(sampleNotification1, retrieved)
    }

    @Test
    fun preloadNotifications_loadsNotificationsCorrectly() = runTest {
        repository.preloadNotifications(
            sampleNotification1,
            sampleNotification2,
            sampleNotification3
        )
        assertEquals(3, repository.getAddedNotifications().size)
    }

    @Test
    fun preloadNotifications_clearsExistingNotifications() = runTest {
        val extra =
            Notification("extra", testUserId, "Extra", "Extra message", NotificationType.MESSAGE)
        repository.addNotification(extra)
        repository.preloadNotifications(sampleNotification1, sampleNotification2)
        val all = repository.getAddedNotifications()
        assertEquals(2, all.size)
        assertFalse(all.any { it.uid == "extra" })
    }

    @Test
    fun clear_removesAllNotifications() = runTest {
        repository.addNotification(sampleNotification1)
        repository.addNotification(sampleNotification2)
        repository.clear()
        assertEquals(0, repository.getAddedNotifications().size)
        assertTrue(repository.getNewUid().startsWith("test-notification-0"))
    }

    @Test
    fun getNotificationById_returnsCorrectNotification() = runTest {
        repository.addNotification(sampleNotification1)
        assertEquals(sampleNotification1, repository.getNotificationById(sampleNotification1.uid))
    }

    @Test
    fun getNotificationById_nonExistent_returnsNull() = runTest {
        assertNull(repository.getNotificationById("non-existent"))
    }
}

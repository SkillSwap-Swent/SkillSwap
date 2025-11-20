/** Created with the help of Cursor */
package com.swent.skillswap.model.notification

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.skillswap.firebase.FirestorePaths.NOTIFICATIONS_COLLECTION
import com.swent.skillswap.model.utils.RepositoryException
import com.swent.skillswap.utils.FirebaseEmulator
import java.util.Date
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationRepositoryFirestoreTest {
    private lateinit var repo: NotificationRepositoryFirestore
    private lateinit var db: FirebaseFirestore
    private val userId1 = "user-1"
    private val userId2 = "user-2"

    init {
        FirebaseEmulator.startEmulator()
        db = FirebaseEmulator.firestore
        repo = NotificationRepositoryFirestore(db)
    }

    @Before
    fun setUp() = runBlocking {
        val notifications = db.collection(NOTIFICATIONS_COLLECTION).get().await()
        for (doc in notifications.documents) {
            doc.reference.delete().await()
        }
    }

    @Test
    fun addAndGet_roundTrip_success() = runTest {
        val uid = repo.getNewUid()
        val notification =
            Notification(
                uid,
                userId1,
                "Test Title",
                "Test Message",
                NotificationType.MESSAGE,
                "chat-1",
                false,
                Timestamp(Date(1000))
            )
        repo.addNotification(notification)
        val fetched = repo.getNotification(uid)
        assertEquals(notification.uid, fetched.uid)
        assertEquals(notification.userId, fetched.userId)
    }

    @Test
    fun addNotification_handlesInvalidAndDuplicate() = runTest {
        val exception1 =
            assertThrows(RepositoryException::class.java) {
                runBlocking {
                    repo.addNotification(
                        Notification("", userId1, "", "", NotificationType.MESSAGE)
                    )
                }
            }
        assertTrue(exception1.message!!.contains("Failed to add notification"))
        val uid = repo.getNewUid()
        val notification = Notification(uid, userId1, "Title", "Message", NotificationType.MESSAGE)
        repo.addNotification(notification)
        val exception2 =
            assertThrows(RepositoryException::class.java) {
                runBlocking { repo.addNotification(notification) }
            }
        assertTrue(exception2.message!!.contains("Failed to add notification"))
    }

    @Test
    fun getNotification_nonExistent_throws() = runTest {
        val exception =
            assertThrows(RepositoryException::class.java) {
                runBlocking { repo.getNotification("non-existent") }
            }
        assertTrue(exception.message!!.contains("does not exist"))
    }

    @Test
    fun getUnreadNotificationsForUser_returnsOnlyUnread() = runTest {
        repo.addNotification(
            Notification(
                repo.getNewUid(),
                userId1,
                "T1",
                "M1",
                NotificationType.MESSAGE,
                isRead = false
            )
        )
        repo.addNotification(
            Notification(
                repo.getNewUid(),
                userId1,
                "T2",
                "M2",
                NotificationType.MESSAGE,
                isRead = true
            )
        )
        assertEquals(1, repo.getUnreadNotificationsForUser(userId1).size)
    }

    @Test
    fun markAsRead_handlesExistingAndNonExistent() = runTest {
        val uid = repo.getNewUid()
        repo.addNotification(
            Notification(uid, userId1, "Title", "Message", NotificationType.MESSAGE, isRead = false)
        )
        repo.markAsRead(uid)
        assertTrue(repo.getNotification(uid).isRead)
        val exception =
            assertThrows(RepositoryException::class.java) {
                runBlocking { repo.markAsRead("non-existent") }
            }
        assertTrue(exception.message!!.contains("does not exist"))
    }

    @Test
    fun markAllAsRead_marksAllUserNotifications() = runTest {
        repo.addNotification(
            Notification(
                repo.getNewUid(),
                userId1,
                "T1",
                "M1",
                NotificationType.MESSAGE,
                isRead = false
            )
        )
        repo.addNotification(
            Notification(
                repo.getNewUid(),
                userId1,
                "T2",
                "M2",
                NotificationType.MESSAGE,
                isRead = false
            )
        )
        repo.markAllAsRead(userId1)
        assertTrue(repo.getNotificationsForUser(userId1).all { it.isRead })
    }

    @Test
    fun deleteNotification_existing_deletesSuccessfully() = runTest {
        val uid = repo.getNewUid()
        repo.addNotification(
            Notification(uid, userId1, "Title", "Message", NotificationType.MESSAGE)
        )
        repo.deleteNotification(uid)
        assertThrows(RepositoryException::class.java) { runBlocking { repo.getNotification(uid) } }
    }

    @Test
    fun deleteAllNotificationsForUser_deletesOnlyUserNotifications() = runTest {
        repo.addNotification(
            Notification(repo.getNewUid(), userId1, "T1", "M1", NotificationType.MESSAGE)
        )
        repo.addNotification(
            Notification(repo.getNewUid(), userId2, "T2", "M2", NotificationType.MESSAGE)
        )
        repo.deleteAllNotificationsForUser(userId1)
        assertTrue(repo.getNotificationsForUser(userId1).isEmpty())
    }
}

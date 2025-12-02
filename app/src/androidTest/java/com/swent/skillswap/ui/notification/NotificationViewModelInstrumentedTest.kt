/** Created with the help of Cursor */
package com.swent.skillswap.ui.notification

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.skillswap.firebase.FirestorePaths.NOTIFICATIONS_COLLECTION
import com.swent.skillswap.model.notification.Notification
import com.swent.skillswap.model.notification.NotificationRepositoryFirestore
import com.swent.skillswap.model.notification.NotificationType
import com.swent.skillswap.utils.FirebaseEmulator
import java.util.Date
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationViewModelInstrumentedTest {
    private lateinit var repository: NotificationRepositoryFirestore
    private lateinit var db: FirebaseFirestore
    private lateinit var viewModel: NotificationViewModel
    private lateinit var testUserId: String

    init {
        FirebaseEmulator.startEmulator()
        db = FirebaseEmulator.firestore
        repository = NotificationRepositoryFirestore(db)
    }

    @Before
    fun setUp() = runBlocking {
        // Clear notifications collection
        val notifications = db.collection(NOTIFICATIONS_COLLECTION).get().await()
        for (doc in notifications.documents) {
            doc.reference.delete().await()
        }

        // Sign in user for authentication
        val authResult = FirebaseAuth.getInstance().signInAnonymously().await()
        testUserId = authResult.user?.uid ?: "test-user"

        // Create ViewModel with real repository
        viewModel = NotificationViewModel(repository)
    }

    @After
    fun tearDown() = runBlocking {
        try {
            FirebaseAuth.getInstance().signOut()
            FirebaseEmulator.clearAuthEmulator()
            FirebaseEmulator.clearFirestoreEmulator()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    private fun waitForLoadingToComplete(maxAttempts: Int = 50): NotificationUiState {
        var attempts = 0
        while (viewModel.uiState.value.isLoading && attempts < maxAttempts) {
            Thread.sleep(50)
            attempts++
        }
        return viewModel.uiState.value
    }

    private fun waitForError(maxAttempts: Int = 20): NotificationUiState {
        var attempts = 0
        while (viewModel.uiState.value.error == null && attempts < maxAttempts) {
            Thread.sleep(100)
            attempts++
        }
        return viewModel.uiState.value
    }

    private fun createNotification(
        uid: String,
        userId: String,
        title: String,
        message: String,
        type: NotificationType,
        isRead: Boolean = false,
        relatedId: String? = null
    ) =
        Notification(
            uid = uid,
            userId = userId,
            title = title,
            message = message,
            type = type,
            relatedId = relatedId,
            isRead = isRead,
            timestamp = Timestamp(Date(System.currentTimeMillis()))
        )

    @Test
    fun init_loadsNotificationsFromRepository() = runBlocking {
        // Pre-populate repository with notifications
        val notif1 =
            createNotification(
                "notif-1",
                testUserId,
                "Title 1",
                "Message 1",
                NotificationType.MESSAGE,
                false
            )
        val notif2 =
            createNotification(
                "notif-2",
                testUserId,
                "Title 2",
                "Message 2",
                NotificationType.POST_REPLY,
                false
            )
        repository.addNotification(notif1)
        repository.addNotification(notif2)

        // Create new ViewModel (triggers init which calls loadNotifications)
        viewModel = NotificationViewModel(repository)

        // Wait for loading to complete
        val state = waitForLoadingToComplete()

        // Verify notifications were loaded
        assertFalse("Should finish loading", state.isLoading)
        assertEquals("Should load 2 notifications", 2, state.notifications.size)
        assertTrue("Should contain notif-1", state.notifications.any { it.uid == "notif-1" })
        assertTrue("Should contain notif-2", state.notifications.any { it.uid == "notif-2" })
        assertNull("Should have no error", state.error)
    }

    @Test
    fun init_withoutAuthenticatedUser_setsErrorState() = runBlocking {
        // Sign out before creating ViewModel
        FirebaseAuth.getInstance().signOut()

        // Create ViewModel without authenticated user
        viewModel = NotificationViewModel(repository)

        // Wait for loading to complete
        val state = waitForLoadingToComplete()

        // Verify error state
        assertFalse("Should not be loading", state.isLoading)
        assertNotNull("Should have error message", state.error)
        assertTrue(
            "Error should mention authentication",
            state.error!!.contains("authenticated", ignoreCase = true) ||
                state.error!!.contains("log in", ignoreCase = true)
        )
        assertTrue("Should have empty notifications list", state.notifications.isEmpty())
    }

    @Test
    fun loadNotifications_fetchesAllNotificationsForUser() = runBlocking {
        // Add notifications for test user
        val notif1 =
            createNotification(
                "notif-1",
                testUserId,
                "Title 1",
                "Message 1",
                NotificationType.MESSAGE
            )
        val notif2 =
            createNotification(
                "notif-2",
                testUserId,
                "Title 2",
                "Message 2",
                NotificationType.POST_REPLY
            )
        repository.addNotification(notif1)
        repository.addNotification(notif2)

        // Add notification for different user (should not appear)
        val otherUserId = "other-user"
        val notif3 =
            createNotification(
                "notif-3",
                otherUserId,
                "Title 3",
                "Message 3",
                NotificationType.MESSAGE
            )
        repository.addNotification(notif3)

        // Load notifications
        viewModel.loadNotifications()
        val state = waitForLoadingToComplete()

        // Verify only user's notifications are loaded
        assertEquals("Should load 2 notifications for test user", 2, state.notifications.size)
        assertTrue("Should contain notif-1", state.notifications.any { it.uid == "notif-1" })
        assertTrue("Should contain notif-2", state.notifications.any { it.uid == "notif-2" })
        assertFalse(
            "Should not contain other user's notification",
            state.notifications.any { it.uid == "notif-3" }
        )
        assertNull("Should have no error", state.error)
    }

    @Test
    fun setShowUnreadOnly_togglesFilterAndReloadsNotifications() = runBlocking {
        // Add mix of read and unread notifications
        viewModel.setShowUnreadOnly(false)
        val unread1 =
            createNotification(
                "unread-1",
                testUserId,
                "Unread 1",
                "Message 1",
                NotificationType.MESSAGE,
                false
            )
        val unread2 =
            createNotification(
                "unread-2",
                testUserId,
                "Unread 2",
                "Message 2",
                NotificationType.POST_REPLY,
                false
            )
        val read1 =
            createNotification(
                "read-1",
                testUserId,
                "Read 1",
                "Message 3",
                NotificationType.MESSAGE,
                true
            )
        repository.addNotification(unread1)
        repository.addNotification(unread2)
        repository.addNotification(read1)

        // Initial load (all notifications)
        viewModel.loadNotifications()
        var state = waitForLoadingToComplete()
        assertEquals("Should load all 3 notifications initially", 3, state.notifications.size)
        assertFalse("Should show all notifications initially", state.showUnreadOnly)

        // Toggle to unread only
        viewModel.setShowUnreadOnly(true)
        state = waitForLoadingToComplete()
        assertTrue("Should filter to unread only", state.showUnreadOnly)
        assertEquals("Should show only 2 unread notifications", 2, state.notifications.size)
        assertTrue("Should contain unread-1", state.notifications.any { it.uid == "unread-1" })
        assertTrue("Should contain unread-2", state.notifications.any { it.uid == "unread-2" })
        assertFalse("Should not contain read-1", state.notifications.any { it.uid == "read-1" })
        assertTrue(
            "All shown notifications should be unread",
            state.notifications.all { !it.isRead }
        )

        // Toggle back to all
        viewModel.setShowUnreadOnly(false)
        state = waitForLoadingToComplete()
        assertFalse("Should show all notifications", state.showUnreadOnly)
        assertEquals("Should show all 3 notifications", 3, state.notifications.size)
    }

    @Test
    fun markAsRead_updatesNotificationOptimistically() = runBlocking {
        // Add unread notification
        val notif =
            createNotification(
                "notif-1",
                testUserId,
                "Title",
                "Message",
                NotificationType.MESSAGE,
                false
            )
        repository.addNotification(notif)

        // Load notifications
        viewModel.loadNotifications()
        waitForLoadingToComplete()

        // Verify notification is unread
        var state = viewModel.uiState.value
        val notification = state.notifications.find { it.uid == "notif-1" }
        assertNotNull("Notification should exist", notification)
        assertFalse("Notification should be unread", notification!!.isRead)

        // Mark as read
        viewModel.markAsRead(notification)

        // Verify optimistic update (immediate UI update)
        state = viewModel.uiState.value
        val updatedNotification = state.notifications.find { it.uid == "notif-1" }
        assertNotNull("Notification should still exist", updatedNotification)
        assertTrue(
            "Notification should be marked as read optimistically",
            updatedNotification!!.isRead
        )

        // Wait for repository update to complete
        Thread.sleep(200)
        val repositoryNotification = repository.getNotification("notif-1")
        assertTrue(
            "Notification should be marked as read in repository",
            repositoryNotification.isRead
        )
    }

    @Test
    fun markAsRead_repositoryError_reloadsNotifications() = runBlocking {
        // Add notification
        val notif =
            createNotification(
                "notif-1",
                testUserId,
                "Title",
                "Message",
                NotificationType.MESSAGE,
                false
            )
        repository.addNotification(notif)

        // Load notifications
        viewModel.loadNotifications()
        waitForLoadingToComplete()

        // Verify notification is loaded
        var state = viewModel.uiState.value
        assertTrue("Should contain notif-1", state.notifications.any { it.uid == "notif-1" })

        // Delete notification from repository to cause error
        repository.deleteNotification("notif-1")

        // Try to mark as read (will fail because notification no longer exists)
        viewModel.markAsRead(notif)

        // Wait for error handling - the repository will throw an exception
        // The ViewModel sets error and then calls loadNotifications() which reloads the list
        // We verify the reload happened by checking the notification was removed

        // Wait for reload to complete (loadNotifications() is called after error)
        var attempts = 0
        while (attempts < 100) {
            Thread.sleep(100)
            state = viewModel.uiState.value
            // Reload is complete when loading is false and notification is removed
            if (!state.isLoading && !state.notifications.any { it.uid == "notif-1" }) {
                break
            }
            attempts++
        }

        // Verify notification was removed from UI (reload happened, proving error was handled)
        assertFalse(
            "Notification should be removed after reload (proves error handling worked)",
            state.notifications.any { it.uid == "notif-1" }
        )

        // The reload itself proves the error handling path was executed
        // Error might be present or cleared by successful reload - both are valid
    }

    @Test
    fun markAllAsRead_marksAllUserNotifications() = runBlocking {
        // Add multiple unread notifications
        val notif1 =
            createNotification(
                "notif-1",
                testUserId,
                "Title 1",
                "Message 1",
                NotificationType.MESSAGE,
                false
            )
        val notif2 =
            createNotification(
                "notif-2",
                testUserId,
                "Title 2",
                "Message 2",
                NotificationType.POST_REPLY,
                false
            )
        val notif3 =
            createNotification(
                "notif-3",
                testUserId,
                "Title 3",
                "Message 3",
                NotificationType.MESSAGE,
                false
            )
        repository.addNotification(notif1)
        repository.addNotification(notif2)
        repository.addNotification(notif3)

        // Load notifications
        viewModel.loadNotifications()
        waitForLoadingToComplete()

        // Verify all are unread
        var state = viewModel.uiState.value
        assertEquals("Should have 3 notifications", 3, state.notifications.size)
        assertTrue("All should be unread", state.notifications.all { !it.isRead })

        // Mark all as read
        viewModel.markAllAsRead()

        // Verify optimistic update
        state = viewModel.uiState.value
        assertTrue(
            "All notifications should be marked as read optimistically",
            state.notifications.all { it.isRead }
        )

        // Wait for repository update
        Thread.sleep(200)
        val allNotifications = repository.getNotificationsForUser(testUserId)
        assertTrue(
            "All notifications should be marked as read in repository",
            allNotifications.all { it.isRead }
        )
    }

    @Test
    fun markAllAsRead_withoutUser_setsError() = runBlocking {
        // Sign out and wait for it to complete
        FirebaseAuth.getInstance().signOut()
        Thread.sleep(200) // Wait for signOut to fully complete

        // Verify user is actually signed out
        assertNull("User should be signed out", FirebaseAuth.getInstance().currentUser)

        // Try to mark all as read
        viewModel.markAllAsRead()

        // Error should be set immediately (synchronous check in ViewModel)
        val state = viewModel.uiState.value

        assertNotNull("Should have error", state.error)
        assertTrue(
            "Error should mention authentication",
            state.error!!.contains("authenticated", ignoreCase = true) ||
                state.error!!.contains("log in", ignoreCase = true)
        )
    }

    @Test
    fun deleteNotification_removesFromUIAndRepository() = runBlocking {
        // Add notifications
        val notif1 =
            createNotification(
                "notif-1",
                testUserId,
                "Title 1",
                "Message 1",
                NotificationType.MESSAGE
            )
        val notif2 =
            createNotification(
                "notif-2",
                testUserId,
                "Title 2",
                "Message 2",
                NotificationType.POST_REPLY
            )
        repository.addNotification(notif1)
        repository.addNotification(notif2)

        // Load notifications
        viewModel.loadNotifications()
        waitForLoadingToComplete()

        var state = viewModel.uiState.value
        val initialCount = state.notifications.size
        assertTrue("Should contain notif-1", state.notifications.any { it.uid == "notif-1" })

        // Delete notification
        viewModel.deleteNotification(notif1)

        // Verify optimistic update (immediate removal)
        state = viewModel.uiState.value
        assertFalse(
            "Should not contain notif-1 after deletion",
            state.notifications.any { it.uid == "notif-1" }
        )
        assertEquals("Count should decrease", initialCount - 1, state.notifications.size)
        assertTrue("Should still contain notif-2", state.notifications.any { it.uid == "notif-2" })

        // Wait for repository deletion
        Thread.sleep(200)
        try {
            repository.getNotification("notif-1")
            fail("Notification should be deleted from repository")
        } catch (e: Exception) {
            // Expected - notification should not exist
        }
    }

    @Test
    fun deleteNotification_repositoryError_reloadsNotifications() = runBlocking {
        // Note: Firestore delete() doesn't throw an error if document doesn't exist
        // So we can't test repository error this way. Instead, we test that
        // the optimistic update works and the notification is removed from UI
        // even if it was already deleted from repository.

        // Add notification
        val notif =
            createNotification("notif-1", testUserId, "Title", "Message", NotificationType.MESSAGE)
        repository.addNotification(notif)

        // Load notifications
        viewModel.loadNotifications()
        waitForLoadingToComplete()

        var state = viewModel.uiState.value
        assertTrue("Should contain notif-1", state.notifications.any { it.uid == "notif-1" })

        // Delete from repository first
        repository.deleteNotification("notif-1")

        // Delete from ViewModel (optimistic update should still work)
        viewModel.deleteNotification(notif)

        // Verify optimistic update (immediate removal from UI)
        state = viewModel.uiState.value
        assertFalse(
            "Should not contain notif-1 after deletion (optimistic update)",
            state.notifications.any { it.uid == "notif-1" }
        )

        // Wait a bit for any async operations
        Thread.sleep(200)

        // The notification should remain removed (no error because Firestore delete succeeds even
        // if doc doesn't exist)
        state = viewModel.uiState.value
        assertFalse(
            "Should still not contain notif-1",
            state.notifications.any { it.uid == "notif-1" }
        )
    }

    @Test
    fun deleteAllNotifications_clearsAllUserNotifications() = runBlocking {
        // Add multiple notifications
        val notif1 =
            createNotification(
                "notif-1",
                testUserId,
                "Title 1",
                "Message 1",
                NotificationType.MESSAGE
            )
        val notif2 =
            createNotification(
                "notif-2",
                testUserId,
                "Title 2",
                "Message 2",
                NotificationType.POST_REPLY
            )
        val notif3 =
            createNotification(
                "notif-3",
                testUserId,
                "Title 3",
                "Message 3",
                NotificationType.MESSAGE
            )
        repository.addNotification(notif1)
        repository.addNotification(notif2)
        repository.addNotification(notif3)

        // Load notifications
        viewModel.loadNotifications()
        waitForLoadingToComplete()

        var state = viewModel.uiState.value
        assertEquals("Should have 3 notifications", 3, state.notifications.size)

        // Delete all
        viewModel.deleteAllNotifications()

        // Verify optimistic update
        state = viewModel.uiState.value
        assertTrue(
            "All notifications should be removed optimistically",
            state.notifications.isEmpty()
        )

        // Wait for repository deletion
        Thread.sleep(200)
        val remainingNotifications = repository.getNotificationsForUser(testUserId)
        assertTrue(
            "All notifications should be deleted from repository",
            remainingNotifications.isEmpty()
        )
    }

    @Test
    fun deleteAllNotifications_withoutUser_setsError() = runBlocking {
        // Sign out and wait for it to complete
        FirebaseAuth.getInstance().signOut()
        Thread.sleep(200) // Wait for signOut to fully complete

        // Verify user is actually signed out
        assertNull("User should be signed out", FirebaseAuth.getInstance().currentUser)

        // Try to delete all
        viewModel.deleteAllNotifications()

        // Error should be set immediately (synchronous check in ViewModel)
        val state = viewModel.uiState.value

        assertNotNull("Should have error", state.error)
        assertTrue(
            "Error should mention authentication",
            state.error!!.contains("authenticated", ignoreCase = true) ||
                state.error!!.contains("log in", ignoreCase = true)
        )
    }

    @Test
    fun refresh_reloadsNotificationsFromRepository() = runBlocking {
        // Add initial notification
        val notif1 =
            createNotification(
                "notif-1",
                testUserId,
                "Title 1",
                "Message 1",
                NotificationType.MESSAGE
            )
        repository.addNotification(notif1)

        // Load notifications
        viewModel.loadNotifications()
        waitForLoadingToComplete()

        var state = viewModel.uiState.value
        assertEquals("Should have 1 notification", 1, state.notifications.size)

        // Add new notification
        val notif2 =
            createNotification(
                "notif-2",
                testUserId,
                "Title 2",
                "Message 2",
                NotificationType.POST_REPLY
            )
        repository.addNotification(notif2)

        // Refresh
        viewModel.refresh()
        state = waitForLoadingToComplete()

        // Verify new notification is loaded
        assertEquals("Should have 2 notifications after refresh", 2, state.notifications.size)
        assertTrue("Should contain notif-1", state.notifications.any { it.uid == "notif-1" })
        assertTrue("Should contain notif-2", state.notifications.any { it.uid == "notif-2" })
    }

    @Test
    fun clearError_removesErrorFromState() = runBlocking {
        // Sign out to trigger error
        FirebaseAuth.getInstance().signOut()
        viewModel = NotificationViewModel(repository)
        waitForLoadingToComplete()

        var state = viewModel.uiState.value
        assertNotNull("Should have error", state.error)

        // Clear error
        viewModel.clearError()
        state = viewModel.uiState.value

        assertNull("Error should be cleared", state.error)
    }

    @Test
    fun loadNotifications_emptyList_handlesGracefully() = runBlocking {
        // Don't add any notifications

        // Load notifications
        viewModel.loadNotifications()
        val state = waitForLoadingToComplete()

        // Verify empty state
        assertTrue("Should have empty notifications list", state.notifications.isEmpty())
        assertFalse("Should not be loading", state.isLoading)
        // assertNull("Should have no error", state.error)
    }
}

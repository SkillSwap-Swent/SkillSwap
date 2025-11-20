/** Created with the help of Cursor */
package com.swent.skillswap.model.notification

import kotlinx.coroutines.delay

/**
 * In-memory NotificationRepository implementation for testing. Provides deterministic behavior and
 * allows testing success/failure cases without actual database interactions.
 */
class FakeNotificationRepository : NotificationRepository {
    private val notifications = mutableMapOf<String, Notification>()
    private var uidCounter = 0
    private var shouldFailOnAdd = false
    private var shouldFailOnGet = false
    private var shouldFailOnUpdate = false
    private var delayMillis = 0L

    // Preload notifications for deterministic testing
    fun preloadNotifications(vararg notificationsToPreload: Notification) {
        notifications.clear()
        notificationsToPreload.forEach { notifications[it.uid] = it }
    }

    // Simulate failures for error testing
    fun setShouldFailOnAdd(fail: Boolean) {
        shouldFailOnAdd = fail
    }

    fun setShouldFailOnGet(fail: Boolean) {
        shouldFailOnGet = fail
    }

    fun setShouldFailOnUpdate(fail: Boolean) {
        shouldFailOnUpdate = fail
    }

    // Convenience method for setting all failure flags
    fun setShouldFail(fail: Boolean) {
        shouldFailOnAdd = fail
        shouldFailOnGet = fail
        shouldFailOnUpdate = fail
    }

    // Set delay for async operations (to test loading states)
    fun setDelay(delayMs: Long) {
        delayMillis = delayMs
    }

    override fun getNewUid(): String {
        return "test-notification-${uidCounter++}"
    }

    override suspend fun getNotificationsForUser(userId: String): List<Notification> {
        if (delayMillis > 0) {
            delay(delayMillis)
        }
        if (shouldFailOnGet) {
            throw Exception("Simulated get failure")
        }
        return notifications.values
            .filter { it.userId == userId }
            .sortedByDescending { it.timestamp }
    }

    override suspend fun getUnreadNotificationsForUser(userId: String): List<Notification> {
        if (delayMillis > 0) {
            delay(delayMillis)
        }
        if (shouldFailOnGet) {
            throw Exception("Simulated get failure")
        }
        return notifications.values
            .filter { it.userId == userId && !it.isRead }
            .sortedByDescending { it.timestamp }
    }

    override suspend fun getNotification(notificationId: String): Notification {
        if (delayMillis > 0) {
            delay(delayMillis)
        }
        if (shouldFailOnGet) {
            throw Exception("Simulated get failure")
        }
        return notifications[notificationId]
            ?: throw Exception("Notification not found: $notificationId")
    }

    override suspend fun addNotification(notification: Notification) {
        if (delayMillis > 0) {
            delay(delayMillis)
        }
        if (shouldFailOnAdd) {
            throw Exception("Simulated add failure")
        }
        notifications[notification.uid] = notification
    }

    override suspend fun markAsRead(notificationId: String) {
        if (delayMillis > 0) {
            delay(delayMillis)
        }
        if (shouldFailOnUpdate) {
            throw Exception("Simulated update failure")
        }
        val notification =
            notifications[notificationId]
                ?: throw Exception("Cannot mark non-existent notification as read: $notificationId")
        notifications[notificationId] = notification.copy(isRead = true)
    }

    override suspend fun markAllAsRead(userId: String) {
        if (delayMillis > 0) {
            delay(delayMillis)
        }
        if (shouldFailOnUpdate) {
            throw Exception("Simulated update failure")
        }
        notifications.forEach { (id, notification) ->
            if (notification.userId == userId && !notification.isRead) {
                notifications[id] = notification.copy(isRead = true)
            }
        }
    }

    override suspend fun deleteNotification(notificationId: String) {
        notifications.remove(notificationId)
    }

    override suspend fun deleteAllNotificationsForUser(userId: String) {
        notifications.entries.removeAll { it.value.userId == userId }
    }

    // Test helpers
    fun getAddedNotifications(): List<Notification> = notifications.values.toList()

    fun getNotificationById(id: String): Notification? = notifications[id]

    fun clear() {
        notifications.clear()
        uidCounter = 0
    }
}

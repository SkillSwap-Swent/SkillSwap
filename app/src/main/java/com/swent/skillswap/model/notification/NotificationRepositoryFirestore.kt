/** Created with the help of Cursor */
package com.swent.skillswap.model.notification

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.skillswap.firebase.FirestorePaths
import com.swent.skillswap.model.utils.RepositoryException
import kotlinx.coroutines.tasks.await

class NotificationRepositoryFirestore(private val db: FirebaseFirestore) : NotificationRepository {

    private val notificationsCollection = db.collection(FirestorePaths.NOTIFICATIONS_COLLECTION)

    override fun getNewUid(): String {
        return notificationsCollection.document().id
    }

    override suspend fun getNotificationsForUser(userId: String): List<Notification> {
        return try {
            notificationsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .map { documentToNotification(it) }
                .sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            throw RepositoryException("Failed to get notifications for user $userId", e)
        }
    }

    override suspend fun getUnreadNotificationsForUser(userId: String): List<Notification> {
        return try {
            notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
                .map { documentToNotification(it) }
                .sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            throw RepositoryException("Failed to get unread notifications for user $userId", e)
        }
    }

    override suspend fun getNotification(notificationId: String): Notification {
        return try {
            val document = notificationsCollection.document(notificationId).get().await()
            if (!document.exists()) {
                throw RepositoryException("Notification $notificationId does not exist", null)
            }
            documentToNotification(document)
        } catch (e: Exception) {
            if (e is RepositoryException) throw e
            throw RepositoryException("Failed to get notification $notificationId", e)
        }
    }

    override suspend fun addNotification(notification: Notification) {
        try {
            require(notification.validate()) { "Notification fields are invalid" }
            val docRef = notificationsCollection.document(notification.uid)
            val snapshot = docRef.get().await()

            // Ensure addNotification isn't being used to overwrite
            require(!snapshot.exists()) {
                "Notification with UID ${notification.uid} already exists"
            }
            docRef.set(serializeNotification(notification)).await()
        } catch (e: Exception) {
            throw RepositoryException("Failed to add notification ${notification.uid}", e)
        }
    }

    override suspend fun markAsRead(notificationId: String) {
        try {
            val docRef = notificationsCollection.document(notificationId)
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                throw RepositoryException("Notification $notificationId does not exist", null)
            }
            docRef.update("isRead", true).await()
        } catch (e: Exception) {
            if (e is RepositoryException) throw e
            throw RepositoryException("Failed to mark notification $notificationId as read", e)
        }
    }

    override suspend fun markAllAsRead(userId: String) {
        try {
            val notifications =
                notificationsCollection
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isRead", false)
                    .get()
                    .await()

            notifications.forEach { notification ->
                notification.reference.update("isRead", true).await()
            }
        } catch (e: Exception) {
            throw RepositoryException(
                "Failed to mark all notifications as read for user $userId",
                e
            )
        }
    }

    override suspend fun markChatNotificationsAsRead(chatId: String, userId: String) {
        try {
            // Fetch unread notifications for user
            val unreadNotifications = getUnreadNotificationsForUser(userId)
            // Filter notifications with relatedId == chatId
            val toMark = unreadNotifications.filter { it.relatedId == chatId }

            if (toMark.isEmpty()) return

            for (notification in toMark) {
                markAsRead(notification.uid)
            }
        } catch (e: Exception) {
            throw RepositoryException(
                "Failed to mark chat notifications as read for chatId $chatId and user $userId",
                e
            )
        }
    }

    override suspend fun markPostNotificationsAsRead(postId: String, userId: String) {
        try {
            // Fetch unread notifications for user
            val unreadNotifications = getUnreadNotificationsForUser(userId)
            // Filter post-related notifications with relatedId == postId
            val postNotificationTypes =
                listOf(
                    com.swent.skillswap.model.notification.NotificationType.POST_REPLY,
                    com.swent.skillswap.model.notification.NotificationType.POST_ACCEPTED,
                    com.swent.skillswap.model.notification.NotificationType.POST_REJECTED,
                    com.swent.skillswap.model.notification.NotificationType.NEW_MATCHING_POST
                )
            val toMark =
                unreadNotifications.filter {
                    it.relatedId == postId && it.type in postNotificationTypes
                }

            if (toMark.isEmpty()) return

            for (notification in toMark) {
                markAsRead(notification.uid)
            }
        } catch (e: Exception) {
            throw RepositoryException(
                "Failed to mark post notifications as read for postId $postId and user $userId",
                e
            )
        }
    }

    override suspend fun deleteNotification(notificationId: String) {
        try {
            notificationsCollection.document(notificationId).delete().await()
        } catch (e: Exception) {
            throw RepositoryException("Failed to delete notification $notificationId", e)
        }
    }

    override suspend fun deleteAllNotificationsForUser(userId: String) {
        try {
            val notifications = notificationsCollection.whereEqualTo("userId", userId).get().await()

            notifications.forEach { notification -> notification.reference.delete().await() }
        } catch (e: Exception) {
            throw RepositoryException("Failed to delete all notifications for user $userId", e)
        }
    }

    private fun documentToNotification(document: DocumentSnapshot): Notification {
        val uid = document.id
        val userId = requireField("userId", document.getString("userId"))
        val title = requireField("title", document.getString("title"))
        val message = requireField("message", document.getString("message"))
        val type = safeEnum<NotificationType>(requireField("type", document.getString("type")))
        val relatedId = document.getString("relatedId")
        val isRead = document.getBoolean("isRead") ?: false
        val timestamp = requireField("timestamp", document.getTimestamp("timestamp"))

        return Notification(
            uid = uid,
            userId = userId,
            title = title,
            message = message,
            type = type,
            relatedId = relatedId,
            isRead = isRead,
            timestamp = timestamp
        )
    }

    private fun serializeNotification(notification: Notification): Map<String, Any> {
        return mapOf(
            "userId" to notification.userId,
            "title" to notification.title,
            "message" to notification.message,
            "type" to notification.type.name,
            "relatedId" to (notification.relatedId ?: ""),
            "isRead" to notification.isRead,
            "timestamp" to notification.timestamp
        )
    }

    private fun <T> requireField(name: String, value: T?): T =
        value ?: throw IllegalArgumentException("Missing or invalid field: $name")

    private inline fun <reified T : Enum<T>> safeEnum(raw: String?): T {
        val value = raw ?: throw IllegalArgumentException("Missing ${T::class.simpleName} value")
        try {
            return enumValueOf(value)
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid ${T::class.simpleName} value: $value")
        }
    }
}

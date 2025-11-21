/** Created with the help of Cursor */
package com.swent.skillswap.model.notification

import com.google.firebase.Timestamp

/**
 * Represents a notification in the SkillSwap application.
 *
 * @property uid Unique identifier for the notification
 * @property userId The ID of the user who should receive this notification
 * @property title The title of the notification
 * @property message The message content of the notification
 * @property type The type of notification (e.g., MESSAGE, POST_REPLY, POST_ACCEPTED)
 * @property relatedId Optional ID of related entity (e.g., chatId, postId)
 * @property isRead Whether the notification has been read by the user
 * @property timestamp When the notification was created
 */
data class Notification(
    val uid: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val relatedId: String? = null,
    val isRead: Boolean = false,
    val timestamp: Timestamp = Timestamp.now()
) {
    /**
     * Validates that the notification has all required fields.
     *
     * @return `true` if the notification is valid, `false` otherwise.
     */
    fun validate(): Boolean {
        return uid.isNotBlank() && userId.isNotBlank() && title.isNotBlank() && message.isNotBlank()
    }
}

/** Enum representing the different types of notifications. */
enum class NotificationType {
    /** A new message was received in a chat */
    MESSAGE,
    /** A new reply was added to a post */
    POST_REPLY,
    /** A post reply was accepted */
    POST_ACCEPTED,
    /** A post reply was rejected */
    POST_REJECTED,
    /** A new post matching user's skills was created */
    NEW_MATCHING_POST
}

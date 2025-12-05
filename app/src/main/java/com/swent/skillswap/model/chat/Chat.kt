package com.swent.skillswap.model.chat

import android.annotation.SuppressLint
import com.swent.skillswap.model.post.PostType
import kotlinx.serialization.Serializable

/**
 * Data class representing a chat message.
 *
 * @property id The unique identifier of the message
 * @property senderId The ID of the user who sent the message
 * @property content The content of the message
 * @property timestamp The time the message was sent, represented as a Unix timestamp
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
class Message(val id: String, val senderId: String, val content: String, val timestamp: Long)

/**
 * Data class representing a chat.
 *
 * @property id The unique identifier of the chat
 * @property participants The list of user IDs participating in the chat
 * @property relatedPostId The ID of the post related to the chat
 * @property relatedPostType The type of the post related to the chat (OFFER or REQUEST)
 * @property messages The list of messages in the chat
 * @property status The current status of the chat (ACTIVE or INACTIVE), default ot ACTIVE
 */
class Chat(
    val id: String,
    val participants: List<String>,
    val relatedPostId: String,
    val relatedPostType: PostType,
    val messages: List<Message>,
    val status: ChatStatus = ChatStatus.ACTIVE
) {
    /**
     * Checks if the chat is active.
     *
     * @return True if the chat status is ACTIVE, false otherwise.
     */
    fun isActive(): Boolean {
        return status == ChatStatus.ACTIVE
    }
}

const val NUMBER_OF_CHAT_PARTICIPANTS = 2

enum class ChatStatus {
    ACTIVE,
    INACTIVE
}

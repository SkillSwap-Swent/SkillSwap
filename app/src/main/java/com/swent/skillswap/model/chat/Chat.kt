package com.swent.skillswap.model.chat

/**
 * Data class representing a chat message.
 *
 * @property id The unique identifier of the message
 * @property senderId The ID of the user who sent the message
 * @property content The content of the message
 * @property timestamp The time the message was sent, represented as a Unix timestamp
 */
class Message(val id: String, val senderId: String, val content: String, val timestamp: Long)
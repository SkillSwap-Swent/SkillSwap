package com.swent.skillswap.model.utils

import com.swent.skillswap.model.chat.Message
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Utility functions for serializing and deserializing chat messages. */
fun serializeMessage(message: Message): String {
    /** Preconditions */
    require(message.id.isNotEmpty()) { "id cannot be empty" }
    require(message.senderId.isNotEmpty()) { "senderId cannot be empty" }
    require(message.content.isNotEmpty()) { "content cannot be empty" }
    require(message.timestamp > 0) { "timestamp must be positive" }
    return Json.encodeToString(message)
}

fun deserializeMessage(data: String): Message {
    try {
        return Json.decodeFromString<Message>(data)
    } catch (e: Exception) {
        throw Exception("Error deserializing message: ${e.message}")
    }
}

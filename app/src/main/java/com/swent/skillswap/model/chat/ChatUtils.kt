package com.swent.skillswap.model.chat

/** Utility functions for serializing and deserializing chat messages. */
fun serializeMessage(message: Message): String {
    return "${message.id}|${message.senderId}|${message.content}|${message.timestamp}"
}

fun deserializeMessage(data: String): Message {
    return data.split("|").let {
        Message(id = it[0], senderId = it[1], content = it[2], timestamp = it[3].toLong())
    }
}

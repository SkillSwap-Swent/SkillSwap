package com.swent.skillswap.model.chat

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chat operations. Provides methods for streaming messages and sending
 * messages
 */
interface ChatRepository {

    /**
     * Streams messages in a chat with real-time updates.
     *
     * @param chatId The ID of the chat to observe
     * @return Flow emitting updated list of messages
     */
    fun streamMessages(chatId: String): Flow<List<Message>>

    /**
     * Sends a message in a chat.
     *
     * @param chatId The chat ID
     * @param senderId The sender's user ID
     * @param content The message content
     */
    suspend fun sendMessage(chatId: String, message: Message)
}

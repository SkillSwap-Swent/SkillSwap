package com.swent.skillswap.model.chat

import com.swent.skillswap.model.post.PostType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chat operations. Provides methods for streaming messages and sending
 * messages
 */
interface ChatRepository {

    /**
     * Creates a new chat between two users.
     *
     * @param participants The list of user IDs participating in the chat
     * @param relatedPostId The ID of the post related to the chat
     * @param relatedPostType The type of the post related to the chat (OFFER or REQUEST)
     * @return The ID of the newly created chat
     * @throws Exception will throw db exceptions
     */
    suspend fun createChat(
        participants: List<String>,
        relatedPostId: String,
        relatedPostType: PostType
    ): String

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
    suspend fun sendMessage(chatId: String, senderId: String, content: String)

    /**
     * Gets the list of chats for the current user filtered by post type.
     *
     * @param relatedPostType The type of post linked to the chats to be retrieved
     */
    suspend fun getChatsOfCurrentUser(relatedPostType: PostType): List<Chat>

    /**
     * Gets a chat by its ID.
     *
     * @param chatId The ID of the chat to retrieve
     * @return The Chat object
     */
    suspend fun getChat(chatId: String): Chat
}

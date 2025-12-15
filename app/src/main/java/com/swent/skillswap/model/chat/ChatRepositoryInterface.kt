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
     * Gets the list of chats that are not pending for the current user filtered by post type.
     *
     * @param relatedPostType The type of post linked to the chats to be retrieved
     */
    suspend fun getChatsOfCurrentUser(relatedPostType: PostType): List<Chat>
    /**
     * Gets the list of pending chats for the current user filtered by post type.
     *
     * @param relatedPostType The type of post linked to the chats to be retrieved
     */
    suspend fun getPendingChatsOfCurrentUser(relatedPostType: PostType): List<Chat>
    /**
     * @param chat the chat from which we want to get the owner of the related post
     * @return the owner id of the related post to the chat in the arg
     */
    suspend fun isOwnerOfRelatedPost(chat: Chat): Boolean
    /**
     * do all the necessary to accept a chat for a post and remove the other ones
     *
     * @param chat the chat that we want to accept for the post
     */
    /**
     * Gets a chat by its ID.
     *
     * @param chatId The ID of the chat to retrieve
     * @return The Chat object
     */
    suspend fun getChat(chatId: String): Chat
    /**
     * Accept a chat as the only chat who will be use for the exchange of knowledge discard the
     * other and mark the post of the chat has completed
     *
     * @param chat the chat that the user accept
     */
    suspend fun acceptAPostReplyChat(chat: Chat)
    /**
     * close the chat with the given id (change chatStatus to INACTIVE)
     *
     * @param chatId the id of the chat that need to be closed
     */
    suspend fun closeChat(chatId: String)
}

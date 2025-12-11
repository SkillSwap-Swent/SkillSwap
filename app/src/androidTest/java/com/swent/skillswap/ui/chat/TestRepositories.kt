package com.swent.skillswap.ui.chat

import com.swent.skillswap.model.chat.Chat
import com.swent.skillswap.model.chat.ChatRepository
import com.swent.skillswap.model.chat.Message
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepositery
import kotlinx.coroutines.flow.flowOf

class FailingChatRepository : ChatRepository {
    override suspend fun createChat(
        participants: List<String>,
        relatedPostId: String,
        relatedPostType: PostType
    ) = ""

    override fun streamMessages(chatId: String) = flowOf(emptyList<Message>())

    override suspend fun sendMessage(chatId: String, senderId: String, content: String) {}

    override suspend fun getChatsOfCurrentUser(relatedPostType: PostType): List<Chat> =
        throw Exception("Network error")

    override suspend fun getPendingChatsOfCurrentUser(relatedPostType: PostType): List<Chat> =
        throw Exception("Network error")

    override suspend fun isOwnerOfRelatedPost(chat: Chat) = false

    override suspend fun acceptAPostReplyChat(chat: Chat) {}

    override suspend fun getChat(chatId: String): Chat = throw Exception("Not found")
}

class FailingUserRepository : UserRepositery {
    override fun getNewUid() = ""

    override suspend fun getUser(userID: String): User = throw Exception("User not found")

    override suspend fun addUser(user: User) {}

    override suspend fun editUser(userID: String, newValue: User) {}

    override suspend fun deleteUser(userID: String) {}

    override suspend fun userExists(userId: String) = false

    override suspend fun updateFcmToken(userId: String, fcmToken: String) {}

    override suspend fun updateRating(userId: String, incomingRating: Float) {}
}

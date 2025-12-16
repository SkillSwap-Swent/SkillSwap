package com.swent.skillswap.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.swent.skillswap.model.chat.Chat
import com.swent.skillswap.model.chat.ChatRepository
import com.swent.skillswap.model.chat.ChatStatus
import com.swent.skillswap.model.notification.NotificationType
import com.swent.skillswap.model.post.PostRepository
import com.swent.skillswap.model.post.PostStatus
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.user.UserRepositery
import com.swent.skillswap.ui.notification.NotificationViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the chat list screen.
 *
 * @property chats Active chats for the current user.
 * @property usernames User ID to display name mapping.
 * @property postTitles Post ID to title mapping.
 * @property avatars User ID to profile picture URL mapping.
 * @property isLoading Whether data is being loaded.
 * @property error Error message from the last failed operation, or null.
 * @property associatedPostStatuses Post ID to status mapping.
 */
data class ChatListUIState(
    val chats: List<Chat> = emptyList(),
    val usernames: Map<String, String> = emptyMap(),
    val postTitles: Map<String, String> = emptyMap(),
    val avatars: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val associatedPostStatuses: Map<String, PostStatus> = emptyMap()
)

/**
 * ViewModel for the chat list screen.
 *
 * Coordinates chat, user, and post repositories to display conversations and manage rating
 * functionality for the current user.
 */
class ChatListViewModel(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepositery,
    private val postRepository: PostRepository,
    private val notificationViewModel: NotificationViewModel? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUIState())
    val uiState: StateFlow<ChatListUIState> = _uiState.asStateFlow()

    /**
     * Get chats of current user filtered by related post type and update the ui state accordingly
     *
     * @param relatedPostType post type of the related post to the chats we want to fetch
     * @param pending OPTIONAL PARAM DEFAULT FALSE say if we want to see pending chat (not
     * @param isOwner OPTIONAL PARAM DEFAULT NULL specify if we want to filter chat base on if the
     *   user using the screen is the owner of them or no
     */
    fun getChatsOfCurrentUser(
        relatedPostType: PostType,
        pending: Boolean = false,
        isOwner: Boolean? = null
    ) {
        viewModelScope.launch {
            val chats =
                try {
                    if (!pending) {
                        chatRepository.getChatsOfCurrentUser(relatedPostType)
                    } else {
                        chatRepository.getPendingChatsOfCurrentUser(relatedPostType)
                    }
                } catch (exception: Exception) {
                    Log.e("ChatViewModel", "Error fetching chats", exception)
                    _uiState.update { it.copy(error = "Error fetching chats") }
                    return@launch
                }
            val filteredIsOwnerChats =
                try {
                    chats.filter {
                        if (isOwner == null) {
                            true
                        } else {
                            chatRepository.isOwnerOfRelatedPost(it) == isOwner
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error filtering chats by ownership", e)
                    chats
                }
            val fullFilteredChats = filteredIsOwnerChats.filter { it.status == ChatStatus.ACTIVE }
            _uiState.update { it.copy(chats = fullFilteredChats) }
            chats.forEach { chat ->
                try {
                    val postStatus =
                        postRepository.getPost(chat.relatedPostType, chat.relatedPostId).status
                    _uiState.update {
                        it.copy(
                            associatedPostStatuses =
                                it.associatedPostStatuses + (chat.relatedPostId to postStatus)
                        )
                    }
                } catch (exception: Exception) {
                    Log.e(
                        "ChatListViewModel",
                        "Error fetching post status for postId: ${chat.relatedPostId}",
                        exception
                    )
                }
            }
        }
    }

    /** Accepts a pending chat request, activating the conversation. */
    fun acceptAPostReplyChat(chat: Chat) {
        viewModelScope.launch {
            try {
                chatRepository.acceptAPostReplyChat(chat)
                createPostAcceptedNotification(chat)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error accepting chat", e)
            }
        }
    }

    /**
     * Creates a POST_ACCEPTED notification for the user whose reply was accepted.
     *
     * @param chat The chat where a reply was accepted
     */
    private suspend fun createPostAcceptedNotification(chat: Chat) {
        notificationViewModel?.let { vm ->
            try {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId == null) {
                    Log.w("ChatListViewModel", "Cannot create notification: no authenticated user")
                    return
                }

                // Get the other participant (the one whose reply was accepted)
                val acceptedUserId = chat.participants.firstOrNull { it != currentUserId }
                if (acceptedUserId == null) {
                    Log.w("ChatListViewModel", "Cannot create notification: no accepted user found")
                    return
                }

                // Get post title for the notification message
                val postTitle =
                    try {
                        postRepository.getPost(chat.relatedPostType, chat.relatedPostId).title
                    } catch (e: Exception) {
                        Log.e(
                            "ChatListViewModel",
                            "Error fetching post title for notification, using default",
                            e
                        )
                        "your post"
                    }

                vm.addNotification(
                    recipientId = acceptedUserId,
                    message = "Your reply to \"$postTitle\" has been accepted!",
                    type = NotificationType.POST_ACCEPTED,
                    relatedId = chat.relatedPostId
                )
            } catch (e: Exception) {
                Log.e("ChatListViewModel", "Error creating POST_ACCEPTED notification", e)
            }
        }
    }

    /** Fetches and caches the username and avatar for a user into [uiState]. */
    fun getUsernameAndAvatar(userId: String) {
        viewModelScope.launch {
            val user =
                try {
                    userRepository.getUser(userId)
                } catch (exception: Exception) {
                    Log.e(
                        "ChatViewModel",
                        "Error fetching username and avatar of user with Id: $userId"
                    )
                    _uiState.update { it.copy(error = "Error loading username and avatar") }
                    return@launch
                }
            val username = user.username
            val avatar = user.profilePicture
            _uiState.update {
                it.copy(
                    usernames = it.usernames + (userId to username),
                    avatars = it.avatars + (userId to avatar)
                )
            }
        }
    }

    /** Fetches and caches the post title into [uiState]. */
    fun getPostTitle(postId: String, postType: PostType) {
        viewModelScope.launch {
            val title =
                try {
                    postRepository.getPost(postType, postId).title
                } catch (exception: Exception) {
                    Log.e("ChatViewModel", "Error fetching post title for post with id: $postId")
                    _uiState.update { it.copy(error = "Error loading post title") }
                    return@launch
                }
            _uiState.update { it.copy(postTitles = it.postTitles + (postId to title)) }
        }
    }

    /** Returns true if rating button should show (chat active, post completed/archived). */
    fun shouldDisplayRatingButton(chat: Chat): Boolean {
        val postStatus = uiState.value.associatedPostStatuses[chat.relatedPostId] ?: return false
        return chat.isActive() &&
            (postStatus == PostStatus.COMPLETED || postStatus == PostStatus.ARCHIVED)
    }

    /** Submits a rating for another user and closes the chat. */
    fun updateUserRating(userId: String, incomingRating: Float, chatId: String) {
        viewModelScope.launch {
            try {
                userRepository.updateRating(userId, incomingRating)
                chatRepository.closeChat(chatId)
            } catch (exception: Exception) {
                Log.e("ChatListViewModel", "Error updating rating for user $userId", exception)
            }
        }
    }
}

/** Factory for creating [ChatListViewModel] instances with required dependencies. */
class ChatListViewModelFactory(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepositery,
    private val postRepository: PostRepository,
    private val notificationViewModel: NotificationViewModel? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatListViewModel(
            chatRepository,
            userRepository,
            postRepository,
            notificationViewModel
        )
            as T
    }
}

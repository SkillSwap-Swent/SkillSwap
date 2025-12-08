package com.swent.skillswap.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swent.skillswap.model.chat.Chat
import com.swent.skillswap.model.chat.ChatRepository
import com.swent.skillswap.model.chat.ChatStatus
import com.swent.skillswap.model.post.PostRepository
import com.swent.skillswap.model.post.PostStatus
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.user.UserRepositery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatListUIState(
    val chats: List<Chat> = emptyList(),
    val usernames: Map<String, String> = emptyMap(),
    val postTitles: Map<String, String> = emptyMap(),
    val avatars: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val associatedPostStatuses: Map<String, PostStatus> = emptyMap()
)

// ViewModel for Chat List Screen
// Uses chatRepository to fetch chats
// Uses userRepository to fetch user details
// Uses postRepository to fetch post details
class ChatListViewModel(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepositery,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUIState())
    val uiState: StateFlow<ChatListUIState> = _uiState.asStateFlow()

    // Get chats of current user filtered by related post type, pre-fetches and caches post statuses
    fun getChatsOfCurrentUser(relatedPostType: PostType) {
        viewModelScope.launch {
            val chats =
                try {
                    chatRepository.getChatsOfCurrentUser(relatedPostType).filter {
                        it.status == ChatStatus.ACTIVE
                    }
                } catch (exception: Exception) {
                    emptyList()
                }
            _uiState.update { it.copy(chats = chats) }
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
                    ""
                }
            }
        }
    }

    // Get username by user ID
    fun getUsername(userId: String) {
        viewModelScope.launch {
            val username =
                try {
                    userRepository.getUser(userId).username
                } catch (exception: Exception) {
                    ""
                }
            _uiState.update { it.copy(usernames = it.usernames + (userId to username)) }
        }
    }

    // Get post title by post ID
    fun getPostTitle(postId: String, postType: PostType) {
        viewModelScope.launch {
            val title =
                try {
                    postRepository.getPost(postType, postId).title
                } catch (exception: Exception) {
                    ""
                }
            _uiState.update { it.copy(postTitles = it.postTitles + (postId to title)) }
        }
    }

    // Get avatar (profile picture) by post ID
    fun getAvatar(userId: String) {
        viewModelScope.launch {
            val avatar =
                try {
                    userRepository.getUser(userId).profilePicture
                } catch (exception: Exception) {
                    ""
                }
            _uiState.update { it.copy(avatars = it.avatars + (userId to avatar)) }
        }
    }

    // If chat is active and the associated post is completed or archived, show rating button
    fun shouldDisplayRatingButton(chat: Chat): Boolean {
        val postStatus = uiState.value.associatedPostStatuses[chat.relatedPostId] ?: return false
        return chat.isActive() &&
            (postStatus == PostStatus.COMPLETED || postStatus == PostStatus.ARCHIVED)
    }

    fun updateUserRating(userId: String, incomingRating: Float) {
        viewModelScope.launch {
            try {
                userRepository.updateRating(userId, incomingRating)
            } catch (exception: Exception) {
                ""
            }
        }
    }
}

class ChatListViewModelFactory(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepositery,
    private val postRepository: PostRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatListViewModel(chatRepository, userRepository, postRepository) as T
    }
}

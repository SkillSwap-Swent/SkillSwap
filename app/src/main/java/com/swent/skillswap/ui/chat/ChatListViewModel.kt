package com.swent.skillswap.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swent.skillswap.model.chat.Chat
import com.swent.skillswap.model.chat.ChatRepository
import com.swent.skillswap.model.chat.ChatStatus
import com.swent.skillswap.model.post.PostRepository
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
    val isLoading: Boolean = false,
    val error: String? = null
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
                    emptyList()
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
                    chats
                }
            val fullFilteredChats = filteredIsOwnerChats.filter { it.status == ChatStatus.ACTIVE }
            _uiState.update { it.copy(chats = fullFilteredChats) }
        }
    }

    fun acceptAPostReplyChat(chat: Chat) {
        viewModelScope.launch {
            try {
                chatRepository.acceptAPostReplyChat(chat)
            } catch (e: Exception) {
                Log.e(e.javaClass.toString(), e.message, e)
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

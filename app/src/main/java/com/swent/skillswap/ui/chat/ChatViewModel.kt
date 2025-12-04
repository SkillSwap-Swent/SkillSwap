package com.swent.skillswap.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.swent.skillswap.model.chat.ChatRepository
import com.swent.skillswap.model.chat.Message
import com.swent.skillswap.model.notification.NotificationRepository
import com.swent.skillswap.model.user.UserRepositery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
   Data class representing the UI state of the chat screen.
   It includes the list of messages, loading status, and any error messages.
*/
data class ChatUIState(
    val messages: List<Message> = emptyList<Message>(),
    val chatId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

/*
   ViewModel for managing chat state and interactions.
   It includes functionality to stream messages and send new messages.
   Each ChatViewModel instance is tied to a specific chat identified by chatId.
*/
class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val notificationRepository: NotificationRepository,
    private val userRepositery: UserRepositery,
    private val chatId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUIState(chatId = chatId))
    val uiState: StateFlow<ChatUIState> = _uiState.asStateFlow()

    init {
        startListening()
    }

    private fun startListening() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                chatRepository.streamMessages(chatId).collect { messages ->
                    _uiState.update {
                        it.copy(
                            messages = messages.sortedBy { it.timestamp },
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (exception: Exception) {
                _uiState.update { it.copy(isLoading = false, error = exception.message) }
            }
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            try {
                val senderId =
                    try {
                        FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    } catch (e: Exception) {
                        Log.e("ChatViewModel", "Error getting the sender ID: ${e.message}")
                        throw e
                    }
                chatRepository.sendMessage(chatId, senderId, content)
            } catch (exception: Exception) {
                _uiState.update { it.copy(error = exception.message, isLoading = false) }
            }
        }
    }

    /**
     * Returns the recipient's uid in the chat, given the sender's uid. Assumes the chat has exactly
     * two participants.
     */
    suspend fun getRecipientId(senderId: String): String {
        return try {
            val chat = chatRepository.getChat(chatId)
            chat.participants.firstOrNull { it != senderId } ?: ""
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error getting the recipient ID: ${e.message}")
            throw e
        }
    }
}

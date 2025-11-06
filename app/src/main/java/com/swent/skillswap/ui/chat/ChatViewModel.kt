package com.swent.skillswap.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swent.skillswap.model.chat.ChatRepository
import com.swent.skillswap.model.chat.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUIState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val currentUserId: String,
    private val chatId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUIState())
    val uiState: StateFlow<ChatUIState> = _uiState.asStateFlow()

    init {
        startListening()
    }

    private fun startListening() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                chatRepository.streamMessages(chatId).collect { messages ->
                    _uiState.value = _uiState.value.copy(
                        messages = messages.sortedBy { it.timestamp },
                        isLoading = false,
                        error = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message
                )
            }
        }
    }

    fun sendMessage(content: String) {

        viewModelScope.launch {
            try {
                chatRepository.sendMessage(chatId, currentUserId, content)
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = exception.message,
                    isLoading = false
                )
            }
        }
    }
}
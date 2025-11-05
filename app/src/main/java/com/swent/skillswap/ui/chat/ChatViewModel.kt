package com.swent.skillswap.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ChatUIState(
    val messages: List<Message> = emptyList(),
    val users: Pair<String, String> = Pair("", "")
)

class ChatViewModel (
    private val chatRepository: ChatRepository
) : ViewModel {
    private val _uiState = MutableStateFlow(ChatUIState())
    val uiState: StateFlow<ChatUIState> = _uiState

    init {
        startListening()
    }

    fun startListening(chatId: String) {
        viewModel.scope.launch {
            chatRepository.streamMessages(chatId) { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    fun sendMessage(chatId, message: String) {
        chatRepository.sendMessage(chatId, userId, message)
    }
}
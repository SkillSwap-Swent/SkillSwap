/* With the help of Sonnet 4.5 for repetitive tasks */

package com.swent.skillswap.ui.chat

import com.swent.skillswap.model.chat.ChatRepository
import com.swent.skillswap.model.chat.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private lateinit var fakeRepo: FakeChatRepository
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeRepo = FakeChatRepository()
        // Initialize new view model. This implies startListening is called.
        viewModel = ChatViewModel(fakeRepo, "user1", "chat1")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsEmpty() = runTest {
        assertTrue(viewModel.uiState.value.messages.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun startListeningGetsMessages() = runTest {
        fakeRepo.addMessages(listOf(
            Message("1", "user1", "Hello", 1000L)
        ))

        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.messages.size)
        assertEquals("Hello", viewModel.uiState.value.messages[0].content)
    }

    @Test
    fun messagesAreSortedByTime() = runTest {
        fakeRepo.addMessages(listOf(
            Message("2", "u2", "Second", 2000L),
            Message("1", "u1", "First", 1000L)
        ))

        advanceUntilIdle()

        assertEquals("First", viewModel.uiState.value.messages[0].content)
        assertEquals("Second", viewModel.uiState.value.messages[1].content)
    }

    @Test
    fun sendMessageCallsRepository() = runTest {
        viewModel.sendMessage("Hello")
        advanceUntilIdle()

        assertEquals(1, fakeRepo.sentMessages.size)
        assertEquals("chat1", fakeRepo.sentMessages[0].chatId)
        assertEquals("Hello", fakeRepo.sentMessages[0].content)
    }

    private class FakeChatRepository : ChatRepository {
        private val messagesFlow = MutableStateFlow<List<Message>>(emptyList())
        val sentMessages = mutableListOf<SentMessage>()

        override fun streamMessages(chatId: String) = messagesFlow

        override suspend fun sendMessage(chatId: String, senderId: String, content: String) {
            sentMessages.add(SentMessage(chatId, senderId, content))
        }

        fun addMessages(messages: List<Message>) {
            messagesFlow.value = messages
        }

        data class SentMessage(val chatId: String, val senderId: String, val content: String)
    }
}
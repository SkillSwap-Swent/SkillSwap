/* With the help of Sonnet 4.5 for repetitive tasks */

package com.swent.skillswap.ui.chat

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.skillswap.model.chat.ChatRepository
import com.swent.skillswap.model.chat.Message
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatScreenTest {

    @get:Rule val composeRule = createComposeRule()

    private fun createFakeViewModel(messages: List<Message> = emptyList()): ChatViewModel {
        return ChatViewModel(
            chatRepository =
                object : ChatRepository {
                    override fun streamMessages(chatId: String) = flowOf(messages)

                    override suspend fun sendMessage(
                        chatId: String,
                        senderId: String,
                        content: String
                    ) {}
                },
            currentUserId = "user1",
            chatId = "chat1"
        )
    }

    @Test
    fun chatScreen_displays_messages() {
        val messages =
            listOf(
                Message("1", "user1", "Hello!", 1000L),
                Message("2", "user2", "Hi there!", 2000L)
            )

        composeRule.setContent {
            MaterialTheme { ChatScreen(viewModel = createFakeViewModel(messages)) }
        }

        composeRule.onNodeWithText("Hello!").assertExists()
        composeRule.onNodeWithText("Hi there!").assertExists()
    }

    @Test
    fun chatScreen_shows_message_input_and_send_button() {
        composeRule.setContent { MaterialTheme { ChatScreen(viewModel = createFakeViewModel()) } }

        composeRule.onNodeWithTag(ChatScreenTags.MESSAGE_INPUT).assertExists()
        composeRule.onNodeWithTag(ChatScreenTags.SEND_BUTTON).assertExists()
    }

    @Test
    fun send_button_disabled_when_input_is_empty() {
        composeRule.setContent { MaterialTheme { ChatScreen(viewModel = createFakeViewModel()) } }

        composeRule.onNodeWithTag(ChatScreenTags.SEND_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun send_button_enabled_when_input_has_text() {
        composeRule.setContent { MaterialTheme { ChatScreen(viewModel = createFakeViewModel()) } }

        composeRule.onNodeWithTag(ChatScreenTags.MESSAGE_INPUT).performTextInput("Test message")
        composeRule.onNodeWithTag(ChatScreenTags.SEND_BUTTON).assertIsEnabled()
    }

    @Test
    fun input_text_can_be_typed() {
        composeRule.setContent { MaterialTheme { ChatScreen(viewModel = createFakeViewModel()) } }

        composeRule.onNodeWithTag(ChatScreenTags.MESSAGE_INPUT).performTextInput("Hello world")
        composeRule.onNodeWithText("Hello world").assertExists()
    }

    @Test
    fun back_button_triggers_callback() {
        var backPressed = false

        composeRule.setContent {
            MaterialTheme {
                ChatScreen(viewModel = createFakeViewModel(), onGoBack = { backPressed = true })
            }
        }

        composeRule.onNodeWithTag(ChatScreenTags.BACK_BUTTON).performClick()
        assert(backPressed)
    }

    @Test
    fun send_button_click_clears_input_and_sends_message() {
        composeRule.setContent { MaterialTheme { ChatScreen(viewModel = createFakeViewModel()) } }

        composeRule.onNodeWithTag(ChatScreenTags.MESSAGE_INPUT).performTextInput("Test message")
        composeRule.onNodeWithTag(ChatScreenTags.SEND_BUTTON).performClick()

        // Input should be cleared after sending
        composeRule
            .onNodeWithTag(ChatScreenTags.MESSAGE_INPUT)
            .assertTextEquals("", includeEditableText = true)

        // Whitespace-only input should not clear (message not sent)
        composeRule.onNodeWithTag(ChatScreenTags.MESSAGE_INPUT).performTextInput("   ")
        composeRule.onNodeWithTag(ChatScreenTags.SEND_BUTTON).performClick()
        composeRule.onNodeWithText("   ").assertExists() // Input still there
    }

    @Test
    fun messageBubble_displays_correctly_for_current_user() {
        val message = Message("1", "user1", "My message", 1000L)

        composeRule.setContent {
            MaterialTheme { MessageBubble(message = message, isCurrentUser = true) }
        }

        composeRule.onNodeWithText("My message").assertExists()
        composeRule.onNodeWithTag("${ChatScreenTags.MESSAGE_BUBBLE}_1").assertExists()
    }

    @Test
    fun messageBubble_displays_correctly_for_other_user() {
        val message = Message("2", "user2", "Their message", 2000L)

        composeRule.setContent {
            MaterialTheme { MessageBubble(message = message, isCurrentUser = false) }
        }

        composeRule.onNodeWithText("Their message").assertExists()
        composeRule.onNodeWithTag("${ChatScreenTags.MESSAGE_BUBBLE}_2").assertExists()
    }
}

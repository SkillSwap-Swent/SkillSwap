/* With the help of Sonnet 4.5 for repetitive tasks */

package com.swent.skillswap.ui.chat

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.swent.skillswap.model.chat.Chat
import com.swent.skillswap.model.chat.ChatRepository
import com.swent.skillswap.model.chat.Message
import com.swent.skillswap.model.notification.Notification
import com.swent.skillswap.model.notification.NotificationRepository
import com.swent.skillswap.model.notification.NotificationType
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.ui.notification.NotificationViewModel
import com.swent.skillswap.utils.FirebaseEmulator
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
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

                    override suspend fun createChat(
                        participants: List<String>,
                        relatedPostId: String,
                        relatedPostType: PostType
                    ): String {
                        return "fake-chat-id"
                    }

                    override suspend fun getChatsOfCurrentUser(
                        relatedPostType: PostType
                    ): List<Chat> {
                        return emptyList()
                    }

                    override suspend fun getChat(chatId: String): Chat {
                        return Chat("mock", emptyList(), "", PostType.REQUEST, emptyList())
                    }
                },
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
            MaterialTheme {
                ChatScreen(
                    chatViewModel = createFakeViewModel(messages),
                    notificationViewModel = null
                )
            }
        }

        composeRule.onNodeWithText("Hello!").assertExists()
        composeRule.onNodeWithText("Hi there!").assertExists()
    }

    @Test
    fun chatScreen_shows_message_input_and_send_button() {
        composeRule.setContent {
            MaterialTheme {
                ChatScreen(chatViewModel = createFakeViewModel(), notificationViewModel = null)
            }
        }

        composeRule.onNodeWithTag(ChatScreenTags.MESSAGE_INPUT).assertExists()
        composeRule.onNodeWithTag(ChatScreenTags.SEND_BUTTON).assertExists()
    }

    @Test
    fun send_button_disabled_when_input_is_empty() {
        composeRule.setContent {
            MaterialTheme {
                ChatScreen(chatViewModel = createFakeViewModel(), notificationViewModel = null)
            }
        }

        composeRule.onNodeWithTag(ChatScreenTags.SEND_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun send_button_enabled_when_input_has_text() {
        composeRule.setContent {
            MaterialTheme {
                ChatScreen(chatViewModel = createFakeViewModel(), notificationViewModel = null)
            }
        }

        composeRule.onNodeWithTag(ChatScreenTags.MESSAGE_INPUT).performTextInput("Test message")
        composeRule.onNodeWithTag(ChatScreenTags.SEND_BUTTON).assertIsEnabled()
    }

    @Test
    fun input_text_can_be_typed() {
        composeRule.setContent {
            MaterialTheme {
                ChatScreen(chatViewModel = createFakeViewModel(), notificationViewModel = null)
            }
        }

        composeRule.onNodeWithTag(ChatScreenTags.MESSAGE_INPUT).performTextInput("Hello world")
        composeRule.onNodeWithText("Hello world").assertExists()
    }

    @Test
    fun back_button_triggers_callback() {
        var backPressed = false

        composeRule.setContent {
            MaterialTheme {
                ChatScreen(
                    chatViewModel = createFakeViewModel(),
                    notificationViewModel = null,
                    onGoBack = { backPressed = true }
                )
            }
        }

        composeRule.onNodeWithTag(ChatScreenTags.BACK_BUTTON).performClick()
        assert(backPressed)
    }

    @Test
    fun send_button_click_clears_input_and_sends_message() {
        composeRule.setContent {
            MaterialTheme {
                ChatScreen(chatViewModel = createFakeViewModel(), notificationViewModel = null)
            }
        }

        composeRule.onNodeWithTag(ChatScreenTags.MESSAGE_INPUT).performTextInput("Test message")
        composeRule.onNodeWithTag(ChatScreenTags.SEND_BUTTON).performClick()

        // Input should be cleared after sending
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Test message").assertDoesNotExist()

        // Whitespace-only input should not clear (message not sent)
        composeRule.onNodeWithTag(ChatScreenTags.MESSAGE_INPUT).performTextInput("   ")
        composeRule.onNodeWithTag(ChatScreenTags.SEND_BUTTON).performClick()
        composeRule.waitForIdle()
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

    // New test to cover the send -> sendMessage + notification path
    @Test
    fun send_button_triggers_sendMessage_and_adds_notification() = runBlocking {
        // Start emulator and sign in so NotificationViewModel can work without touching production
        // Firebase
        FirebaseEmulator.reinitialize()
        FirebaseEmulator.startEmulator()
        val authResult = FirebaseAuth.getInstance().signInAnonymously().await()
        val currentUserId = authResult.user?.uid ?: "test-sender"

        // Capture sent messages
        val sentMessages =
            mutableListOf<Triple<String, String, String>>() // chatId, senderId, content

        // Fake ChatRepository that records sendMessage and returns a chat with two participants
        val fakeChatRepository =
            object : ChatRepository {
                override fun streamMessages(chatId: String) = flowOf(emptyList<Message>())

                override suspend fun sendMessage(
                    chatId: String,
                    senderId: String,
                    content: String
                ) {
                    sentMessages.add(Triple(chatId, senderId, content))
                }

                override suspend fun createChat(
                    participants: List<String>,
                    relatedPostId: String,
                    relatedPostType: PostType
                ): String {
                    return "fake-chat-id"
                }

                override suspend fun getChatsOfCurrentUser(relatedPostType: PostType): List<Chat> {
                    return emptyList()
                }

                override suspend fun getChat(chatId: String): Chat {
                    // return a chat where recipient is "recipient-uid"
                    return Chat(
                        chatId,
                        listOf(currentUserId, "recipient-uid"),
                        "",
                        PostType.REQUEST,
                        emptyList()
                    )
                }
            }

        val chatViewModel = ChatViewModel(fakeChatRepository, "chat1")

        // Simple in-memory NotificationRepository
        val addedNotifications = mutableListOf<Notification>()
        val fakeNotificationRepository =
            object : NotificationRepository {
                override fun getNewUid(): String = "notif-${addedNotifications.size + 1}"

                override suspend fun getNotificationsForUser(userId: String): List<Notification> =
                    emptyList()

                override suspend fun getUnreadNotificationsForUser(
                    userId: String
                ): List<Notification> = emptyList()

                override suspend fun getNotification(notificationId: String): Notification {
                    return Notification("", "", "", "", NotificationType.MESSAGE, "")
                }

                override suspend fun addNotification(notification: Notification) {
                    addedNotifications.add(notification)
                }

                override suspend fun markAsRead(notificationId: String) {}

                override suspend fun markAllAsRead(userId: String) {}

                override suspend fun deleteNotification(notificationId: String) {}

                override suspend fun deleteAllNotificationsForUser(userId: String) {}

                override suspend fun markChatNotificationsAsRead(chatId: String, userId: String) {}
            }

        val notificationViewModel = NotificationViewModel(fakeNotificationRepository)

        // Render screen with our fake view models and with currentUserId set so getRecipientId
        // works
        composeRule.setContent {
            MaterialTheme {
                ChatScreen(
                    chatViewModel = chatViewModel,
                    notificationViewModel = notificationViewModel,
                    currentUserId = currentUserId
                )
            }
        }

        // Type and send a message
        val messageText = "Hello recipient"
        composeRule.onNodeWithTag(ChatScreenTags.MESSAGE_INPUT).performTextInput(messageText)
        composeRule.onNodeWithTag(ChatScreenTags.SEND_BUTTON).performClick()

        // Wait for async operations to complete: use composeRule.waitUntil to avoid busy polling
        // Give a generous timeout for CI; if this times out the test will fail with context
        val waitTimeoutMs = 10_000L
        composeRule.waitUntil(waitTimeoutMs) {
            sentMessages.isNotEmpty() && addedNotifications.isNotEmpty()
        }

        // Verify sendMessage was called
        assert(sentMessages.isNotEmpty()) { "Expected sendMessage to be called" }
        val sent = sentMessages.first()
        assert(sent.second == currentUserId)
        assert(sent.third == messageText)

        // Verify notification was added
        assert(addedNotifications.isNotEmpty()) { "Expected a notification to be added" }
        val notif = addedNotifications.first()
        assert(notif.userId == "recipient-uid")
        assert(notif.message == messageText)
        assert(notif.type == NotificationType.MESSAGE)
        assert(notif.relatedId == "chat1")

        // Verify input was cleared in the UI
        composeRule.onNodeWithText(messageText).assertDoesNotExist()
    }

    @Test
    fun chatScreen_scrolls_to_last_message_when_many_messages() {
        // Create 50 messages
        val messages =
            List(50) { i ->
                Message(
                    id = i.toString(),
                    senderId = "user${i % 2}",
                    content = "Message $i",
                    timestamp = i * 1000L
                )
            }
        composeRule.setContent {
            MaterialTheme {
                ChatScreen(
                    chatViewModel = createFakeViewModel(messages),
                    notificationViewModel = null
                )
            }
        }
        // Assert that the last message is visible
        composeRule.onNodeWithText("Message 49").assertExists()
    }
}

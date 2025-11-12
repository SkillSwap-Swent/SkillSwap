package com.swent.skillswap.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.skillswap.model.chat.Message

// Encapsulating object for test tags
object ChatScreenTags {
    const val SCREEN = "chat_screen"
    const val BACK_BUTTON = "chat_back_button"
    const val TITLE = "chat_title"
    const val MESSAGE_LIST = "chat_message_list"
    const val MESSAGE_BUBBLE = "chat_message_bubble"
    const val MESSAGE_INPUT = "chat_message_input"
    const val SEND_BUTTON = "chat_send_button"
}

/*
    Composable function representing the chat screen UI.
    It displays a list of messages, an input field for new messages, and a send button.
    The screen also includes a top app bar with a title and a back button.
*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel, chatTitle: String = "Chat", onGoBack: () -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().testTag(ChatScreenTags.SCREEN)) {
        TopAppBar(
            title = {
                Text(
                    text = chatTitle,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag(ChatScreenTags.TITLE)
                )
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
            navigationIcon = {
                IconButton(
                    onClick = onGoBack,
                    modifier = Modifier.testTag(ChatScreenTags.BACK_BUTTON)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        LazyColumn(
            modifier =
                Modifier.weight(1f)
                    .padding(horizontal = 16.dp)
                    .testTag(ChatScreenTags.MESSAGE_LIST),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(uiState.messages) { message ->
                MessageBubble(message = message, message.senderId == viewModel.getCurrentUserId())
            }
        }

        MessageInput(
            text = inputText,
            onTextChange = { inputText = it },
            onSend = {
                if (inputText.isNotBlank()) {
                    viewModel.sendMessage(inputText)
                    inputText = ""
                }
            }
        )
    }
}

/*
    Composable function representing a single message bubble in the chat.
    It styles the bubble differently based on whether the message is from the current user or another user.
*/
@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color =
                if (isCurrentUser) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
            modifier =
                Modifier.widthIn(max = 280.dp)
                    .testTag("${ChatScreenTags.MESSAGE_BUBBLE}_${message.id}")
        ) {
            Text(
                text = message.content,
                color =
                    if (isCurrentUser) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

/*
   Composable function for the message input area at the bottom of the chat screen.
   It includes a text field for typing messages and a send button, which is enabled only when there is text to send.
   onTextChange: Callback for when the text input changes.
   onSend: Callback for when the send button is pressed.
*/
@Composable
fun MessageInput(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f).testTag(ChatScreenTags.MESSAGE_INPUT),
                placeholder = { Text("Message") },
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank(),
                modifier = Modifier.testTag(ChatScreenTags.SEND_BUTTON),
                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

// Written by Sonnet 4.5
/*
@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    SkillSwapAppTheme {
        ChatScreen(
            viewModel =
                ChatViewModel(
                    chatRepository =
                        object : ChatRepository {
                            override fun streamMessages(chatId: String) =
                                flowOf(
                                    listOf(
                                        Message(
                                            "1",
                                            "user1",
                                            "Hey there!",
                                            System.currentTimeMillis() - 600000
                                        ),
                                        Message(
                                            "2",
                                            "user2",
                                            "Hi! How are you?",
                                            System.currentTimeMillis() - 540000
                                        ),
                                        Message(
                                            "3",
                                            "user1",
                                            "I'm good, thanks!",
                                            System.currentTimeMillis() - 480000
                                        ),
                                        Message(
                                            "4",
                                            "user2",
                                            "What are you up to?",
                                            System.currentTimeMillis() - 420000
                                        ),
                                        Message(
                                            "5",
                                            "user1",
                                            "Just working on a project",
                                            System.currentTimeMillis() - 360000
                                        ),
                                        Message(
                                            "6",
                                            "user2",
                                            "Nice! What kind of project?",
                                            System.currentTimeMillis() - 300000
                                        ),
                                        Message(
                                            "7",
                                            "user1",
                                            "A chat app using Jetpack Compose",
                                            System.currentTimeMillis() - 240000
                                        ),
                                        Message(
                                            "8",
                                            "user2",
                                            "That sounds interesting!",
                                            System.currentTimeMillis() - 180000
                                        ),
                                        Message(
                                            "9",
                                            "user1",
                                            "Yeah, it's coming along well",
                                            System.currentTimeMillis() - 120000
                                        ),
                                        Message(
                                            "10",
                                            "user2",
                                            "Can I see it when you're done?",
                                            System.currentTimeMillis() - 60000
                                        ),
                                        Message(
                                            "11",
                                            "user1",
                                            "Sure thing!",
                                            System.currentTimeMillis() - 30000
                                        ),
                                        Message(
                                            "12",
                                            "user2",
                                            "Awesome!",
                                            System.currentTimeMillis() - 15000
                                        ),
                                        Message(
                                            "13",
                                            "user1",
                                            "Talk soon!",
                                            System.currentTimeMillis()
                                        )
                                    )
                                )

                            override suspend fun sendMessage(
                                chatId: String,
                                senderId: String,
                                content: String
                            ) {}
                        },
                    currentUserId = "user1",
                    chatId = "preview"
                )
        )
    }
}
*/

package com.swent.skillswap.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.swent.skillswap.model.chat.Chat
import com.swent.skillswap.model.post.PostType

@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel = viewModel(),
    currentUserId: String =
        try {
            FirebaseAuth.getInstance().currentUser?.uid ?: ""
        } catch (e: Exception) {
            ""
        },
    onChatClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPostType by remember { mutableStateOf(PostType.OFFER) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Title
        Text(
            text = "Chat",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        )

        // Related post type filter buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PostTypeFilterButton(
                text = "Offer",
                isSelected = selectedPostType == PostType.OFFER,
                onClick = { selectedPostType = PostType.OFFER },
                modifier = Modifier.weight(1f)
            )

            PostTypeFilterButton(
                text = "Request",
                isSelected = selectedPostType == PostType.REQUEST,
                onClick = { selectedPostType = PostType.REQUEST },
                modifier = Modifier.weight(1f)
            )
        }

        // Chat List
        LaunchedEffect(selectedPostType) { viewModel.getChatsOfCurrentUser(selectedPostType) }
        val filteredChats = uiState.chats
        if (filteredChats.isEmpty()) {
            // Empty state
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No chats available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredChats) { chat ->
                    ChatConversationItem(
                        viewModel = viewModel,
                        currentUserId,
                        chat = chat,
                        onClick = { onChatClick(chat.id) }
                    )
                }
            }
        }
    }
}

/** Filter button for post types (Offer/Request) */
@Composable
fun PostTypeFilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
            ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color =
                if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

/** Individual post conversation item */
@Composable
fun ChatConversationItem(
    viewModel: ChatListViewModel,
    currentUserId: String,
    chat: Chat,
    onClick: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()

    val currentUser = currentUserId
    // Assuming two participants
    val otherUser = chat.participants.first { it != currentUser }

    LaunchedEffect(chat.relatedPostId) {
        viewModel.getPostTitle(chat.relatedPostId, chat.relatedPostType)
    }
    LaunchedEffect(otherUser) { viewModel.getUsername(otherUser) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Related post title
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.postTitles[chat.relatedPostId] ?: "Loading...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Right side - Other chat participant username
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(
                    text = uiState.usernames[otherUser] ?: "Loading...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

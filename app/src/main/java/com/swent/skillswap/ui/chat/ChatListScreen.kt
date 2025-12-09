package com.swent.skillswap.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.swent.skillswap.model.chat.Chat
import com.swent.skillswap.model.post.PostType

object ChatListTestTags {
    const val SCREEN = "ChatListScreen"
    const val TITLE = "ChatListTitle"
    const val OFFER = "OfferFilterButton"
    const val REQUEST = "RequestFilterButton"
    const val WAITING = "WaitingFilterButton"
    const val TO_APPROVE = "ToApprovePostsList"
    const val POSTS_LIST = "PostsList"
    const val ACCEPT_CHAT = "AcceptChatButton"
    const val EMPTY_STATE = "EmptyState"
    const val AVATAR = "Avatar"
}

@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel = viewModel(),
    currentUserId: String = "",
    onChatClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPostType by remember { mutableStateOf(PostType.REQUEST) }
    var isPendingSelected by remember { mutableStateOf(false) }
    var isOwnerSelected by remember { mutableStateOf<Boolean?>(null) }
    // Chat List
    viewModel.getChatsOfCurrentUser(selectedPostType, isPendingSelected, isOwnerSelected)
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag(ChatListTestTags.SCREEN)) {
        // Title
        Text(
            text = "Chat",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier =
                Modifier.fillMaxWidth().padding(bottom = 24.dp).testTag(ChatListTestTags.TITLE)
        )

        // Related post type filter buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PostTypeFilterButton(
                text = "Offer",
                isSelected = selectedPostType == PostType.OFFER && !isPendingSelected,
                onClick = {
                    selectedPostType = PostType.OFFER
                    isPendingSelected = false
                    isOwnerSelected = null
                },
                modifier = Modifier.weight(1f).testTag(ChatListTestTags.OFFER)
            )

            PostTypeFilterButton(
                text = "Request",
                isSelected = selectedPostType == PostType.REQUEST && !isPendingSelected,
                onClick = {
                    selectedPostType = PostType.REQUEST
                    isPendingSelected = false
                    isOwnerSelected = null
                },
                modifier = Modifier.weight(1f).testTag(ChatListTestTags.REQUEST)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PostTypeFilterButton(
                text = "To Approve",
                isSelected =
                    selectedPostType == PostType.REQUEST &&
                        isPendingSelected &&
                        isOwnerSelected == true,
                onClick = {
                    selectedPostType = PostType.REQUEST
                    isPendingSelected = true
                    isOwnerSelected = true
                },
                modifier = Modifier.weight(1f).testTag(ChatListTestTags.TO_APPROVE)
            )
            PostTypeFilterButton(
                text = "Awaiting",
                isSelected =
                    selectedPostType == PostType.REQUEST &&
                        isPendingSelected &&
                        isOwnerSelected == false,
                onClick = {
                    selectedPostType = PostType.REQUEST
                    isPendingSelected = true
                    isOwnerSelected = false
                },
                modifier = Modifier.weight(1f).testTag(ChatListTestTags.WAITING)
            )
        }

        val filteredChats = uiState.chats
        if (filteredChats.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize().testTag(ChatListTestTags.EMPTY_STATE),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No chats available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.testTag(ChatListTestTags.POSTS_LIST)
            ) {
                items(filteredChats) { chat ->
                    ChatConversationItem(
                        viewModel = viewModel,
                        currentUserId = currentUserId,
                        chat = chat,
                        onClick = { onChatClick(chat.id) },
                        isOwner = isOwnerSelected
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

/** Individual chat conversation item */
@Composable
fun ChatConversationItem(
    viewModel: ChatListViewModel,
    currentUserId: String,
    chat: Chat,
    onClick: () -> Unit,
    isOwner: Boolean? = null
) {

    val uiState by viewModel.uiState.collectAsState()

    var showRatingDialog by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableIntStateOf(0) }

    val currentUser = currentUserId
    val otherUser = chat.participants.first { it != currentUser } // Assuming two participants

    LaunchedEffect(chat.relatedPostId) {
        viewModel.getPostTitle(chat.relatedPostId, chat.relatedPostType)
    }
    LaunchedEffect(otherUser) { viewModel.getUsernameAndAvatar(otherUser) }
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
            // Profile picture on the left
            val avatarUrl = uiState.avatars[otherUser]
            if (avatarUrl.isNullOrBlank()) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Default profile picture",
                    modifier =
                        Modifier.size(48.dp).clip(CircleShape).testTag(ChatListTestTags.AVATAR),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Profile picture",
                    modifier =
                        Modifier.size(48.dp).clip(CircleShape).testTag(ChatListTestTags.AVATAR),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
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
            if (isOwner == true) {
                Icon(
                    Icons.Default.GppGood,
                    "approve",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier =
                        Modifier.clickable(onClick = { viewModel.acceptAPostReplyChat(chat) })
                            .testTag(ChatListTestTags.ACCEPT_CHAT)
                )
            }
        }

        // Rate user button
        if (viewModel.shouldDisplayRatingButton(chat)) {
            IconButton(onClick = { showRatingDialog = true }, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = "Rate User",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

    if (showRatingDialog) {
        Dialog(
            onDismissRequest = { showRatingDialog = false },
            content = {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Rate this exchange", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp))
                        Row {
                            (1..5).forEach { rating ->
                                IconButton(onClick = { selectedRating = rating }) {
                                    Icon(
                                        imageVector =
                                            if (rating <= selectedRating) Icons.Filled.Star
                                            else Icons.Outlined.Star,
                                        contentDescription = "rating stars",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { showRatingDialog = false }) { Text("Cancel") }
                            Button(
                                onClick = {
                                    if (selectedRating > 0) {
                                        viewModel.updateUserRating(
                                            userId = otherUser,
                                            incomingRating = selectedRating.toFloat()
                                        )
                                    }
                                    showRatingDialog = false
                                }
                            ) {
                                Text("Submit")
                            }
                        }
                    }
                }
            }
        )
    }
}

package com.swent.skillswap.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swent.skillswap.model.chat.Chat
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.ui.utils.AvatarDisplay

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
    const val ERROR = "ErrorMessage"
}

@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel = viewModel(),
    currentUserId: String = "",
    onChatClick: (String) -> Unit = {},
    onAvatarClick: (String) -> Unit = {},
    onNotificationClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPostType by remember { mutableStateOf(PostType.REQUEST) }
    var isPendingSelected by remember { mutableStateOf(false) }
    var isOwnerSelected by remember { mutableStateOf<Boolean?>(null) }
    // Chat List
    viewModel.getChatsOfCurrentUser(selectedPostType, isPendingSelected, isOwnerSelected)
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag(ChatListTestTags.SCREEN)) {
        // Title with notification button
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Text(
                text = "Chat",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier.fillMaxWidth().align(Alignment.Center).testTag(ChatListTestTags.TITLE)
            )
            if (onNotificationClick != null) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications"
                    )
                }
            }
        }

        // Related post type filter buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
        when {
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().fillMaxSize().testTag(ChatListTestTags.ERROR),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = uiState.error ?: "An unknown error occurred",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            filteredChats.isEmpty() -> {
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
            }
            else -> {
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
                            isOwner = isOwnerSelected,
                            onAvatarClick = { userId -> onAvatarClick(userId) }
                        )
                    }
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
    isOwner: Boolean? = null,
    onAvatarClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRatingDialog by remember { mutableStateOf(false) }
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
            AvatarDisplay(
                avatarUrl = uiState.avatars[otherUser],
                modifier = Modifier.testTag(ChatListTestTags.AVATAR),
                onClick = { onAvatarClick(otherUser) }
            )
            Spacer(modifier = Modifier.width(16.dp))
            PostTitleDisplay(
                title = uiState.postTitles[chat.relatedPostId],
                modifier = Modifier.weight(1f)
            )
            UsernameDisplay(username = uiState.usernames[otherUser], modifier = Modifier.weight(1f))
            ApprovalIcon(isOwner = isOwner, onApprove = { viewModel.acceptAPostReplyChat(chat) })
        }
        RatingButton(
            shouldDisplay = viewModel.shouldDisplayRatingButton(chat),
            onClick = { showRatingDialog = true }
        )
    }
    RatingDialog(
        show = showRatingDialog,
        onCancel = { showRatingDialog = false },
        onSubmit = { rating ->
            if (rating > 0) {
                viewModel.updateUserRating(
                    userId = otherUser,
                    incomingRating = rating.toFloat()
                )
            }
            showRatingDialog = false
        }
    )
}

@Composable
private fun PostTitleDisplay(title: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = title ?: "Loading...",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun UsernameDisplay(username: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.End) {
        Text(
            text = username ?: "Loading...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ApprovalIcon(isOwner: Boolean?, onApprove: () -> Unit) {
    if (isOwner == true) {
        Icon(
            Icons.Default.GppGood,
            "approve",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.clickable(onClick = onApprove).testTag(ChatListTestTags.ACCEPT_CHAT)
        )
    }
}

@Composable
private fun RatingButton(shouldDisplay: Boolean, onClick: () -> Unit) {
    if (shouldDisplay) {
        IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Outlined.Star,
                contentDescription = "Rate User",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun RatingDialog(
    show: Boolean,
    onCancel: () -> Unit,
    onSubmit: (Int) -> Unit
) {
    if (show) {
        Dialog(
            onDismissRequest = onCancel,
            content = {
                var selectedRating by remember { mutableIntStateOf(0) }

                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Rate this exchange", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp))
                        Row {
                            (1..5).forEach { rating ->
                                val isSelected = rating <= selectedRating
                                IconButton(onClick = { selectedRating = rating }) {
                                    Icon(
                                        imageVector =
                                            if (isSelected) Icons.Filled.Star
                                            else Icons.Outlined.Star,
                                        contentDescription = "rating stars",
                                        tint =
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = onCancel) { Text("Cancel") }
                            Button(onClick = { onSubmit(selectedRating) }) { Text("Submit") }
                        }
                    }
                }
            }
        )
    }
}

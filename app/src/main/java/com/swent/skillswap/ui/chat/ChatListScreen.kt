package com.swent.skillswap.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.swent.skillswap.model.chat.Chat
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.ui.chat.ChatListTestTags.BLOCK_BUTTON
import com.swent.skillswap.ui.chat.ChatListTestTags.CHAT_MENU_BUTTON
import com.swent.skillswap.ui.chat.ChatListTestTags.NOTIFICATION
import com.swent.skillswap.ui.chat.ChatListTestTags.ONGOING_TAB
import com.swent.skillswap.ui.chat.ChatListTestTags.PENDING_TAB
import com.swent.skillswap.ui.chat.ChatListTestTags.REPLIES_TAB
import com.swent.skillswap.ui.chat.ChatListTestTags.USERNAME
import com.swent.skillswap.ui.chat.ChatListTestTags.USER_AVATAR
import com.swent.skillswap.ui.utils.AvatarDisplay
import com.swent.skillswap.ui.utils.pill_shape

const val defaultText = "Loading..."

object ChatListTestTags {
    const val SCREEN = "ChatListScreen"
    const val TITLE = "ChatListTitle"
    const val ONGOING_TAB = "RequestFilterButton"
    const val PENDING_TAB = "WaitingFilterButton"
    const val REPLIES_TAB = "ToApprovePostsList"
    const val POSTS_LIST = "PostsList"
    const val ACCEPT_CHAT = "AcceptChatButton"
    const val EMPTY_STATE = "EmptyState"
    const val USER_AVATAR = "UserAvatar"
    const val CHAT_MENU_BUTTON = "ChatMenuButton"
    const val NOTIFICATION = "Notification"
    const val BLOCK_BUTTON = "BlockButton"
    const val USERNAME = "Username"
    const val AVATAR = "Avatar"
    const val ERROR = "ErrorMessage"
}

enum class TabEntry(
    val label: String,
    val testTag: String,
    val selectedPostType: PostType,
    val isPendingSelected: Boolean,
    val isOwnerSelected: Boolean?
) {
    ONGOING("Ongoing", ONGOING_TAB, PostType.REQUEST, false, null),
    REPLIES("Replies", REPLIES_TAB, PostType.REQUEST, true, true),
    PENDING("Pending", PENDING_TAB, PostType.REQUEST, true, false)
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
    val uid = Firebase.auth.uid ?: ""
    var selectedDestination by remember { mutableStateOf(TabEntry.ONGOING) }
    LaunchedEffect(uid) {
        if (uid != "") {
            viewModel.getUsernameAndAvatar(uid)
        }
    }
    // Chat List
    viewModel.getChatsOfCurrentUser(
        selectedDestination.selectedPostType,
        selectedDestination.isPendingSelected,
        selectedDestination.isOwnerSelected
    )
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).testTag(ChatListTestTags.SCREEN)) {
        // Title with notification button
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            ChatListTopBarInfo(
                modifier = Modifier.fillMaxWidth(0.90f),
                username = uiState.usernames[uid],
                avatarURL = uiState.avatars[uid]
            )
            if (onNotificationClick != null) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.align(Alignment.CenterEnd).testTag(NOTIFICATION)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications"
                    )
                }
            }
        }
        // Related post type filter buttons
        ChatListTopBarTab(
            selectedDestination = selectedDestination,
            onClick =
                arrayOf(
                    { selectedDestination = TabEntry.ONGOING },
                    { selectedDestination = TabEntry.REPLIES },
                    { selectedDestination = TabEntry.PENDING }
                ),
            modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(12.dp))
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
                            isOwner = selectedDestination.isOwnerSelected,
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        var showMenu by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(70.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RatingButton(
                shouldDisplay = viewModel.shouldDisplayRatingButton(chat),
                onClick = { showRatingDialog = true }
            )
            ApprovalIcon(isOwner = isOwner, onApprove = { viewModel.acceptAPostReplyChat(chat) })
            Spacer(modifier = Modifier.weight(0.2f))
            AvatarDisplay(
                avatarUrl = uiState.avatars[otherUser],
                modifier = Modifier.testTag(ChatListTestTags.AVATAR),
                onClick = { onAvatarClick(otherUser) }
            )
            Spacer(modifier = Modifier.width(16.dp))
            UsernameDisplay(username = uiState.usernames[otherUser], modifier = Modifier.weight(1f))
            PostTitleDisplay(
                title = uiState.postTitles[chat.relatedPostId],
                modifier = Modifier.weight(1f)
            )
            Box() {
                IconButton(
                    onClick = { showMenu = !showMenu },
                    colors =
                        IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                    modifier = Modifier.testTag(CHAT_MENU_BUTTON)
                ) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                }
                if (showMenu) {
                    DropdownMenu(
                        expanded = true,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text("Block User", color = MaterialTheme.colorScheme.onSurface)
                            },
                            onClick = { showMenu = false },
                            modifier = Modifier.testTag(BLOCK_BUTTON)
                        )
                    }
                }
            }
        }
    }
    RatingDialog(
        show = showRatingDialog,
        onCancel = { showRatingDialog = false },
        onSubmit = { rating ->
            if (rating > 0) {
                viewModel.updateUserRating(
                    userId = otherUser,
                    incomingRating = rating.toFloat(),
                    chatId = chat.id
                )
            }
            showRatingDialog = false
        }
    )
}

@Composable
fun ChatListTopBarInfo(
    modifier: Modifier = Modifier,
    username: String? = null,
    avatarURL: String? = null
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier) {
            AvatarDisplay(
                avatarUrl = avatarURL,
                modifier = Modifier.testTag(USER_AVATAR),
                onClick = {}
            )
            Spacer(modifier = Modifier.weight(0.5f))
            Text(
                text = username ?: defaultText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.CenterVertically).testTag(USERNAME)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        HorizontalDivider(modifier = modifier.fillMaxWidth(1f), thickness = 1.dp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListTopBarTab(
    modifier: Modifier = Modifier,
    selectedDestination: TabEntry,
    vararg onClick: () -> Unit = arrayOf({}, {}, {})
) {
    PrimaryTabRow(
        selectedTabIndex = selectedDestination.ordinal,
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        TabEntry.entries.forEachIndexed { index, destination ->
            Tab(
                selected = selectedDestination == destination,
                onClick = { onClick[index]() },
                modifier = Modifier.testTag(destination.testTag),
                text = {
                    Text(
                        color =
                            if (selectedDestination == destination)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        text = destination.label,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

@Composable
private fun PostTitleDisplay(title: String?, modifier: Modifier = Modifier) {
    Surface(
        shape = pill_shape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
        modifier = modifier
    ) {
        Box(modifier = Modifier.padding(5.dp, 1.dp).fillMaxWidth(1f)) {
            Text(
                text = title ?: "defaultText",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun UsernameDisplay(username: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Text(
            text = username ?: "defaultText",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ApprovalIcon(isOwner: Boolean? = true, onApprove: () -> Unit = {}) {
    if (isOwner == true) {
        IconButton(
            onClick = onApprove,
            colors =
                IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
            modifier = Modifier.testTag(ChatListTestTags.ACCEPT_CHAT).size(32.dp)
        ) {
            Icon(Icons.Default.Check, "approve", tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun RatingButton(shouldDisplay: Boolean, onClick: () -> Unit) {
    if (shouldDisplay) {
        IconButton(
            onClick = onClick,
            colors =
                IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Star,
                contentDescription = "Rate User",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun RatingDialog(show: Boolean, onCancel: () -> Unit, onSubmit: (Int) -> Unit) {
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
                                        imageVector = starIcon(isSelected),
                                        contentDescription = "rating stars",
                                        tint =
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.4f
                                                )
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

private fun starIcon(isSelected: Boolean) =
    if (isSelected) Icons.Filled.Star else Icons.Outlined.Star

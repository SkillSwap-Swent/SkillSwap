/** Created with the help of Cursor */
package com.swent.skillswap.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swent.skillswap.model.notification.Notification
import com.swent.skillswap.model.notification.NotificationType
import java.text.SimpleDateFormat
import java.util.Locale

object NotificationScreenTags {
    const val SCREEN = "notification_screen"
    const val TITLE = "notification_title"
    const val LOADING_INDICATOR = "notification_loading"
    const val ERROR_MESSAGE = "notification_error"
    const val EMPTY_STATE = "notification_empty"
    const val NOTIFICATIONS_LIST = "notifications_list"
    const val NOTIFICATION_ITEM = "notification_item"
    const val FILTER_ALL = "filter_all"
    const val FILTER_UNREAD = "filter_unread"
    const val MARK_ALL_READ = "mark_all_read"
    const val DELETE_ALL = "delete_all"
}

/**
 * Screen that displays all notifications for the current user.
 *
 * Features:
 * - Filter between all and unread notifications
 * - Different styling based on notification type
 * - Tap to navigate to related content (chat/post)
 * - Swipe to delete or mark as read
 *
 * @param viewModel The ViewModel managing the notifications state
 * @param onGoBack Callback when user navigates back
 * @param onNotificationClick Callback when a notification is tapped (provides notification for
 *   navigation)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onNotificationClick: (Notification) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Notifications", modifier = Modifier.testTag(NotificationScreenTags.TITLE))
                },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Mark all as read button
                    if (uiState.notifications.any { !it.isRead }) {
                        TextButton(
                            onClick = { viewModel.markAllAsRead() },
                            modifier = Modifier.testTag(NotificationScreenTags.MARK_ALL_READ)
                        ) {
                            Text(
                                "Mark all read",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .testTag(NotificationScreenTags.SCREEN)
        ) {
            // Filter Buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !uiState.showUnreadOnly,
                    onClick = { viewModel.setShowUnreadOnly(false) },
                    label = { Text("All") },
                    modifier = Modifier.weight(1f).testTag(NotificationScreenTags.FILTER_ALL),
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                )
                FilterChip(
                    selected = uiState.showUnreadOnly,
                    onClick = { viewModel.setShowUnreadOnly(true) },
                    label = { Text("Unread") },
                    modifier = Modifier.weight(1f).testTag(NotificationScreenTags.FILTER_UNREAD),
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                )
            }

            // Content
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.testTag(NotificationScreenTags.LOADING_INDICATOR)
                        )
                    }
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = uiState.error ?: "An error occurred",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag(NotificationScreenTags.ERROR_MESSAGE)
                            )
                            Button(onClick = { viewModel.refresh() }) { Text("Retry") }
                        }
                    }
                }
                uiState.notifications.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Text(
                                text =
                                    if (uiState.showUnreadOnly) "No unread notifications"
                                    else "No notifications",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag(NotificationScreenTags.EMPTY_STATE)
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.testTag(NotificationScreenTags.NOTIFICATIONS_LIST),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.notifications, key = { it.uid }) { notification ->
                            NotificationItem(
                                notification = notification,
                                onClick = {
                                    if (!notification.isRead) {
                                        viewModel.markAsRead(notification)
                                    }
                                    onNotificationClick(notification)
                                },
                                onDelete = { viewModel.deleteNotification(notification) },
                                modifier =
                                    Modifier.testTag(
                                        "${NotificationScreenTags.NOTIFICATION_ITEM}_${notification.uid}"
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Individual notification item with type-based styling. */
@Composable
private fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typeConfig = getNotificationTypeConfig(notification.type)
    val dateFormatter = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (notification.isRead) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    }
            ),
        elevation =
            CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 1.dp else 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Top) {
            // Type Icon with colored background
            Box(
                modifier =
                    Modifier.size(44.dp).clip(CircleShape).background(typeConfig.backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeConfig.icon,
                    contentDescription = typeConfig.label,
                    tint = typeConfig.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                // Header: Title + Type Label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight =
                            if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Type badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = typeConfig.backgroundColor.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = typeConfig.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = typeConfig.iconColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Message
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme.onSurface.copy(
                            alpha = if (notification.isRead) 0.6f else 0.9f
                        ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Footer: Timestamp + Unread indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateFormatter.format(notification.timestamp.toDate()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    if (!notification.isRead) {
                        Box(
                            modifier =
                                Modifier.size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }

            // Delete button
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete notification",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/** Configuration for notification type styling. */
private data class NotificationTypeConfig(
    val icon: ImageVector,
    val label: String,
    val backgroundColor: Color,
    val iconColor: Color
)

/** Returns the styling configuration for a given notification type. */
@Composable
private fun getNotificationTypeConfig(type: NotificationType): NotificationTypeConfig {
    return when (type) {
        NotificationType.MESSAGE ->
            NotificationTypeConfig(
                icon = Icons.AutoMirrored.Filled.Chat,
                label = "Chat",
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                iconColor = MaterialTheme.colorScheme.primary
            )
        NotificationType.POST_REPLY ->
            NotificationTypeConfig(
                icon = Icons.Default.QuestionAnswer,
                label = "Reply",
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                iconColor = MaterialTheme.colorScheme.secondary
            )
        NotificationType.POST_ACCEPTED ->
            NotificationTypeConfig(
                icon = Icons.Default.CheckCircle,
                label = "Accepted",
                backgroundColor = Color(0xFFE8F5E9), // Light green
                iconColor = Color(0xFF4CAF50) // Green
            )
        NotificationType.POST_REJECTED ->
            NotificationTypeConfig(
                icon = Icons.Default.Close,
                label = "Rejected",
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                iconColor = MaterialTheme.colorScheme.error
            )
        NotificationType.NEW_MATCHING_POST ->
            NotificationTypeConfig(
                icon = Icons.Default.NewReleases,
                label = "New Post",
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                iconColor = MaterialTheme.colorScheme.tertiary
            )
    }
}

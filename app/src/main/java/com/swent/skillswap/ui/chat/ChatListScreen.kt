// AI-Generated: Chat list screen with post-based conversations and filtering
// This file implements a chat list interface that displays conversations with posts instead of
// users.
// Features include filtering by Offer/Request post types, stable UI with proper component
// architecture,
// and integration with existing Post and User models from the codebase.
package com.swent.skillswap.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.user.User

object ChatListTestTags {
    const val SCREEN = "ChatListScreen"
    const val TITLE = "ChatListTitle"
    const val OFFER = "OfferFilterButton"
    const val REQUEST = "RequestFilterButton"
    const val POSTS_LIST = "PostsList"
    const val EMPTY_STATE = "EmptyState"
}

/**
 * Chat list screen that displays conversations with posts instead of users. Shows posts in
 * rectangles with username on left and skill on right. Includes filtering by Offer/Request post
 * types.
 */
@Composable
fun ChatListScreen(
    posts: List<Post> = emptyList(),
    users: Map<String, User> = emptyMap(),
    onPostClick: (Post) -> Unit = {}
) {
    var selectedPostType by remember { mutableStateOf(PostType.OFFER) }

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

        // Post Type Filter Buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PostTypeFilterButton(
                text = "Offer",
                isSelected = selectedPostType == PostType.OFFER,
                onClick = { selectedPostType = PostType.OFFER },
                modifier = Modifier.weight(1f).testTag(ChatListTestTags.OFFER)
            )

            PostTypeFilterButton(
                text = "Request",
                isSelected = selectedPostType == PostType.REQUEST,
                onClick = { selectedPostType = PostType.REQUEST },
                modifier = Modifier.weight(1f).testTag(ChatListTestTags.REQUEST)
            )
        }

        // Posts List
        val filteredPosts =
            remember(posts, selectedPostType) { posts.filter { it.type == selectedPostType } }

        if (filteredPosts.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize().testTag(ChatListTestTags.EMPTY_STATE),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No ${selectedPostType.name.lowercase()} posts available",
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
                items(filteredPosts) { post ->
                    PostConversationItem(
                        post = post,
                        user = users[post.ownerId],
                        onClick = { onPostClick(post) }
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
fun PostConversationItem(
    post: Post,
    user: User?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Username
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.username ?: "Unknown User",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right side - Skill/Tags
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(
                    text = "Skills:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
                Text(
                    text =
                        if (post.tags.isEmpty()) {
                            "No skills listed"
                        } else {
                            post.tags.take(2).joinToString(", ") {
                                it.toString().replace("_", " ").lowercase().replaceFirstChar { char
                                    ->
                                    if (char.isLowerCase()) char.titlecase() else char.toString()
                                }
                            }
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

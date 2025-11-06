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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.user.User

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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Title
        Text(
            text = "Chat",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
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
                modifier = Modifier.weight(1f)
            )

            PostTypeFilterButton(
                text = "Request",
                isSelected = selectedPostType == PostType.REQUEST,
                onClick = { selectedPostType = PostType.REQUEST },
                modifier = Modifier.weight(1f)
            )
        }

        // Posts List
        val filteredPosts =
            remember(posts, selectedPostType) { posts.filter { it.type == selectedPostType } }

        if (filteredPosts.isEmpty()) {
            // Empty state
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No ${selectedPostType.name.lowercase()} posts available",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                containerColor = if (isSelected) Color(0xFF0F3F66) else Color.Transparent
            ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color(0xFF0F3F66),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

/** Individual post conversation item */
@Composable
fun PostConversationItem(post: Post, user: User?, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F3F66)),
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = post.title,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right side - Skill/Tags
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(text = "Skills:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
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
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

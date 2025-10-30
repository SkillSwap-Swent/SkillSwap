// AI-Generated: Main chat screen with navigation and state management
package com.swent.skillswap.ui.chat

import androidx.compose.runtime.*

/** Main chat screen that manages navigation and state */
@Composable
fun ChatMainScreen() {
    // Sample data for demonstration
    val sampleUsers = ChatScreenData.getSampleUsers()
    val samplePosts = ChatScreenData.getSamplePosts()

    ChatScreen(
        posts = samplePosts,
        users = sampleUsers,
        onPostClick = { post ->
            // TODO: Navigate to individual chat with post
            println("Clicked on post: ${post.title}")
        }
    )
}

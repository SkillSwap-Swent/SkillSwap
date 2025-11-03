package com.swent.skillswap.model.offer

import androidx.compose.runtime.State
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostType

interface FeedController {
    /** The current post being displayed in the UI. */
    val currentPost: State<Post?>

    /** The thumbnail of the current post. */
    val currentThumbnail: State<Image?>

    /** The ID of the user interacting with the feed. */
    val userIdPerformingActions: String

    /** The type of posts to be displayed in the feed. */
    val feedType: PostType

    /** Accepts the current post with a message. */
    suspend fun acceptPost(message: String)

    /** Skips the current post. */
    suspend fun skipPost()

    /** Fetches the thumbnail for a given ID. */
    suspend fun getThumbnail(thumbnailId: String)
}

package com.swent.skillswap.model.feed

import androidx.compose.runtime.State
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.User

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
    /**
     * add a report to the specified post
     *
     * @param offerId the postId that we reported
     * @param postType the post type either request or offer
     */
    suspend fun reportPost(postId: String, postType: PostType)

    suspend fun updateDistanceFilter(distance: Float)

    suspend fun updateLocation(isLiveLocationOn: Boolean)

    suspend fun inferRelevantSkill(): Skill
    /**
     * block a specific user for the user performing the action
     *
     * @param blockedUserUID uid of the user being blocked
     */
    suspend fun blockUser(blockedUserUID: String)

    suspend fun retrieveUser(post: Post): User

    /** Fetches a user by their UID. or an Exception if the user does not exist */
    suspend fun getUser(uid: String): User
}

package com.swent.skillswap.model.offer

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.Timestamp
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostReply
import com.swent.skillswap.model.post.PostRepository
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.post.Request

interface FeedController

internal class FeedControllerImpl(
    private val recommendationEngine: RecommendationEngine,
    private val thumbnailRepository: ThumbnailRepository,
    private val postRepository: PostRepository,
    private val chatRepository: ChatRepository,
    val userIdPerformingActions: String,
    val feedType: PostType,
) : FeedController {

    // Keep a couple of posts preloaded in advance
    private val preloadThreshold = 3
    private val postQueue: MutableList<Post> = mutableListOf()

    // The current post being displayed in the UI
    private val _currentPost = mutableStateOf<Post?>(null)
    val currentPost: State<Post?> = _currentPost
    private val _currentThumbnail = mutableStateOf<Image?>(null)
    val currentThumbnail: State<Image?> = _currentThumbnail

    /**
     * Initializes the controller by preloading the first batch of posts. This should be called from
     * a CoroutineScope, typically in a ViewModel.
     */
    suspend fun initialLoad() {
        fetchPosts()
        _currentPost.value = getNextPost()
    }

    suspend fun acceptPost(message: String) {
        val post =
            when (_currentPost.value) {
                is Request -> _currentPost.value as Request
                else -> throw Exception("A post is not currently selected")
            }

        val postReply =
            PostReply(
                postId = post.uid,
                ownerId = userIdPerformingActions,
                creation = Timestamp.now(),
                message = message,
                postType = feedType
            )

        postRepository.editPost(post.uid, post.copy(postReplies = post.postReplies + postReply))
        // TODO: send a chat message with reply
        // TODO: Update recommendation engine

        _currentPost.value = getNextPost()
    }

    suspend fun skipPost() {
        // TODO: Update recommendation engine
        _currentPost.value = getNextPost()
    }

    suspend fun getThumbnail(thumbnailId: String) {
        // TODO: Implement logic to fetch thumbnail
        _currentThumbnail.value = Image()
    }

    private suspend fun fetchPosts() {
        // Fetch posts and add them to the queue
        val newPosts = postRepository.getMultiplePosts(10, feedType)
        postQueue.addAll(newPosts)
    }

    /**
     * Retrieves the next post from the queue.
     *
     * @param onPreloadNeeded A lambda function that will be invoked if the queue needs refilling.
     *   This allows the caller (e.g., a ViewModel) to decide when and how to launch the background
     *   fetch operation.
     * @return The next [Post] or null if the queue is empty and cannot be refilled.
     */
    private suspend fun getNextPost(): Post? {
        if (postQueue.isEmpty()) {
            fetchPosts()
        }
        if (postQueue.isEmpty()) {
            return null
        }
        if (postQueue.size <= preloadThreshold) {
            fetchPosts()
        }

        return postQueue.removeAt(0)
    }
}

class RecommendationEngine

class ThumbnailRepository

class ChatRepository

class Image

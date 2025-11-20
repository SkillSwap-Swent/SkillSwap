package com.swent.skillswap.model.feed

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.Timestamp
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostReply
import com.swent.skillswap.model.post.PostRepository
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.post.ReplyStatus
import com.swent.skillswap.model.post.Request
import com.swent.skillswap.model.utils.LocationManager

const val NUMB_POSTS_TO_FETCH = 10L
const val PRELOAD_THRESHOLD = 3

private class FeedControllerImpl(
    private val recommendationEngine: RecommendationEngine,
    private val thumbnailRepository: ThumbnailRepository,
    private val postRepository: PostRepository,
    private val chatRepository: ChatRepository,
    override val userIdPerformingActions: String,
    override val feedType: PostType,
    private val locationManager: LocationManager?
) : FeedController {
    private val postQueue: MutableList<Post> = mutableListOf()

    // The current post being displayed in the UI
    private val _currentPost = mutableStateOf<Post?>(null)
    override val currentPost: State<Post?> = _currentPost
    private val _currentThumbnail = mutableStateOf<Image?>(null)
    override val currentThumbnail: State<Image?> = _currentThumbnail

    // Use EPFL location as reference by default
    private val currentUserLocation = mutableStateOf(LocationManager.DEFAULT_LOCATION)
    private val maxDistance = mutableFloatStateOf(0f)

    /**
     * Initializes the controller by preloading the first batch of posts. This should be called from
     * a CoroutineScope, typically in a ViewModel.
     */
    suspend fun initialLoad() {
        fetchPosts()
        _currentPost.value = getNextPost()
    }

    override suspend fun updateDistanceFilter(distance: Float) {
        maxDistance.floatValue = distance
        initialLoad()
    }

    override suspend fun acceptPost(message: String) {
        val post =
            when (_currentPost.value) {
                is Request -> _currentPost.value as Request
                else -> return
            }

        val postReply =
            PostReply(
                postId = post.uid,
                ownerId = userIdPerformingActions,
                creation = Timestamp.now(),
                message = message,
                postType = feedType,
                replyStatus = ReplyStatus.PROPOSED
            )

        postRepository.editPost(post.uid, post.copy(postReplies = post.postReplies + postReply))
        // TODO: send a chat message with reply
        // TODO: Update recommendation engine

        _currentPost.value = getNextPost()
    }

    override suspend fun skipPost() {
        // TODO: Update recommendation engine
        _currentPost.value = getNextPost()
    }

    override suspend fun getThumbnail(thumbnailId: String) {
        // TODO: Implement logic to fetch thumbnail
        _currentThumbnail.value = Image()
    }

    override suspend fun updateLocation(isLiveLocationOn: Boolean) {
        when (isLiveLocationOn) {
            true -> {
                if (locationManager != null)
                    currentUserLocation.value = locationManager.getCurrentLocationSync()
            }
            false -> {
                currentUserLocation.value = LocationManager.DEFAULT_LOCATION
                Log.d("FeedController", "Using default location")
            }
        }
        initialLoad()
    }

    private suspend fun fetchPosts() {
        // Fetch posts and add them to the queue
        val newPosts =
            postRepository.getMultiplePosts(
                NUMB_POSTS_TO_FETCH,
                feedType,
                userLocation = currentUserLocation.value,
                maxDistanceKm = maxDistance.floatValue
            )
        postQueue.addAll(newPosts)
    }

    /**
     * Retrieves the next post from the queue.
     *
     * @return The next [Post] or null if the queue is empty and cannot be refilled.
     */
    private suspend fun getNextPost(): Post? {
        if (postQueue.isEmpty()) {
            fetchPosts()
        }
        if (postQueue.isEmpty()) {
            return null
        }
        if (postQueue.size <= PRELOAD_THRESHOLD) {
            fetchPosts()
        }

        return postQueue.removeAt(0)
    }
}

/**
 * A factory responsible for creating instances of [FeedController]. This is useful for dependency
 * injection and separating creation logic from the ViewModel.
 */
class FeedControllerFactory(
    private val recommendationEngine: RecommendationEngine,
    private val thumbnailRepository: ThumbnailRepository,
    private val postRepository: PostRepository,
    private val chatRepository: ChatRepository,
    private val locationManager: LocationManager?
) {
    /**
     * Creates a new instance of [FeedController].
     *
     * @param userIdPerformingActions The ID of the user interacting with the feed.
     * @param feedType The type of posts to be displayed in the feed.
     * @return A configured instance of [FeedController].
     */
    suspend fun create(userIdPerformingActions: String, feedType: PostType): FeedController {
        val fc =
            FeedControllerImpl(
                recommendationEngine = recommendationEngine,
                thumbnailRepository = thumbnailRepository,
                postRepository = postRepository,
                chatRepository = chatRepository,
                userIdPerformingActions = userIdPerformingActions,
                feedType = feedType,
                locationManager = locationManager
            )
        fc.initialLoad()
        return fc
    }
}

class RecommendationEngine

class ThumbnailRepository

class ChatRepository

class Image

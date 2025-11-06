package com.swent.skillswap.model.post

import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.tags.EveryTag
import com.swent.skillswap.model.user.calculateDistance
import kotlinx.coroutines.delay

// In-memory PostRepository implementation for testing. Provides deterministic behavior
// and allows testing success/failure cases without actual database interactions.
// Used for testing
class FakePostRepository : PostRepository {
    private val posts = mutableMapOf<String, Post>()
    private var uidCounter = 0
    private var shouldFailOnAdd = false
    private var shouldFailOnEdit = false
    private var shouldFailOnGet = false
    private var delayMillis = 0L

    // Preload posts for deterministic testing
    fun preloadPosts(vararg postsToPreload: Post) {
        posts.clear()
        postsToPreload.forEach { posts[it.uid] = it }
    }

    // Simulate failures for error testing
    fun setShouldFailOnAdd(fail: Boolean) {
        shouldFailOnAdd = fail
    }

    fun setShouldFailOnEdit(fail: Boolean) {
        shouldFailOnEdit = fail
    }

    fun setShouldFailOnGet(fail: Boolean) {
        shouldFailOnGet = fail
    }

    // Convenience method for setting all failure flags
    fun setShouldFail(fail: Boolean) {
        shouldFailOnAdd = fail
        shouldFailOnEdit = fail
        shouldFailOnGet = fail
    }

    // Set delay for async operations (to test loading states)
    fun setDelay(delayMs: Long) {
        delayMillis = delayMs
    }

    override fun getNewUid(type: PostType): String {
        return "test-${type.name.lowercase()}-${uidCounter++}"
    }

    override suspend fun getMultiplePosts(
        numberOfPosts: Long,
        type: PostType,
        titleContains: String,
        ownerId: String,
        paymentMethod: PaymentMethod,
        tags: Set<EveryTag>,
        status: PostStatus?,
        userLocation: GeoPoint?,
        maxDistanceKm: Double?
    ): List<Post> {
        var filteredPosts =
            posts.values
                .filter { it.type == type }
                .filter {
                    titleContains.isEmpty() || it.title.contains(titleContains, ignoreCase = true)
                }
                .filter { ownerId.isEmpty() || it.ownerId == ownerId }
                .filter { it.paymentMethod == paymentMethod }
                .filter { status == null || it.status == status }

        // Filter by distance if location and maxDistance are provided
        if (userLocation != null && maxDistanceKm != null) {
            filteredPosts =
                filteredPosts.filter { post ->
                    calculateDistance(userLocation, post.location) <= maxDistanceKm
                }
        }

        return filteredPosts.take(numberOfPosts.toInt())
    }

    override suspend fun getPost(type: PostType, postId: String): Post {
        if (delayMillis > 0) {
            delay(delayMillis)
        }
        if (shouldFailOnGet) {
            throw Exception("Simulated get failure")
        }
        return posts[postId] ?: throw Exception("Post not found: $postId")
    }

    override suspend fun addPost(post: Post) {
        if (delayMillis > 0) {
            delay(delayMillis)
        }
        if (shouldFailOnAdd) {
            throw Exception("Simulated add failure")
        }
        posts[post.uid] = post
    }

    override suspend fun editPost(postId: String, newPost: Post) {
        if (delayMillis > 0) {
            delay(delayMillis)
        }
        if (shouldFailOnEdit) {
            throw Exception("Simulated edit failure")
        }
        if (!posts.containsKey(postId)) {
            throw Exception("Cannot edit non-existent post: $postId")
        }
        posts[postId] = newPost
    }

    override suspend fun deletePost(type: PostType, postId: String) {
        posts.remove(postId)
    }

    // Test helpers
    fun getAddedPosts(): List<Post> = posts.values.toList()

    fun getPostById(id: String): Post? = posts[id]

    fun clear() {
        posts.clear()
        uidCounter = 0
    }
}

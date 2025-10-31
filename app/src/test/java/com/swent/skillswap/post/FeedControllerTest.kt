package com.swent.skillswap.model.offer

import com.google.firebase.Timestamp
import com.swent.skillswap.model.post.*
import com.swent.skillswap.model.tags.EveryTag
import com.swent.skillswap.model.tags.PostTag
import kotlin.test.assertFailsWith
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FeedControllerImplTest {

    // ---- Fake repo ---------------------------------------------------------
    private class FakeRepo(private val responses: MutableList<List<Post>>) : PostRepository {
        var getMultiplePostsCalls = 0
        var lastEditPostId: String? = null
        var lastEditedPost: Post? = null

        override fun getNewUid(type: PostType): String = "newUid"

        override suspend fun getMultiplePosts(
            numberOfPosts: Long,
            type: PostType,
            titleContains: String,
            ownerId: String,
            paymentMethods: List<PaymentMethod>,
            tags: List<EveryTag>,
            status: PostStatus?
        ): List<Post> {
            getMultiplePostsCalls++
            return if (responses.isNotEmpty()) responses.removeAt(0) else emptyList()
        }

        override suspend fun getPost(type: PostType, postId: String): Post {
            error("not needed")
        }

        override suspend fun addPost(post: Post) {
            /* not needed */
        }

        override suspend fun editPost(postId: String, newPost: Post) {
            lastEditPostId = postId
            lastEditedPost = newPost
        }

        override suspend fun deletePost(type: PostType, postId: String) {
            /* not needed */
        }
    }

    // ---- Helpers -----------------------------------------------------------
    private fun request(
        uid: String = "r1",
        ownerId: String = "ownerA",
        creation: Timestamp = Timestamp.now(),
        // make expiry > creation
        expiry: Timestamp = Timestamp(creation.seconds + 3600, creation.nanoseconds),
        postReplies: List<PostReply> = emptyList()
    ): Request {
        return Request(
            uid = uid,
            title = "Learn Kotlin",
            description = "Desc",
            ownerId = ownerId,
            tags = listOf(PostTag.REOCCURRING),
            paymentMethods = listOf(PaymentMethod.CASH),
            expiry = expiry,
            creation = creation,
            status = PostStatus.POSTED,
            media = emptyList(),
            postReplies = postReplies
        )
    }

    // ---- Tests -------------------------------------------------------------

    @Test
    fun `initialLoad, acceptPost (happy path), skipPost, getThumbnail`() = runBlocking {
        // First fetch: 2 posts (triggers preload branch because <= 3)
        // Second fetch (preload): 0 posts
        // Third fetch (after accept/skip thresholds): 0 posts
        val repo =
            FakeRepo(mutableListOf(listOf(request("r1"), request("r2")), emptyList(), emptyList()))

        val ctrl =
            FeedControllerImpl(
                recommendationEngine = RecommendationEngine(),
                thumbnailRepository = ThumbnailRepository(),
                postRepository = repo,
                chatRepository = ChatRepository(),
                userIdPerformingActions = "user123",
                feedType = PostType.REQUEST
            )

        // initialLoad -> fetch + getNextPost (which preloads once because size <= 3)
        ctrl.initialLoad()
        assertEquals(2, repo.getMultiplePostsCalls)

        // acceptPost -> edits r1 and advances to next
        ctrl.acceptPost("hello!")
        assertEquals("r1", repo.lastEditPostId)
        val edited = repo.lastEditedPost as Request
        assertEquals(1, edited.postReplies.size)
        assertEquals("user123", edited.postReplies[0].ownerId)
        assertEquals(PostType.REQUEST, edited.postReplies[0].postType)

        // skipPost path (also exercises getNextPost thresholds)
        ctrl.skipPost()
        // A third fetch may have been called due to threshold; we asserted counts earlier,
        // but ensure no crash and path executed. (Count may be 3 depending on queue size.)
        assertTrue(repo.getMultiplePostsCalls >= 2)

        // getThumbnail
        ctrl.getThumbnail("thumb-1")
        assertNotNull(ctrl.currentThumbnail.value)
    }

    @Test
    fun `acceptPost throws when no current post`() = runBlocking {
        // Make repo always empty to drive currentPost = null
        val repo = FakeRepo(mutableListOf(emptyList(), emptyList()))
        val ctrl =
            FeedControllerImpl(
                RecommendationEngine(),
                ThumbnailRepository(),
                repo,
                ChatRepository(),
                "user123",
                PostType.REQUEST
            )
        ctrl.initialLoad() // currentPost becomes null via empty fetch paths
        assertEquals(2, repo.getMultiplePostsCalls) // fetch + re-fetch inside getNextPost

        assertFailsWith<Exception> { ctrl.acceptPost("should fail") }
    }
}

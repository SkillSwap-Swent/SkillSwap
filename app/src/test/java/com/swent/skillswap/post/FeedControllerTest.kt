package com.swent.skillswap.model.offer

import com.swent.skillswap.model.post.FakePostRepository
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.post.Request
import com.swent.skillswap.post.PostDataClassTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

open class FeedControllerTest : PostDataClassTest() {

    suspend fun initController(): Pair<FakePostRepository, FeedController> {
        val repo = FakePostRepository()
        repo.addPost(request1.copy(uid = "123"))
        repo.addPost(request1.copy(uid = "456"))

        val ctrl =
            FeedControllerFactory(
                    recommendationEngine = RecommendationEngine(),
                    thumbnailRepository = ThumbnailRepository(),
                    postRepository = repo,
                    chatRepository = ChatRepository()
                )
                .create(userIdPerformingActions = "user123", feedType = PostType.REQUEST)

        return Pair(repo, ctrl)
    }

    @Test
    fun controllerInit() {
        runTest {
            val (repo, ctrl) = initController()

            // verify initialLoad happened + getNextPost will run another fetch since queue has less
            // than 3 (threshold) posts
            assertEquals(2, repo.getMultiplePostsCalls)
            assert(ctrl.currentPost.value != null)
        }
    }

    @Test
    fun controllerAccept() {
        runTest {
            val (repo, ctrl) = initController()

            // acceptPost -> edits r1 and advances to next
            ctrl.acceptPost("message")
            assertEquals("123", repo.lastEditedPost?.uid)
            val edited = repo.lastEditedPost as Request
            assertEquals(1, edited.postReplies.size)
            assertEquals("user123", edited.postReplies.first().ownerId)
            assertEquals(PostType.REQUEST, edited.postReplies.first().postType)
        }
    }

    @Test
    fun controllerSkip() {
        runTest {
            val (_, ctrl) = initController()
            assert(ctrl.currentPost.value != null)
            val firstPostUid = ctrl.currentPost.value!!.uid // assert non null after assert non null

            ctrl.skipPost()
            assertNotEquals(firstPostUid, ctrl.currentPost.value?.uid)
        }
    }

    @Test
    fun controllerRefills() {
        runTest {
            val (_, ctrl) = initController()
            assert(ctrl.currentPost.value != null)
            val firstPostUid = ctrl.currentPost.value!!.uid // assert non null after assert non null

            ctrl.skipPost()
            ctrl.skipPost()
            // repo only had 2 posts to start, so the first post should now be shown again since the
            // automatic queue fetch will just loop back on the posts
            // maybe in the future showing the same posts again is not desired behaviour
            assertEquals(firstPostUid, ctrl.currentPost.value?.uid)
        }
    }
}

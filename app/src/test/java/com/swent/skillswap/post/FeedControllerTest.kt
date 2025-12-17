package com.swent.skillswap.model.feed

import com.swent.skillswap.model.chat.Chat
import com.swent.skillswap.model.chat.ChatRepository
import com.swent.skillswap.model.chat.ChatStatus
import com.swent.skillswap.model.chat.Message
import com.swent.skillswap.model.post.FakePostRepository
import com.swent.skillswap.model.post.PostStatus
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.post.Request
import com.swent.skillswap.model.user.FakeUserRepository
import com.swent.skillswap.model.user.User
import com.swent.skillswap.post.PostDataClassTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

open class FeedControllerTest : PostDataClassTest() {

    private class FakeChatRepository(var chats: Map<PostType, List<Chat>> = emptyMap()) :
        ChatRepository {
        override suspend fun createChat(
            participants: List<String>,
            relatedPostId: String,
            relatedPostType: PostType
        ) = "chat1"

        override fun streamMessages(chatId: String): Flow<List<Message>> = flowOf(emptyList())

        override suspend fun sendMessage(chatId: String, senderId: String, content: String) {}

        override suspend fun getChatsOfCurrentUser(relatedPostType: PostType) =
            chats[relatedPostType] ?: emptyList()

        override suspend fun getPendingChatsOfCurrentUser(relatedPostType: PostType): List<Chat> {
            // JUST HERE FOR OVERRIDE REASON
            return emptyList()
        }

        override suspend fun isOwnerOfRelatedPost(chat: Chat): Boolean {
            // JUST HERE FOR OVERRIDE REASON
            return false
        }

        override suspend fun getChat(chatId: String): Chat {
            // JUST HERE TO REMOVE OVERRIDE ERROR
            return Chat("mock", emptyList(), "", PostType.REQUEST, emptyList())
        }

        override suspend fun acceptAPostReplyChat(chat: Chat) {
            // JUST HERE FOR OVERRIDE REASON
        }

        override suspend fun closeChat(chatId: String) {
            val lastClosedChatId = chatId

            chats =
                chats
                    .mapValues { (_, chatList) ->
                        chatList.map { chat ->
                            if (chat.id == chatId)
                                Chat(
                                    chat.id,
                                    chat.participants,
                                    chat.relatedPostId,
                                    chat.relatedPostType,
                                    chat.messages,
                                    ChatStatus.INACTIVE
                                )
                            else chat
                        }
                    }
                    .toMutableMap()
        }
    }

    suspend fun initController(): Pair<FakePostRepository, FeedController> {
        val repo = FakePostRepository()
        repo.addPost(request1.copy(uid = "123"))
        repo.addPost(request1.copy(uid = "456"))

        val ctrl =
            FeedControllerFactory(
                    recommendationEngine = RecommendationEngineImpl(),
                    thumbnailRepository = ThumbnailRepository(),
                    postRepository = repo,
                    chatRepository = FakeChatRepository(),
                    userRepository = FakeUserRepository(),
                    locationManager = null
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
            assertNotNull(ctrl.currentPost.value)
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
            val firstPostUid = requireNotNull(ctrl.currentPost.value).uid

            ctrl.skipPost()
            assertNotEquals(firstPostUid, ctrl.currentPost.value?.uid)
        }
    }

    @Test
    fun updateLocationWithLiveLocationOn() {
        runTest {
            val repo = FakePostRepository()
            repo.addPost(request1.copy(uid = "123"))
            repo.addPost(request1.copy(uid = "456"))

            val ctrl =
                FeedControllerFactory(
                        recommendationEngine = RecommendationEngineImpl(),
                        thumbnailRepository = ThumbnailRepository(),
                        postRepository = repo,
                        chatRepository = FakeChatRepository(),
                        userRepository = FakeUserRepository(),
                        locationManager = null
                    )
                    .create(userIdPerformingActions = "user123", feedType = PostType.REQUEST)

            val initialFetchCalls = repo.getMultiplePostsCalls

            ctrl.updateLocation(isLiveLocationOn = true)

            // Verify that fetchPosts was called again (queue cleared and refilled)
            assertEquals(initialFetchCalls + 2, repo.getMultiplePostsCalls)
            assertNotNull(ctrl.currentPost.value)
        }
    }

    @Test
    fun updateReportCountByReportingRequest() {
        runTest {
            val repo = FakePostRepository()
            repo.addPost(request1.copy(uid = "123"))
            repo.addPost(request1.copy(uid = "456"))

            val ctrl =
                FeedControllerFactory(
                        recommendationEngine = RecommendationEngineImpl(),
                        thumbnailRepository = ThumbnailRepository(),
                        postRepository = repo,
                        chatRepository = FakeChatRepository(),
                        locationManager = null,
                        userRepository = FakeUserRepository()
                    )
                    .create(userIdPerformingActions = "user123", feedType = PostType.REQUEST)
            ctrl.reportPost("123", PostType.REQUEST)
            ctrl.reportPost("456", PostType.REQUEST)
            ctrl.reportPost("456", PostType.REQUEST)
            assertEquals(2L, repo.getPost(PostType.REQUEST, "456").reportCount)
            assertEquals(1L, repo.getPost(PostType.REQUEST, "123").reportCount)
        }
    }

    @Test
    fun updateLocationWithLiveLocationOff() {
        runTest {
            val repo = FakePostRepository()
            repo.addPost(request1.copy(uid = "123"))
            repo.addPost(request1.copy(uid = "456"))

            val ctrl =
                FeedControllerFactory(
                        recommendationEngine = RecommendationEngineImpl(),
                        thumbnailRepository = ThumbnailRepository(),
                        postRepository = repo,
                        chatRepository = FakeChatRepository(),
                        userRepository = FakeUserRepository(),
                        locationManager = null
                    )
                    .create(userIdPerformingActions = "user123", feedType = PostType.REQUEST)

            val initialFetchCalls = repo.getMultiplePostsCalls

            ctrl.updateLocation(isLiveLocationOn = false)

            // Verify that fetchPosts was called again (queue cleared and refilled)
            assertEquals(initialFetchCalls + 2, repo.getMultiplePostsCalls)
            assertNotNull(ctrl.currentPost.value)
        }
    }

    @Test
    fun canBlockUser() {
        runTest {
            val repo = FakeUserRepository()
            val uid1 = "User1"
            val uid2 = "User2"
            val chatRepo =
                FakeChatRepository(
                    mapOf(
                        PostType.REQUEST to
                            listOf(
                                Chat(
                                    "1",
                                    listOf(uid1, uid2),
                                    "2",
                                    PostType.REQUEST,
                                    emptyList(),
                                    ChatStatus.ACTIVE
                                )
                            )
                    )
                )
            repo.addUser(User(uid = uid1))
            repo.addUser(User(uid = uid2))
            val ctrl =
                FeedControllerFactory(
                        recommendationEngine = RecommendationEngineImpl(),
                        thumbnailRepository = ThumbnailRepository(),
                        postRepository = FakePostRepository(),
                        chatRepository = chatRepo,
                        userRepository = repo,
                        locationManager = null
                    )
                    .create(userIdPerformingActions = uid1, feedType = PostType.REQUEST)

            ctrl.blockUser(uid2)

            // Verify that the user uid2 as been correctly blocked by user uid1
            assert(repo.getUser(uid1).blockedUsers.isNotEmpty())
            assert(repo.getUser(uid1).blockedUsers.contains(uid2))
            assert(
                chatRepo.getChatsOfCurrentUser(PostType.REQUEST).get(0).status ==
                    ChatStatus.INACTIVE
            )
        }
    }

    @Test
    fun doNotShowCompletedPost() {
        runTest {
            val repo = FakePostRepository()
            repo.addPost(request1.copy(uid = "123"))
            repo.addPost(request1.copy(uid = "456", status = PostStatus.COMPLETED))

            val ctrl =
                FeedControllerFactory(
                        recommendationEngine = RecommendationEngineImpl(),
                        thumbnailRepository = ThumbnailRepository(),
                        postRepository = repo,
                        chatRepository = FakeChatRepository(),
                        userRepository = FakeUserRepository(),
                        locationManager = null
                    )
                    .create(userIdPerformingActions = "user123", feedType = PostType.REQUEST)

            assertNotNull(ctrl.currentPost.value)
            ctrl.skipPost()
            assertNull(ctrl.currentPost.value)
        }
    }
}

/* With the help of Sonnet 4.5 for repetitive tasks */

package com.swent.skillswap.ui.chat

import com.swent.skillswap.model.chat.Chat
import com.swent.skillswap.model.chat.ChatRepository
import com.swent.skillswap.model.chat.Message
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostRepository
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepositery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatListViewModelTest {

    private lateinit var viewModel: ChatListViewModel
    private val chat = Chat("c1", listOf("u1", "u2"), "p1", PostType.OFFER, emptyList())
    private val user = User("u1", "John", "", "", emptySet(), 0f, emptyList())
    private val post =
        object : Post {
            override val uid = "p1"
            override val title = "Test"
            override val description = ""
            override val ownerId = ""
            override val skills = emptySet<SkillTag>()
            override val tags = emptySet<PostTag>()
            override val paymentMethod = com.swent.skillswap.model.post.PaymentMethod.SKILLS
            override val expiry = com.google.firebase.Timestamp.now()
            override val creation = com.google.firebase.Timestamp.now()
            override val status = com.swent.skillswap.model.post.PostStatus.POSTED
            override val media = emptyList<String>()
            override val location = com.google.firebase.firestore.GeoPoint(0.0, 0.0)
            override val type = PostType.OFFER
            override val postReplies = emptyList<com.swent.skillswap.model.post.PostReply>()
            override val reportCount: Long = 0L
            override val searchKeys = listOf<String>()
        }

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel =
            ChatListViewModel(
                object : ChatRepository {
                    override suspend fun createChat(
                        participants: List<String>,
                        relatedPostId: String,
                        relatedPostType: PostType
                    ) = ""

                    override fun streamMessages(chatId: String) = flowOf(emptyList<Message>())

                    override suspend fun sendMessage(
                        chatId: String,
                        senderId: String,
                        content: String
                    ) {}

                    override suspend fun getChatsOfCurrentUser(relatedPostType: PostType) =
                        listOf(chat)
                },
                object : UserRepositery {
                    override fun getNewUid() = ""

                    override suspend fun getUser(userID: String) = user

                    override suspend fun addUser(user: User) {}

                    override suspend fun editUser(userID: String, newValue: User) {}

                    override suspend fun deleteUser(userID: String) {}

                    override suspend fun userExists(userId: String) = true

                    override suspend fun updateFcmToken(userId: String, fcmToken: String) {}
                },
                object : PostRepository {
                    override fun getNewUid(type: PostType) = ""

                    override suspend fun getMultiplePosts(
                        numberOfPosts: Long,
                        type: PostType,
                        titleContains: String,
                        ownerId: String,
                        paymentMethod: com.swent.skillswap.model.post.PaymentMethod?,
                        skills: Set<SkillTag>,
                        tags: Set<PostTag>,
                        status: com.swent.skillswap.model.post.PostStatus?,
                        userLocation: com.google.firebase.firestore.GeoPoint?,
                        maxDistanceKm: Float
                    ) = emptyList<Post>()

                    override suspend fun getPost(type: PostType, postId: String) = post

                    override suspend fun addPost(post: Post) {}

                    override suspend fun editPost(postId: String, newPost: Post) {}

                    override suspend fun deletePost(type: PostType, postId: String) {}
                }
            )
    }

    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun initialState_isEmpty() {
        assertTrue(viewModel.uiState.value.chats.isEmpty())
        assertTrue(viewModel.uiState.value.usernames.isEmpty())
    }

    @Test
    fun getChatsOfCurrentUser_updatesState() = runTest {
        viewModel.getChatsOfCurrentUser(PostType.OFFER)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.chats.size)
    }

    @Test
    fun getUsername_addsToMap() = runTest {
        viewModel.getUsername("u1")
        advanceUntilIdle()
        assertEquals("John", viewModel.uiState.value.usernames["u1"])
    }

    @Test
    fun getPostTitle_addsToMap() = runTest {
        viewModel.getPostTitle("p1", PostType.OFFER)
        advanceUntilIdle()
        assertEquals("Test", viewModel.uiState.value.postTitles["p1"])
    }
}

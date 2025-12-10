/* With the help of Sonnet 4.5 for repetitive tasks */

package com.swent.skillswap.ui.chat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.swent.skillswap.model.chat.Chat
import com.swent.skillswap.model.chat.ChatRepository
import com.swent.skillswap.model.chat.Message
import com.swent.skillswap.model.notification.FakeNotificationRepository
import com.swent.skillswap.model.post.FakePostRepository
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostRepository
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepositery
import com.swent.skillswap.ui.notification.NotificationViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
    private lateinit var failingViewModel: ChatListViewModel
    private val chat1 = Chat("c1", listOf("u1", "u2"), "p1", PostType.OFFER, emptyList())
    private val chat2 = Chat("c2", listOf("u1", "u2"), "p2", PostType.REQUEST, emptyList())
    private val user = User("u1", "John", "", "", emptySet(), 0f, emptyList())
    private var acceptedChat: MutableList<Chat> = mutableListOf()
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
                        listOf(chat1)

                    override suspend fun getPendingChatsOfCurrentUser(
                        relatedPostType: PostType
                    ): List<Chat> {
                        if (relatedPostType == PostType.REQUEST) {
                            return listOf(chat2)
                        } else {
                            return emptyList()
                        }
                    }

                    override suspend fun isOwnerOfRelatedPost(chat: Chat): Boolean {
                        return chat == chat1
                    }

                    override suspend fun acceptAPostReplyChat(chat: Chat) {
                        acceptedChat.add(chat)
                    }

                    override suspend fun getChat(chatId: String): Chat {
                        // JUST HERE TO REMOVE OVERRIDE ERROR
                        return Chat("mock", emptyList(), "", PostType.REQUEST, emptyList())
                    }
                },
                object : UserRepositery {
                    override fun getNewUid() = ""

                    override suspend fun getUser(userID: String) = user

                    override suspend fun addUser(user: User) {}

                    override suspend fun editUser(userID: String, newValue: User) {}

                    override suspend fun deleteUser(userID: String) {}

                    override suspend fun userExists(userId: String) = true

                    override suspend fun updateFcmToken(userId: String, fcmToken: String) {}

                    override suspend fun updateRating(userId: String, incomingRating: Float) {}
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

        failingViewModel =
            ChatListViewModel(
                FailingChatRepository(),
                FailingUserRepository(),
                FakePostRepository().apply { setShouldFailOnGet(true) }
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
    fun getUsername_AndAvatar_addsToMap() = runTest {
        viewModel.getUsernameAndAvatar("u1")
        advanceUntilIdle()
        assertEquals("John", viewModel.uiState.value.usernames["u1"])
    }

    @Test
    fun getPostTitle_addsToMap() = runTest {
        viewModel.getPostTitle("p1", PostType.OFFER)
        advanceUntilIdle()
        assertEquals("Test", viewModel.uiState.value.postTitles["p1"])
    }

    @Test
    fun getPendingChatWorkCorrectly() = runTest {
        viewModel.getChatsOfCurrentUser(PostType.REQUEST, true)
        advanceUntilIdle()
        assertEquals("c2", viewModel.uiState.value.chats[0].id)
        viewModel.getChatsOfCurrentUser(PostType.OFFER, true)
        advanceUntilIdle()
        assert(viewModel.uiState.value.chats.isEmpty())
    }

    @Test
    fun getChatOwnerByUserWorkCorrectly() = runTest {
        viewModel.getChatsOfCurrentUser(PostType.OFFER, false, true)
        advanceUntilIdle()
        assertEquals("c1", viewModel.uiState.value.chats[0].id)
        viewModel.getChatsOfCurrentUser(PostType.OFFER, isOwner = false)
        advanceUntilIdle()
        assert(viewModel.uiState.value.chats.isEmpty())
    }

    @Test
    fun acceptChatWorkCorrectly() = runTest {
        viewModel.acceptAPostReplyChat(chat1)
        assert(acceptedChat.contains(chat1))
        viewModel.acceptAPostReplyChat(chat2)
        assert(acceptedChat.contains(chat1))
        assert(acceptedChat.contains(chat2))
    }

    @Test
    fun getChatsOfCurrentUser_onError_setsErrorState() = runTest {
        failingViewModel.getChatsOfCurrentUser(PostType.OFFER)
        advanceUntilIdle()
        assertEquals("Error fetching chats", failingViewModel.uiState.value.error)
    }

    @Test
    fun getUsernameAndAvatar_onError_setsErrorState() = runTest {
        failingViewModel.getUsernameAndAvatar("u1")
        advanceUntilIdle()
        assertEquals("Error loading username and avatar", failingViewModel.uiState.value.error)
    }

    @Test
    fun getPostTitle_onError_setsErrorState() = runTest {
        failingViewModel.getPostTitle("p1", PostType.OFFER)
        advanceUntilIdle()
        assertEquals("Error loading post title", failingViewModel.uiState.value.error)
    }

    @Test
    fun acceptAPostReplyChat_createsPostAcceptedNotification() = runTest {
        // Mock FirebaseAuth
        mockkStatic(FirebaseAuth::class)
        val mockAuth = mockk<FirebaseAuth>(relaxed = true)
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "u1"
        every { FirebaseAuth.getInstance() } returns mockAuth

        val fakeNotificationRepository = FakeNotificationRepository()
        val notificationViewModel = NotificationViewModel(fakeNotificationRepository)

        // Create a new viewModel with notificationViewModel
        val viewModelWithNotifications =
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
                        emptyList()

                    override suspend fun getPendingChatsOfCurrentUser(
                        relatedPostType: PostType
                    ): List<Chat> = emptyList()

                    override suspend fun isOwnerOfRelatedPost(chat: Chat): Boolean = false

                    override suspend fun acceptAPostReplyChat(chat: Chat) {
                        acceptedChat.add(chat)
                    }

                    override suspend fun getChat(chatId: String): Chat {
                        return Chat("mock", emptyList(), "", PostType.REQUEST, emptyList())
                    }
                },
                object : UserRepositery {
                    override fun getNewUid() = ""

                    override suspend fun getUser(userID: String) = user

                    override suspend fun addUser(user: User) {}

                    override suspend fun editUser(userID: String, newValue: User) {}

                    override suspend fun deleteUser(userID: String) {}

                    override suspend fun userExists(userId: String) = true

                    override suspend fun updateFcmToken(userId: String, fcmToken: String) {}

                    override suspend fun updateRating(userId: String, incomingRating: Float) {}
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
                },
                notificationViewModel
            )

        // Create a chat where current user (u1) accepts reply from u2
        val chatWithOtherUser = Chat("c3", listOf("u1", "u2"), "p1", PostType.OFFER, emptyList())

        viewModelWithNotifications.acceptAPostReplyChat(chatWithOtherUser)
        advanceUntilIdle()

        // Verify notification was created
        val notifications = fakeNotificationRepository.getNotificationsForUser("u2")
        assertTrue(
            "Should create POST_ACCEPTED notification",
            notifications.any {
                it.type == com.swent.skillswap.model.notification.NotificationType.POST_ACCEPTED &&
                    it.relatedId == "p1" &&
                    it.userId == "u2"
            }
        )

        // Cleanup
        unmockkStatic(FirebaseAuth::class)
    }
}

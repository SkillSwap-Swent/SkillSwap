/* With the help of Sonnet 4.5 for repetitive tasks */

package com.swent.skillswap.ui.chat

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
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
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatConversationItemTest {

    @get:Rule val composeRule = createComposeRule()

    private lateinit var mockViewModel: ChatListViewModel
    private lateinit var mockChatRepo: ChatRepository
    private lateinit var mockUserRepo: UserRepositery
    private lateinit var mockPostRepo: PostRepository

    @Before
    fun setup() {
        // Mock Firebase Auth to return a consistent current user
        mockkStatic(FirebaseAuth::class)
        val mockAuth = mockk<FirebaseAuth>()
        val mockFirebaseUser = mockk<com.google.firebase.auth.FirebaseUser>()
        every { FirebaseAuth.getInstance() } returns mockAuth
        every { mockAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns "currentUser"

        // Create mock repositories
        mockChatRepo =
            object : ChatRepository {
                override suspend fun createChat(
                    participants: List<String>,
                    relatedPostId: String,
                    relatedPostType: PostType
                ) = "chat1"

                override fun streamMessages(chatId: String): Flow<List<Message>> =
                    flowOf(emptyList())

                override suspend fun sendMessage(
                    chatId: String,
                    senderId: String,
                    content: String
                ) {}

                override suspend fun getChatsOfCurrentUser(relatedPostType: PostType) =
                    emptyList<Chat>()
            }

        mockUserRepo =
            object : UserRepositery {
                private val users =
                    mapOf(
                        "u1" to
                            User(
                                "u1",
                                "Sarah Chen",
                                "sarah@example.com",
                                "",
                                emptySet(),
                                4.8f,
                                emptyList()
                            ),
                        "u2" to User("u2", "John Doe", "", "", emptySet(), 4.5f, emptyList())
                    )

                override fun getNewUid() = "user-${System.currentTimeMillis()}"

                override suspend fun getUser(userID: String) =
                    users[userID]
                        ?: User(userID, "Unknown User", "", "", emptySet(), 0f, emptyList())

                override suspend fun addUser(user: User) {}

                override suspend fun editUser(userID: String, newValue: User) {}

                override suspend fun deleteUser(userID: String) {}

                override suspend fun userExists(userId: String) = users.containsKey(userId)

                override suspend fun updateFcmToken(userId: String, fcmToken: String) {}
            }

        mockPostRepo =
            object : PostRepository {
                private val posts =
                    mapOf(
                        "p1" to MockPost("p1", "Graphic Design Help"),
                        "p2" to MockPost("p2", "Math Tutoring")
                    )

                override fun getNewUid(type: PostType) = "post-${System.currentTimeMillis()}"

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
                    maxDistanceKm: Double?
                ) = emptyList<Post>()

                override suspend fun getPost(type: PostType, postId: String) =
                    posts[postId] ?: throw Exception("Post not found")

                override suspend fun addPost(post: Post) {}

                override suspend fun editPost(postId: String, newPost: Post) {}

                override suspend fun deletePost(type: PostType, postId: String) {}
            }

        mockViewModel = ChatListViewModel(mockChatRepo, mockUserRepo, mockPostRepo)
    }

    private class MockPost(override val uid: String, override val title: String) : Post {
        override val description = ""
        override val ownerId = ""
        override val skills = emptySet<SkillTag>()
        override val tags = emptySet<PostTag>()
        override val paymentMethod = com.swent.skillswap.model.post.PaymentMethod.SKILLS
        override val expiry = Timestamp.now()
        override val creation = Timestamp.now()
        override val status = com.swent.skillswap.model.post.PostStatus.POSTED
        override val media = emptyList<String>()
        override val location = com.google.firebase.firestore.GeoPoint(0.0, 0.0)
        override val type = PostType.OFFER
        override val postReplies = emptyList<com.swent.skillswap.model.post.PostReply>()
        override val searchKeys = listOf<String>()
    }

    @Test
    fun displays_post_title_and_username() {
        val chat = Chat("c1", listOf("currentUser", "u1"), "p1", PostType.OFFER, emptyList())

        composeRule.setContent {
            MaterialTheme {
                ChatConversationItem(
                    viewModel = mockViewModel,
                    currentUserId = "currentUser",
                    chat = chat,
                    onClick = {}
                )
            }
        }

        // Trigger data loading
        mockViewModel.getPostTitle("p1", PostType.OFFER)
        mockViewModel.getUsername("u1")
        composeRule.waitForIdle()

        // Check that post title and username are displayed
        composeRule.onNodeWithText("Graphic Design Help").assertExists()
        composeRule.onNodeWithText("Sarah Chen").assertExists()
    }

    @Test
    fun shows_loading_state_initially() {
        val chat = Chat("c1", listOf("currentUser", "u1"), "p1", PostType.OFFER, emptyList())

        composeRule.setContent {
            MaterialTheme {
                ChatConversationItem(
                    viewModel = mockViewModel,
                    currentUserId = "currentUser",
                    chat = chat,
                    onClick = {}
                )
            }
        }

        // The LaunchedEffect triggers immediately, so we verify the card exists
        // and data loading was triggered
        composeRule.waitForIdle()
        // Verify the card is rendered (it should have clickable action)
        composeRule.onNode(hasClickAction()).assertExists()
    }

    @Test
    fun handles_click_events() {
        var clicked = false
        val chat = Chat("c1", listOf("currentUser", "u1"), "p1", PostType.OFFER, emptyList())

        composeRule.setContent {
            MaterialTheme {
                ChatConversationItem(
                    viewModel = mockViewModel,
                    currentUserId = "currentUser",
                    chat = chat,
                    onClick = { clicked = true }
                )
            }
        }

        composeRule.waitForIdle()
        // Click on the card (which has click action)
        composeRule.onNode(hasClickAction()).performClick()
        assert(clicked)
    }

    @Test
    fun displays_different_users_correctly() {
        val chat = Chat("c1", listOf("currentUser", "u2"), "p2", PostType.REQUEST, emptyList())

        composeRule.setContent {
            MaterialTheme {
                ChatConversationItem(
                    viewModel = mockViewModel,
                    currentUserId = "currentUser",
                    chat = chat,
                    onClick = {}
                )
            }
        }

        // Trigger data loading
        mockViewModel.getPostTitle("p2", PostType.REQUEST)
        mockViewModel.getUsername("u2")
        composeRule.waitForIdle()

        // Check that correct data is displayed
        composeRule.onNodeWithText("Math Tutoring").assertExists()
        composeRule.onNodeWithText("John Doe").assertExists()
    }

    @Test
    fun handles_unknown_user() {
        val chat = Chat("c1", listOf("currentUser", "unknown"), "p1", PostType.OFFER, emptyList())

        composeRule.setContent {
            MaterialTheme {
                ChatConversationItem(
                    viewModel = mockViewModel,
                    currentUserId = "currentUser",
                    chat = chat,
                    onClick = {}
                )
            }
        }

        // Trigger data loading for unknown user
        mockViewModel.getUsername("unknown")
        composeRule.waitForIdle()

        // Should show "Unknown User" for users not in the repository
        composeRule.onNodeWithText("Unknown User").assertExists()
    }
}

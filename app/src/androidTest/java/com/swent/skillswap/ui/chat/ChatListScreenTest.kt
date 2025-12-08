/* With the help of Sonnet 4.5 for repetitive tasks */

package com.swent.skillswap.ui.chat

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.swent.skillswap.model.chat.Chat
import com.swent.skillswap.model.chat.ChatRepository
import com.swent.skillswap.model.chat.ChatStatus
import com.swent.skillswap.model.chat.Message
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostRepository
import com.swent.skillswap.model.post.PostStatus
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepositery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatListScreenTest {

    @get:Rule val composeRule = createComposeRule()

    // Minimal fake implementations
    private class FakeChatRepository(private val chats: Map<PostType, List<Chat>> = emptyMap()) :
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
            // CURRENTLY HERE FOR OVERRIDE REASON WILL BE IMPLEMENTED WHEN CHAT SCREEN CHANGE
            return emptyList()
        }

        override suspend fun isOwnerOfRelatedPost(chat: Chat): Boolean {
            // CURRENTLY HERE FOR OVERRIDE REASON WILL BE IMPLEMENTED WHEN CHAT SCREEN CHANGE
            return false
        }

        override suspend fun acceptAPostReplyChat(chat: Chat) {
            // CURRENTLY HERE FOR OVERRIDE REASON WILL BE IMPLEMENTED WHEN CHAT SCREEN CHANGE
        }

        override suspend fun getChat(chatId: String): Chat {
            // JUST HERE TO REMOVE OVERRIDE ERROR
            return Chat("mock", emptyList(), "", PostType.REQUEST, emptyList())
        }
    }

    private class FakeUserRepository(private val users: Map<String, User>) : UserRepositery {
        override fun getNewUid() = "user-${System.currentTimeMillis()}"

        override suspend fun getUser(userID: String) =
            users[userID] ?: User(userID, "Unknown User", "", "", emptySet(), 0f, emptyList())

        override suspend fun addUser(user: User) {}

        override suspend fun editUser(userID: String, newValue: User) {}

        override suspend fun deleteUser(userID: String) {}

        override suspend fun userExists(userId: String) = users.containsKey(userId)

        override suspend fun updateFcmToken(userId: String, fcmToken: String) {}

        override suspend fun updateRating(userId: String, incomingRating: Float) {}
    }

    private class FakePostRepository(private val posts: Map<String, Post>) : PostRepository {
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
            maxDistanceKm: Float
        ) = emptyList<Post>()

        override suspend fun getPost(type: PostType, postId: String) =
            posts[postId] ?: throw Exception("Post not found")

        override suspend fun addPost(post: Post) {}

        override suspend fun editPost(postId: String, newPost: Post) {}

        override suspend fun deletePost(type: PostType, postId: String) {}
    }

    // Test data factories
    private fun createChat(id: String, postId: String, type: PostType, user: String = "u2") =
        Chat(id, listOf("u1", user), postId, type, emptyList())

    private fun createViewModel(
        offerChats: List<Chat> = emptyList(),
        requestChats: List<Chat> = emptyList(),
        users: Map<String, User> = emptyMap(),
        posts: Map<String, Post> = emptyMap()
    ) =
        ChatListViewModel(
            FakeChatRepository(
                mapOf(PostType.OFFER to offerChats, PostType.REQUEST to requestChats)
            ),
            FakeUserRepository(users),
            FakePostRepository(posts)
        )

    // Minimal mock posts for titles
    private class MockPost(
        override val uid: String,
        override val title: String,
    ) : Post {
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
        override val reportCount: Long = 0L
    }

    @Test
    fun shows_filters_and_empty_state_by_default() {
        composeRule.setContent {
            MaterialTheme { ChatListScreen(viewModel = createViewModel(), currentUserId = "u1") }
        }
        composeRule.onNodeWithText("Chat").assertExists()
        composeRule.onNodeWithText("Offer").assertExists()
        composeRule.onNodeWithText("Request").assertExists()
        composeRule.onNodeWithText("No chats available").assertExists()
    }

    @Test
    fun displays_offer_chats_and_switches_to_request_chats() {
        val offerChat = createChat("c1", "p1", PostType.OFFER)
        val requestChat = createChat("c2", "p2", PostType.REQUEST)
        val users = mapOf("u2" to User("u2", "Sarah", "", "", emptySet(), 4.5f, emptyList()))
        val posts =
            mapOf("p1" to MockPost("p1", "Offer Title"), "p2" to MockPost("p2", "Request Title"))

        val viewModel =
            createViewModel(
                offerChats = listOf(offerChat),
                requestChats = listOf(requestChat),
                users = users,
                posts = posts
            )

        composeRule.setContent {
            MaterialTheme { ChatListScreen(viewModel = viewModel, currentUserId = "u1") }
        }

        // Trigger initial load for offers
        viewModel.getChatsOfCurrentUser(PostType.OFFER)
        viewModel.getUsername("u2")
        viewModel.getPostTitle("p1", PostType.OFFER)
        composeRule.waitForIdle()

        // Check offer content
        composeRule.onNodeWithText("Sarah").assertExists()
        composeRule.onNodeWithText("Offer Title").assertExists()

        // Switch to requests
        composeRule.onNodeWithText("Request").performClick()
        viewModel.getChatsOfCurrentUser(PostType.REQUEST)
        viewModel.getPostTitle("p2", PostType.REQUEST)
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Request Title").assertExists()
    }

    @Test
    fun chat_click_triggers_callback() {
        var clickedChatId = ""
        val chat = createChat("c1", "p1", PostType.OFFER)
        val viewModel = createViewModel(offerChats = listOf(chat))

        composeRule.setContent {
            MaterialTheme {
                ChatListScreen(
                    viewModel = viewModel,
                    currentUserId = "u1",
                    onChatClick = { clickedChatId = it }
                )
            }
        }

        viewModel.getChatsOfCurrentUser(PostType.OFFER)
        composeRule.waitForIdle()

        // Click any card (filter buttons are also clickable, so get the last one which is the chat)
        val clickableNodes = composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes()
        composeRule.onAllNodes(hasClickAction())[clickableNodes.size - 1].performClick()
        assert(clickedChatId == "c1")
    }

    @Test
    fun handles_loading_state_for_usernames_and_titles() {
        val chat = createChat("c1", "p1", PostType.OFFER)
        val viewModel = createViewModel(offerChats = listOf(chat))

        composeRule.setContent {
            MaterialTheme { ChatListScreen(viewModel = viewModel, currentUserId = "u1") }
        }

        viewModel.getChatsOfCurrentUser(PostType.OFFER)
        composeRule.waitForIdle()

        // With fake repos, verify chat item is rendered (at least 3: 2 filter buttons + 1 chat)
        assert(composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size >= 3)
    }

    @Test
    fun empty_state_changes_based_on_filter() {
        val requestChat = createChat("c1", "p1", PostType.REQUEST)
        val viewModel = createViewModel(requestChats = listOf(requestChat))

        composeRule.setContent {
            MaterialTheme { ChatListScreen(viewModel = viewModel, currentUserId = "u1") }
        }

        // Default is offers - should be empty
        viewModel.getChatsOfCurrentUser(PostType.OFFER)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("No chats available").assertExists()

        // Switch to requests - should have content
        composeRule.onNodeWithText("Request").performClick()
        viewModel.getChatsOfCurrentUser(PostType.REQUEST)
        composeRule.waitForIdle()
        assert(composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size >= 3)
    }

    @Test
    fun multiple_chats_render_correctly() {
        val chats =
            listOf(
                createChat("c1", "p1", PostType.OFFER, "u2"),
                createChat("c2", "p2", PostType.OFFER, "u3")
            )
        val users =
            mapOf(
                "u2" to User("u2", "User Two", "", "", emptySet(), 4.5f, emptyList()),
                "u3" to User("u3", "User Three", "", "", emptySet(), 4.8f, emptyList())
            )
        val posts =
            mapOf("p1" to MockPost("p1", "First Post"), "p2" to MockPost("p2", "Second Post"))

        val viewModel = createViewModel(offerChats = chats, users = users, posts = posts)

        composeRule.setContent {
            MaterialTheme { ChatListScreen(viewModel = viewModel, currentUserId = "u1") }
        }

        viewModel.getChatsOfCurrentUser(PostType.OFFER)
        chats.forEach { chat ->
            val otherUser = chat.participants.first { it != "u1" }
            viewModel.getUsername(otherUser)
            viewModel.getPostTitle(chat.relatedPostId, chat.relatedPostType)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("User Two").assertExists()
        composeRule.onNodeWithText("User Three").assertExists()
        composeRule.onNodeWithText("First Post").assertExists()
        composeRule.onNodeWithText("Second Post").assertExists()
    }

    @Test
    fun rating_button_shows_for_completed_post_and_dialog_submits_rating() {
        val chat =
            Chat("c1", listOf("u1", "u2"), "p1", PostType.OFFER, emptyList(), ChatStatus.ACTIVE)
        val post =
            object : Post by MockPost("p1", "Test") {
                override val status = PostStatus.COMPLETED
            }
        val viewModel = createViewModel(offerChats = listOf(chat), posts = mapOf("p1" to post))

        composeRule.setContent {
            MaterialTheme { ChatListScreen(viewModel = viewModel, currentUserId = "u1") }
        }
        composeRule.waitForIdle()

        // Rating button should exist and open dialog
        composeRule.onNodeWithContentDescription("Rate User").assertExists().performClick()
        composeRule.onNodeWithText("Rate this exchange").assertExists()

        // Select 4 stars and submit
        composeRule.onAllNodesWithContentDescription("rating stars")[3].performClick()
        composeRule.onNodeWithText("Submit").performClick()
        composeRule.onNodeWithText("Rate this exchange").assertDoesNotExist()
    }

    @Test
    fun rating_button_hidden_for_posted_status() {
        val chat =
            Chat("c1", listOf("u1", "u2"), "p1", PostType.OFFER, emptyList(), ChatStatus.ACTIVE)
        val post =
            object : Post by MockPost("p1", "Test") {
                override val status = PostStatus.POSTED
            }
        val viewModel = createViewModel(offerChats = listOf(chat), posts = mapOf("p1" to post))

        composeRule.setContent {
            MaterialTheme { ChatListScreen(viewModel = viewModel, currentUserId = "u1") }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Rate User").assertDoesNotExist()
    }

    @Test
    fun rating_dialog_cancel_dismisses() {
        val chat =
            Chat("c1", listOf("u1", "u2"), "p1", PostType.OFFER, emptyList(), ChatStatus.ACTIVE)
        val post =
            object : Post by MockPost("p1", "Test") {
                override val status = PostStatus.ARCHIVED
            }
        val viewModel = createViewModel(offerChats = listOf(chat), posts = mapOf("p1" to post))

        composeRule.setContent {
            MaterialTheme { ChatListScreen(viewModel = viewModel, currentUserId = "u1") }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Rate User").performClick()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithText("Rate this exchange").assertDoesNotExist()
    }
}

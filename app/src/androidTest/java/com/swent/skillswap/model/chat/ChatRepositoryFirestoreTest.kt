/* Written with copilot to complete repetitive stuff and test skeleton */
package com.swent.skillswap.model.chat

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.firebase.FirestorePaths.CHATS_COLLECTION
import com.swent.skillswap.model.post.PaymentMethod
import com.swent.skillswap.model.post.PostFirestoreRepository
import com.swent.skillswap.model.post.PostStatus
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.post.Request
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.Preference
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.model.utils.deserializeMessage
import com.swent.skillswap.utils.FirebaseEmulator
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatRepositoryFirestoreTest {
    lateinit var chatRepo: ChatRepositoryFirestore
    lateinit var postRepo: PostFirestoreRepository
    lateinit var testUserId: String
    lateinit var db: FirebaseFirestore
    val postedPost =
        Request(
            uid = "post_posted",
            title = "Fix my bike",
            description = "Need help repairing a flat tire",
            ownerId = "u1",
            skills = setOf<SkillTag>(SkillTag.CALCULUS),
            tags = setOf<PostTag>(),
            paymentMethod = PaymentMethod.SKILLS,
            expiry = Timestamp(Timestamp.now().seconds + 172_800, Timestamp.now().nanoseconds),
            creation = Timestamp.now(),
            status = PostStatus.POSTED,
            media = emptyList<String>(),
            location = GeoPoint(0.0, 0.0),
            reportCount = 0L,
        )
    val completedPost =
        Request(
            uid = "post_completed",
            title = "Teach guitar",
            description = "Offering beginner guitar lessons",
            ownerId = "u2",
            skills = setOf<SkillTag>(SkillTag.CALCULUS),
            tags = setOf<PostTag>(),
            paymentMethod = PaymentMethod.CASH,
            expiry = Timestamp(Timestamp.now().seconds + 172_800, Timestamp.now().nanoseconds),
            creation = Timestamp.now(),
            status = PostStatus.COMPLETED,
            media = emptyList<String>(),
            location = GeoPoint(1.0, 1.0),
            reportCount = 0L,
        )
    val requestPost =
        Request(
            uid = "post_request",
            title = "Fix my bike",
            description = "Need help repairing a flat tire",
            ownerId = "u1",
            skills = setOf(SkillTag.CALCULUS),
            tags = setOf(),
            paymentMethod = PaymentMethod.SKILLS,
            expiry = Timestamp(Timestamp.now().seconds + 172_800, Timestamp.now().nanoseconds),
            creation = Timestamp.now(),
            status = PostStatus.COMPLETED,
            media = emptyList<String>(),
            location = GeoPoint(0.0, 0.0),
            reportCount = 0L,
        )

    // Clean the users collection before each test
    @Before
    fun setUp() {
        runBlocking {
            FirebaseEmulator.startEmulator()
            db = FirebaseEmulator.firestore
            chatRepo = ChatRepositoryFirestore(db)
            postRepo = PostFirestoreRepository(db)
            val userRepository = UserRepoFirestore(db)
            testUserId = FirebaseEmulator.auth.signInAnonymously().await().user!!.uid

            val user =
                User(
                    uid = testUserId,
                    username = "TestUser",
                    email = "myTest@example.com",
                    profilePicture = "",
                    skillSet = emptySet(),
                    rating = 0f,
                    availability = emptyList(),
                    preference = Preference.SKILLS,
                    location = GeoPoint(0.0, 0.0),
                    blockedUsers = emptySet(),
                    fcmToken = null
                )
            val user2 =
                User(
                    uid = "u2",
                    username = "TestUser2",
                    email = "myTest2@example.com",
                    profilePicture = "",
                    skillSet = setOf(Skill(SkillTag.CALCULUS, 0f, "")),
                    rating = 0f,
                    availability = emptyList(),
                    preference = Preference.SKILLS,
                    location = GeoPoint(0.0, 0.0),
                    blockedUsers = emptySet(),
                    fcmToken = null
                )

            userRepository.addUser(user)
            userRepository.addUser(user2)
            postRepo.addPost(postedPost.copy(ownerId = testUserId))
            postRepo.addPost(completedPost)
            postRepo.addPost(requestPost.copy(ownerId = testUserId))
            val users = FirebaseEmulator.firestore.collection(CHATS_COLLECTION).get().await()
            for (doc in users.documents) {
                FirebaseEmulator.firestore
                    .collection(CHATS_COLLECTION)
                    .document(doc.id)
                    .delete()
                    .await()
            }
        }
    }

    @After
    fun cleanup() {
        FirebaseEmulator.clearFirestoreEmulator()
        FirebaseEmulator.clearAuthEmulator()
    }

    @Test
    fun sendNonFirstMessageUpdateChat() = runBlocking {
        val senderId1 = "user1"
        val content1 = "First message"
        val senderId2 = "user2"
        val content2 = "Second message"

        // Create chat and send first message
        val chatId = chatRepo.createChat(listOf("user1", "user2"), "none", PostType.REQUEST)
        chatRepo.sendMessage(chatId, senderId1, content1)

        // Send second message
        chatRepo.sendMessage(chatId, senderId2, content2)

        // Verify both messages are in chat
        val document = db.collection(CHATS_COLLECTION).document(chatId).get().await()
        assert(document.exists())
        val messages = document.get("messages") as? List<*>
        assertNotNull(messages)
        assertEquals(2, messages!!.size)

        val firstMessage = deserializeMessage(messages[0] as String)
        assertEquals(senderId1, firstMessage.senderId)
        assertEquals(content1, firstMessage.content)
        assertTrue(firstMessage.timestamp > 0L)

        val secondMessage = deserializeMessage(messages[1] as String)
        assertEquals(senderId2, secondMessage.senderId)
        assertEquals(content2, secondMessage.content)
        assertTrue(secondMessage.timestamp > 0L)
    }

    @Test
    fun streamMessageReactAtNewMessages() = runTest {
        val senderId1 = "user1"
        val content1 = "First streamed message"
        val senderId2 = "user2"
        val content2 = "Second streamed message"

        // create chat first
        val chatId = chatRepo.createChat(listOf("user1", "user2"), "none", PostType.REQUEST)

        val messagesReceived = mutableListOf<List<Message>>()
        val job = launch {
            chatRepo.streamMessages(chatId).collect { messages -> messagesReceived.add(messages) }
        }

        kotlinx.coroutines.delay(500) // wait for flow to start

        // Send first message
        chatRepo.sendMessage(chatId, senderId1, content1)
        kotlinx.coroutines.delay(500) // wait for flow to collect

        // Send second message
        chatRepo.sendMessage(chatId, senderId2, content2)
        kotlinx.coroutines.delay(500) // wait for flow to collect

        job.cancel()

        // Verify received messages
        assertTrue(messagesReceived.size >= 2)
    }

    @Test
    fun getChatsOfCurrentUser_filtersAndReturnsCorrectChats() = runBlocking {
        val requestChat =
            chatRepo.createChat(listOf(testUserId, "other1"), requestPost.uid, PostType.REQUEST)

        // Test filtering by REQUEST type
        val requestChats = chatRepo.getChatsOfCurrentUser(PostType.REQUEST)
        assertEquals(1, requestChats.size)
        assertEquals(requestChat, requestChats[0].id)

        // Clean up
        FirebaseEmulator.auth.signOut()
    }

    @Test
    fun getPendingChat_WorkCorrectly() = runBlocking {
        val chat1 =
            chatRepo.createChat(listOf(testUserId, "other1"), postedPost.uid, PostType.REQUEST)
        val chat2 =
            chatRepo.createChat(listOf(testUserId, "other1"), requestPost.uid, PostType.REQUEST)
        val chat3 =
            chatRepo.createChat(listOf(testUserId, "other1"), completedPost.uid, PostType.REQUEST)
        val pendingChats = chatRepo.getPendingChatsOfCurrentUser(PostType.REQUEST)
        assertEquals(1, pendingChats.size)
        assertEquals(chat1, pendingChats[0].id)
    }

    @Test
    fun isOwnerOfPostRelatedTo_Chat_Work_Correctly() = runBlocking {
        val chat1 =
            chatRepo.createChat(listOf(testUserId, "other1"), postedPost.uid, PostType.REQUEST)
        val chat2 =
            chatRepo.createChat(listOf(testUserId, "other1"), requestPost.uid, PostType.REQUEST)
        val chat3 =
            chatRepo.createChat(listOf(testUserId, "other1"), completedPost.uid, PostType.REQUEST)
        val chats = chatRepo.getChatsOfCurrentUser(PostType.REQUEST)
        val pendingChats = chatRepo.getPendingChatsOfCurrentUser(PostType.REQUEST)
        for (c in chats) {
            assertEquals(c.relatedPostId == requestPost.uid, chatRepo.isOwnerOfRelatedPost(c))
        }
        for (c in pendingChats) {
            assertEquals(c.relatedPostId == postedPost.uid, chatRepo.isOwnerOfRelatedPost(c))
        }
    }

    @Test
    fun acceptChat_Work_Correctly() = runBlocking {
        val chat1 =
            chatRepo.createChat(listOf(testUserId, "other1"), postedPost.uid, PostType.REQUEST)
        val pendingChats = chatRepo.getPendingChatsOfCurrentUser(PostType.REQUEST)
        for (c in pendingChats) {
            if (c.relatedPostId == postedPost.uid) {
                chatRepo.acceptAPostReplyChat(c)
            }
        }
        assertEquals(
            PostStatus.COMPLETED,
            postRepo.getPost(PostType.REQUEST, postedPost.uid).status
        )
        val chats = chatRepo.getChatsOfCurrentUser(PostType.REQUEST)
        assert(chats.isNotEmpty())
    }

    @Test
    fun acceptChat_Throw_Correctly_onError() = runBlocking {
        val chat1 =
            chatRepo.createChat(listOf(testUserId, "other1"), postedPost.uid, PostType.REQUEST)
        val pendingChats = chatRepo.getPendingChatsOfCurrentUser(PostType.REQUEST)
        postRepo.deletePost(PostType.REQUEST, postedPost.uid)
        for (c in pendingChats) {
            if (c.relatedPostId == postedPost.uid) {
                assert(
                    try {
                        chatRepo.acceptAPostReplyChat(c)
                        false
                    } catch (e: Exception) {
                        true
                    }
                )
            }
        }
    }

    @Test
    fun isOwnerOfPostRelatedTo_Chat_Throw_Correctly_onError() = runBlocking {
        val chat1 =
            chatRepo.createChat(listOf(testUserId, "other1"), postedPost.uid, PostType.REQUEST)
        val chat2 =
            chatRepo.createChat(listOf(testUserId, "other1"), requestPost.uid, PostType.REQUEST)
        val chat3 =
            chatRepo.createChat(listOf(testUserId, "other1"), completedPost.uid, PostType.REQUEST)
        val chats = chatRepo.getChatsOfCurrentUser(PostType.REQUEST)
        val pendingChats = chatRepo.getPendingChatsOfCurrentUser(PostType.REQUEST)
        postRepo.deletePost(PostType.REQUEST, completedPost.uid)
        postRepo.deletePost(PostType.REQUEST, requestPost.uid)
        postRepo.deletePost(PostType.REQUEST, postedPost.uid)
        for (c in chats) {
            assert(
                try {
                    chatRepo.isOwnerOfRelatedPost(c)
                    false
                } catch (e: Exception) {
                    true
                }
            )
        }
        for (c in pendingChats) {
            assert(
                try {
                    chatRepo.isOwnerOfRelatedPost(c)
                    false
                } catch (e: Exception) {
                    true
                }
            )
        }
    }

    @Test
    fun getPendingChat_Throw_Correctly_onError() = runBlocking {
        val chat1 =
            chatRepo.createChat(listOf(testUserId, "other1"), postedPost.uid, PostType.REQUEST)
        val chat2 =
            chatRepo.createChat(listOf(testUserId, "other1"), requestPost.uid, PostType.REQUEST)
        val chat3 =
            chatRepo.createChat(listOf(testUserId, "other1"), completedPost.uid, PostType.REQUEST)
        postRepo.deletePost(PostType.REQUEST, postedPost.uid)
        assert(
            try {
                chatRepo.getPendingChatsOfCurrentUser(PostType.REQUEST)
                false
            } catch (e: Exception) {
                true
            }
        )
    }

    @Test
    fun getChat_fetches_correct_chat_and_handles_errors() = runBlocking {
        val senderId1 = "user1"
        val senderId2 = "user2"

        // create chat first
        val chatId = chatRepo.createChat(listOf("user1", "user2"), "none", PostType.REQUEST)
        val fetchedChat = chatRepo.getChat(chatId)

        // Check chat properties
        assertEquals(chatId, fetchedChat.id)
        assertEquals(listOf(senderId1, senderId2), fetchedChat.participants)
        assertEquals("none", fetchedChat.relatedPostId)
        assertEquals(PostType.REQUEST, fetchedChat.relatedPostType)

        // Check for raised exception on invalid chatId

        val invalidChatId = "nonexistent_chat_id"
        val exception =
            assertThrows(Exception::class.java) { runBlocking { chatRepo.getChat(invalidChatId) } }

        assertTrue(
            exception.message!!.contains(
                "Error while fetching chat in getChat: Chat with ID nonexistent_chat_id does not exist"
            )
        )
    }

    @Test
    fun correctly_close_chat() = runBlocking {
        val senderId1 = "user1"
        val senderId2 = "user2"

        // create chat first
        val chatId = chatRepo.createChat(listOf("user1", "user2"), "none", PostType.REQUEST)
        var fetchedChat = chatRepo.getChat(chatId)

        // Check chat properties
        assertEquals(chatId, fetchedChat.id)
        assertEquals(listOf(senderId1, senderId2), fetchedChat.participants)
        assertEquals(ChatStatus.ACTIVE, fetchedChat.status)
        assertEquals("none", fetchedChat.relatedPostId)
        assertEquals(PostType.REQUEST, fetchedChat.relatedPostType)

        // Close Chat
        chatRepo.closeChat(chatId)
        fetchedChat = chatRepo.getChat(chatId)
        assertEquals(ChatStatus.INACTIVE, fetchedChat.status)
    }
}

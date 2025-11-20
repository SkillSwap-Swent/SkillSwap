/* Written with copilot to complete repetitive stuff and test skeleton */
package com.swent.skillswap.model.chat

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.skillswap.firebase.FirestorePaths.CHATS_COLLECTION
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.utils.deserializeMessage
import com.swent.skillswap.utils.FirebaseEmulator
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatRepositoryFirestoreTest {
    lateinit var repo: ChatRepositoryFirestore
    lateinit var db: FirebaseFirestore

    init {
        FirebaseEmulator.startEmulator()
        db = FirebaseEmulator.firestore // get the firestore instance pointing to the emulator
        repo = ChatRepositoryFirestore(db) // initialize the repository
    }

    // Clean the users collection before each test
    @Before
    fun setUp() = runBlocking {
        val users = FirebaseEmulator.firestore.collection(CHATS_COLLECTION).get().await()
        for (doc in users.documents) {
            FirebaseEmulator.firestore
                .collection(CHATS_COLLECTION)
                .document(doc.id)
                .delete()
                .await()
        }
    }

    @Test
    fun sendNonFirstMessageUpdateChat() = runBlocking {
        val senderId1 = "user1"
        val content1 = "First message"
        val senderId2 = "user2"
        val content2 = "Second message"

        // Create chat and send first message
        val chatId = repo.createChat(listOf("user1", "user2"), "none", PostType.REQUEST)
        repo.sendMessage(chatId, senderId1, content1)

        // Send second message
        repo.sendMessage(chatId, senderId2, content2)

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
    fun sendMessageWithInvalidParametersThrowsException() = runBlocking {
        val chatId = "chat4"
        val validSenderId = "user1"
        val validContent = "Valid message"

        // Test with empty chatId
        try {
            repo.sendMessage("", validSenderId, validContent)
            assert(false) // Should not reach here
        } catch (e: IllegalArgumentException) {
            assertEquals("chatUid cannot be empty", e.message)
        }

        // Test with empty senderId
        try {
            repo.sendMessage(chatId, "", validContent)
            assert(false) // Should not reach here
        } catch (e: IllegalArgumentException) {
            assertEquals("senderId cannot be empty", e.message)
        }

        // Test with empty content
        try {
            repo.sendMessage(chatId, validSenderId, "")
            assert(false) // Should not reach here
        } catch (e: IllegalArgumentException) {
            assertEquals("content cannot be empty", e.message)
        }
    }

    @Test
    fun streamMessageReactAtNewMessages() = runTest {
        val senderId1 = "user1"
        val content1 = "First streamed message"
        val senderId2 = "user2"
        val content2 = "Second streamed message"

        // create chat first
        val chatId = repo.createChat(listOf("user1", "user2"), "none", PostType.REQUEST)

        val messagesReceived = mutableListOf<List<Message>>()
        val job = launch {
            repo.streamMessages(chatId).collect { messages -> messagesReceived.add(messages) }
        }

        kotlinx.coroutines.delay(500) // wait for flow to start

        // Send first message
        repo.sendMessage(chatId, senderId1, content1)
        kotlinx.coroutines.delay(500) // wait for flow to collect

        // Send second message
        repo.sendMessage(chatId, senderId2, content2)
        kotlinx.coroutines.delay(500) // wait for flow to collect

        job.cancel()

        // Verify received messages
        assertTrue(messagesReceived.size >= 2)
    }
}

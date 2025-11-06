package com.swent.skillswap.model.chat

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.skillswap.firebase.FirestorePaths.CHATS_COLLECTION
import com.swent.skillswap.utils.FirebaseEmulator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

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
    fun sendFirstMessageCreatesChat() = runBlocking {
        val chatId = "chat2"
        val message =
            Message(
                id = "msg1",
                senderId = "user1",
                content = "Hello, this is the first message!",
                timestamp = System.currentTimeMillis()
            )
        // verify chat does not exist
        val preDocument = db.collection(CHATS_COLLECTION).document(chatId).get().await()
        assert(!preDocument.exists())

        // Send message (should create chat)
        repo.sendMessage(chatId, message)

        // Verify chat and message
        val document = db.collection(CHATS_COLLECTION).document(chatId).get().await()
        assert(document.exists())
        val messages = document.get("messages") as? List<*>
        assert(messages != null && messages[0] == serializeMessage(message))
    }

    @Test
    fun sendNonFirstMessageUpdateChat() = runBlocking {
        val chatId = "chat3"
        val firstMessage =
            Message(
                id = "msg1",
                senderId = "user1",
                content = "First message",
                timestamp = System.currentTimeMillis()
            )
        val secondMessage =
            Message(
                id = "msg2",
                senderId = "user2",
                content = "Second message",
                timestamp = System.currentTimeMillis()
            )

        // Create chat and send first message
        repo.sendMessage(chatId, firstMessage)

        // Send second message
        repo.sendMessage(chatId, secondMessage)

        // Verify both messages are in chat
        val document = db.collection(CHATS_COLLECTION).document(chatId).get().await()
        assert(document.exists())
        val messages = document.get("messages") as? List<*> // todo tester serialization
        assert(
            messages != null &&
                messages[0] == serializeMessage(firstMessage) &&
                messages[1] == serializeMessage(secondMessage)
        )
    }

    @Test
    fun sendMessageWithInvalidParametersThrowsException() = runBlocking {
        val chatId = "chat4"
        val validMessage =
            Message(
                id = "msg1",
                senderId = "user1",
                content = "Valid message",
                timestamp = System.currentTimeMillis()
            )

        // Test with empty chatId
        try {
            repo.sendMessage("", validMessage)
            assert(false) // Should not reach here
        } catch (e: IllegalArgumentException) {
            assertEquals("chatUid cannot be empty", e.message)
        }

        // Test with empty senderId
        val invalidMessage1 =
            Message(
                id = "msg2",
                senderId = "",
                content = "Invalid senderId",
                timestamp = System.currentTimeMillis()
            )
        try {
            repo.sendMessage(chatId, invalidMessage1)
            assert(false) // Should not reach here
        } catch (e: IllegalArgumentException) {
            assertEquals("senderUid cannot be empty", e.message)
        }

        // Test with empty content
        val invalidMessage2 =
            Message(
                id = "msg3",
                senderId = "user2",
                content = "",
                timestamp = System.currentTimeMillis()
            )
        try {
            repo.sendMessage(chatId, invalidMessage2)
            assert(false) // Should not reach here
        } catch (e: IllegalArgumentException) {
            assertEquals("message content cannot be empty", e.message)
        }

        // Test with non-positive timestamp
        val invalidMessage3 =
            Message(id = "msg4", senderId = "user3", content = "Invalid timestamp", timestamp = 0L)
        try {
            repo.sendMessage(chatId, invalidMessage3)
            assert(false) // Should not reach here
        } catch (e: IllegalArgumentException) {
            assertEquals("timestamp must be positive", e.message)
        }
    }
}

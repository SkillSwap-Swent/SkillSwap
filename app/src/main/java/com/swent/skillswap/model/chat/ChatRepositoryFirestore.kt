package com.swent.skillswap.model.chat

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.skillswap.firebase.FirestorePaths
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class ChatRepositoryFirestore(private val db: FirebaseFirestore) : ChatRepository {

    /**
     * Creates a new chat with the specified participants.
     *
     * @param chatId The ID of the chat to create
     * @throws IllegalArgumentException if chat cannot be created
     */
    suspend fun createChat(chatId: String) {
        /** Precondition */
        require(chatId.isNotBlank()) { "new chatUid cannot be empty" }

        /** Create chat */
        try {
            val document = db.collection(FirestorePaths.CHATS_COLLECTION).document(chatId)
            val newChat = mapOf("messages" to emptyList<Message>())
            document.set(newChat).await()
        } catch (e: Exception) {
            throw Exception("Error while creating chat in createChat: ${e.message}")
        }
    }

    /**
     * override fun getChat(chatUid: String): Chat { /** Preconditions */
     * require(chatUid.isNotEmpty()) { "chatUid cannot be empty" }
     *
     * /** Get chat */ return try { val document =
     * db.collection(FirestorePaths.CHATS_COLLECTION).document(chatUid).get().await()
     * document.toObject(Chat::class.java) //TODO deserialization ?: throw Exception("Chat not found
     * with uid: $chatUid")
     *
     * } catch (e: Exception) { throw Exception("Error while getting chat in getChat: ${e.message}")
     * } }
     */
    override suspend fun sendMessage(chatId: String, message: Message) {
        /** Preconditions */
        require(chatId.isNotEmpty()) { "chatUid cannot be empty" }
        require(message.senderId.isNotEmpty()) { "senderUid cannot be empty" }
        require(message.content.isNotEmpty()) { "message content cannot be empty" }
        require(message.timestamp > 0) { "timestamp must be positive" }

        /** Send message */
        try {
            val document = db.collection(FirestorePaths.CHATS_COLLECTION).document(chatId)
            if (!document.get().await().exists()) {
                createChat(chatId)
            }
            // Append the new message to the existing messages list
            document.update("messages", FieldValue.arrayUnion(serializeMessage(message))).await()
        } catch (e: Exception) {
            throw Exception("Error while sending message in sendMessage: ${e.message}")
        }
    }
    /*
    override fun streamMessages(chatId: String) = callbackFlow {
        val document = db.collection(FirestorePaths.CHATS_COLLECTION).document(chatUid)
        document.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("Listen failed: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val chat = snapshot.toObject(Chat::class.java) // TODO deserialization
                trySend(chat)
            } else {
                println("Current chat data: null")
            }

        }
    }*/
    override fun streamMessages(chatId: String): Flow<List<Message>> {
        TODO("Being implemented")
    }
}

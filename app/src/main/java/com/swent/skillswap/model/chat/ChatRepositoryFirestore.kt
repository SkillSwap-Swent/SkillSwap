package com.swent.skillswap.model.chat

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.skillswap.firebase.FirestorePaths
import java.util.UUID
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepositoryFirestore(private val db: FirebaseFirestore) : ChatRepository {

    /**
     * Creates a new chat with the specified participants.
     *
     * @param chatId The ID of the chat to create
     * @throws IllegalArgumentException if chat cannot be created
     */
    private suspend fun createChat(chatId: String) {
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
     * Generates a unique message ID.
     *
     * @return A unique message ID as a String
     */
    fun getMessageId(): String {
        return UUID.randomUUID().toString()
    }

    override suspend fun sendMessage(chatId: String, senderId: String, content: String) {
        /** Preconditions */
        require(chatId.isNotEmpty()) { "chatUid cannot be empty" }
        require(senderId.isNotEmpty()) { "senderId cannot be empty" }
        require(content.isNotEmpty()) { "content cannot be empty" }

        /** Send message */
        val message =
            Message(
                id = getMessageId(),
                senderId = senderId,
                content = content,
                timestamp = System.currentTimeMillis()
            )
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

    override fun streamMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        require(chatId.isNotEmpty()) { "chatUid cannot be empty" }
        val document = db.collection(FirestorePaths.CHATS_COLLECTION).document(chatId)

        val registration =
            document.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    snapshot.get("messages")?.let { data ->
                        val messagesList =
                            (data as? List<*>)?.mapNotNull {
                                deserializeMessage((it as? String) ?: return@mapNotNull null)
                            } ?: return@addSnapshotListener

                        /** send the fetched messages list to the flow */
                        trySend(messagesList)
                    }
                } else {
                    /** Chat document does not exist */
                    trySend(emptyList())
                }
            }
        /** Clean up listener on flow cancellation */
        awaitClose { registration.remove() }
    }
}

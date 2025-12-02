package com.swent.skillswap.model.chat

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.skillswap.firebase.FirestorePaths
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.utils.deserializeMessage
import com.swent.skillswap.model.utils.serializeMessage
import java.util.UUID
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firestore implementation of the ChatRepository interface.
 *
 * The structure of this component of the database is as follows:
 * - Collection: "chats"
 *     - Document: "{chatId}"
 *     - Field: Chat (object)
 *
 * @property db The FirebaseFirestore instance for database operations
 */
class ChatRepositoryFirestore(private val db: FirebaseFirestore) : ChatRepository {

    override suspend fun createChat(
        participants: List<String>,
        relatedPostId: String,
        relatedPostType: PostType
    ): String {
        /** Precondition */
        require(participants.size == NUMBER_OF_CHAT_PARTICIPANTS) {
            "participants list cannot be empty"
        }
        require(relatedPostId.isNotEmpty()) { "relatedPostId cannot be empty" }

        val chatId = generateUniqueId()
        /** Create chat */
        try {
            val document = db.collection(FirestorePaths.CHATS_COLLECTION).document(chatId)
            val newChat =
                mapOf(
                    "id" to chatId,
                    "participants" to participants,
                    "relatedPostId" to relatedPostId,
                    "relatedPostType" to relatedPostType,
                    "messages" to emptyList<Message>()
                )
            document.set(newChat).await()
        } catch (e: Exception) {
            throw Exception("Error while creating chat in createChat: ${e.message}")
        }
        return chatId
    }

    /**
     * Generates a unique message ID.
     *
     * @return A unique message ID as a String
     */
    private fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }

    override suspend fun sendMessage(chatId: String, senderId: String, content: String) {
        /** Preconditions */
        require(chatId.isNotEmpty()) { "chatUid cannot be empty" }
        require(senderId.isNotEmpty()) { "senderId cannot be empty" }
        require(content.isNotEmpty()) { "content cannot be empty" }

        /** Build and send message */
        val message =
            Message(
                id = generateUniqueId(),
                senderId = senderId,
                content = content,
                timestamp = System.currentTimeMillis()
            )
        try {
            val document = db.collection(FirestorePaths.CHATS_COLLECTION).document(chatId)
            if (!document.get().await().exists()) {
                throw Exception("Chat with ID $chatId does not exist")
            }
            // Append the new message to the existing chat's messages list
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

    override suspend fun getChatsOfCurrentUser(relatedPostType: PostType): List<Chat> {

        val currentUserId =
            try {
                Firebase.auth.currentUser?.uid ?: throw Exception("No authenticated user found")
            } catch (e: Exception) {
                throw Exception("Error while retrieving current user ID: ${e.message}")
            }

        val allDocs =
            try {
                db.collection(FirestorePaths.CHATS_COLLECTION)
                    .whereArrayContains("participants", currentUserId)
                    .get()
                    .await()
            } catch (e: Exception) {
                throw Exception("Error while fetching chats in getChatsOfCurrentUser: ${e.message}")
            }

        // Filter chats by related post type
        return allDocs
            .mapNotNull { doc ->
                try {
                    documentToChat(doc)
                } catch (e: Exception) {
                    null
                }
            }
            .filter { it.relatedPostType == relatedPostType }
    }

    private fun documentToChat(document: DocumentSnapshot): Chat {
        val id = document.getString("id") ?: ""
        val participants: List<String> =
            document.get("participants") as? List<String> ?: emptyList()
        val relatedPostId = document.getString("relatedPostId") ?: ""
        val relatedPostType = PostType.valueOf(document.getString("relatedPostType") ?: "REQUEST")
        val messagesData = document.get("messages") as? List<*> ?: emptyList<Any>()
        val messages =
            messagesData.mapNotNull {
                deserializeMessage((it as? String) ?: return@mapNotNull null)
            }
        return Chat(id, participants, relatedPostId, relatedPostType, messages)
    }

    override suspend fun getChat(chatId: String): Chat {
        try {
            val doc = db.collection(FirestorePaths.CHATS_COLLECTION).document(chatId).get().await()
            if (!doc.exists()) throw Exception("Chat with ID $chatId does not exist")
            return documentToChat(doc)
        } catch (e: Exception) {
            throw Exception("Error while fetching chat in getChat: ${e.message}")
        }
    }
}

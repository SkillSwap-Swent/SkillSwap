/**
 * Credits: Alexander Magnus, some helper methods created with the help of Gemini and ChatGPT
 *
 * Template: CS-311
 */
package com.swent.skillswap.model.post

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.swent.skillswap.model.tags.EveryTag
import kotlinx.coroutines.tasks.await

const val OFFERS_COLLECTION_PATH = "offers"
const val REQUESTS_COLLECTION_PATH = "requests"

class PostFirestoreRepository(private val db: FirebaseFirestore) : PostRepository {

    override fun getNewUid(type: PostType): String {
        return getCollectionPath(type).document().id
    }

    override suspend fun getMultiplePosts(
        numberOfPosts: Long,
        type: PostType,
        titleContains: String?,
        ownerId: String?,
        paymentMethods: List<PaymentMethod>?,
        tags: List<EveryTag>?,
        status: PostStatus?,
    ): List<Post> {
        val query: Query =
            buildQuery(type, ownerId, status, titleContains, paymentMethods, tags, numberOfPosts)

        return query.get().await().map { documentToPost(it) }.sortedByDescending { it.creation }
    }

    private fun buildQuery(
        type: PostType,
        ownerId: String?,
        status: PostStatus?,
        titleContains: String?,
        paymentMethods: List<PaymentMethod>?,
        tags: List<EveryTag>?,
        numberOfPosts: Long
    ): Query {
        var query: Query = getCollectionPath(type)

        // perform equal to filters
        if (ownerId != null) {
            query = query.whereEqualTo("ownerId", ownerId)
        }
        if (status != null) {
            query = query.whereEqualTo("status", status)
        }

        // perform complex seachKeys filter to bypass limit of single whereArrayContainsAny per
        // query
        val searchKeys = buildSearchKeys(titleContains, paymentMethods, tags)
        if (searchKeys.isNotEmpty()) {
            query = query.whereArrayContainsAny("searchKeys", searchKeys)
        }

        return query.limit(numberOfPosts)
    }

    private fun buildSearchKeys(
        titleContains: String?,
        paymentMethods: List<PaymentMethod>?,
        tags: List<EveryTag>?
    ): MutableList<String> {
        val searchKeys = mutableListOf<String>()
        titleContains?.let { it -> searchKeys.addAll(it.split(" ").map { it.lowercase() }) }
        paymentMethods?.let { it -> searchKeys.addAll(it.map { it.toString().lowercase() }) }
        tags?.let { it -> searchKeys.addAll(it.map { it.toString().lowercase() }) }
        return searchKeys
    }

    override suspend fun getPost(type: PostType, postId: String): Post {
        val document = getCollectionPath(type).document(postId).get().await()
        return documentToPost(document)
    }

    override suspend fun addPost(post: Post) {
        require(post.validate()) { "Post fields are invalid" }
        getCollectionPath(post.type).document(post.uid).set(post).await()
    }

    override suspend fun editPost(postId: String, newPost: Post) {
        require(newPost.validate()) { "Post fields are invalid" }

        getCollectionPath(newPost.type).document(postId).set(newPost).await()
    }

    override suspend fun deletePost(type: PostType, postId: String) {
        getCollectionPath(type).document(postId).delete().await()
    }

    private fun documentToPost(document: DocumentSnapshot): Post {
        val uid = document.id
        val title: String = document.getString("title") ?: throw IllegalArgumentException()
        val description: String =
            document.getString("description") ?: throw IllegalArgumentException()
        val ownerId: String = document.getString("ownerId") ?: throw IllegalArgumentException()

        @Suppress("UNCHECKED_CAST")
        val tags =
            (document.get("tags") as? List<String>)?.map { EveryTag.valueOf(it) }
                ?: throw IllegalArgumentException("Tags missing or invalid")

        @Suppress("UNCHECKED_CAST")
        val paymentMethods =
            (document.get("paymentMethods") as? List<String>)?.map { PaymentMethod.valueOf(it) }
                ?: throw IllegalArgumentException("Invalid or missing paymentMethods")

        val expiry = document.getTimestamp("expiry") ?: throw IllegalArgumentException()
        val creation = document.getTimestamp("creation") ?: throw IllegalArgumentException()

        val status =
            document.getString("status")?.let { PostStatus.valueOf(it) }
                ?: throw IllegalArgumentException("Invalid or missing post status")

        @Suppress("UNCHECKED_CAST")
        val media = document.get("media") as? List<String> ?: throw IllegalArgumentException()

        val postType =
            document.getString("type")?.let { PostType.valueOf(it) }
                ?: throw IllegalArgumentException("Invalid or missing post type")

        val post =
            when (postType) {
                PostType.REQUEST ->
                    Request(
                        uid,
                        title,
                        description,
                        ownerId,
                        tags,
                        paymentMethods,
                        expiry,
                        creation,
                        status,
                        media
                    )
                // TODO("Replace with Offer when it's implemented")
                PostType.OFFER -> throw NotImplementedError("Offer posts are not supported yet")
            }
        require(post.validate()) { "Post was not validated successfully" }
        return post
    }

    private fun getCollectionPath(type: PostType): CollectionReference {
        return when (type) {
            PostType.OFFER -> throw NotImplementedError("Offer posts are not supported yet")
            PostType.REQUEST -> db.collection(REQUESTS_COLLECTION_PATH)
        }
    }
}

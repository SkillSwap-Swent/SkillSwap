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
import com.google.firebase.firestore.SetOptions
import com.swent.skillswap.firebase.FirestorePaths
import com.swent.skillswap.firebase.FirestoreSettings
import com.swent.skillswap.model.offer.Offer
import com.swent.skillswap.model.tags.EveryTag
import kotlinx.coroutines.tasks.await

class PostFirestoreRepository(db: FirebaseFirestore) : PostRepository {

    val offersCollection = db.collection(FirestorePaths.OFFERS_COLLECTION)
    val requestsCollection = db.collection(FirestorePaths.REQUESTS_COLLECTION)

    override fun getNewUid(type: PostType): String {
        return getCollectionPath(type).document().id
    }

    override suspend fun getMultiplePosts(
        numberOfPosts: Long,
        type: PostType,
        titleContains: String,
        ownerId: String,
        paymentMethods: List<PaymentMethod>,
        tags: List<EveryTag>,
        status: PostStatus?,
    ): List<Post> {
        val query: Query =
            buildQuery(type, ownerId, status, titleContains, paymentMethods, tags, numberOfPosts)

        return query.get().await().map { documentToPost(it) }.sortedByDescending { it.creation }
    }

    private fun buildQuery(
        type: PostType,
        ownerId: String,
        status: PostStatus?,
        titleContains: String,
        paymentMethods: List<PaymentMethod>,
        tags: List<EveryTag>,
        numberOfPosts: Long
    ): Query {
        var query: Query = getCollectionPath(type)

        // perform equal to filters
        if (ownerId != "") {
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

    /**
     * Builds a list of search keys from the given title, payment methods, and tags. The search keys
     * are used to perform a complex search in Firestore.
     *
     * @param titleContains The title to search for.
     * @param paymentMethods The payment methods to filter by.
     * @param tags The tags to filter by.
     * @return A list of search keys. Note: Firestore's 'array-contains-any' is limited to a maximum
     *   of 10 elements in the comparison array, so this function returns at most 10 distinct search
     *   keys.
     */
    private fun buildSearchKeys(
        titleContains: String,
        paymentMethods: List<PaymentMethod>,
        tags: List<EveryTag>
    ): List<String> {
        val searchKeys = mutableListOf<String>()
        if (titleContains.isNotBlank())
            searchKeys.addAll(titleContains.split(" ").map { it.lowercase() })
        if (paymentMethods.isNotEmpty())
            searchKeys.addAll(paymentMethods.map { it.toString().lowercase() })
        if (tags.isNotEmpty()) searchKeys.addAll(tags.map { it.toString().lowercase() })
        return searchKeys.distinct().take(FirestoreSettings.MAX_SEARCH_KEYS)
    }

    override suspend fun getPost(type: PostType, postId: String): Post {
        val document = getCollectionPath(type).document(postId).get().await()
        return documentToPost(document)
    }

    override suspend fun addPost(post: Post) {
        require(post.validate()) { "Post fields are invalid" }

        val docRef = getCollectionPath(post.type).document(post.uid)
        val snapshot = docRef.get().await()

        require(!snapshot.exists()) { "Post with UID ${post.uid} already exists" }
        docRef.set(post).await()
    }

    override suspend fun editPost(postId: String, newPost: Post) {
        require(newPost.validate()) { "Post fields are invalid" }

        getCollectionPath(newPost.type).document(postId).set(newPost, SetOptions.merge()).await()
    }

    override suspend fun deletePost(type: PostType, postId: String) {
        getCollectionPath(type).document(postId).delete().await()
    }

    private fun documentToPost(document: DocumentSnapshot): Post {
        val uid = document.id
        val title: String = document.getString("title")!!
        val description: String = document.getString("description")!!
        val ownerId: String = document.getString("ownerId")!!

        @Suppress("UNCHECKED_CAST")
        val tags = (document.get("tags") as? List<String>)?.map { EveryTag.valueOf(it) }!!

        @Suppress("UNCHECKED_CAST")
        val paymentMethods =
            (document.get("paymentMethods") as? List<String>)?.map { PaymentMethod.valueOf(it) }!!

        val expiry = document.getTimestamp("expiry")!!
        val creation = document.getTimestamp("creation")!!

        val status = document.getString("status")?.let { PostStatus.valueOf(it) }!!

        @Suppress("UNCHECKED_CAST") val media = (document.get("media") as? List<String>)!!

        val postType = document.getString("type")?.let { PostType.valueOf(it) }!!

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
                PostType.OFFER ->
                    Offer(
                        give = title, // Use title as give for legacy compatibility
                        receive = "", // Empty for now
                        authorID = ownerId, // Use ownerId as authorID for legacy compatibility
                        thumbnail = "", // Empty for now
                        uid = uid,
                        title = title,
                        description = description,
                        ownerId = ownerId,
                        tags = tags,
                        paymentMethods = paymentMethods,
                        expiry = expiry,
                        creation = creation,
                        status = status,
                        media = media,
                        type = PostType.OFFER
                    )
            }
        require(post.validate()) { "Post was not validated successfully" }
        return post
    }

    private fun getCollectionPath(type: PostType): CollectionReference {
        return when (type) {
            PostType.OFFER -> offersCollection
            PostType.REQUEST -> requestsCollection
        }
    }
}

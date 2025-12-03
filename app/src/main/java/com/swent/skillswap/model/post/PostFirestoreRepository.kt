/**
 * Credits: Alexander Magnus, some helper methods created with the help of Gemini and ChatGPT
 *
 * Template: CS-311
 */
package com.swent.skillswap.model.post

import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.swent.skillswap.firebase.FirestorePaths
import com.swent.skillswap.firebase.FirestoreSettings
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.calculateDistance
import com.swent.skillswap.model.utils.RepositoryException
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
        paymentMethod: PaymentMethod?,
        skills: Set<SkillTag>,
        tags: Set<PostTag>,
        status: PostStatus?,
        userLocation: GeoPoint?,
        maxDistanceKm: Float
    ): List<Post> {
        return try {
            // Build query and get posts
            val query: Query =
                buildQuery(
                    type,
                    ownerId,
                    status,
                    titleContains,
                    paymentMethod,
                    skills,
                    tags,
                    numberOfPosts
                )
            var posts = query.get().await().map { documentToPost(it) }

            if (userLocation != null && maxDistanceKm != 0f) {
                val epsilon = 0.05
                posts =
                    posts.filter {
                        calculateDistance(userLocation, it.location) <= maxDistanceKm + epsilon
                    }
            }

            // To fulfil contracted requirements sort by creation date
            posts.sortedByDescending { it.creation }
        } catch (e: Exception) {
            throw RepositoryException("Failed to fetch posts", e)
        }
    }

    private fun buildQuery(
        type: PostType,
        ownerId: String,
        status: PostStatus?,
        titleContains: String,
        paymentMethod: PaymentMethod?,
        skills: Set<SkillTag>,
        tags: Set<PostTag>,
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
        if (paymentMethod != null) {
            query = query.whereEqualTo("paymentMethod", paymentMethod)
        }

        // perform complex searchKeys filter to bypass limit of single whereArrayContainsAny per
        // query, current implementation is limited to using the first 10 search keys, the rest are
        // ignored
        val searchKeys = buildSearchKeys(titleContains, skills, tags)
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
     * @param skills the skill to filter by
     * @param tags The tags to filter by.
     * @return A list of search keys. Note: Firestore's 'array-contains-any' is limited to a maximum
     *   of 10 elements in the comparison array, so this function returns at most 10 distinct search
     *   keys.
     */
    private fun buildSearchKeys(
        titleContains: String,
        skills: Set<SkillTag>,
        tags: Set<PostTag>
    ): List<String> {
        val searchKeys = mutableListOf<String>()
        if (titleContains.isNotBlank())
            searchKeys.addAll(titleContains.split(" ").map { it.lowercase() })
        if (skills.isNotEmpty()) searchKeys.addAll(skills.map { it.toString().lowercase() })
        if (tags.isNotEmpty()) searchKeys.addAll(tags.map { it.toString().lowercase() })
        return searchKeys.distinct().take(FirestoreSettings.MAX_SEARCH_KEYS)
    }

    override suspend fun getPost(type: PostType, postId: String): Post {
        return try {
            val document = getCollectionPath(type).document(postId).get().await()
            if (!document.exists()) {
                throw RepositoryException("Post $postId does not exist", null)
            }
            documentToPost(document)
        } catch (e: Exception) {
            throw RepositoryException("Failed to get post $postId", e)
        }
    }

    override suspend fun addPost(post: Post) {
        try {
            require(post.validate()) { "Post fields are invalid" }
            val docRef = getCollectionPath(post.type).document(post.uid)
            val snapshot = docRef.get().await()

            // ensure addPost isn't being used to overwrite
            require(!snapshot.exists()) { "Post with UID ${post.uid} already exists" }
            docRef.set(serializePost(post)).await()
        } catch (e: Exception) {
            throw RepositoryException("Failed to add post ${post.uid}", e)
        }
    }

    override suspend fun editPost(postId: String, newPost: Post) {
        try {
            require(newPost.validate()) { "Post fields are invalid" }

            getCollectionPath(newPost.type)
                .document(postId)
                .set(serializePost(newPost), SetOptions.merge())
                .await()
        } catch (e: Exception) {
            throw RepositoryException("Failed to edit post $postId", e)
        }
    }

    override suspend fun deletePost(type: PostType, postId: String) {
        try {
            getCollectionPath(type).document(postId).delete().await()
        } catch (e: Exception) {
            throw RepositoryException("Failed to delete post $postId", e)
        }
    }

    fun <T> requireField(name: String, value: T?): T =
        value ?: throw IllegalArgumentException("Missing or invalid field: $name")

    inline fun <reified T : Enum<T>> safeEnum(raw: String?): T {
        val value = raw ?: throw IllegalArgumentException("Missing ${T::class.simpleName} value")
        try {
            return enumValueOf(value)
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid ${T::class.simpleName} value: $value")
        }
    }

    private fun documentToPost(document: DocumentSnapshot): Post {
        val uid = document.id
        val title = requireField("title", document.getString("title"))
        val description = requireField("description", document.getString("description"))
        val ownerId = requireField("ownerId", document.getString("ownerId"))
        val expiry = requireField("expiry", document.getTimestamp("expiry"))
        val creation = requireField("creation", document.getTimestamp("creation"))
        val location = requireField("location", document.getGeoPoint("location"))
        val reportCount = requireField("reportCount", document.getLong("reportCount"))
        val skills =
            requireField(
                "skills",
                (document["skills"] as? List<*>)
                    ?.mapNotNull {
                        try {
                            SkillTag.valueOf(it.toString())
                        } catch (e: IllegalArgumentException) {
                            throw IllegalArgumentException(
                                "Invalid skill entry: $it — ${e.message}",
                                e
                            )
                        }
                    }
                    ?.toSet()
            )
        val tags =
            requireField(
                "tags",
                (document["tags"] as? List<*>)
                    ?.mapNotNull {
                        try {
                            PostTag.valueOf(it.toString())
                        } catch (e: IllegalArgumentException) {
                            throw IllegalArgumentException(
                                "Invalid tag entry: $it — ${e.message}",
                                e
                            )
                        }
                    }
                    ?.toSet()
            )
        val paymentMethod =
            safeEnum<PaymentMethod>(
                requireField("paymentMethod", document.getString("paymentMethod"))
            )

        val status = safeEnum<PostStatus>(requireField("status", document.getString("status")))
        val postType = safeEnum<PostType>(requireField("type", document.getString("type")))
        val media =
            requireField("media", (document.get("media") as? List<*>)?.map { it.toString() })
        val postReplies: Set<PostReply> =
            requireField(
                "postReplies",
                (document.get("postReplies") as? List<*>)
                    ?.map {
                        try {
                            documentToPostReply(it)
                        } catch (e: Exception) {
                            throw IllegalArgumentException(
                                "Invalid postReply entry: $it — ${e.message}",
                                e
                            )
                        }
                    }
                    ?.toSet()
            )

        val post =
            when (postType) {
                PostType.REQUEST ->
                    Request(
                        uid,
                        title,
                        description,
                        ownerId,
                        skills,
                        tags,
                        paymentMethod,
                        expiry,
                        creation,
                        status,
                        media,
                        postReplies,
                        location,
                        reportCount
                    )
                // TODO("Replace with Offer when it's implemented")
                PostType.OFFER -> throw NotImplementedError("FeedPost posts are not supported yet")
            }
        require(post.validate()) { "Post was not validated successfully" }
        return post
    }

    private fun documentToPostReply(item: Any?): PostReply {
        val map =
            item as? Map<*, *>
                ?: error(
                    "Invalid post reply entry: expected Map but got ${item?.javaClass?.simpleName}"
                )

        val uid = map["uid"] as? String ?: error("Missing or invalid 'uid' in post reply: $map")
        val postId =
            map["postId"] as? String ?: error("Missing or invalid 'postId' in post reply: $map")
        val ownerId =
            map["ownerId"] as? String ?: error("Missing or invalid 'ownerId' in post reply: $map")
        val message =
            map["message"] as? String ?: error("Missing or invalid 'message' in post reply: $map")
        val creation =
            map["creation"] as? Timestamp
                ?: error("Missing or invalid 'creation' in post reply: $map")
        val postTypeStr =
            map["postType"] as? String ?: error("Missing or invalid 'postType' in post reply: $map")
        val replyStatusStr =
            map["replyStatus"] as? String
                ?: error("Missing or invalid 'replyStatus' in post reply: $map")
        val postType =
            runCatching { PostType.valueOf(postTypeStr) }
                .getOrElse { error("Invalid postType value: '$postTypeStr' in post reply: $map") }
        val replyStatus =
            runCatching { ReplyStatus.valueOf(replyStatusStr) }
                .getOrElse {
                    error("Invalid replyStatus value: '$replyStatusStr' in post reply: $map")
                }

        return PostReply(
            uid = uid,
            postId = postId,
            ownerId = ownerId,
            message = message,
            creation = creation,
            postType = postType,
            replyStatus = replyStatus
        )
    }

    private fun getCollectionPath(type: PostType): CollectionReference {
        return when (type) {
            PostType.OFFER -> throw NotImplementedError("FeedPost posts are not supported yet")
            PostType.REQUEST -> requestsCollection
        }
    }

    private fun serializePost(post: Post): SerializablePost {
        return SerializablePost(
            uid = post.uid,
            title = post.title,
            description = post.description,
            ownerId = post.ownerId,
            skills = post.skills.toList(),
            tags = post.tags.toList(),
            paymentMethod = post.paymentMethod,
            expiry = post.expiry,
            creation = post.creation,
            status = post.status,
            media = post.media,
            type = post.type,
            location = post.location,
            postReplies = post.postReplies.toList(),
            reportCount = post.reportCount
        )
    }
}

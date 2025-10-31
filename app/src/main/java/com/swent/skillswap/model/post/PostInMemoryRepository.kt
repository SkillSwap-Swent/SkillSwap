package com.swent.skillswap.model.post

import com.swent.skillswap.model.tags.EveryTag
import java.util.UUID
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PostInMemoryRepository(initialPosts: List<Post> = emptyList()) : PostRepository {

    private val mutex = Mutex()

    // type -> (postId -> Post)
    private val store: MutableMap<PostType, MutableMap<String, Post>> =
        mutableMapOf(PostType.REQUEST to mutableMapOf(), PostType.OFFER to mutableMapOf())

    init {
        // preload the repository with any initial posts passed in
        initialPosts.forEach { post ->
            require(post.validate()) { "Invalid post in initialPosts: ${post.uid}" }
            val bucket = store.getOrPut(post.type) { mutableMapOf() }
            bucket[post.uid] = post
        }
    }

    override fun getNewUid(type: PostType): String = UUID.randomUUID().toString()

    override suspend fun getMultiplePosts(
        numberOfPosts: Long,
        type: PostType,
        titleContains: String,
        ownerId: String,
        paymentMethods: List<PaymentMethod>,
        tags: List<EveryTag>,
        status: PostStatus?,
    ): List<Post> =
        mutex.withLock {
            val searchKeys = buildSearchKeys(titleContains, paymentMethods, tags)
            store[type]
                .orEmpty()
                .values
                .asSequence()
                .filter { ownerId.isBlank() || it.ownerId == ownerId }
                .filter { status == null || it.status == status }
                .filter { searchKeys.isEmpty() || it.searchKeys.any { key -> key in searchKeys } }
                .sortedByDescending { it.creation }
                .take(numberOfPosts.toInt())
                .toList()
        }

    private fun buildSearchKeys(
        titleContains: String,
        paymentMethods: List<PaymentMethod>,
        tags: List<EveryTag>
    ): List<String> {
        val keys = mutableListOf<String>()
        if (titleContains.isNotBlank()) keys += titleContains.split(" ").map { it.lowercase() }
        if (paymentMethods.isNotEmpty()) keys += paymentMethods.map { it.toString().lowercase() }
        if (tags.isNotEmpty()) keys += tags.map { it.toString().lowercase() }
        return keys.distinct()
    }

    override suspend fun getPost(type: PostType, postId: String): Post =
        mutex.withLock {
            store[type]?.get(postId)
                ?: throw NoSuchElementException("Post $postId ($type) not found")
        }

    override suspend fun addPost(post: Post) =
        mutex.withLock {
            require(post.validate()) { "Post fields are invalid" }
            val bucket = store.getOrPut(post.type) { mutableMapOf() }
            require(post.uid !in bucket) { "Post with UID ${post.uid} already exists" }
            bucket[post.uid] = post
        }

    override suspend fun editPost(postId: String, newPost: Post) =
        mutex.withLock {
            require(newPost.validate()) { "Post fields are invalid" }
            store.values.forEach { it.remove(postId) }
            val target = store.getOrPut(newPost.type) { mutableMapOf() }
            target[postId] = newPost
        }

    override suspend fun deletePost(type: PostType, postId: String) =
        mutex.withLock {
            if (store[type]?.remove(postId) == null) {
                throw NoSuchElementException("Post $postId ($type) not found")
            }
        }
}

/**
 * Credits: Code: Alexander Magnus
 *
 * Comments: Gemini
 *
 * Template: CS-311
 */
package com.swent.skillswap.model.post

import com.swent.skillswap.model.tags.EveryTag

/**
 * Interface for the post repository, which handles all the database operations related to posts.
 *
 * WARNING: This class does not handle error handling. Make sure to catch exceptions and handle them
 * appropriately in the UI.
 */
interface PostRepository {

    /**
     * Generates a new unique ID for a post.
     *
     * @return A unique string identifier.
     */
    fun getNewUid(type: PostType): String

    /**
     * Retrieves a list of posts based on specified filters. Posts will be sorted by creation date.
     *
     * @param numberOfPosts The maximum number of posts to retrieve.
     * @param type The type of the posts to retrieve (e.g., OFFER, REQUEST). filters:
     * @param titleContains: The title of the post to filter by.
     * @param ownerId: The ID of the owner of the post to filter by.
     * @param paymentMethod The accepted payment methods to filter by.
     * @param tags The list of tags to filter by. Applies OR logic (any of).
     * @param status The status of the posts to retrieve. Applies OR logic (any of).
     * @return A list of posts matching the criteria.
     * @throws Exception Database exceptions will be thrown.
     */
    suspend fun getMultiplePosts(
        numberOfPosts: Long,
        type: PostType,
        // optional filters
        titleContains: String = "",
        ownerId: String = "",
        paymentMethod: PaymentMethod = PaymentMethod.SKILLSANDCASH,
        tags: List<EveryTag> = emptyList(),
        status: PostStatus? = null
    ): List<Post>

    /**
     * Retrieves a single post by its ID.
     *
     * @param type PostType
     * @param postId The unique identifier of the post to retrieve.
     * @return The Post object.
     * @throws Exception Database exceptions will be thrown.
     */
    suspend fun getPost(type: PostType, postId: String): Post

    /**
     * Adds a new post to the database.
     *
     * @param post The post object to add.
     * @throws Exception Database exceptions will be thrown.
     */
    suspend fun addPost(post: Post)

    /**
     * Edits an existing post in the database.
     *
     * @param postId The unique identifier of the post to edit.
     * @param newPost The new post object that will replace the old one.
     * @throws Exception Database exceptions will be thrown.
     */
    suspend fun editPost(postId: String, newPost: Post)

    /**
     * Deletes a post from the database.
     *
     * @param type PostType
     * @param postId The unique identifier of the post to delete.
     * @throws Exception Database exceptions will be thrown.
     */
    suspend fun deletePost(type: PostType, postId: String)
}

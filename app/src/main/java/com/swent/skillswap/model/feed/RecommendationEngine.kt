package com.swent.skillswap.model.feed

import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.UserRepositery

/**
 * Interface defining a recommendation engine for skill-based feeds.
 *
 * Implementations manage dynamic post filtering and ranking based on user interactions
 * (accepts/skips), skill preferences, and blocked users.
 *
 * Features:
 * - Tracks user interactions to update blacklisted/whitelisted skills.
 * - Supports dynamic filters that are always applied when filtering posts.
 * - Provides filtering and ranking utilities for post feeds.
 *
 * @author Joey Gugler using ChatGPT
 */
interface RecommendationEngine {

    /**
     * Initializes the recommendation engine for a given user and feed.
     *
     * @param userId The ID of the current user performing actions.
     * @param feedType The type of posts this engine will handle (REQUEST or OFFER).
     * @param blockedUsers A set of user IDs whose posts should never be displayed.
     * @param userRepository The user repository instance
     * @param undesiredSkillThreshold Ratio of skipped posts for a skill above which it becomes
     *   blacklisted.
     * @param desiredSkillThreshold Ratio of accepted posts for a skill above which it becomes
     *   whitelisted.
     */
    suspend fun initialize(
        userId: String,
        feedType: PostType,
        blockedUsers: Set<String>,
        userRepository: UserRepositery,
        undesiredSkillThreshold: Float,
        desiredSkillThreshold: Float
    )

    /**
     * Registers that the current user skipped a post.
     *
     * Updates internal counters and blacklists any skills that exceed the undesired threshold.
     *
     * @param post The post that was skipped.
     */
    suspend fun registerSkip(post: Post)

    /**
     * Registers that the current user accepted a post.
     *
     * Updates internal counters and whitelists any skills that exceed the desired threshold.
     *
     * @param post The post that was accepted.
     */
    suspend fun registerAccept(post: Post)

    /**
     * Adds a custom filter that must always hold for a post to be kept.
     *
     * A filter is a lambda: `(Post) -> Boolean`. Example: `addFilter { post -> post.uid !in
     * blockedUsers }`
     *
     * @param filter Lambda that returns `true` to keep the post, `false` to filter it out.
     */
    fun addFilter(filter: (Post) -> Boolean)

    /**
     * Clears all dynamic filters added via [addFilter].
     *
     * Useful when the user changes skill preferences or feed mode. Built-in filters (e.g., blocked
     * users) may be preserved depending on implementation.
     */
    fun clearFilters()

    /**
     * Checks whether a single post should be filtered out based on all internal logic, including
     * blacklisted skills and dynamic filters.
     *
     * @param post The post to evaluate.
     * @return `true` if the post should be filtered out, `false` otherwise.
     */
    fun shouldFilterOut(post: Post): Boolean

    /**
     * Filters a list of posts by applying:
     * - All built-in logic (whitelist/blacklist/blocked users)
     * - All dynamic filters added via [addFilter]
     *
     * @param posts The list of posts to filter.
     * @return A list of posts that pass all filters.
     */
    fun filterPosts(posts: List<Post>): List<Post>

    /**
     * Optionally reorders posts after filtering.
     *
     * Ranking may prioritize whitelisted skills, recency, or other metrics depending on
     * implementation.
     *
     * @param posts The filtered list of posts.
     * @return A new list of posts sorted according to ranking logic.
     */
    fun rankPosts(posts: List<Post>): List<Post>

    /**
     * Infers the most relevant skill associated with the given post based on the engine's internal
     * state (whitelist, blacklist, user preferences) and the feed type.
     *
     * Behavior:
     * - **For OFFER posts**: Returns the skill that is most likely to interest the current user,
     *   based on accumulated accept/skip behavior and whitelist/blacklist signals.
     * - **For REQUEST posts**: Uses exploration logic to avoid polarization:
     *     - Excludes undesired skills first.
     *     - If none remain, returns a random “discovery” skill.
     *     - Otherwise, returns a skill chosen from the neutral set in a controlled-random manner.
     *
     * @param post The post for which the engine must infer the most relevant skill.
     * @return The Skill the feed should emphasize when displaying this post.
     */
    suspend fun inferRelevantSkill(post: Post): Skill
}

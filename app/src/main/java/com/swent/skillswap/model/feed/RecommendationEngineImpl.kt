package com.swent.skillswap.model.feed

import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.tags.SkillTag

/**
 * Implementation of [RecommendationEngine] that provides dynamic skill-based post recommendations
 * for a feed.
 *
 * This engine tracks user interactions (accepts and skips) to update a whitelist and blacklist of
 * skills. Posts are filtered and ranked based on these preferences, as well as optional dynamic
 * filters and a blocked user list.
 *
 * Features:
 * - Tracks undesired and desired skill ratios per user interactions.
 * - Blacklists skills exceeding the undesired threshold.
 * - Whitelists skills exceeding the desired threshold.
 * - Supports additional dynamic filters for custom post filtering logic.
 * - Automatically filters posts from blocked users.
 * - Provides ranked post lists prioritizing whitelisted skills.
 *
 * @author Joey Gugler using ChatGPT
 */
class RecommendationEngineImpl : RecommendationEngine {
    private lateinit var userId: String
    private lateinit var feedType: PostType
    private var blockedUsers: Set<String> = emptySet()

    private var undesiredSkillThreshold: Float = 0.3f
    private var desiredSkillThreshold: Float = 0.6f

    // Counters for each skill
    private val undesiredSkillCounts = mutableMapOf<SkillTag, Int>()
    private val desiredSkillCounts = mutableMapOf<SkillTag, Int>()
    private val totalSkillAppearances = mutableMapOf<SkillTag, Int>()

    // Skills currently blacklisted / whitelisted
    private val blacklistedSkills = mutableSetOf<SkillTag>()
    private val whitelistedSkills = mutableSetOf<SkillTag>()

    // Dynamic filters
    private val dynamicFilters = mutableListOf<(Post) -> Boolean>()

    /**
     * Initializes the recommendation engine for a specific user and feed type.
     *
     * @param userId The ID of the current user performing actions.
     * @param feedType The type of posts this engine will handle (REQUEST or OFFER).
     * @param blockedUsers A set of user IDs whose posts should never be displayed.
     * @param undesiredSkillThreshold The ratio of skipped posts for a skill above which the skill
     *   will be blacklisted.
     * @param desiredSkillThreshold The ratio of accepted posts for a skill above which the skill
     *   will be whitelisted.
     */
    override suspend fun initialize(
        userId: String,
        feedType: PostType,
        blockedUsers: Set<String>,
        undesiredSkillThreshold: Float,
        desiredSkillThreshold: Float
    ) {
        this.userId = userId
        this.feedType = feedType
        this.blockedUsers = blockedUsers
        this.undesiredSkillThreshold = undesiredSkillThreshold
        this.desiredSkillThreshold = desiredSkillThreshold

        // Always apply a filter for blocked users
        addFilter { post -> post.ownerId !in blockedUsers }
    }

    /**
     * Registers that the current user skipped a post.
     *
     * Updates the undesired skill counts and blacklists any skills that exceed the undesired skill
     * threshold.
     *
     * @param post The post that was skipped.
     */
    override suspend fun registerSkip(post: Post) {
        post.skills.forEach { skill ->
            totalSkillAppearances[skill] = (totalSkillAppearances[skill] ?: 0) + 1
            undesiredSkillCounts[skill] = (undesiredSkillCounts[skill] ?: 0) + 1

            val total = totalSkillAppearances[skill] ?: 1
            val undesiredRatio = (undesiredSkillCounts[skill] ?: 0).toFloat() / total
            if (undesiredRatio >= undesiredSkillThreshold) {
                blacklistedSkills.add(skill)
            }
        }
        updateSkillFilters()
    }

    /**
     * Registers that the current user accepted a post.
     *
     * Updates the desired skill counts and whitelists any skills that exceed the desired skill
     * threshold.
     *
     * @param post The post that was accepted.
     */
    override suspend fun registerAccept(post: Post) {
        post.skills.forEach { skill ->
            totalSkillAppearances[skill] = (totalSkillAppearances[skill] ?: 0) + 1
            desiredSkillCounts[skill] = (desiredSkillCounts[skill] ?: 0) + 1

            val total = totalSkillAppearances[skill] ?: 1
            val desiredRatio = (desiredSkillCounts[skill] ?: 0).toFloat() / total
            if (desiredRatio >= desiredSkillThreshold) {
                whitelistedSkills.add(skill)
            }
        }
        updateSkillFilters()
    }

    private fun updateSkillFilters() {
        // Remove old skill filters
        dynamicFilters.removeAll { it is SkillFilter }

        // Add filters for blacklisted skills
        blacklistedSkills.forEach { skill -> addFilter(SkillFilter(skill)) }
    }
    /**
     * Adds a dynamic filter to the engine. Dynamic filters are always applied when filtering posts
     * and can implement custom filtering logic.
     *
     * @param filter A lambda that returns `true` to keep the post, `false` to filter it out.
     */
    override fun addFilter(filter: (Post) -> Boolean) {
        dynamicFilters.add(filter)
    }

    /** Clears all dynamic filters but preserves the default blocked users filter. */
    override fun clearFilters() {
        dynamicFilters.clear()
        // Always keep blocked users filter
        addFilter { post -> post.ownerId !in blockedUsers }
    }

    /**
     * Determines if a post should be filtered out based on blacklisted skills and dynamic filters.
     *
     * @param post The post to evaluate.
     * @return `true` if the post should be filtered out, `false` otherwise.
     */
    override fun shouldFilterOut(post: Post): Boolean {
        // Returns true if any filter fails or skill is blacklisted
        if (post.skills.any { it in blacklistedSkills }) return true
        return dynamicFilters.any { filter -> !filter(post) }
    }

    /**
     * Filters a list of posts using the engine's blacklist, whitelist, and dynamic filters.
     *
     * @param posts The list of posts to filter.
     * @return A list of posts that pass all filters.
     */
    override fun filterPosts(posts: List<Post>): List<Post> {
        return posts.filter { post -> !shouldFilterOut(post) }
    }

    /**
     * Ranks posts, prioritizing posts containing whitelisted skills at the top of the list.
     * Blacklisted posts are assumed to be filtered out already.
     *
     * @param posts The list of posts to rank.
     * @return A new list of posts sorted by whitelist relevance.
     */
    override fun rankPosts(posts: List<Post>): List<Post> {
        // Whitelist first, neutral, blacklisted last (already filtered)
        return posts.sortedWith(
            compareByDescending<Post> { post -> post.skills.count { it in whitelistedSkills } }
        )
    }

    // Internal wrapper for skill-based filter
    private inner class SkillFilter(private val skill: SkillTag) : (Post) -> Boolean {
        override fun invoke(post: Post): Boolean {
            return skill !in post.skills
        }
    }
}
/**
 * Factory class for creating and initializing instances of [RecommendationEngineImpl].
 *
 * Provides default thresholds for undesired and desired skill ratios.
 *
 * @author Joey Gugler using ChatGPT
 */
class RecommendationEngineFactory(
    private val undesiredSkillThreshold: Float = 0.3f,
    private val desiredSkillThreshold: Float = 0.6f
) {

    /**
     * Creates a new [RecommendationEngineImpl] instance and initializes it.
     *
     * @param userId The ID of the current user performing actions.
     * @param feedType The type of posts the engine will handle (REQUEST or OFFER).
     * @param blockedUsers A set of user IDs whose posts should never be displayed.
     * @return A fully initialized [RecommendationEngineImpl] ready for use.
     */
    suspend fun create(
        userId: String,
        feedType: PostType,
        blockedUsers: Set<String>
    ): RecommendationEngineImpl {
        val engine = RecommendationEngineImpl()
        engine.initialize(
            userId = userId,
            feedType = feedType,
            blockedUsers = blockedUsers,
            undesiredSkillThreshold = undesiredSkillThreshold,
            desiredSkillThreshold = desiredSkillThreshold
        )
        return engine
    }
}

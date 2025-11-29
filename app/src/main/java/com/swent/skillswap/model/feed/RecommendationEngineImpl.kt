package com.swent.skillswap.model.feed

import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.UserRepositery
import com.swent.skillswap.ui.utils.report_treshold

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
open class RecommendationEngineImpl : RecommendationEngine {
    private lateinit var userId: String
    private lateinit var feedType: PostType
    private lateinit var userRepository: UserRepositery
    private var blockedUsers: Set<String> = emptySet()
    private val blockedPosts: MutableSet<String> = mutableSetOf()
    private val viewedPosts: MutableSet<String> = mutableSetOf()
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
    private val dynamicFilters = mutableSetOf<(Post) -> Boolean>()

    private var lastError: Exception? = null

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
        userRepository: UserRepositery,
        undesiredSkillThreshold: Float,
        desiredSkillThreshold: Float
    ) {
        this.userId = userId
        this.feedType = feedType
        blockedUsers =
            try {
                userRepository.getUser(userId).blockedUsers
            } catch (e: Exception) {
                lastError = e
                emptySet()
            }
        this.userRepository = userRepository
        this.undesiredSkillThreshold = undesiredSkillThreshold
        this.desiredSkillThreshold = desiredSkillThreshold

        // Always apply a filter for blocked users
        addFilter { it.ownerId !in blockedUsers }
        addFilter { it.reportCount < report_treshold }
    }

    /**
     * Notifies the recommendation engine that the caller's blocked-users list has been updated in
     * the repository.
     *
     * This refreshes internal filtering rules so that:
     * - Newly blocked users are excluded from future recommendations.
     * - Newly unblocked users are allowed again (unless filtered by other rules).
     *
     * The method fetches the updated user profile from [userRepository], updates the internal
     * [blockedUsers] set, and re-applies the blocked-users filter.
     *
     * @return The updated set of blocked user IDs.
     * @throws Exception if the updated user information cannot be retrieved from the repository.
     */
    override suspend fun updateBlockedUser(): Set<String> {
        blockedUsers =
            try {
                userRepository.getUser(userId).blockedUsers
            } catch (e: Exception) {
                emptySet()
            }
        addFilter { post -> post.ownerId !in blockedUsers }
        return blockedUsers
    }

    /**
     * Reports a post, marking it as blocked for the current user.
     *
     * This method adds the given post's ID to the internal blocked set, so it will be filtered out
     * from the feed in future fetches.
     *
     * @param postId The [Post] to report and block.
     */
    override suspend fun reportPost(postId: String) {
        blockedPosts.add(postId)
        addFilter { post -> post.ownerId !in blockedPosts }
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
        addViewedPost(post.uid)
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
        addViewedPost(post.uid)
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

    /**
     * Infers the most relevant skill associated with a given post, depending on the feed type.
     *
     * For OFFER feeds:
     * - The provider explicitly advertises one or more skills.
     * - The relevance is trivial: we return the first advertised skill.
     *
     * For REQUEST feeds:
     * - The requester provides a list of skills they possess.
     * - The engine must decide which of those skills is most relevant for matching.
     * - Delegates to [inferSkillForRequest] to select a suitable skill based on user preferences,
     *   skip/accept history, and blacklist rules.
     *
     * @param post The post for which a relevant skill must be inferred.
     * @return The skill most relevant for recommendation purposes.
     */
    override suspend fun inferRelevantSkill(post: Post): Skill {
        return when (feedType) {
            PostType.OFFER ->
                Skill(
                    post.skills.first(),
                    0f,
                    ""
                ) // TODO Will address this when making the request Screen
            PostType.REQUEST -> inferSkillForRequest(post)
        }
    }
    /**
     * Determines the most relevant skill for a REQUEST post by analyzing the requester's skill set
     * and the user's personal preference history.
     *
     * This method performs the following steps:
     * 1. Fetches the requester from Firestore using [userRepository] to access their full skill
     *    set.
     * 2. Extracts all skills the requester possesses.
     * 3. Filters out:
     *     - Skills that have been blacklisted based on the user's skip history.
     *     - Skills with undesired skip frequency.
     * 4. If no valid skills remain:
     *     - Returns a random skill from the requester's set (DISCOVER mode).
     * 5. If valid skills remain:
     *     - Selects the most desirable one, based on the highest accept ratio stored in
     *       [desiredSkillCounts].
     *
     * This logic ensures the feed:
     * - Avoids repeatedly showing unwanted skills.
     * - Favors skills the user has shown interest toward.
     * - Still provides exploration opportunities when no preference is clear.
     *
     * @param post The REQUEST-type post for which a relevant skill must be inferred.
     * @return The skill most relevant to display for this post.
     * @throws Exception if requester data cannot be retrieved from the repository.
     */
    private suspend fun inferSkillForRequest(post: Post): Skill {
        val requester = userRepository.getUser(post.ownerId)
        val requesterSkills = requester.skillSet.map { it.name }

        val filtered =
            requesterSkills.filter { skill ->
                skill !in blacklistedSkills && (undesiredSkillCounts[skill] ?: 0) == 0
            }

        if (filtered.isEmpty()) {
            return requester.skillSet.random()
        }
        val tag = filtered.maxByOrNull { desiredSkillCounts[it] ?: 0 }!!
        return requester.skillSet.first { it.name == tag }
    }

    private fun addViewedPost(uid: String) {
        viewedPosts.add(uid)
        addFilter { it.uid !in viewedPosts }
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
    private val userRepository: UserRepositery,
    private val undesiredSkillThreshold: Float = 0.3f,
    private val desiredSkillThreshold: Float = 0.6f
) {
    suspend fun create(
        userId: String,
        feedType: PostType,
    ): RecommendationEngineImpl {
        val engine = RecommendationEngineImpl()
        engine.initialize(
            userId = userId,
            feedType = feedType,
            userRepository = userRepository,
            undesiredSkillThreshold = undesiredSkillThreshold,
            desiredSkillThreshold = desiredSkillThreshold
        )
        return engine
    }
}

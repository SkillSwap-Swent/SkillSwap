package com.swent.skillswap.post

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.feed.RecommendationEngineFactory
import com.swent.skillswap.model.feed.RecommendationEngineImpl
import com.swent.skillswap.model.post.PaymentMethod
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostReply
import com.swent.skillswap.model.post.PostStatus
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.model.tags.SkillTag
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class RecommendationEngineTest {

    private lateinit var engine: RecommendationEngineImpl
    private lateinit var engineFactory: RecommendationEngineFactory

    // Sample skills
    private val skillA = SkillTag.ENGINEERING_ETHICS
    private val skillB = SkillTag.ENGINEERING_ETHICS
    private val skillC = SkillTag.CALCULUS

    private val blockedUserId = "user_blocked"
    private val activeUserId = "user_active"

    // Sample posts
    private val post1 =
        object : Post {
            override val uid = "post1"
            override val title = "Post 1"
            override val description = "Description"
            override val ownerId = activeUserId
            override val skills = listOf(skillA)
            override val tags = emptyList<PostTag>()
            override val paymentMethod = PaymentMethod.SKILLS
            override val expiry = Timestamp.now()
            override val creation = Timestamp.now()
            override val status = PostStatus.POSTED
            override val media = emptyList<String>()
            override val type = PostType.REQUEST
            override val location = GeoPoint(0.0, 0.0)
            override val searchKeys = emptyList<String>()
            override val postReplies = emptyList<PostReply>()
        }

    private val post2Blocked =
        object : Post {
            override val uid = "post2"
            override val title = "Post 2"
            override val description = "Description"
            override val ownerId = blockedUserId
            override val skills = listOf(skillB)
            override val tags = emptyList<PostTag>()
            override val paymentMethod = PaymentMethod.SKILLS
            override val expiry = Timestamp.now()
            override val creation = Timestamp.now()
            override val status = PostStatus.POSTED
            override val media = emptyList<String>()
            override val type = PostType.OFFER
            override val location = GeoPoint(0.0, 0.0)
            override val searchKeys = emptyList<String>()
            override val postReplies = emptyList<PostReply>()
        }

    @Before
    fun setup() {
        // Create factory with custom thresholds
        engineFactory =
            RecommendationEngineFactory(
                undesiredSkillThreshold = 0.5f,
                desiredSkillThreshold = 0.5f
            )

        // Use factory to create and initialize engine
        engine = runBlocking {
            engineFactory.create(
                userId = activeUserId,
                feedType = PostType.REQUEST,
                blockedUsers = setOf(blockedUserId)
            )
        }
    }

    @Test
    fun testSkipBlacklistsSkill() {
        runBlocking { repeat(2) { engine.registerSkip(post1) } }

        assert(engine.filterPosts(listOf(post1)).isEmpty())
    }

    @Test
    fun testAcceptWhitelistsSkill() {
        runBlocking { repeat(2) { engine.registerAccept(post1) } }

        val filtered = engine.filterPosts(listOf(post1))
        assert(filtered.contains(post1))
        val ranked = engine.rankPosts(filtered)
        assert(post1 == ranked.first())
    }

    @Test
    fun testBlockedUserFiltering() {
        val filtered = engine.filterPosts(listOf(post1, post2Blocked))
        assert(filtered.contains(post1))
        assert(!filtered.contains(post2Blocked))
    }

    @Test
    fun testDynamicFilterAddition() = runBlocking {
        engine.addFilter { post -> post.skills.none { it == SkillTag.LINEAR_ALGEBRA } }

        val filtered = engine.filterPosts(listOf(post1, post2Blocked))

        assert(filtered.contains(post1))
        assert(!filtered.contains(post2Blocked))
    }

    @Test
    fun testClearFilters() {
        engine.addFilter { post -> post.skills.none { it == skillB } }
        engine.clearFilters() // Should remove dynamic filter but keep blocked users
        val filtered = engine.filterPosts(listOf(post1, post2Blocked))
        assert(filtered.contains(post1))
        assert(!filtered.contains(post2Blocked))
    }

    @Test
    fun testShouldFilterOut() {
        assert(!engine.shouldFilterOut(post1))
        assert(engine.shouldFilterOut(post2Blocked))
    }
}

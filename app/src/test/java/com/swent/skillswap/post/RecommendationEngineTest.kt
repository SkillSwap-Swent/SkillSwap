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
import com.swent.skillswap.model.post.Request
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.Preference
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepositery
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class RecommendationEngineTest {

    private lateinit var engine: RecommendationEngineImpl
    private lateinit var engineFactory: RecommendationEngineFactory
    private lateinit var userRepo: UserRepositery

    // Sample skills
    private val skillA = SkillTag.ENGINEERING_ETHICS
    private val skillB = SkillTag.ENGINEERING_ETHICS

    private val blockedUserId = "user_blocked"
    private val activeUserId = "user_active"

    private val activeUser =
        User(
            uid = activeUserId,
            username = "Alice",
            email = "alice@example.com",
            profilePicture = "",
            skillSet =
                setOf(Skill(SkillTag.CALCULUS, 0f, ""), Skill(SkillTag.PHYSICS_MECHANICS, 0f, "")),
            rating = 0f,
            availability = emptyList(),
            preference = Preference.SKILLS,
            location = GeoPoint(0.0, 0.0),
            blockedUsers = setOf(blockedUserId),
            fcmToken = null
        )

    private val blockedUser =
        User(
            uid = blockedUserId,
            username = "Bob",
            email = "bob@example.com",
            profilePicture = "",
            skillSet = setOf(Skill(SkillTag.LINEAR_ALGEBRA, 0f, "")),
            rating = 0f,
            availability = emptyList(),
            preference = Preference.SKILLS,
            location = GeoPoint(0.0, 0.0),
            blockedUsers = setOf(),
            fcmToken = null
        )

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
    fun setup() = runBlocking {
        userRepo =
            object : UserRepositery {
                override suspend fun getUser(userID: String): User =
                    when (userID) {
                        activeUserId -> activeUser
                        blockedUserId -> blockedUser
                        else -> throw Exception("User not found")
                    }

                override suspend fun addUser(user: User) {}

                override suspend fun editUser(userID: String, newValue: User) {}

                override suspend fun deleteUser(userID: String) {}

                override suspend fun userExists(userId: String): Boolean = true

                override suspend fun updateFcmToken(userId: String, fcmToken: String) {}

                override fun getNewUid(): String = UUID.randomUUID().toString()
            }

        // Create factory with custom thresholds
        engineFactory =
            RecommendationEngineFactory(
                undesiredSkillThreshold = 0.5f,
                desiredSkillThreshold = 0.5f,
                userRepository = userRepo
            )

        // Use factory to create and initialize engine with UserRepositery
        engine =
            engineFactory
                .create(
                    userId = activeUserId,
                    feedType = PostType.REQUEST,
                    blockedUsers = setOf(blockedUserId)
                )
                .also {
                    it.initialize(
                        userId = activeUserId,
                        feedType = PostType.REQUEST,
                        blockedUsers = setOf(blockedUserId),
                        userRepository = userRepo,
                        undesiredSkillThreshold = 0.5f,
                        desiredSkillThreshold = 0.5f
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

    @Test
    fun testInferRelevantSkillForRequest() = runBlocking {
        // Post from blocked user (should still infer skill ignoring blacklist)
        val postFromActiveUser =
            Request(
                uid = "post1",
                ownerId = activeUserId,
                title = "Need help",
                description = "Help me with calculations",
                skills = setOf(SkillTag.CALCULUS, SkillTag.PHYSICS_MECHANICS),
                tags = setOf(),
                paymentMethod = PaymentMethod.CASH,
                expiry = Timestamp.now(),
                creation = Timestamp.now(),
                status = PostStatus.POSTED,
                location = GeoPoint(0.0, 0.0),
                media = emptyList(),
                postReplies = emptySet(),
            )

        val inferredSkill = engine.inferRelevantSkill(postFromActiveUser)

        // Should pick the most desirable skill (first in whitelist/desired counts)
        assert(activeUser.skillSet.contains(inferredSkill))
    }

    @Test
    fun explorThenCommit() = runBlocking {
        val post1 =
            Request(
                uid = "post1",
                ownerId = activeUserId,
                title = "Need help",
                description = "Help me with calculations",
                skills = setOf(SkillTag.CALCULUS, SkillTag.PHYSICS_MECHANICS),
                tags = emptySet(),
                paymentMethod = PaymentMethod.CASH,
                expiry = Timestamp.now(),
                creation = Timestamp.now(),
                status = PostStatus.POSTED,
                location = GeoPoint(0.0, 0.0),
                media = emptyList(),
                postReplies = emptySet(),
            )

        // Explore mode - first inference picks something allowed
        val firstSkill = engine.inferRelevantSkill(post1)
        assert(firstSkill.name in post1.skills)

        // Accept the post - should update desiredSkillCounts / whitelist
        engine.registerAccept(post1)

        // Second inference - should favor previously accepted skill
        val secondSkill = engine.inferRelevantSkill(post1)
        assert(firstSkill.name == secondSkill.name)
    }

    @Test
    fun inferRelevantSkillBlackList() = runBlocking {
        val post =
            Request(
                uid = "post1",
                ownerId = activeUserId,
                title = "Need help",
                description = "Help me with calculations",
                skills = setOf(SkillTag.CALCULUS, SkillTag.PHYSICS_MECHANICS),
                tags = emptySet(),
                paymentMethod = PaymentMethod.CASH,
                expiry = Timestamp.now(),
                creation = Timestamp.now(),
                status = PostStatus.POSTED,
                location = GeoPoint(0.0, 0.0),
                media = emptyList(),
                postReplies = emptySet(),
            )

        // Simulate all skills blacklisted
        engine.registerSkip(post)
        engine.registerSkip(post)

        val inferredSkill = engine.inferRelevantSkill(post)
        assert(inferredSkill in post.ownerId.let { userRepo.getUser(it).skillSet })
    }
}

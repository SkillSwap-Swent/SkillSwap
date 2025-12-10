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
import com.swent.skillswap.model.post.ReplyStatus
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
    private val skillA = SkillTag.CALCULUS
    private val skillB = SkillTag.PHYSICS_MECHANICS

    private val blockedUserId = "user_blocked"
    private val otherUserId = "other_user"
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
    private val otherUser =
        User(
            uid = otherUserId,
            username = "Fred",
            email = "fred@example.com",
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
            override val ownerId = otherUserId
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
            override val reportCount: Long = 0
        }
    private val post2 =
        object : Post {
            override val uid = "post2"
            override val title = "Post 2"
            override val description = "Description"
            override val ownerId = otherUserId
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
            override val reportCount: Long = 0
        }
    private val post3 =
        object : Post {
            override val uid = "post3"
            override val title = "Post 3"
            override val description = "Description"
            override val ownerId = otherUserId
            override val skills = listOf(skillB)
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
            override val reportCount: Long = 0
        }
    private val post4 =
        object : Post {
            override val uid = "post2"
            override val title = "Post 2"
            override val description = "Description"
            override val ownerId = otherUserId
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
            override val postReplies =
                listOf(
                    PostReply(
                        "1",
                        "post2",
                        activeUserId,
                        Timestamp.now(),
                        "",
                        PostType.REQUEST,
                        ReplyStatus.PROPOSED
                    )
                )
            override val reportCount: Long = 0
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
            override val reportCount: Long = 0
        }

    @Before
    fun setup() = runBlocking {
        userRepo =
            object : UserRepositery {
                override suspend fun getUser(userID: String): User =
                    when (userID) {
                        activeUserId -> activeUser
                        blockedUserId -> blockedUser
                        otherUserId -> otherUser
                        else -> throw Exception("User not found")
                    }

                override suspend fun addUser(user: User) {}

                override suspend fun editUser(userID: String, newValue: User) {}

                override suspend fun deleteUser(userID: String) {}

                override suspend fun userExists(userId: String): Boolean = true

                override suspend fun updateFcmToken(userId: String, fcmToken: String) {}

                override fun getNewUid(): String = UUID.randomUUID().toString()

                override suspend fun updateRating(userId: String, incomingRating: Float) {}
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
                )
                .also {
                    it.initialize(
                        userId = activeUserId,
                        feedType = PostType.REQUEST,
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
        runBlocking { engine.registerAccept(post1) }
        val filtered = engine.filterPosts(listOf(post1, post2, post3))
        assert(!filtered.contains(post1))
        val ranked = engine.rankPosts(filtered)
        assert(post2 == ranked.first())
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
                paymentMethod = PaymentMethod.SKILLS,
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
        return@runBlocking
    }

    @Test
    fun exploreThenCommit() = runBlocking {
        val post1 =
            Request(
                uid = "post1",
                ownerId = activeUserId,
                title = "Need help",
                description = "Help me with calculations",
                skills = setOf(SkillTag.CALCULUS, SkillTag.PHYSICS_MECHANICS),
                tags = emptySet(),
                paymentMethod = PaymentMethod.SKILLS,
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
                paymentMethod = PaymentMethod.SKILLS,
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

    @Test
    fun inferCash() = runBlocking {
        val post =
            Request(
                uid = "p1",
                ownerId = activeUserId,
                title = "Cash job",
                description = "Paying cash",
                skills = setOf(skillA),
                tags = emptySet(),
                paymentMethod = PaymentMethod.CASH,
                expiry = Timestamp.now(),
                creation = Timestamp.now(),
                status = PostStatus.POSTED,
                location = GeoPoint(0.0, 0.0),
                media = emptyList(),
                postReplies = emptySet(),
            )

        val result = engine.inferRelevantSkill(post)
        assert(SkillTag.MONEY == result.name)
        return@runBlocking
    }

    @Test
    fun inferCashWhenUserPreferCash() = runBlocking {
        engine.setTestUserPreference(Preference.MONEY)

        val post =
            Request(
                uid = "p2",
                ownerId = activeUserId,
                title = "Mixed payment",
                description = "Money preferred",
                skills = setOf(skillA),
                tags = emptySet(),
                paymentMethod = PaymentMethod.SKILLSANDCASH,
                expiry = Timestamp.now(),
                creation = Timestamp.now(),
                status = PostStatus.POSTED,
                location = GeoPoint(0.0, 0.0),
                media = emptyList(),
                postReplies = emptySet(),
            )

        val result = engine.inferRelevantSkill(post)

        assert(SkillTag.MONEY == result.name)
    }

    @Test
    fun inferSkillWhenUserPreferenceSkill() = runBlocking {
        engine.setTestUserPreference(Preference.SKILLS)

        // Make sure requester has known skills
        val requester = userRepo.getUser(activeUserId)
        val requesterSkill = requester.skillSet.first()

        val post =
            Request(
                uid = "p3",
                ownerId = activeUserId,
                title = "Mixed payment",
                description = "Skills preferred",
                skills = setOf(skillA),
                tags = emptySet(),
                paymentMethod = PaymentMethod.SKILLSANDCASH,
                expiry = Timestamp.now(),
                creation = Timestamp.now(),
                status = PostStatus.POSTED,
                location = GeoPoint(0.0, 0.0),
                media = emptyList(),
                postReplies = emptySet(),
            )

        val result = engine.inferRelevantSkill(post)

        assert(result.name != SkillTag.MONEY)
        assert(result in requester.skillSet)
    }

    @Test
    fun inferSkill() = runBlocking {
        val expectedSkills = activeUser.skillSet

        val post =
            Request(
                uid = "p4",
                ownerId = activeUserId,
                title = "Skill-only request",
                description = "Skills only",
                skills = setOf(skillA),
                tags = emptySet(),
                paymentMethod = PaymentMethod.SKILLS,
                expiry = Timestamp.now(),
                creation = Timestamp.now(),
                status = PostStatus.POSTED,
                location = GeoPoint(0.0, 0.0),
                media = emptyList(),
                postReplies = emptySet(),
            )

        val result = engine.inferRelevantSkill(post)

        assert(result.name != SkillTag.MONEY)
        assert(result in expectedSkills)
    }

    @Test
    fun testUpdateBlockedUser() = runBlocking {
        val mutableActiveUser = activeUser.copy(blockedUsers = emptySet())
        var currentActiveUser = mutableActiveUser

        val testRepo =
            object : UserRepositery {
                override suspend fun getUser(userID: String): User =
                    when (userID) {
                        activeUserId -> currentActiveUser
                        blockedUserId -> blockedUser
                        else -> throw Exception("User not found")
                    }

                override suspend fun addUser(user: User) {}

                override suspend fun editUser(userID: String, newValue: User) {}

                override suspend fun deleteUser(userID: String) {}

                override suspend fun userExists(userId: String): Boolean = true

                override suspend fun updateFcmToken(userId: String, fcmToken: String) {}

                override fun getNewUid(): String = UUID.randomUUID().toString()

                override suspend fun updateRating(userId: String, incomingRating: Float) {}
            }

        engineFactory =
            RecommendationEngineFactory(
                undesiredSkillThreshold = 0.5f,
                desiredSkillThreshold = 0.5f,
                userRepository = testRepo
            )
        engine =
            engineFactory
                .create(
                    userId = activeUserId,
                    feedType = PostType.REQUEST,
                )
                .also {
                    it.initialize(
                        userId = activeUserId,
                        feedType = PostType.REQUEST,
                        userRepository = testRepo,
                        undesiredSkillThreshold = 0.5f,
                        desiredSkillThreshold = 0.5f
                    )
                }

        var updatedBlockedUsers = engine.updateBlockedUser()
        assert(updatedBlockedUsers.isEmpty())

        currentActiveUser = activeUser.copy(blockedUsers = setOf(blockedUserId))

        updatedBlockedUsers = engine.updateBlockedUser()

        assert(updatedBlockedUsers.contains(blockedUserId))

        val filtered = engine.filterPosts(listOf(post1, post2Blocked))
        assert(filtered.contains(post1))
        assert(!filtered.contains(post2Blocked))
    }

    @Test
    fun removePostAllReadyAcceptedFromView() {
        val filtered = engine.filterPosts(listOf(post4, post2, post1))
        assert(filtered.contains(post1))
        assert(filtered.contains(post2))
        assert(!filtered.contains(post4))
    }
}

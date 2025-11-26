package com.swent.skillswap.ui.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.firebase.FirestorePaths
import com.swent.skillswap.firebase.FirestoreSettings
import com.swent.skillswap.model.feed.ChatRepository
import com.swent.skillswap.model.feed.FeedControllerFactory
import com.swent.skillswap.model.feed.RecommendationEngineImpl
import com.swent.skillswap.model.feed.ThumbnailRepository
import com.swent.skillswap.model.post.*
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.utils.FirebaseEmulator
import java.util.Calendar
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Fake navigation for testing navigation calls. */
class FakeFeedNavigation : FeedScreenNavigation {
    private val _visitedProfiles = mutableListOf<String>()

    fun getVisitedProfiles(): List<String> = _visitedProfiles

    override fun goToProfileView(userId: String) {
        _visitedProfiles.add(userId)
    }
}

class FeedScreenInstrumentedTest {

    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var navigation: FakeFeedNavigation
    private lateinit var postRepository: PostRepository
    private lateinit var controllerFactory: FeedControllerFactory
    private lateinit var testUserId: String
    private val REQUESTS_COLLECTION = "requests"

    /** Helper: create a valid SerializablePost for tests */
    private fun createValidPost(
        uid: String,
        title: String,
        ownerId: String = "authorId",
        location: GeoPoint? = null,
        creation: Timestamp? = null
    ): Post {
        val expiry = Timestamp(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.time)

        return SerializablePost(
            uid = uid,
            title = title,
            description = "Valid description for $title",
            ownerId = ownerId,
            skills = setOf(SkillTag.MACHINE_DESIGN).toList(),
            tags = setOf(PostTag.ONE_TIME).toList(),
            expiry = expiry,
            creation = creation ?: Timestamp.now(),
            status = PostStatus.POSTED,
            location = location ?: GeoPoint(0.0, 0.0),
            media = listOf("https://picsum.photos/200"),
            paymentMethod = PaymentMethod.SKILLSANDCASH,
            type = PostType.REQUEST,
            postReplies = emptyList(),
            reportCount = 0L
        )
    }

    private suspend fun addPostToEmulator(post: Post) {
        val postMap =
            mapOf(
                "uid" to post.uid,
                "title" to post.title,
                "description" to post.description,
                "ownerId" to post.ownerId,
                "skills" to post.skills.map { it.toString() },
                "tags" to post.tags.map { it.toString() },
                "paymentMethod" to post.paymentMethod.toString(),
                "expiry" to post.expiry,
                "creation" to post.creation,
                "status" to post.status.toString(),
                "type" to post.type.toString(),
                "location" to post.location,
                "media" to post.media,
                "postReplies" to
                    post.postReplies.map { reply ->
                        mapOf(
                            "uid" to reply.uid,
                            "postId" to reply.postId,
                            "ownerId" to reply.ownerId,
                            "message" to reply.message,
                            "creation" to reply.creation,
                            "postType" to reply.postType.toString(),
                            "replyStatus" to reply.replyStatus.toString()
                        )
                    },
                "searchKeys" to buildSearchKeysForPost(post),
                "reportCount" to post.reportCount
            )

        FirebaseEmulator.firestore
            .collection(FirestorePaths.REQUESTS_COLLECTION)
            .document(post.uid)
            .set(postMap)
            .await()
    }

    // Helper to generate searchKeys (matches repository logic)
    private fun buildSearchKeysForPost(post: Post): List<String> {
        val keys = mutableListOf<String>()
        keys.addAll(post.title.split(" ").map { it.lowercase() })
        keys.addAll(post.tags.map { it.toString().lowercase() })
        return keys.distinct().take(FirestoreSettings.MAX_SEARCH_KEYS)
    }

    /** Get post from emulator */
    private suspend fun getPostFromEmulator(postId: String): Post? {
        val doc =
            FirebaseEmulator.firestore
                .collection(REQUESTS_COLLECTION)
                .document(postId)
                .get()
                .await()
        if (!doc.exists()) return null
        return doc.toObject(SerializablePost::class.java)
    }

    @Before
    fun setUp() {
        FirebaseEmulator.startEmulator()
        navigation = FakeFeedNavigation()
        postRepository = PostFirestoreRepository(FirebaseEmulator.firestore)
        controllerFactory =
            FeedControllerFactory(
                recommendationEngine = RecommendationEngineImpl(),
                thumbnailRepository = ThumbnailRepository(),
                postRepository = postRepository,
                chatRepository = ChatRepository(),
                locationManager = null
            )

        runBlocking { testUserId = FirebaseEmulator.auth.signInAnonymously().await().user!!.uid }
    }

    @After
    fun tearDown() {
        runBlocking {
            FirebaseEmulator.auth.signOut()
            FirebaseEmulator.clearAuthEmulator()
            FirebaseEmulator.clearFirestoreEmulator()
        }
    }

    @Test
    fun noOfferAvailable_DisplaysNoOfferText() {
        val controller = runBlocking { controllerFactory.create(testUserId, PostType.REQUEST) }
        val vm = FeedScreenViewModel(navigation, controller)

        composeTestRule.setContent { Box(Modifier.fillMaxSize()) { FeedScreen(vm = vm) } }

        composeTestRule.onNodeWithTag(FeedScreenTestTags.NO_OFFER_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FeedScreenTestTags.FEED_CARD).assertDoesNotExist()
    }

    @Test
    fun initialLoad_DisplaysFirstOffer() {
        runBlocking {
            val post1 = createValidPost("post1", "Learn Guitar")
            addPostToEmulator(post1)

            FirebaseEmulator.firestore.collection("requests").get().await()

            val controller = controllerFactory.create(testUserId, PostType.REQUEST)
            val vm = FeedScreenViewModel(navigation, controller)

            // Set up UI content
            composeTestRule.setContent { Box(Modifier.fillMaxSize()) { FeedScreen(vm = vm) } }

            val expectedText = "You will get: ${post1.title}"
            composeTestRule.waitUntil(timeoutMillis = 10_000) {
                composeTestRule.onAllNodesWithText(expectedText).fetchSemanticsNodes().isNotEmpty()
            }

            composeTestRule.onNodeWithText(expectedText).assertIsDisplayed()
            composeTestRule.onNodeWithTag(FeedScreenTestTags.FEED_CARD).assertIsDisplayed()
        }
    }

    /**
     * Helper function to safely get text from a SemanticsNode. Place this inside your test class or
     * file.
     */
    private fun SemanticsNode.getTextString(): String? {
        // text is stored as a list, join it to get the full string
        return this.config.getOrNull(SemanticsProperties.Text)?.joinToString()
    }

    @Test
    fun acceptOffer_LoadsNextOffer() = runBlocking {
        val post1 = createValidPost("post1", "Learn Guitar", "author1")
        val post2 = createValidPost("post2", "Learn Piano", "author2")

        // Add posts (order in emulator is non-deterministic)
        addPostToEmulator(post1)
        addPostToEmulator(post2)
        FirebaseEmulator.firestore.collection("requests").get().await()

        val controller = controllerFactory.create(testUserId, PostType.REQUEST)
        val factory = FeedScreenViewModelFactory(navigation, controller)

        composeTestRule.setContent {
            val vm: FeedScreenViewModel = viewModel(factory = factory)
            Box(Modifier.fillMaxSize()) { FeedScreen(vm = vm) }
        }

        // Wait until a skill title appears (either post1 or post2)
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            try {
                composeTestRule.onNodeWithTag(FeedScreenTestTags.SKILL_GIVE).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Determine which post is shown first
        val shownIsPost1 =
            try {
                composeTestRule
                    .onNodeWithTag(FeedScreenTestTags.SKILL_GIVE)
                    .assertTextContains(post1.title, substring = true, ignoreCase = true)
                true
            } catch (e: AssertionError) {
                false
            }

        val expectedNextTitle = if (shownIsPost1) post2.title else post1.title

        // Accept the currently visible offer
        composeTestRule.onNodeWithTag(FeedScreenTestTags.ACCEPT_BUTTON).performClick()

        // Wait until the other post is displayed
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            try {
                composeTestRule
                    .onNodeWithTag(FeedScreenTestTags.SKILL_GIVE)
                    .assertTextContains(expectedNextTitle, substring = true, ignoreCase = true)
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Final explicit check
        composeTestRule
            .onNodeWithTag(FeedScreenTestTags.SKILL_GIVE)
            .assertTextContains(expectedNextTitle, substring = true, ignoreCase = true)
        return@runBlocking
    }

    @Test
    fun skipOffer_LoadsNextOffer() = runBlocking {
        val post1 = createValidPost("post1", "Learn Guitar", "author1")
        val post2 = createValidPost("post2", "Learn Piano", "author2")

        // Add posts (order may not be deterministic)
        addPostToEmulator(post1)
        addPostToEmulator(post2)
        FirebaseEmulator.firestore.collection("requests").get().await()

        // Create controller and ViewModel via factory
        val controller = controllerFactory.create(testUserId, PostType.REQUEST)
        val factory = FeedScreenViewModelFactory(navigation, controller)

        composeTestRule.setContent {
            Box(Modifier.fillMaxSize()) {
                val vm: FeedScreenViewModel = viewModel(factory = factory)
                FeedScreen(vm = vm)
            }
        }

        // Wait until a skill title appears
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            try {
                composeTestRule.onNodeWithTag(FeedScreenTestTags.SKILL_GIVE).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Determine which post is shown first
        val shownIsPost1 =
            try {
                composeTestRule
                    .onNodeWithTag(FeedScreenTestTags.SKILL_GIVE)
                    .assertTextContains(post1.title, substring = true, ignoreCase = true)
                true
            } catch (e: AssertionError) {
                false
            }

        val expectedNextTitle = if (shownIsPost1) post2.title else post1.title

        // Skip current offer (decline button)
        composeTestRule.onNodeWithTag(FeedScreenTestTags.DECLINE_BUTTON).performClick()

        // Wait until the next post is displayed
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            try {
                composeTestRule
                    .onNodeWithTag(FeedScreenTestTags.SKILL_GIVE)
                    .assertTextContains(expectedNextTitle, substring = true, ignoreCase = true)
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Final explicit check
        composeTestRule
            .onNodeWithTag(FeedScreenTestTags.SKILL_GIVE)
            .assertTextContains(expectedNextTitle, substring = true, ignoreCase = true)
        return@runBlocking
    }

    @Test
    fun allTestTagsDisplayedAndMenuInteractions() = runBlocking {
        // Arrange: create a feed offer
        val post1 = createValidPost("1", "Guitar Lessons", "author123")
        addPostToEmulator(post1)
        FirebaseEmulator.firestore.collection("requests").get().await()

        val controller = controllerFactory.create(testUserId, PostType.REQUEST)
        val vm = FeedScreenViewModel(navigation, controller)

        composeTestRule.setContent { Box(Modifier.fillMaxSize()) { FeedScreen(vm = vm) } }

        // List of test tags that require scrolling
        val scrollableTags =
            listOf(
                FeedScreenTestTags.SKILL_GIVE,
                FeedScreenTestTags.SPECIFICATION_TITLE,
                FeedScreenTestTags.SPECIFICATION_DESCRIPTION
            )

        // Scroll each scrollable node into view and assert displayed
        scrollableTags.forEach { tag ->
            try {
                composeTestRule.onNodeWithTag(tag).performScrollTo()
                composeTestRule.waitForIdle()
                composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
            } catch (e: AssertionError) {
                throw AssertionError("❌ UI element with testTag '$tag' was NOT displayed.", e)
            }
        }

        // Other elements that are always visible (no scroll needed)
        val visibleTags =
            listOf(
                FeedScreenTestTags.FEED_CARD,
                FeedScreenTestTags.SKILL_REQUESTED,
                FeedScreenTestTags.FEED_MENU_BUTTON,
                FeedScreenTestTags.FEED_THUMBNAIL,
                FeedScreenTestTags.REQUESTER_PROFILE_PICTURE,
                FeedScreenTestTags.REQUESTER_NAME,
                FeedScreenTestTags.ACCEPT_BUTTON,
                FeedScreenTestTags.DECLINE_BUTTON
            )

        visibleTags.forEach { tag ->
            try {
                composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
            } catch (e: AssertionError) {
                throw AssertionError("❌ UI element with testTag '$tag' was NOT displayed.", e)
            }
        }

        // === Menu interactions ===
        composeTestRule.onNodeWithTag(FeedScreenTestTags.FEED_MENU_BUTTON).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Block User").assertIsDisplayed()
        composeTestRule.onNodeWithText("Report Offer").assertIsDisplayed()

        composeTestRule.onNodeWithText("Block User").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(FeedScreenTestTags.FEED_MENU_BUTTON).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Report Offer").performClick()
        composeTestRule.waitForIdle()

        // Dismiss menu
        composeTestRule.onRoot().performTouchInput { click(center) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Block User").assertDoesNotExist()
        composeTestRule.onNodeWithText("Report Offer").assertDoesNotExist()
    }

    @Test
    fun distanceFilter_FiltersPostsByDistanceFromDefaultLocation() = runBlocking {
        // Default device location: GeoPoint(46.5191, 6.5668) - EPFL, Lausanne

        // Post 1: Very close (~0 km) - EPFL Campus (oldest post)
        val postNearby =
            createValidPost(
                uid = "post1",
                title = "Nearby Post",
                ownerId = "author1",
                location = GeoPoint(46.5191, 6.5668),
                creation =
                    Timestamp(Calendar.getInstance().apply { add(Calendar.SECOND, -10) }.time)
            )

        // Post 2: Medium distance (~8 km) - Lausanne center (middle post)
        val postMedium =
            createValidPost(
                uid = "post2",
                title = "Medium Distance Post",
                ownerId = "author2",
                location = GeoPoint(46.5197, 6.6323),
                creation = Timestamp(Calendar.getInstance().apply { add(Calendar.SECOND, -5) }.time)
            )

        // Post 3: Far away (~15 km) - Montreux area (newest post)
        val postFar =
            createValidPost(
                uid = "post3",
                title = "Far Post",
                ownerId = "author3",
                location = GeoPoint(46.4312, 6.9106),
                creation = Timestamp.now()
            )

        // Add posts to emulator
        addPostToEmulator(postNearby)
        addPostToEmulator(postMedium)
        addPostToEmulator(postFar)
        FirebaseEmulator.firestore.collection("requests").get().await()

        val controller = controllerFactory.create(testUserId, PostType.REQUEST)
        val factory = FeedScreenViewModelFactory(navigation, controller)

        composeTestRule.setContent {
            val vm: FeedScreenViewModel = viewModel(factory = factory)
            Box(Modifier.fillMaxSize()) { FeedScreen(vm = vm) }
        }

        // Wait for initial post to load
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            try {
                composeTestRule.onNodeWithTag(FeedScreenTestTags.SKILL_GIVE).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Initially, postFar should be visible (newest post, no filter)
        composeTestRule
            .onNodeWithTag(FeedScreenTestTags.SKILL_GIVE)
            .assertTextContains(postFar.title, substring = true, ignoreCase = true)

        // Open distance filter slider
        composeTestRule.onNodeWithTag(FeedScreenTestTags.DISTANCE_FILTER_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Wait for distance filter popup to fully render
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            try {
                composeTestRule
                    .onNodeWithTag(FeedScreenTestTags.DISTANCE_SLIDER)
                    .assertIsDisplayed()
                composeTestRule
                    .onNodeWithTag(FeedScreenTestTags.DISTANCE_VALUE_TEXT)
                    .assertIsDisplayed()
                composeTestRule
                    .onNodeWithTag(FeedScreenTestTags.LIVE_LOCATION_CHECKBOX)
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Assert all elements are displayed
        composeTestRule.onNodeWithText("Use Live Location").assertIsDisplayed()
        composeTestRule.onNodeWithText("1-20 km").assertIsDisplayed()

        // Set filter to 10 km (should exclude postFar at ~15 km)
        composeTestRule.onNodeWithTag(FeedScreenTestTags.DISTANCE_SLIDER).performTouchInput {
            val sliderWidth = width.toFloat()
            val targetPosition = sliderWidth * (9f / 19f) // (10-1)/(20-1)
            click(center.copy(x = left + targetPosition))
        }

        // Wait for the slider value to update before checking
        composeTestRule.waitForIdle()
        Thread.sleep(500) // Give extra time for UI to stabilize

        // Wait until distance value text updates to ~10 km
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            try {
                val distanceText =
                    composeTestRule
                        .onNodeWithTag(FeedScreenTestTags.DISTANCE_VALUE_TEXT)
                        .fetchSemanticsNode()
                        .getTextString() ?: ""
                distanceText.contains("10 km", ignoreCase = true)
            } catch (e: Exception) {
                false
            }
        }

        // Dismiss the slider by clicking outside of it
        composeTestRule.onRoot().performTouchInput { click(bottomCenter) }
        composeTestRule.waitForIdle()

        // After filter applied, feed should refresh showing postMedium (newest within 10km)
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            try {
                val skillGiveNode = composeTestRule.onNodeWithTag(FeedScreenTestTags.SKILL_GIVE)
                val text = skillGiveNode.fetchSemanticsNode().getTextString() ?: ""
                text.contains(postMedium.title, ignoreCase = true)
            } catch (e: AssertionError) {
                false
            }
        }

        // Verify postMedium is shown first (newest filtered post)
        composeTestRule
            .onNodeWithTag(FeedScreenTestTags.SKILL_GIVE)
            .assertTextContains(postMedium.title, substring = true, ignoreCase = true)

        // Press decline to move to next filtered post
        composeTestRule.onNodeWithTag(FeedScreenTestTags.DECLINE_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Wait for postNearby to appear (second filtered post)
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            try {
                val skillGiveNode = composeTestRule.onNodeWithTag(FeedScreenTestTags.SKILL_GIVE)
                val text = skillGiveNode.fetchSemanticsNode().getTextString() ?: ""
                text.contains(postNearby.title, ignoreCase = true)
            } catch (e: AssertionError) {
                false
            }
        }

        // Verify postNearby is shown (oldest filtered post)
        composeTestRule
            .onNodeWithTag(FeedScreenTestTags.SKILL_GIVE)
            .assertTextContains(postNearby.title, substring = true, ignoreCase = true)

        // Verify postFar was never shown after filtering
        val finalText =
            composeTestRule
                .onNodeWithTag(FeedScreenTestTags.SKILL_GIVE)
                .fetchSemanticsNode()
                .getTextString() ?: ""
        assert(!finalText.contains(postFar.title, ignoreCase = true)) {
            "Post at ~15 km should remain filtered out"
        }
    }
}

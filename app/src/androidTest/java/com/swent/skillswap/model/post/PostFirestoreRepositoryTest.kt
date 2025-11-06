package com.swent.skillswap.model.post

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.utils.FirebaseEmulator
import java.util.Date
import kotlin.String
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostRepositoryInstrumentedTest {

    private lateinit var repo: PostRepository

    private val epflLocation = GeoPoint(46.5191, 6.5668)
    private val lausanneLocation = GeoPoint(46.5197, 6.6323)
    private val genevaLocation = GeoPoint(46.2044, 6.1432)

    val request1 =
        Request(
            uid = "123",
            title = "Need help with Kotlin",
            description = "Looking for an expert to teach me Kotlin.",
            ownerId = "user456",
            tags = setOf(PostTag.REOCCURRING),
            expiry = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            creation = Timestamp.now(),
            status = PostStatus.POSTED,
            media = listOf("media_url_1", "media_url_2"),
            paymentMethod = PaymentMethod.SKILLSANDCASH,
            location = epflLocation,
            postReplies =
                setOf(
                    PostReply(
                        postId = "123",
                        ownerId = "replier123",
                        creation = Timestamp.now(),
                        message = "I want to help!",
                        postType = PostType.REQUEST
                    )
                )
        )

    @Before
    fun setUp() {
        FirebaseEmulator.startEmulator()
        assertTrue("Firestore emulator must be running", FirebaseEmulator.isRunning)
        FirebaseEmulator.clearFirestoreEmulator()
        repo = PostFirestoreRepository(FirebaseEmulator.firestore)
    }

    @Test
    fun getNewUid_returnsUniqueIds() {
        val a = repo.getNewUid(PostType.REQUEST)
        val b = repo.getNewUid(PostType.REQUEST)
        assertNotEquals(a, b)
    }

    @Test
    fun addAndGet_roundTrip_success() {
        runTest {
            val id = repo.getNewUid(PostType.REQUEST)
            val req = request1.copy(uid = id)

            repo.addPost(req)

            val fetched = repo.getPost(PostType.REQUEST, id) as Request
            assertEquals(req.uid, fetched.uid)
            assertEquals(req.title, fetched.title)
            assertEquals(req.description, fetched.description)
            assertEquals(req.ownerId, fetched.ownerId)
            assertEquals(req.tags, fetched.tags)
            assertEquals(req.paymentMethod, fetched.paymentMethod)
            assertEquals(req.status, fetched.status)
            assertEquals(req.media, fetched.media)
            assertEquals(PostType.REQUEST, fetched.type)
            assertEquals(req.location.latitude, fetched.location.latitude, 0.0001)
            assertEquals(req.location.longitude, fetched.location.longitude, 0.0001)
            assertEquals(req.location, fetched.location)
            assertEquals(req.postReplies, fetched.postReplies)
        }
    }

    @Test
    fun addPost_invalid_throws() {
        val badId: String = repo.getNewUid(PostType.REQUEST)
        val badTitle: Request = request1.copy(uid = badId, title = "")

        assertThrows(IllegalArgumentException::class.java) { runTest { repo.addPost(badTitle) } }
    }

    @Test
    fun editPost_invalid_throws() {
        runTest {
            val id = repo.getNewUid(PostType.REQUEST)
            val original = request1.copy(uid = id, title = "Need help with Kotlin")
            repo.addPost(original)
            val bad: Request = original.copy(title = "")

            assertThrows(IllegalStateException::class.java) { runTest { repo.editPost(id, bad) } }
        }
    }

    @Test
    fun editPost_overwritesDocument() {
        runTest {
            val id = repo.getNewUid(PostType.REQUEST)
            val original = request1.copy(uid = id, title = "Need help with Kotlin")
            repo.addPost(original)

            val updated =
                original.copy(
                    title = "Need help with Advanced Kotlin",
                    description = "Coroutines & Flows deep dive",
                    location = lausanneLocation
                )
            repo.editPost(id, updated)

            val fetched = repo.getPost(PostType.REQUEST, id) as Request
            assertEquals("Need help with Advanced Kotlin", fetched.title)
            assertEquals("Coroutines & Flows deep dive", fetched.description)
            assertEquals(lausanneLocation.latitude, fetched.location.latitude, 0.0001)
            assertEquals(lausanneLocation.longitude, fetched.location.longitude, 0.0001)
        }
    }

    @Test
    fun deletePost_removesDocument() {
        runTest {
            val id: String = repo.getNewUid(PostType.REQUEST)
            val req: Request = request1.copy(uid = id)
            repo.addPost(req)

            repo.deletePost(PostType.REQUEST, id)

            assertThrows(IllegalStateException::class.java) {
                runTest { repo.getPost(PostType.REQUEST, id) }
            }
        }
    }

    @Test
    fun getMultiplePosts_filters_owner_and_status() {
        runTest {
            val a1 =
                request1.copy(
                    uid = repo.getNewUid(PostType.REQUEST),
                    ownerId = "ownerA",
                    status = PostStatus.POSTED
                )
            val a2 =
                request1.copy(
                    uid = repo.getNewUid(PostType.REQUEST),
                    ownerId = "ownerA",
                    status = PostStatus.ARCHIVED
                )
            val b1 =
                request1.copy(
                    uid = repo.getNewUid(PostType.REQUEST),
                    ownerId = "ownerB",
                    status = PostStatus.POSTED
                )
            repo.addPost(a1)
            repo.addPost(a2)
            repo.addPost(b1)

            val results =
                repo.getMultiplePosts(
                    numberOfPosts = 10,
                    type = PostType.REQUEST,
                    ownerId = "ownerA",
                    status = PostStatus.POSTED
                )

            assertEquals(1, results.size)
            assertEquals(a1.uid, (results.first() as Request).uid)
        }
    }

    @Test
    fun getMultiplePosts_searchKeys_filters() {
        runTest {
            val id = repo.getNewUid(PostType.REQUEST)
            val req = request1.copy(uid = id, title = "Need help with Kotlin Concurrency")
            repo.addPost(req)

            val results =
                repo.getMultiplePosts(
                    numberOfPosts = 10,
                    type = PostType.REQUEST,
                    titleContains = "help Kotlin",
                    tags = setOf(PostTag.REOCCURRING)
                )

            assertTrue(results.any { (it as Request).uid == id })
        }
    }

    @Test
    fun getMultiplePosts_filtersByDistance_withinRadius() {
        runTest {
            val nearbyId = repo.getNewUid(PostType.REQUEST)
            val nearbyPost = request1.copy(uid = nearbyId, location = epflLocation)

            val farId = repo.getNewUid(PostType.REQUEST)
            val farPost = request1.copy(uid = farId, location = genevaLocation)

            repo.addPost(nearbyPost)
            repo.addPost(farPost)

            // Search from Lausanne (5km radius should include EPFL but not Geneva)
            val results =
                repo.getMultiplePosts(
                    numberOfPosts = 10,
                    type = PostType.REQUEST,
                    userLocation = lausanneLocation,
                    maxDistanceKm = 5.0
                )

            assertEquals(1, results.size)
            assertEquals(nearbyId, (results.first() as Request).uid)
        }
    }

    @Test
    fun getMultiplePosts_filtersByDistance_outsideRadius() {
        runTest {
            val id = repo.getNewUid(PostType.REQUEST)
            val post = request1.copy(uid = id, location = genevaLocation)
            repo.addPost(post)

            // Search from EPFL with 10km radius (Geneva is ~50km away)
            val results =
                repo.getMultiplePosts(
                    numberOfPosts = 10,
                    type = PostType.REQUEST,
                    userLocation = epflLocation,
                    maxDistanceKm = 10.0
                )

            assertTrue(results.isEmpty())
        }
    }

    @Test
    fun getMultiplePosts_noLocationFilter_returnsAll() {
        runTest {
            val id1 = repo.getNewUid(PostType.REQUEST)
            val id2 = repo.getNewUid(PostType.REQUEST)
            val post1 = request1.copy(uid = id1, location = epflLocation)
            val post2 = request1.copy(uid = id2, location = genevaLocation)

            repo.addPost(post1)
            repo.addPost(post2)

            // No location filter provided
            val results = repo.getMultiplePosts(numberOfPosts = 10, type = PostType.REQUEST)

            assertEquals(2, results.size)
        }
    }

    @Test
    fun getMultiplePosts_combinedFilters_searchKeysAndDistance() {
        runTest {
            val nearbyMatchId = repo.getNewUid(PostType.REQUEST)
            val nearbyMatch =
                request1.copy(
                    uid = nearbyMatchId,
                    title = "Kotlin Expert Needed",
                    location = lausanneLocation
                )

            val farMatchId = repo.getNewUid(PostType.REQUEST)
            val farMatch =
                request1.copy(
                    uid = farMatchId,
                    title = "Kotlin Help Required",
                    location = genevaLocation
                )

            val nearbyNoMatchId = repo.getNewUid(PostType.REQUEST)
            val nearbyNoMatch =
                request1.copy(
                    uid = nearbyNoMatchId,
                    title = "Python Programming",
                    location = epflLocation
                )

            repo.addPost(nearbyMatch)
            repo.addPost(farMatch)
            repo.addPost(nearbyNoMatch)

            // Search for "Kotlin" within 10km of EPFL
            val results =
                repo.getMultiplePosts(
                    numberOfPosts = 10,
                    type = PostType.REQUEST,
                    titleContains = "Kotlin",
                    userLocation = epflLocation,
                    maxDistanceKm = 10.0
                )

            // Should only return nearbyMatch (nearby AND contains "Kotlin")
            assertEquals(1, results.size)
            assertEquals(nearbyMatchId, (results.first() as Request).uid)
        }
    }

    @Test
    fun getPost_wrongType_throws() {
        runTest {
            val id = repo.getNewUid(PostType.REQUEST)
            val req = request1.copy(uid = id)
            repo.addPost(req)

            assertThrows(IllegalStateException::class.java) {
                runTest { repo.getPost(PostType.OFFER, id) }
            }
        }
    }

    @Test
    fun deletePost_nonexistent_throws() {
        runTest {
            val ghost = repo.getNewUid(PostType.REQUEST)

            assertThrows(IllegalStateException::class.java) {
                runTest { repo.deletePost(PostType.REQUEST, ghost) }
            }
        }
    }

    @Test
    fun getUid_Offer_fails() {
        assertThrows(NotImplementedError::class.java) { repo.getNewUid(PostType.OFFER) }
    }
}

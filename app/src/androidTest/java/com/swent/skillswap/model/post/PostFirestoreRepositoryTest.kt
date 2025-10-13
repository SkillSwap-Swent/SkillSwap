package com.swent.skillswap.model.post

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.utils.FirebaseEmulator
import java.util.Date
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostRepositoryInstrumentedTest {

    private lateinit var repo: PostRepository

    private val request1 =
        Request(
            uid = "123",
            title = "Need help with Kotlin",
            description = "Looking for an expert to teach me Kotlin.",
            ownerId = "user456",
            tags = listOf(PostTag.REOCCURRING),
            expiry = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            creation = Timestamp.now(),
            status = PostStatus.POSTED,
            media = listOf("media_url_1", "media_url_2"),
            paymentMethods = listOf(PaymentMethod.SKILLS, PaymentMethod.CASH)
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
    fun addAndGet_roundTrip_success() = runBlocking {
        val id = repo.getNewUid(PostType.REQUEST)
        val req = request1.copy(uid = id)

        repo.addPost(req)

        val fetched = repo.getPost(PostType.REQUEST, id) as Request
        assertEquals(req.uid, fetched.uid)
        assertEquals(req.title, fetched.title)
        assertEquals(req.description, fetched.description)
        assertEquals(req.ownerId, fetched.ownerId)
        assertEquals(req.tags, fetched.tags)
        assertEquals(req.paymentMethods, fetched.paymentMethods)
        assertEquals(req.status, fetched.status)
        assertEquals(req.media, fetched.media)
        assertEquals(PostType.REQUEST, fetched.type)
    }

    @Test
    fun addPost_invalid_throws() = runBlocking {
        val badId: String = repo.getNewUid(PostType.REQUEST)
        val badTitle: Request = request1.copy(uid = badId, title = "")
        val badDescription: Request = request1.copy(uid = badId, description = "")
        val badOwner: Request = request1.copy(uid = badId, ownerId = "")

        try {
            repo.addPost(badTitle)
            fail("Expected IllegalArgumentException when adding invalid post")
        } catch (_: IllegalArgumentException) {
            // expected — test passes
        }
        try {
            repo.addPost(badDescription)
            fail("Expected IllegalArgumentException when adding invalid post")
        } catch (_: IllegalArgumentException) {
            // expected — test passes
        }
        try {
            repo.addPost(badOwner)
            fail("Expected IllegalArgumentException when adding invalid post")
        } catch (_: IllegalArgumentException) {
            // expected — test passes
        }
    }

    @Test
    fun editPost_invalid_throws() = runBlocking {
        val id = repo.getNewUid(PostType.REQUEST)
        val original = request1.copy(uid = id, title = "Need help with Kotlin")
        repo.addPost(original)
        val bad: Request = original.copy(title = "")

        try {
            repo.editPost(id, bad)
            fail("Expected IllegalArgumentException when adding invalid post")
        } catch (_: IllegalArgumentException) {
            // expected — test passes
        }
    }

    @Test
    fun editPost_overwritesDocument() = runBlocking {
        val id = repo.getNewUid(PostType.REQUEST)
        val original = request1.copy(uid = id, title = "Need help with Kotlin")
        repo.addPost(original)

        val updated =
            original.copy(
                title = "Need help with Advanced Kotlin",
                description = "Coroutines & Flows deep dive"
            )
        repo.editPost(id, updated)

        val fetched = repo.getPost(PostType.REQUEST, id) as Request
        assertEquals("Need help with Advanced Kotlin", fetched.title)
        assertEquals("Coroutines & Flows deep dive", fetched.description)
    }

    @Test
    fun deletePost_removesDocument() = runBlocking {
        val id: String = repo.getNewUid(PostType.REQUEST)
        val req: Request = request1.copy(uid = id)
        repo.addPost(req)

        repo.deletePost(PostType.REQUEST, id)

        try {
            repo.getPost(PostType.REQUEST, id)
            fail("Expected IllegalArgumentException when fetching a deleted post")
        } catch (_: IllegalArgumentException) {
            // ✅ expected — test passes
        }
    }

    @Test
    fun getMultiplePosts_filters_owner_and_status() = runBlocking {
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

    @Test
    fun getMultiplePosts_searchKeys_filters() = runBlocking {
        val id = repo.getNewUid(PostType.REQUEST)
        val req = request1.copy(uid = id, title = "Need help with Kotlin Concurrency")
        repo.addPost(req)

        val results =
            repo.getMultiplePosts(
                numberOfPosts = 10,
                type = PostType.REQUEST,
                titleContains = "help Kotlin",
                tags = listOf(PostTag.REOCCURRING),
                paymentMethods = listOf(PaymentMethod.SKILLS)
            )

        assertTrue(results.any { (it as Request).uid == id })
    }
}

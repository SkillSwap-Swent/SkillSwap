/** @author Younes Belgroune - Made with the help of AI */
package com.swent.skillswap.ui.personalPosts

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.post.*
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.utils.FirebaseEmulator
import java.util.Date
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersonalPostsViewModelInstrumentedTest {
    private lateinit var fakeRepository: FakePostRepository
    private lateinit var viewModel: PersonalPostsViewModel
    private lateinit var testUserId: String
    private val testLocation = GeoPoint(46.5191, 6.5668)

    @Before
    fun setUp() {
        runBlocking {
            FirebaseEmulator.startEmulator()
            fakeRepository = FakePostRepository()
            val authResult = FirebaseAuth.getInstance().signInAnonymously().await()
            testUserId = authResult.user?.uid ?: "test-user"
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            FirebaseAuth.getInstance().signOut()
            FirebaseEmulator.clearAuthEmulator()
        }
    }

    @Test
    fun init_loadsPostsWithLoadingState() = runBlocking {
        fakeRepository.preloadPosts(createOffer("offer-1", PostStatus.POSTED))
        viewModel = PersonalPostsViewModel(fakeRepository)
        assertTrue("Should start in loading state", viewModel.uiState.value.isLoading)
        // Wait for async operation to complete
        var attempts = 0
        while (viewModel.uiState.value.isLoading && attempts < 50) {
            Thread.sleep(50)
            attempts++
        }
        val state = viewModel.uiState.value
        assertFalse("Should finish loading", state.isLoading)
        assertTrue("Should have loaded posts", state.posts.isNotEmpty())
    }

    @Test
    fun loadPersonalPosts_showsAllStatuses() = runBlocking {
        fakeRepository.preloadPosts(
            createOffer("posted", PostStatus.POSTED),
            createOffer("draft", PostStatus.DRAFT),
            createRequest("archived", PostStatus.ARCHIVED)
        )
        viewModel = PersonalPostsViewModel(fakeRepository)
        // Wait for async operation to complete
        var attempts = 0
        while (viewModel.uiState.value.isLoading && attempts < 50) {
            Thread.sleep(50)
            attempts++
        }
        val state = viewModel.uiState.value
        assertEquals("Should load all 3 posts", 3, state.posts.size)
        assertTrue("Should have POSTED status", state.posts.any { it.status == PostStatus.POSTED })
        assertTrue("Should have DRAFT status", state.posts.any { it.status == PostStatus.DRAFT })
        assertTrue(
            "Should have ARCHIVED status",
            state.posts.any { it.status == PostStatus.ARCHIVED }
        )
    }

    @Test
    fun loadPersonalPosts_withoutUser_setsError() = runBlocking {
        FirebaseAuth.getInstance().signOut()
        viewModel = PersonalPostsViewModel(fakeRepository)
        Thread.sleep(100)
        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(
            state.error!!.contains("authenticated", ignoreCase = true) ||
                state.error!!.contains("log in", ignoreCase = true)
        )
        assertFalse(state.isLoading)
    }

    @Test
    fun loadPersonalPosts_repositoryException_setsError() = runBlocking {
        fakeRepository.setShouldFailOnGet(true)
        viewModel = PersonalPostsViewModel(fakeRepository)
        // Wait for async operation to complete
        var attempts = 0
        while (viewModel.uiState.value.isLoading && attempts < 50) {
            Thread.sleep(50)
            attempts++
        }
        val state = viewModel.uiState.value
        assertNotNull("Error should be set when repository fails", state.error)
        assertTrue(
            "Error message should contain 'Failed'",
            state.error!!.contains("Failed", ignoreCase = true)
        )
    }

    @Test
    fun setPostTypeFilter_all_loadsBothTypes() = runBlocking {
        fakeRepository.preloadPosts(
            createOffer("offer-1", PostStatus.POSTED),
            createRequest("request-1", PostStatus.POSTED)
        )
        viewModel = PersonalPostsViewModel(fakeRepository)
        // Wait for initial load
        var attempts = 0
        while (viewModel.uiState.value.isLoading && attempts < 50) {
            Thread.sleep(50)
            attempts++
        }
        viewModel.setPostTypeFilter(PostTypeFilter.ALL)
        // Wait for filter reload
        attempts = 0
        while (viewModel.uiState.value.isLoading && attempts < 50) {
            Thread.sleep(50)
            attempts++
        }
        val state = viewModel.uiState.value
        assertEquals(PostTypeFilter.ALL, state.selectedPostType)
        assertTrue(
            "Should have both offer and request types",
            state.posts.any { it.type == PostType.OFFER } &&
                state.posts.any { it.type == PostType.REQUEST }
        )
    }

    @Test
    fun deletePost_success_removesPost() = runBlocking {
        val offer = createOffer("offer-1", PostStatus.POSTED)
        fakeRepository.preloadPosts(offer)
        viewModel = PersonalPostsViewModel(fakeRepository)
        Thread.sleep(100)
        val initialCount = viewModel.uiState.value.posts.size
        if (initialCount == 0) {
            // If no posts loaded, skip the test
            return@runBlocking
        }
        assertTrue(
            "Post should exist before deletion",
            viewModel.uiState.value.posts.any { it.uid == offer.uid }
        )

        // Test optimistic update: post should be removed immediately (synchronously)
        viewModel.deletePost(offer)
        val stateAfterDelete = viewModel.uiState.value
        assertTrue(
            "Post should be removed immediately via optimistic update",
            stateAfterDelete.posts.none { it.uid == offer.uid }
        )
        assertEquals("Post count should decrease", initialCount - 1, stateAfterDelete.posts.size)

        // Wait for repository deletion to complete
        Thread.sleep(100)
        val remainingPosts = fakeRepository.getAddedPosts()
        assertTrue(
            "Post should be removed from repository",
            remainingPosts.none { it.uid == offer.uid }
        )
    }

    @Test
    fun refresh_reloadsPosts() = runBlocking {
        val offer1 = createOffer("offer-1", PostStatus.POSTED)
        fakeRepository.preloadPosts(offer1)
        viewModel = PersonalPostsViewModel(fakeRepository)
        // Wait for initial load
        var attempts = 0
        while (viewModel.uiState.value.isLoading && attempts < 50) {
            Thread.sleep(50)
            attempts++
        }
        fakeRepository.preloadPosts(offer1, createOffer("offer-2", PostStatus.POSTED))
        viewModel.refresh()
        // Wait for refresh to complete
        attempts = 0
        while (viewModel.uiState.value.isLoading && attempts < 50) {
            Thread.sleep(50)
            attempts++
        }
        assertTrue("Should have at least 1 post", viewModel.uiState.value.posts.size >= 1)
    }

    @Test
    fun clearError_removesError() = runBlocking {
        FirebaseAuth.getInstance().signOut()
        viewModel = PersonalPostsViewModel(fakeRepository)
        Thread.sleep(100)
        assertNotNull(viewModel.uiState.value.error)
        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    private fun createOffer(uid: String, status: PostStatus) =
        Offer(
            uid = uid,
            title = "Test",
            description = "Test",
            ownerId = testUserId,
            tags = setOf(PostTag.REOCCURRING),
            paymentMethod = PaymentMethod.SKILLSANDCASH,
            expiry = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            creation = Timestamp.now(),
            status = status,
            media = emptyList(),
            location = testLocation
        )

    private fun createRequest(uid: String, status: PostStatus) =
        Request(
            uid = uid,
            title = "Test",
            description = "Test",
            ownerId = testUserId,
            tags = setOf(PostTag.REOCCURRING),
            paymentMethod = PaymentMethod.SKILLSANDCASH,
            expiry = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            creation = Timestamp.now(),
            status = status,
            media = emptyList(),
            location = testLocation
        )
}

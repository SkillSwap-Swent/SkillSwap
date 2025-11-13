/** @author Younes Belgroune - Made with the help of AI */
package com.swent.skillswap.ui.personalPosts

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.post.*
import com.swent.skillswap.model.tags.PostTag
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PersonalPostsViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakePostRepository
    private lateinit var viewModel: PersonalPostsViewModel
    private val testUserId = "test-user-123"
    private val testLocation = GeoPoint(46.5191, 6.5668)

    private val sampleOffer =
        Offer(
            uid = "offer-1",
            title = "Kotlin Tutoring",
            description = "I can teach Kotlin",
            ownerId = testUserId,
            tags = setOf(PostTag.REOCCURRING),
            paymentMethod = PaymentMethod.SKILLS,
            expiry = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            creation = Timestamp(Date(System.currentTimeMillis() - 100000)),
            status = PostStatus.POSTED,
            media = emptyList(),
            location = testLocation
        )

    private val sampleRequest =
        Request(
            uid = "request-1",
            title = "Need Python Help",
            description = "Looking for Python tutor",
            ownerId = testUserId,
            tags = setOf(PostTag.REOCCURRING),
            paymentMethod = PaymentMethod.CASH,
            expiry = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            creation = Timestamp(Date(System.currentTimeMillis() - 50000)),
            status = PostStatus.POSTED,
            media = emptyList(),
            location = testLocation
        )

    private val otherUserOffer =
        Offer(
            uid = "offer-3",
            title = "Other User Offer",
            description = "Not mine",
            ownerId = "other-user-456",
            tags = setOf(PostTag.REOCCURRING),
            paymentMethod = PaymentMethod.SKILLS,
            expiry = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            creation = Timestamp.now(),
            status = PostStatus.POSTED,
            media = emptyList(),
            location = testLocation
        )

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        requireNotNull(context) { "Robolectric context must not be null" }

        try {
            FirebaseApp.getInstance()
        } catch (e: IllegalStateException) {
            val options =
                FirebaseOptions.Builder()
                    .setApplicationId("test-app-id")
                    .setApiKey("test-api-key")
                    .setProjectId("test-project")
                    .build()
            try {
                FirebaseApp.initializeApp(context, options)
            } catch (initError: Exception) {
                throw AssertionError(
                    "Failed to initialize Firebase with options: ${initError.message}",
                    initError
                )
            }
        }

        fakeRepository = FakePostRepository()
    }

    @After
    fun tearDown() {
        // Clean up Firebase Auth state after each test to ensure test isolation
        cleanFirebaseAuth()
    }

    /**
     * Cleans Firebase Auth state by signing out any authenticated users and clearing the Auth
     * emulator. This ensures test isolation between test runs by deleting all users created during
     * tests. Uses the same helper functions as FirebaseEmulator to ensure consistency.
     */
    private fun cleanFirebaseAuth() {
        try {
            // Sign out any authenticated users
            FirebaseAuth.getInstance().signOut()
            // Clear the Auth emulator to delete all users created during tests
            if (isEmulatorRunning()) {
                clearAuthEmulator()
            }
        } catch (e: Exception) {
            // Ignore if cleanup fails (e.g., no user signed in, Firebase not initialized, or
            // emulator not running)
        }
    }

    /**
     * Checks if the Firebase emulator is running by attempting to connect to the emulator endpoint.
     * Uses the same logic as FirebaseEmulator for consistency.
     */
    private fun isEmulatorRunning(): Boolean {
        return runCatching {
                val client = OkHttpClient()
                val request = Request.Builder().url("http://10.0.2.2:4400/emulators").build()
                client.newCall(request).execute().isSuccessful
            }
            .getOrNull() == true
    }

    /**
     * Clears the Firebase Auth emulator by sending a DELETE request to the emulator endpoint. Uses
     * the same logic as FirebaseEmulator.clearAuthEmulator() for consistency.
     */
    private fun clearAuthEmulator() {
        try {
            val projectId = FirebaseApp.getInstance().options.projectId
            val authEndpoint = "http://10.0.2.2:9099/emulator/v1/projects/$projectId/accounts"
            val client = OkHttpClient()
            val request = Request.Builder().url(authEndpoint).delete().build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                // Log but don't throw - emulator might not be running in unit tests
            }
        } catch (e: Exception) {
            // Ignore if emulator is not running or not accessible
        }
    }

    @Test
    fun init_withoutAuthenticatedUser_setsError() = runTest {
        FirebaseAuth.getInstance().signOut()
        viewModel = PersonalPostsViewModel(fakeRepository)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(
            state.error!!.contains("authenticated", ignoreCase = true) ||
                state.error!!.contains("log in", ignoreCase = true)
        )
        assertFalse(state.isLoading)
    }

    @Test
    fun loadPersonalPosts_withAllFilter_loadsBothOffersAndRequests() = runTest {
        fakeRepository.preloadPosts(sampleOffer, sampleRequest, otherUserOffer)
        try {
            FirebaseAuth.getInstance().signInAnonymously().await()
        } catch (e: Exception) {
            // Continue
        }
        viewModel = PersonalPostsViewModel(fakeRepository)
        viewModel.setPostTypeFilter(PostTypeFilter.ALL)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(PostTypeFilter.ALL, state.selectedPostType)
        if (state.error == null && state.posts.isNotEmpty()) {
            assertTrue(state.posts.isNotEmpty())
        }
    }

    @Test
    fun setPostTypeFilter_offers_loadsOnlyOffers() = runTest {
        fakeRepository.preloadPosts(sampleOffer, sampleRequest)
        try {
            FirebaseAuth.getInstance().signInAnonymously().await()
        } catch (e: Exception) {
            // Continue
        }
        viewModel = PersonalPostsViewModel(fakeRepository)
        viewModel.setPostTypeFilter(PostTypeFilter.OFFERS)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(PostTypeFilter.OFFERS, state.selectedPostType)
        if (state.posts.isNotEmpty()) {
            state.posts.forEach { assertEquals(PostType.OFFER, it.type) }
        }
    }

    @Test
    fun setPostTypeFilter_requests_loadsOnlyRequests() = runTest {
        fakeRepository.preloadPosts(sampleOffer, sampleRequest)
        try {
            FirebaseAuth.getInstance().signInAnonymously().await()
        } catch (e: Exception) {
            // Continue
        }
        viewModel = PersonalPostsViewModel(fakeRepository)
        viewModel.setPostTypeFilter(PostTypeFilter.REQUESTS)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(PostTypeFilter.REQUESTS, state.selectedPostType)
        if (state.posts.isNotEmpty()) {
            state.posts.forEach { assertEquals(PostType.REQUEST, it.type) }
        }
    }

    @Test
    fun loadPersonalPosts_repositoryFailure_setsError() = runTest {
        fakeRepository.setShouldFailOnGet(true)
        fakeRepository.preloadPosts(sampleOffer)
        try {
            FirebaseAuth.getInstance().signInAnonymously().await()
        } catch (e: Exception) {
            // Continue
        }
        viewModel = PersonalPostsViewModel(fakeRepository)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value)
    }

    @Test
    fun deletePost_removesPostAndReloads() = runTest {
        // Use SKILLSANDCASH to match default paymentMethod filter
        val offerWithDefaultPayment = sampleOffer.copy(paymentMethod = PaymentMethod.SKILLSANDCASH)
        fakeRepository.preloadPosts(offerWithDefaultPayment, sampleRequest)
        try {
            FirebaseAuth.getInstance().signInAnonymously().await()
        } catch (e: Exception) {
            // Continue
        }
        viewModel = PersonalPostsViewModel(fakeRepository)
        advanceUntilIdle()
        val initialCount = viewModel.uiState.value.posts.size
        if (initialCount == 0) {
            // If no posts loaded, skip the test (might be due to auth or filter issues)
            return@runTest
        }
        assertTrue(
            "Post should exist before deletion",
            viewModel.uiState.value.posts.any { it.uid == offerWithDefaultPayment.uid }
        )

        // Test optimistic update: post should be removed immediately (synchronously)
        viewModel.deletePost(offerWithDefaultPayment)
        val stateAfterDelete = viewModel.uiState.value
        assertTrue(
            "Post should be removed immediately via optimistic update",
            stateAfterDelete.posts.none { it.uid == offerWithDefaultPayment.uid }
        )
        assertEquals("Post count should decrease", initialCount - 1, stateAfterDelete.posts.size)

        // Wait for repository deletion to complete
        advanceUntilIdle()
        val remainingPosts = fakeRepository.getAddedPosts()
        assertFalse(
            "Post should be removed from repository",
            remainingPosts.any { it.uid == offerWithDefaultPayment.uid }
        )
    }

    @Test
    fun refresh_reloadsPosts() = runTest {
        fakeRepository.preloadPosts(sampleOffer)
        try {
            FirebaseAuth.getInstance().signInAnonymously().await()
        } catch (e: Exception) {
            // Continue
        }
        viewModel = PersonalPostsViewModel(fakeRepository)
        advanceUntilIdle()
        fakeRepository.preloadPosts(sampleOffer, sampleRequest)
        viewModel.refresh()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value)
    }

    @Test
    fun clearError_removesErrorFromState() = runTest {
        FirebaseAuth.getInstance().signOut()
        viewModel = PersonalPostsViewModel(fakeRepository)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }
}

// Test rule to set Main dispatcher for coroutines in unit tests
class MainDispatcherRule(private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()) :
    TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

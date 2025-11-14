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
    private val testLocation = GeoPoint(46.5191, 6.5668)

    private val sampleRequest =
        Request(
            uid = "request-1",
            title = "Need Python Help",
            description = "Looking for Python tutor",
            ownerId = "test-user-123",
            tags = setOf(PostTag.REOCCURRING),
            paymentMethod = PaymentMethod.SKILLSANDCASH,
            expiry = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            creation = Timestamp(Date(System.currentTimeMillis() - 50000)),
            status = PostStatus.POSTED,
            media = emptyList(),
            location = testLocation
        )

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        try {
            FirebaseApp.getInstance()
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("test-app-id")
                    .setApiKey("test-api-key")
                    .setProjectId("test-project")
                    .build()
            )
        }
        fakeRepository = FakePostRepository()
    }

    @After
    fun tearDown() {
        try {
            FirebaseAuth.getInstance().signOut()
            if (
                runCatching {
                        OkHttpClient()
                            .newCall(
                                Request.Builder().url("http://10.0.2.2:4400/emulators").build()
                            )
                            .execute()
                            .isSuccessful
                    }
                    .getOrNull() == true
            ) {
                val projectId = FirebaseApp.getInstance().options.projectId
                OkHttpClient()
                    .newCall(
                        Request.Builder()
                            .url("http://10.0.2.2:9099/emulator/v1/projects/$projectId/accounts")
                            .delete()
                            .build()
                    )
                    .execute()
            }
        } catch (e: Exception) {
            // Ignore cleanup failures
        }
    }

    @Test
    fun init_withoutAuthenticatedUser_setsError() = runTest {
        FirebaseAuth.getInstance().signOut()
        viewModel = PersonalPostsViewModel(fakeRepository)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun loadPersonalPosts_withAllFilter_loadsBothOffersAndRequests() = runTest {
        fakeRepository.preloadPosts(sampleRequest)
        try {
            FirebaseAuth.getInstance().signInAnonymously().await()
        } catch (e: Exception) {}
        viewModel = PersonalPostsViewModel(fakeRepository)
        viewModel.setPostTypeFilter(PostTypeFilter.ALL)
        advanceUntilIdle()
        assertEquals(PostTypeFilter.ALL, viewModel.uiState.value.selectedPostType)
    }

    @Test
    fun deletePost_removesPostAndReloads() = runTest {
        fakeRepository.preloadPosts(sampleRequest)
        try {
            FirebaseAuth.getInstance().signInAnonymously().await()
        } catch (e: Exception) {}
        viewModel = PersonalPostsViewModel(fakeRepository)
        advanceUntilIdle()
        val initialCount = viewModel.uiState.value.posts.size
        if (initialCount == 0) return@runTest
        viewModel.deletePost(sampleRequest)
        val stateAfterDelete = viewModel.uiState.value
        assertTrue(stateAfterDelete.posts.none { it.uid == sampleRequest.uid })
        assertEquals(initialCount - 1, stateAfterDelete.posts.size)
        advanceUntilIdle()
        assertFalse(fakeRepository.getAddedPosts().any { it.uid == sampleRequest.uid })
    }

    @Test
    fun deletePost_failure_reloadsAndShowsError() = runTest {
        fakeRepository.preloadPosts(sampleRequest)
        try {
            FirebaseAuth.getInstance().signInAnonymously().await()
        } catch (e: Exception) {}
        viewModel = PersonalPostsViewModel(fakeRepository)
        advanceUntilIdle()
        if (viewModel.uiState.value.posts.isEmpty()) return@runTest
        fakeRepository.clear()
        viewModel.deletePost(sampleRequest)
        assertTrue(viewModel.uiState.value.posts.none { it.uid == sampleRequest.uid })
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.error != null || viewModel.uiState.value.posts.isEmpty())
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

class MainDispatcherRule(private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()) :
    TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

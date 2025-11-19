/* With the help of Sonnet 4.5 for repetitive tasks */

package com.swent.skillswap.post

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.post.*
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.ui.post.PostOperation
import com.swent.skillswap.ui.post.RequestViewModel
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class RequestViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakePostRepository
    private lateinit var viewModel: RequestViewModel
    private val testUserId = "test-user-123"

    private val testLocation = GeoPoint(46.5191, 6.5668)

    // Test fixture
    private val sampleRequest =
        Request(
            uid = "existing-request-1",
            title = "Need Kotlin Help",
            description = "Looking for someone to teach Kotlin basics",
            ownerId = testUserId,
            tags = setOf(PostTag.REOCCURRING),
            paymentMethod = PaymentMethod.SKILLSANDCASH,
            expiry = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            creation = Timestamp.now(),
            status = PostStatus.POSTED,
            media = emptyList(),
            location = testLocation
        )

    @Before
    fun setUp() {
        fakeRepository = FakePostRepository()
    }

    // ========== INITIALIZATION TESTS ==========

    @Test
    fun init_withoutPostId_hasEmptyState() = runTest {
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = null)

        val state = viewModel.uiState.value
        assertEquals("", state.uid)
        assertEquals("", state.title)
        assertEquals("", state.description)
        assertTrue(state.tags.isEmpty())
        assertTrue(state.paymentMethod == PaymentMethod.SKILLS)
        assertFalse(state.isLoading)
        assertFalse(state.isSubmitSuccessful)
    }

    @Test
    fun init_withPostId_loadsExistingPost() = runTest {
        fakeRepository.preloadPosts(sampleRequest)
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = sampleRequest.uid)

        // Wait for coroutine to complete
        kotlinx.coroutines.delay(100)

        val state = viewModel.uiState.value
        assertEquals(sampleRequest.uid, state.uid)
        assertEquals(sampleRequest.title, state.title)
        assertEquals(sampleRequest.description, state.description)
        assertEquals(sampleRequest.tags, state.tags)
        assertEquals(sampleRequest.paymentMethod, state.paymentMethod)
    }

    @Test
    fun init_withInvalidPostId_setsError() = runTest {
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = "non-existent")

        kotlinx.coroutines.delay(100)

        val state = viewModel.uiState.value
        assertNotNull(state.submitError)
        assertTrue(state.submitError!!.contains("Failed to load post"))
    }

    // ========== TITLE VALIDATION TESTS ==========

    @Test
    fun setTitle_validTitle_updatesStateAndClearsError() = runTest {
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = null)

        viewModel.setTitle("Valid Title")

        val state = viewModel.uiState.value
        assertEquals("Valid Title", state.title)
        assertEquals("", state.titleError)
    }

    @Test
    fun setTitle_emptyTitle_setsError() = runTest {
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = null)

        viewModel.setTitle("")

        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertEquals("Title cannot be empty", state.titleError)
    }

    @Test
    fun setTitle_blankTitle_setsError() = runTest {
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = null)

        viewModel.setTitle("   ")

        val state = viewModel.uiState.value
        assertEquals("Title cannot be empty", state.titleError)
    }

    // ========== DESCRIPTION VALIDATION TESTS ==========

    @Test
    fun setDescription_validDescription_updatesStateAndClearsError() = runTest {
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = null)

        viewModel.setDescription("Valid description")

        val state = viewModel.uiState.value
        assertEquals("Valid description", state.description)
        assertEquals("", state.descriptionError)
    }

    @Test
    fun setDescription_emptyDescription_setsError() = runTest {
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = null)

        viewModel.setDescription("")

        val state = viewModel.uiState.value
        assertEquals("Description cannot be empty", state.descriptionError)
    }

    // ========== TAG MANAGEMENT TESTS ==========

    @Test
    fun addTag_newTag_addsToListAndClearsError() = runTest {
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = null)

        viewModel.addTag(PostTag.REOCCURRING)

        val state = viewModel.uiState.value
        assertTrue(state.tags.contains(PostTag.REOCCURRING))
        assertEquals("", state.tagsError)
    }

    @Test
    fun addTag_duplicateTag_doesNotAddTwice() = runTest {
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = null)

        viewModel.addTag(PostTag.REOCCURRING)
        viewModel.addTag(PostTag.REOCCURRING)

        val state = viewModel.uiState.value
        assertEquals(1, state.tags.count { it == PostTag.REOCCURRING })
    }

    @Test
    fun removeTag_existingTag_removesFromList() = runTest {
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = null)

        viewModel.addTag(PostTag.REOCCURRING)
        viewModel.removeTag(PostTag.REOCCURRING)

        val state = viewModel.uiState.value
        assertFalse(state.tags.contains(PostTag.REOCCURRING))
    }

    @Test
    fun removeTag_nonExistentTag_doesNotCrash() = runTest {
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = null)

        // Should not throw exception
        viewModel.removeTag(PostTag.REOCCURRING)

        val state = viewModel.uiState.value
        assertFalse(state.tags.contains(PostTag.REOCCURRING))
    }

    // ========== PAYMENT METHOD TESTS ==========

    @Test
    fun togglePaymentMethod_notPresent_adds() = runTest {
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = null)

        viewModel.togglePaymentMethod(PaymentMethod.CASH)

        val state = viewModel.uiState.value
        assertTrue(state.paymentMethod == PaymentMethod.CASH)
    }

    @Test
    fun togglePaymentMethod_present_doesNotAlter() = runTest {
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = null)

        viewModel.togglePaymentMethod(PaymentMethod.CASH)
        viewModel.togglePaymentMethod(PaymentMethod.CASH)

        val state = viewModel.uiState.value
        assertTrue(state.paymentMethod == PaymentMethod.CASH)
    }

    // ========== SAVE - ADD MODE TESTS ==========

    @Test
    fun save_addMode_validData_createsNewPost() = runTest {
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = null)

        viewModel.setTitle("Test Request")
        viewModel.setDescription("Test Description")
        viewModel.addTag(PostTag.REOCCURRING)
        viewModel.togglePaymentMethod(PaymentMethod.CASH)

        viewModel.save(PostOperation.ADD)
        kotlinx.coroutines.delay(100)

        val addedPosts = fakeRepository.getAddedPosts()
        assertEquals(1, addedPosts.size)
        val addedPost = addedPosts.first() as Request
        assertEquals("Test Request", addedPost.title)
        assertEquals("Test Description", addedPost.description)
        assertEquals(testUserId, addedPost.ownerId)
        assertTrue(addedPost.tags.contains(PostTag.REOCCURRING))
        assertTrue(addedPost.paymentMethod == PaymentMethod.CASH)
    }

    @Test
    fun save_addMode_success_setsSubmitSuccessful() = runTest {
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = null)

        viewModel.setTitle("Test")
        viewModel.setDescription("Test")
        viewModel.addTag(PostTag.REOCCURRING)
        viewModel.togglePaymentMethod(PaymentMethod.CASH)

        viewModel.save(PostOperation.ADD)
        kotlinx.coroutines.delay(100)

        val state = viewModel.uiState.value
        assertTrue(state.isSubmitSuccessful)
        assertFalse(state.isLoading)
    }

    @Test
    fun save_addMode_repositoryFailure_setsError() = runTest {
        fakeRepository.setShouldFailOnAdd(true)
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = null)

        viewModel.setTitle("Test")
        viewModel.setDescription("Test")
        viewModel.addTag(PostTag.REOCCURRING)
        viewModel.togglePaymentMethod(PaymentMethod.CASH)

        viewModel.save(PostOperation.ADD)
        kotlinx.coroutines.delay(100)

        val state = viewModel.uiState.value
        assertNotNull(state.submitError)
        assertFalse(state.isSubmitSuccessful)
    }

    // ========== SAVE - EDIT MODE TESTS ==========

    @Test
    fun save_editMode_validData_updatesExistingPost() = runTest {
        fakeRepository.preloadPosts(sampleRequest)
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = sampleRequest.uid)

        kotlinx.coroutines.delay(100)

        viewModel.setTitle("Updated Title")
        viewModel.save(PostOperation.EDIT)
        kotlinx.coroutines.delay(100)

        val updatedPost = fakeRepository.getPostById(sampleRequest.uid) as Request
        assertEquals("Updated Title", updatedPost.title)
        assertEquals(sampleRequest.uid, updatedPost.uid)
    }

    @Test
    fun save_editMode_usesExistingPostId() = runTest {
        fakeRepository.preloadPosts(sampleRequest)
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = sampleRequest.uid)

        kotlinx.coroutines.delay(100)

        viewModel.save(PostOperation.EDIT)
        kotlinx.coroutines.delay(100)

        val posts = fakeRepository.getAddedPosts()
        assertEquals(1, posts.size)
        assertEquals(sampleRequest.uid, posts.first().uid)
    }

    @Test
    fun save_editMode_repositoryFailure_setsError() = runTest {
        fakeRepository.preloadPosts(sampleRequest)
        fakeRepository.setShouldFailOnEdit(true)
        viewModel = RequestViewModel(null, fakeRepository, testUserId, postId = sampleRequest.uid)

        kotlinx.coroutines.delay(100)

        viewModel.save(PostOperation.EDIT)
        kotlinx.coroutines.delay(100)

        val state = viewModel.uiState.value
        assertNotNull(state.submitError)
        assertFalse(state.isSubmitSuccessful)
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

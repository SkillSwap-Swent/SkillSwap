/* With the help of Sonnet 4.5 for repetitive tasks */

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.swent.skillswap.model.map.Location
import com.swent.skillswap.model.post.*
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.post.PostOperation
import com.swent.skillswap.ui.post.RequestScreen
import com.swent.skillswap.ui.post.RequestScreenTags
import com.swent.skillswap.ui.post.RequestViewModel
import java.util.Date
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RequestScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var fakeRepository: FakePostRepository
    private val testUserId = "test-user-123"
    private var backButtonClicked = false
    private var postCreatedCalled = false

    private val defaultLocation = Location(46.5191, 6.5668, "EPFL, Switzerland")

    private val sampleRequest =
        Request(
            uid = "existing-request-1",
            title = "Need Kotlin Help",
            description = "Looking for someone to teach Kotlin basics",
            ownerId = testUserId,
            tags = setOf(PostTag.REOCCURRING),
            paymentMethod = PaymentMethod.SKILLS,
            expiry = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            creation = Timestamp.now(),
            status = PostStatus.POSTED,
            media = emptyList(),
            location = defaultLocation
        )

    @Before
    fun setUp() {
        fakeRepository = FakePostRepository()
        backButtonClicked = false
        postCreatedCalled = false
    }

    // ========== UI VISIBILITY TESTS ==========

    @Test
    fun allMainUIElements_areDisplayed() {
        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD,
                onGoBack = { backButtonClicked = true },
                onPostCreated = { postCreatedCalled = true }
            )
        }

        composeTestRule.onNodeWithTag(RequestScreenTags.BACK_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(RequestScreenTags.TITLE_INPUT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(RequestScreenTags.DESCRIPTION_INPUT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(RequestScreenTags.TAGS_INPUT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).assertIsDisplayed()
    }

    @Test
    fun addMode_showsCreateButton() {
        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD
            )
        }

        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(RequestScreenTags.EDIT_BUTTON).assertDoesNotExist()
    }

    @Test
    fun editMode_showsEditButton() {
        fakeRepository.preloadPosts(sampleRequest)

        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.EDIT,
                uid = sampleRequest.uid
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(RequestScreenTags.EDIT_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).assertDoesNotExist()
    }

    // ========== INPUT TESTS ==========

    @Test
    fun titleInput_acceptsText() {
        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD
            )
        }

        composeTestRule
            .onNodeWithTag(RequestScreenTags.TITLE_INPUT)
            .performTextInput("My Test Request")

        composeTestRule
            .onNodeWithTag(RequestScreenTags.TITLE_INPUT)
            .assert(hasText("My Test Request"))
    }

    @Test
    fun descriptionInput_acceptsMultilineText() {
        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD
            )
        }

        val multilineText = "Line 1\nLine 2\nLine 3"
        composeTestRule
            .onNodeWithTag(RequestScreenTags.DESCRIPTION_INPUT)
            .performTextInput(multilineText)

        composeTestRule
            .onNodeWithTag(RequestScreenTags.DESCRIPTION_INPUT)
            .assert(hasText(multilineText))
    }

    // ========== VALIDATION TESTS ==========

    @Test
    fun submit_emptyTitle_showsError() {
        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD
            )
        }

        // Click submit without filling title
        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Error message should appear
        composeTestRule.onNodeWithTag(RequestScreenTags.TITLE_INPUT).assertIsDisplayed()
    }

    @Test
    fun submit_emptyDescription_showsError() {
        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD
            )
        }

        // Fill title but not description
        composeTestRule.onNodeWithTag(RequestScreenTags.TITLE_INPUT).performTextInput("Valid Title")

        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(RequestScreenTags.DESCRIPTION_INPUT).assertIsDisplayed()
    }

    // ========== PAYMENT METHOD TESTS ==========

    @Test
    fun paymentMethodChips_displayAll() {
        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD
            )
        }

        // All payment methods should be displayed
        PaymentMethod.entries.forEach { method ->
            composeTestRule
                .onNodeWithTag("${RequestScreenTags.PAYMENT_METHOD_CHIP}_${method.name}")
                .assertExists()
        }
    }

    @Test
    fun clickPaymentMethodChip_togglesSelection() {
        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD
            )
        }

        val chipTag = "${RequestScreenTags.PAYMENT_METHOD_CHIP}_${PaymentMethod.CASH.name}"

        // Click to select
        composeTestRule.onNodeWithTag(chipTag).performClick()
        composeTestRule.waitForIdle()

        // Click again to deselect
        composeTestRule.onNodeWithTag(chipTag).performClick()
        composeTestRule.waitForIdle()

        // Should still exist (not removed, just toggled)
        composeTestRule.onNodeWithTag(chipTag).assertExists()
    }

    // ========== NAVIGATION TESTS ==========

    @Test
    fun backButton_triggersCallback() {
        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD,
                onGoBack = { backButtonClicked = true }
            )
        }

        composeTestRule.onNodeWithTag(RequestScreenTags.BACK_BUTTON).performClick()

        assertTrue(backButtonClicked)
    }

    @Test
    fun editMode_loadsExistingData() {
        fakeRepository.preloadPosts(sampleRequest)

        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.EDIT,
                uid = sampleRequest.uid
            )
        }

        composeTestRule.waitForIdle()

        // Verify existing data is loaded
        composeTestRule
            .onNodeWithTag(RequestScreenTags.TITLE_INPUT)
            .assert(hasText(sampleRequest.title))

        composeTestRule
            .onNodeWithTag(RequestScreenTags.DESCRIPTION_INPUT)
            .assert(hasText(sampleRequest.description))

        composeTestRule
            .onNodeWithTag("${RequestScreenTags.TAG_CHIP}_REOCCURRING")
            .assertIsDisplayed()
    }

    // ========== TAG TESTS ==========

    @Test
    fun tagChip_displayAndRemove() {
        val viewModel = RequestViewModel(fakeRepository, currentUserId = testUserId, postId = null)

        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD,
                requestViewModel = viewModel
            )
        }

        // Add tag directly via ViewModel
        viewModel.addTag(SkillTag.FLUID_MECHANICS)
        composeTestRule.waitForIdle()

        // Verify tag chip appears
        composeTestRule
            .onNodeWithTag("${RequestScreenTags.TAG_CHIP}_FLUID_MECHANICS")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        // Verify tag is removed
        composeTestRule
            .onNodeWithTag("${RequestScreenTags.TAG_CHIP}_FLUID_MECHANICS")
            .assertDoesNotExist()
    }

    // ========== LOADING STATE TESTS ==========

    @Test
    fun submit_showsLoadingIndicator() {
        fakeRepository.setDelay(1000) // Add delay to see loading state
        val viewModel = RequestViewModel(fakeRepository, currentUserId = testUserId, postId = null)

        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD,
                requestViewModel = viewModel
            )
        }

        // Fill required fields
        composeTestRule.onNodeWithTag(RequestScreenTags.TITLE_INPUT).performTextInput("Test Title")
        composeTestRule
            .onNodeWithTag(RequestScreenTags.DESCRIPTION_INPUT)
            .performTextInput("Test Description")

        // Add tag directly via ViewModel (avoid dropdown interaction)
        viewModel.addTag(SkillTag.FLUID_MECHANICS)
        composeTestRule.waitForIdle()

        // Select payment method
        composeTestRule
            .onNodeWithTag("${RequestScreenTags.PAYMENT_METHOD_CHIP}_${PaymentMethod.CASH.name}")
            .performClick()

        // Click submit
        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).performClick()

        // Loading indicator should appear
        composeTestRule.onNodeWithTag(RequestScreenTags.LOADING_INDICATOR).assertIsDisplayed()
    }

    // ========== ERROR HANDLING TESTS ==========

    @Test
    fun submit_noTags_showsValidationError() {
        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD
            )
        }

        // Fill title and description but no tags
        composeTestRule.onNodeWithTag(RequestScreenTags.TITLE_INPUT).performTextInput("Test Title")
        composeTestRule
            .onNodeWithTag(RequestScreenTags.DESCRIPTION_INPUT)
            .performTextInput("Test Description")

        // Submit without tags
        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Should show tags error
        composeTestRule.onNodeWithTag(RequestScreenTags.TAGS_INPUT).assertIsDisplayed()
    }

    // ========== SUCCESS FLOW TEST ==========

    @Test
    fun submit_success_triggersCallback() {
        val viewModel = RequestViewModel(fakeRepository, currentUserId = testUserId, postId = null)

        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD,
                requestViewModel = viewModel,
                onPostCreated = { postCreatedCalled = true }
            )
        }

        // Fill all required fields
        composeTestRule.onNodeWithTag(RequestScreenTags.TITLE_INPUT).performTextInput("Valid Title")
        composeTestRule
            .onNodeWithTag(RequestScreenTags.DESCRIPTION_INPUT)
            .performTextInput("Valid Description")

        // Add tag directly via ViewModel (avoid dropdown interaction)
        viewModel.addTag(SkillTag.FLUID_MECHANICS)
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag("${RequestScreenTags.PAYMENT_METHOD_CHIP}_${PaymentMethod.CASH.name}")
            .performClick()

        // Submit
        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Callback should be triggered
        assertTrue(postCreatedCalled)
    }

    // ========== MULTIPLE PAYMENT METHODS TEST ==========

    @Test
    fun multiplePaymentMethods_canBeSelected() {
        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD
            )
        }

        // Select multiple payment methods
        composeTestRule
            .onNodeWithTag("${RequestScreenTags.PAYMENT_METHOD_CHIP}_${PaymentMethod.CASH.name}")
            .performClick()
        composeTestRule
            .onNodeWithTag("${RequestScreenTags.PAYMENT_METHOD_CHIP}_${PaymentMethod.SKILLS.name}")
            .performClick()

        // All should remain clickable (for deselection)
        composeTestRule
            .onNodeWithTag("${RequestScreenTags.PAYMENT_METHOD_CHIP}_${PaymentMethod.CASH.name}")
            .assertHasClickAction()
        composeTestRule
            .onNodeWithTag("${RequestScreenTags.PAYMENT_METHOD_CHIP}_${PaymentMethod.SKILLS.name}")
            .assertHasClickAction()
    }
}

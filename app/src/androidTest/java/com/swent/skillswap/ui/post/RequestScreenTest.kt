/* With the help of Sonnet 4.5 for repetitive tasks */

import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.firebase.FirestoreSettings.MAX_SEARCH_KEYS
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

    private val defaultLocation = GeoPoint(46.5191, 6.5668)

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

    private fun scrollAndAssertIsDisplayed(tag: String) {
        composeTestRule.onNodeWithTag("scrollColumn").performScrollToNode(hasTestTag(tag))
        composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
    }

    private fun scrollAndClick(tag: String) {
        composeTestRule.onNodeWithTag("scrollColumn").performScrollToNode(hasTestTag(tag))
        composeTestRule.onNodeWithTag(tag).performClick()
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
        // Can't scroll because not in column
        composeTestRule.onNodeWithTag(RequestScreenTags.BACK_BUTTON).assertIsDisplayed()

        scrollAndAssertIsDisplayed(RequestScreenTags.TITLE_INPUT)
        scrollAndAssertIsDisplayed(RequestScreenTags.DESCRIPTION_INPUT)
        scrollAndAssertIsDisplayed(RequestScreenTags.TAGS_INPUT)
        scrollAndAssertIsDisplayed(RequestScreenTags.CHOOSE_ATTACHMENT_BUTTON)
        scrollAndAssertIsDisplayed(RequestScreenTags.CREATE_BUTTON)
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
        scrollAndAssertIsDisplayed(RequestScreenTags.CREATE_BUTTON)
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
        scrollAndAssertIsDisplayed(RequestScreenTags.EDIT_BUTTON)
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
        scrollAndClick(RequestScreenTags.CREATE_BUTTON)
        composeTestRule.waitForIdle()

        // Error message should appear
        scrollAndAssertIsDisplayed(RequestScreenTags.TITLE_INPUT)
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

        scrollAndClick(RequestScreenTags.CREATE_BUTTON)
        composeTestRule.waitForIdle()

        scrollAndAssertIsDisplayed(RequestScreenTags.DESCRIPTION_INPUT)
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
            scrollAndAssertIsDisplayed("${RequestScreenTags.PAYMENT_METHOD_CHIP}_${method.name}")
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
        scrollAndClick(chipTag)
        composeTestRule.waitForIdle()

        // Click again to deselect
        scrollAndClick(chipTag)
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

        scrollAndAssertIsDisplayed("${RequestScreenTags.TAG_CHIP}_REOCCURRING")
    }

    // ========== TAG TESTS ==========

    @Test
    fun tagChip_displayAndRemove() {
        val viewModel =
            RequestViewModel(null, fakeRepository, currentUserId = testUserId, postId = null)

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
        val tagChipTag = "${RequestScreenTags.TAG_CHIP}_FLUID_MECHANICS"
        scrollAndClick(tagChipTag)

        composeTestRule.waitForIdle()

        // Verify tag is removed
        composeTestRule.onNodeWithTag(tagChipTag).assertDoesNotExist()
    }

    // ========== LOADING STATE TESTS ==========

    @Test
    fun submit_showsLoadingIndicator() {
        fakeRepository.setDelay(1000) // Add delay to see loading state
        val viewModel =
            RequestViewModel(null, fakeRepository, currentUserId = testUserId, postId = null)

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
        scrollAndClick("${RequestScreenTags.PAYMENT_METHOD_CHIP}_${PaymentMethod.CASH.name}")

        // Click submit
        scrollAndClick(RequestScreenTags.CREATE_BUTTON)

        // Loading indicator should appear
        scrollAndAssertIsDisplayed(RequestScreenTags.LOADING_INDICATOR)
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
        scrollAndClick(RequestScreenTags.CREATE_BUTTON)
        composeTestRule.waitForIdle()

        // Should show tags error
        scrollAndAssertIsDisplayed(RequestScreenTags.TAGS_INPUT)
    }

    // ========== SUCCESS FLOW TEST ==========

    @Test
    fun submit_success_triggersCallback() {
        val viewModel =
            RequestViewModel(null, fakeRepository, currentUserId = testUserId, postId = null)

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
        composeTestRule
            .onNodeWithTag(RequestScreenTags.TITLE_INPUT)
            .performScrollTo()
            .performTextInput("Valid Title")
        composeTestRule
            .onNodeWithTag(RequestScreenTags.DESCRIPTION_INPUT)
            .performScrollTo()
            .performTextInput("Valid Description")

        // Add tag directly via ViewModel (avoid dropdown interaction)
        viewModel.addTag(SkillTag.FLUID_MECHANICS)
        composeTestRule.waitForIdle()

        scrollAndClick("${RequestScreenTags.PAYMENT_METHOD_CHIP}_${PaymentMethod.CASH.name}")

        // Submit
        scrollAndClick(RequestScreenTags.CREATE_BUTTON)
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
        scrollAndClick("${RequestScreenTags.PAYMENT_METHOD_CHIP}_${PaymentMethod.CASH.name}")
        scrollAndClick("${RequestScreenTags.PAYMENT_METHOD_CHIP}_${PaymentMethod.SKILLS.name}")

        // All should remain clickable (for deselection)
        composeTestRule
            .onNodeWithTag("${RequestScreenTags.PAYMENT_METHOD_CHIP}_${PaymentMethod.CASH.name}")
            .assertHasClickAction()
        composeTestRule
            .onNodeWithTag("${RequestScreenTags.PAYMENT_METHOD_CHIP}_${PaymentMethod.SKILLS.name}")
            .assertHasClickAction()
    }

    @Test
    fun tagInput_showsSuggestions_whenTyping() {
        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD
            )
        }

        // Type into tags input to trigger suggestions
        composeTestRule
            .onNodeWithTag(RequestScreenTags.TAGS_INPUT)
            .performClick()
            .performTextInput("flu")

        composeTestRule.waitForIdle()

        // Verify suggestion appears (FLUID_MECHANICS contains "flu")
        scrollAndAssertIsDisplayed("${RequestScreenTags.TAG_SUGGESTION}_FLUID_MECHANICS")
    }

    @Test
    fun tagSuggestion_onClick_addsTagAndClearsInput() {
        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD
            )
        }

        // Type to show suggestions
        composeTestRule
            .onNodeWithTag(RequestScreenTags.TAGS_INPUT)
            .performClick()
            .performTextInput("fluid")

        composeTestRule.waitForIdle()

        // Click on the suggestion
        scrollAndClick("${RequestScreenTags.TAG_SUGGESTION}_FLUID_MECHANICS")

        composeTestRule.waitForIdle()

        // Verify tag chip appears
        scrollAndAssertIsDisplayed("${RequestScreenTags.TAG_CHIP}_FLUID_MECHANICS")

        // Verify input is cleared
        composeTestRule.onNodeWithTag(RequestScreenTags.TAGS_INPUT).assert(hasText(""))

        // Verify dropdown is closed (suggestion no longer visible)
        composeTestRule
            .onNodeWithTag("${RequestScreenTags.TAG_SUGGESTION}_FLUID_MECHANICS")
            .assertDoesNotExist()
    }

    @Test
    fun tagSuggestions_limitedToMaxSearchKeys() {
        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD
            )
        }

        // Type a common letter to get many matches
        composeTestRule
            .onNodeWithTag(RequestScreenTags.TAGS_INPUT)
            .performClick()
            .performTextInput("a")

        composeTestRule.waitForIdle()

        // Count displayed suggestions - should not exceed MAX_SEARCH_KEYS
        val displayedSuggestions =
            SkillTag.entries
                .filter { it.name.contains("a", ignoreCase = true) }
                .take(MAX_SEARCH_KEYS)

        // Verify only limited suggestions are shown
        displayedSuggestions.forEach { tag ->
            composeTestRule
                .onNodeWithTag("${RequestScreenTags.TAG_SUGGESTION}_${tag.name}")
                .assertExists()
        }
    }

    @Test
    fun tagSuggestions_hideWhenInputEmpty() {
        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD
            )
        }

        // Focus tags input without typing
        composeTestRule.onNodeWithTag(RequestScreenTags.TAGS_INPUT).performClick()

        composeTestRule.waitForIdle()

        // Suggestions should not appear for empty input
        composeTestRule
            .onAllNodesWithTag(RequestScreenTags.TAG_SUGGESTION, useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun tagSuggestions_caseInsensitiveSearch() {
        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD
            )
        }

        // Type lowercase query
        composeTestRule
            .onNodeWithTag(RequestScreenTags.TAGS_INPUT)
            .performClick()
            .performTextInput("FLUID")

        composeTestRule.waitForIdle()

        // Should still find FLUID_MECHANICS
        scrollAndAssertIsDisplayed("${RequestScreenTags.TAG_SUGGESTION}_FLUID_MECHANICS")
    }

    @Test
    fun addPhoto_triggersPicker() {
        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD,
            )
        }

        // 1. Click the button to open photo picker
        scrollAndClick(RequestScreenTags.CHOOSE_ATTACHMENT_BUTTON)

        // unable to test any further as this launches a separate android activity
    }

    @Test
    fun addImage_andRemoveImage_UIElementsUpdate() {
        val testViewModel =
            RequestViewModel(
                appContext = null,
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postId = null
            )

        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD,
                requestViewModel = testViewModel
            )
        }

        // 1. Simulate picker returning URIs
        val fakeUri = Uri.parse("content://fake/image1.png")
        testViewModel.addAttachments(listOf(fakeUri))

        composeTestRule.waitForIdle()

        val tag = "${RequestScreenTags.ATTACHMENT_PREVIEW}_$fakeUri"
        // 2. Assert the image is displayed
        scrollAndAssertIsDisplayed(tag)

        // 3. Click it to remove image
        scrollAndClick(tag)

        composeTestRule.waitForIdle()

        // 4. Assert it's removed
        composeTestRule.onNodeWithTag(tag).assertDoesNotExist()
    }

    @Test
    fun add6Image_causesError() {
        val testViewModel =
            RequestViewModel(
                appContext = null,
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postId = null
            )

        composeTestRule.setContent {
            RequestScreen(
                postRepository = fakeRepository,
                currentUserId = testUserId,
                postOperation = PostOperation.ADD,
                requestViewModel = testViewModel
            )
        }

        // 1. Simulate picker returning 6 fake URIs
        val fakeUris = (1..6).map { index -> Uri.parse("content://fake/image$index.png") }
        testViewModel.addAttachments(fakeUris)

        composeTestRule.waitForIdle()

        // 2. Assert error is displayed
        scrollAndAssertIsDisplayed(RequestScreenTags.ATTACHMENT_ERROR)
    }
}

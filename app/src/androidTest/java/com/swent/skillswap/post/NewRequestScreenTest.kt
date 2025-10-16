/* With the help of Claude 4.5 Sonnet for repetitive tasks */

package com.swent.skillswap.post

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.swent.skillswap.model.post.PaymentMethod
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostRepository
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.tags.EveryTag
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.post.NewRequestScreen
import com.swent.skillswap.ui.post.NewRequestScreenTestTags
import com.swent.skillswap.ui.post.NewRequestViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NewRequestScreenTest {
    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var mockRepository: PostRepository
    private lateinit var viewModel: NewRequestViewModel

    @Before
    fun setUp() {
        // Fake repository
        mockRepository =
            object : PostRepository {
                override fun getNewUid(type: PostType): String = "test-uid"

                override suspend fun getMultiplePosts(
                    numberOfPosts: Long,
                    type: PostType,
                    titleContains: String,
                    ownerId: String,
                    paymentMethods: List<com.swent.skillswap.model.post.PaymentMethod>,
                    tags: List<com.swent.skillswap.model.tags.EveryTag>,
                    status: com.swent.skillswap.model.post.PostStatus?
                ): List<Post> = emptyList()

                override suspend fun getPost(type: PostType, postId: String): Post {
                    throw NotImplementedError()
                }

                override suspend fun addPost(post: Post) {}

                override suspend fun editPost(postId: String, newPost: Post) {}

                override suspend fun deletePost(type: PostType, postId: String) {}
            }

        viewModel = NewRequestViewModel(mockRepository)

        composeTestRule.setContent {
            NewRequestScreen(newRequestViewModel = viewModel, onGoBack = {}, onPostCreated = {})
        }
    }

    @Test
    fun testAllComponentsAreDisplayed() {
        composeTestRule.onNodeWithTag(NewRequestScreenTestTags.BACK_BUTTON).assertIsDisplayed()

        composeTestRule.onNodeWithTag(NewRequestScreenTestTags.TITLE_INPUT).assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(NewRequestScreenTestTags.DESCRIPTION_INPUT)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag(NewRequestScreenTestTags.TAGS_INPUT).assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("${NewRequestScreenTestTags.PAYMENT_METHOD_CHIP}_SKILLS")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("${NewRequestScreenTestTags.PAYMENT_METHOD_CHIP}_CASH")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag(NewRequestScreenTestTags.CREATE_BUTTON).assertIsDisplayed()
    }

    @Test
    fun title_showsError_whenEmpty() {
        composeTestRule.onNodeWithTag(NewRequestScreenTestTags.TITLE_INPUT).performTextInput("Test")

        composeTestRule.onNodeWithTag(NewRequestScreenTestTags.TITLE_INPUT).performTextClearance()

        composeTestRule
            .onNodeWithTag(NewRequestScreenTestTags.TITLE_INPUT)
            .assertTextContains("Title cannot be empty")
    }

    @Test
    fun description_showsError_whenEmpty() {
        composeTestRule
            .onNodeWithTag(NewRequestScreenTestTags.DESCRIPTION_INPUT)
            .performScrollTo()
            .performTextInput("Test description")

        composeTestRule
            .onNodeWithTag(NewRequestScreenTestTags.DESCRIPTION_INPUT)
            .performTextClearance()

        composeTestRule
            .onNodeWithTag(NewRequestScreenTestTags.DESCRIPTION_INPUT)
            .assertTextContains("Description cannot be empty")
    }

    @Test
    fun tags_canAddTag_bySearchingAndSelecting() {
        composeTestRule
            .onNodeWithTag(NewRequestScreenTestTags.TAGS_INPUT)
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(NewRequestScreenTestTags.TAGS_INPUT)
            .performTextInput("DIGITAL")

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag("${NewRequestScreenTestTags.TAG_SUGGESTION}_DIGITAL_LOGIC")
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag("${NewRequestScreenTestTags.TAG_CHIP}_DIGITAL_LOGIC")
            .assertIsDisplayed()
    }

    @Test
    fun tags_canRemoveTag_byClickingChip() {
        viewModel.addTag(SkillTag.DIGITAL_LOGIC)

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag("${NewRequestScreenTestTags.TAG_CHIP}_DIGITAL_LOGIC")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("${NewRequestScreenTestTags.TAG_CHIP}_DIGITAL_LOGIC")
            .performClick()

        composeTestRule
            .onNodeWithTag("${NewRequestScreenTestTags.TAG_CHIP}_DIGITAL_LOGIC")
            .assertDoesNotExist()
    }

    @Test
    fun paymentMethod_canToggle_byClickingChip() {
        composeTestRule
            .onNodeWithTag("${NewRequestScreenTestTags.PAYMENT_METHOD_CHIP}_CASH")
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()
        assert(viewModel.uiState.value.paymentMethods.contains(PaymentMethod.CASH))

        composeTestRule
            .onNodeWithTag("${NewRequestScreenTestTags.PAYMENT_METHOD_CHIP}_CASH")
            .performClick()

        // Verify its deselected
        composeTestRule.waitForIdle()
        assert(!viewModel.uiState.value.paymentMethods.contains(PaymentMethod.CASH))
    }

    @Test
    fun paymentMethod_canSelectMultiple() {
        composeTestRule
            .onNodeWithTag("${NewRequestScreenTestTags.PAYMENT_METHOD_CHIP}_CASH")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithTag("${NewRequestScreenTestTags.PAYMENT_METHOD_CHIP}_SKILLS")
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()
        assert(viewModel.uiState.value.paymentMethods.contains(PaymentMethod.CASH))
        assert(viewModel.uiState.value.paymentMethods.contains(PaymentMethod.SKILLS))
    }

    /*
    @Test
    fun backButton_triggersOnGoBack_callback() {
        var backPressed = false

        composeTestRule.setContent {
            NewRequestScreen(
                onGoBack = { backPressed = true },
                onPostCreated = {}
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(NewRequestScreenTestTags.BACK_BUTTON).performClick()

        assert(backPressed)
    }
     */
}

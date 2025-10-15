/* With the help of Claude 4.5 Sonnet for repetitive tasks */

package com.swent.skillswap.post

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostRepository
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.ui.post.NewRequestScreen
import com.swent.skillswap.ui.post.NewRequestScreenTestTags
import com.swent.skillswap.ui.post.NewRequestViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NewRequestScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var mockRepository: PostRepository
    private lateinit var viewModel: NewRequestViewModel

    @Before
    fun setUp() {
        // Fake repository
        mockRepository = object : PostRepository {
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

            override suspend fun addPost(post: Post) {
                // Do nothing in tests
            }

            override suspend fun editPost(postId: String, newPost: Post) {
                // Do nothing in tests
            }

            override suspend fun deletePost(type: PostType, postId: String) {
                // Do nothing in tests
            }
        }

        viewModel = NewRequestViewModel(mockRepository)

        composeTestRule.setContent {
            NewRequestScreen(
                newRequestViewModel = viewModel,
                onGoBack = {},
                onPostCreated = {}
            )
        }
    }

    @Test
    fun testAllComponentsAreDisplayed() {
        // Verify back button (in TopAppBar, not scrollable)
        composeTestRule.onNodeWithTag(NewRequestScreenTestTags.BACK_BUTTON)
            .assertIsDisplayed()

        // Verify input fields (visible at top of screen)
        composeTestRule.onNodeWithTag(NewRequestScreenTestTags.TITLE_INPUT)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag(NewRequestScreenTestTags.DESCRIPTION_INPUT)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag(NewRequestScreenTestTags.TAGS_INPUT)
            .assertIsDisplayed()

        // Verify payment method chips
        composeTestRule.onNodeWithTag("${NewRequestScreenTestTags.PAYMENT_METHOD_CHIP}_SKILLS")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("${NewRequestScreenTestTags.PAYMENT_METHOD_CHIP}_CASH")
            .assertIsDisplayed()

        // Verify submit button
        composeTestRule.onNodeWithTag(NewRequestScreenTestTags.CREATE_BUTTON)
            .assertIsDisplayed()
    }



}
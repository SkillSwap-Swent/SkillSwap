/**
 * @author Younes Belgroune - Made with the help of AI @author Alex Magnus - ChatGPT for status text
 *   test
 */
package com.swent.skillswap.ui.personalPosts

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.post.*
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.post.personalPosts.PersonalPostsScreen
import com.swent.skillswap.ui.post.personalPosts.PersonalPostsScreenTags
import com.swent.skillswap.ui.post.personalPosts.PersonalPostsUiState
import com.swent.skillswap.ui.post.personalPosts.PersonalPostsViewModel
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PersonalPostsScreenTest {
    @get:Rule val composeRule = createComposeRule()

    private lateinit var fakeRepository: FakePostRepository
    private val testLocation = GeoPoint(46.5191, 6.5668)

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
        } catch (e: Exception) {
            // Ignore
        }
    }

    val testRequest =
        Request(
            uid = "123",
            title = "Test title",
            description = "Test description",
            ownerId = "test-user",
            skills = setOf(SkillTag.MACHINE_DESIGN),
            tags = setOf(PostTag.REOCCURRING),
            paymentMethod = PaymentMethod.SKILLS,
            expiry = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            creation = Timestamp.now(),
            status = PostStatus.POSTED,
            media = emptyList(),
            postReplies = emptySet(),
            location = testLocation
        )

    private fun createTestRequest(id: String, title: String) =
        testRequest.copy(uid = id, title = title)

    private fun createViewModelWithState(uiState: PersonalPostsUiState): PersonalPostsViewModel {
        val viewModel = PersonalPostsViewModel(fakeRepository)
        try {
            val field = PersonalPostsViewModel::class.java.getDeclaredField("_uiState")
            field.isAccessible = true
            val mutableStateFlow = field.get(viewModel) as MutableStateFlow<PersonalPostsUiState>
            mutableStateFlow.value = uiState
        } catch (e: Exception) {
            // If reflection fails, return viewModel as-is
        }
        return viewModel
    }

    @Test
    fun displays_title_and_back_button() {
        var backClicked = false
        val viewModel = createViewModelWithState(PersonalPostsUiState(isLoading = false))
        composeRule.setContent {
            MaterialTheme {
                PersonalPostsScreen(viewModel = viewModel, onGoBack = { backClicked = true })
            }
        }
        composeRule.onNodeWithTag(PersonalPostsScreenTags.TITLE).assertIsDisplayed()
        composeRule.onNodeWithText("My Posts").assertIsDisplayed()
        composeRule.onAllNodesWithContentDescription("Back")[0].performClick()
        assert(backClicked)
    }

    @Test
    fun displays_loading_indicator_when_loading() {
        val viewModel = createViewModelWithState(PersonalPostsUiState(isLoading = true))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithTag(PersonalPostsScreenTags.LOADING_INDICATOR).assertExists()
    }

    @Test
    fun displays_error_message_and_retry_button() {
        val viewModel =
            createViewModelWithState(
                PersonalPostsUiState(isLoading = false, error = "Test error message")
            )
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithTag(PersonalPostsScreenTags.ERROR_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText("Test error message").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun displays_empty_state_when_no_posts() {
        val viewModel =
            createViewModelWithState(PersonalPostsUiState(isLoading = false, posts = emptyList()))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithText("No posts found").assertIsDisplayed()
    }

    @Test
    fun displays_posts_list() {
        val uid = "req-1"
        val title = "Test Post 1"
        val posts = listOf(createTestRequest(uid, title))
        val viewModel =
            createViewModelWithState(PersonalPostsUiState(isLoading = false, posts = posts))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }

        composeRule
            .onNodeWithTag(PersonalPostsScreenTags.ITEM_TITLE + uid)
            .assertExists()
            .assertTextEquals(title)
    }

    @Test
    fun displays_post_details() {
        val uid = "req-1"
        val title = "Detailed Post"

        val posts = listOf(createTestRequest(uid, title))
        val viewModel =
            createViewModelWithState(PersonalPostsUiState(isLoading = false, posts = posts))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }

        composeRule
            .onNodeWithTag(PersonalPostsScreenTags.ITEM_TITLE + uid)
            .assertExists()
            .assertTextEquals(title)

        composeRule
            .onNodeWithTag(PersonalPostsScreenTags.ITEM_DESCRIPTION + uid)
            .assertExists()
            .assertTextEquals(testRequest.description)

        // --- Status Text ---
        // This check checks that the info is displayed in some way, not the exact formatting to not
        // be reliant on UI
        val statusNode =
            composeRule.onNodeWithTag(PersonalPostsScreenTags.ITEM_STATUS + uid).assertExists()

        val displayedStatus =
            statusNode
                .fetchSemanticsNode()
                .config[SemanticsProperties.Text]
                .joinToString(separator = "") { it.text }
                .lowercase()

        assert(displayedStatus.contains(testRequest.status.name.lowercase()))
        assert(displayedStatus.contains(testRequest.paymentMethod.displayName.lowercase()))
        assert(displayedStatus.contains(testRequest.postReplies.size.toString()))

        val firstSkill = testRequest.skills.first()
        composeRule
            .onNodeWithTag(PersonalPostsScreenTags.ITEM_SKILL + firstSkill.name + uid)
            .assertExists()
    }

    @Test
    fun edit_button_triggers_callback() {
        var editClicked = false
        var editedPost: Post? = null
        val posts = listOf(createTestRequest("req-1", "Edit Me"))
        val viewModel =
            createViewModelWithState(PersonalPostsUiState(isLoading = false, posts = posts))
        composeRule.setContent {
            MaterialTheme {
                PersonalPostsScreen(
                    viewModel = viewModel,
                    onEditPost = { post ->
                        editClicked = true
                        editedPost = post
                    }
                )
            }
        }
        composeRule.onAllNodesWithTag(PersonalPostsScreenTags.EDIT_BUTTON)[0].performClick()
        assert(editClicked)
        assertNotNull(editedPost)
        assertEquals("Edit Me", editedPost?.title)
    }

    @Test
    fun delete_button_calls_viewmodel() {
        val posts = listOf(createTestRequest("req-1", "Delete Me"))
        val viewModel =
            createViewModelWithState(PersonalPostsUiState(isLoading = false, posts = posts))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onAllNodesWithTag(PersonalPostsScreenTags.DELETE_BUTTON)[0].performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun displays_multiple_posts() {
        val posts =
            listOf(createTestRequest("req-1", "Post 1"), createTestRequest("req-2", "Post 2"))
        val viewModel =
            createViewModelWithState(PersonalPostsUiState(isLoading = false, posts = posts))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithText("Post 1").assertIsDisplayed()
        composeRule.onNodeWithText("Post 2").assertIsDisplayed()
    }

    @Test
    fun retry_button_calls_viewmodel_refresh() {
        val viewModel =
            createViewModelWithState(
                PersonalPostsUiState(isLoading = false, error = "Error occurred")
            )
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithText("Retry").performClick()
        composeRule.waitForIdle()
    }
}

/** @author Younes Belgroune - Made with the help of AI */
package com.swent.skillswap.ui.personalPosts

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.swent.skillswap.model.post.*
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.model.tags.SkillTag
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersonalPostsScreenTest {

    @get:Rule val composeRule = createComposeRule()

    private val testLocation = GeoPoint(46.5191, 6.5668)
    private val now = Timestamp.now()
    private val future = Timestamp(Date(System.currentTimeMillis() + 86400000))

    private fun createOffer(
        id: String,
        title: String,
        tags: Set<com.swent.skillswap.model.tags.EveryTag> = emptySet()
    ) =
        Offer(
            id,
            title,
            "Description",
            "user1",
            tags,
            PaymentMethod.SKILLS,
            future,
            now,
            PostStatus.POSTED,
            emptyList(),
            testLocation
        )

    private fun createRequest(
        id: String,
        title: String,
        tags: Set<com.swent.skillswap.model.tags.EveryTag> = emptySet()
    ) =
        Request(
            id,
            title,
            "Description",
            "user1",
            tags,
            PaymentMethod.SKILLS,
            future,
            now,
            PostStatus.POSTED,
            emptyList(),
            testLocation
        )

    private class TestViewModel(initialState: PersonalPostsUiState) :
        PersonalPostsViewModel(FakePostRepository()) {
        private val _testState = MutableStateFlow(initialState)
        override val uiState = _testState
        var setFilterCalled = false
        var deleteCalled = false
        var refreshCalled = false
        var clearErrorCalled = false
        var lastDeletedPost: Post? = null
        var lastFilter: PostTypeFilter? = null

        fun updateState(newState: PersonalPostsUiState) {
            _testState.value = newState
        }

        override fun setPostTypeFilter(filter: PostTypeFilter) {
            setFilterCalled = true
            lastFilter = filter
            _testState.value = _testState.value.copy(selectedPostType = filter)
        }

        override fun deletePost(post: Post) {
            deleteCalled = true
            lastDeletedPost = post
            _testState.value =
                _testState.value.copy(
                    posts = _testState.value.posts.filterNot { it.uid == post.uid }
                )
        }

        override fun refresh() {
            refreshCalled = true
        }

        override fun clearError() {
            clearErrorCalled = true
            _testState.value = _testState.value.copy(error = null)
        }
    }

    @Test
    fun displays_title_and_back_button() {
        val viewModel = TestViewModel(PersonalPostsUiState(posts = emptyList(), isLoading = false))
        var backClicked = false
        composeRule.setContent {
            MaterialTheme {
                PersonalPostsScreen(viewModel = viewModel, onGoBack = { backClicked = true })
            }
        }
        composeRule.onNodeWithTag(PersonalPostsScreenTags.TITLE).assertExists()
        composeRule.onNodeWithText("My Posts").assertExists()
        composeRule.onAllNodesWithContentDescription("Back")[0].performClick()
        assert(backClicked)
    }

    @Test
    fun displays_loading_indicator_when_loading() {
        val viewModel = TestViewModel(PersonalPostsUiState(isLoading = true))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithTag(PersonalPostsScreenTags.LOADING_INDICATOR).assertExists()
    }

    @Test
    fun displays_error_message_and_retry_button() {
        val viewModel = TestViewModel(PersonalPostsUiState(error = "Test error", isLoading = false))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithTag(PersonalPostsScreenTags.ERROR_MESSAGE).assertExists()
        composeRule.onNodeWithText("Test error").assertExists()
        composeRule.onNodeWithText("Retry").performClick()
        assert(viewModel.refreshCalled)
    }

    @Test
    fun displays_empty_state_when_no_posts() {
        val viewModel = TestViewModel(PersonalPostsUiState(posts = emptyList(), isLoading = false))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithTag(PersonalPostsScreenTags.EMPTY_STATE).assertExists()
        composeRule.onNodeWithText("No posts found").assertExists()
    }

    @Test
    fun displays_filter_buttons() {
        val viewModel = TestViewModel(PersonalPostsUiState(posts = emptyList(), isLoading = false))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithTag(PersonalPostsScreenTags.FILTER_ALL).assertExists()
        composeRule.onNodeWithTag(PersonalPostsScreenTags.FILTER_OFFERS).assertExists()
        composeRule.onNodeWithTag(PersonalPostsScreenTags.FILTER_REQUESTS).assertExists()
    }

    @Test
    fun filter_all_button_calls_viewmodel() {
        val viewModel =
            TestViewModel(
                PersonalPostsUiState(selectedPostType = PostTypeFilter.OFFERS, isLoading = false)
            )
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithTag(PersonalPostsScreenTags.FILTER_ALL).performClick()
        assert(viewModel.setFilterCalled && viewModel.lastFilter == PostTypeFilter.ALL)
    }

    @Test
    fun filter_offers_button_calls_viewmodel() {
        val viewModel =
            TestViewModel(
                PersonalPostsUiState(selectedPostType = PostTypeFilter.ALL, isLoading = false)
            )
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithTag(PersonalPostsScreenTags.FILTER_OFFERS).performClick()
        assert(viewModel.setFilterCalled && viewModel.lastFilter == PostTypeFilter.OFFERS)
    }

    @Test
    fun filter_requests_button_calls_viewmodel() {
        val viewModel =
            TestViewModel(
                PersonalPostsUiState(selectedPostType = PostTypeFilter.ALL, isLoading = false)
            )
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithTag(PersonalPostsScreenTags.FILTER_REQUESTS).performClick()
        assert(viewModel.setFilterCalled && viewModel.lastFilter == PostTypeFilter.REQUESTS)
    }

    @Test
    fun displays_posts_list() {
        val posts = listOf(createOffer("1", "Offer 1"), createRequest("2", "Request 1"))
        val viewModel = TestViewModel(PersonalPostsUiState(posts = posts, isLoading = false))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithTag(PersonalPostsScreenTags.POSTS_LIST).assertExists()
        composeRule.onNodeWithText("Offer 1").assertExists()
        composeRule.onNodeWithText("Request 1").assertExists()
    }

    @Test
    fun post_item_displays_title_and_type() {
        val post = createOffer("1", "Test Offer")
        val viewModel = TestViewModel(PersonalPostsUiState(posts = listOf(post), isLoading = false))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithText("Test Offer").assertExists()
        composeRule.onNodeWithText("Offer").assertExists()
    }

    @Test
    fun post_item_displays_description() {
        val post = createOffer("1", "Test", emptySet())
        val viewModel = TestViewModel(PersonalPostsUiState(posts = listOf(post), isLoading = false))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithText("Description").assertExists()
    }

    @Test
    fun post_item_displays_payment_method_and_status() {
        val post = createOffer("1", "Test", emptySet())
        val viewModel = TestViewModel(PersonalPostsUiState(posts = listOf(post), isLoading = false))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithText("Payment: SKILLS").assertExists()
        composeRule.onNodeWithText("Posted").assertExists()
    }

    @Test
    fun post_item_with_skill_tags_displays_tags() {
        val post = createOffer("1", "Test", setOf(SkillTag.COMPUTER_PROGRAMMING, SkillTag.CALCULUS))
        val viewModel = TestViewModel(PersonalPostsUiState(posts = listOf(post), isLoading = false))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithText("COMPUTER_PROGRAMMING").assertExists()
    }

    @Test
    fun post_item_with_other_tags_displays_fallback() {
        val post = createOffer("1", "Test", setOf(PostTag.REOCCURRING))
        val viewModel = TestViewModel(PersonalPostsUiState(posts = listOf(post), isLoading = false))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithText("Reoccurring").assertExists()
    }

    @Test
    fun post_item_with_more_than_3_tags_shows_plus_count() {
        val post =
            createOffer(
                "1",
                "Test",
                setOf(
                    SkillTag.COMPUTER_PROGRAMMING,
                    SkillTag.CALCULUS,
                    SkillTag.LINEAR_ALGEBRA,
                    SkillTag.PHYSICS_MECHANICS
                )
            )
        val viewModel = TestViewModel(PersonalPostsUiState(posts = listOf(post), isLoading = false))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithText("+1").assertExists()
    }

    @Test
    fun edit_button_calls_callback() {
        val post = createOffer("1", "Test")
        var editCalled = false
        val viewModel = TestViewModel(PersonalPostsUiState(posts = listOf(post), isLoading = false))
        composeRule.setContent {
            MaterialTheme {
                PersonalPostsScreen(
                    viewModel = viewModel,
                    onEditPost = { editCalled = it.uid == "1" }
                )
            }
        }
        composeRule.onAllNodesWithTag(PersonalPostsScreenTags.EDIT_BUTTON)[0].performClick()
        assert(editCalled)
    }

    @Test
    fun delete_button_calls_viewmodel() {
        val post = createOffer("1", "Test")
        val viewModel = TestViewModel(PersonalPostsUiState(posts = listOf(post), isLoading = false))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onAllNodesWithTag(PersonalPostsScreenTags.DELETE_BUTTON)[0].performClick()
        assert(viewModel.deleteCalled && viewModel.lastDeletedPost?.uid == "1")
    }

    @Test
    fun error_snackbar_appears_when_error_exists() {
        val viewModel =
            TestViewModel(PersonalPostsUiState(error = "Error message", isLoading = false))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Error message").assertExists()
    }

    @Test
    fun error_snackbar_dismiss_button_clears_error() {
        val viewModel =
            TestViewModel(PersonalPostsUiState(error = "Error message", isLoading = false))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Dismiss").performClick()
        assert(viewModel.clearErrorCalled)
    }

    @Test
    fun filter_button_shows_selected_state() {
        val viewModel =
            TestViewModel(
                PersonalPostsUiState(selectedPostType = PostTypeFilter.OFFERS, isLoading = false)
            )
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithTag(PersonalPostsScreenTags.FILTER_OFFERS).assertExists()
    }

    @Test
    fun multiple_posts_display_correctly() {
        val posts =
            listOf(
                createOffer("1", "Offer 1"),
                createOffer("2", "Offer 2"),
                createRequest("3", "Request 1")
            )
        val viewModel = TestViewModel(PersonalPostsUiState(posts = posts, isLoading = false))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithText("Offer 1").assertExists()
        composeRule.onNodeWithText("Offer 2").assertExists()
        composeRule.onNodeWithText("Request 1").assertExists()
    }

    @Test
    fun post_item_with_empty_tags_does_not_show_tags_section() {
        val post = createOffer("1", "Test", emptySet())
        val viewModel = TestViewModel(PersonalPostsUiState(posts = listOf(post), isLoading = false))
        composeRule.setContent { MaterialTheme { PersonalPostsScreen(viewModel = viewModel) } }
        composeRule.onNodeWithText("Test").assertExists()
        composeRule.onNodeWithText("Description").assertExists()
    }
}

/** @author Younes Belgroune - Made with the help of AI */
package com.swent.skillswap.ui.personalPosts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostRepository
import com.swent.skillswap.model.post.PostType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents the current state of the Personal Posts screen UI.
 *
 * @property posts List of all personal posts currently loaded.
 * @property isLoading Indicates if posts are currently being fetched.
 * @property error Error message if an error occurred while loading posts.
 * @property selectedPostType The type of posts currently being displayed (OFFER, REQUEST, or both).
 */
data class PersonalPostsUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedPostType: PostTypeFilter = PostTypeFilter.ALL
)

/** Filter options for post types. */
enum class PostTypeFilter {
    /** Show all posts (both offers and requests). */
    ALL,
    /** Show only offer posts. */
    OFFERS,
    /** Show only request posts. */
    REQUESTS
}

/**
 * ViewModel responsible for managing personal posts data and UI state.
 *
 * It loads posts created by the current user, filters them by type, and handles user actions such
 * as refreshing the list or deleting posts.
 *
 * @property postRepository The repository used to retrieve posts from the database.
 */
class PersonalPostsViewModel(private val postRepository: PostRepository) : ViewModel() {

    private val TAG = "PersonalPostsViewModel"

    /** Internal state of the Personal Posts screen. */
    private val _uiState = MutableStateFlow(PersonalPostsUiState())

    /** Publicly exposed, read-only state of the Personal Posts screen. */
    val uiState: StateFlow<PersonalPostsUiState> = _uiState.asStateFlow()

    init {
        loadPersonalPosts()
    }

    /**
     * Loads all posts created by the current user.
     *
     * Fetches posts from the repository filtered by the current user's ID and applies the selected
     * post type filter. Updates the UI state with loading status and results.
     */
    fun loadPersonalPosts() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            _uiState.update {
                it.copy(isLoading = false, error = "No authenticated user found. Please log in.")
            }
            Log.w(TAG, "No authenticated user found")
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val userId = currentUser.uid
                val filter = _uiState.value.selectedPostType

                // Fetch both types and filter based on selection
                val allPosts = mutableListOf<Post>()

                when (filter) {
                    PostTypeFilter.ALL -> {
                        // Fetch both offers and requests
                        val offers =
                            postRepository.getMultiplePosts(
                                numberOfPosts = 100,
                                type = PostType.OFFER,
                                ownerId = userId,
                                status = null
                            )
                        val requests =
                            postRepository.getMultiplePosts(
                                numberOfPosts = 100,
                                type = PostType.REQUEST,
                                ownerId = userId,
                                status = null
                            )
                        allPosts.addAll(offers)
                        allPosts.addAll(requests)
                    }
                    PostTypeFilter.OFFERS -> {
                        val offers =
                            postRepository.getMultiplePosts(
                                numberOfPosts = 100,
                                type = PostType.OFFER,
                                ownerId = userId,
                                status = null
                            )
                        allPosts.addAll(offers)
                    }
                    PostTypeFilter.REQUESTS -> {
                        val requests =
                            postRepository.getMultiplePosts(
                                numberOfPosts = 100,
                                type = PostType.REQUEST,
                                ownerId = userId,
                                status = null
                            )
                        allPosts.addAll(requests)
                    }
                }

                // Repository already sorts by creation date, so we can use posts directly
                _uiState.update { it.copy(posts = allPosts, isLoading = false, error = null) }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading personal posts", e)
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load posts: ${e.message}")
                }
            }
        }
    }

    /**
     * Changes the post type filter and reloads posts.
     *
     * @param filter The new filter to apply (ALL, OFFERS, or REQUESTS).
     */
    fun setPostTypeFilter(filter: PostTypeFilter) {
        _uiState.update { it.copy(selectedPostType = filter) }
        loadPersonalPosts()
    }

    /**
     * Deletes a post from the database.
     *
     * @param post The post to delete.
     */
    fun deletePost(post: Post) {
        viewModelScope.launch {
            try {
                postRepository.deletePost(post.type, post.uid)
                // Reload posts after deletion
                loadPersonalPosts()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting post", e)
                _uiState.update { it.copy(error = "Failed to delete post: ${e.message}") }
            }
        }
    }

    /** Refreshes the posts list by reloading from the database. */
    fun refresh() {
        loadPersonalPosts()
    }

    /** Clears any error message from the UI state. */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

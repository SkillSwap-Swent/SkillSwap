package com.swent.skillswap.ui.feed

import android.util.Log
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.swent.skillswap.model.feed.FeedController
import com.swent.skillswap.model.feed.FeedOffer
import com.swent.skillswap.model.post.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing offer data, navigation, and UI state for the FeedOffer screen.
 *
 * It loads, updates, and transitions between offers, and handles user actions such as swiping or
 * navigating to another user’s profile.
 *
 * @property navigation The navigation handler for transitioning to other screens.
 * @property controller The data source used to retrieve and update offers.
 * @property db The firestore dataBase instance used to instanciate userRepoFirestore
 * @author Joey Gugler using chatGPT
 */
open class FeedScreenViewModel(
    private val navigation: FeedScreenNavigation,
    private val controller: FeedController,
) : ViewModel() {

    /**
     * The unique identifier of the current user (temporary fallback when Firebase is unavailable).
     */
    private val uid: String = Firebase.auth.uid ?: "AnoUser"
    /** Internal state of the FeedOffer screen. */
    private val _uiState = MutableStateFlow<FeedOffer?>(null)

    /** Publicly exposed, read-only state of the FeedOffer screen. */
    open val uiState: StateFlow<FeedOffer?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                    controller.currentPost.value?.let { post ->
                        _uiState.value = toFeedOffer(post, uid)
                    }
                }
                .onFailure { e -> Log.e("FeedScreenViewModel", "Error loading initial post", e) }

            snapshotFlow { controller.currentPost.value }
                .filterNotNull()
                .collect { post ->
                    runCatching { _uiState.value = toFeedOffer(post, uid) }
                        .onFailure { e -> Log.e("FeedScreenViewModel", "Error updating post", e) }
                }
        }
    }

    /** Accepts the specified offer on behalf of the current user. */
    fun accept(offer: FeedOffer) {
        viewModelScope.launch { controller.acceptPost("I'm interested in this offer") }
    } // TODO: A Pop-up Window with a textField or preFab message to send is a good idea

    /** Declines the specified offer, removes it from the feed, and loads the next one. */
    fun decline(offer: FeedOffer) {
        skip()
        // TODO: Placeholder for rollback implementation
    }

    /** Navigates to the profile screen of the specified user. */
    fun goToProfile(userId: String) {
        navigation.goToProfileView(userId)
    }

    /** Skips the current offer and moves to the next one. */
    fun skip() {
        viewModelScope.launch { controller.skipPost() }
    }
    /** Temporarily blocks a user by adding their ID to an in-memory list. */
    fun blockUser(userId: String) {
        viewModelScope.launch { controller.blockUser(userId) }
    }

    /** Temporarily reports an offer by adding it to an in-memory list. */
    fun reportOffer(offer: FeedOffer) {
        // TODO: Replace with backend reporting logic
    }
    /* Sets the maxDistance value for the location filtering */
    fun updateDistanceFilter(distance: Float) {
        viewModelScope.launch { controller.updateDistanceFilter(distance) }
    }

    fun toggleLiveLocation(isLiveLocationOn: Boolean) {
        viewModelScope.launch { controller.updateLocation(isLiveLocationOn) }
    }

    /**
     * Converts a [Post] object into a [FeedOffer] for display on the feed.
     *
     * @param post The post to convert.
     * @param userId The ID of the current user.
     * @return A corresponding [FeedOffer] object.
     */
    private fun toFeedOffer(post: Post, userId: String): FeedOffer {
        return FeedOffer(
            skillProvided = post.title,
            authorID = post.ownerId,
            authorName = "AnoUser",
            // TODO Need to modify the post structure to handle this or use external object
            //  (decrease duplicate data)
            requesterAvatar = "https://picsum.photos/200",
            receiverName = userId,
            skillRequested =
                "TODO : Implement", // TODO this should be provided by the recommendation algo
            thumbnail = post.media.firstOrNull() ?: "",
            specification = post.description,
            description = post.description
        )
    }
}
/**
 * Factory for creating [FeedScreenViewModel] with parameters.
 *
 * @param navigation The navigation handler for the feed screen.
 * @param controller The controller handling feed data.
 */
class FeedScreenViewModelFactory(
    private val navigation: FeedScreenNavigation,
    private val controller: FeedController,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeedScreenViewModel::class.java)) {
            return FeedScreenViewModel(navigation, controller) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

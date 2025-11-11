package com.swent.skillswap.ui.feedScreen

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.swent.skillswap.model.offer.FeedController
import com.swent.skillswap.model.offer.FeedOffer
import com.swent.skillswap.model.post.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.internal.wait

/**
 * ViewModel responsible for managing offer data, navigation, and UI state for the FeedOffer screen.
 *
 * It loads, updates, and transitions between offers, and handles user actions such as swiping or
 * navigating to another user’s profile.
 *
 * @property navigation The navigation handler for transitioning to other screens.
 * @property controller The data source used to retrieve and update offers.
 * @author Joey Gugler using chatGPT
 */
open class FeedScreenViewModel(
    private val navigation: FeedScreenNavigation,
    private val controller: FeedController
) : ViewModel() {

    private val feedScreenLogTag = "FeedScreen"

    /**
     * The unique identifier of the current user (temporary fallback when Firebase is unavailable).
     */
    private val uid: String = Firebase.auth.uid ?: "anoUser"

    /** Internal state of the FeedOffer screen. */
    private val _uiState = MutableStateFlow<FeedOffer?>(null)

    /** Publicly exposed, read-only state of the FeedOffer screen. */
    open val uiState: StateFlow<FeedOffer?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            controller.currentPost.value?.let { post ->
                _uiState.value = toFeedOffer(post, uid)
            }

            // Observe future post changes
            snapshotFlow { controller.currentPost.value }
                .filterNotNull()
                .collect { post ->
                    _uiState.value = toFeedOffer(post, uid)
                }
        }
    }

    /** Accepts the specified offer on behalf of the current user. */
    fun accept(offer: FeedOffer) {
        viewModelScope.launch {
            controller.acceptPost("")
        }
    }

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
        viewModelScope.launch {
            controller.skipPost()
        }
    }
    /** Temporarily blocks a user by adding their ID to an in-memory list. */
    fun blockUser(userId: String) {
        // TODO: Replace with persistent backend logic
    }

    /** Temporarily reports an offer by adding it to an in-memory list. */
    fun reportOffer(offer: FeedOffer) {
        // TODO: Replace with backend reporting logic
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
            authorName = "",
            requesterAvatar = "",
            receiverName = "",
            skillRequested = "",
            thumbnail = post.media.firstOrNull() ?: "",
            specification = post.description,
            description = post.description
        )
    }
}


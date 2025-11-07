package com.swent.skillswap.ui.feedScreen

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import com.swent.skillswap.model.offer.FakeFeedNavigation
import com.swent.skillswap.model.offer.FakeFeedRepository
import com.swent.skillswap.model.offer.FeedOffer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents the current state of the FeedOffer screen UI.
 *
 * @property offers List of all available offers currently loaded.
 * @property current The offer currently being displayed.
 */
data class FeedScreenUiState(
    val offers: List<FeedOffer> = emptyList(),
    val current: FeedOffer = FeedOffer(),
    val isMenuVisible: Boolean = false
)

/**
 * ViewModel responsible for managing offer data, navigation, and UI state for the FeedOffer screen.
 *
 * It loads, updates, and transitions between offers, and handles user actions such as swiping or
 * navigating to another user’s profile.
 *
 * @property navigation The navigation handler for transitioning to other screens.
 * @property repository The data source used to retrieve and update offers.
 */
open class FeedScreenViewModel(
    private val navigation: FeedScreenNavigation = FakeFeedNavigation(),
    private val repository: FeedRepository = FakeFeedRepository()
) : ViewModel() {
    var feedScreenLogTag = "FeedScreen"

    /**
     * The unique identifier of the current user (temporary fallback when Firebase is unavailable).
     */
    private val uid: String = "AnoUser"

    /** Internal state of the FeedOffer screen. */
    private val _uiState = MutableStateFlow(FeedScreenUiState())

    /** Publicly exposed, read-only state of the FeedOffer screen. */
    open val uiState: StateFlow<FeedScreenUiState> = _uiState.asStateFlow()

    init {
        runCatching { next() }.onFailure { Log.e("FeedScreen", "Failed to load initial offer", it) }
    }

    /** Advances to the next offer in the list or fetches a new one. */
    fun next() {
        val state = _uiState.value
        val offers = state.offers

        if (offers.isEmpty()) {
            runCatching {
                    val offer = repository.getPost(uid)
                    _uiState.value = FeedScreenUiState(offers = listOf(offer), current = offer)
                }
                .onFailure { Log.e(feedScreenLogTag, "Failed to fetch first offer", it) }
            return
        }

        val currentIndex = offers.indexOf(state.current)
        if (currentIndex == -1) {
            Log.w(feedScreenLogTag, "Current offer not found in list; resetting to first.")
            _uiState.value = state.copy(current = offers.first())
            return
        }

        if (currentIndex < offers.lastIndex) {
            _uiState.value = state.copy(current = offers[currentIndex + 1])
        } else {
            runCatching {
                    val newOffer = repository.getPost(uid)
                    _uiState.value = state.copy(offers = offers + newOffer, current = newOffer)
                }
                .onFailure { Log.e(feedScreenLogTag, "Failed to fetch new offer", it) }
        }
    }

    /** Accepts the specified offer on behalf of the current user. */
    fun accept(offer: FeedOffer) {
        repository.accept(offer, uid)

        val state = _uiState.value
        val remainingOffers = state.offers.filterNot { it == offer }

        if (remainingOffers.isNotEmpty()) {
            _uiState.value = state.copy(offers = remainingOffers, current = remainingOffers.last())
        } else {
            _uiState.value = state.copy(offers = emptyList(), current = FeedOffer())
            next()
        }
    }

    /** Declines (skips) the specified offer and loads the next one. */
    fun decline(offer: FeedOffer) {
        Log.d(feedScreenLogTag, "Declined offer: ${offer.authorID}")
        repository.skip(offer, uid)
        next()
    }

    /** Updates the current offer to the previous one in the list, if available. */
    fun previous() {
        val state = _uiState.value
        val offers = state.offers
        val currentIndex = offers.indexOf(state.current)

        if (currentIndex > 0 && currentIndex < offers.size) {
            _uiState.value = state.copy(current = offers[currentIndex - 1])
        } else {
            Log.e(feedScreenLogTag, "Index out of bound for previous()")
        }
    }

    /** Navigates to the profile screen of the specified user. */
    fun goToProfile(userId: String) {
        navigation.goToProfileView(userId)
    }

    /** Shows or hides the menu for the current offer card. */
    fun showMenu() {
        _uiState.value = _uiState.value.copy(isMenuVisible = !_uiState.value.isMenuVisible)
    }

    /** Skips the current offer and moves to the next. */
    fun skip() {
        val currentOffer = uiState.value.current
        if (currentOffer.skillProvided.isNotEmpty()) {
            repository.skip(currentOffer, uid)
            next()
        }
    }

    // --- Track blocked/report actions for tests ---
    private val _blockedUsers = mutableListOf<String>()
    val blockedUsers: List<String>
        get() = _blockedUsers

    private val _reportedOffers = mutableListOf<FeedOffer>()
    val reportedOffers: List<FeedOffer>
        get() = _reportedOffers

    /**
     * Temporarily blocks a user by adding their ID to an in-memory list.
     *
     * This implementation is temporary and should later be replaced with a persistent or
     * server-side blocking mechanism.
     *
     * @param userId The unique identifier of the user to block.
     */
    fun blockUser(userId: String) {
        _blockedUsers.add(userId)
        repository.blockUser(userId)
    }

    /**
     * Temporarily reports an offer by adding it to an in-memory list.
     *
     * This implementation is temporary and should later be replaced with a proper reporting system
     * that sends data to a backend service.
     *
     * @param offer The [FeedOffer] to report.
     */
    fun reportOffer(offer: FeedOffer) {
        _reportedOffers.add(offer)
        repository.reportOffer(offer)
    }

    /** Test helper: directly sets the UI state. Only use in tests. */
    @VisibleForTesting
    fun setUiState(state: FeedScreenUiState) {
        _uiState.value = state
    }
}

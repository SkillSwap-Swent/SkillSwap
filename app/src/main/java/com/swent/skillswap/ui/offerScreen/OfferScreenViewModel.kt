package com.swent.skillswap.ui.offerScreen

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import com.swent.skillswap.model.offer.Offer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents the current state of the Offer screen UI.
 *
 * @property offers List of all available offers currently loaded.
 * @property current The offer currently being displayed.
 */
data class OfferScreenUiState(val offers: List<Offer> = emptyList(), val current: Offer = Offer())
/**
 * ViewModel responsible for managing offer data, navigation, and UI state for the Offer screen.
 *
 * It loads, updates, and transitions between offers, and handles user actions such as swiping or
 * navigating to another user’s profile.
 *
 * @property navigation The navigation handler for transitioning to other screens.
 * @property repository The data source used to retrieve and update offers.
 */
class OfferScreenViewModel(
    private val navigation: OfferNavigation = FakeNavigation(),
    private val repository: OfferRepository = FakeOfferRepository()
) : ViewModel() {
    /**
     * The unique identifier of the current user (temporary fallback when Firebase is unavailable).
     */
    private val uid: String = "AnoUser" // Firebase.auth.uid;
    /** Internal state of the Offer screen. */
    private val _uiState = MutableStateFlow(OfferScreenUiState())
    /** Publicly exposed, read-only state of the Offer screen. */
    val uiState: StateFlow<OfferScreenUiState> = _uiState.asStateFlow()

    init {
        runCatching { next() }
            .onFailure { Log.e("OfferScreen", "Failed to load initial offer", it) }
    }

    /**
     * Advances to the next offer in the list. If there are no offers, a new one is fetched and
     * displayed. If the current offer is the last in the list, a new offer is fetched and appended.
     */
    fun next() {
        val state = _uiState.value
        val offers = state.offers

        // If the offer list is empty, fetch the first one
        if (offers.isEmpty()) {
            runCatching {
                    val offer = repository.getPost(uid)
                    _uiState.value = OfferScreenUiState(offers = listOf(offer), current = offer)
                }
                .onFailure { Log.e("OfferScreen", "Failed to fetch first offer", it) }
            return
        }

        val currentIndex = offers.indexOf(state.current)

        // If current offer not found, default to first
        if (currentIndex == -1) {
            Log.w("OfferScreen", "Current offer not found in list; resetting to first.")
            _uiState.value = state.copy(current = offers.first())
            return
        }

        // If not at end → move to next
        if (currentIndex < offers.lastIndex) {
            _uiState.value = state.copy(current = offers[currentIndex + 1])
        }
        // If at end → fetch new offer
        else {
            runCatching {
                    val newOffer = repository.getPost(uid)
                    _uiState.value = state.copy(offers = offers + newOffer, current = newOffer)
                }
                .onFailure { Log.e("OfferScreen", "Failed to fetch new offer", it) }
        }
    }

    /**
     * Accepts the specified offer on behalf of the current user.
     *
     * @param offer the offer to be accepted
     */
    fun accept(offer: Offer) {
        repository.accept(offer, uid)
    }
    /**
     * Updates the current offer to the previous one in the list, if available.
     *
     * Logs an error if the current offer index is out of bounds.
     */
    fun previous() {
        val state = _uiState.value
        val offers = state.offers
        val currentIndex = offers.indexOf(state.current)

        if (currentIndex > 0 && currentIndex < offers.size) {
            _uiState.value = state.copy(current = offers[currentIndex - 1])
        } else {
            Log.e("OfferScreen", "Index out of Bound for previous from OfferScreen")
        }
    }
    /**
     * Navigates to the profile screen of the specified user.
     *
     * @param userId the unique identifier of the user whose profile should be displayed
     */
    fun goToProfile(userId: String) {
        navigation.goToProfileView(userId)
    }

    /** Test helper: directly sets the UI state. Only use in tests. */
    @VisibleForTesting
    fun setUiState(state: OfferScreenUiState) {
        _uiState.value = state
    }
}

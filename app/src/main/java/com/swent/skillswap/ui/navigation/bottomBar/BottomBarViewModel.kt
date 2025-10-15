package com.swent.skillswap.ui.navigation.bottomBar

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.swent.skillswap.model.navigation.FakeNavigationBottomBar
import com.swent.skillswap.model.navigation.NavigationBottomBar

/**
 * Enum representing the screens managed by the BottomBar.
 *
 * Used to track which screen is currently selected in the UI.
 *
 * @author Joey Gugler Made Using Ai (chatGPT)
 */
enum class BottomBarScreen {
    /** Profile screen. */
    PROFILE,

    /** Offer screen. */
    OFFER,

    /** Chat screen. */
    CHAT
}

/**
 * UI state for the [BottomBar] composable.
 *
 * @property selectedScreen The currently selected screen in the bottom bar.
 * @author Joey Gugler Made Using Ai (chatGPT)
 */
data class BottomBarUiState(val selectedScreen: BottomBarScreen = BottomBarScreen.OFFER)

/**
 * ViewModel for the [BottomBar] composable.
 *
 * Handles the current UI state of the bottom bar and triggers navigation actions via a
 * [NavigationBottomBar] implementation.
 *
 * @property navigation The navigation handler used to navigate to screens. Defaults to
 *   [FakeNavigationBottomBar] for testing purposes.
 * @property uiState The observable UI state of the bottom bar.
 * @author Joey Gugler Made Using Ai (chatGPT)
 */
class BottomBarViewModel(val navigation: NavigationBottomBar) {
    /** UI state observed by the BottomBar composable. */
    var uiState: MutableState<BottomBarUiState> = mutableStateOf(BottomBarUiState())
        private set

    /**
     * Called when a bottom bar button is selected.
     *
     * Updates the [uiState.selectedScreen] and triggers the corresponding navigation action in
     * [navigation].
     *
     * @param screen The [BottomBarScreen] that was selected.
     */
    fun onScreenSelected(screen: BottomBarScreen) {
        uiState.value = uiState.value.copy(selectedScreen = screen)
        when (screen) {
            BottomBarScreen.PROFILE -> navigation.goToProfile()
            BottomBarScreen.OFFER -> navigation.goToOfferScreen()
            BottomBarScreen.CHAT -> navigation.goToChat()
        }
    }
}

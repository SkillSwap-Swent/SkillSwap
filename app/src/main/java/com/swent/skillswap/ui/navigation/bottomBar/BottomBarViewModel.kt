package com.swent.skillswap.ui.navigation.bottomBar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swent.skillswap.model.navigation.NavigationBottomBar
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

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
data class BottomBarUiState(val selectedScreen: BottomBarScreen = BottomBarScreen.PROFILE)

/** Events emitted by [BottomBarViewModel] for one-time actions like navigation. */
sealed class BottomBarEvent {
    object NavigateToProfile : BottomBarEvent()

    object NavigateToOffer : BottomBarEvent()

    object NavigateToChat : BottomBarEvent()
}

/**
 * ViewModel for the [BottomBar] composable.
 *
 * Handles UI state and emits navigation events when a screen is selected.
 *
 * @property navigation Optional navigation handler, used mainly for previews or tests.
 * @author Joey Gugler
 */
class BottomBarViewModel(val navigation: NavigationBottomBar) : ViewModel() {

    /** UI state observed by the BottomBar composable. */
    var uiState = androidx.compose.runtime.mutableStateOf(BottomBarUiState())
        private set

    /** Event flow for one-time navigation events. */
    private val _eventFlow = MutableSharedFlow<BottomBarEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    /** Called when a bottom bar button is selected. */
    fun onScreenSelected(screen: BottomBarScreen) {
        uiState.value = uiState.value.copy(selectedScreen = screen)
        viewModelScope.launch {
            when (screen) {
                BottomBarScreen.PROFILE -> _eventFlow.emit(BottomBarEvent.NavigateToProfile)
                BottomBarScreen.OFFER -> _eventFlow.emit(BottomBarEvent.NavigateToOffer)
                BottomBarScreen.CHAT -> _eventFlow.emit(BottomBarEvent.NavigateToChat)
            }
        }
    }
}

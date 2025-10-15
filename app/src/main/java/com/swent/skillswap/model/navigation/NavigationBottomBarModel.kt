package com.swent.skillswap.model.navigation

import com.swent.skillswap.ui.navigation.NavigationActions
import com.swent.skillswap.ui.navigation.Screen

class NavigationBottomBarModel(private val navigationActions: NavigationActions) :
    NavigationBottomBar {
    override fun goToProfile() {
        navigationActions.navigateTo(Screen.Profile)
    }

    override fun goToChat() {
        navigationActions.navigateTo(Screen.Chat)
    }

    override fun goToOfferScreen() {
        navigationActions.navigateTo(Screen.Offers)
    }
}

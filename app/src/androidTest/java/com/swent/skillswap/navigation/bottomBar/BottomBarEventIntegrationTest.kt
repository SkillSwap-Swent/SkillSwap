package com.swent.skillswap.navigation.bottomBar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.swent.skillswap.model.navigation.FakeNavigationBottomBar
import com.swent.skillswap.ui.navigation.bottomBar.*
import org.junit.Rule
import org.junit.Test

/**
 * Integration test ensuring that [BottomBar] correctly updates its UI state when buttons are
 * clicked, based on [BottomBarViewModel] state changes.
 *
 * @author Joey Gugler
 */
class BottomBarEventIntegrationTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun clickingButtons_updatesSelectedState() {
        val fakeNav = FakeNavigationBottomBar()
        val vm = BottomBarViewModel(fakeNav)

        composeTestRule.setContent { BottomBar(vm = vm) }

        // Initial state: Profile is selected by default (from BottomBarUiState)
        composeTestRule.onNodeWithTag(BottomBarTestTag.PROFILE_BUTTON).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(BottomBarTestTag.OFFER_SCREEN_BUTTON).assertIsEnabled()
        composeTestRule.onNodeWithTag(BottomBarTestTag.CHAT_BUTTON).assertIsEnabled()

        // Click Offer
        composeTestRule.onNodeWithTag(BottomBarTestTag.OFFER_SCREEN_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Verify new state: Offer is selected
        composeTestRule.onNodeWithTag(BottomBarTestTag.PROFILE_BUTTON).assertIsEnabled()
        composeTestRule.onNodeWithTag(BottomBarTestTag.OFFER_SCREEN_BUTTON).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(BottomBarTestTag.CHAT_BUTTON).assertIsEnabled()

        // Click Chat
        composeTestRule.onNodeWithTag(BottomBarTestTag.CHAT_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Verify new state: Chat is selected
        composeTestRule.onNodeWithTag(BottomBarTestTag.PROFILE_BUTTON).assertIsEnabled()
        composeTestRule.onNodeWithTag(BottomBarTestTag.OFFER_SCREEN_BUTTON).assertIsEnabled()
        composeTestRule.onNodeWithTag(BottomBarTestTag.CHAT_BUTTON).assertIsNotEnabled()

        // Click Profile
        composeTestRule.onNodeWithTag(BottomBarTestTag.PROFILE_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Verify new state: Profile is selected
        composeTestRule.onNodeWithTag(BottomBarTestTag.PROFILE_BUTTON).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(BottomBarTestTag.OFFER_SCREEN_BUTTON).assertIsEnabled()
        composeTestRule.onNodeWithTag(BottomBarTestTag.CHAT_BUTTON).assertIsEnabled()
    }

    @Test
    fun bottomBar_buttonsAreDisplayed() {
        val fakeNav = FakeNavigationBottomBar()
        composeTestRule.setContent { BottomBar(BottomBarViewModel(fakeNav)) }

        composeTestRule.onNodeWithTag(BottomBarTestTag.PROFILE_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(BottomBarTestTag.OFFER_SCREEN_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(BottomBarTestTag.CHAT_BUTTON).assertIsDisplayed()
    }
}

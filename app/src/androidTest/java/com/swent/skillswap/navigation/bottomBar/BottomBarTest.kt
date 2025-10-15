package com.swent.skillswap.navigation.bottomBar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.swent.skillswap.model.navigation.FakeNavigationBottomBar
import com.swent.skillswap.ui.navigation.bottomBar.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** @author Joey Gugler Made Using Ai (chatGPT) */
class BottomBarTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun bottomBar_buttonsAreDisplayed_andClickable() {
        // Arrange
        val fakeNav = FakeNavigationBottomBar()
        val vm = BottomBarViewModel(fakeNav)

        // Set the BottomBar composable
        composeTestRule.setContent { BottomBar(vm = vm) }

        // Assert: all buttons are displayed
        composeTestRule.onNodeWithTag(BottomBarTestTag.PROFILE_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(BottomBarTestTag.OFFER_SCREEN_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(BottomBarTestTag.CHAT_BUTTON).assertIsDisplayed()

        composeTestRule.waitForIdle()

        // Click Offers
        fakeNav.reset()
        composeTestRule.onNodeWithTag(BottomBarTestTag.OFFER_SCREEN_BUTTON).performClick()
        composeTestRule.waitForIdle()
        assertEquals(BottomBarScreen.OFFER, vm.uiState.value.selectedScreen)
        assertTrue(fakeNav.goToOfferScreenCalled)

        // Click Profile
        fakeNav.reset()
        composeTestRule.onNodeWithTag(BottomBarTestTag.PROFILE_BUTTON).performClick()
        composeTestRule.waitForIdle()
        assertEquals(BottomBarScreen.PROFILE, vm.uiState.value.selectedScreen)
        assertTrue(fakeNav.goToProfileCalled)

        // Click Chat
        fakeNav.reset()
        composeTestRule.onNodeWithTag(BottomBarTestTag.CHAT_BUTTON).performClick()
        composeTestRule.waitForIdle()
        assertEquals(BottomBarScreen.CHAT, vm.uiState.value.selectedScreen)
        assertTrue(fakeNav.goToChatCalled)
    }

    @Test
    fun bottomBar_containerIsDisplayed() {
        composeTestRule.setContent { BottomBar(BottomBarViewModel()) }
        composeTestRule.onNodeWithTag(BottomBarTestTag.BOTTOM_BAR).assertIsDisplayed()
    }
}

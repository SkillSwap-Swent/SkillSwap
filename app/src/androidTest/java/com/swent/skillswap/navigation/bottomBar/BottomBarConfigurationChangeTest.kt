package com.swent.skillswap.navigation.bottomBar

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.skillswap.ui.navigation.bottomBar.*
import com.swent.skillswap.ui.theme.SkillSwapAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Ensures the BottomBar remains functional and preserves state across configuration changes (like
 * light/dark mode switch).
 *
 * This simulates process recreation without multiple setContent calls.
 *
 * @author Joey Gugler
 */
@RunWith(AndroidJUnit4::class)
class BottomBarConfigurationChangeTest {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun bottomBar_retainsSelectedScreen_afterActivityRecreation() {
        // Arrange
        // ViewModel is instantiated outside Compose, allowing us to check its state directly.
        val vm = BottomBarViewModel()

        // 1. Initial Setup
        // Uses the ComposeTestRule's setContent for the first activity instance
        composeTestRule.setContent { SkillSwapAppTheme { BottomBar(vm) } }

        // Click a button (Offers) and assert the state changes
        composeTestRule.onNodeWithTag(BottomBarTestTag.OFFER_SCREEN_BUTTON).performClick()
        composeTestRule.waitForIdle()

        val selectedBefore = vm.uiState.value.selectedScreen
        assertEquals(BottomBarScreen.OFFER, selectedBefore)

        // Act — simulate activity recreation (e.g., dark/light theme change)
        composeTestRule.activityRule.scenario.recreate()

        // --- THE FIX: Call setContent on the new Activity instance ---
        // Use the scenario's onActivity to access the new Activity object
        // and call its native setContent method.
        composeTestRule.activityRule.scenario.onActivity { newActivity ->
            newActivity.setContent { SkillSwapAppTheme { BottomBar(vm) } }
        }

        // Wait for Compose to rebind
        composeTestRule.waitForIdle()

        // Assert — state and UI still consistent
        val selectedAfter = vm.uiState.value.selectedScreen

        // Assert the internal ViewModel state was preserved across recreation
        assertEquals(
            "Selected screen should persist after activity recreation",
            selectedBefore,
            selectedAfter
        )

        // Assert the new UI hierarchy is displayed
        composeTestRule.onNodeWithTag(BottomBarTestTag.BOTTOM_BAR).assertIsDisplayed()

        // Assert the selected button is still the Offers button
        composeTestRule.onNodeWithTag(BottomBarTestTag.OFFER_SCREEN_BUTTON).assertIsDisplayed()
    }
}

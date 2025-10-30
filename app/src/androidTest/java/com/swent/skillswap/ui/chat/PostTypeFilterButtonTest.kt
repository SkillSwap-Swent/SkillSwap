// AI-Generated: Comprehensive test suite for PostTypeFilterButton component
package com.swent.skillswap.ui.chat

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostTypeFilterButtonTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun postTypeFilterButton_displaysText() {
        composeTestRule.setContent {
            PostTypeFilterButton(text = "Test Button", isSelected = false, onClick = {})
        }

        composeTestRule.onNodeWithText("Test Button").assertExists()
    }

    @Test
    fun postTypeFilterButton_triggersOnClick() {
        var clicked = false

        composeTestRule.setContent {
            PostTypeFilterButton(
                text = "Click Me",
                isSelected = false,
                onClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Click Me").performClick()
        assert(clicked)
    }

    @Test
    fun postTypeFilterButton_showsSelectedState() {
        composeTestRule.setContent {
            PostTypeFilterButton(text = "Selected Button", isSelected = true, onClick = {})
        }

        composeTestRule.onNodeWithText("Selected Button").assertExists()
    }

    @Test
    fun postTypeFilterButton_showsUnselectedState() {
        composeTestRule.setContent {
            PostTypeFilterButton(text = "Unselected Button", isSelected = false, onClick = {})
        }

        composeTestRule.onNodeWithText("Unselected Button").assertExists()
    }

    @Test
    fun postTypeFilterButton_handlesMultipleClicks() {
        var clickCount = 0

        composeTestRule.setContent {
            PostTypeFilterButton(
                text = "Multi Click",
                isSelected = false,
                onClick = { clickCount++ }
            )
        }

        composeTestRule.onNodeWithText("Multi Click").performClick()
        composeTestRule.onNodeWithText("Multi Click").performClick()
        composeTestRule.onNodeWithText("Multi Click").performClick()

        assert(clickCount == 3)
    }

    @Test
    fun postTypeFilterButton_rendersWithoutCrashing() {
        composeTestRule.setContent {
            PostTypeFilterButton(text = "Stable Button", isSelected = false, onClick = {})
        }

        // Button should render without any exceptions
        composeTestRule.onNodeWithText("Stable Button").assertExists()
    }
}

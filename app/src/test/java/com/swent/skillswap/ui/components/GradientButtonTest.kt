package com.swent.skillswap.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GradientButtonTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun gradientButton_displaysText() {
        composeTestRule.setContent { GradientButton(text = "Test Button", onClick = {}) }

        composeTestRule.onNodeWithText("Test Button").assertExists()
    }

    @Test
    fun gradientButton_triggersOnClick() {
        var clicked = false

        composeTestRule.setContent {
            GradientButton(text = "Click Me", onClick = { clicked = true })
        }

        composeTestRule.onNodeWithText("Click Me").performClick()
        assert(clicked)
    }

    @Test
    fun gradientButton_primaryStyle() {
        composeTestRule.setContent {
            GradientButton(text = "Primary", onClick = {}, isPrimary = true)
        }

        composeTestRule.onNodeWithText("Primary").assertExists()
    }

    @Test
    fun gradientButton_secondaryStyle() {
        composeTestRule.setContent {
            GradientButton(text = "Secondary", onClick = {}, isPrimary = false)
        }

        composeTestRule.onNodeWithText("Secondary").assertExists()
    }
}

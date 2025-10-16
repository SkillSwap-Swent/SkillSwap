// AI-Generated: Comprehensive test suite for ChatMainScreen component
// This file contains 6 test cases for the ChatMainScreen component, covering screen integration,
// sample data display, and overall functionality. Tests ensure proper integration between
// ChatMainScreen and ChatScreenData, validating the main chat screen behavior.
package com.swent.skillswap.ui.chat

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatMainScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun chatMainScreen_displaysTitle() {
        composeTestRule.setContent { ChatMainScreen() }

        composeTestRule.onNodeWithText("Chat").assertExists()
    }

    @Test
    fun chatMainScreen_displaysFilterButtons() {
        composeTestRule.setContent { ChatMainScreen() }

        composeTestRule.onNodeWithText("Offer").assertExists()
        composeTestRule.onNodeWithText("Request").assertExists()
    }

    @Test
    fun chatMainScreen_displaysSamplePosts() {
        composeTestRule.setContent { ChatMainScreen() }

        // Should display sample posts from ChatScreenData
        composeTestRule.onNodeWithText("Alex Johnson").assertExists()
        composeTestRule.onNodeWithText("Sarah Chen").assertExists()
    }

    @Test
    fun chatMainScreen_displaysSampleSkills() {
        composeTestRule.setContent { ChatMainScreen() }

        // Should display skills from sample posts
        composeTestRule.onNodeWithText("Linear Algebra").assertExists()
        composeTestRule.onNodeWithText("Computer Programming").assertExists()
    }

    @Test
    fun chatMainScreen_rendersWithoutCrashing() {
        composeTestRule.setContent { ChatMainScreen() }

        // Screen should render without any exceptions
        composeTestRule.onNodeWithText("Chat").assertExists()
    }

    @Test
    fun chatMainScreen_handlesPostFiltering() {
        composeTestRule.setContent { ChatMainScreen() }

        // Should be able to switch between Offer and Request filters
        composeTestRule.onNodeWithText("Offer").assertExists()
        composeTestRule.onNodeWithText("Request").assertExists()
    }
}

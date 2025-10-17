// AI-Generated: Comprehensive test suite for profile screen components
package com.swent.skillswap.user

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.skillswap.ui.user.MySkillsScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MySkillsScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun mySkillsScreen_displaysTitle() {
        composeTestRule.setContent { MySkillsScreen(onBackClick = {}) }

        composeTestRule.onNodeWithText("My Skills").assertExists()
    }

    @Test
    fun mySkillsScreen_displaysBackButton() {
        composeTestRule.setContent { MySkillsScreen(onBackClick = {}) }

        composeTestRule.onNodeWithText("Back").assertExists()
    }

    @Test
    fun mySkillsScreen_backButtonTriggersCallback() {
        var backClicked = false

        composeTestRule.setContent { MySkillsScreen(onBackClick = { backClicked = true }) }

        composeTestRule.onNodeWithText("Back").performClick()
        assert(backClicked)
    }

    @Test
    fun mySkillsScreen_rendersWithoutCrashing() {
        composeTestRule.setContent { MySkillsScreen(onBackClick = {}) }

        // Screen should render without any exceptions
        composeTestRule.onNodeWithText("My Skills").assertExists()
    }
}

// AI-Generated: Comprehensive test suite for profile screen components
package com.swent.skillswap.ui.components

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.skillswap.ui.utils.SkillSwapButtonV1
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GradientButtonTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun gradientButton_displaysText() {
        composeTestRule.setContent { SkillSwapButtonV1(onClick = {}) { Text("Test Button") } }

        composeTestRule.onNodeWithText("Test Button").assertExists()
    }

    @Test
    fun gradientButton_triggersOnClick() {
        var clicked = false

        composeTestRule.setContent {
            SkillSwapButtonV1(onClick = { clicked = true }) { Text("Click Me") }
        }

        composeTestRule.onNodeWithText("Click Me").performClick()
        assert(clicked)
    }

    @Test
    fun gradientButton_rendersWithContent() {
        composeTestRule.setContent { SkillSwapButtonV1(onClick = {}) { Text("Primary") } }

        composeTestRule.onNodeWithText("Primary").assertExists()
    }

    @Test
    fun gradientButton_rendersWithCustomContent() {
        composeTestRule.setContent { SkillSwapButtonV1(onClick = {}) { Text("Secondary") } }

        composeTestRule.onNodeWithText("Secondary").assertExists()
    }
}

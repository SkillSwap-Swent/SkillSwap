package com.swent.skillswap.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.skillswap.ui.utils.SkillSwapButtonOutline
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SkillSwapButtonTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun button_displays_label_and_is_clickable() {
        var clicked = false
        composeTestRule.setContent {
            MaterialTheme {
                SkillSwapButtonOutline(labelText = "Click Me", onClick = { clicked = true })
            }
        }

        // Verify the text is displayed
        composeTestRule.onNodeWithText("Click Me").assertIsDisplayed().assertHasClickAction()

        // Perform a click
        composeTestRule.onNodeWithText("Click Me").performClick()

        // Verify click triggered
        assert(clicked)
    }

    @Test
    fun button_is_disabled_when_enabled_false() {
        composeTestRule.setContent {
            MaterialTheme {
                SkillSwapButtonOutline(labelText = "Disabled", onClick = {}, enabled = false)
            }
        }

        // Verify it shows and is disabled
        composeTestRule.onNodeWithText("Disabled").assertIsDisplayed().assertIsNotEnabled()
    }

    @Test
    fun button_with_icon_displays_icon_and_text() {
        composeTestRule.setContent {
            MaterialTheme {
                SkillSwapButtonOutline(labelText = "Add", onClick = {}, icon = Icons.Default.Add)
            }
        }

        // Verify text
        composeTestRule.onNodeWithText("Add").assertIsDisplayed()

        // Verify icon using the contentDescription we added
        composeTestRule.onNodeWithContentDescription("Add icon").assertIsDisplayed()
    }
}

// AI-Generated: Comprehensive test suite for profile screen components
package com.swent.skillswap.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileDividerTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun profileDivider_rendersWithoutCrashing() {
        composeTestRule.setContent { ProfileDivider() }

        // Divider should render without any exceptions
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun profileDivider_rendersMultipleInstances() {
        composeTestRule.setContent {
            ProfileDivider()
            ProfileDivider()
            ProfileDivider()
        }

        // Multiple dividers should render without issues
        composeTestRule.onRoot().assertExists()
    }
}

package com.swent.skillswap.user

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.swent.skillswap.ui.user.UserScreen
import org.junit.Rule
import org.junit.Test

class UserScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun userScreen_displaysPlaceholderText() {
        composeTestRule.setContent { UserScreen() }
        composeTestRule.onNodeWithText("Placeholder Profile Screen").assertIsDisplayed()
    }
}

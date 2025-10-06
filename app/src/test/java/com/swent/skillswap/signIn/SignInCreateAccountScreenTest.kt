/** @author Topaze17(Eliott) */
package com.swent.skillswap.signIn

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.ui.signIn.CreateAccountTags
import com.swent.skillswap.ui.signIn.SignInCreateAccountScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignInCreateAccountScreenTest : TestCase() {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        composeTestRule.setContent { SignInCreateAccountScreen() }
    }

    @Test
    fun testEverythingIsDisplay() {
        composeTestRule.onNodeWithTag(CreateAccountTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.DONE_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.EMAIL_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.SKILLS_FLOW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.PASSWORD_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CreateAccountTags.USERNAME_FIELD).assertIsDisplayed()
    }
}

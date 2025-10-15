// AI-Generated: Comprehensive test suite for profile screen components
/** @author Topaze17(Eliott) */
package com.swent.skillswap.signIn

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.ui.signIn.SignInMainScreen
import com.swent.skillswap.ui.signIn.SignInTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignInMainScreenTest : TestCase() {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        composeTestRule.setContent { SignInMainScreen() }
    }

    @Test
    fun testEverythingIsDisplay() {
        composeTestRule.onNodeWithTag(SignInTags.LOGO).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.LOGO).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SignInTags.SIGN_IN_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.SIGN_IN_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SignInTags.OR_TEXT).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.OR_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SignInTags.GOOGLE_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.GOOGLE_BUTTON).assertIsDisplayed()
    }

    /** smoke test as currently we cannot see the consequence of a success */
    @Test
    fun smokeTestForBackendCoverage() {
        composeTestRule.onNodeWithTag(SignInTags.GOOGLE_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.GOOGLE_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SignInTags.GOOGLE_BUTTON).performClick()
        composeTestRule.onNodeWithTag(SignInTags.SIGN_IN_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.SIGN_IN_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SignInTags.SIGN_IN_BUTTON).performClick()
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SignInTags.CREATE_ACCOUNT_TEXT).performClick()
    }
}

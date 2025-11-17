package com.swent.skillswap.ui

import androidx.compose.ui.test.junit4.createComposeRule
import com.swent.skillswap.ui.user.ProfileTestTags



import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class DumbTest : TestCase() {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun test() = run {
        composeTestRule.setContent {
            Greatings()
        }
        composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertIsDisplayed()
    }
}

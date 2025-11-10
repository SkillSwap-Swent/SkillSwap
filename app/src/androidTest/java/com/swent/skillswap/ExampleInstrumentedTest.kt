// AI-Generated: Comprehensive test suite for profile screen components
package com.swent.skillswap

import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.ui.auth.SignInTags
import com.swent.skillswap.utils.FirebaseEmulator
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest : TestCase() {

    @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()
    val ctx = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun test() = run {
        step("Start Main Activity") {
            FirebaseApp.initializeApp(ctx)
            assert(FirebaseEmulator.isRunning) { Log.e("Firebase", "not running") }
            composeTestRule.onNodeWithTag(SignInTags.LOGO).performScrollTo()
            composeTestRule.onNodeWithTag(SignInTags.LOGO).assertIsDisplayed()
        }
    }
}

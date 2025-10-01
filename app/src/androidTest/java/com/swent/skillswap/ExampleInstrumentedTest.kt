package com.swent.skillswap

import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.swent.skillswap.screen.MainScreen
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.swent.skillswap.utils.FirebaseEmulator
import io.github.kakaocup.compose.node.element.ComposeScreen
import org.junit.Assert.*
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
      ComposeScreen.onComposeScreen<MainScreen>(composeTestRule) {
        simpleText {
          FirebaseApp.initializeApp(ctx)
          assert(FirebaseEmulator.isRunning) { Log.e("Firebase", "not running") }
          assertIsDisplayed()
          assertTextEquals("Hello Android!")
        }
      }
    }
  }
}

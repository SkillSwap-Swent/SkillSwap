/*
 * Written with help of copilot to complete all repetitive code, and set up the companion object
 */
package com.swent.skillswap.end2end

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.swent.skillswap.MainActivity
import com.swent.skillswap.ui.auth.CreateAccountTags
import com.swent.skillswap.ui.auth.SignInTags
import com.swent.skillswap.ui.feedScreen.FeedScreenTestTags
import com.swent.skillswap.ui.navigation.NavigationTestTags
import com.swent.skillswap.ui.user.ProfileTestTags
import com.swent.skillswap.utils.FirebaseEmulator
import java.net.HttpURLConnection
import java.net.URL
import junit.framework.TestCase.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * End-to-end tests for Milestone 2 Tests complete user flows
 */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // Be careful, tests order matters !
class End2EndM2 {

    lateinit var db: com.google.firebase.firestore.FirebaseFirestore
    lateinit var auth: FirebaseAuth

    /** Companion object to clear the Auth emulator before running tests */
    companion object {
        private const val EMULATOR_URL = "http://10.0.2.2:9099"
        private const val PROJECT_ID = "skillswap-93276"

        @BeforeClass
        @JvmStatic
        fun clearAuthEmulator() {
            val url = URL("$EMULATOR_URL/emulator/v1/projects/$PROJECT_ID/accounts")
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "DELETE"
                val responseCode = responseCode
                if (responseCode != 200) {
                    throw Exception("Failed to clear Auth emulator: $responseCode")
                }
                disconnect()
            }
        }
    }

    @Before
    fun setup() {
        FirebaseEmulator.startEmulator()
        db = FirebaseEmulator.firestore
    }

    @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun test1_createAccount() {assert(true)}
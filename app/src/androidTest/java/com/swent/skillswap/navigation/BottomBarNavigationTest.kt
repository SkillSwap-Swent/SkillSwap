package com.swent.skillswap.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.skillswap.SkillSwapApp
import com.swent.skillswap.resources.theme.SkillSwapAppTheme
import com.swent.skillswap.ui.feed.FeedScreenTestTags
import com.swent.skillswap.ui.navigation.*
import com.swent.skillswap.ui.user.ProfileTestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BottomBarNavigationTest {

    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var navController: NavHostController

    @Before
    fun setUp() {
        composeTestRule.setContent {
            navController = rememberNavController()

            SkillSwapAppTheme { SkillSwapApp(navController = navController) }
        }
        composeTestRule.waitForIdle()

        // Navigate to the Profile screen
        composeTestRule.runOnUiThread {
            navController.navigate(Screen.Profile.route) {
                popUpTo(Screen.AuthMain.route) { inclusive = true }
            }
        }
        composeTestRule.waitForIdle()
    }

    /** @author Topaze17(Eliott) made with chatGPT */
    fun ComposeTestRule.assertAnyDisplayed(vararg tags: String) {
        val errors = mutableListOf<String>()

        for (tag in tags) {
            try {
                onNodeWithTag(tag).assertIsDisplayed()
                return // success → at least one is displayed
            } catch (e: AssertionError) {
                errors.add("Node with tag '$tag' not displayed")
            }
        }

        // If we reach here, none of them were displayed
        throw AssertionError(
            "None of the expected nodes were displayed:\n" + errors.joinToString("\n")
        )
    }

    @Test
    fun allScreens_displayBottomNavigationBar() {
        val screens =
            listOf(
                NavigationTestTags.PROFILE_TAB,
                NavigationTestTags.FEED_TAB,
                NavigationTestTags.CHAT_TAB
            )

        screens.forEach { tab ->
            composeTestRule.onNodeWithTag(tab).performClick()
            composeTestRule.waitForIdle()

            // Verify bottom navigation is always displayed
            composeTestRule
                .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU)
                .assertIsDisplayed()

            // Verify all tabs remain visible
            composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsDisplayed()
            composeTestRule.onNodeWithTag(NavigationTestTags.FEED_TAB).assertIsDisplayed()
            composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).assertIsDisplayed()
        }
    }

    @Test
    fun bottomBar_clickingFeedTabNavigatesToFeedScreen() {
        // Navigate to Feed
        composeTestRule.onNodeWithTag(NavigationTestTags.FEED_TAB).performClick()
        composeTestRule.waitForIdle()

        // Verify feed screen is displayed
        composeTestRule.assertAnyDisplayed(
            FeedScreenTestTags.FEED_CARD,
            FeedScreenTestTags.NO_OFFER_TEXT
        )
    }

    @Test
    fun bottomBar_clickingChatTabNavigatesToChatScreen() {
        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).assertIsSelected()
    }

    @Test
    fun bottomBar_navigationBetweenAllTabs() {
        // Start at Profile (initial state)
        composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsSelected()
        composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertIsDisplayed()

        // Navigate to Feed
        composeTestRule.onNodeWithTag(NavigationTestTags.FEED_TAB).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(NavigationTestTags.FEED_TAB).assertIsSelected()
        composeTestRule.assertAnyDisplayed(
            FeedScreenTestTags.FEED_CARD,
            FeedScreenTestTags.NO_OFFER_TEXT
        )

        // Navigate to Chat
        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).assertIsSelected()

        // Navigate back to Profile
        composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsSelected()
        composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertIsDisplayed()
    }

    @Test
    fun bottomBar_onlyOneTabIsSelectedAtATime() {
        // Click Feed tab
        composeTestRule.onNodeWithTag(NavigationTestTags.FEED_TAB).performClick()
        composeTestRule.waitForIdle()

        // Verify only Feed is selected
        composeTestRule.onNodeWithTag(NavigationTestTags.FEED_TAB).assertIsSelected()
        composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsNotSelected()
        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).assertIsNotSelected()
    }

    @Test
    fun bottomBar_clickingCurrentTabDoesNotNavigate() {
        // Start at Profile screen
        composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsSelected()

        val initialProfileTitle = composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE)
        initialProfileTitle.assertIsDisplayed()

        // Click Profile tab again (already selected)
        composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()
        composeTestRule.waitForIdle()

        // Verify we're still on the same Profile screen
        composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).assertIsSelected()
        initialProfileTitle.assertIsDisplayed()

        // Navigate to Feed
        composeTestRule.onNodeWithTag(NavigationTestTags.FEED_TAB).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.assertAnyDisplayed(
            FeedScreenTestTags.FEED_CARD,
            FeedScreenTestTags.NO_OFFER_TEXT
        )

        // Click Feed tab again (already selected)
        composeTestRule.onNodeWithTag(NavigationTestTags.FEED_TAB).performClick()
        composeTestRule.waitForIdle()

        // Verify we're still on the same Feed screen
        composeTestRule.onNodeWithTag(NavigationTestTags.FEED_TAB).assertIsSelected()
        composeTestRule.assertAnyDisplayed(
            FeedScreenTestTags.FEED_CARD,
            FeedScreenTestTags.NO_OFFER_TEXT
        )

        // Navigate to Chat
        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).assertIsSelected()

        // Click Chat tab again (already selected)
        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()
        composeTestRule.waitForIdle()

        // Verify we're still on the same Chat screen
        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).assertIsSelected()
    }

}

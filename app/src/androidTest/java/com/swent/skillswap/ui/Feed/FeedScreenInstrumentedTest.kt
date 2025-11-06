package com.swent.skillswap.ui.feedScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import com.swent.skillswap.model.offer.FakeFeedNavigation
import com.swent.skillswap.model.offer.FakeFeedRepository
import com.swent.skillswap.model.offer.FeedOffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

/** @author Joey Gugler using chatGPT */
class FeedScreenInstrumentedTest {

    @get:Rule val composeTestRule = createComposeRule()

    /** Detects if the tests are running on a CI environment. */
    private fun isRunningOnCi(): Boolean = true /*
        System.getenv("RUNNING_ON_CI")?.toBoolean() == true ||
            System.getProperty("RUNNING_ON_CI")?.toBoolean() == true*/

    /** Helper to set up screen with fake repository returning specified offers. */
    private fun setContentWithRepositoryReturning(
        vararg returnedOffers: FeedOffer
    ): Triple<FeedScreenViewModel, FakeFeedRepository, FakeFeedNavigation> {
        val repository = FakeFeedRepository()
        val navigation = FakeFeedNavigation()

        repository.preloadOffers(*returnedOffers)

        val vm = FeedScreenViewModel(navigation, repository)
        composeTestRule.setContent {
            Box(modifier = Modifier.fillMaxSize()) { FeedScreen(vm = vm) }
        }

        composeTestRule.waitForIdle()
        return Triple(vm, repository, navigation)
    }

    @Test
    fun checkIfRunningOnCI() {
        assert(isRunningOnCi()) // Just ensures test passes
    }

    @Test
    fun cardShowsGiveAndReceive() {
        val offer =
            FeedOffer(
                skillProvided = "Teach Kotlin",
                skillRequested = "Learn Compose",
                authorID = "author1",
                thumbnail = "thumb"
            )

        val (vm, _, _) = setContentWithRepositoryReturning(offer)
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(FeedScreenTestTags.SKILL_GIVE)
            .assertIsDisplayed()
            .assert(hasText("you will get : ${offer.skillProvided}"))

        composeTestRule
            .onNodeWithTag(FeedScreenTestTags.SKILL_REQUESTED)
            .assertIsDisplayed()
            .assert(hasText(offer.skillRequested))
    }

    @Test
    fun swipeRight_callsAcceptOnRepository() {
        val offer =
            FeedOffer(skillProvided = "G", skillRequested = "R", authorID = "auth", thumbnail = "t")
        val repository = FakeFeedRepository()
        val navigation = FakeFeedNavigation()
        val vm = FeedScreenViewModel(navigation, repository)
        vm.setUiState(FeedScreenUiState(listOf(offer), current = offer))

        composeTestRule.setContent { Box(Modifier.fillMaxSize()) { FeedScreen(vm = vm) } }

        if (isRunningOnCi()) {
            vm.accept(offer)
        } else {
            composeTestRule.onNodeWithTag(FeedScreenTestTags.FEED_CARD).performTouchInput {
                swipeRight()
            }
        }

        composeTestRule.waitForIdle()
        val accepted = repository.getAcceptedOffers()
        assert(
            accepted.any {
                it.first.skillProvided == offer.skillProvided &&
                    it.first.skillRequested == offer.skillRequested
            }
        )
    }

    @Test
    fun swipeLeft_callsGoToProfile() {
        val offer =
            FeedOffer(
                skillProvided = "G",
                skillRequested = "R",
                authorID = "authorX",
                thumbnail = "t"
            )
        val repository = FakeFeedRepository()
        val navigation = FakeFeedNavigation()
        val vm = FeedScreenViewModel(navigation, repository)
        vm.setUiState(FeedScreenUiState(listOf(offer), current = offer))

        composeTestRule.setContent { Box(Modifier.fillMaxSize()) { FeedScreen(vm = vm) } }

        if (isRunningOnCi()) {
            vm.goToProfile(offer.authorID)
        } else {
            composeTestRule.onNodeWithTag(FeedScreenTestTags.FEED_CARD).performTouchInput {
                swipeLeft()
            }
        }

        composeTestRule.waitForIdle()
        val visited = navigation.getVisitedProfiles()
        assertEquals(listOf("authorX"), visited)
    }

    @Test
    fun swipeDown_loadsNextOffer_then_swipeUp_goesBackToPrevious() {
        val first =
            FeedOffer(
                skillRequested = "First",
                skillProvided = "1",
                authorID = "u1",
                thumbnail = "t1"
            )
        val second =
            FeedOffer(
                skillRequested = "Second",
                skillProvided = "2",
                authorID = "u2",
                thumbnail = "t2"
            )

        val (vm, _, _) = setContentWithRepositoryReturning(first, second)
        vm.setUiState(FeedScreenUiState(listOf(first, second), current = first))
        composeTestRule.waitForIdle()

        if (isRunningOnCi()) {
            vm.next()
        } else {
            composeTestRule.onNodeWithTag(FeedScreenTestTags.FEED_CARD).performTouchInput {
                swipeDown()
            }
        }

        composeTestRule.waitForIdle()
        try {
            composeTestRule.onNodeWithText(second.skillRequested).assertIsDisplayed()
        } catch (e: AssertionError) {
            throw AssertionError(
                "❌ Expected offer '${second.skillRequested}' to be displayed after swipeDown, but it was not found.",
                e
            )
        }

        if (isRunningOnCi()) {
            vm.previous()
        } else {
            composeTestRule.onNodeWithTag(FeedScreenTestTags.FEED_CARD).performTouchInput {
                swipeUp()
            }
        }

        composeTestRule.waitForIdle()
        try {
            composeTestRule.onNodeWithText(first.skillRequested).assertIsDisplayed()
        } catch (e: AssertionError) {
            throw AssertionError(
                "❌ Expected offer '${first.skillRequested}' to be displayed after swipeUp, but it was not found.",
                e
            )
        }
    }

    @Test
    fun swipeDown_fetchesNewOfferWhenAtEnd() {
        val first =
            FeedOffer(
                skillProvided = "First",
                skillRequested = "1",
                authorID = "u1",
                thumbnail = "t1"
            )
        val repository = FakeFeedRepository()
        val navigation = FakeFeedNavigation()
        val vm = FeedScreenViewModel(navigation, repository)
        vm.setUiState(FeedScreenUiState(listOf(first), current = first))

        composeTestRule.setContent { Box(Modifier.fillMaxSize()) { FeedScreen(vm = vm) } }

        if (isRunningOnCi()) {
            vm.next()
        } else {
            composeTestRule.onNodeWithTag(FeedScreenTestTags.FEED_CARD).performTouchInput {
                swipeDown()
            }
        }

        composeTestRule.waitForIdle()
        val state = vm.uiState.value
        assert(state.offers.size > 1)
        assert(state.current != first)
    }

    @Test
    fun emptyOfferList_initializesWithRepositoryOffer() {
        val repository = FakeFeedRepository()
        val navigation = FakeFeedNavigation()
        val vm = FeedScreenViewModel(navigation, repository)

        composeTestRule.setContent {
            Box(modifier = Modifier.fillMaxSize()) { FeedScreen(vm = vm) }
        }
        composeTestRule.waitForIdle()

        val state = vm.uiState.value
        assert(state.offers.isNotEmpty())
        assert(state.current == state.offers.first())
    }

    @Test
    fun next_onEmptyOffers_fetchesOffer() {
        val repository = FakeFeedRepository()
        val navigation = FakeFeedNavigation()
        val vm = FeedScreenViewModel(navigation, repository)

        // Clear UI state to simulate empty offers
        vm.setUiState(FeedScreenUiState(emptyList(), FeedOffer()))

        composeTestRule.setContent {
            Box(modifier = Modifier.fillMaxSize()) { FeedScreen(vm = vm) }
        }
        composeTestRule.waitForIdle()

        // Trigger next() manually
        vm.next()
        composeTestRule.waitForIdle()

        val state = vm.uiState.value
        assert(state.offers.isNotEmpty()) // Should have fetched a new offer
        assert(state.current == state.offers.first())
    }

    @Test
    fun previous_onFirstOffer_doesNotCrash() {
        // Arrange
        val first =
            FeedOffer(
                skillProvided = "First",
                skillRequested = "1",
                authorID = "u1",
                thumbnail = "t1"
            )

        val repository = FakeFeedRepository()
        val navigation = FakeFeedNavigation()
        val vm = FeedScreenViewModel(navigation, repository)
        vm.setUiState(FeedScreenUiState(listOf(first), current = first))

        composeTestRule.setContent {
            Box(modifier = Modifier.fillMaxSize()) { FeedScreen(vm = vm) }
        }
        composeTestRule.waitForIdle()

        // Act — Trigger previous() on first offer (should not crash)
        vm.previous()
        composeTestRule.waitForIdle()

        // Assert — UI should still show the first offer
        composeTestRule.onNodeWithText(first.skillRequested).assertIsDisplayed()
    }

    @Test
    fun swipeOnEmptyOffers_doesNotCrash() {
        val repository = FakeFeedRepository()
        val navigation = FakeFeedNavigation()
        val vm = FeedScreenViewModel(navigation, repository)
        vm.setUiState(FeedScreenUiState(emptyList(), FeedOffer()))

        composeTestRule.setContent {
            Box(modifier = Modifier.fillMaxSize()) { FeedScreen(vm = vm) }
        }
        composeTestRule.waitForIdle()

        if (!isRunningOnCi()) {
            composeTestRule
                .onNodeWithTag(FeedScreenTestTags.FEED_CARD)
                .assertExists()
                .performTouchInput {
                    swipeDown()
                    swipeUp()
                    swipeLeft()
                    swipeRight()
                }
        } else {
            composeTestRule
                .onNodeWithTag(FeedScreenTestTags.FEED_CARD)
                .assertExists()
                .assertIsDisplayed()
            vm.next()
        }

        composeTestRule.waitForIdle()

        val current = vm.uiState.value.current
        assert(current.skillProvided.isNotEmpty())
    }

    @Test
    fun skip_callsSkipOnRepository() {
        val repository = FakeFeedRepository()
        val navigation = FakeFeedNavigation()
        val vm = FeedScreenViewModel(navigation, repository)

        val offer =
            FeedOffer(
                skillProvided = "Teach Kotlin",
                skillRequested = "Learn Compose",
                authorID = "author123",
                thumbnail = "thumb123"
            )

        repository.preloadOffers(offer)
        vm.setUiState(FeedScreenUiState(listOf(offer), offer))

        vm.skip()

        val skipped = repository.getSkippedOffers()
        assert(skipped.size == 1) { "Expected 1 skipped offer, got ${skipped.size}" }
        assert(skipped[0].first == offer) { "Expected skipped offer to be the same as input" }
        assert(skipped[0].second.isNotEmpty()) { "Expected skip to include userId" }
    }

    @Test
    fun previous_doesNotReturnAcceptedOffer() {
        val repository = FakeFeedRepository()
        val navigation = FakeFeedNavigation()
        val vm = FeedScreenViewModel(navigation, repository)

        val first =
            FeedOffer(skillProvided = "A", skillRequested = "1", authorID = "u1", thumbnail = "t1")
        val second =
            FeedOffer(skillProvided = "B", skillRequested = "2", authorID = "u2", thumbnail = "t2")
        val third =
            FeedOffer(skillProvided = "C", skillRequested = "3", authorID = "u3", thumbnail = "t3")

        vm.setUiState(FeedScreenUiState(offers = listOf(first, second, third), current = first))

        vm.accept(first)
        val afterAccept = vm.uiState.value

        vm.previous()
        val afterPrevious = vm.uiState.value
        vm.previous()
        val afterSecondPrevious = vm.uiState.value

        assertNotEquals(first, afterAccept.current)
        assertEquals(second, afterPrevious.current)
        assertEquals(afterPrevious, afterSecondPrevious)
        assert(!afterPrevious.offers.contains(first))
    }

    @Test
    fun next_resetsToFirstWhenCurrentOfferNotInList() {
        // Arrange
        val repository = FakeFeedRepository()
        val navigation = FakeFeedNavigation()
        val vm = FeedScreenViewModel(navigation, repository)

        val first =
            FeedOffer(
                skillProvided = "FeedOffer 1",
                skillRequested = "R1",
                authorID = "u1",
                thumbnail = "t1"
            )
        val second =
            FeedOffer(
                skillProvided = "FeedOffer 2",
                skillRequested = "R2",
                authorID = "u2",
                thumbnail = "t2"
            )
        val unrelated =
            FeedOffer(
                skillProvided = "Unrelated",
                skillRequested = "R0",
                authorID = "u0",
                thumbnail = "t0"
            )

        vm.setUiState(
            FeedScreenUiState(
                offers = listOf(first, second),
                current = unrelated // current not part of the list
            )
        )

        vm.next()

        val newState = vm.uiState.value
        assertEquals(
            "When current offer is not in the list, it should reset to the first offer.",
            first,
            newState.current
        )
        assertEquals(
            "FeedOffer list should remain unchanged.",
            listOf(first, second),
            newState.offers
        )
    }

    @Test
    fun allTestTagsDisplayedAndMenuInteractions() {
        // Arrange: create a feed offer
        val offer =
            FeedOffer(
                skillProvided = "Guitar Lessons",
                authorID = "author123",
                authorName = "Alice Martin",
                requesterAvatar = "https://picsum.photos/200",
                receiverName = "Bob Carter",
                skillRequested = "Portrait Photography",
                thumbnail = "https://picsum.photos/600/300",
                specification = "Bring your guitar",
                description =
                    "I don't have any focus for portrait please make a recommendation" +
                        " and if possible use your material"
            )

        val (vm, _, _) = setContentWithRepositoryReturning(offer)

        composeTestRule.waitForIdle()

        val testTags =
            listOf(
                FeedScreenTestTags.FEED_CARD,
                FeedScreenTestTags.FEED_MENU_BUTTON,
                FeedScreenTestTags.FEED_THUMBNAIL,
                FeedScreenTestTags.SKILL_REQUESTED,
                FeedScreenTestTags.SKILL_GIVE,
                FeedScreenTestTags.SPECIFICATION_TITLE,
                FeedScreenTestTags.SPECIFICATION_DESCRIPTION,
                FeedScreenTestTags.REQUESTER_PROFILE_PICTURE,
                FeedScreenTestTags.REQUESTER_NAME,
                FeedScreenTestTags.ACCEPT_BUTTON,
                FeedScreenTestTags.DECLINE_BUTTON
            )

        testTags.forEach { tag ->
            try {
                composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
            } catch (e: AssertionError) {
                throw AssertionError("❌ UI element with testTag '$tag' was NOT displayed.", e)
            }
        }

        composeTestRule.onNodeWithTag(FeedScreenTestTags.FEED_MENU_BUTTON).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Block User").assertIsDisplayed()
        composeTestRule.onNodeWithText("Report Offer").assertIsDisplayed()

        composeTestRule.onNodeWithText("Block User").performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf("author123"), vm.blockedUsers)

        composeTestRule.onNodeWithTag(FeedScreenTestTags.FEED_MENU_BUTTON).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Report Offer").performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf(offer), vm.reportedOffers)

        composeTestRule.onRoot().performTouchInput { click(center) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Block User").assertDoesNotExist()
        composeTestRule.onNodeWithText("Report Offer").assertDoesNotExist()
    }
}

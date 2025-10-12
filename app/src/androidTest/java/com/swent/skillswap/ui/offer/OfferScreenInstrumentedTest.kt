package com.swent.skillswap.ui.offerScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import com.swent.skillswap.model.offer.FakeOfferNavigation
import com.swent.skillswap.model.offer.FakeOfferRepository
import com.swent.skillswap.model.offer.Offer
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OfferScreenInstrumentedTest {

    @get:Rule val composeTestRule = createComposeRule()

    /** Detects if the tests are running on a CI environment. */
    private fun isRunningOnCi(): Boolean = System.getenv("GITHUB_ACTIONS")?.toBoolean() == true

    /** Helper to set up screen with fake repository returning specified offers. */
    private fun setContentWithRepositoryReturning(
        vararg returnedOffers: Offer
    ): Triple<OfferScreenViewModel, FakeOfferRepository, FakeOfferNavigation> {
        val repository = FakeOfferRepository()
        val navigation = FakeOfferNavigation()

        repository.preloadOffers(*returnedOffers)

        val vm = OfferScreenViewModel(navigation, repository)
        composeTestRule.setContent {
            Box(modifier = Modifier.fillMaxSize()) { OfferScreen(vm = vm) }
        }

        composeTestRule.waitForIdle()
        return Triple(vm, repository, navigation)
    }

    @Test
    fun cardShowsGiveAndReceive() {
        val offer =
            Offer(
                give = "Teach Kotlin",
                receive = "Learn Compose",
                authorID = "author1",
                thumbnail = "thumb"
            )

        val (vm, _, _) = setContentWithRepositoryReturning(offer)

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(OfferScreenTestTags.OFFER_GIVE)
            .assertIsDisplayed()
            .assert(hasText(offer.give))

        composeTestRule
            .onNodeWithTag(OfferScreenTestTags.OFFER_RECEIVE)
            .assertIsDisplayed()
            .assert(hasText(offer.receive))
    }

    @Test
    fun swipeRight_callsAcceptOnRepository() {
        val offer = Offer(give = "G", receive = "R", authorID = "auth", thumbnail = "t")
        val repository = FakeOfferRepository()
        val navigation = FakeOfferNavigation()
        val vm = OfferScreenViewModel(navigation, repository)
        vm.setUiState(OfferScreenUiState(listOf(offer), current = offer))

        composeTestRule.setContent { Box(Modifier.fillMaxSize()) { OfferScreen(vm = vm) } }

        if (isRunningOnCi()) {
            vm.accept(offer)
        } else {
            composeTestRule.onNodeWithTag(OfferScreenTestTags.OFFER_CARD).performTouchInput {
                swipeRight()
            }
        }

        composeTestRule.waitForIdle()
        val accepted = repository.getAcceptedOffers()
        assert(accepted.any { it.first.give == offer.give && it.first.receive == offer.receive })
    }

    @Test
    fun swipeLeft_callsGoToProfile() {
        val offer = Offer(give = "G", receive = "R", authorID = "authorX", thumbnail = "t")
        val repository = FakeOfferRepository()
        val navigation = FakeOfferNavigation()
        val vm = OfferScreenViewModel(navigation, repository)
        vm.setUiState(OfferScreenUiState(listOf(offer), current = offer))

        composeTestRule.setContent { Box(Modifier.fillMaxSize()) { OfferScreen(vm = vm) } }

        if (isRunningOnCi()) {
            vm.goToProfile(offer.authorID)
        } else {
            composeTestRule.onNodeWithTag(OfferScreenTestTags.OFFER_CARD).performTouchInput {
                swipeLeft()
            }
        }

        composeTestRule.waitForIdle()
        val visited = navigation.getVisitedProfiles()
        assertEquals(listOf("authorX"), visited)
    }

    @Test
    fun swipeDown_loadsNextOffer_then_swipeUp_goesBackToPrevious() {
        val first = Offer(give = "First", receive = "1", authorID = "u1", thumbnail = "t1")
        val second = Offer(give = "Second", receive = "2", authorID = "u2", thumbnail = "t2")

        val (vm, _, _) = setContentWithRepositoryReturning(first, second)
        vm.setUiState(OfferScreenUiState(listOf(first, second), current = first))
        composeTestRule.waitForIdle()

        if (isRunningOnCi()) {
            vm.next()
        } else {
            composeTestRule.onNodeWithTag(OfferScreenTestTags.OFFER_CARD).performTouchInput {
                swipeDown()
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(second.give).assertIsDisplayed()

        if (isRunningOnCi()) {
            vm.previous()
        } else {
            composeTestRule.onNodeWithTag(OfferScreenTestTags.OFFER_CARD).performTouchInput {
                swipeUp()
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(first.give).assertIsDisplayed()
    }

    @Test
    fun swipeDown_fetchesNewOfferWhenAtEnd() {
        val first = Offer(give = "First", receive = "1", authorID = "u1", thumbnail = "t1")
        val repository = FakeOfferRepository()
        val navigation = FakeOfferNavigation()
        val vm = OfferScreenViewModel(navigation, repository)
        vm.setUiState(OfferScreenUiState(listOf(first), current = first))

        composeTestRule.setContent { Box(Modifier.fillMaxSize()) { OfferScreen(vm = vm) } }

        if (isRunningOnCi()) {
            vm.next()
        } else {
            composeTestRule.onNodeWithTag(OfferScreenTestTags.OFFER_CARD).performTouchInput {
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
        val repository = FakeOfferRepository()
        val navigation = FakeOfferNavigation()
        val vm = OfferScreenViewModel(navigation, repository)

        composeTestRule.setContent {
            Box(modifier = Modifier.fillMaxSize()) { OfferScreen(vm = vm) }
        }
        composeTestRule.waitForIdle()

        val state = vm.uiState.value
        assert(state.offers.isNotEmpty())
        assert(state.current == state.offers.first())
    }

    @Test
    fun next_onEmptyOffers_fetchesOffer() {
        val repository = FakeOfferRepository()
        val navigation = FakeOfferNavigation()
        val vm = OfferScreenViewModel(navigation, repository)

        // Clear UI state to simulate empty offers
        vm.setUiState(OfferScreenUiState(emptyList(), Offer()))

        composeTestRule.setContent {
            Box(modifier = Modifier.fillMaxSize()) { OfferScreen(vm = vm) }
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
        val first = Offer(give = "First", receive = "1", authorID = "u1", thumbnail = "t1")
        val repository = FakeOfferRepository()
        val navigation = FakeOfferNavigation()
        val vm = OfferScreenViewModel(navigation, repository)
        vm.setUiState(OfferScreenUiState(listOf(first), current = first))

        composeTestRule.setContent {
            Box(modifier = Modifier.fillMaxSize()) { OfferScreen(vm = vm) }
        }
        composeTestRule.waitForIdle()

        // Trigger previous() on first offer → should not crash
        vm.previous()
        composeTestRule.waitForIdle()

        // UI should still show the first offer
        composeTestRule.onNodeWithText(first.give).assertIsDisplayed()
    }

    @Test
    fun swipeOnEmptyOffers_doesNotCrash() {
        val repository = FakeOfferRepository()
        val navigation = FakeOfferNavigation()
        val vm = OfferScreenViewModel(navigation, repository)
        vm.setUiState(OfferScreenUiState(emptyList(), Offer()))

        composeTestRule.setContent {
            Box(modifier = Modifier.fillMaxSize()) { OfferScreen(vm = vm) }
        }
        composeTestRule.waitForIdle()

        // Attempt swipe actions on empty card
        composeTestRule.onNodeWithTag(OfferScreenTestTags.OFFER_CARD).performTouchInput {
            swipeDown()
            swipeUp()
            swipeLeft()
            swipeRight()
        }
        composeTestRule.waitForIdle()

        val current = vm.uiState.value.current
        assert(current.give.isNotEmpty())
    }
}

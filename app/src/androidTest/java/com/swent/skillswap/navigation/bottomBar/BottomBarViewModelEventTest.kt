package com.swent.skillswap.navigation.bottomBar

import com.swent.skillswap.model.navigation.FakeNavigationBottomBar
import com.swent.skillswap.ui.navigation.bottomBar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A JUnit rule to set the Main dispatcher to a TestDispatcher for coroutine tests. This is
 * necessary for testing ViewModels that use viewModelScope.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainCoroutineRule(val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()) :
    TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

/**
 * Unit tests for [BottomBarViewModel] event emission behavior.
 *
 * Ensures that the correct [BottomBarEvent] is emitted when a screen is selected.
 *
 * @author Joey Gugler
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BottomBarViewModelEventTest {

    // This rule replaces Dispatchers.Main (even the Android UI thread)
    // with an UnconfinedTestDispatcher.
    @get:Rule val mainCoroutineRule = MainCoroutineRule()

    // This is NOT a rule. The annotation was removed.
    val fakeNav = FakeNavigationBottomBar()

    @Test
    fun selectingProfile_emitsNavigateToProfileEvent() =
        runTest(mainCoroutineRule.testDispatcher) {
            val vm = BottomBarViewModel()
            var event: BottomBarEvent? = null

            // 1. Launch a collector.
            val job = launch { event = vm.eventFlow.first() }

            // 2. Trigger the action.
            vm.onScreenSelected(BottomBarScreen.PROFILE)

            // 3. Assert the event was received.
            //    (This works because the UnconfinedTestDispatcher ran both
            //    the collector and the emitter eagerly)
            assertTrue(event is BottomBarEvent.NavigateToProfile)

            // 4. Clean up the background job
            job.cancel()
        }

    @Test
    fun selectingOffer_emitsNavigateToOfferEvent() =
        runTest(mainCoroutineRule.testDispatcher) {
            val vm = BottomBarViewModel()
            var event: BottomBarEvent? = null

            val job = launch { event = vm.eventFlow.first() }

            vm.onScreenSelected(BottomBarScreen.OFFER)

            assertTrue(event is BottomBarEvent.NavigateToOffer)
            job.cancel()
        }

    @Test
    fun selectingChat_emitsNavigateToChatEvent() =
        runTest(mainCoroutineRule.testDispatcher) {
            val vm = BottomBarViewModel()
            var event: BottomBarEvent? = null

            val job = launch { event = vm.eventFlow.first() }

            vm.onScreenSelected(BottomBarScreen.CHAT)

            assertTrue(event is BottomBarEvent.NavigateToChat)
            job.cancel()
        }
}

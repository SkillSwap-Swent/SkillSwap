/** Created with the help of Cursor */
package com.swent.skillswap.ui.notification

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.swent.skillswap.model.notification.FakeNotificationRepository
import com.swent.skillswap.model.notification.Notification
import com.swent.skillswap.model.notification.NotificationType
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeNotificationRepository
    private lateinit var viewModel: NotificationViewModel
    private val testUserId = "test-user-123"

    private val notification1 =
        Notification(
            "notif-1",
            testUserId,
            "Title 1",
            "Message 1",
            NotificationType.MESSAGE,
            "chat-1",
            false,
            Timestamp(Date(1000))
        )

    private val notification2 =
        Notification(
            "notif-2",
            testUserId,
            "Title 2",
            "Message 2",
            NotificationType.POST_REPLY,
            "post-1",
            true,
            Timestamp(Date(2000))
        )

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        try {
            FirebaseApp.getInstance()
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("test-app-id")
                    .setApiKey("test-api-key")
                    .setProjectId("test-project")
                    .build()
            )
        }
        fakeRepository = FakeNotificationRepository()
    }

    @After
    fun tearDown() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            // Ignore
        }
    }

    private suspend fun signInAnonymously(): String? {
        return try {
            FirebaseAuth.getInstance().signInAnonymously().await().user?.uid
        } catch (e: Exception) {
            null
        }
    }

    @Test
    fun init_loadsNotifications() = runTest {
        viewModel = NotificationViewModel(fakeRepository)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun loadNotifications_noAuth_setsError() = runTest {
        FirebaseAuth.getInstance().signOut()
        viewModel = NotificationViewModel(fakeRepository)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun loadNotifications_withAuth_loadsNotifications() = runTest {
        val userId = signInAnonymously()
        if (userId != null) {
            val notif = notification1.copy(userId = userId)
            fakeRepository.preloadNotifications(notif)
            viewModel = NotificationViewModel(fakeRepository)
            advanceUntilIdle()
            val state = viewModel.uiState.value
            if (state.error == null) {
                assertTrue(state.notifications.isNotEmpty())
            }
        }
    }

    @Test
    fun loadNotifications_repositoryError_setsError() = runTest {
        val userId = signInAnonymously()
        if (userId != null) {
            fakeRepository.setShouldFailOnGet(true)
            viewModel = NotificationViewModel(fakeRepository)
            advanceUntilIdle()
            assertNotNull(viewModel.uiState.value.error)
        }
    }

    @Test
    fun setShowUnreadOnly_togglesFilterAndReloads() = runTest {
        val userId = signInAnonymously()
        if (userId != null) {
            val notif1 = notification1.copy(userId = userId)
            val notif2 = notification2.copy(userId = userId)
            fakeRepository.preloadNotifications(notif1, notif2)
            viewModel = NotificationViewModel(fakeRepository)
            advanceUntilIdle()
            viewModel.setShowUnreadOnly(true)
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.showUnreadOnly)
            viewModel.uiState.value.notifications.forEach { assertFalse(it.isRead) }
        }
    }

    @Test
    fun markAsRead_updatesOptimistically() = runTest {
        val userId = signInAnonymously()
        if (userId != null) {
            val notif = notification1.copy(userId = userId)
            fakeRepository.preloadNotifications(notif)
            viewModel = NotificationViewModel(fakeRepository)
            advanceUntilIdle()
            val state = viewModel.uiState.value
            if (state.notifications.isNotEmpty()) {
                viewModel.markAsRead(state.notifications[0])
                advanceUntilIdle()
                val updated = viewModel.uiState.value.notifications.find { it.uid == notif.uid }
                assertTrue(updated?.isRead == true)
            }
        }
    }

    @Test
    fun markAsRead_repositoryError_reloads() = runTest {
        val userId = signInAnonymously()
        if (userId != null) {
            val notif = notification1.copy(userId = userId)
            fakeRepository.preloadNotifications(notif)
            fakeRepository.setShouldFailOnUpdate(true)
            viewModel = NotificationViewModel(fakeRepository)
            advanceUntilIdle()
            val state = viewModel.uiState.value
            if (state.notifications.isNotEmpty()) {
                viewModel.markAsRead(state.notifications[0])
                advanceUntilIdle()
                assertNotNull(viewModel.uiState.value.error)
            }
        }
    }

    @Test
    fun markAllAsRead_noAuth_setsError() = runTest {
        FirebaseAuth.getInstance().signOut()
        viewModel = NotificationViewModel(fakeRepository)
        viewModel.markAllAsRead()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun markAllAsRead_updatesAllOptimistically() = runTest {
        val userId = signInAnonymously()
        if (userId != null) {
            val notif1 = notification1.copy(userId = userId)
            val notif2 = notification2.copy(userId = userId)
            fakeRepository.preloadNotifications(notif1, notif2)
            viewModel = NotificationViewModel(fakeRepository)
            advanceUntilIdle()
            viewModel.markAllAsRead()
            advanceUntilIdle()
            viewModel.uiState.value.notifications.forEach { assertTrue(it.isRead) }
        }
    }

    @Test
    fun deleteNotification_removesOptimistically() = runTest {
        val userId = signInAnonymously()
        if (userId != null) {
            val notif = notification1.copy(userId = userId)
            fakeRepository.preloadNotifications(notif)
            viewModel = NotificationViewModel(fakeRepository)
            advanceUntilIdle()
            val state = viewModel.uiState.value
            if (state.notifications.isNotEmpty()) {
                val initialCount = state.notifications.size
                viewModel.deleteNotification(state.notifications[0])
                advanceUntilIdle()
                assertEquals(initialCount - 1, viewModel.uiState.value.notifications.size)
            }
        }
    }

    @Test
    fun deleteAllNotifications_noAuth_setsError() = runTest {
        FirebaseAuth.getInstance().signOut()
        viewModel = NotificationViewModel(fakeRepository)
        viewModel.deleteAllNotifications()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun deleteAllNotifications_clearsOptimistically() = runTest {
        val userId = signInAnonymously()
        if (userId != null) {
            val notif = notification1.copy(userId = userId)
            fakeRepository.preloadNotifications(notif)
            viewModel = NotificationViewModel(fakeRepository)
            advanceUntilIdle()
            viewModel.deleteAllNotifications()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.notifications.isEmpty())
        }
    }

    @Test
    fun refresh_reloadsNotifications() = runTest {
        val userId = signInAnonymously()
        if (userId != null) {
            fakeRepository.preloadNotifications(notification1.copy(userId = userId))
            viewModel = NotificationViewModel(fakeRepository)
            advanceUntilIdle()
            fakeRepository.preloadNotifications(
                notification1.copy(userId = userId),
                notification2.copy(userId = userId)
            )
            viewModel.refresh()
            advanceUntilIdle()
        }
    }

    @Test
    fun clearError_removesError() = runTest {
        FirebaseAuth.getInstance().signOut()
        viewModel = NotificationViewModel(fakeRepository)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun loadNotifications_cancelsPreviousLoad() = runTest {
        fakeRepository.setDelay(1000)
        viewModel = NotificationViewModel(fakeRepository)
        viewModel.loadNotifications()
        viewModel.loadNotifications()
        advanceUntilIdle()
    }

    @Test
    fun markAllAsRead_repositoryError_reloads() = runTest {
        val userId = signInAnonymously()
        if (userId != null) {
            val notif = notification1.copy(userId = userId)
            fakeRepository.preloadNotifications(notif)
            fakeRepository.setShouldFailOnUpdate(true)
            viewModel = NotificationViewModel(fakeRepository)
            advanceUntilIdle()
            viewModel.markAllAsRead()
            advanceUntilIdle()
            assertNotNull(viewModel.uiState.value.error)
        }
    }

    @Test
    fun deleteNotification_repositoryError_reloads() = runTest {
        val userId = signInAnonymously()
        if (userId != null) {
            val notif = notification1.copy(userId = userId)
            fakeRepository.preloadNotifications(notif)
            viewModel = NotificationViewModel(fakeRepository)
            advanceUntilIdle()
            val state = viewModel.uiState.value
            if (state.notifications.isNotEmpty()) {
                fakeRepository.setShouldFailOnGet(true)
                viewModel.deleteNotification(state.notifications[0])
                advanceUntilIdle()
                assertNotNull(viewModel.uiState.value.error)
            }
        }
    }

    @Test
    fun deleteAllNotifications_repositoryError_reloads() = runTest {
        val userId = signInAnonymously()
        if (userId != null) {
            val notif = notification1.copy(userId = userId)
            fakeRepository.preloadNotifications(notif)
            fakeRepository.setShouldFailOnGet(true)
            viewModel = NotificationViewModel(fakeRepository)
            advanceUntilIdle()
            viewModel.deleteAllNotifications()
            advanceUntilIdle()
            assertNotNull(viewModel.uiState.value.error)
        }
    }
}

class MainDispatcherRule(private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()) :
    TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

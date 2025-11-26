/** Created with the help of Cursor */
package com.swent.skillswap.ui.notification

import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.swent.skillswap.model.notification.FakeNotificationRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationViewModelFactoryTest {

    private lateinit var fakeRepository: FakeNotificationRepository
    private lateinit var factory: NotificationViewModelFactory

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
        factory = NotificationViewModelFactory(fakeRepository)
    }

    @Test
    fun create_withNotificationViewModelClass_returnsViewModel() {
        val viewModel = factory.create(NotificationViewModel::class.java)
        assertNotNull(viewModel)
    }

    @Test
    fun create_withUnknownViewModelClass_throwsException() {
        try {
            factory.create(UnknownViewModel::class.java)
            fail("Should throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Unknown ViewModel class", e.message)
        }
    }

    private class UnknownViewModel : ViewModel()
}

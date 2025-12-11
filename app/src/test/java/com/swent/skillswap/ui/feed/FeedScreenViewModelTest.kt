package com.swent.skillswap.ui.feed

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.swent.skillswap.model.feed.FeedController
import com.swent.skillswap.model.feed.FeedPost
import com.swent.skillswap.model.feed.Image
import com.swent.skillswap.model.notification.FakeNotificationRepository
import com.swent.skillswap.model.notification.NotificationType
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.SkillRank
import com.swent.skillswap.ui.notification.NotificationViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(ExperimentalCoroutinesApi::class)
class FeedScreenViewModelTest {

    private lateinit var viewModel: FeedScreenViewModel
    private lateinit var viewModelWithNotifications: FeedScreenViewModel
    private lateinit var fakeNotificationRepository: FakeNotificationRepository
    private lateinit var notificationViewModel: NotificationViewModel
    private val testPost =
        FeedPost(
            offerId = "post-1",
            skillProvided = "Guitar",
            authorID = "author-1",
            authorName = "John",
            requesterAvatar = "",
            receiverName = "user-1",
            skillRequested = "Piano",
            thumbnail = "",
            specification = "Learn Guitar",
            authorRating = 4.5f,
            description = "I want to learn guitar"
        )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        // Initialize FirebaseApp to prevent IllegalStateException when accessing Firebase.auth
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

        // Mock Firebase Auth to return a user so NotificationViewModel can work
        mockkStatic(FirebaseAuth::class)
        val mockAuth = mockk<FirebaseAuth>(relaxed = true)
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { mockUser.uid } returns "user-1"
        every { mockAuth.currentUser } returns mockUser
        every { FirebaseAuth.getInstance() } returns mockAuth

        fakeNotificationRepository = FakeNotificationRepository()
        notificationViewModel = NotificationViewModel(fakeNotificationRepository)

        val mockController =
            object : FeedController {
                override val currentPost: State<com.swent.skillswap.model.post.Post?> =
                    mutableStateOf<com.swent.skillswap.model.post.Post?>(null)
                override val currentThumbnail: State<Image?> = mutableStateOf<Image?>(null)
                override val userIdPerformingActions = "user-1"
                override val feedType = com.swent.skillswap.model.post.PostType.REQUEST

                override suspend fun acceptPost(message: String) {
                    // Mock implementation
                }

                override suspend fun skipPost() {
                    // Mock implementation
                }

                override suspend fun getThumbnail(thumbnailId: String) {
                    // Mock implementation
                }

                override suspend fun reportPost(
                    postId: String,
                    postType: com.swent.skillswap.model.post.PostType
                ) {
                    // Mock implementation
                }

                override suspend fun updateDistanceFilter(distance: Float) {
                    // Mock implementation
                }

                override suspend fun updateLocation(isLiveLocationOn: Boolean) {
                    // Mock implementation
                }

                override suspend fun inferRelevantSkill() =
                    Skill(SkillTag.COMPUTER_PROGRAMMING, SkillRank.CAPABLE.value, "Guitar")

                override suspend fun blockUser(blockedUserUID: String) {
                    // Mock implementation
                }

                override suspend fun retrieveUser(post: com.swent.skillswap.model.post.Post) =
                    com.swent.skillswap.model.user.User(
                        "author-1",
                        "John",
                        "",
                        "",
                        emptySet(),
                        4.5f,
                        emptyList()
                    )
            }

        val mockNavigation = FeedScreenNavigation { userId -> }

        viewModel = FeedScreenViewModel(mockNavigation, mockController)
        viewModelWithNotifications =
            FeedScreenViewModel(mockNavigation, mockController, notificationViewModel)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(FirebaseAuth::class)
    }

    @Test
    fun accept_withNotificationViewModel_createsPostReplyNotification() = runTest {
        viewModelWithNotifications.accept(testPost)
        advanceUntilIdle()
        val notifications = fakeNotificationRepository.getNotificationsForUser("author-1")
        assertTrue(
            "Should create POST_REPLY notification",
            notifications.any {
                it.type == NotificationType.POST_REPLY &&
                    it.relatedId == "post-1" &&
                    it.userId == "author-1" &&
                    it.message.contains("Learn Guitar")
            }
        )
    }

    @Test
    fun accept_withoutNotificationViewModel_doesNotCrash() = runTest {
        viewModel.accept(testPost)
        advanceUntilIdle()
    }

    @Test
    fun markPostNotificationsAsRead_withNotificationViewModel_callsViewModel() = runTest {
        val notification =
            com.swent.skillswap.model.notification.Notification(
                uid = "notif-1",
                userId = "user-1",
                title = "Post Reply",
                message = "Someone replied",
                type = NotificationType.POST_REPLY,
                relatedId = "post-1",
                isRead = false
            )
        fakeNotificationRepository.addNotification(notification)
        viewModelWithNotifications.markPostNotificationsAsRead("post-1")
        advanceUntilIdle()
        assertTrue(
            "Notification should be marked as read",
            fakeNotificationRepository.getNotification("notif-1").isRead
        )
    }

    @Test
    fun markPostNotificationsAsRead_withoutNotificationViewModel_doesNotCrash() = runTest {
        viewModel.markPostNotificationsAsRead("post-1")
        advanceUntilIdle()
    }
}

package com.swent.skillswap.ui.feed

import android.util.Log
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.swent.skillswap.model.feed.FeedController
import com.swent.skillswap.model.feed.FeedPost
import com.swent.skillswap.model.notification.NotificationType
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.ui.notification.NotificationViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents events (usually blocking or reporting other user/offer) that occur during
 * FeedScreenFlow
 */
sealed class FeedScreenEvent() {

    /** Event indicating that the user has successfully block a user */
    data class SuccessFullBlock(val authorName: String) : FeedScreenEvent()

    /** Event indicating that the user has successfully report a post */
    data class SuccessFullReport(val authorName: String) : FeedScreenEvent()
    /** represent event attach to an exception* */
    sealed class ExceptionEvent(val exception: Throwable) : FeedScreenEvent() {
        /** Event indicating that the user has got an error while trying to report a post */
        class ErrorOnReport(exception: Throwable) : ExceptionEvent(exception)

        /** Event indicating that the user has got an error while trying to block a user */
        class ErrorOnBlock(exception: Throwable) : ExceptionEvent(exception)
    }
}
/**
 * ViewModel responsible for managing offer data, navigation, and UI state for the FeedOffer screen.
 *
 * It loads, updates, and transitions between offers, and handles user actions such as swiping or
 * navigating to another user’s profile.
 *
 * @property navigation The navigation handler for transitioning to other screens.
 * @property controller The data source used to retrieve and update offers.
 * @author Joey Gugler using chatGPT
 */
open class FeedScreenViewModel(
    private val navigation: FeedScreenNavigation,
    private val controller: FeedController,
    private val notificationViewModel: NotificationViewModel? = null
) : ViewModel() {

    /**
     * The unique identifier of the current user (temporary fallback when Firebase is unavailable).
     */
    private val uid: String = Firebase.auth.currentUser?.uid ?: "AnoUser"
    /** Internal state of the FeedOffer screen. */
    private val _uiState = MutableStateFlow<FeedPost?>(null)
    /** internal event handler* */
    private val _eventFlow = MutableSharedFlow<FeedScreenEvent>()
    /** outside event notifier* */
    val eventFlow: SharedFlow<FeedScreenEvent> = _eventFlow
    /** Publicly exposed, read-only state of the FeedOffer screen. */
    open val uiState: StateFlow<FeedPost?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                    controller.currentPost.value?.let { post ->
                        _uiState.value = toFeedPost(post, uid)
                    }
                }
                .onFailure { e -> Log.e("FeedScreenViewModel", "Error loading initial post", e) }
            snapshotFlow { controller.currentPost.value }
                .collect { post ->
                    if (post == null) {
                        _uiState.value = null
                    } else {
                        runCatching { _uiState.value = toFeedPost(post, uid) }
                            .onFailure { e ->
                                Log.e("FeedScreenViewModel", "Error updating post", e)
                            }
                    }
                }
        }
    }

    /** Accepts the specified offer on behalf of the current user. */
    fun accept(post: FeedPost) {
        viewModelScope.launch {
            controller.acceptPost("I'm interested in this offer")

            // Create POST_REPLY notification for the post owner
            notificationViewModel?.let { vm ->
                try {
                    vm.addNotification(
                        recipientId = post.authorID,
                        message = "Someone is interested in your post: ${post.specification}",
                        type = NotificationType.POST_REPLY,
                        relatedId = post.offerId
                    )
                } catch (e: Exception) {
                    Log.e("FeedScreenViewModel", "Error creating POST_REPLY notification", e)
                }
            }
        }
    } // TODO: A Pop-up Window with a textField or preFab message to send is a good idea

    /** Declines the specified offer, removes it from the feed, and loads the next one. */
    fun decline(post: FeedPost) {
        skip()
        // TODO: Placeholder for rollback implementation
    }

    /** Navigates to the profile screen of the specified user. */
    fun goToProfile(userId: String) {
        navigation.goToProfileView(userId)
    }

    /** Skips the current offer and moves to the next one. */
    fun skip() {
        viewModelScope.launch { controller.skipPost() }
    }
    /** Temporarily blocks a user by adding their ID to an in-memory list. */
    fun blockUser(userId: String) {
        viewModelScope.launch {
            try {
                val userName: String = _uiState.value?.authorName ?: ""
                controller.blockUser(userId)
                _eventFlow.emit(FeedScreenEvent.SuccessFullBlock(userName))
            } catch (e: Exception) {
                _eventFlow.emit(FeedScreenEvent.ExceptionEvent.ErrorOnBlock(e))
                Log.e("BlockUserError", "failed to block the user. Cause: ", e)
            }
        }
    }

    /** Report an offer and then decline it if reporting worked. */
    // TODO naming logic of function will need to be adjust the Request/Offer mess start to be hard
    // to follow
    fun reportOffer(offer: FeedPost) {

        viewModelScope.launch {
            try {
                val userName: String = _uiState.value?.authorName ?: ""
                controller.reportPost(offer.offerId, PostType.REQUEST)
                decline(offer)
                _eventFlow.emit(FeedScreenEvent.SuccessFullReport(userName))
            } catch (e: Exception) {
                _eventFlow.emit(FeedScreenEvent.ExceptionEvent.ErrorOnReport(e))
                Log.e("ReportPostError", "failed to report the post cause: ", e)
            }
        }
    }
    /* Sets the maxDistance value for the location filtering */
    fun updateDistanceFilter(distance: Float) {
        viewModelScope.launch { controller.updateDistanceFilter(distance) }
    }

    fun toggleLiveLocation(isLiveLocationOn: Boolean) {
        viewModelScope.launch { controller.updateLocation(isLiveLocationOn) }
    }

    /** Marks post notifications as read when viewing a post */
    fun markPostNotificationsAsRead(postId: String) {
        notificationViewModel?.markPostNotificationsAsRead(postId)
    }

    /**
     * Converts a [Post] object into a [FeedPost] for display on the feed.
     *
     * @param post The post to convert.
     * @param userId The ID of the current user.
     * @return A corresponding [FeedPost] object.
     */
    private suspend fun toFeedPost(post: Post, userId: String): FeedPost {
        when (post.type) {
            PostType.REQUEST -> {
                val skillProvided =
                    try {
                        controller.inferRelevantSkill().name.toUIString()
                    } catch (e: Exception) {
                        "None"
                    }
                val user =
                    try {
                        controller.retrieveUser(post)
                    } catch (e: Exception) {
                        null
                    }
                return FeedPost(
                    offerId = post.uid,
                    skillProvided = skillProvided,
                    authorID = post.ownerId,
                    authorName = user?.username ?: "None",
                    requesterAvatar = user?.profilePicture ?: "",
                    receiverName = userId,
                    skillRequested = post.skills.firstOrNull()?.toUIString() ?: "None",
                    thumbnail = post.media.firstOrNull() ?: "",
                    specification = post.title,
                    authorRating = user?.rating ?: 0f,
                    description = post.description
                )
            }
            PostType.OFFER -> TODO()
        }
    }
}
/**
 * Factory for creating [FeedScreenViewModel] with parameters.
 *
 * @param navigation The navigation handler for the feed screen.
 * @param controller The controller handling feed data.
 */
class FeedScreenViewModelFactory(
    private val navigation: FeedScreenNavigation,
    private val controller: FeedController,
    private val notificationViewModel: NotificationViewModel? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeedScreenViewModel::class.java)) {
            return FeedScreenViewModel(navigation, controller, notificationViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

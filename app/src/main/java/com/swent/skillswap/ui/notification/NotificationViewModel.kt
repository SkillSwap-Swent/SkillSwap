/** Created with the help of Cursor */
package com.swent.skillswap.ui.notification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.swent.skillswap.model.notification.Notification
import com.swent.skillswap.model.notification.NotificationRepository
import com.swent.skillswap.model.notification.NotificationRepositoryFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showUnreadOnly: Boolean = false
)

class NotificationViewModel(
    private val notificationRepository: NotificationRepository =
        NotificationRepositoryFirestore(FirebaseFirestore.getInstance())
) : ViewModel() {

    private val TAG = "NotificationViewModel"
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            _uiState.update {
                it.copy(isLoading = false, error = "No authenticated user found. Please log in.")
            }
            Log.w(TAG, "No authenticated user found")
            return
        }

        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null) }

        loadJob =
            viewModelScope.launch {
                try {
                    val userId = currentUser.uid
                    val showUnreadOnly = _uiState.value.showUnreadOnly

                    val notifications =
                        if (showUnreadOnly) {
                            notificationRepository.getUnreadNotificationsForUser(userId)
                        } else {
                            notificationRepository.getNotificationsForUser(userId)
                        }

                    _uiState.update {
                        it.copy(notifications = notifications, isLoading = false, error = null)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading notifications", e)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load notifications: ${e.message}"
                        )
                    }
                }
            }
    }

    fun setShowUnreadOnly(showUnreadOnly: Boolean) {
        _uiState.update { it.copy(showUnreadOnly = showUnreadOnly) }
        loadNotifications()
    }

    fun markAsRead(notification: Notification) {
        _uiState.update {
            it.copy(
                notifications =
                    it.notifications.map { n ->
                        if (n.uid == notification.uid) n.copy(isRead = true) else n
                    }
            )
        }

        viewModelScope.launch {
            try {
                notificationRepository.markAsRead(notification.uid)
            } catch (e: Exception) {
                Log.e(TAG, "Error marking notification as read", e)
                loadNotifications()
                _uiState.update {
                    it.copy(error = "Failed to mark notification as read: ${e.message}")
                }
            }
        }
    }

    fun markAllAsRead() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            _uiState.update { it.copy(error = "No authenticated user found. Please log in.") }
            return
        }

        _uiState.update { it.copy(notifications = it.notifications.map { it.copy(isRead = true) }) }

        viewModelScope.launch {
            try {
                notificationRepository.markAllAsRead(currentUser.uid)
            } catch (e: Exception) {
                Log.e(TAG, "Error marking all notifications as read", e)
                loadNotifications()
                _uiState.update {
                    it.copy(error = "Failed to mark all notifications as read: ${e.message}")
                }
            }
        }
    }

    fun markChatNotificationsAsRead(chatId: String) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "Cannot mark chat notifications as read: user not authenticated")
            return
        }

        // Find all unread notifications related to this chat
        val chatNotifications =
            _uiState.value.notifications.filter { it.relatedId == chatId && !it.isRead }

        if (chatNotifications.isEmpty()) {
            return
        }

        // Optimistically update UI
        _uiState.update {
            it.copy(
                notifications =
                    it.notifications.map { n ->
                        if (n.relatedId == chatId && !n.isRead) n.copy(isRead = true) else n
                    }
            )
        }

        // Mark each notification as read in repository
        viewModelScope.launch {
            try {
                chatNotifications.forEach { notification ->
                    notificationRepository.markAsRead(notification.uid)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error marking chat notifications as read", e)
                // Reload to sync with actual state
                loadNotifications()
            }
        }
    }

    fun deleteNotification(notification: Notification) {
        _uiState.update {
            it.copy(notifications = it.notifications.filterNot { n -> n.uid == notification.uid })
        }

        viewModelScope.launch {
            try {
                notificationRepository.deleteNotification(notification.uid)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting notification", e)
                loadNotifications()
                _uiState.update { it.copy(error = "Failed to delete notification: ${e.message}") }
            }
        }
    }

    fun deleteAllNotifications() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            _uiState.update { it.copy(error = "No authenticated user found. Please log in.") }
            return
        }

        _uiState.update { it.copy(notifications = emptyList()) }

        viewModelScope.launch {
            try {
                notificationRepository.deleteAllNotificationsForUser(currentUser.uid)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting all notifications", e)
                loadNotifications()
                _uiState.update {
                    it.copy(error = "Failed to delete all notifications: ${e.message}")
                }
            }
        }
    }

    fun refresh() {
        loadNotifications()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

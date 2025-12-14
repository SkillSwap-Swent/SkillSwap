package com.swent.skillswap.ui.user.unblockUser

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.model.user.UserRepositery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UnblockCardView(val name: String, val avatarUrl: String, val uid: String)
/**
 * ViewModel responsible for managing the blocked users list and unblocking functionality.
 *
 * This ViewModel fetches the current user's blocked users from [UserRepositery] and exposes them as
 * a [StateFlow] of [UnblockCardView] for UI consumption. It also handles unblocking a user when
 * triggered by UI events.
 *
 * @property userRepo Repository used to fetch and update user data. Defaults to [UserRepoFirestore]
 *   with Firebase Firestore instance.
 * @author Joey Gugler using chatGPT
 */
class UnblockUserViewModel(
    private val userRepo: UserRepositery = UserRepoFirestore(FirebaseFirestore.getInstance())
) : ViewModel() {

    private val errorTag = "UnBlockUserVM"
    private val uid: String = Firebase.auth.uid!!

    private lateinit var user: User

    private val _uiState = MutableStateFlow<List<UnblockCardView>>(emptyList())
    val uiState: StateFlow<List<UnblockCardView>> = _uiState

    init {
        loadBlockedUsers()
    }
    /**
     * Loads the blocked users for the current user from the repository.
     *
     * Each blocked user is converted into an [UnblockCardView]. If a blocked user cannot be found
     * in the repository, it is skipped and a log is emitted.
     */
    private fun loadBlockedUsers() {
        viewModelScope.launch {
            runCatching {
                    user = userRepo.getUser(uid)
                    val views =
                        user.blockedUsers.mapNotNull { blockedUid ->
                            runCatching {
                                    val blockedUser = userRepo.getUser(blockedUid)
                                    UnblockCardView(
                                        name = blockedUser.username,
                                        avatarUrl = blockedUser.profilePicture,
                                        uid = blockedUid
                                    )
                                }
                                .onFailure {
                                    Log.e(
                                        errorTag,
                                        "Blocked user $blockedUid not found. Skipping.",
                                        it
                                    )
                                }
                                .getOrNull()
                        }

                    _uiState.value = views
                }
                .onFailure { e -> Log.e(errorTag, "Error loading blocked users", e) }
        }
    }

    /**
     * Triggered by a UI event (e.g., clicking the unblock button) to remove a user from the blocked
     * list.
     *
     * @param targetUid UID of the user to unblock.
     */
    fun onUnblockUserClicked(targetUid: String) {
        viewModelScope.launch { unBlockUser(targetUid) }
    }
    /**
     * Removes a user from the current user's blocked list and updates the repository.
     *
     * Updates the local cache ([user]) and [_uiState] to reflect the change in the UI.
     *
     * @param userID UID of the user to unblock.
     */
    private suspend fun unBlockUser(userID: String) {
        runCatching {
                val newBlockedList = user.blockedUsers.filter { it != userID }.toSet()
                userRepo.editUser(uid, user.copy(blockedUsers = newBlockedList))
                user = user.copy(blockedUsers = newBlockedList)
                _uiState.value = _uiState.value.filter { it.uid != userID }
            }
            .onFailure { e -> Log.e(errorTag, "Error unblocking user", e) }
    }
}

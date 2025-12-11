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

data class UnblockCardView(
    val name: String,
    val avatarUrl: String,
    val uid: String
)

class UnblockUserViewModel(
    private val userRepo: UserRepositery = UserRepoFirestore(FirebaseFirestore.getInstance())
) : ViewModel() {

    private val errorTag = "UnBlockUserVM"
    private val uid: String = Firebase.auth.uid!!

    private lateinit var user: User

    private val _unblockCardViews = MutableStateFlow<List<UnblockCardView>>(emptyList())
    val unblockCardViews: StateFlow<List<UnblockCardView>> = _unblockCardViews

    init {
        loadBlockedUsers()
    }

    private fun loadBlockedUsers() {
        viewModelScope.launch {
            runCatching {
                user = userRepo.getUser(uid)
                val views = user.blockedUsers.mapNotNull { blockedUid ->
                    runCatching {
                        val blockedUser = userRepo.getUser(blockedUid)
                        UnblockCardView(
                            name = blockedUser.username,
                            avatarUrl = blockedUser.profilePicture,
                            uid = blockedUid
                        )
                    }.onFailure {
                        Log.e(errorTag, "Blocked user $blockedUid not found. Skipping.", it)
                    }.getOrNull()
                }

                _unblockCardViews.value = views
            }.onFailure { e ->
                Log.e(errorTag, "Error loading blocked users", e)
            }
        }
    }

    /** Called by UI event (button click in each card) */
    fun onUnblockUserClicked(targetUid: String) {
        viewModelScope.launch {
            unBlockUser(targetUid)
        }
    }

    private suspend fun unBlockUser(userID: String) {
        runCatching {
            val newBlockedList = user.blockedUsers.filter { it != userID }.toSet()
            userRepo.editUser(uid, user.copy(blockedUsers = newBlockedList))
            user = user.copy(blockedUsers = newBlockedList)
            _unblockCardViews.value =
                _unblockCardViews.value.filter { it.uid != userID }
        }.onFailure { e ->
            Log.e(errorTag, "Error unblocking user", e)
        }
    }
}

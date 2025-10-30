package com.swent.skillswap.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.skillswap.model.user.Availability
import com.swent.skillswap.model.user.Preference
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repo: UserRepoFirestore = UserRepoFirestore(FirebaseFirestore.getInstance())
) : ViewModel() {
    private val _userState = MutableStateFlow<User>(User())
    val userState: StateFlow<User> = _userState

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val uid =
            FirebaseAuth.getInstance().currentUser?.uid ?: throw Exception("No user with this uid")
        viewModelScope.launch {
            try {
                val user = repo.getUser(uid)
                _userState.value = user
            } catch (e: Exception) {
                Log.e("UserRepoFirestore", "Error while getting user in getUser", e)
                throw Exception("Error while getting user in getUser")
            }
        }
    }

    fun updateUser(user: User) {
        val uid = user.uid.ifEmpty { FirebaseAuth.getInstance().currentUser?.uid }
        if (uid == null) {
            Log.e("ProfileViewModel", "No UID available to update user")
            return
        }

        viewModelScope.launch {
            try {
                repo.editUser(uid, user.copy(uid = uid))
                _userState.value = user.copy(uid = uid)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to update user", e)
            }
        }
    }

    fun updateUserAttributes(
        username: String? = null,
        email: String? = null,
        profilePicture: String? = null,
        skillSet: Set<Skill>? = null,
        rating: Float? = null,
        availability: List<Availability>? = null,
        preference: Preference? = null
    ) {
        val current = _userState.value
        val uid = current.uid.ifEmpty { FirebaseAuth.getInstance().currentUser?.uid }
        if (uid == null) {
            Log.e("ProfileViewModel", "No UID available to update attributes")
            return
        }

        val updated =
            current.copy(
                uid = uid,
                username = username ?: current.username,
                email = email ?: current.email,
                profilePicture = profilePicture ?: current.profilePicture,
                skillSet = skillSet ?: current.skillSet,
                rating = rating ?: current.rating,
                availability = availability ?: current.availability,
                preference = preference ?: current.preference
            )

        viewModelScope.launch {
            repo.editUser(uid, updated)
            _userState.value = updated
        }
    }
}

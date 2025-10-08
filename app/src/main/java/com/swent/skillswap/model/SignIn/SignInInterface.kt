package com.swent.skillswap.model.SignIn

import android.app.Activity
import androidx.credentials.CredentialManager
import com.swent.skillswap.model.tags.SkillTag

interface SignInInterface {
    suspend fun signIn(
        email: String = "",
        password: String = "",
        credentialManager: CredentialManager,
        activity: Activity
    ) = Unit

    suspend fun createAccount(
        username: String,
        email: String,
        skills: List<SkillTag>,
        password: String = ""
    ) = Unit

    suspend fun logOut() = Unit
}

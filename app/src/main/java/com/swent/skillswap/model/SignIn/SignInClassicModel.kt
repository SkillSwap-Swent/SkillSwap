package com.swent.skillswap.model.SignIn

import android.app.Activity
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.swent.skillswap.model.tags.SkillTag
import kotlinx.coroutines.tasks.await

class SignInClassicModel : SignInAbstractClass() {

    override suspend fun signIn(
        email: String,
        password: String,
        credentialManager: CredentialManager,
        activity: Activity
    ) {
        require(email.isNotBlank() && password.isNotBlank()) { "Email and password required." }
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun createAccount(
        username: String,
        email: String,
        skills: List<SkillTag>,
        password: String
    ) {
        require(email.isNotBlank() && password.isNotBlank())

        val auth = FirebaseAuth.getInstance()
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("User not created")
        /*TODO Wait for UserUtils*/
    }
}

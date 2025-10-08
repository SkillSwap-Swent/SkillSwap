package com.swent.skillswap.model.SignIn

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.swent.skillswap.model.tags.SkillTag

class SignInGoogleModel : SignInAbstractClass() {
    suspend fun requestGoogleIdToken(
        credentialManager: CredentialManager,
        activity: Activity
    ): String? {
        val googleIdOption =
            GetSignInWithGoogleOption.Builder(
                    "1093507723333-3b1m7h16p2rk3fv7ulkg52lh3iprs83v.apps.googleusercontent.com"
                )
                .build()

        val request =
            GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .setPreferImmediatelyAvailableCredentials(false)
                .build()

        return try {
            val result = credentialManager.getCredential(activity, request)
            val google = GoogleIdTokenCredential.createFrom(result.credential.data)
            google.idToken
        } catch (e: GetCredentialException) {
            null
        }
    }

    override suspend fun signIn(
        email: String,
        password: String,
        credentialManager: CredentialManager,
        activity: Activity
    ) {
        val idToken = requestGoogleIdToken(credentialManager, activity) ?: return

        val auth = FirebaseAuth.getInstance()
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(firebaseCredential).addOnFailureListener { /* show error */}
    }

    suspend fun googleAccountInfoAreSavedInFirestore(): Boolean {
        /*TODO Wait for search utils in firestore*/
        return false
    }

    override suspend fun createAccount(
        username: String,
        email: String,
        skills: List<SkillTag>,
        password: String
    ) {
        /*TODO Wait for UserUtils*/
    }
}

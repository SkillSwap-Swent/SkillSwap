/** @author Topaze17 used ChatGPT for comment. */
package com.swent.skillswap.model.SignIn

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Handles authentication with Google Sign-In using the Android Credential Manager and Firebase
 * Authentication.
 *
 * This class provides methods for requesting an ID token from Google, signing in with Firebase, and
 * later verifying whether a user's account information has been stored in Firestore.
 */
class SignInGoogleModel : SignInAbstractClass() {
    /**
     * Requests a Google ID token using the Android Credential Manager API.
     *
     * This function launches the Google One-Tap sign-in flow using the provided [credentialManager]
     * and [activity]. If the user successfully selects a Google account, an ID token is extracted
     * from the returned credential data.
     *
     * @param credentialManager The [CredentialManager] responsible for managing stored credentials
     *   and triggering the Google sign-in flow.
     * @param activity The [Activity] that hosts the sign-in UI.
     * @return The Google ID token as a [String], or `null` if sign-in fails or is cancelled by the
     *   user.
     */
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

    override suspend fun signIn(params: SignInParams) {
        val googleParams: SignInGoogleParams = params as SignInGoogleParams
        val credentialManager = googleParams.credentialManager
        val activity = googleParams.activity
        val idToken = requestGoogleIdToken(credentialManager, activity) ?: return

        val auth = FirebaseAuth.getInstance()
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

        auth
            .signInWithCredential(firebaseCredential)
            .addOnFailureListener { throw Exception("failed connection") }
            .await()
    }

    /**
     * Checks whether the signed-in Google user's account information is already stored in
     * Firestore.
     *
     * This method will later interact with Firestore once the related utility functions are
     * implemented. For now, it simply returns `false`.
     *
     * @return `true` if the user's account information already exists in Firestore, otherwise
     *   `false`.
     */
    suspend fun googleAccountInfoAreSavedInFirestore(): Boolean {
        /*TODO Wait for search utils in firestore*/
        return false
    }

    override suspend fun createAccount(params: CreateAccountParams) {
        val googleParams: CreateAccountGoogleParams = params as CreateAccountGoogleParams
        val username = params.username
        val skills = params.skills
        require(username.isNotBlank() && skills.isNotEmpty())
        /*TODO Wait for UserUtils*/
    }
}

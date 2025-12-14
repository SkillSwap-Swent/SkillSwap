/** @author Topaze17 used ChatGPT for comment. */
package com.swent.skillswap.model.auth

import android.app.Activity
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.tasks.await

/**
 * Handles authentication with Google Sign-In using the Android Credential Manager and Firebase
 * Authentication.
 *
 * This class provides methods for requesting an ID token from Google, signing in with Firebase, and
 * later verifying whether a user's account information has been stored in Firestore.
 */
class AuthGoogleModel(
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : AuthAbstractClass(auth, firestore) {
    private val clientId =
        "1093507723333-3b1m7h16p2rk3fv7ulkg52lh3iprs83v.apps.googleusercontent.com"
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
    ): Pair<String?, String?> {
        val rawNonce = UUID.randomUUID().toString()
        val hashedNonce =
            MessageDigest.getInstance("SHA-256").digest(rawNonce.toByteArray()).joinToString("") {
                "%02x".format(it)
            }

        val googleIdOption =
            GetSignInWithGoogleOption.Builder(clientId).setNonce(hashedNonce).build()

        val request =
            GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .setPreferImmediatelyAvailableCredentials(false)
                .build()

        return try {
            val result = credentialManager.getCredential(activity, request)
            val google = GoogleIdTokenCredential.createFrom(result.credential.data)
            google.idToken to rawNonce
        } catch (e: GetCredentialCancellationException) {
            Log.w("AuthGoogle", "User cancelled Google sign-in", e)
            null to null
        } catch (e: GetCredentialException) {
            Log.e("AuthGoogle", "Google sign-in failed", e)
            null to null
        }
    }

    override suspend fun signIn(params: SignInParams) {
        val googleParams =
            params as? SignInGoogleParams
                ?: throw IllegalArgumentException("Invalid params for Google sign-in")

        val (idToken, rawNonce) =
            requestGoogleIdToken(googleParams.credentialManager, googleParams.activity)
        if (idToken == null || rawNonce == null) {
            Log.w("AuthGoogle", "Google sign-in was cancelled or requires reauth")
            return
        }

        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, rawNonce)

        try {
            auth.signInWithCredential(firebaseCredential).await()
        } catch (e: Exception) {
            throw Exception("Firebase sign-in failed", e)
        }
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
        val user = auth.currentUser ?: return false
        val repo = UserRepoFirestore(db)

        // Check if Google provider is linked
        val isGoogleUser = user.providerData.any { it.providerId == "google.com" }
        if (!isGoogleUser) return false

        // Check if user exists in Firestore
        return repo.userExists(user.uid)
    }

    override suspend fun createAccount(params: CreateAccountParams) {
        val googleParams: CreateAccountGoogleParams = params as CreateAccountGoogleParams
        val repo = UserRepoFirestore(db)
        val username = googleParams.username
        val skills = googleParams.skills
        val userLogged = auth.currentUser
        require(username.isNotBlank() && userLogged != null && userLogged.email != null)
        val user =
            User(
                uid = userLogged.uid,
                username = username,
                email = userLogged.email ?: "",
                profilePicture = "",
                skillSet = skills,
                rating = 0f,
                availability = listOf(),
            )
        repo.addUser(user)
    }
}

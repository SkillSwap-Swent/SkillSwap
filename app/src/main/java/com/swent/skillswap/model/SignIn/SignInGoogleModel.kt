/** @author Topaze17 used ChatGPT for comment. */
package com.swent.skillswap.model.SignIn

import android.app.Activity
import androidx.compose.ui.layout.FirstBaseline
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.skillswap.model.user.Availability
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import kotlinx.coroutines.tasks.await
import kotlin.String

/**
 * Handles authentication with Google Sign-In using the Android Credential Manager and Firebase
 * Authentication.
 *
 * This class provides methods for requesting an ID token from Google, signing in with Firebase, and
 * later verifying whether a user's account information has been stored in Firestore.
 */
class SignInGoogleModel(
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : SignInAbstractClass(auth, firestore) {
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
                    "1093507723333-3b1m7h16p2rk3fv7ulkg52lh3iprs83v.apps.googleusercontent.com"//TODO add to XML at some point
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

         val auth = this.auth
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
        val repo = UserRepoFirestore(firestore)
        val user = auth.currentUser
        if(user == null) {
            return false
        }
        else {
            return when (user.providerId) {
                "google.com" -> { try {
                    repo.getUser(user.uid) //TODO ask change to that function to get boolean if user exist or make a function for it
                    true
                }
                catch (e: Exception) {
                    if(e.message == "No data found for user with ID: ${user.uid}") {
                        false
                    }
                    else {
                        throw e
                    }
                }
                }
                "password" -> false
                else -> false
            }
        }
    }

    override suspend fun createAccount(params: CreateAccountParams) {
        val googleParams: CreateAccountGoogleParams = params as CreateAccountGoogleParams
        val repo = UserRepoFirestore(firestore)
        val username = googleParams.username
        val skills = googleParams.skills
        val userLogged = auth.currentUser
        require(username.isNotBlank() && skills.isNotEmpty() && userLogged != null && userLogged.email != null)
        val skillSet = mutableSetOf<Skill>()
        for (skill in skills) {
            skillSet.add(Skill(skill, 0f, ""))//TODO change handling of description at some point
        }
        val user = User(
            uid = userLogged.uid,
            username = username,
            email = userLogged.email ?: "",
            profilePicture = "",
            skillSet = skillSet,
            rating = 0f,
            availability = listOf(),
        )
        repo.addUser(user)
    }
}

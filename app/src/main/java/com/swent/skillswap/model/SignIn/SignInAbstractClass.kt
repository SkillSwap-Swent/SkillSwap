/** @author Topaze17 used ChatGPT for comment. */
package com.swent.skillswap.model.SignIn

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Abstract base class providing a common implementation for sign-in models.
 *
 * This class implements [SignInInterface] and defines shared authentication behavior for all
 * sign-in strategies (e.g., classic email/password or Google sign-in).
 *
 * Subclasses such as [SignInClassicModel] and [SignInGoogleModel] are responsible for providing
 * concrete implementations of the sign-in and account-creation logic.
 */
abstract class SignInAbstractClass(
    protected val auth: FirebaseAuth,
    protected val firestore: FirebaseFirestore
) : SignInInterface {
    override suspend fun logOut() {
        auth.signOut()
    }
}

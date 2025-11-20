/** @author Topaze17 used ChatGPT for comment. */
package com.swent.skillswap.model.Auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Abstract base class providing a common implementation for sign-in models.
 *
 * This class implements [SignInInterface] and defines shared authentication behavior for all
 * sign-in strategies (e.g., classic email/password or Google sign-in).
 *
 * Subclasses such as [AuthClassicModel] and [AuthGoogleModel] are responsible for providing
 * concrete implementations of the sign-in and account-creation logic.
 */
abstract class AuthAbstractClass(
    protected val auth: FirebaseAuth,
    protected val db: FirebaseFirestore
) : SignInInterface {
    override suspend fun logOut() {
        auth.signOut()
    }
}

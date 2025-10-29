/** @author Topaze17 used ChatGPT for comment. */
package com.swent.skillswap.model.SignIn

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Handles classic (email and password) authentication using Firebase Authentication.
 *
 * This class implements the sign-in and account creation logic for users who register or log in via
 * email/password instead of third-party providers like Google.
 */
class SignInClassicModel(
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : SignInAbstractClass(auth, firestore) {

    override suspend fun signIn(params: SignInParams) {
        /* TODO remove when refactor backend
        val classicParams: SignInClassicParams = params as SignInClassicParams
        val email = classicParams.email
        val password = classicParams.password
        require(email.isNotBlank() && password.isNotBlank()) { "Email and password required." }
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()*/
    }

    override suspend fun createAccount(params: CreateAccountParams) {
        /*TODO remove when refactor backend
        val classicParams: CreateAccountClassicParams = params as CreateAccountClassicParams
        val email = classicParams.email
        val password = classicParams.password
        val skills = classicParams.skills
        val username = classicParams.username
        require(
            email.isNotBlank() &&
                password.isNotBlank() &&
                skills.isNotEmpty() &&
                username.isNotBlank()
        )*/
        /*TODO Wait for UserUtils
        val auth = FirebaseAuth.getInstance()
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("User not created")
        */
    }
}

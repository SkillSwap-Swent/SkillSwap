/** @author Topaze17 used ChatGPT for comment. */
package com.swent.skillswap.model.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import kotlinx.coroutines.tasks.await

/**
 * Handles classic (email and password) authentication using Firebase Authentication.
 *
 * This class implements the sign-in and account creation logic for users who register or log in via
 * email/password instead of third-party providers like Google.
 */
class AuthClassicModel(
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : AuthAbstractClass(auth, firestore) {

    override suspend fun signIn(params: SignInParams) {

        val classicParams: SignInClassicParams = params as SignInClassicParams
        val email = classicParams.email
        val password = classicParams.password
        require(email.isNotBlank() && password.isNotBlank())
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun createAccount(params: CreateAccountParams) {
        val classicParams: CreateAccountClassicParams = params as CreateAccountClassicParams
        val email = classicParams.email
        val password = classicParams.password
        val skills = classicParams.skills
        val username = classicParams.username
        val repo = UserRepoFirestore(db)
        require(email.isNotBlank() && password.isNotBlank() && username.isNotBlank())
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user
        if (user == null) {
            throw Exception("failed creation of user")
        } else {
            repo.addUser(
                User(
                    uid = user.uid,
                    username = username,
                    email = email,
                    profilePicture = "",
                    skillSet = skills,
                    rating = 0f,
                    availability = listOf()
                )
            )
        }
    }
}

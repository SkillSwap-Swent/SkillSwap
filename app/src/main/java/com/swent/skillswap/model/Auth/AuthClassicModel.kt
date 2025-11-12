/** @author Topaze17 used ChatGPT for comment. */
package com.swent.skillswap.model.Auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.skillswap.model.user.Skill
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
        val repo = UserRepoFirestore(firestore)
        require(
            email.isNotBlank() &&
                password.isNotBlank() &&
                skills.isNotEmpty() &&
                username.isNotBlank()
        )
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user
        val skillSet = mutableSetOf<Skill>()
        for (skill in skills) {
            skillSet.add(Skill(skill, 0f, "")) // TODO change handling of description at some point
        }
        if (user == null) {
            throw Exception("failed creation of user")
        } else {
            repo.addUser(
                User(
                    uid = user.uid,
                    username = username,
                    email = email,
                    profilePicture = "",
                    skillSet = skillSet,
                    rating = 0f,
                    availability = listOf()
                )
            )
        }
    }
}

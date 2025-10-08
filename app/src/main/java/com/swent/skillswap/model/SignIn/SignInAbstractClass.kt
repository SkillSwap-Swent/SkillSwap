package com.swent.skillswap.model.SignIn

import com.google.firebase.auth.FirebaseAuth

abstract class SignInAbstractClass : SignInInterface {
    override suspend fun logOut() {
        FirebaseAuth.getInstance().signOut()
    }
}

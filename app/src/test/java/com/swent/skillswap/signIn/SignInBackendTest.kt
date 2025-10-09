package com.swent.skillswap.signIn

import com.swent.skillswap.model.SignIn.CreateAccountClassicParams
import com.swent.skillswap.model.SignIn.CreateAccountGoogleParams
import com.swent.skillswap.model.SignIn.SignInClassicModel
import com.swent.skillswap.model.SignIn.SignInClassicParams
import com.swent.skillswap.model.SignIn.SignInGoogleModel
import com.swent.skillswap.model.tags.SkillTag
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SignInClassicModelTest {
    private val model = SignInClassicModel()

    @Test(expected = IllegalArgumentException::class)
    fun signInRejectsBlankEmail() = runBlocking {
        model.signIn(SignInClassicParams(email = "", password = "pw"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun signInRejectsBlankPassword() = runBlocking {
        model.signIn(SignInClassicParams(email = "user@example.com", password = ""))
    }

    @Test(expected = IllegalArgumentException::class)
    fun createAccountRejectsBlankEmail() = runBlocking {
        model.createAccount(
            CreateAccountClassicParams(
                email = "",
                password = "pw",
                username = "bob",
                skills = setOf(SkillTag.MACHINE_DESIGN)
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun createAccountRejectsBlankPassword() = runBlocking {
        model.createAccount(
            CreateAccountClassicParams(
                email = "user@example.com",
                password = "",
                username = "bob",
                skills = setOf(SkillTag.MACHINE_DESIGN)
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun createAccountRejectsBlankUsername() = runBlocking {
        model.createAccount(
            CreateAccountClassicParams(
                email = "user@example.com",
                password = "pw",
                username = "",
                skills = setOf(SkillTag.MACHINE_DESIGN)
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun createAccountRejectsEmptySkills() = runBlocking {
        model.createAccount(
            CreateAccountClassicParams(
                email = "user@example.com",
                password = "pw",
                username = "bob",
                skills = setOf()
            )
        )
    }
}

class SignInGoogleModel {
    private val model = SignInGoogleModel()

    @Test(expected = IllegalArgumentException::class)
    fun createAccountRejectsBlankUsername() = runBlocking {
        model.createAccount(
            CreateAccountGoogleParams(username = "", skills = setOf(SkillTag.MACHINE_DESIGN))
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun createAccountRejectsEmptySkills() = runBlocking {
        model.createAccount(CreateAccountGoogleParams(username = "Bob", skills = setOf()))
    }
}

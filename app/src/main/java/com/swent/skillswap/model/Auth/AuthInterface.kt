/** @author Topaze17 used ChatGPT for comment. */
package com.swent.skillswap.model.Auth

import android.app.Activity
import androidx.credentials.CredentialManager
import com.swent.skillswap.model.user.Skill

/**
 * Represents a set of parameters used for any sign-in flow.
 *
 * Implementations define the required data for a specific authentication method. Example:
 * - [SignInClassicParams] for email/password sign-in
 * - [SignInGoogleParams] for Google sign-in using Credential Manager
 */
sealed interface SignInParams

/**
 * Parameters required for classic (email & password) authentication.
 *
 * @property email User's email address.
 * @property password User's password.
 */
data class SignInClassicParams(val email: String, val password: String) : SignInParams

/**
 * Parameters required for Google authentication.
 *
 * @property activity The Activity used to trigger the Google sign-in flow.
 * @property credentialManager Android's CredentialManager for handling credentials.
 */
data class SignInGoogleParams(val activity: Activity, val credentialManager: CredentialManager) :
    SignInParams

/**
 * Represents a set of parameters used for creating a user account.
 *
 * Implementations define the data needed for each type of account creation flow. Example:
 * - [CreateAccountClassicParams] for email/password account creation
 * - [CreateAccountGoogleParams] for Google-linked account creation
 */
sealed interface CreateAccountParams

/**
 * Parameters required to create an account via email/password.
 *
 * @property username Desired username.
 * @property email User's email address.
 * @property skills Set of user skill tags (domain-specific data).
 * @property password User's chosen password.
 */
data class CreateAccountClassicParams(
    val username: String,
    val email: String,
    val skills: Set<Skill>,
    val password: String
) : CreateAccountParams

/**
 * Parameters required to create an account linked to a Google account.
 *
 * @property username Desired username.
 * @property skills Set of user skill tags (domain-specific data).
 */
data class CreateAccountGoogleParams(val username: String, val skills: Set<Skill>) :
    CreateAccountParams

/**
 * Defines a generic interface for authentication operations.
 *
 * Implementations should handle specific authentication methods (e.g., Google, email/password).
 *
 * By using [SignInParams] and [CreateAccountParams], the interface remains type-safe and flexible.
 */
interface SignInInterface {
    /**
     * Signs a user into the app using the provided [params].
     *
     * Implementations determine how to handle each subtype of [SignInParams]. For example:
     * - [SignInClassicParams] → Firebase email/password sign-in
     * - [SignInGoogleParams] → Google one-tap sign-in via CredentialManager
     */
    suspend fun signIn(params: SignInParams) = Unit

    /**
     * Creates a new user account using the provided [params].
     *
     * Implementations determine how to handle each subtype of [CreateAccountParams]. For example:
     * - [CreateAccountClassicParams] → create Firebase Auth user + Firestore entry
     * - [CreateAccountGoogleParams] → link Google account + Firestore entry
     */
    suspend fun createAccount(params: CreateAccountParams) = Unit

    /** Logs out the current authenticated user. */
    suspend fun logOut() = Unit
}

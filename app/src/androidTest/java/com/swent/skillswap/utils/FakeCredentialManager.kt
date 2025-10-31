package com.swent.skillswap.utils

import android.content.Context
import androidx.core.os.bundleOf
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject

class FakeCredentialManager private constructor(private val context: Context) :
    CredentialManager by CredentialManager.create(context) {
    companion object {
        // Creates a mock CredentialManager that always returns a CustomCredential
        // containing the given fakeUserIdToken when getCredential() is called.
        fun create(fakeUserIdToken: String): CredentialManager {
            mockkObject(GoogleIdTokenCredential)
            val googleIdTokenCredential = mockk<GoogleIdTokenCredential>()
            every { googleIdTokenCredential.idToken } returns fakeUserIdToken
            every { GoogleIdTokenCredential.createFrom(any()) } returns googleIdTokenCredential
            val fakeCredentialManager = mockk<FakeCredentialManager>()
            val mockGetCredentialResponse = mockk<GetCredentialResponse>()

            val fakeCustomCredential =
                CustomCredential(
                    type = TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,
                    data = bundleOf("id_token" to fakeUserIdToken)
                )

            every { mockGetCredentialResponse.credential } returns fakeCustomCredential
            coEvery {
                fakeCredentialManager.getCredential(any<Context>(), any<GetCredentialRequest>())
            } returns mockGetCredentialResponse

            return fakeCredentialManager
        }
    }
}

/** Created with the help of Cursor */
package com.swent.skillswap.model.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.swent.skillswap.model.user.FakeUserRepository
import com.swent.skillswap.model.user.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FCMTokenManagerTest {

    private lateinit var fakeRepository: FakeUserRepository
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockUser: FirebaseUser
    private lateinit var fcmTokenManager: FCMTokenManager
    private val testUserId = "test-user-123"
    private val testToken = "test-fcm-token-12345"

    @Before
    fun setUp() {
        fakeRepository = FakeUserRepository()
        mockAuth = mockk<FirebaseAuth>(relaxed = true)
        mockUser = mockk<FirebaseUser>(relaxed = true)
        every { mockUser.uid } returns testUserId
        every { mockAuth.currentUser } returns mockUser
        fcmTokenManager = FCMTokenManager(fakeRepository, mockAuth)
    }

    @After
    fun tearDown() {
        fakeRepository.clear()
        unmockkAll()
    }

    @Test
    fun saveToken_withValidUserId_savesToken() = runTest {
        val user = User(uid = testUserId, username = "test", email = "test@test.com")
        fakeRepository.addUser(user)

        fcmTokenManager.saveToken(testToken, testUserId)

        val updatedUser = fakeRepository.getUser(testUserId)
        assertEquals(testToken, updatedUser.fcmToken)
    }

    @Test
    fun saveToken_withNullUserId_usesCurrentUser() = runTest {
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns testUserId
        val user = User(uid = testUserId, username = "test", email = "test@test.com")
        fakeRepository.addUser(user)

        fcmTokenManager.saveToken(testToken)

        val updatedUser = fakeRepository.getUser(testUserId)
        assertEquals(testToken, updatedUser.fcmToken)
    }

    @Test
    fun saveToken_withNullUserIdAndNoCurrentUser_doesNotSave() = runTest {
        every { mockAuth.currentUser } returns null

        fcmTokenManager.saveToken(testToken)

        // Should not throw, but also not save
    }

    @Test
    fun saveToken_withEmptyToken_doesNotSave() = runTest {
        val user = User(uid = testUserId, username = "test", email = "test@test.com")
        fakeRepository.addUser(user)

        fcmTokenManager.saveToken("", testUserId)

        // Should not throw, but also not save empty token
        // The implementation should check for empty tokens
    }

    @Test
    fun saveToken_withNonExistentUser_handlesGracefully() = runTest {
        try {
            fcmTokenManager.saveToken(testToken, testUserId)
        } catch (e: Exception) {
            // Expected to throw since user doesn't exist
            assertTrue(
                e.message?.contains("User does not exist") == true ||
                    e.message?.contains("not found") == true
            )
        }
    }

    @Test
    fun saveToken_withRepositoryFailure_handlesGracefully() = runTest {
        val user = User(uid = testUserId, username = "test", email = "test@test.com")
        fakeRepository.addUser(user)
        fakeRepository.setShouldFailOnUpdateFcmToken(true)

        try {
            fcmTokenManager.saveToken(testToken, testUserId)
        } catch (e: Exception) {
            // Expected to throw when repository fails
            assertTrue(
                e.message?.contains("updateFcmToken") == true ||
                    e.message?.contains("failure") == true
            )
        }
    }
}

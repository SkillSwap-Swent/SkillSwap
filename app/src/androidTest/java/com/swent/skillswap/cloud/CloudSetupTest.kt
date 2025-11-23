// Coded with love with help of copilot
package com.swent.skillswap.cloud

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.swent.skillswap.firebase.CloudReferences.PROFILE_PICTURES_PATH
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CloudSetupTest {
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        FirebaseApp.initializeApp(context)
        storage = Firebase.storage
        auth = FirebaseAuth.getInstance()
        auth.useEmulator("10.0.2.2", 9099)
        createOrSignInTestUser()
    }

    /**
     * Creates a test user with the given email and password, or signs in if the user already exists.
     * AI-generated code
     */
    fun createOrSignInTestUser(email: String = "test@local.com", password: String = "Password123") {
        try {
            Tasks.await(auth.createUserWithEmailAndPassword(email, password), 15, TimeUnit.SECONDS)
            Tasks.await(auth.signInWithEmailAndPassword(email, password), 15, TimeUnit.SECONDS)
        } catch (e: Exception) {
            //if the user already exists, just sign in
            Tasks.await(auth.signInWithEmailAndPassword(email, password), 15, TimeUnit.SECONDS)
        }
    }

    /**
     * Clears the authentication state by deleting the current user and signing out.
     * AI-generated code
     */
    @After
    fun clearAuth() {
        try {
            auth.currentUser?.let { Tasks.await(it.delete(), 10, TimeUnit.SECONDS) }
        } catch (_: Exception) {
        } finally {
            auth.signOut()
        }
    }

    @Test
    fun canLoadImageInCloud() {
        runBlocking {
            withTimeout(60_000) {
                // point to emulator (CI/device emulator localhost)
                storage.useEmulator("10.0.2.2", 9199)

                val remoteRef =
                    storage.reference.child("$PROFILE_PICTURES_PATH/test_image_instrumented.jpg")
                val bytes = "simple-test-bytes".toByteArray()

                val uploadTask = remoteRef.putBytes(bytes)
                val snapshot = Tasks.await(uploadTask, 30, TimeUnit.SECONDS)
                assertNotNull(snapshot)
                assertTrue(snapshot.bytesTransferred > 0)

                // verify download url is retrievable
                val url = Tasks.await(remoteRef.downloadUrl, 30, TimeUnit.SECONDS)
                assertNotNull(url)
            }
        }
    }

    @Test
    fun canFetchImageFromCloud() {
        runBlocking {
            withTimeout(60_000) {
                storage.useEmulator("10.0.2.2", 9199)

                val remoteRef =
                    storage.reference.child("$PROFILE_PICTURES_PATH/test_image_instrumented.jpg")

                // Ensure the file exists: try a tiny HEAD/getBytes(1), if fails upload a small
                // payload
                try {
                    Tasks.await(remoteRef.getBytes(1), 5, TimeUnit.SECONDS)
                } catch (e: Exception) {
                    Tasks.await(
                        remoteRef.putBytes("fetch-ensure".toByteArray()),
                        30,
                        TimeUnit.SECONDS
                    )
                }

                // fetch the file (up to 1MB)
                val data = Tasks.await(remoteRef.getBytes(1024 * 1024), 30, TimeUnit.SECONDS)
                assertNotNull(data)
                assertTrue(data.isNotEmpty())

                // cleanup test artifact
                try {
                    Tasks.await(remoteRef.delete(), 30, TimeUnit.SECONDS)
                } catch (ignored: Exception) {
                    // ignore cleanup errors
                }
            }
        }
    }
}

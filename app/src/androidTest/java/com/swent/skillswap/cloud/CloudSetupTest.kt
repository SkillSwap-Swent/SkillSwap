// Coded with love and with help of copilot
package com.swent.skillswap.cloud

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.swent.skillswap.firebase.CloudReferences.PROFILE_PICTURES_PATH
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CloudSetupTest {
    private lateinit var storage: FirebaseStorage

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        FirebaseApp.initializeApp(context)
        storage = Firebase.storage
    }

    @Test
    fun canLoadImageInCloud() = runBlocking {
        val testFileName = "test_image_0.jpg"
        val testPath = "$PROFILE_PICTURES_PATH/$testFileName"
        val testData = "test image content".toByteArray()

        var uploadSuccess = false

        val storageRef = storage.reference.child(testPath)

        storageRef
            .putBytes(testData)
            .addOnSuccessListener { taskSnapshot ->
                assertNotNull("Upload task snapshot should not be null", taskSnapshot)
                assertNotNull("Metadata should not be null", taskSnapshot.metadata)
                uploadSuccess = true
            }
            .addOnFailureListener { _ -> uploadSuccess = false }

        withTimeout(15_000L) {
            assertTrue("Failed to upload image to Firebase Storage", uploadSuccess)
        }

        storageRef.delete()
    }

    @Test
    fun canFetchImageFromCloud() {
        val testFileName = "test_image_1.jpg"
        val testPath = "$PROFILE_PICTURES_PATH/$testFileName"
        val testData = "test image content".toByteArray()

        val storageRef = storage.reference.child(testPath)

        var uploadSuccess = false
        var downloadSuccess = false

        // Upload test image
        storageRef
            .putBytes(testData)
            .addOnSuccessListener { uploadSuccess = true }
            .addOnFailureListener { _ -> uploadSuccess = false }

        runBlocking {
            withTimeout(15_000L) {
                assertTrue("Failed to upload image to Firebase Storage", uploadSuccess)
            }
        }

        // Download test image
        storageRef
            .getBytes(1024 * 1024)
            .addOnSuccessListener { bytes ->
                assertNotNull("Downloaded bytes should not be null", bytes)
                assertTrue(
                    "Downloaded data should match uploaded data",
                    bytes.contentEquals(testData)
                )
                downloadSuccess = true
            }
            .addOnFailureListener { _ -> downloadSuccess = false }

        runBlocking {
            withTimeout(15_000L) {
                assertTrue("Failed to download image from Firebase Storage", downloadSuccess)
            }
        }

        // Clean up
        storageRef.delete()
    }
}

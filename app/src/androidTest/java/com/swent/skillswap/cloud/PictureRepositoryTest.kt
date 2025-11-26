package com.swent.skillswap.cloud

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.swent.skillswap.firebase.CloudReferences
import com.swent.skillswap.firebase.CloudReferences.FEED_PICTURES_PATH
import com.swent.skillswap.firebase.CloudReferences.PROFILE_PICTURES_PATH
import com.swent.skillswap.model.images.PictureRepository
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.compareTo
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PictureRepositoryTest {
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    private lateinit var pictureRepo: PictureRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        FirebaseApp.initializeApp(context)
        storage = Firebase.storage
        storage.useEmulator("10.0.2.2", 9199)
        auth = FirebaseAuth.getInstance()
        auth.useEmulator("10.0.2.2", 9099)
        createOrSignInTestUser()

        pictureRepo = PictureRepository(storage)
    }

    /**
     * HELPER FUNCTION Creates a test user with the given email and password, or signs in if the
     * user already exists.
     */
    fun createOrSignInTestUser(email: String = "test@local.com", password: String = "Password123") {
        try {
            Tasks.await(auth.createUserWithEmailAndPassword(email, password), 15, TimeUnit.SECONDS)
            Tasks.await(auth.signInWithEmailAndPassword(email, password), 15, TimeUnit.SECONDS)
        } catch (_: Exception) {
            // if the user already exists, just sign in
            Tasks.await(auth.signInWithEmailAndPassword(email, password), 15, TimeUnit.SECONDS)
        }
    }

    /**
     * Clears the authentication state by deleting the current user and signing out, and cleaning
     * storage AI-generated code
     */
    @After
    fun cleanUp() {
        /** Delete the test user */
        /** Clean up storage by deleting all files in both directories */
        runBlocking {
            for (path in CloudReferences.values) {
                val storageRef = FirebaseStorage.getInstance().reference.child(path)
                val listResult = storageRef.listAll().await()
                listResult.items.forEach { it.delete().await() }
            }
        }

        try {
            auth.currentUser?.let { Tasks.await(it.delete(), 10, TimeUnit.SECONDS) }
        } catch (_: Exception) {} finally {
            auth.signOut()
        }
    }

    /**
     * HELPER FUNCTION Generates a test image Uri by creating a Bitmap, saving it to a temporary
     * file
     *
     * @return Uri of the generated test image
     *
     *   Ai-generated code
     */
    fun generateTestImageUri(
        context: Context,
        width: Int = 100,
        height: Int = 100,
        color: Int = Color.RED
    ): Uri {
        /** Create a bitmap */
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(color)

        /** Create a temporary file to hold the bitmap */
        val tempFile = File(context.cacheDir, "test_image_${System.currentTimeMillis()}.png")
        tempFile.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }

        return Uri.fromFile(tempFile)
    }

    /** Tests */
    @Test
    fun uploadPictureThrowsExceptionForInvalidArguments() {
        val invalidUri = Uri.parse("http://invalid-uri.com/image.jpg")

        /** Invalid URI */
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { pictureRepo.uploadPicture("abc", invalidUri, PROFILE_PICTURES_PATH) }
        }

        /** Invalid Path */
        val validUri = Uri.parse("content://valid-uri.com/image.jpg")
        val invalidPath = "invalid/path"

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { pictureRepo.uploadPicture("abc", validUri, invalidPath) }
        }
    }

    @Test
    fun uploadPictureSucceedsWithValidArguments() {
        /** Generate a test image URI */
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val uri = generateTestImageUri(context)

        /** Upload the picture and verify it exists in storage */
        runBlocking {
            val downloadUrl = pictureRepo.uploadPicture("uid", uri, PROFILE_PICTURES_PATH)
            assertTrue(
                downloadUrl.toString().startsWith("http://") ||
                    downloadUrl.toString().startsWith("https://")
            )

            val listResult = storage.reference.child(PROFILE_PICTURES_PATH).listAll().await()
            val item =
                listResult.items.firstOrNull()
                    ?: throw AssertionError("Uploaded file not found in storage")

            try {
                val metadata = Tasks.await(item.metadata, 10, TimeUnit.SECONDS)
                assertTrue(metadata.sizeBytes > 0)
            } catch (e: Exception) {
                throw AssertionError("Cannot get the metadata because of : ${e.message}", e)
            }
        }
    }

    @Test
    fun deletePictureThrowsExceptionForInvalidArguments() {
        /** Invalid uid */
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { pictureRepo.deletePicture("", PROFILE_PICTURES_PATH) }
        }

        /** Invalid Path */
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { pictureRepo.deletePicture("abc", "bibi") }
        }
    }

    @Test
    fun deletePictureSucceedsWithValidArguments() {
        /** Generate a test image URI and upload it first */
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val uri = generateTestImageUri(context)

        runBlocking {
            pictureRepo.uploadPicture("uid_to_delete", uri, FEED_PICTURES_PATH)
            val listResultWithElement =
                storage.reference.child(FEED_PICTURES_PATH).listAll().await()
            /** Verify the picture exists in storage */
            assertTrue(listResultWithElement.items.first().name == "uid_to_delete")

            /** Now delete the picture */
            pictureRepo.deletePicture("uid_to_delete", FEED_PICTURES_PATH)

            /** Verify the picture no longer exists in storage */
            val listResult = storage.reference.child(FEED_PICTURES_PATH).listAll().await()
            assertThrows(NoSuchElementException::class.java) { listResult.items.first() }
        }
    }
}

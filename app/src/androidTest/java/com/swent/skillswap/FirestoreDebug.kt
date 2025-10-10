package com.swent.skillswap

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.tasks.Tasks
import com.swent.skillswap.utils.FirebaseEmulator
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import kotlin.text.set

@RunWith(AndroidJUnit4::class)
class FirestoreDebugTest {

    @Test
    fun testWriteToFirestoreEmulator() {
        val firestore = FirebaseEmulator.firestore

        val testDocument = mapOf("hello" to "world")
        val collection = firestore.collection("testCollection")

        val task = collection.document("testDocument").set(testDocument)
        val result = Tasks.await(task, 5, TimeUnit.SECONDS)
        assertTrue("Document should be written successfully", task.isSuccessful)

    }
}

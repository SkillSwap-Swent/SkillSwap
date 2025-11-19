package com.swent.skillswap

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.swent.skillswap.utils.FirebaseEmulator
import java.util.concurrent.TimeUnit
import kotlin.text.set
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirestoreDebugTest {

    @Test
    fun testWriteToFirestoreEmulator() {
        val firestore = FirebaseEmulator.firestore

        val testDocument = mapOf("hello" to "world")
        val collection = firestore.collection("testCollection")

        val task = collection.document("testDocument").set(testDocument)
        val result = Tasks.await(task, 10_000L, TimeUnit.SECONDS)
        assertTrue("Document should be written successfully", task.isSuccessful)
    }
}

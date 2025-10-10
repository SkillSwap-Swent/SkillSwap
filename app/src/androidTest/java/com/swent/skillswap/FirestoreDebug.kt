package com.swent.skillswap

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.swent.skillswap.utils.FirebaseEmulator
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

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            task.addOnCompleteListener {
                assertTrue("Document should be written successfully", it.isSuccessful)
            }
        }
    }
}

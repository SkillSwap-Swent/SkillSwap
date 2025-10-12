/*
 * @author: Léo. MARTI
 * /!\ Written with help of Copilot
 * > Give me a template for firestore android instrumented test
 */

package com.swent.skillswap.UserFirestore

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.swent.skillswap.model.user.User
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserFireStoreTest {

    private lateinit var db: FirebaseFirestore

    @Before
    fun setUp() {
        // Initialisation FirebaseApp si nécessaire
        if (
            FirebaseApp.getApps(
                    /* context = */ androidx.test.platform.app.InstrumentationRegistry
                        .getInstrumentation()
                        .targetContext
                )
                .isEmpty()
        ) {
            val options =
                FirebaseOptions.Builder()
                    .setProjectId("demo-test")
                    .setApplicationId("1:123:android:123abc")
                    .build()
            FirebaseApp.initializeApp(
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
                    .targetContext,
                options
            )
        }
        db = FirebaseFirestore.getInstance()
        db.useEmulator("10.0.2.2", 8080) // 10.0.2.2 pour l'émulateur Android
        db.firestoreSettings =
            FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false) // avoid cashing stuff between tests
                .build()
    }

    @After
    fun tearDown() = runBlocking {
        // Nettoyage de la collection users
        val snapshot = db.collection("users").get().await()
        for (doc in snapshot.documents) {
            doc.reference.delete().await()
        }
    }

    @Test
    fun addAndRetrieveUser_succeeds() = runBlocking {
        val user =
            User(
                uid = "testuid",
                username = "testuser",
                email = "test@example.com",
                profilePicture = "",
                skillSet = setOf(),
                rating = 5.0f,
                availability = listOf()
            )

        // Ajout de l'utilisateur
        db.collection("users").document(user.uid).set(user).await()

        // Récupération de l'utilisateur
        val snapshot = db.collection("users").document(user.uid).get().await()
        val retrievedUser = snapshot.toObject(User::class.java)

        assertNotNull(retrievedUser)
        assertEquals(user.uid, retrievedUser?.uid)
        assertEquals(user.username, retrievedUser?.username)
    }
}

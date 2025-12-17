/*
 * Written with help of copilot to complete all repetitive code, and set up the companion object
 */
package com.swent.skillswap.end2end

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.swent.skillswap.MainActivity
import com.swent.skillswap.firebase.CloudReferences.values
import com.swent.skillswap.firebase.FirestorePaths.REQUESTS_COLLECTION
import com.swent.skillswap.model.post.PostFirestoreRepository
import com.swent.skillswap.model.post.PostReply
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.post.ReplyStatus
import com.swent.skillswap.model.post.Request
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.ui.auth.SignInTags
import com.swent.skillswap.ui.chat.ChatListTestTags
import com.swent.skillswap.ui.navigation.NavigationTestTags
import com.swent.skillswap.ui.post.RequestScreenTags
import com.swent.skillswap.ui.post.personalPosts.PersonalPostsScreenTags
import com.swent.skillswap.ui.user.ProfileTestTags
import com.swent.skillswap.utils.FirebaseEmulator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/** End-to-end tests for Milestone 3 Tests complete user flows */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class End2EndM3RequestInteractionFLow {
    lateinit var db: com.google.firebase.firestore.FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var storage: com.google.firebase.storage.FirebaseStorage
    lateinit var postRepo: PostFirestoreRepository
    lateinit var chatRepo: com.swent.skillswap.model.chat.ChatRepositoryFirestore
    lateinit var userRepo: com.swent.skillswap.model.user.UserRepoFirestore

    val testEmail = "e2e@test.com"
    val responderEmail = "e2eResponder@test.com"
    val testPassword = "Password1234"
    val RESPONDER_UID = "RESPONDER_UID"

    val user =
        User(
            uid = "", // evaluated at runtime
            username = "E2ETester",
            email = testEmail,
            skillSet = setOf(Skill(SkillTag.DATA_STRUCTURES, 2.5F, "I'm good"))
        )

    val responder =
        User(
            uid = RESPONDER_UID,
            username = "E2EResponder",
            email = responderEmail,
            skillSet = setOf(Skill(SkillTag.CALCULUS, 3.0F, "I can help"))
        )

    companion object {

        @BeforeClass
        @JvmStatic
        fun setupEmulator() {
            FirebaseEmulator.reinitialize()
            FirebaseEmulator.clearAuthEmulator()
            FirebaseEmulator.clearFirestoreEmulator()
        }

        @AfterClass
        @JvmStatic
        fun tearDownFirebase() {
            // Sign out before clearing
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (_: Exception) {}

            // Clear emulators AFTER this test class finishes
            FirebaseEmulator.clearAuthEmulator()
            // FirebaseEmulator.clearFirestoreEmulator()
        }
    }

    @Before
    fun initialSetup() {
        db = FirebaseEmulator.firestore
        auth = FirebaseEmulator.auth
        storage = FirebaseEmulator.storage
        postRepo = PostFirestoreRepository(db)
        chatRepo = com.swent.skillswap.model.chat.ChatRepositoryFirestore(db, postRepo)
        userRepo = com.swent.skillswap.model.user.UserRepoFirestore(db)

        runBlocking {
            try {
                auth.createUserWithEmailAndPassword(testEmail, testPassword).await()
                auth.createUserWithEmailAndPassword(responderEmail, testPassword).await()
            } catch (_: Exception) {
                // User may already exist, do nothing
            }
        }
    }

    /**
     * Clears all files in Firebase Storage emulator under specified paths
     *
     * @throws Exception if clearing storage fails
     */
    fun clearStorage() {
        for (path in values) {
            val storageRef = storage.reference.child(path)
            storageRef
                .listAll()
                .addOnSuccessListener { listResult ->
                    for (item in listResult.items) {
                        item.delete()
                    }
                }
                .addOnFailureListener {
                    throw Exception(
                        "Failed to clear storage after end2end-request interaction flow at a child path of: $path"
                    )
                }
        }
    }

    @get:Rule
    val grantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.POST_NOTIFICATIONS
        )

    @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun t0_createPost() {
        /** Wait for initial load of login screen */
        composeTestRule.waitUntil(10_000) {
            try {
                composeTestRule.onNodeWithTag(SignInTags.LOGO).assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeTestRule.waitForIdle()

        /** Login flow */
        composeTestRule.onNodeWithTag(SignInTags.EMAIL_FIELD).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.EMAIL_FIELD).performTextInput("e2e@test.com")
        composeTestRule.onNodeWithTag(SignInTags.PASSWORD_FIELD).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.PASSWORD_FIELD).performTextInput("Password1234")
        composeTestRule.onNodeWithTag(SignInTags.SIGN_IN_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(SignInTags.SIGN_IN_BUTTON).performClick()
        runBlocking { auth.signInWithEmailAndPassword("e2e@test.com", "Password1234").await() }

        /** simulate a correctly created account */
        val signedUser = user.copy(uid = auth.currentUser!!.uid)
        runBlocking {
            userRepo.addUser(signedUser)
            userRepo.addUser(responder)
        }

        /** Wait for the profile screen to load after login */
        composeTestRule.waitUntil(10_000) {
            try {
                composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeTestRule.waitForIdle()

        /** Navigate to Request Screen */
        composeTestRule.onNodeWithTag(NavigationTestTags.POSTS_TAB).performClick()

        /** Wait for the Request Creation Screen to load */
        composeTestRule.waitUntil(10_000) {
            try {
                composeTestRule.onNodeWithTag(RequestScreenTags.TITLE_INPUT).assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeTestRule.waitForIdle()

        /** Create Request Flow */
        composeTestRule.onNodeWithTag(RequestScreenTags.TITLE_INPUT).performScrollTo()
        composeTestRule.onNodeWithTag(RequestScreenTags.TITLE_INPUT).performTextInput("TitleForE2E")
        composeTestRule.onNodeWithTag(RequestScreenTags.DESCRIPTION_INPUT).performScrollTo()
        composeTestRule
            .onNodeWithTag(RequestScreenTags.DESCRIPTION_INPUT)
            .performTextInput("DescriptionForE2E")
        composeTestRule.onNodeWithTag(RequestScreenTags.TAGS_INPUT).performScrollTo()
        composeTestRule.onNodeWithTag(RequestScreenTags.TAGS_INPUT).performTextInput("CALCULU")

        /** wait for the suggestion to display and click it */
        composeTestRule.onNodeWithText("CALCULUS").performClick()
        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).performScrollTo()
        composeTestRule.onNodeWithTag(RequestScreenTags.CREATE_BUTTON).performClick()

        /** wait for the post to be upload on firestore */
        composeTestRule.waitUntil(10_000) {
            runBlocking {
                val querySnapshot =
                    db.collection(REQUESTS_COLLECTION)
                        .whereEqualTo("title", "TitleForE2E")
                        .get()
                        .await()

                querySnapshot.documents.isNotEmpty()
            }
        }

        /** Navigate to Chat to see the reply */
        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()

        /** Wait for the Chat List Screen to load */
        composeTestRule.waitUntil(20_000) {
            try {
                composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()
                composeTestRule.onNodeWithTag(ChatListTestTags.SCREEN).assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(ChatListTestTags.REPLIES_TAB).performClick()

        /** Add a response to the created post in firestore */
        runBlocking {
            val querySnapshot =
                db.collection(REQUESTS_COLLECTION)
                    .whereEqualTo("title", "TitleForE2E")
                    .get()
                    .await()

            val post = postRepo.getPost(PostType.REQUEST, querySnapshot.documents[0].id)

            val postReply =
                PostReply(
                    postId = post.uid,
                    ownerId = "RESPONDER_UID",
                    creation = Timestamp.now(),
                    message = "Hi, I can help you with that!",
                    postType = PostType.REQUEST,
                    replyStatus = ReplyStatus.PROPOSED
                )

            val responsePost = (post as Request).copy(postReplies = post.postReplies + postReply)

            /** Inject the reply directly into firestore */
            postRepo.editPost(responsePost.uid, responsePost)

            /** Manage chat creation */
            chatRepo.createChat(listOf("RESPONDER_UID", post.ownerId), post.uid, post.type)
        }

        /** Wait for the reply to show up */
        composeTestRule.waitUntil(30_000) {
            try {
                composeTestRule.onNodeWithText("E2EResponder").assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }

        /** Go back to profile screen */
        composeTestRule.onNodeWithTag(NavigationTestTags.PROFILE_TAB).performClick()

        /** Wait for the profile screen to load */
        composeTestRule.waitUntil(10_000) {
            try {
                composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeTestRule.waitForIdle()

        /** Go to my post screen */
        composeTestRule.onNodeWithTag(ProfileTestTags.MY_POSTS_BUTTON).performClick()

        /** Wait for the my posts screen to load */
        composeTestRule.waitUntil(30_000) {
            try {
                composeTestRule.onNodeWithTag(PersonalPostsScreenTags.SCREEN).assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeTestRule.waitForIdle()

        /** Click on the edit button of the created post */
        composeTestRule.onNodeWithTag(PersonalPostsScreenTags.EDIT_BUTTON).performClick()

        /** Wait for the edit post screen to load */
        composeTestRule.waitUntil(10_000) {
            try {
                composeTestRule.onNodeWithTag(RequestScreenTags.TITLE_INPUT).assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeTestRule.waitForIdle()

        /** Edit the created Request Flow */
        composeTestRule.onNodeWithTag(RequestScreenTags.TITLE_INPUT).performScrollTo()
        composeTestRule.onNodeWithTag(RequestScreenTags.TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(RequestScreenTags.TITLE_INPUT).performTextInput("Edited")
        composeTestRule.onNodeWithText("Submit").performScrollTo()
        composeTestRule.onNodeWithText("Submit").performClick()

        /** Wait for the post to be edited on firestore */
        composeTestRule.waitUntil(10_000) {
            runBlocking {
                val querySnapshot =
                    db.collection(REQUESTS_COLLECTION).whereEqualTo("title", "Edited").get().await()

                querySnapshot.documents.isNotEmpty()
            }
        }
        /** GO back to profile */
        composeTestRule.onNodeWithTag(PersonalPostsScreenTags.BACK_BUTTON).performClick()

        /** Wait for the profile screen to load */
        composeTestRule.waitUntil(10_000) {
            try {
                composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeTestRule.waitForIdle()

        /** Go back to chat tab */
        composeTestRule.onNodeWithTag(NavigationTestTags.CHAT_TAB).performClick()

        /** Wait for the Chat List Screen to load */
        composeTestRule.waitUntil(20_000) {
            try {
                composeTestRule.onNodeWithTag(ChatListTestTags.SCREEN).assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(ChatListTestTags.REPLIES_TAB).performClick()
        /** Verify that the chat related to the edited post is removed */
        composeTestRule.waitUntil(10_000) {
            try {
                composeTestRule.onNodeWithText("Edited").assertDoesNotExist()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }
}

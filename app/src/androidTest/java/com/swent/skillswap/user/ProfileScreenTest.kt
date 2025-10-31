package com.swent.skillswap.user

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.skillswap.SkillSwapApp
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.Preference
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepoFirestore
import com.swent.skillswap.ui.user.ProfileTestTags
import com.swent.skillswap.ui.user.SkillsEditTestTags
import com.swent.skillswap.utils.FakeJwtGenerator
import com.swent.skillswap.utils.FirebaseEmulator
import com.swent.skillswap.viewModel.ProfileViewModel
import kotlin.text.get
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var userRepo: UserRepoFirestore
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var token: String

    private val email = "profiletest@example.com"
    private val displayName = "Profile Tester"

    companion object {
        @JvmStatic lateinit var auth: com.google.firebase.auth.FirebaseAuth
        @JvmStatic lateinit var firestore: FirebaseFirestore

        @BeforeClass
        @JvmStatic
        fun globalSetUp() {
            FirebaseEmulator.startEmulator()
            auth = FirebaseEmulator.auth
            firestore = FirebaseEmulator.firestore
        }
    }

    @Before
    fun setUp() {
        FirebaseEmulator.clearAuthEmulator()
        FirebaseEmulator.clearFirestoreEmulator()

        token = FakeJwtGenerator.createFakeGoogleIdToken(name = displayName, email = email)

        FirebaseEmulator.createGoogleUser(token)

        runBlocking {
            val cred = GoogleAuthProvider.getCredential(token, null)
            auth.signInWithCredential(cred).await()
            val uid = requireNotNull(auth.currentUser?.uid)

            userRepo = UserRepoFirestore(firestore)

            val testUser =
                User(
                    uid = uid,
                    username = "profiletester",
                    email = email,
                    profilePicture = "",
                    skillSet =
                        setOf(
                            Skill(SkillTag.MACHINE_DESIGN, 4.5f, "CAD and 3D modeling"),
                            Skill(SkillTag.ALGORITHMS, 5.0f, "Data structures expertise")
                        ),
                    rating = 4.8f,
                    availability = emptyList(),
                    preference = Preference.SKILLS
                )

            userRepo.addUser(testUser)
        }

        profileViewModel = ProfileViewModel()

        composeTestRule.setContent { SkillSwapApp() }
    }

    @After
    fun tearDown() {
        auth.signOut()
        FirebaseEmulator.clearAuthEmulator()
        FirebaseEmulator.clearFirestoreEmulator()
    }

    @Test
    fun profileScreen_displaysTitle() {
        composeTestRule.onNodeWithTag(ProfileTestTags.PROFILE_TITLE).assertExists()
    }

    @Test
    fun profileScreen_displaysEmailSection() {
        composeTestRule.onNodeWithTag(ProfileTestTags.EMAIL_SECTION).assertExists()
    }

    @Test
    fun profileScreen_displaysUsernameSection() {
        composeTestRule.onNodeWithTag(ProfileTestTags.USERNAME_SECTION).assertExists()
    }

    @Test
    fun profileScreen_displaysSkillsSection() {
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).assertExists()
    }

    @Test
    fun profileScreen_displaysPreferencesSection() {
        composeTestRule.onNodeWithTag(ProfileTestTags.PREFERENCES_SECTION).assertExists()
    }

    @Test
    fun profileScreen_expandsEmailSection() {
        composeTestRule.onNodeWithTag(ProfileTestTags.EMAIL_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.EMAIL_VALUE).assertExists()
        composeTestRule.onNodeWithTag(ProfileTestTags.EMAIL_EDIT).assertExists()
    }

    @Test
    fun profileScreen_expandsUsernameSection() {
        composeTestRule.onNodeWithTag(ProfileTestTags.USERNAME_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.USERNAME_VALUE).assertExists()
        composeTestRule.onNodeWithTag(ProfileTestTags.USERNAME_EDIT).assertExists()
    }

    @Test
    fun profileScreen_expandsSkillsSection() {
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_COUNT).assertExists()
    }

    @Test
    fun profileScreen_expandsPreferencesSection() {
        composeTestRule.onNodeWithTag(ProfileTestTags.PREFERENCES_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.PREF_OPTION_MONEY).assertExists()
        composeTestRule.onNodeWithTag(ProfileTestTags.PREF_OPTION_SKILLS).assertExists()
    }

    @Test
    fun profileScreen_skillsSectionCanExpandAndCollapse() {
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_COUNT).assertExists()

        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
    }

    @Test
    fun profileScreen_displaysUserSkills() {
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_LIST).assertExists()
    }

    @Test
    fun profileScreen_displayUserPreference() {
        composeTestRule.onNodeWithTag(ProfileTestTags.PREFERENCES_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.PREF_OPTION_SKILLS).assertExists()
    }

    @Test
    fun profileScreen_selectingMoneyPreferenceUpdatesFirestore() {
        composeTestRule.onNodeWithTag(ProfileTestTags.PREFERENCES_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.PREF_OPTION_MONEY).performClick()

        Thread.sleep(200)

        runBlocking {
            val uid = requireNotNull(auth.currentUser?.uid)
            val user = userRepo.getUser(uid)
            val preference = user.preference

            assert(preference == Preference.MONEY)
        }

        composeTestRule.onNodeWithTag(ProfileTestTags.PREF_OPTION_SKILLS).performClick()
        Thread.sleep(200)

        runBlocking {
            val uid = requireNotNull(auth.currentUser?.uid)
            val user = userRepo.getUser(uid)
            val preference = user.preference

            assert(preference == Preference.SKILLS)
        }
    }

    @Test
    fun skillsEditScreen_displaysAllElements() {
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_EDIT).performClick()

        // Verify screen container
        composeTestRule.onNodeWithTag(SkillsEditTestTags.SCREEN_CONTAINER).assertExists()

        // Verify title
        composeTestRule.onNodeWithTag(SkillsEditTestTags.TITLE).assertExists()

        // Verify search field
        composeTestRule.onNodeWithTag(SkillsEditTestTags.SEARCH_FIELD).assertExists()

        // Verify selected skills count
        composeTestRule.onNodeWithTag(SkillsEditTestTags.SELECTED_COUNT).assertExists()

        // Verify selected skills list
        composeTestRule.onNodeWithTag(SkillsEditTestTags.SELECTED_LIST).assertExists()

        // Verify action buttons
        composeTestRule.onNodeWithTag(SkillsEditTestTags.CANCEL_BUTTON).assertExists()
        composeTestRule.onNodeWithTag(SkillsEditTestTags.SAVE_BUTTON).assertExists()

        // Verify dropdown exists
        composeTestRule.onNodeWithTag(SkillsEditTestTags.DROPDOWN).assertExists()
    }

    @Test
    fun skillsEditScreen_removingSkillUpdatesFirestore() {
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_EDIT).performClick()

        composeTestRule
            .onNodeWithTag(
                "${SkillsEditTestTags.SKILL_CHIP_PREFIX}_${SkillTag.MACHINE_DESIGN.name}"
            )
            .performClick()

        composeTestRule.onNodeWithTag(SkillsEditTestTags.SAVE_BUTTON).performClick()

        Thread.sleep(500)

        runBlocking {
            val uid = requireNotNull(auth.currentUser?.uid)
            val user = userRepo.getUser(uid)
            val skillNames = user.skillSet.map { it.name }

            assert(!skillNames.contains(SkillTag.MACHINE_DESIGN))
            assert(skillNames.contains(SkillTag.ALGORITHMS))
        }
    }

    @Test
    fun skillsEditScreen_addingSkillUpdatesFirestore() {
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_EDIT).performClick()

        // Type in search field to filter skills
        composeTestRule.onNodeWithTag(SkillsEditTestTags.SEARCH_FIELD).performClick()
        composeTestRule.onNodeWithTag(SkillsEditTestTags.SEARCH_FIELD).performTextInput("calculus")

        // Click on the first suggestion (Calculus)
        composeTestRule
            .onNodeWithTag("${SkillsEditTestTags.SUGGESTION_ITEM_PREFIX}_0")
            .performClick()

        // Click save button
        composeTestRule.onNodeWithTag(SkillsEditTestTags.SAVE_BUTTON).performClick()

        Thread.sleep(500)

        runBlocking {
            val uid = requireNotNull(auth.currentUser?.uid)
            val user = userRepo.getUser(uid)
            val skillNames = user.skillSet.map { it.name }

            assert(skillNames.contains(SkillTag.CALCULUS))
            assert(skillNames.contains(SkillTag.MACHINE_DESIGN))
            assert(skillNames.contains(SkillTag.ALGORITHMS))
        }
    }

    @Test
    fun skillsEditScreen_cancelButtonTriggersCallback() {
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_EDIT).performClick()

        composeTestRule.onNodeWithTag(SkillsEditTestTags.CANCEL_BUTTON).performClick()

        // Verify we're back on profile screen
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).assertExists()
    }

    @Test
    fun skillsEditScreen_searchFieldAcceptsInput() {
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_SECTION).performClick()
        composeTestRule.onNodeWithTag(ProfileTestTags.SKILLS_EDIT).performClick()

        composeTestRule.onNodeWithTag(SkillsEditTestTags.SEARCH_FIELD).performClick()
        composeTestRule.onNodeWithTag(SkillsEditTestTags.SEARCH_FIELD).performTextInput("prog")

        // Verify suggestions appear
        composeTestRule.onNodeWithTag(SkillsEditTestTags.SUGGESTIONS_LIST).assertExists()
    }
}

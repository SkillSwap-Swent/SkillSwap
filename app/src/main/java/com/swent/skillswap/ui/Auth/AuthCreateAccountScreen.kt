/**
 * @author Topaze17 (Eliott) Used ChatGPT for tagging the composables and commenting, but all tags
 *   and comments were checked manually.
 */
package com.swent.skillswap.ui.Auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.utils.SkillSwapOutlinedTextField
import com.swent.skillswap.ui.utils.SkillSwapPasswordOutlinedTextField
import com.swent.skillswap.ui.utils.SkillSwapShadowButton
import com.swent.skillswap.viewModel.CreateAccountEvent
import com.swent.skillswap.viewModel.CreateAccountViewModel
import com.swent.skillswap.viewModel.CreateAccountVmFactory
import kotlin.Boolean

// ----- Tags used for UI testing -----
object CreateAccountTags {
    const val TITLE = "CREATE_TITLE"
    const val USERNAME_FIELD = "CREATE_USERNAME_FIELD"
    const val EMAIL_FIELD = "CREATE_EMAIL_FIELD"
    const val PASSWORD_FIELD = "CREATE_PASSWORD_FIELD"
    const val CONFIRM_PASSWORD_FIELD = "CREATE_CONFIRM_PASSWORD_FIELD"
    const val SKILLS_FLOW = "CREATE_SKILLS_FLOW"
    const val SKILL_CHIP_PREFIX = "CREATE_SKILL_" // final tag = SKILL_CHIP_PREFIX + skill.name
    const val ERROR = "FIELD_ERROR"
    const val NEXT_BUTTON = "NEXT_BUTTON"
}

/**
 * Defines all route constants and utility functions used for the multi-step Create Account
 * navigation flow.
 *
 * Each step of the sign-up process corresponds to one route in the [NavHost]. Includes helpers to:
 * - Determine the next step based on the current route.
 * - Compute progress bar fill percentage.
 */
object CreateAccountRoutes {
    const val USERNAME = "username"
    const val EMAIL = "email"
    const val PASSWORD = "password"
    const val SKILLS = "create"

    /**
     * Determines the next route to navigate to, depending on the current step and whether the user
     * is creating a Google-based account.
     */
    fun next(route: String?, isGoogleAccount: Boolean): String {
        return when (route) {
            USERNAME -> if (!isGoogleAccount) EMAIL else SKILLS
            EMAIL -> PASSWORD
            PASSWORD -> SKILLS
            else -> USERNAME
        }
    }

    /**
     * Returns a progress percentage (0–1f) based on which route we’re on. Used by the top progress
     * bar.
     */
    fun percentageFill(route: String?): Float {
        return when (route) {
            USERNAME -> 0.2f
            EMAIL -> 0.5f
            PASSWORD -> 0.7f
            SKILLS -> 1f
            else -> 0.2f
        }
    }
}

/**
 * Root composable for the Create Account flow.
 *
 * It contains:
 * - a top progress bar (progress based on route)
 * - a bottom navigation button (Next/Done)
 * - a nested NavHost that displays one screen per step
 * - an event collector for ViewModel navigation events
 */
@Composable
fun AuthCreateAccountScreen(
    goToMainScreen: () -> Unit = {},
    googleAccount: Boolean = FirebaseAuth.getInstance().currentUser != null,
    vm: CreateAccountViewModel = viewModel(factory = CreateAccountVmFactory(googleAccount))
) {
    // Collect one-time events emitted from the ViewModel
    LaunchedEffect(Unit) {
        vm.eventFlow.collect { event ->
            when (event) {
                is CreateAccountEvent.NavigateToMainScreen -> goToMainScreen()
            }
        }
    }
    LaunchedEffect(Unit) {
        // Immediately verify if the user should skip account creation (already signed in)
        vm.check()
    }

    val scroll = rememberScrollState()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    // Scaffold provides consistent layout (top/bottom bars)
    Scaffold(
        bottomBar = { CreateAccountBottomBar(route, navController, googleAccount, vm) },
        topBar = { CreateAccountTopBar(route) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().verticalScroll(scroll)) {
            // Navigation between form steps
            NavHost(
                navController = navController,
                startDestination = CreateAccountRoutes.USERNAME,
            ) {
                // Step 1: Username entry screen
                composable(CreateAccountRoutes.USERNAME) { UsernameScreen(vm) }
                // Step 2: Email entry screen (skipped for Google sign-ins)
                composable(CreateAccountRoutes.EMAIL) { EmailScreen(vm) }
                // Step 3: Password creation screen (skipped for Google sign-ins)
                composable(CreateAccountRoutes.PASSWORD) { PasswordScreen(vm) }
                // Step 4: Skill selection screen (final step before account creation)
                composable(CreateAccountRoutes.SKILLS) { SkillScreen(vm) }
            }
        }
    }
}

/**
 * Bottom bar that contains the “Next” button for navigation between steps, or triggers account
 * creation when the user reaches the final screen.
 */
@Composable
fun CreateAccountBottomBar(
    currRoute: String?,
    navController: NavController,
    isGoogleAccount: Boolean,
    vm: CreateAccountViewModel
) {
    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.12f)) {
        SkillSwapShadowButton(
            onClick = {
                if (
                    currRoute != CreateAccountRoutes.SKILLS && vm.validateByRoute(currRoute ?: "")
                ) {
                    // Go to next step if validation passes
                    navController.navigate(
                        CreateAccountRoutes.next(currRoute ?: "", isGoogleAccount)
                    )
                } else if (currRoute == CreateAccountRoutes.SKILLS) {
                    // Last step — create the account
                    vm.done()
                }
            },
            modifier =
                Modifier.align(Alignment.TopCenter)
                    .testTag(CreateAccountTags.NEXT_BUTTON)
                    .fillMaxWidth(0.4f)
        ) {
            Text(text = "Next", fontSize = 24.sp)
        }
    }
}

/**
 * Displays the progress bar at the top, showing how far through the account creation process the
 * user is.
 */
@Composable
fun CreateAccountTopBar(currRoute: String?) {
    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.1f)) {
        Box(
            modifier =
                Modifier.align(Alignment.BottomStart)
                    .background(MaterialTheme.colorScheme.primary)
                    .height(12.dp)
                    .fillMaxWidth(CreateAccountRoutes.percentageFill(currRoute))
        )
    }
}

/** Step 1 — Choose a username. */
@Composable
fun UsernameScreen(vm: CreateAccountViewModel) {
    val uiState by vm.uiState.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "My Username \n\nis ",
            fontSize = 44.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally).testTag(CreateAccountTags.TITLE)
        )
        Spacer(modifier = Modifier.height(100.dp))
        SkillSwapOutlinedTextField(
            value = uiState.username,
            supportText = uiState.usernameError,
            onValueChange = { vm.onUsernameChange(it) },
            label = "Username",
            placeholder = "username",
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .testTag(CreateAccountTags.USERNAME_FIELD)
        )
    }
}

/** Step 2 — Enter email address (skipped for Google sign-ins). */
@Composable
fun EmailScreen(vm: CreateAccountViewModel) {
    val uiState by vm.uiState.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "My Email is ",
            fontSize = 44.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally).testTag(CreateAccountTags.TITLE)
        )
        Spacer(modifier = Modifier.height(150.dp))
        SkillSwapOutlinedTextField(
            value = uiState.email,
            supportText = uiState.emailError,
            onValueChange = { vm.onEmailChange(it) },
            label = "Email",
            placeholder = "your.email@gmail.com",
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    capitalization = KeyboardCapitalization.None,
                    imeAction = ImeAction.Next
                ),
            modifier =
                Modifier.align(Alignment.CenterHorizontally).testTag(CreateAccountTags.EMAIL_FIELD)
        )
    }
}

/** Step 3 — Choose and confirm a password (skipped for Google sign-ins). */
@Composable
fun PasswordScreen(vm: CreateAccountViewModel) {
    val uiState by vm.uiState.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "My password is ",
            fontSize = 44.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally).testTag(CreateAccountTags.TITLE)
        )
        Spacer(modifier = Modifier.height(100.dp))
        SkillSwapPasswordOutlinedTextField(
            value = uiState.password,
            supportText = uiState.passwordError,
            label = "Password",
            placeholder = "enter password",
            onValueChange = { vm.onPasswordChange(it) },
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .testTag(CreateAccountTags.PASSWORD_FIELD)
        )
        SkillSwapPasswordOutlinedTextField(
            value = uiState.confirmPassword,
            supportText = uiState.confirmPasswordError,
            onValueChange = { vm.onConfirmPasswordChange(it) },
            label = "Confirm Password",
            placeholder = "enter password",
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .testTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
        )
    }
}

/** Step 4 — Select main skills (final step before account creation). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillScreen(vm: CreateAccountViewModel) {
    val uiState by vm.uiState.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "Pick your \n\nmain skills !",
            fontSize = 44.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally).testTag(CreateAccountTags.TITLE)
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = uiState.skillsError,
            color = Color.Red,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier =
                Modifier.fillMaxWidth(0.7f)
                    .align(Alignment.CenterHorizontally)
                    .testTag(CreateAccountTags.SKILLS_FLOW)
        ) {
            // Loop through all skill tags and render as selectable chips
            for (skill in SkillTag.entries) {
                val skillColor = if (uiState.skills.contains(skill)) Color.Red else Color.Black
                Box(
                    modifier =
                        Modifier.border(
                                width = 1.dp,
                                color = skillColor,
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { vm.clickSkill(skill) }
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                            .testTag(CreateAccountTags.SKILL_CHIP_PREFIX + skill.name)
                ) {
                    Text(
                        text = skill.name, // TODO: make enum names user-friendly
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = skillColor,
                    )
                }
            }
        }
    }
}

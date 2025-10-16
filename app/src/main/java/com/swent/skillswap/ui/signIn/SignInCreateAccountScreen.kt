/** @author Topaze17(Eliott) used chatGPT for tagging the composable but they were checked */
package com.swent.skillswap.ui.signIn

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.theme.SkillSwapAppTheme
import com.swent.skillswap.ui.utils.GradientButton
import com.swent.skillswap.ui.utils.SkillSwapPasswordTextField
import com.swent.skillswap.ui.utils.SkillSwapTextField
import com.swent.skillswap.viewModel.CreateAccountViewModel
import com.swent.skillswap.viewModel.CreateAccountVmFactory
import kotlin.Boolean

object CreateAccountTags {

    const val TITLE = "CREATE_TITLE"
    const val USERNAME_FIELD = "CREATE_USERNAME_FIELD"
    const val EMAIL_FIELD = "CREATE_EMAIL_FIELD"
    const val PASSWORD_FIELD = "CREATE_PASSWORD_FIELD"
    const val CONFIRM_PASSWORD_FIELD = "CREATE_CONFIRM_PASSWORD_FIELD"
    const val SKILLS_INPUT = "CREATE_SKILLS_INPUT"
    const val SKILLS_FLOW = "CREATE_SKILLS_FLOW"
    const val SKILL_SUGGESTION_PREFIX = "CREATE_SKILL_SUGGESTION_"
    const val SKILL_CHIP_PREFIX = "CREATE_SKILL_" // final tag = SKILL_CHIP_PREFIX + skill.name
    const val ERROR = "FIELD_ERROR"
    const val NEXT_BUTTON = "NEXT_BUTTON"
}

object CreateAccountRoutes {
    const val USERNAME = "username"
    const val EMAIL = "email"
    const val PASSWORD = "password"
    const val SKILLS = "create"

    fun next(route: String?, isGoogleAccount: Boolean): String {
        return when (route) {
            USERNAME -> if (!isGoogleAccount) EMAIL else SKILLS
            EMAIL -> PASSWORD
            PASSWORD -> SKILLS
            else -> {
                USERNAME
            }
        }
    }

    fun percentageFill(route: String?): Float {
        return when (route) {
            USERNAME -> 0.2f
            EMAIL -> 0.5f
            PASSWORD -> 0.7f
            SKILLS -> 1f
            else -> {
                0.2f
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateAccountPreview() {
    SkillSwapAppTheme(dynamicColor = false, content = { SignInCreateAccountScreen() })
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SignInCreateAccountScreen(
    goToMainScreen: () -> Unit = {},
    googleAccount: Boolean =
        false /*TODO Remove when using it in main app FirebaseAuth.getInstance().currentUser != null*/,
    vm: CreateAccountViewModel =
        viewModel(factory = CreateAccountVmFactory(goToMainScreen, googleAccount))
) {
    /*val uiState by vm.uiState.collectAsState()*/
    val scroll = rememberScrollState()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    Scaffold(
        bottomBar = { CreateAccountBottomBar(route, navController, googleAccount, vm) },
        topBar = { CreateAccountTopBar(route) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize(1f).verticalScroll(scroll)) {
            NavHost(
                navController = navController,
                startDestination = CreateAccountRoutes.USERNAME,
            ) {
                composable(CreateAccountRoutes.USERNAME) { UsernameScreen(vm) }
                composable(CreateAccountRoutes.EMAIL) { EmailScreen(vm) }
                composable(CreateAccountRoutes.PASSWORD) { PasswordScreen(vm) }
                composable(CreateAccountRoutes.SKILLS) { SkillScreen(vm) }
            }
        }
    }
}

@Composable
fun CreateAccountBottomBar(
    currRoute: String?,
    navController: NavController,
    isGoogleAccount: Boolean,
    vm: CreateAccountViewModel
) {
    Box(modifier = Modifier.fillMaxWidth(1f).fillMaxHeight(0.12f)) {
        GradientButton(
            {
                if (
                    currRoute != CreateAccountRoutes.SKILLS && vm.validateByRoute(currRoute ?: "")
                ) {
                    navController.navigate(
                        CreateAccountRoutes.next(currRoute ?: "", isGoogleAccount)
                    )
                } else if (currRoute == CreateAccountRoutes.SKILLS) {
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

@Composable
fun CreateAccountTopBar(currRoute: String?) {
    Box(modifier = Modifier.fillMaxWidth(1f).fillMaxHeight(0.1f)) {
        Box(
            modifier =
                Modifier.align(Alignment.BottomStart)
                    .background(MaterialTheme.colorScheme.primary)
                    .height(12.dp)
                    .fillMaxWidth(CreateAccountRoutes.percentageFill(currRoute))
        )
    }
}

@Composable
fun UsernameScreen(vm: CreateAccountViewModel) {
    val uiState by vm.uiState.collectAsState()
    Column(modifier = Modifier.fillMaxSize(1f)) {
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "My Username \n\nis ",
            fontSize = 44.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(100.dp))
        SkillSwapTextField(
            value = uiState.username,
            supportText = uiState.usernameError,
            onValueChange = { it -> vm.onUsernameChange(it) },
            label = "Username",
            placeholder = "username",
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .testTag(CreateAccountTags.USERNAME_FIELD)
        )
    }
}

@Composable
fun EmailScreen(vm: CreateAccountViewModel) {
    val uiState by vm.uiState.collectAsState()
    Column(modifier = Modifier.fillMaxSize(1f)) {
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "My Email is ",
            fontSize = 44.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(150.dp))
        SkillSwapTextField(
            value = uiState.email,
            supportText = uiState.emailError,
            onValueChange = { it -> vm.onEmailChange(it) },
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

@Composable
fun PasswordScreen(vm: CreateAccountViewModel) {
    val uiState by vm.uiState.collectAsState()
    Column(modifier = Modifier.fillMaxSize(1f)) {
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "My password is ",
            fontSize = 44.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(100.dp))
        SkillSwapPasswordTextField(
            value = uiState.password,
            supportText = uiState.passwordError,
            label = "Password",
            placeholder = "enter password",
            onValueChange = { it -> vm.onPasswordChange(it) },
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .testTag(CreateAccountTags.PASSWORD_FIELD)
        )
        SkillSwapPasswordTextField(
            value = uiState.confirmPassword,
            supportText = uiState.confirmPasswordError,
            onValueChange = { it -> vm.onConfirmPasswordChange(it) },
            label = "Confirm Password",
            placeholder = "enter password",
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .testTag(CreateAccountTags.CONFIRM_PASSWORD_FIELD)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillScreen(vm: CreateAccountViewModel) {
    val uiState by vm.uiState.collectAsState()
    Column(modifier = Modifier.fillMaxSize(1f)) {
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "Pick your \n\nmain skills !",
            fontSize = 44.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
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
                        text = skill.name /*TODO util function for better enum name*/,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = skillColor,
                    )
                }
            }
        }
    }
}

/**
 * @author Léonard MARTI 394185 /!\ Written with help of Copilot /!\
 * > Helped me finding the right compose functions, complete all the repetitive code (construction
 * > of instances for example)
 */
package com.swent.skillswap.ui.editUser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.swent.skillswap.ui.theme.*
import com.swent.skillswap.ui.utils.*

object EditUserTags {
    const val GO_BACK_BUTTON = "edit_user_go_back_button"
    const val USERNAME_TEXTFIELD = "edit_user_username_textfield"
    const val VALIDATE_BUTTON = "edit_user_validate_button"
    const val PROFILE_PICTURE = "edit_user_profile_picture"
    const val GENERAL_ERROR = "edit_user_general_error"
    const val SUCCESS_MESSAGE = "edit_user_success_message"
}

/** Displays the edit user screen. */
@Composable
fun EditUserScreen(
    vm: EditUserViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onSkillsPressed: () -> Unit = {}
) {
    val uiState by vm.uiState.collectAsState()
    val user = uiState.editedUser

    var username by remember { mutableStateOf(user?.username ?: "") }

    Scaffold() { paddingValues ->
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
        ) {
            Column(
                modifier =
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "Edit Profile",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                /** Profile picture */
                Box(modifier = Modifier.testTag(EditUserTags.PROFILE_PICTURE).width(180.dp)) {
                    if (user?.profilePicture != null && user.profilePicture.isNotEmpty()) {
                        AsyncImage(
                            model = user.profilePicture,
                            contentDescription = "Profil picture",
                            modifier =
                                Modifier.size(120.dp).clip(CircleShape).align(Alignment.TopCenter),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        /* No valid url for profile picture */
                        Box(
                            modifier =
                                Modifier.size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .align(Alignment.TopCenter),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default profile picture",
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    SkillSwapEditButton(
                        onClick = { /* TODO Next Sprint: Open image picker to change profile picture */},
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }

                Spacer(modifier = Modifier.weight(0.4f))

                /** Username Field */
                SkillSwapOutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        vm.setUsername(username)
                    },
                    label = "Username",
                    placeholder = "type your new username",
                    supportText =
                        if (uiState.usernameError != null) uiState.usernameError!! else "",
                    modifier = Modifier.fillMaxWidth().testTag(EditUserTags.USERNAME_TEXTFIELD)
                )

                Spacer(modifier = Modifier.weight(0.05f))

                /** General Error Message */
                if (uiState.generalError != null) {
                    Text(
                        text = uiState.generalError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier =
                            Modifier.padding(vertical = 8.dp).testTag(EditUserTags.GENERAL_ERROR)
                    )
                }

                /** Success Message */
                if (uiState.isSaved) {
                    Text(
                        text = "Profile updated successfully",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier =
                            Modifier.padding(vertical = 8.dp).testTag(EditUserTags.SUCCESS_MESSAGE)
                    )
                }

                Spacer(modifier = Modifier.weight(0.4f))

                /** Validate Button */
                SkillSwapShadowButton(
                    onClick = {
                        if (!uiState.isLoading) vm.validate()
                        else {
                            /*do nothing, validation already pending*/
                        }
                    },
                    modifier = Modifier.height(56.dp).testTag(EditUserTags.VALIDATE_BUTTON),
                ) {
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Done")
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (uiState.isLoading) "Loading..." else "Done",
                        fontSize = 16.sp,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                /** go back button* */
                SkillSwapShadowButton(
                    onClick = { onGoBack() },
                    modifier = Modifier.height(56.dp).testTag(EditUserTags.GO_BACK_BUTTON),
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (uiState.isLoading) "Loading..." else "Back",
                        fontSize = 16.sp,
                    )
                }
            }
        }
    }
}

/** Fake repository for preview purposes. */
/*
private class FakeUserRepository : UserRepositery {
    override fun getNewUid(): String = "fake-uid-123"

    override suspend fun getUser(userID: String): User {
        return User(
            uid = userID,
            username = "John Doe",
            email = "john.doe@example.com",
            profilePicture = "",
            skillSet = emptySet(),
            rating = 4.5f,
            availability = emptyList()
        )
    }

    override suspend fun addUser(user: User) {
        /* no-op */
    }

    override suspend fun editUser(userID: String, newValue: User) {
        /* no-op */
    }

    override suspend fun deleteUser(userID: String) {
        /* no-op */
    }
}

@Preview(showBackground = true)
@Composable
fun EditUserScreenPreview() {
    SkillSwapAppTheme {
        EditUserScreen(
            // vm = EditUserViewModel(repo = FakeUserRepository())
        )
    }
}
*/

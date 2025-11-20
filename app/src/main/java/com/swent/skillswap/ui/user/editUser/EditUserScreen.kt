/**
 * @author Léonard MARTI 394185 /!\ Written with help of Copilot /!\
 * > Helped me finding the right compose functions, complete all the repetitive code (construction
 * > of instances for example)
 */
package com.swent.skillswap.ui.user.editUser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.swent.skillswap.model.user.User
import com.swent.skillswap.ui.utils.*

object EditUserTags {
    const val GO_BACK_BUTTON = "edit_user_go_back_button"
    const val USERNAME_TEXTFIELD = "edit_user_username_textfield"
    const val VALIDATE_BUTTON = "edit_user_validate_button"
    const val PROFILE_PICTURE = "edit_user_profile_picture"
    const val GENERAL_ERROR = "edit_user_general_error"
    const val SUCCESS_MESSAGE = "edit_user_success_message"
    const val PROFILE_PICTURE_TEXTFIELD = "edit_user_profile_picture_textfield"
    const val PROFILE_PICTURE_CONTENT = "edit_user_profile_picture_content"
    const val DELETE_PROFILE_PICTURE = "edit_user_delete_profile_picture_button"
}

@Composable
fun EditUserScreen(
    vm: EditUserViewModel = viewModel(),
    onGoBack: () -> Unit = {},
) {
    val uiState by vm.uiState.collectAsState()
    val user = uiState.editedUser
    var username by remember { mutableStateOf(user?.username ?: "AnoUser") }
    var url by remember { mutableStateOf(user?.profilePicture ?: "") }

    DisposableEffect(Unit) { onDispose { vm.clearLoadedState() } }

    Scaffold { paddingValues ->
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
                HeaderTitle("Edit Profile")
                Spacer(modifier = Modifier.height(10.dp))

                ProfilePictureSection(
                    user = user,
                    url = url,
                    onChangeUrl = {
                        url = it
                        vm.setProfilePicture(it)
                    },
                )

                Spacer(modifier = Modifier.weight(0.4f))

                SkillSwapOutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        vm.setUsername(it)
                    },
                    label = "Username",
                    placeholder = "type your new username",
                    supportText = uiState.usernameError.orEmpty(),
                    modifier = Modifier.fillMaxWidth().testTag(EditUserTags.USERNAME_TEXTFIELD)
                )

                Spacer(modifier = Modifier.weight(0.05f))

                Button(
                    onClick = {
                        url = ""
                        vm.deleteProfilePicture()
                    },
                    modifier =
                        Modifier.fillMaxWidth(0.6f).testTag(EditUserTags.DELETE_PROFILE_PICTURE),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                ) {
                    Text(
                        text = "Delete Profile Picture",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onError
                    )
                }

                Spacer(modifier = Modifier.weight(0.05f))

                SkillSwapOutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        vm.setProfilePicture(it)
                    },
                    label = "profile picture URL",
                    placeholder = "Put your profile picture URL here",
                    supportText = uiState.profilePictureError.orEmpty(),
                    modifier =
                        Modifier.fillMaxWidth().testTag(EditUserTags.PROFILE_PICTURE_TEXTFIELD)
                )

                Spacer(modifier = Modifier.weight(0.05f))

                ErrorMessage(uiState.generalError)
                SuccessMessage(uiState.isSaved)

                Spacer(modifier = Modifier.weight(0.4f))

                ActionButtons(
                    isLoading = uiState.isLoading,
                    onValidate = { if (!uiState.isLoading) vm.validate() },
                    onGoBack = onGoBack
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeaderTitle(title: String) {
    Text(
        text = title,
        fontSize = 36.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 24.dp)
    )
}

@Composable
private fun ProfilePictureSection(
    user: User?,
    url: String,
    onChangeUrl: (String) -> Unit,
) {
    Box(modifier = Modifier.testTag(EditUserTags.PROFILE_PICTURE).width(180.dp)) {
        if (url.isNotEmpty()) {
            AsyncImage(
                model = url,
                contentDescription = "Profile picture",
                modifier =
                    Modifier.size(120.dp)
                        .clip(CircleShape)
                        .align(Alignment.TopCenter)
                        .testTag(EditUserTags.PROFILE_PICTURE_CONTENT),
                contentScale = ContentScale.Crop
            )
        } else {
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
            onClick = { /* TODO: Open image picker */},
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun ErrorMessage(error: String?) {
    error?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 8.dp).testTag(EditUserTags.GENERAL_ERROR)
        )
    }
}

@Composable
private fun SuccessMessage(isSaved: Boolean) {
    if (isSaved) {
        Text(
            text = "Profile updated successfully",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp).testTag(EditUserTags.SUCCESS_MESSAGE)
        )
    }
}

@Composable
private fun ActionButtons(isLoading: Boolean, onValidate: () -> Unit, onGoBack: () -> Unit) {
    SkillSwapShadowButton(
        onClick = onValidate,
        modifier = Modifier.height(56.dp).testTag(EditUserTags.VALIDATE_BUTTON)
    ) {
        Icon(imageVector = Icons.AutoMirrored.Default.ArrowForward, contentDescription = "Save")
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = if (isLoading) "Loading..." else "Save", fontSize = 16.sp)
    }

    SkillSwapShadowButton(
        onClick = onGoBack,
        modifier = Modifier.height(56.dp).testTag(EditUserTags.GO_BACK_BUTTON)
    ) {
        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = if (isLoading) "Loading..." else "Back", fontSize = 16.sp)
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
            profilePicture = "https://upload.wikimedia.org/wikipedia/en/thumb/9/96/Meme_Man_on_transparent_background.webp/316px-Meme_Man_on_transparent_background.webp.png",
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

    override suspend fun userExists(userId: String): Boolean {
        return true
    }
}

@Preview(showBackground = true)
@Composable
fun EditUserScreenPreview() {
    SkillSwapAppTheme {
        EditUserScreen(
             vm = EditUserViewModel(repo = FakeUserRepository())
        )
    }
}*/

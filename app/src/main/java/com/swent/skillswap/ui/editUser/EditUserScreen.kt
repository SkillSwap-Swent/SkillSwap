/**
 * @author Léonard MARTI 394185
 * /!\ Written with help of Copilot /!\
 * > Helped me finding the right compose functions complete all the repetitive code (construction of
 * > instances for example)
 */

package com.swent.skillswap.ui.editUser

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swent.skillswap.model.user.User
import com.swent.skillswap.model.user.UserRepositery
import coil.compose.AsyncImage
import com.swent.skillswap.firebase.FirestoreSettings.MAX_SEARCH_KEYS
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.signIn.CreateAccountTags
import com.swent.skillswap.ui.theme.*
import com.swent.skillswap.ui.utils.*
import com.swent.skillswap.viewModel.EditUserViewModel

/** Displays the edit user screen. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditUserScreen(
     vm: EditUserViewModel = viewModel(),
     onGoBack: () -> Unit = {},
) {
     val uiState by vm.uiState.collectAsState()
     val user = uiState.editedUser
/*
    var username by remember { mutableStateOf(user?.username ?:  "") }
    var email by remember { mutableStateOf(user?.email ?:  "") }
   */

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                navigationIcon = {
                    IconButton(
                        onClick = {onGoBack()},
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                /** Profile picture */
                Box() {
                    if (user?.profilePicture != null && user.profilePicture.isNotEmpty()) {
                        AsyncImage(
                            model = user.profilePicture,
                            contentDescription = "Profil picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        /* No valid url for profile picture */
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "edit profile picture",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }


                Spacer(modifier = Modifier.height(32.dp))

                /** Username Field */
                OutlinedTextField(
                    value = user?.username ?: "Username",
                    onValueChange = {vm.setUsername(it)},
                    label = { Text("Username") },
                    placeholder = { Text("type your new username") },
                    isError = uiState.usernameError != null,
                    supportingText = {
                        if (uiState.usernameError != null) {
                            Text(
                                uiState.usernameError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                /** Email Field */
                /** REMARK
                 * Is it really a good idea ? The User login with a certain email and get some credentials
                 * from Firebase Auth. If he changes his email here, it will desynchronize the Auth
                 * system and the Firestore database. Maybe it's better to not allow email changing from
                 * here, but rather from a dedicated "Change Email" screen that would also update the
                 * Firebase Auth email.
                 */
                OutlinedTextField(
                    value = user?.email ?: "example@email.com",
                    onValueChange = {vm.setEmail(it)},
                    label = { Text("E-mail") },
                    placeholder = { Text("type your new email") },
                    isError = uiState.emailError != null,
                    supportingText = {
                        if (uiState.emailError != null) {
                            Text(
                                uiState.emailError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                /** Skill Set Field */
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)

                ) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { /* TODO Next Sprint: Open skill selection screen */ }
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .testTag(CreateAccountTags.SKILLS_FLOW)
                                .fillMaxSize()
                                .padding(10.dp)


                        ) {
                            for (skill in uiState.editedUser?.skillSet ?: emptySet()) {
                                Box(
                                    modifier =
                                        Modifier
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(50)
                                            )
                                            .clip(RoundedCornerShape(50))
                                            .background(MaterialTheme.colorScheme.background)
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = skill.name.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modify skills",
                            modifier = Modifier.size(17.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }


                /** General Error Message */
                if (uiState.generalError != null) {
                    Text(
                        text = uiState.generalError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                /** Success Message */
                if (uiState.isSaved) { //TODO: voir quand ce truc disparait
                    Text(
                        text = "Profile updated successfully",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                /** Validate Button */
                GradientButton(
                    onClick = {  if(!uiState.isLoading)vm.validate() else {/*do nothing, validation already pending*/} },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp),
                    gradientDirection = BrushDirection.LEFT_RIGHT
                ) {
                    Text(
                        text = if (uiState.isLoading) "Loading..." else "Validate",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}


/** Fake repository for preview purposes. */
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

package com.swent.skillswap.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.swent.skillswap.model.user.Preference
import com.swent.skillswap.ui.user.ProfileTestTags.EDIT_PROFILE
import com.swent.skillswap.ui.utils.SkillSwapEditButton
import com.swent.skillswap.viewModel.ProfileViewModel

object ProfileTestTags {
    const val PROFILE_TITLE = "profile_title"

    const val EDIT_PROFILE = "edit_profile"
    const val PROFILE_PICTURE = "profile_picture"

    const val EMAIL_SECTION = "email_section"
    const val EMAIL_VALUE = "email_value"
    const val USERNAME_SECTION = "username_section"
    const val USERNAME_VALUE = "username_value"
    const val SKILLS_SECTION = "skills_section"
    const val SKILLS_COUNT = "skills_count"
    const val SKILLS_LIST = "skills_list"
    const val SKILLS_EMPTY = "skills_empty"
    const val PREFERENCES_SECTION = "preferences_section"
    const val PREF_OPTION_MONEY = "pref_option_money"
    const val PREF_OPTION_SKILLS = "pref_option_skills"
}

@Preview(showBackground = true)
@Composable
fun ProfileScreen(
    onEditProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    vm: ProfileViewModel = viewModel()
) {

    val uiState by vm.userState.collectAsState()
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scroll),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Profile Info",
            fontSize = 36.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp).testTag(ProfileTestTags.PROFILE_TITLE)
        )
        Spacer(modifier = Modifier.height(10.dp))
        // Profile picture Section
        Box(
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .height(150.dp)
                    .width(280.dp)
                    .padding(8.dp)
                    .testTag(ProfileTestTags.PROFILE_PICTURE)
        ) {
            if (uiState.profilePicture.isNotEmpty()) {
                AsyncImage(
                    model = uiState.profilePicture,
                    contentDescription = "Profile picture",
                    modifier = Modifier.size(140.dp).clip(CircleShape).align(Alignment.TopCenter),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier =
                        Modifier.size(120.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile picture placeholder",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            // Edit button overlay
            SkillSwapEditButton(
                onClick = { onEditProfileClick() },
                modifier = Modifier.align(Alignment.BottomEnd).testTag(EDIT_PROFILE)
            )
        }

        Spacer(Modifier.height(40.dp))
        Box(
            modifier =
                Modifier.fillMaxWidth(0.9f)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .padding(24.dp, 25.dp, 24.dp, 5.dp)
        ) {
            Row {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row() {
                        Icon(
                            contentDescription = "Email",
                            imageVector = Icons.Outlined.Email,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.email,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(25.dp))
                    Row() {
                        Icon(
                            contentDescription = "Username",
                            imageVector = Icons.Outlined.Person,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.username,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(25.dp))
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Row() {
                        Text(
                            text = "I prefer cash",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.fillMaxWidth(0.75f))
                        Switch(
                            checked = uiState.preference == Preference.MONEY,
                            onCheckedChange = { wantMoney ->
                                if (wantMoney)
                                    vm.updateUserAttributes(preference = Preference.MONEY)
                                else vm.updateUserAttributes(preference = Preference.SKILLS)
                            },
                            modifier = Modifier.width(52.dp),
                            colors =
                                SwitchDefaults.colors(
                                    uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    uncheckedTrackColor =
                                        MaterialTheme.colorScheme.primaryContainer,
                                    uncheckedBorderColor =
                                        MaterialTheme.colorScheme.onPrimaryContainer,
                                    checkedBorderColor =
                                        MaterialTheme.colorScheme.onPrimaryContainer,
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                            thumbContent = {}
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {},
            shape = RoundedCornerShape(26),
            contentPadding = PaddingValues(12.dp, 0.dp)
        ) {
            Icon(
                contentDescription = "Skills",
                imageVector = Icons.Outlined.BookmarkBorder,
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = "My skills")
        }
        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onLogoutClick() },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(
                text = "Logout",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onError
            )
        }
    }
}

package com.swent.skillswap.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.swent.skillswap.model.user.Preference
import com.swent.skillswap.ui.user.ProfileTestTags.EDIT_PROFILE_BUTTON
import com.swent.skillswap.ui.user.ProfileTestTags.EMAIL_VALUE
import com.swent.skillswap.ui.user.ProfileTestTags.INFO_CARD
import com.swent.skillswap.ui.user.ProfileTestTags.LOGOUT_BUTTON
import com.swent.skillswap.ui.user.ProfileTestTags.PREFERENCE_SWITCH
import com.swent.skillswap.ui.user.ProfileTestTags.PROFILE_PICTURE_IMAGE
import com.swent.skillswap.ui.user.ProfileTestTags.SKILLS_BUTTON
import com.swent.skillswap.ui.user.ProfileTestTags.USERNAME_VALUE
import com.swent.skillswap.ui.utils.SkillSwapEditButton
import com.swent.skillswap.viewModel.ProfileViewModel

object ProfileTestTags {
    const val PROFILE_TITLE = "profile_title"

    // Profile picture
    const val PROFILE_PICTURE_BOX = "profile_picture_box"
    const val PROFILE_PICTURE_IMAGE = "profile_picture_image"
    const val EDIT_PROFILE_BUTTON = "edit_profile_button"

    // Info card
    const val INFO_CARD = "profile_info_card"
    const val EMAIL_VALUE = "profile_email_value"
    const val USERNAME_VALUE = "profile_username_value"

    // Preference
    const val PREFERENCE_SWITCH = "profile_preference_switch"

    // Buttons
    const val SKILLS_BUTTON = "profile_skills_button"
    const val LOGOUT_BUTTON = "profile_logout_button"
}

@Composable
fun ProfileScreen(
    onEditProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onSkillClick: () -> Unit = {},
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
        /** Profile picture Section */
        Box(
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .height(150.dp)
                    .width(280.dp)
                    .padding(8.dp)
                    .testTag(ProfileTestTags.PROFILE_PICTURE_BOX)
        ) {
            if (uiState.profilePicture.isNotEmpty()) {
                AsyncImage(
                    model = uiState.profilePicture,
                    contentDescription = "Profile picture",
                    modifier =
                        Modifier.testTag(PROFILE_PICTURE_IMAGE)
                            .size(140.dp)
                            .clip(CircleShape)
                            .align(Alignment.TopCenter),
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
                        modifier = Modifier.size(60.dp).testTag(PROFILE_PICTURE_IMAGE),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            /** Edit button overlay */
            SkillSwapEditButton(
                onClick = { onEditProfileClick() },
                modifier = Modifier.align(Alignment.BottomEnd).testTag(EDIT_PROFILE_BUTTON)
            )
        }

        Spacer(Modifier.height(40.dp))
        /** Info card */
        Box(
            modifier =
                Modifier.fillMaxWidth(0.9f)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag(INFO_CARD)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .padding(24.dp, 25.dp, 24.dp, 5.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                /** Email field */
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.testTag(EMAIL_VALUE)
                    )
                }
                Spacer(modifier = Modifier.height(25.dp))
                /** Username field */
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.testTag(USERNAME_VALUE)
                    )
                }
                Spacer(modifier = Modifier.height(25.dp))
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                /** Preference field */
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
                            if (wantMoney) vm.updateUserAttributes(preference = Preference.MONEY)
                            else vm.updateUserAttributes(preference = Preference.SKILLS)
                        },
                        modifier = Modifier.width(52.dp).testTag(PREFERENCE_SWITCH),
                        colors =
                            SwitchDefaults.colors(
                                uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                uncheckedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                checkedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                        thumbContent = {}
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        /** Skills Button */
        Button(
            onClick = { onSkillClick() },
            shape = RoundedCornerShape(26),
            contentPadding = PaddingValues(12.dp, 0.dp),
            modifier = Modifier.testTag(SKILLS_BUTTON)
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
        /** log Out button */
        Button(
            onClick = { onLogoutClick() },
            modifier = Modifier.fillMaxWidth(0.3f).padding(top = 24.dp).testTag(LOGOUT_BUTTON),
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

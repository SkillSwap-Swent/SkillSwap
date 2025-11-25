package com.swent.skillswap.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.swent.skillswap.ui.post.RequestScreenTags
import com.swent.skillswap.ui.utils.SkillPill

object OtherUserScreenTestTags {
    const val PROFILE_TITLE = "profile_title"

    // Profile picture
    const val PROFILE_PICTURE_BOX = "profile_picture_box"
    const val PROFILE_PICTURE_IMAGE = "profile_picture_image"

    // Info card
    const val INFO_CARD = "profile_info_card"
    const val EMAIL_VALUE = "profile_email_value"
    const val USERNAME_VALUE = "profile_username_value"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OtherUserScreen(onGoBack: () -> Unit = {}, vm: OtherUserViewModel = viewModel()) {

    val uiState by vm.userState.collectAsState()
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scroll),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Profile Info",
                    modifier = Modifier.testTag(OtherUserScreenTestTags.PROFILE_TITLE)
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { onGoBack() },
                    modifier = Modifier.testTag(RequestScreenTags.BACK_BUTTON)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))
        /** Profile picture Section */
        Box(
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .height(150.dp)
                    .width(280.dp)
                    .padding(8.dp)
                    .testTag(OtherUserScreenTestTags.PROFILE_PICTURE_BOX)
        ) {
            if (uiState.profilePicture.isNotEmpty()) {
                AsyncImage(
                    model = uiState.profilePicture,
                    contentDescription = "Profile picture",
                    modifier =
                        Modifier.testTag(OtherUserScreenTestTags.PROFILE_PICTURE_IMAGE)
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
                        modifier =
                            Modifier.size(60.dp)
                                .testTag(OtherUserScreenTestTags.PROFILE_PICTURE_IMAGE),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
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
                    .testTag(OtherUserScreenTestTags.INFO_CARD)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .padding(24.dp, 25.dp, 24.dp, 5.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                /** Email field */
                Row {
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
                        modifier = Modifier.testTag(OtherUserScreenTestTags.EMAIL_VALUE)
                    )
                }
                Spacer(modifier = Modifier.height(25.dp))
                /** Username field */
                Row {
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
                        modifier = Modifier.testTag(OtherUserScreenTestTags.USERNAME_VALUE)
                    )
                }
                Spacer(modifier = Modifier.height(25.dp))
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                /** Preference field */
                Row {
                    Text(
                        text = "User prefers ${uiState.preference.string}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
        /** Skills Area */
        Spacer(Modifier.height(10.dp))
        FlowRow(
            modifier =
                Modifier.fillMaxWidth(0.9f)
                    .heightIn(max = 200.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.skillSet.forEach { skill -> SkillPill(skill = skill.name) }
        }
    }
}

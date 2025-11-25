package com.swent.skillswap.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swent.skillswap.ui.post.RequestScreenTags
import com.swent.skillswap.ui.utils.SkillPill

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
                    modifier = Modifier.testTag(ProfileTestTags.PROFILE_TITLE)
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
        ProfilePictureBox(uiState)

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
                    .testTag(ProfileTestTags.INFO_CARD)
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
                        modifier = Modifier.testTag(ProfileTestTags.EMAIL_VALUE)
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
                        modifier = Modifier.testTag(ProfileTestTags.USERNAME_VALUE)
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

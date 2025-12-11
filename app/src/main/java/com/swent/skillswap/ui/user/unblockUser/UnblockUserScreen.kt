package com.swent.skillswap.ui.user.unblockUser

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
object UnblockUserScreenTestTag{
    const val EMPTY_TEXT = "unblock_user_empty_text"
    const val PROFILE_NAME = "unblock_user_profile_name"
    const val PROFILE_AVATAR = "unblock_user_profile_avatar"
    const val PROFILE_CARD = "unblock_user_profile_card"
    const val UNBLOCK_BUTTON = "unblock_user_deny_button"
    const val UNBLOCK_TITLE = "unblock_user_unblock_title"
    const val BACK_BUTTON = "unblock_user_back_button"
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnblockUserScreen(
    viewModel: UnblockUserViewModel = viewModel(),
    onAvatarClick: (String) -> Unit,
    onGoBack: () -> Unit
) {
    val blockedUsers by viewModel.unblockCardViews.collectAsState()

    Column(Modifier.fillMaxSize()) {

        TopAppBar(
            title = { Text(modifier = Modifier.testTag(UnblockUserScreenTestTag.UNBLOCK_TITLE),
                text = "Blocked Users") },
            navigationIcon = {
                IconButton(onClick = onGoBack,
                    modifier = Modifier.testTag(UnblockUserScreenTestTag.BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back"
                    )
                }
            }
        )

        if (blockedUsers.isEmpty()) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(modifier = Modifier.testTag(UnblockUserScreenTestTag.EMPTY_TEXT),
                    text = "Empty")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(blockedUsers) { card ->
                    BlockedUserCard(
                        card = card,
                        onUnblock = { viewModel.onUnblockUserClicked(card.uid) },
                        onAvatarClick = { onAvatarClick(card.uid) }
                    )
                }
            }
        }
    }
}
@Composable
fun BlockedUserCard(
    card: UnblockCardView,
    onUnblock: () -> Unit,
    onAvatarClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize()
            .testTag(UnblockUserScreenTestTag.PROFILE_CARD),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // Avatar + Name
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Avatar
                UserAvatar(card.avatarUrl,onClick = onAvatarClick)

                Spacer(modifier = Modifier.width(12.dp))

                // Name
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleMedium
                        , modifier = Modifier.testTag(UnblockUserScreenTestTag.PROFILE_NAME)
                )
            }

            // Unblock button (cross icon)
            IconButton(onClick = onUnblock) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Unblock user",
                    modifier = Modifier.testTag(UnblockUserScreenTestTag.UNBLOCK_BUTTON)
                )
            }
        }
    }
}
@Composable
fun UserAvatar(url: String,onClick: () -> Unit) {
    if (url.isNotEmpty()) {
        AsyncImage(
            model = url,
            contentDescription = "User avatar",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .testTag(UnblockUserScreenTestTag.PROFILE_AVATAR)
        )
    } else {
        // fallback avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default avatar",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.testTag(UnblockUserScreenTestTag.PROFILE_AVATAR)
            )
        }
    }
}




package com.swent.skillswap.ui.user.unblockUser

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

/**
 * Contains [String] constants used as test tags for UI testing the Unblock User screen.
 *
 * These tags allow Compose UI tests to locate specific nodes like text, buttons, avatars, and
 * cards.
 */
object UnblockUserScreenTestTag {
    /** Test tag for the "Empty" state text when there are no blocked users. */
    const val EMPTY_TEXT = "unblock_user_empty_text"

    /** Test tag for displaying the blocked user's name in a card. */
    const val PROFILE_NAME = "unblock_user_profile_name"

    /** Test tag for displaying the blocked user's avatar in a card. */
    const val PROFILE_AVATAR = "unblock_user_profile_avatar"

    /** Test tag for the blocked user's card container. */
    const val PROFILE_CARD = "unblock_user_profile_card"

    /** Test tag for the unblock (cross) button in a blocked user's card. */
    const val UNBLOCK_BUTTON = "unblock_user_deny_button"

    /** Test tag for the top app bar title of the Unblock User screen. */
    const val UNBLOCK_TITLE = "unblock_user_unblock_title"

    /** Test tag for the back button in the top app bar. */
    const val BACK_BUTTON = "unblock_user_back_button"
}

/**
 * Composable screen displaying the list of blocked users for the current user.
 *
 * Shows a [TopAppBar] with a back button, and either an empty state or a list of
 * [BlockedUserCard]s. Users can be unblocked via the card's unblock button, or navigate to their
 * profile by clicking the avatar.
 *
 * @param viewModel [UnblockUserViewModel] providing the blocked users state and unblocking logic.
 * @param onAvatarClick Callback triggered when a user avatar is clicked, passing the user's UID.
 * @param onGoBack Callback triggered when the back button is clicked.
 * @author Joey Gugler using chatGPT
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnblockUserScreen(
    viewModel: UnblockUserViewModel = viewModel(),
    onAvatarClick: (String) -> Unit,
    onGoBack: () -> Unit
) {
    val blockedUsers by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    modifier = Modifier.testTag(UnblockUserScreenTestTag.UNBLOCK_TITLE),
                    text = "Blocked Users",
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onGoBack,
                    modifier = Modifier.testTag(UnblockUserScreenTestTag.BACK_BUTTON),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )

        if (blockedUsers.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    modifier = Modifier.testTag(UnblockUserScreenTestTag.EMPTY_TEXT),
                    text = "Empty",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(blockedUsers) { card ->
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
}

@Composable
private fun BlockedUserCard(
    card: UnblockCardView,
    onUnblock: () -> Unit,
    onAvatarClick: () -> Unit
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(0.75f)
                .padding(8.dp)
                .animateContentSize()
                .testTag(UnblockUserScreenTestTag.PROFILE_CARD),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // Avatar + Name
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Avatar
                UserAvatar(card.avatarUrl, onClick = onAvatarClick)

                Spacer(modifier = Modifier.width(12.dp))

                // Name
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.testTag(UnblockUserScreenTestTag.PROFILE_NAME),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Unblock button (cross icon)
            IconButton(onClick = onUnblock) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Unblock user",
                    modifier = Modifier.testTag(UnblockUserScreenTestTag.UNBLOCK_BUTTON),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun UserAvatar(url: String, onClick: () -> Unit) {
    if (url.isNotEmpty()) {
        AsyncImage(
            model = url,
            contentDescription = "User avatar",
            modifier =
                Modifier.size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClick)
                    .testTag(UnblockUserScreenTestTag.PROFILE_AVATAR)
        )
    } else {
        // fallback avatar
        Box(
            modifier =
                Modifier.size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClick)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default avatar",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.testTag(UnblockUserScreenTestTag.PROFILE_AVATAR)
            )
        }
    }
}

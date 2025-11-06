@file:OptIn(ExperimentalMaterial3Api::class)

package com.swent.skillswap.ui.feedScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.swent.skillswap.ui.utils.SkillSwapButtonOutline
import com.swent.skillswap.ui.utils.SkillSwapButtonShape
import com.swent.skillswap.ui.utils.SkillSwapButtonSize

/**
 * Displays the main FeedOffer screen.
 *
 * This composable observes the [FeedScreenViewModel] to render the current offer and handle user
 * interactions such as swipes or button actions.
 *
 * @param vm The [FeedScreenViewModel] that provides the UI state and handles user actions. Defaults
 *   to the ViewModel instance obtained via [viewModel].
 */
@Composable
fun FeedScreen(
    vm: FeedScreenViewModel = viewModel(),
) {
    val uiState by vm.uiState.collectAsState()
    val swipeThreshold = 50f
    val offer = uiState.current
    var showMenu by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp

    val horizontalPadding = screenWidthDp * 0.05f
    val verticalPadding = screenHeightDp * 0.03f

    Box(
        modifier =
            Modifier.fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier =
                Modifier.testTag(FeedScreenTestTags.FEED_CARD)
                    .fillMaxWidth()
                    .aspectRatio(0.8f)
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            val (x, y) = dragAmount
                            when {
                                y > swipeThreshold -> vm.next() // swipe down
                                y < -swipeThreshold -> vm.previous() // swipe up
                                x < -swipeThreshold -> vm.goToProfile(offer.authorID) // swipe left
                                x > swipeThreshold -> vm.accept(offer) // swipe right
                            }
                        }
                    },
            elevation = CardDefaults.cardElevation(8.dp),
            colors =
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)) {
                // === Header ===
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = offer.requesterAvatar,
                        contentDescription = "Requester Avatar",
                        modifier =
                            Modifier.size(screenWidthDp * 0.1f)
                                .clip(CircleShape)
                                .testTag(FeedScreenTestTags.REQUESTER_PROFILE_PICTURE)
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = offer.authorName,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.testTag(FeedScreenTestTags.REQUESTER_NAME)
                        )
                        Text(
                            text = offer.skillRequested,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag(FeedScreenTestTags.SKILL_REQUESTED)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag(FeedScreenTestTags.FEED_MENU_BUTTON)
                        ) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                        }

                        if (showMenu) {
                            FeedOfferMenu(
                                onBlockUser = { vm.blockUser(offer.authorID) },
                                onReportOffer = { vm.reportOffer(offer) },
                                onDismiss = { showMenu = false }
                            )
                        }
                    }
                }

                // === Thumbnail ===
                AsyncImage(
                    model = offer.thumbnail,
                    contentDescription = "FeedOffer Thumbnail",
                    modifier =
                        Modifier.fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .testTag(FeedScreenTestTags.FEED_THUMBNAIL)
                )

                Spacer(Modifier.height(12.dp))

                // === FeedOffer Info ===
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
                    Text(
                        text = "you will get : ${offer.skillProvided}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag(FeedScreenTestTags.SKILL_GIVE)
                    )

                    Text(
                        text = offer.specification,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.testTag(FeedScreenTestTags.SPECIFICATION_TITLE)
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = offer.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.testTag(FeedScreenTestTags.SPECIFICATION_DESCRIPTION)
                    )
                }

                // === Action Buttons ===
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SkillSwapButtonOutline(
                        labelText = "",
                        onClick = { vm.decline(offer) },
                        icon = Icons.Default.Close,
                        shape = SkillSwapButtonShape.ROUND,
                        size = SkillSwapButtonSize.S,
                        modifier = Modifier.testTag(FeedScreenTestTags.DECLINE_BUTTON)
                    )

                    SkillSwapButtonOutline(
                        labelText = "",
                        onClick = { vm.accept(offer) },
                        icon = Icons.Default.Check,
                        shape = SkillSwapButtonShape.ROUND,
                        size = SkillSwapButtonSize.S,
                        modifier = Modifier.testTag(FeedScreenTestTags.ACCEPT_BUTTON)
                    )
                }
            }
        }
    }
}

@Composable
fun FeedOfferMenu(onBlockUser: () -> Unit, onReportOffer: () -> Unit, onDismiss: () -> Unit) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = { onDismiss() },
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        DropdownMenuItem(
            text = { Text("Block User", color = MaterialTheme.colorScheme.onSurface) },
            onClick = {
                onBlockUser()
                onDismiss()
            }
        )

        DropdownMenuItem(
            text = { Text("Report Offer", color = MaterialTheme.colorScheme.onSurface) },
            onClick = {
                onReportOffer()
                onDismiss()
            }
        )
    }
}

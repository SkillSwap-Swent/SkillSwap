@file:OptIn(ExperimentalMaterial3Api::class)

package com.swent.skillswap.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.swent.skillswap.ui.utils.SkillSwapButtonOutline
import com.swent.skillswap.ui.utils.SkillSwapButtonShape
import com.swent.skillswap.ui.utils.SkillSwapButtonSize
import com.swent.skillswap.ui.utils.StarRatingBar

/**
 * Displays the main FeedPost screen.
 *
 * This composable observes the [FeedScreenViewModel] to render the current offer and handle user
 * interactions such as swipes or button actions.
 *
 * @param vm The [FeedScreenViewModel] that provides the UI state and handles user actions. Defaults
 *   to the ViewModel instance obtained via [viewModel].
 */
@Composable
fun FeedScreen(
    swipeThreshold: Float = 50f,
    vm: FeedScreenViewModel = viewModel(),
) {
    val uiState by vm.uiState.collectAsState()
    val offer = uiState
    var showMenu by remember { mutableStateOf(false) }
    var showDistanceSlider by remember { mutableStateOf(false) }
    var distance by remember { mutableFloatStateOf(0f) }
    var isLiveLocationEnabled by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp

    val horizontalPadding = screenWidthDp * 0.05f
    val verticalPadding = screenHeightDp * 0.03f
    val avatarSize = min(screenWidthDp * 0.12f, 40.dp)
    val maxThumbnailHeight = min(screenHeightDp * 0.4f, 250.dp)

    Box(
        modifier =
            Modifier.fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                .pointerInput(showDistanceSlider) {
                    if (showDistanceSlider) {
                        detectTapGestures { showDistanceSlider = false }
                    }
                },
        contentAlignment = Alignment.Center
    ) {

        //  Filter Bar
        FilterBar(
            offer = offer,
            horizontalPadding = horizontalPadding,
            distance = distance,
            onDistanceFilterClick = { showDistanceSlider = !showDistanceSlider },
            onClearFiltersClick = {
                distance = 0f
                vm.updateDistanceFilter(0f)
            },
            modifier = Modifier.align(Alignment.TopStart)
        )

        if (offer == null) {
            // === No Offer Available ===
            Column {
                Text(
                    text = "No offer available",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag(FeedScreenTestTags.NO_OFFER_TEXT)
                )

                Spacer(Modifier.height(12.dp))

                SkillSwapButtonOutline(
                    onClick = { vm.skip() },
                    labelText = "Refresh",
                    modifier = Modifier.testTag(FeedScreenTestTags.REFRESH_BUTTON),
                    icon = null,
                    enabled = true,
                    shape = SkillSwapButtonShape.ROUND,
                    size = SkillSwapButtonSize.L
                )
            }
        } else {
            // === Offer Card ===
            Card(
                modifier =
                    Modifier.testTag(FeedScreenTestTags.FEED_CARD)
                        .widthIn(max = screenWidthDp * 0.9f)
                        .heightIn(max = screenHeightDp * 0.9f)
                        .aspectRatio(0.8f)
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                val (x, y) = dragAmount
                                when {
                                    y > swipeThreshold -> vm.skip() // swipe down
                                    // TODO: previous is disable since the controller don't
                                    // implement it
                                    // y < -swipeThreshold -> vm.previous() // swipe up
                                    x < -swipeThreshold ->
                                        vm.goToProfile(offer.authorID) // swipe left
                                    x > swipeThreshold -> vm.accept(offer) // swipe right
                                }
                            }
                        },
                elevation = CardDefaults.cardElevation(8.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)) {
                    // === Header ===
                    Row(
                        modifier =
                            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = offer.requesterAvatar,
                            contentDescription = "Requester Avatar",
                            modifier =
                                Modifier.size(avatarSize)
                                    .clip(CircleShape)
                                    .clickable { vm.goToProfile(offer.authorID) }
                                    .testTag(FeedScreenTestTags.REQUESTER_PROFILE_PICTURE)
                        )

                        Spacer(Modifier.width(12.dp))

                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = offer.authorName,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.testTag(FeedScreenTestTags.REQUESTER_NAME)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                StarRatingBar(
                                    modifier = Modifier.testTag(FeedScreenTestTags.RATING),
                                    rating = uiState?.authorRating ?: 0f,
                                    size = 20
                                )
                            }
                            Text(
                                text =
                                    buildAnnotatedString {
                                        append("I want : ")

                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append(offer.skillRequested)
                                        }
                                    },
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
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menu"
                                )
                            }

                            if (showMenu) {
                                FeedOfferMenu(
                                    onBlockUser = { vm.blockUser(offer.authorID) },
                                    onReportOffer = { vm.reportPost(offer) },
                                    onDismiss = { showMenu = false }
                                )
                            }
                        }
                    }

                    // === Thumbnail ===
                    AsyncImage(
                        model = offer.thumbnail,
                        contentDescription = "FeedPost Thumbnail",
                        modifier =
                            Modifier.fillMaxWidth()
                                .heightIn(max = maxThumbnailHeight)
                                .aspectRatio(16f / 9f)
                                .testTag(FeedScreenTestTags.FEED_THUMBNAIL)
                    )

                    Spacer(Modifier.height(12.dp))

                    // === Info + Actions ===
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 10.dp)
                    ) {
                        Column(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .weight(1f)
                                    .testTag(FeedScreenTestTags.SCROLL_BOX)
                        ) {
                            Text(
                                text =
                                    buildAnnotatedString {
                                        append("You will get: ")

                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append(offer.skillProvided.toString())
                                        }
                                    },
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
                                modifier =
                                    Modifier.testTag(FeedScreenTestTags.SPECIFICATION_DESCRIPTION)
                            )
                        }

                        // === Buttons ===
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
        // Distance Filter Button - positioned at top
        if (showDistanceSlider) {
            FeedDistanceFilterButton(
                distance = distance,
                onDistanceChange = {
                    distance = it
                    vm.updateDistanceFilter(it)
                },
                modifier =
                    Modifier.align(Alignment.TopCenter)
                        .padding(top = if (offer == null) 0.dp else 8.dp),
                onLiveLocationClicked = {
                    isLiveLocationEnabled = !isLiveLocationEnabled
                    vm.toggleLiveLocation(isLiveLocationEnabled)
                },
                checked = isLiveLocationEnabled
            )
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

@Composable
fun FeedDistanceFilterButton(
    distance: Float,
    onDistanceChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onLiveLocationClicked: () -> Unit,
    checked: Boolean
) {
    Card(
        modifier = modifier.widthIn(min = 240.dp, max = 300.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${distance.toInt()} km",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag(FeedScreenTestTags.DISTANCE_VALUE_TEXT)
            )

            Spacer(Modifier.height(8.dp))

            // Live Location Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { onLiveLocationClicked() },
                    modifier = Modifier.testTag(FeedScreenTestTags.LIVE_LOCATION_CHECKBOX)
                )
                Text(text = "Use Live Location", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(12.dp))

            Slider(
                value = distance,
                onValueChange = onDistanceChange,
                valueRange = 1f..20f,
                steps = 20,
                modifier = Modifier.testTag(FeedScreenTestTags.DISTANCE_SLIDER)
            )

            Text(
                text = "1-20 km",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilterBar(
    offer: Any?,
    horizontalPadding: Dp,
    distance: Float,
    onDistanceFilterClick: () -> Unit,
    onClearFiltersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier =
            modifier
                .padding(top = if (offer == null) 0.dp else 8.dp)
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // DISTANCE FILTER BUTTON
        Button(
            onClick = onDistanceFilterClick,
            modifier = Modifier.testTag(FeedScreenTestTags.DISTANCE_FILTER_BUTTON),
            shape = MaterialTheme.shapes.extraLarge,
            colors =
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            elevation =
                ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 8.dp)
        ) {
            Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Distance filter")
            Spacer(Modifier.width(8.dp))
            Text(
                text =
                    if (distance == 0f) "Distance: No limit" else "Distance: ${distance.toInt()}km"
            )
        }

        // CLEAR FILTERS BUTTON
        Button(
            onClick = onClearFiltersClick,
            modifier = Modifier.testTag(FeedScreenTestTags.CLEAR_FILTERS_BUTTON),
            shape = MaterialTheme.shapes.extraLarge,
            colors =
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            elevation =
                ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 8.dp)
        ) {
            Text("Clear Filters")
        }
    }
}

package com.swent.skillswap.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/** Variant for avatar display */
enum class AvatarVariant {
    COMPACT,
    PROFILE
}

/**
 * Universal avatar composable used across the app.
 * - avatarUrl: nullable string. If null or blank, a placeholder is shown.
 * - variant: layout variant (COMPACT or PROFILE) which controls sizes and placeholder styling.
 * - modifier: caller-supplied modifier (keeps alignment/spacing responsibility with the caller).
 * - testTag: optional tag for testing.
 * - onClick: optional click callback. If provided, the avatar becomes clickable.
 */
@Composable
fun AvatarDisplay(
    avatarUrl: String?,
    variant: AvatarVariant = AvatarVariant.COMPACT,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: (() -> Unit)? = null
) {
    when (variant) {
        AvatarVariant.COMPACT -> {
            val imageSize = 48.dp
            if (avatarUrl.isNullOrBlank()) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Default profile picture",
                    modifier =
                        modifier
                            .size(imageSize)
                            .clip(CircleShape)
                            .then(
                                if (onClick != null) Modifier.clickable(onClick = onClick)
                                else Modifier
                            )
                            .then(
                                if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier
                            ),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Profile picture",
                    modifier =
                        modifier
                            .size(imageSize)
                            .clip(CircleShape)
                            .then(
                                if (onClick != null) Modifier.clickable(onClick = onClick)
                                else Modifier
                            )
                            .then(
                                if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier
                            ),
                    contentScale = ContentScale.Crop
                )
            }
        }
        AvatarVariant.PROFILE -> {
            val imageSize = 140.dp
            val placeholderContainerSize = 120.dp
            val iconSize = 60.dp
            if (avatarUrl.isNullOrBlank()) {
                Box(
                    modifier =
                        modifier
                            .size(placeholderContainerSize)
                            .clip(CircleShape)
                            .then(
                                if (onClick != null) Modifier.clickable(onClick = onClick)
                                else Modifier
                            )
                            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile picture placeholder",
                        modifier = Modifier.size(iconSize),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Profile picture",
                    modifier =
                        modifier
                            .size(imageSize)
                            .clip(CircleShape)
                            .then(
                                if (onClick != null) Modifier.clickable(onClick = onClick)
                                else Modifier
                            )
                            .then(
                                if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier
                            ),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

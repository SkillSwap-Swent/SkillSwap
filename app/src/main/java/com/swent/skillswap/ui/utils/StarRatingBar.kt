package com.swent.skillswap.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StarRatingBar(
    rating: Float,
    size: Int = 28,
    max: Int = 5,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        val iconSize = Modifier.size(size.dp)

        val fullStars = rating.toInt()
        val hasHalf = rating - fullStars >= 0.5f

        repeat(fullStars) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = iconSize
            )
        }

        if (hasHalf) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.StarHalf,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = iconSize
            )
        }

        repeat(max - fullStars - if (hasHalf) 1 else 0) {
            Icon(
                imageVector = Icons.Filled.StarOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = iconSize
            )
        }
    }
}

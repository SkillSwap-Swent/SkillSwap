// Source: Created by ChatGPT
package com.swent.skillswap.resources.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable

// Optional alpha for container colors
private const val container_field_alpha = 0.6f

object LoginTextFieldColors {
    val colors
        @Composable
        get() =
            TextFieldDefaults.colors(
                focusedContainerColor =
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(container_field_alpha),
                unfocusedContainerColor =
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(container_field_alpha),
                disabledContainerColor =
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(container_field_alpha),
                errorContainerColor =
                    MaterialTheme.colorScheme.errorContainer.copy(container_field_alpha),
                focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorTextColor = MaterialTheme.colorScheme.error,
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
}

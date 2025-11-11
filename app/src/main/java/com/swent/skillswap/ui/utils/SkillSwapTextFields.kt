/**
 * TextField variants used throughout SkillSwap.
 *
 * Provides two Material 3–based composables:
 * - [SkillSwapTextField]: A filled TextField with translucent container and asymmetric corners.
 * - [SkillSwapOutlinedTextField]: An OutlinedTextField with rounded corners defined by dimension
 *   resources.
 *
 * Both variants:
 * - Display contextual trailing icons based on input or error state.
 * - Support label, placeholder, and supporting (error) text.
 * - Allow an optional [leadingIcon] for contextual decoration (e.g. search, email, etc.).
 *
 * Icon states:
 * - Error → `Info` icon (colored with [MaterialTheme.colorScheme.error]).
 * - Filled → `Done` icon (indicates valid input).
 * - Empty → `Cancel` icon (indicates empty or resettable field).
 *
 * Comments drafted with ChatGPT, reviewed and validated manually. Author: Topaze17 (Eliott)
 */
package com.swent.skillswap.ui.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import com.swent.skillswap.R
import com.swent.skillswap.ui.auth.CreateAccountTags

/**
 * SkillSwap text field — **filled** variant.
 *
 * Visuals:
 * - Uses a translucent container based on [MaterialTheme.colorScheme.surfaceContainerHighest].
 * - Corner radii read from dimension resources to allow asymmetric rounding.
 *
 * Behavior:
 * - Error state triggered when [supportText] is not blank.
 * - Trailing icon shows `Info` for errors, `Done` when filled, or `Cancel` when empty.
 * - Label and icon colors adjust automatically depending on error state.
 *
 * @param modifier Optional [Modifier] for layout or styling.
 * @param value Current text value.
 * @param supportText Supporting or validation message shown below the field.
 * @param label Label displayed above the input when active or focused.
 * @param placeholder Hint text shown when [value] is empty.
 * @param leadingIcon Optional leading icon composable (e.g. search, email).
 * @param onValueChange Callback invoked when the text changes.
 * @param keyboardOptions Keyboard configuration for input type, capitalization, etc.
 * @param enabled Whether the field is enabled for input.
 */
@Composable
fun SkillSwapTextField(
    modifier: Modifier = Modifier,
    value: String = "",
    supportText: String = "",
    label: String = "",
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true
) {
    val isFill = value.isNotBlank()
    val isError = supportText.isNotBlank()

    TextField(
        value = value,
        onValueChange = { it -> onValueChange(it) },
        label = { Text(text = label) },
        singleLine = true,
        placeholder = { Text(text = placeholder) },
        isError = isError,
        supportingText = {
            Text(text = supportText, modifier = Modifier.testTag(CreateAccountTags.ERROR))
        },
        shape =
            RoundedCornerShape(
                topStart = dimensionResource(id = R.dimen.corner_radius_top),
                topEnd = dimensionResource(id = R.dimen.corner_radius_top),
                bottomStart = dimensionResource(id = R.dimen.corner_radius_bottom),
                bottomEnd = dimensionResource(id = R.dimen.corner_radius_bottom)
            ),
        keyboardOptions = keyboardOptions,
        enabled = enabled,
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor =
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(container_field_alpha),
                unfocusedContainerColor =
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(container_field_alpha),
                disabledContainerColor =
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(container_field_alpha),
                focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorTextColor = MaterialTheme.colorScheme.error,
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
        trailingIcon = {
            val image =
                when {
                    isError -> Icons.Filled.Info
                    isFill -> Icons.Outlined.Done
                    else -> Icons.Outlined.Cancel
                }
            val description =
                when {
                    isError -> "Error"
                    isFill -> "Filled"
                    else -> "Empty"
                }
            Icon(
                imageVector = image,
                contentDescription = description,
                tint =
                    if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = leadingIcon,
        modifier = modifier.fillMaxWidth(0.8f)
    )
}

/**
 * SkillSwap text field — **outlined** variant.
 *
 * Visuals:
 * - Standard [OutlinedTextField] using Material 3 outline colors.
 * - Corner radius defined by `corner_radius_outlined_text` dimension resource.
 *
 * Behavior mirrors [SkillSwapTextField]:
 * - Error state determined by [supportText].
 * - Trailing icon reflects input status (Error, Filled, Empty).
 * - Label and icon colors respond to error state.
 *
 * @param modifier Optional [Modifier] for layout or styling.
 * @param value Current text value.
 * @param supportText Supporting or validation message shown below the field.
 * @param label Label displayed above the input when active or focused.
 * @param placeholder Hint text shown when [value] is empty.
 * @param leadingIcon Optional leading icon composable.
 * @param onValueChange Callback invoked when the text changes.
 * @param keyboardOptions Keyboard configuration for input type, capitalization, etc.
 * @param enabled Whether the field is enabled for input.
 */
@Composable
fun SkillSwapOutlinedTextField(
    modifier: Modifier = Modifier,
    value: String = "",
    supportText: String = "",
    label: String = "",
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true
) {
    val isFill = value.isNotBlank()
    val isError = supportText.isNotBlank()

    OutlinedTextField(
        value = value,
        onValueChange = { it -> onValueChange(it) },
        label = {
            Text(
                text = label,
            )
        },
        singleLine = true,
        placeholder = { Text(text = placeholder) },
        supportingText = {
            Text(text = supportText, modifier = Modifier.testTag(CreateAccountTags.ERROR))
        },
        shape = MaterialTheme.shapes.extraSmall,
        keyboardOptions = keyboardOptions,
        enabled = enabled,
        isError = isError,
        trailingIcon = {
            val image =
                when {
                    isError -> Icons.Filled.Info
                    isFill -> Icons.Outlined.Done
                    else -> Icons.Outlined.Cancel
                }
            val description =
                when {
                    isError -> "Error"
                    isFill -> "Filled"
                    else -> "Empty"
                }
            Icon(
                imageVector = image,
                contentDescription = description,
                tint =
                    if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = leadingIcon,
        modifier = modifier.fillMaxWidth(0.8f)
    )
}

/**
 * Password TextField variants for SkillSwap.
 *
 * Provides two Material 3 implementations:
 * - [SkillSwapPasswordTextField]: Filled TextField with translucent container and asymmetric
 *   corners.
 * - [SkillSwapPasswordOutlinedTextField]: OutlinedTextField with rounded corners from resources.
 *
 * Both variants:
 * - Support error/success/empty trailing icon states when not focused.
 * - Show a toggle (search/close icons) while focused to simulate show/hide password behavior.
 * - Expose label, placeholder, supporting (error) text and enable/disable.
 *
 * Notes:
 * - Error state is driven by a non-blank [supportText].
 * - Success state is when [value] is non-blank and there is no error.
 * - Focus is tracked via [onFocusChanged] to switch trailing icon behavior.
 *
 * Comments drafted with ChatGPT, reviewed and validated manually. Author: Topaze17 (Eliott)
 */
package com.swent.skillswap.ui.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.swent.skillswap.R
import com.swent.skillswap.ui.auth.CreateAccountTags

// Shared keyboard options for all password fields
val PasswordKeyboardOptions =
    KeyboardOptions(
        keyboardType = KeyboardType.Password,
        autoCorrect = false,
        capitalization = KeyboardCapitalization.None,
        imeAction = ImeAction.Done
    )

// Shared leading icon for password fields
val PasswordLeadingIcon: @Composable (() -> Unit) = {
    Icon(imageVector = Icons.Outlined.Lock, contentDescription = "password")
}

// Shared visual transformation toggle helper
fun passwordVisualTransformation(showPassword: Boolean): VisualTransformation =
    if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
// The trailing Icon logic for password field
@Composable
fun TrailingIconLogic(
    showPassword: MutableState<Boolean>,
    isFocused: Boolean,
    isError: Boolean,
    isFilled: Boolean
) { // Determine trailing icon and its description from the current state:
    val (image, description) =
        if (!isFocused) {
            when {
                isError -> Icons.Filled.Info to "Error"
                isFilled -> Icons.Outlined.Done to "Filled"
                else -> Icons.Outlined.Cancel to "Empty"
            }
        } else {
            if (showPassword.value) Icons.Filled.Close to "Hide password"
            else Icons.Filled.Search to "Show password"
        }
    IconButton(onClick = { showPassword.value = !showPassword.value }, enabled = isFocused) {
        Icon(
            imageVector = image,
            contentDescription = description,
            tint =
                if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
/**
 * SkillSwap password field — **filled** variant.
 *
 * Visuals:
 * - Translucent container using `surfaceContainerHighest` (same alpha for all states).
 * - Asymmetric corners driven by dimension resources (top rounded / bottom configurable).
 *
 * Behavior:
 * - Error state is active when [supportText] is not blank; label and trailing icon reflect error
 *   color.
 * - When not focused:
 *     * Error → `Info` icon
 *     * Non-empty & no error → `Done` icon
 *     * Empty → `Cancel` icon
 * - When focused:
 *     * Trailing icon switches between `Search` (hidden) and `Close` (visible) to simulate
 *       show/hide password toggling; actual masking is controlled via [showPassword].
 *
 * Accessibility:
 * - Content descriptions change to match the current trailing icon state.
 *
 * @param modifier Optional [Modifier] for layout/styling.
 * @param value Current text value.
 * @param supportText Supporting text shown under the field (use for validation messages).
 * @param label Floating label displayed above the field.
 * @param placeholder Placeholder text shown when [value] is empty.
 * @param onValueChange Callback when the input changes.
 * @param enabled Enables or disables the field.
 */
@Composable
fun SkillSwapPasswordTextField(
    modifier: Modifier = Modifier,
    value: String = "",
    supportText: String = "",
    label: String = "",
    placeholder: String = "",
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true,
    maxSizeFilled: Float = 0.8f
) {
    val showPassword = remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val isFilled = value.isNotBlank()
    val isError = supportText.isNotBlank()

    TextField(
        value = value,
        onValueChange = { it -> onValueChange(it) },
        label = { Text(label) },
        isError = isError,
        placeholder = { Text(placeholder) },
        supportingText = {
            Text(text = supportText, modifier = Modifier.testTag(CreateAccountTags.ERROR))
        },
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor =
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(container_field_alpha),
                unfocusedContainerColor =
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(container_field_alpha),
                disabledContainerColor =
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(container_field_alpha),
                errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(container_field_alpha),
                focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorTextColor = MaterialTheme.colorScheme.error,
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
        shape =
            RoundedCornerShape(
                topStart = dimensionResource(id = R.dimen.corner_radius_top),
                topEnd = dimensionResource(id = R.dimen.corner_radius_top),
                bottomStart = dimensionResource(id = R.dimen.corner_radius_bottom),
                bottomEnd = dimensionResource(id = R.dimen.corner_radius_bottom)
            ),
        singleLine = true,
        keyboardOptions = PasswordKeyboardOptions,
        enabled = enabled,
        leadingIcon = PasswordLeadingIcon,
        visualTransformation = passwordVisualTransformation(showPassword.value),
        trailingIcon = { TrailingIconLogic(showPassword, isFocused, isError, isFilled) },
        modifier =
            modifier.fillMaxWidth(maxSizeFilled).onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
    )
}

/**
 * SkillSwap password field — **outlined** variant.
 *
 * Visuals:
 * - Standard M3 [OutlinedTextField] using theme defaults for colors.
 * - Uniform rounded corners read from dimension resources.
 *
 * Behavior mirrors [SkillSwapPasswordTextField]:
 * - Error state derives from non-blank [supportText].
 * - Trailing icon conveys error/success/empty when not focused, and toggles show/hide when focused.
 *
 * @param modifier Optional [Modifier] for layout/styling.
 * @param value Current text value.
 * @param supportText Supporting text shown under the field (use for validation messages).
 * @param label Floating label displayed above the field.
 * @param placeholder Placeholder text shown when [value] is empty.
 * @param onValueChange Callback when the input changes.
 * @param enabled Enables or disables the field.
 */
@Composable
fun SkillSwapPasswordOutlinedTextField(
    modifier: Modifier = Modifier,
    value: String = "",
    supportText: String = "",
    label: String = "",
    placeholder: String = "",
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true,
    maxSizeFilled: Float = 0.8f
) {
    val showPassword = remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val isFilled = value.isNotBlank()
    val isError = supportText.isNotBlank()

    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(label) },
        isError = isError,
        placeholder = { Text(placeholder) },
        supportingText = {
            Text(text = supportText, modifier = Modifier.testTag(CreateAccountTags.ERROR))
        },
        shape = MaterialTheme.shapes.extraSmall,
        singleLine = true,
        keyboardOptions = PasswordKeyboardOptions,
        enabled = enabled,
        leadingIcon = PasswordLeadingIcon,
        visualTransformation = passwordVisualTransformation(showPassword.value),
        trailingIcon = { TrailingIconLogic(showPassword, isFocused, isError, isFilled) },
        modifier =
            modifier.fillMaxWidth(maxSizeFilled).onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
    )
}

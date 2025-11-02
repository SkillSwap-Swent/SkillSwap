package com.swent.skillswap.ui.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import com.swent.skillswap.R
import com.swent.skillswap.ui.signIn.CreateAccountTags

@Composable
fun SkillSwapTextFieldV1(
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
        label = {
            Text(
                text = label,
                color =
                    if (!isError) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.error,
            )
        },
        singleLine = true,
        placeholder = { Text(text = placeholder) },
        isError = isError,
        supportingText = {
            Text(
                text = supportText,
                color = Color.Red,
                modifier = Modifier.testTag(CreateAccountTags.ERROR)
            )
        },
        shape =
            RoundedCornerShape(
                topStart = dimensionResource(id = R.dimen.corner_radius_top),
                topEnd = dimensionResource(id = R.dimen.corner_radius_top),
                bottomStart = dimensionResource(id = R.dimen.corner_radius_bottom),
                bottomEnd = dimensionResource(id = R.dimen.corner_radius_bottom)
            ),
        onValueChange = { it -> onValueChange(it) },
        keyboardOptions = keyboardOptions,
        enabled = enabled,
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor =
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                unfocusedContainerColor =
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                disabledContainerColor =
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
            ),
        trailingIcon = {
            val image =
                if (isError) {
                    Icons.Filled.Info
                } else if (isFill) {
                    Icons.Outlined.Done
                } else {
                    Icons.Outlined.Cancel
                }

            val description =
                if (isError) {
                    "Error"
                } else if (isFill) {
                    "Filled"
                } else {
                    "Empty"
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillSwapTextFieldV2(
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
        label = {
            Text(
                text = label,
                color =
                    if (!isError) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.error,
            )
        },
        singleLine = true,
        placeholder = { Text(text = placeholder) },
        supportingText = {
            Text(
                text = supportText,
                color = Color.Red,
                modifier = Modifier.testTag(CreateAccountTags.ERROR)
            )
        },
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_outlined_text)),
        onValueChange = { it -> onValueChange(it) },
        keyboardOptions = keyboardOptions,
        enabled = enabled,
        isError = isError,
        colors = TextFieldDefaults.outlinedTextFieldColors(),
        trailingIcon = {
            val image =
                if (isError) {
                    Icons.Filled.Info
                } else if (isFill) {
                    Icons.Outlined.Done
                } else {
                    Icons.Outlined.Cancel
                }

            val description =
                if (isError) {
                    "Error"
                } else if (isFill) {
                    "Filled"
                } else {
                    "Empty"
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

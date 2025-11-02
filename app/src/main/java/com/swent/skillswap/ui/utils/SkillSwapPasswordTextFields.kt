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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import com.swent.skillswap.ui.signIn.CreateAccountTags

@Composable
fun SkillSwapPasswordTextFieldV1(
    modifier: Modifier = Modifier,
    value: String = "",
    supportText: String = "",
    label: String = "",
    placeholder: String = "",
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true,
) {
    var showPassword by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val isFill = value.isNotBlank()
    val isError = supportText.isNotBlank()
    TextField(
        value = value,
        onValueChange = { it -> onValueChange(it) },
        label = {
            Text(
                label,
                color =
                    if (!isError) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.error,
            )
        },
        isError = isError,
        placeholder = { Text(placeholder) },
        supportingText = {
            Text(
                text = supportText,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag(CreateAccountTags.ERROR)
            )
        },
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor =
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                unfocusedContainerColor =
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                disabledContainerColor =
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
            ),
        shape =
            RoundedCornerShape(
                topStart = dimensionResource(id = R.dimen.corner_radius_top),
                topEnd = dimensionResource(id = R.dimen.corner_radius_top),
                bottomStart = dimensionResource(id = R.dimen.corner_radius_bottom),
                bottomEnd = dimensionResource(id = R.dimen.corner_radius_bottom)
            ),
        singleLine = true,
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Password,
                autoCorrect = false,
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Done
            ),
        enabled = enabled,
        leadingIcon = { Icon(imageVector = Icons.Outlined.Lock, contentDescription = "password") },
        visualTransformation =
            if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image =
                if (isError && !isFocused) {
                    Icons.Filled.Info
                } else if (isFill && !isFocused) {
                    Icons.Outlined.Done
                } else if (!isFocused) {
                    Icons.Outlined.Cancel
                } else {
                    if (showPassword) Icons.Filled.Close else Icons.Filled.Search
                }

            val description =
                if (isError && !isFocused) {
                    "Error"
                } else if (isFill && !isFocused) {
                    "Filled"
                } else if (!isFocused) {
                    "Empty"
                } else {
                    if (showPassword) "Hide password" else "Show password"
                }

            IconButton(onClick = { showPassword = !showPassword }, enabled = isFocused) {
                Icon(
                    imageVector = image,
                    contentDescription = description,
                    tint =
                        if (isError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier =
            modifier.fillMaxWidth(0.8f).onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillSwapPasswordTextFieldV2(
    modifier: Modifier = Modifier,
    value: String = "",
    supportText: String = "",
    label: String = "",
    placeholder: String = "",
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true,
) {
    var showPassword by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val isFill = value.isNotBlank()
    val isError = supportText.isNotBlank()
    OutlinedTextField(
        value = value,
        onValueChange = { it -> onValueChange(it) },
        label = {
            Text(
                label,
                color =
                    if (!isError) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.error,
            )
        },
        isError = isError,
        placeholder = { Text(placeholder) },
        supportingText = {
            Text(
                text = supportText,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag(CreateAccountTags.ERROR)
            )
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_outlined_text)),
        singleLine = true,
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Password,
                autoCorrect = false,
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Done
            ),
        enabled = enabled,
        leadingIcon = { Icon(imageVector = Icons.Outlined.Lock, contentDescription = "password") },
        visualTransformation =
            if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image =
                if (isError && !isFocused) {
                    Icons.Filled.Info
                } else if (isFill && !isFocused) {
                    Icons.Outlined.Done
                } else if (!isFocused) {
                    Icons.Outlined.Cancel
                } else {
                    if (showPassword) Icons.Filled.Close else Icons.Filled.Search
                }

            val description =
                if (isError && !isFocused) {
                    "Error"
                } else if (isFill && !isFocused) {
                    "Filled"
                } else if (!isFocused) {
                    "Empty"
                } else {
                    if (showPassword) "Hide password" else "Show password"
                }

            IconButton(onClick = { showPassword = !showPassword }, enabled = isFocused) {
                Icon(
                    imageVector = image,
                    contentDescription = description,
                    tint =
                        if (isError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier =
            modifier.fillMaxWidth(0.8f).onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
    )
}

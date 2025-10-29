package com.swent.skillswap.ui.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.swent.skillswap.ui.signIn.CreateAccountTags

@Composable
fun SkillSwapPasswordTextField(
    modifier: Modifier = Modifier,
    value: String = "",
    supportText: String = "",
    label: String = "",
    placeholder: String = "",
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true,
) {
    var showPassword by remember { mutableStateOf(false) }
    TextField(
        value = value,
        onValueChange = { it -> onValueChange(it) },
        label = {
            Text(
                label,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        placeholder = {
            Text(placeholder, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
        },
        supportingText = {
            Text(
                text = supportText,
                color = Color.Red,
                modifier = Modifier.testTag(CreateAccountTags.ERROR)
            )
        },
        singleLine = true,
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Password,
                autoCorrect = false,
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Done
            ),
        enabled = enabled,
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledTextColor = Color.Transparent,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                disabledIndicatorColor = MaterialTheme.colorScheme.primary
            ),
        visualTransformation =
            if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (showPassword) Icons.Filled.Close else Icons.Filled.Search

            val description = if (showPassword) "Hide password" else "Show password"

            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(imageVector = image, contentDescription = description)
            }
        },
        modifier = modifier.fillMaxWidth(0.8f)
    )
}

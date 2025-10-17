package com.swent.skillswap.ui.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.swent.skillswap.ui.signIn.CreateAccountTags
import com.swent.skillswap.ui.theme.SkillSwapLightThemePrimary

@Preview
@Composable
fun SkillSwapTextField(
    modifier: Modifier = Modifier,
    value: String = "",
    supportText: String = "",
    label: String = "",
    placeholder: String = "",
    onValueChange: (String) -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true
) {
    TextField(
        value = value,
        label = {
            Text(
                text = label,
                color = SkillSwapLightThemePrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        singleLine = true,
        placeholder = {
            Text(text = placeholder, color = SkillSwapLightThemePrimary, fontSize = 18.sp)
        },
        supportingText = {
            Text(
                text = supportText,
                color = Color.Red,
                modifier = Modifier.testTag(CreateAccountTags.ERROR)
            )
        },
        onValueChange = { it -> onValueChange(it) },
        keyboardOptions = keyboardOptions,
        enabled = enabled,
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledTextColor = Color.Transparent,
                unfocusedIndicatorColor = SkillSwapLightThemePrimary,
                focusedIndicatorColor = SkillSwapLightThemePrimary,
                disabledIndicatorColor = SkillSwapLightThemePrimary
            ),
        modifier = modifier.fillMaxWidth(0.8f)
    )
}

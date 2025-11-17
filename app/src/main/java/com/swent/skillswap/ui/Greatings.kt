package com.swent.skillswap.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.swent.skillswap.ui.user.ProfileTestTags

@Composable
fun Greatings() {
    Text(text = "hello", modifier = Modifier.testTag(ProfileTestTags.PROFILE_TITLE))
}

package com.swent.skillswap.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.model.user.Skill
import com.swent.skillswap.ui.editUser.EditUserViewModel
import com.swent.skillswap.ui.utils.SkillSwapShadowButton

object SkillsEditTestTags {
    const val SCREEN_CONTAINER = "skills_edit_screen_container"
    const val TITLE = "skills_edit_title"

    const val SEARCH_FIELD = "skills_search_field"
    const val DROPDOWN = "skills_dropdown"
    const val SUGGESTIONS_LIST = "skills_suggestions_list"
    const val SUGGESTION_ITEM_PREFIX = "skills_suggestion"

    const val SELECTED_COUNT = "skills_selected_count"
    const val SELECTED_LIST = "skills_selected_list"
    const val SKILL_CHIP_PREFIX = "skills_chip"

    const val CANCEL_BUTTON = "skills_cancel_button"
    const val SAVE_BUTTON = "skills_save_button"
}

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SkillsEditScreen(vm: EditUserViewModel = viewModel(), onBackClick: () -> Unit = {}) {
    val userState by vm.uiState.collectAsState()
    Column(modifier = Modifier.fillMaxSize(1f)) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(text = "Your Skills",
            fontSize = 36.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

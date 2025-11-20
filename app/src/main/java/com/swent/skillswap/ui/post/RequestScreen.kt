/* With the help of Claude 4.5 Sonnet to:
    - Adapt the Tag input component from the CreateAccountScreen
    - Create the preview composable
    - Adding test tags
    - Some other repetitive tasks
*/

package com.swent.skillswap.ui.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swent.skillswap.firebase.FirestoreSettings.MAX_SEARCH_KEYS
import com.swent.skillswap.model.post.FakePostRepository
import com.swent.skillswap.model.post.PaymentMethod
import com.swent.skillswap.model.post.PostRepository
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.resources.theme.SkillSwapAppTheme
import com.swent.skillswap.ui.utils.GradientButton

object RequestScreenTags {
    const val BACK_BUTTON = "backButton"
    const val TITLE_INPUT = "titleInput"
    const val DESCRIPTION_INPUT = "descriptionInput"
    const val TAGS_INPUT = "tagsInput"
    const val TAG_CHIP = "tagChip"
    const val TAG_SUGGESTION = "tagSuggestion"
    const val PAYMENT_METHOD_CHIP = "paymentMethodChip"
    const val CREATE_BUTTON = "createButton"
    const val EDIT_BUTTON = "editButton"
    const val ERROR_MESSAGE = "errorMessage"
    const val LOADING_INDICATOR = "loadingIndicator"
}

/*
    This screen is intended to be shared between request creation and edition:
    // For ADD operation:
    RequestScreen(
        postRepository = postRepository,
        currentUserId = currentUserId,
        postOperation = PostOperation.ADD
    )

    // For EDIT operation:
    RequestScreen(
        postRepository = postRepository,
        currentUserId = currentUserId,
        postOperation = PostOperation.EDIT,
        uid = "the-post-id-to-edit"
    )
*/
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RequestScreen(
    postRepository: PostRepository,
    currentUserId: String,
    uid: String? = null,
    requestViewModel: RequestViewModel =
        viewModel(
            factory =
                RequestViewModelFactory(
                    postRepository = postRepository,
                    currentUserId = currentUserId,
                    postId = uid
                )
        ),
    onGoBack: () -> Unit = {},
    onPostCreated: () -> Unit = {},
    postOperation: PostOperation,
) {
    val uiState by requestViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSubmitSuccessful) { if (uiState.isSubmitSuccessful) onPostCreated() }

    Column(modifier = Modifier.fillMaxSize()) {
        RequestTopBar(postOperation = postOperation, onGoBack = onGoBack)

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TitleInput(uiState.title, uiState.titleError) { requestViewModel.setTitle(it) }
            DescriptionInput(uiState.description, uiState.descriptionError) {
                requestViewModel.setDescription(it)
            }
            TagInputSection(
                uiState.skills.toList(),
                uiState.tagsError,
                { requestViewModel.addTag(it) },
                { requestViewModel.removeTag(it) }
            )
            PaymentMethodSelection(uiState.paymentMethod) {
                requestViewModel.togglePaymentMethod(it)
            }
            SubmitButton(uiState.isLoading, postOperation) { requestViewModel.save(postOperation) }
            SubmitError(uiState.submitError)
        }
    }
}

// Top App Bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequestTopBar(postOperation: PostOperation, onGoBack: () -> Unit) {
    TopAppBar(
        title = { Text(postOperation.toTitle() + " Request") },
        navigationIcon = {
            IconButton(
                onClick = onGoBack,
                modifier = Modifier.testTag(RequestScreenTags.BACK_BUTTON)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}

// Title Input
@Composable
private fun TitleInput(value: String, error: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Title") },
        placeholder = { Text("Enter the title of your request") },
        isError = error.isNotEmpty(),
        supportingText = {
            if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
        },
        modifier = Modifier.fillMaxWidth().testTag(RequestScreenTags.TITLE_INPUT)
    )
}

// Description Input
@Composable
private fun DescriptionInput(value: String, error: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Description") },
        placeholder = { Text("Describe the skill you are requesting") },
        isError = error.isNotEmpty(),
        supportingText = {
            if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
        },
        modifier = Modifier.fillMaxWidth().testTag(RequestScreenTags.DESCRIPTION_INPUT)
    )
}

// Tags Input Section
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagInputSection(
    selectedTags: List<SkillTag>,
    error: String,
    onAddTag: (SkillTag) -> Unit,
    onRemoveTag: (SkillTag) -> Unit
) {
    var tagsQuery by remember { mutableStateOf("") }
    var tagsExpanded by remember { mutableStateOf(false) }
    var tagsHasFocus by remember { mutableStateOf(false) }

    val suggestions =
        remember(tagsQuery) {
            SkillTag.entries
                .filter { it.name.contains(tagsQuery, ignoreCase = true) && it !in selectedTags }
                .take(MAX_SEARCH_KEYS)
        }

    Column {
        OutlinedTextField(
            value = tagsQuery,
            onValueChange = {
                tagsQuery = it
                tagsExpanded = it.isNotBlank()
            },
            label = { Text("Tags") },
            placeholder = { Text("Search and add skill tags") },
            isError = error.isNotEmpty(),
            supportingText = {
                if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
            },
            modifier =
                Modifier.fillMaxWidth()
                    .onFocusChanged {
                        tagsHasFocus = it.isFocused
                        if (it.isFocused && tagsQuery.isNotBlank()) tagsExpanded = true
                    }
                    .testTag(RequestScreenTags.TAGS_INPUT)
        )

        androidx.compose.material3.DropdownMenu(
            expanded = tagsExpanded && tagsHasFocus && suggestions.isNotEmpty(),
            onDismissRequest = { tagsExpanded = false },
            properties = PopupProperties(focusable = false),
            modifier = Modifier.fillMaxWidth()
        ) {
            suggestions.forEach { tag ->
                DropdownMenuItem(
                    text = {
                        Text(
                            tag.name.replace("_", " "),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = {
                        onAddTag(tag)
                        tagsQuery = ""
                        tagsExpanded = false
                    },
                    modifier = Modifier.testTag("${RequestScreenTags.TAG_SUGGESTION}_${tag.name}")
                )
            }
        }

        // Display selected tags as chips
        Box(modifier = Modifier.height(100.dp).fillMaxWidth()) {
            val scroll = rememberScrollState()
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.verticalScroll(scroll)
            ) {
                selectedTags.forEach { tag ->
                    Box(
                        modifier =
                            Modifier.background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onRemoveTag(tag) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("${RequestScreenTags.TAG_CHIP}_$tag")
                    ) {
                        Text(
                            tag.name.replace("_", " "),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

// Payment Method Selection
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PaymentMethodSelection(selected: PaymentMethod, onSelect: (PaymentMethod) -> Unit) {
    Text(
        text = "Payment Methods",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp)
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        PaymentMethod.entries.forEach { method ->
            val isSelected = selected == method
            val backgroundColor =
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            val textColor =
                if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            Box(
                modifier =
                    Modifier.background(backgroundColor, RoundedCornerShape(16.dp))
                        .clickable { onSelect(method) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("${RequestScreenTags.PAYMENT_METHOD_CHIP}_${method.name}")
            ) {
                Text(
                    text = method.displayName,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = textColor
                )
            }
        }
    }
}

// Submit Button
@Composable
private fun SubmitButton(isLoading: Boolean, postOperation: PostOperation, onClick: () -> Unit) {
    GradientButton(
        onClick = onClick,
        enabled = !isLoading,
        modifier =
            Modifier.fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag(
                    if (postOperation == PostOperation.ADD) RequestScreenTags.CREATE_BUTTON
                    else RequestScreenTags.EDIT_BUTTON
                )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp).testTag(RequestScreenTags.LOADING_INDICATOR),
                color = Color.White
            )
        } else {
            Text(text = "Submit", fontSize = 18.sp)
        }
    }
}

// Submission Error
@Composable
private fun SubmitError(error: String?) {
    error?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp).testTag(RequestScreenTags.ERROR_MESSAGE)
        )
    }
}
// NOSONAR_START
@Preview(showBackground = true)
@Composable
fun NewRequestScreenPreview() {
    // Create a fake repository for preview
    val fakeRepository = FakePostRepository()

    val viewModel = RequestViewModel(fakeRepository, currentUserId = "preview-user", postId = null)

    SkillSwapAppTheme {
        RequestScreen(
            postRepository = fakeRepository,
            currentUserId = "preview-user",
            requestViewModel = viewModel,
            postOperation = PostOperation.EDIT
        )
    }
}
// NOSONAR_END

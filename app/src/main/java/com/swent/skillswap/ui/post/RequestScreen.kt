/* With the help of Claude 4.5 Sonnet to:
    - Adapt the Tag input component from the CreateAccountScreen
    - Create the preview composable
    - Adding test tags
    - Some other repetitive tasks
*/

package com.swent.skillswap.ui.post

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.swent.skillswap.firebase.FirestoreSettings
import com.swent.skillswap.firebase.FirestoreSettings.MAX_SEARCH_KEYS
import com.swent.skillswap.model.post.FakePostRepository
import com.swent.skillswap.model.post.PaymentMethod
import com.swent.skillswap.model.post.PostRepository
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.resources.theme.SkillSwapAppTheme
import com.swent.skillswap.ui.utils.SkillSwapShadowButton

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
    const val CHOOSE_ATTACHMENT_BUTTON = "chooseAttachmentButton"
    const val ATTACHMENT_PREVIEW = "attachmentPreview"
    const val ATTACHMENT_ERROR = "attachmentError"
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
                    appContext = LocalContext.current.applicationContext,
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
    val pickMultipleMedia =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(FirestoreSettings.MAX_ATTACHMENTS)
        ) { uris ->
            requestViewModel.addAttachments(uris)
        }

    LaunchedEffect(uiState.isSubmitSuccessful) {
        if (uiState.isSubmitSuccessful) {
            onPostCreated()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(postOperation.toTitle() + " Request") },
            navigationIcon = {
                IconButton(
                    onClick = { onGoBack() },
                    modifier = Modifier.testTag(RequestScreenTags.BACK_BUTTON)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                // Title Input
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { requestViewModel.setTitle(it) },
                    label = { Text("Title") },
                    placeholder = { Text("Enter the title of your request") },
                    isError = uiState.titleError.isNotEmpty(),
                    supportingText = {
                        if (uiState.titleError.isNotEmpty()) {
                            Text(uiState.titleError, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag(RequestScreenTags.TITLE_INPUT)
                )
            }

            item {
                // Description Input
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { requestViewModel.setDescription(it) },
                    label = { Text("Description") },
                    placeholder = { Text("Describe the skill you are requesting") },
                    isError = uiState.descriptionError.isNotEmpty(),
                    supportingText = {
                        if (uiState.descriptionError.isNotEmpty()) {
                            Text(uiState.descriptionError, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag(RequestScreenTags.DESCRIPTION_INPUT)
                )
            }

            item {
                /* Tag input. The following is heavily inspired by the implementation in the create account screen. */
                var tagsExpanded by remember { mutableStateOf(false) }
                val tagsQuery = remember { mutableStateOf("") }
                var tagsHasFocus by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                    val tagSuggestions =
                        remember(tagsQuery.value) {
                            SkillTag.entries
                                .filter {
                                    tagsQuery.value.isNotBlank() &&
                                        it.name.contains(tagsQuery.value, ignoreCase = true) &&
                                        it !in uiState.tags // Exclude already selected tags
                                }
                                .take(MAX_SEARCH_KEYS)
                        }

                    Column {
                        OutlinedTextField(
                            value = tagsQuery.value,
                            onValueChange = {
                                tagsQuery.value = it
                                if (it.isNotBlank()) {
                                    tagsExpanded = true
                                }
                            },
                            label = { Text("Tags") },
                            placeholder = { Text("Search and add skill tags") },
                            isError = uiState.tagsError.isNotEmpty(),
                            supportingText = {
                                if (uiState.tagsError.isNotEmpty()) {
                                    Text(uiState.tagsError, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier =
                                Modifier.fillMaxWidth()
                                    .onFocusChanged {
                                        tagsHasFocus = it.isFocused
                                        if (it.isFocused && tagsQuery.value.isNotBlank()) {
                                            tagsExpanded = true
                                        }
                                    }
                                    .testTag(RequestScreenTags.TAGS_INPUT)
                        )

                        androidx.compose.material3.DropdownMenu(
                            expanded = tagsExpanded && tagsHasFocus && tagSuggestions.isNotEmpty(),
                            onDismissRequest = { tagsExpanded = false },
                            properties =
                                PopupProperties(focusable = false), // KEY: This prevents focus loss
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tagSuggestions.forEach { tag ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            tag.name.replace("_", " "),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    onClick = {
                                        requestViewModel.addTag(tag)
                                        tagsQuery.value = ""
                                        tagsExpanded = false
                                    },
                                    modifier =
                                        Modifier.testTag(
                                            "${RequestScreenTags.TAG_SUGGESTION}_${tag.name}"
                                        )
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Display selected tags as chips
                Box(modifier = Modifier.height(100.dp).fillMaxWidth()) {
                    val flowScroll = rememberScrollState()
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.verticalScroll(flowScroll)
                    ) {
                        uiState.tags.forEach { tag ->
                            Box(
                                modifier =
                                    Modifier.background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable { requestViewModel.removeTag(tag) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("${RequestScreenTags.TAG_CHIP}_${tag}")
                            ) {
                                Text(
                                    text =
                                        (tag as? SkillTag)?.name?.replace("_", " ")
                                            ?: tag.toString(),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Add a photo
                Text(
                    text =
                        "Photos: ${uiState.attachments.size}/${FirestoreSettings.MAX_ATTACHMENTS}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (uiState.attachments.isNotEmpty()) {
                    Row(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(top = 8.dp)
                                .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.attachments.forEach { uri ->
                            Box(
                                modifier =
                                    Modifier.size(80.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            requestViewModel.removeAttachments(setOf(uri))
                                        }
                                        .testTag("${RequestScreenTags.ATTACHMENT_PREVIEW}_${uri}")
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Selected photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        }
                    }
                }

                if (uiState.attachmentsError.isNotBlank()) {
                    Text(
                        text = uiState.attachmentsError,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag(RequestScreenTags.ATTACHMENT_ERROR)
                    )
                }
            }

            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    SkillSwapShadowButton(
                        onClick = {
                            pickMultipleMedia.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier =
                            Modifier.testTag(RequestScreenTags.CHOOSE_ATTACHMENT_BUTTON)
                                .fillMaxWidth(0.4f)
                                .height(55.dp)
                    ) {
                        Text(text = "Add photos")
                    }
                }
            }

            item {
                // Payment method input
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
                        val isSelected = uiState.paymentMethod == method
                        val backgroundColor =
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant

                        val textColor =
                            if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier =
                                Modifier.background(
                                        color = backgroundColor,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { requestViewModel.togglePaymentMethod(method) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .testTag(
                                        "${RequestScreenTags.PAYMENT_METHOD_CHIP}_${method.name}"
                                    )
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

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item(key = "createButtonItem") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .testTag(RequestScreenTags.CREATE_BUTTON)   // <-- now on the item
                ) {
                    SkillSwapShadowButton(
                        onClick = { requestViewModel.save(postOperation) },
                        enable = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                                    .testTag(RequestScreenTags.LOADING_INDICATOR),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text("Submit", fontSize = 18.sp)
                        }
                    }
                }
            }

            item {
                // Show error if submission failed
                if (uiState.submitError != null) {
                    Text(
                        text = uiState.submitError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier =
                            Modifier.padding(top = 8.dp).testTag(RequestScreenTags.ERROR_MESSAGE)
                    )
                }
            }
        }
    }
}
// NOSONAR_START
@Preview(showBackground = true)
@Composable
fun NewRequestScreenPreview() {
    // Create a fake repository for preview
    val fakeRepository = FakePostRepository()

    val viewModel =
        RequestViewModel(null, fakeRepository, currentUserId = "preview-user", postId = null)

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

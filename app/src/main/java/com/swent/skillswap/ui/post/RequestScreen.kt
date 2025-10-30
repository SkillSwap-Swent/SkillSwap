/* With the help of Claude 4.5 Sonnet to:
    - Adapt the Tag input component from the CreateAccountScreen
    - Create the preview composable
    - Adding test tags
    - Some other repetitive tasks
*/

package com.swent.skillswap.ui.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.swent.skillswap.model.post.PaymentMethod
import com.swent.skillswap.model.post.PostRepository
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.theme.SkillSwapAppTheme
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

    LaunchedEffect(uiState.isSubmitSuccessful) {
        if (uiState.isSubmitSuccessful) {
            onPostCreated()
        }
    }

    Scaffold(
        topBar = {
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
        },
        content = { paddingValues ->
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

                /* Tag input. The following is heavily inspired by the implementation in the create account screen. */
                var tagsExpanded by remember { mutableStateOf(false) }
                val tagsQuery = remember { mutableStateOf("") }
                var tagsHasFocus by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = tagsExpanded,
                    onExpandedChange = { tagsExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val tagSuggestions =
                        remember(tagsQuery.value) {
                            SkillTag.entries
                                .filter {
                                    tagsQuery.value.isNotBlank() &&
                                        it.name.contains(tagsQuery.value, ignoreCase = true)
                                }
                                .take(MAX_SEARCH_KEYS)
                        }

                    // Could be using SkillSwapTextField for consistency
                    OutlinedTextField(
                        value = tagsQuery.value,
                        onValueChange = { tagsQuery.value = it },
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
                                .menuAnchor()
                                .onFocusChanged { tagsHasFocus = it.isFocused }
                                .testTag(RequestScreenTags.TAGS_INPUT)
                    )

                    DropdownMenu(
                        expanded = tagsExpanded && tagsHasFocus && tagSuggestions.isNotEmpty(),
                        onDismissRequest = { tagsExpanded = false },
                        properties = PopupProperties(focusable = false),
                        modifier = Modifier.fillMaxWidth().focusable(false)
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
                                },
                                modifier =
                                    Modifier.testTag(
                                        "${RequestScreenTags.TAG_SUGGESTION}_${tag.name}"
                                    )
                            )
                        }
                    }
                }

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
                        val isSelected = uiState.paymentMethods.contains(method)
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
                                text = method.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = textColor
                            )
                        }
                    }
                }

                // Submit button at bottom
                Spacer(modifier = Modifier.height(16.dp))
                GradientButton(
                    onClick = { requestViewModel.save(postOperation) },
                    enabled = !uiState.isLoading,
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .testTag(
                                when (postOperation) {
                                    PostOperation.ADD -> RequestScreenTags.CREATE_BUTTON
                                    PostOperation.EDIT -> RequestScreenTags.EDIT_BUTTON
                                }
                            )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(24.dp).testTag(RequestScreenTags.LOADING_INDICATOR),
                            color = Color.White
                        )
                    } else {
                        Text(text = "Submit", fontSize = 18.sp)
                    }
                }
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
    )
}

@Preview(showBackground = true)
@Composable
fun NewRequestScreenPreview() {
    // Create a fake repository for preview
    val fakeRepository =
        object : com.swent.skillswap.model.post.PostRepository {
            override fun getNewUid(type: com.swent.skillswap.model.post.PostType): String =
                "preview-uid"

            override suspend fun getMultiplePosts(
                numberOfPosts: Long,
                type: com.swent.skillswap.model.post.PostType,
                titleContains: String,
                ownerId: String,
                paymentMethods: List<PaymentMethod>,
                tags: List<com.swent.skillswap.model.tags.EveryTag>,
                status: com.swent.skillswap.model.post.PostStatus?
            ): List<com.swent.skillswap.model.post.Post> = emptyList()

            override suspend fun getPost(
                type: com.swent.skillswap.model.post.PostType,
                postId: String
            ): com.swent.skillswap.model.post.Post {
                throw NotImplementedError()
            }

            override suspend fun addPost(post: com.swent.skillswap.model.post.Post) {
                // Do nothing in preview
            }

            override suspend fun editPost(
                postId: String,
                newPost: com.swent.skillswap.model.post.Post
            ) {
                // Do nothing in preview
            }

            override suspend fun deletePost(
                type: com.swent.skillswap.model.post.PostType,
                postId: String
            ) {
                // Do nothing in preview
            }
        }

    val viewModel = RequestViewModel(fakeRepository, currentUserId = "preview-user", postId = null)

    SkillSwapAppTheme {
        RequestScreen(
            postRepository = fakeRepository,
            currentUserId = "preview-user",
            requestViewModel = viewModel,
            postOperation = PostOperation.ADD
        )
    }
}

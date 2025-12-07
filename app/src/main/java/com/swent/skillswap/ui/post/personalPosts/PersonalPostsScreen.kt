/** @author Younes Belgroune - Made with the help of AI */
package com.swent.skillswap.ui.post.personalPosts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.swent.skillswap.model.post.PaymentMethod
import com.swent.skillswap.model.post.Post
import com.swent.skillswap.model.post.PostFirestoreRepository
import com.swent.skillswap.model.post.PostReply
import com.swent.skillswap.model.post.PostStatus
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.post.ReplyStatus
import com.swent.skillswap.model.post.Request
import com.swent.skillswap.model.tags.PostTag
import com.swent.skillswap.model.tags.SkillTag
import com.swent.skillswap.ui.utils.SkillPill
import java.util.Date

object PersonalPostsScreenTags {
    const val SCREEN = "personal_posts_screen"
    const val TITLE = "personal_posts_title"
    const val FILTER_ALL = "filter_all"
    const val FILTER_OFFERS = "filter_offers"
    const val FILTER_REQUESTS = "filter_requests"
    const val POSTS_LIST = "posts_list"
    const val LOADING_INDICATOR = "loading_indicator"
    const val ERROR_MESSAGE = "error_message"
    const val EMPTY_STATE = "empty_state"
    const val POST_ITEM = "post_item"
    const val EDIT_BUTTON = "edit_button"
    const val DELETE_BUTTON = "delete_button"
}

/**
 * Screen that displays all posts created by the current user.
 *
 * Features:
 * - Filter posts by type (All, Offers, Requests)
 * - Edit posts (navigates to edit screen)
 * - Delete posts
 * - Pull-to-refresh
 * - Loading and error states
 *
 * @param viewModel The ViewModel managing the personal posts state
 * @param onGoBack Callback when user navigates back
 * @param onEditPost Callback when user wants to edit a post (navigates to edit screen)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalPostsScreen(
    viewModel: PersonalPostsViewModel =
        viewModel(
            factory = PersonalPostsViewModelFactory(PostFirestoreRepository(Firebase.firestore))
        ),
    onGoBack: () -> Unit = {},
    onEditPost: (Post) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("My Posts", modifier = Modifier.testTag(PersonalPostsScreenTags.TITLE))
                },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .testTag(PersonalPostsScreenTags.SCREEN)
        ) {
            // Filter Buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterButton(
                    text = "All",
                    isSelected = uiState.selectedPostType == PostTypeFilter.ALL,
                    onClick = { viewModel.setPostTypeFilter(PostTypeFilter.ALL) },
                    modifier = Modifier.weight(1f).testTag(PersonalPostsScreenTags.FILTER_ALL)
                )
                FilterButton(
                    text = "Offers",
                    isSelected = uiState.selectedPostType == PostTypeFilter.OFFERS,
                    onClick = { viewModel.setPostTypeFilter(PostTypeFilter.OFFERS) },
                    modifier = Modifier.weight(1f).testTag(PersonalPostsScreenTags.FILTER_OFFERS)
                )
                FilterButton(
                    text = "Requests",
                    isSelected = uiState.selectedPostType == PostTypeFilter.REQUESTS,
                    onClick = { viewModel.setPostTypeFilter(PostTypeFilter.REQUESTS) },
                    modifier = Modifier.weight(1f).testTag(PersonalPostsScreenTags.FILTER_REQUESTS)
                )
            }

            // Content
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.testTag(PersonalPostsScreenTags.LOADING_INDICATOR)
                        )
                    }
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = uiState.error ?: "An error occurred",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag(PersonalPostsScreenTags.ERROR_MESSAGE)
                            )
                            Button(onClick = { viewModel.refresh() }) { Text("Retry") }
                        }
                    }
                }
                uiState.posts.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No posts found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag(PersonalPostsScreenTags.EMPTY_STATE)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.testTag(PersonalPostsScreenTags.POSTS_LIST)
                    ) {
                        items(uiState.posts, key = { it.uid }) { post ->
                            PostItem(
                                post = post,
                                onEditClick = { onEditPost(post) },
                                onDeleteClick = { viewModel.deletePost(post) },
                                modifier =
                                    Modifier.testTag(
                                        "${PersonalPostsScreenTags.POST_ITEM}_${post.uid}"
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Filter button for post types */
@Composable
private fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier.height(40.dp),
        colors =
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
    )
}

/** Individual post item card */
@Composable
private fun PostItem(
    post: Post,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Title and Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val statustext =
                        post.status.name.lowercase().replaceFirstChar { it.uppercase() }
                    val paymentText = post.paymentMethod.displayName
                    val repliesText =
                        "${post.postReplies.size} ${if (post.postReplies.size != 1) "replies" else "reply"}"

                    Text(
                        text = "$statustext • $paymentText • $repliesText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.testTag(PersonalPostsScreenTags.EDIT_BUTTON)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.testTag(PersonalPostsScreenTags.DELETE_BUTTON)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Description
            Text(
                text = post.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            if (post.skills.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    post.skills.take(3).forEach { skill ->
                        SkillPill(skill = skill, isSelected = false, onClick = {})
                    }

                    if (post.skills.size > 3) {
                        Text(
                            text = "+${post.skills.size - 3}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PostItemPreview() {
    val request1 =
        Request(
            uid = "123",
            title = "Need help with Kotlin",
            description = "Looking for an expert to teach me Kotlin.",
            ownerId = "user456",
            skills =
                setOf(
                    SkillTag.MACHINE_DESIGN,
                    SkillTag.CHEMISTRY,
                    SkillTag.COMPUTER_PROGRAMMING,
                    SkillTag.CIRCUIT_ANALYSIS,
                    SkillTag.THERMODYNAMICS
                ),
            tags = setOf(PostTag.REOCCURRING),
            expiry = Timestamp(Date(System.currentTimeMillis() + 86400000)),
            creation = Timestamp.now(),
            status = PostStatus.POSTED,
            media = listOf("media_url_1", "media_url_2"),
            paymentMethod = PaymentMethod.SKILLSANDCASH,
            location = GeoPoint(46.5191, 6.5668),
            postReplies =
                setOf(
                    PostReply(
                        postId = "123",
                        ownerId = "replier123",
                        creation = Timestamp.now(),
                        message = "I want to help!",
                        postType = PostType.REQUEST,
                        replyStatus = ReplyStatus.PROPOSED
                    )
                )
        )
    PostItem(request1, {}, {})
}

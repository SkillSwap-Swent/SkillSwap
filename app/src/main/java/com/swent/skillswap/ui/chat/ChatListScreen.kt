package com.swent.skillswap.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.swent.skillswap.model.chat.Chat
import com.swent.skillswap.model.chat.ChatRepository
import com.swent.skillswap.model.chat.Message
import com.swent.skillswap.model.post.PostRepository
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.model.user.UserRepositery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun ChatListScreen(viewModel: ChatListViewModel = viewModel(), onPostClick: (String) -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPostType by remember { mutableStateOf(PostType.OFFER) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Title
        Text(
            text = "Chat",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        )

        // Related post type filter buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PostTypeFilterButton(
                text = "Offer",
                isSelected = selectedPostType == PostType.OFFER,
                onClick = { selectedPostType = PostType.OFFER },
                modifier = Modifier.weight(1f)
            )

            PostTypeFilterButton(
                text = "Request",
                isSelected = selectedPostType == PostType.REQUEST,
                onClick = { selectedPostType = PostType.REQUEST },
                modifier = Modifier.weight(1f)
            )
        }

        // Chat List
        LaunchedEffect(selectedPostType) { viewModel.getChatsOfCurrentUser(selectedPostType) }
        val filteredChats = uiState.chats
        if (filteredChats.isEmpty()) {
            // Empty state
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No chats available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredChats) { chat ->
                    ChatConversationItem(
                        viewModel = viewModel,
                        chat = chat,
                        onClick = { onPostClick(chat.id) }
                    )
                }
            }
        }
    }
}

/** Filter button for post types (Offer/Request) */
@Composable
fun PostTypeFilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
            ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color =
                if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

/** Individual post conversation item */
@Composable
fun ChatConversationItem(viewModel: ChatListViewModel, chat: Chat, onClick: () -> Unit) {

    val uiState by viewModel.uiState.collectAsState()

    val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    // Assuming two participants
    val otherUser = chat.participants.first { it != currentUser }

    LaunchedEffect(chat.relatedPostId) {
        viewModel.getPostTitle(chat.relatedPostId, chat.relatedPostType)
    }
    LaunchedEffect(otherUser) { viewModel.getUsername(otherUser) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Related post title
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.postTitles[chat.relatedPostId] ?: "Loading...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Right side - Other chat participant username
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(
                    text = uiState.usernames[otherUser] ?: "Loading...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/* @Preview(showBackground = true)
@Composable
fun ChatListScreenPreview() {
    val mockChatRepo =
        object : ChatRepository {
            override suspend fun createChat(
                participants: List<String>,
                relatedPostId: String,
                relatedPostType: PostType
            ) = ""

            override fun streamMessages(chatId: String): Flow<List<Message>> = flowOf(emptyList())

            override suspend fun sendMessage(chatId: String, senderId: String, content: String) {}

            override suspend fun getChatsOfCurrentUser(relatedPostType: PostType) =
                listOf(
                    Chat("1", listOf("user1", "user2"), "post1", PostType.OFFER, emptyList()),
                    Chat("2", listOf("user1", "user3"), "post2", PostType.OFFER, emptyList())
                )
        }
    val mockUserRepo =
        object : UserRepositery {
            override fun getNewUid() = ""

            override suspend fun getUser(userID: String) =
                com.swent.skillswap.model.user.User(
                    uid = userID,
                    username = "User_$userID",
                    email = "$userID@test.com"
                )

            override suspend fun addUser(user: com.swent.skillswap.model.user.User) {}

            override suspend fun editUser(
                userID: String,
                newValue: com.swent.skillswap.model.user.User
            ) {}

            override suspend fun deleteUser(userID: String) {}

            override suspend fun userExists(userId: String) = false
        }
    val mockPostRepo =
        object : PostRepository {
            override fun getNewUid(type: PostType) = ""

            override suspend fun getMultiplePosts(
                numberOfPosts: Long,
                type: PostType,
                titleContains: String,
                ownerId: String,
                paymentMethod: com.swent.skillswap.model.post.PaymentMethod?,
                tags: Set<com.swent.skillswap.model.tags.EveryTag>,
                status: com.swent.skillswap.model.post.PostStatus?,
                userLocation: com.google.firebase.firestore.GeoPoint?,
                maxDistanceKm: Double?
            ) = emptyList<com.swent.skillswap.model.post.Post>()

            override suspend fun getPost(type: PostType, postId: String) =
                com.swent.skillswap.model.post.Offer(
                    uid = postId,
                    title = "Mock Post $postId",
                    description = "Description",
                    ownerId = "owner1",
                    tags = emptySet(),
                    paymentMethod = com.swent.skillswap.model.post.PaymentMethod.SKILLS,
                    expiry = com.google.firebase.Timestamp.now(),
                    creation = com.google.firebase.Timestamp.now(),
                    status = com.swent.skillswap.model.post.PostStatus.POSTED,
                    media = emptyList(),
                    location = com.google.firebase.firestore.GeoPoint(0.0, 0.0)
                )

            override suspend fun addPost(post: com.swent.skillswap.model.post.Post) {}

            override suspend fun editPost(
                postId: String,
                newPost: com.swent.skillswap.model.post.Post
            ) {}

            override suspend fun deletePost(type: PostType, postId: String) {}
        }

    ChatListScreen(ChatListViewModel(mockChatRepo, mockUserRepo, mockPostRepo))
} */

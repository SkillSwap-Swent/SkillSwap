package com.swent.skillswap

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.swent.skillswap.model.chat.ChatListScreenData
import com.swent.skillswap.model.feed.ChatRepository
import com.swent.skillswap.model.feed.FeedController
import com.swent.skillswap.model.feed.FeedControllerFactory
import com.swent.skillswap.model.feed.RecommendationEngine
import com.swent.skillswap.model.feed.ThumbnailRepository
import com.swent.skillswap.model.post.PostFirestoreRepository
import com.swent.skillswap.model.post.PostType
import com.swent.skillswap.resources.C
import com.swent.skillswap.resources.theme.SkillSwapAppTheme
import com.swent.skillswap.ui.auth.AuthCreateAccountScreen
import com.swent.skillswap.ui.auth.AuthMainScreen
import com.swent.skillswap.ui.auth.PasswordRecoveryScreen
import com.swent.skillswap.ui.chat.ChatListScreen
import com.swent.skillswap.ui.feed.FeedScreen
import com.swent.skillswap.ui.feed.FeedScreenViewModel
import com.swent.skillswap.ui.feed.FeedScreenViewModelFactory
import com.swent.skillswap.ui.navigation.BottomNavigationMenu
import com.swent.skillswap.ui.navigation.NavigationActions
import com.swent.skillswap.ui.navigation.Screen
import com.swent.skillswap.ui.navigation.Tab
import com.swent.skillswap.ui.post.PostOperation
import com.swent.skillswap.ui.post.RequestScreen
import com.swent.skillswap.ui.post.personalPosts.PersonalPostsScreen
import com.swent.skillswap.ui.user.ProfileScreen
import com.swent.skillswap.ui.user.ProfileViewModel
import com.swent.skillswap.ui.user.editUser.EditUserScreen
import com.swent.skillswap.ui.user.editUser.EditUserViewModel
import com.swent.skillswap.ui.user.editUser.SkillsEditScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*For testing purposes on sign in*/
        // FirebaseAuth.getInstance().signOut()
        setContent {
            SkillSwapAppTheme() {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier =
                        Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
                    color = MaterialTheme.colorScheme.background
                ) {
                    SkillSwapApp()
                }
            }
        }
    }
}

// Enabling navController to be passed as an argument to facilitate testing
@Composable
fun SkillSwapApp(navController: NavHostController = rememberNavController()) {
    val focusManager = LocalFocusManager.current

    val navigationActions = remember(navController) { NavigationActions(navController) }
    val startDestination = Screen.AuthMain.name

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val editProfileViewModel: EditUserViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    var controller by remember { mutableStateOf<FeedController?>(null) }

    LaunchedEffect(Unit) {
        controller =
            FeedControllerFactory(
                    recommendationEngine = RecommendationEngine(),
                    thumbnailRepository = ThumbnailRepository(),
                    postRepository = PostFirestoreRepository(Firebase.firestore),
                    chatRepository = ChatRepository()
                )
                .create(
                    userIdPerformingActions = Firebase.auth.uid ?: "AnoUser",
                    feedType = PostType.REQUEST
                )
    }

    Scaffold(
        bottomBar = {
            val currentTab =
                when (currentRoute) {
                    Screen.Profile.route -> Tab.Profile
                    Screen.Feed.route -> Tab.Feed
                    Screen.Chat.route -> Tab.Chat
                    else -> null
                }

            currentTab?.let { tab ->
                BottomNavigationMenu(
                    selectedTab = tab,
                    onTabSelected = { selectedTab ->
                        navigationActions.navigateTo(selectedTab.destination)
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier =
                Modifier.fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
                    .padding(paddingValues)
        ) {
            // SIGN IN / CREATE ACCOUNT SCREENS
            navigation(startDestination = Screen.AuthMain.route, route = Screen.AuthMain.name) {
                composable(Screen.AuthMain.route) {
                    AuthMainScreen(
                        goToCreateAccountScreen = {
                            navigationActions.navigateTo(Screen.CreateAccount)
                        },
                        goToMainScreen = { navigationActions.navigateTo(Screen.Profile) },
                        goToPasswordRecovery = {
                            navigationActions.navigateTo(Screen.PasswordRecovery)
                        }
                    )
                }
                composable(Screen.CreateAccount.route) {
                    AuthCreateAccountScreen(
                        goToMainScreen = { navigationActions.navigateTo(Screen.Profile) },
                    )
                }
                composable(Screen.PasswordRecovery.route) {
                    PasswordRecoveryScreen(goBackToSignIn = { navigationActions.goBack() })
                }
            }

            // USER SCREENS
            navigation(startDestination = Screen.Profile.route, route = Screen.Profile.name) {
                composable(Screen.Profile.route) {
                    editProfileViewModel.loadCurrentUser()
                    profileViewModel.loadCurrentUser()
                    ProfileScreen(
                        vm = profileViewModel,
                        onLogoutClick = {
                            editProfileViewModel.clearLoadedState()
                            FirebaseAuth.getInstance().signOut()
                            navigationActions.navigateTo(Screen.AuthMain)
                        },
                        onEditProfileClick = { navigationActions.navigateTo(Screen.EditProfile) },
                        onSkillClick = { navigationActions.navigateTo(Screen.EditSkills) },
                        onAddPostClick = { navigationActions.navigateTo(Screen.AddRequest) },
                        onSeeMyPostsClick = { navigationActions.navigateTo(Screen.PersonalPosts) }
                    )
                }
                composable(Screen.EditProfile.route) {
                    EditUserScreen(
                        vm = editProfileViewModel,
                        onGoBack = { navigationActions.goBack() },
                    )
                }
                composable(Screen.EditSkills.route) {
                    SkillsEditScreen(
                        vm = editProfileViewModel,
                        onBackClick = { navigationActions.goBack() }
                    )
                }
                composable(Screen.PersonalPosts.route) {
                    val postRepository = PostFirestoreRepository(Firebase.firestore)
                    PersonalPostsScreen(
                        onGoBack = { navigationActions.goBack() },
                        onEditPost = { post ->
                            // Navigate to edit screen based on post type
                            if (post.type == PostType.REQUEST) {
                                if (post.uid.isNotBlank()) {
                                    navController.navigate(Screen.EditRequest.createRoute(post.uid))
                                }
                            } else {
                                // TODO: Add navigation to edit offer screen when available
                            }
                        }
                    )
                }
                composable(
                    route = Screen.EditRequest.route,
                    arguments = listOf(navArgument("postId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("postId") ?: ""
                    val postRepository = PostFirestoreRepository(Firebase.firestore)
                    val currentUserId = Firebase.auth.uid ?: ""
                    RequestScreen(
                        postRepository = postRepository,
                        currentUserId = currentUserId,
                        uid = postId,
                        postOperation = PostOperation.EDIT,
                        onGoBack = { navigationActions.goBack() },
                        onPostCreated = {
                            // Navigate back to personal posts after successful edit
                            navigationActions.navigateTo(Screen.PersonalPosts)
                        }
                    )
                }
            }

            composable(Screen.Feed.route) {
                if (controller == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val factory =
                        remember(controller) {
                            FeedScreenViewModelFactory(
                                navigation = { /* TODO: implement navigation to other user profile */},
                                controller = controller!!
                            )
                        }
                    val vm: FeedScreenViewModel = viewModel(factory = factory)
                    FeedScreen(vm = vm)
                }
            }

            composable(Screen.AddRequest.route) {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId == null) {
                    Log.d("MainActivity", "AddPost screen skipped: currentUserId is null")
                    return@composable
                }
                RequestScreen(
                    postRepository = PostFirestoreRepository(Firebase.firestore),
                    currentUserId = currentUserId,
                    uid = null,
                    onGoBack = { navigationActions.goBack() },
                    onPostCreated = { navigationActions.navigateTo(Screen.Profile) },
                    postOperation = PostOperation.ADD,
                )
            }

            composable(Screen.Chat.route) {
                ChatListScreen(
                    posts = ChatListScreenData.getSamplePosts(),
                    users = ChatListScreenData.getSampleUsers(),
                    onPostClick = { post ->
                        // TODO: Navigate to individual chat with post
                        println("Clicked on post: ${post.title}")
                    },
                )
            }
        }
    }
}

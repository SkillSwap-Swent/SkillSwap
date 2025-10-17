package com.swent.skillswap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swent.skillswap.model.navigation.NavigationBottomBarModel
import com.swent.skillswap.resources.C
import com.swent.skillswap.ui.chat.ChatScreen
import com.swent.skillswap.ui.navigation.NavigationActions
import com.swent.skillswap.ui.navigation.Screen
import com.swent.skillswap.ui.navigation.bottomBar.BottomBar
import com.swent.skillswap.ui.navigation.bottomBar.BottomBarViewModel
import com.swent.skillswap.ui.offerScreen.OfferScreen
import com.swent.skillswap.ui.signIn.SignInCreateAccountScreen
import com.swent.skillswap.ui.signIn.SignInMainScreen
import com.swent.skillswap.ui.theme.SkillSwapAppTheme
import com.swent.skillswap.ui.user.ProfileMainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkillSwapAppTheme(dynamicColor = false) {
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

    val navigationActions = remember(navController) { NavigationActions(navController) }
    val startDestination = Screen.SignInMain.route
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val showBottomBar =
        currentRoute in listOf(Screen.Offers.route, Screen.Chat.route, Screen.Profile.route)

    val bottomBarViewModel = remember {
        BottomBarViewModel(NavigationBottomBarModel(navigationActions))
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(vm = bottomBarViewModel)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.SignInMain.route) {
                SignInMainScreen(
                    goToCreateAccountScreen = {
                        navigationActions.navigateTo(Screen.SignInCreateAccount)
                    },
                )
            }
            composable(Screen.SignInCreateAccount.route) {
                SignInCreateAccountScreen(
                    goToMainScreen = { navigationActions.navigateTo(Screen.Profile) },
                )
            }

            composable(Screen.Offers.route) { OfferScreen() }
            composable(Screen.Chat.route) { ChatScreen() }
            composable(Screen.Profile.route) { ProfileMainScreen() }
        }
    }
}

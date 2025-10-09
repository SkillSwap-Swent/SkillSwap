package com.swent.skillswap.ui.profile

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

enum class ProfileScreenType {
    MAIN,
    MY_SKILLS,
    INFORMATION,
    SECURITY,
    ACCOUNT,
    ACCOUNT_REVIEW,
    ADD_ACCOUNTS
}

@Composable
fun ProfileMainScreen() {
    var currentScreen by remember { mutableStateOf(ProfileScreenType.MAIN) }

    when (currentScreen) {
        ProfileScreenType.MAIN -> {
            ProfileScreen(
                onMySkillsClick = { currentScreen = ProfileScreenType.MY_SKILLS },
                onInformationClick = { currentScreen = ProfileScreenType.INFORMATION },
                onSecurityClick = { currentScreen = ProfileScreenType.SECURITY },
                onAccountClick = { currentScreen = ProfileScreenType.ACCOUNT },
                onAccountReviewClick = { currentScreen = ProfileScreenType.ACCOUNT_REVIEW },
                onAddAccountsClick = { currentScreen = ProfileScreenType.ADD_ACCOUNTS }
            )
        }
        ProfileScreenType.MY_SKILLS -> {
            MySkillsScreen(onBackClick = { currentScreen = ProfileScreenType.MAIN })
        }
        ProfileScreenType.INFORMATION -> {
            InformationScreen(onBackClick = { currentScreen = ProfileScreenType.MAIN })
        }
        ProfileScreenType.SECURITY -> {
            SecurityScreen(onBackClick = { currentScreen = ProfileScreenType.MAIN })
        }
        ProfileScreenType.ACCOUNT -> {
            AccountScreen(onBackClick = { currentScreen = ProfileScreenType.MAIN })
        }
        ProfileScreenType.ACCOUNT_REVIEW -> {
            AccountReviewScreen(onBackClick = { currentScreen = ProfileScreenType.MAIN })
        }
        ProfileScreenType.ADD_ACCOUNTS -> {
            AddAccountsScreen(onBackClick = { currentScreen = ProfileScreenType.MAIN })
        }
    }
}

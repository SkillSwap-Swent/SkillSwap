package com.swent.skillswap.ui.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

object NavigationTestTags {
    const val BOTTOM_NAVIGATION_MENU = "BottomNavigationMenu"
    const val PROFILE_TAB = "ProfileTab"
    const val FEED_TAB = "FeedTab"
    const val CHAT_TAB = "ChatTab"

    fun getTabTestTag(tab: Tab): String =
        when (tab) {
            is Tab.Profile -> PROFILE_TAB
            is Tab.Feed -> FEED_TAB
            is Tab.Chat -> CHAT_TAB
        }
}

sealed class Tab(val name: String, val icon: ImageVector, val destination: Screen) {
    object Profile : Tab("Profile", Icons.Outlined.People, Screen.Profile)

    object Feed : Tab("Feed", Icons.AutoMirrored.Outlined.List, Screen.Feed)

    object Chat : Tab("Chat", Icons.Outlined.Mail, Screen.Chat)
}

private val tabs = listOf(Tab.Profile, Tab.Feed, Tab.Chat)

@Composable
fun BottomNavigationMenu(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier =
            modifier
                .fillMaxWidth()
                .height(70.dp)
                .testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
    ) {
        tabs.forEach { tab ->
            NavigationBarItem(
                icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                label = { Text(tab.name) },
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                modifier = Modifier.testTag(NavigationTestTags.getTabTestTag(tab))
            )
        }
    }
}

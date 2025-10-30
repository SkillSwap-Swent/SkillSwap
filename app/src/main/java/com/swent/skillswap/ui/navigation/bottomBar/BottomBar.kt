package com.swent.skillswap.ui.navigation.bottomBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.swent.skillswap.model.navigation.NavigationBottomBarModel
import com.swent.skillswap.ui.navigation.NavigationActions
import com.swent.skillswap.ui.theme.SkillSwapAppTheme

/**
 * Contains test tags for the [BottomBar] UI components.
 *
 * These tags are used in Compose UI tests to uniquely identify the buttons and container of the
 * bottom navigation bar.
 *
 * @author Joey Gugler Made Using Ai (chatGPT)
 */
object BottomBarTestTag {

    /** Test tag for the Profile button in the bottom bar. */
    const val PROFILE_BUTTON = "PROFILE_BUTTON"

    /** Test tag for the Chat button in the bottom bar. */
    const val CHAT_BUTTON = "CHAT_BUTTON"

    /** Test tag for the Offer Screen button in the bottom bar. */
    const val OFFER_SCREEN_BUTTON = "OFFER_SCREEN_BUTTON"

    /** Test tag for the bottom bar container (Row). */
    const val BOTTOM_BAR = "BOTTOM_BAR"
}

/**
 * Composable for the Bottom Navigation Bar.
 *
 * Displays a horizontal row with three buttons for navigation:
 * - Profile
 * - Offers
 * - Chat
 *
 * The currently selected screen is highlighted by disabling the corresponding button and applying
 * the secondary color.
 *
 * @param vm The [BottomBarViewModel] that manages navigation and UI state.
 * @author Joey Gugler Made Using Ai (chatGPT)
 */
@Composable
fun BottomBar(vm: BottomBarViewModel = BottomBarViewModel()) {
    val state by vm.uiState
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp)
                .testTag(BottomBarTestTag.BOTTOM_BAR),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BottomBarButton(
            label = "Profile",
            selected = state.selectedScreen == BottomBarScreen.PROFILE,
            onClick = { vm.onScreenSelected(BottomBarScreen.PROFILE) },
            tag = BottomBarTestTag.PROFILE_BUTTON,
            icon = Icons.Default.Person,
        )

        BottomBarButton(
            label = "Offers",
            selected = state.selectedScreen == BottomBarScreen.OFFER,
            onClick = { vm.onScreenSelected(BottomBarScreen.OFFER) },
            tag = BottomBarTestTag.OFFER_SCREEN_BUTTON,
            icon = Icons.AutoMirrored.Filled.List,
        )

        BottomBarButton(
            label = "Chat",
            selected = state.selectedScreen == BottomBarScreen.CHAT,
            onClick = { vm.onScreenSelected(BottomBarScreen.CHAT) },
            tag = BottomBarTestTag.CHAT_BUTTON,
            icon = Icons.Default.Email
        )
    }
}

/**
 * Composable for a single button in the [BottomBar].
 *
 * Displays an icon with optional label and handles click events. The button is disabled and styled
 * differently if it is currently selected.
 *
 * @param label The text label for the button (used for accessibility).
 * @param icon The [ImageVector] icon to display inside the button.
 * @param selected Whether this button represents the currently selected screen.
 * @param onClick Lambda to execute when the button is clicked.
 * @param tag The test tag to identify this button in UI tests.
 * @author Joey Gugler Made Using Ai (chatGPT)
 */
@Composable
fun BottomBarButton(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Button(
        onClick = onClick,
        enabled = !selected,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.secondary
            ),
        modifier = Modifier.testTag(tag)
    ) {
        Row(horizontalArrangement = Arrangement.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint =
                    if (selected) MaterialTheme.colorScheme.onSecondary
                    else MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomBarPreview() {
    SkillSwapAppTheme() {
        val navController = rememberNavController()
        val navigationActions = NavigationActions(navController)
        val vm = remember {
            BottomBarViewModel(navigation = NavigationBottomBarModel(navigationActions))
        } // keep VM across recompositions
        BottomBar(vm)
    }
}

package com.swent.skillswap.ui.offerScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swent.skillswap.ui.theme.SkillSwapAppTheme

/**
 * Displays the main Offer screen.
 *
 * This composable observes the [OfferScreenViewModel] to render the current offer and handle user
 * interactions such as swipes or button actions.
 *
 * @param vm The [OfferScreenViewModel] that provides the UI state and handles user actions.
 *   Defaults to the ViewModel instance obtained via [viewModel].
 */
@Composable
fun OfferScreen(
    vm: OfferScreenViewModel = viewModel(),
) {
    val uiState by vm.uiState.collectAsState()
    val swipeThreshold = 50f
    val offer = uiState.current

    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Card(
            modifier =
                Modifier.testTag(OfferScreenTestTags.OFFER_CARD)
                    .fillMaxWidth()
                    .height(250.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            val (x, y) = dragAmount

                            when {
                                y > swipeThreshold -> vm.next() // swipe down
                                y < -swipeThreshold -> vm.previous() // swipe up
                                x < -swipeThreshold -> vm.goToProfile(offer.authorID) // swipe left
                                x > swipeThreshold -> vm.accept(offer) // swipe right
                            }
                        }
                    }
                    .border(
                        width = 1.34.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(percent = 15)
                    ),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
            shape = RoundedCornerShape(percent = 15),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .border(
                                width = 1.34.dp,
                                color = MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(percent = 50)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(percent = 50)
                            )
                            .padding(12.dp)
                            .testTag(OfferScreenTestTags.OFFER_GIVE),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = offer.give.ifEmpty { "No offer available" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .border(
                                width = 1.34.dp,
                                color = MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(percent = 50)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(percent = 50)
                            )
                            .padding(12.dp)
                            .testTag(OfferScreenTestTags.OFFER_RECEIVE),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = offer.receive.ifEmpty { "—" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomBarPreview() {
    SkillSwapAppTheme(dynamicColor = false) {
        val vm = remember { OfferScreenViewModel() }
        OfferScreen(vm)
    }
}

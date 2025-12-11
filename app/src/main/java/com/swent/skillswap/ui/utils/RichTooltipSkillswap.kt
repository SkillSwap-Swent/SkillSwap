/** Template from https://developer.android.com/develop/ui/compose/components/tooltip */
package com.swent.skillswap.ui.utils

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.launch

object RichTooltipTestTags {
    // Screen-level
    const val BUTTON = "button"
}

/**
 * A reusable Tooltip component.
 *
 * This wrapper around Material3's [RichTooltip] provides:
 * - Multi-line informational text
 * - A dismiss button
 * - Optional initial visibility
 * - Support for persistent tooltips (In our usecase this is default behaviour, after pressing the
 *   button it should not dissapear untilk dismissed by user).
 *
 * The tooltip is opened by tapping the Info icon, and dismissed via the action button inside the
 * tooltip. Text content should be supplied from `TooltipDescriptions` to ensure consistent
 * messaging across the app.
 *
 * @param modifier Optional [Modifier] applied to the surrounding [TooltipBox].
 * @param body Text displayed inside the tooltip. Should come from `TooltipDescriptions`.
 * @param buttonText Label for the dismiss button.
 * @param initialIsVisible Whether the tooltip is shown on first composition.
 * @param isPersistent Whether the tooltip stays visible until manually dismissed or auto dissapears
 *   after a couple of seconds.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RichTooltipSkillswap(
    modifier: Modifier = Modifier,
    body: String = "Rich tooltips support multiple lines of informational text.",
    buttonText: String = "Dismiss",
    initialIsVisible: Boolean = false,
    isPersistent: Boolean = true,
) {
    val tooltipState =
        rememberTooltipState(initialIsVisible = initialIsVisible, isPersistent = isPersistent)
    val coroutineScope = rememberCoroutineScope()

    TooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        tooltip = {
            RichTooltip(
                action = {
                    Row {
                        TextButton(
                            modifier = Modifier.testTag(RichTooltipTestTags.BUTTON),
                            onClick = { coroutineScope.launch { tooltipState.dismiss() } }
                        ) {
                            Text(buttonText)
                        }
                    }
                },
            ) {
                Text(body)
            }
        },
        state = tooltipState
    ) {
        IconButton(onClick = { coroutineScope.launch { tooltipState.show() } }) {
            Icon(imageVector = Icons.Filled.Info, contentDescription = "Open tooltip")
        }
    }
}

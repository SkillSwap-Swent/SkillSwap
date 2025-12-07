/** Template from https://developer.android.com/develop/ui/compose/components/tooltip*/
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
import kotlinx.coroutines.launch

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
                        TextButton(onClick = { coroutineScope.launch { tooltipState.dismiss() } }) {
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
            Icon(imageVector = Icons.Filled.Info, contentDescription = "Open camera")
        }
    }
}

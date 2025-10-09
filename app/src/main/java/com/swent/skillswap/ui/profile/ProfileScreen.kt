package com.swent.skillswap.ui.profile
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember

@Composable
fun ProfileScreen(
    onMySkillsClick: () -> Unit = {},
    onInformationClick: () -> Unit = {},
    onSecurityClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onAccountReviewClick: () -> Unit = {},
    onAddAccountsClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Centered "Profile" title at the top
        Text(
            text = "Profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Gradient rectangle with curved borders
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush =
                            Brush.verticalGradient(colors = listOf(Color.Black, Color(0xFF2B5080)))
                    )
                    .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // My skills button
                Text(
                    text = "My skills",
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onMySkillsClick()
                        }
                        .padding(vertical = 12.dp)
                )

                // Horizontal white line
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                ) {
                    drawLine(
                        color = Color.White,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Your informations and permissions button
                Text(
                    text = "Your informations and permissions",
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onInformationClick()
                        }
                        .padding(vertical = 12.dp)
                )

                // Horizontal white line
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                ) {
                    drawLine(
                        color = Color.White,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Password and security button
                Text(
                    text = "Password and security",
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onSecurityClick()
                        }
                        .padding(vertical = 12.dp)
                )
            }
        }

        // Add spacing between rectangles
        Spacer(modifier = Modifier.height(16.dp))

        // Second gradient rectangle for Account
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush =
                            Brush.verticalGradient(colors = listOf(Color.Black, Color(0xFF2B5080)))
                    )
                    .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Account section with emoticon and text (clickable)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onAccountClick()
                        }
                        .padding(vertical = 8.dp)
                ) {
                    // Emoticon (using a simple circle as emoticon)
                    Canvas(
                        modifier = Modifier.size(20.dp)
                    ) {
                        drawCircle(
                            color = Color.White,
                            radius = 8.dp.toPx()
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Account text
                    Text(
                        text = "Account",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Description text aligned with Account (clickable)
                Text(
                    text = "Review and update your accounts",
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onAccountReviewClick()
                        }
                        .padding(start = 28.dp, bottom = 12.dp)
                )

                // Horizontal white line
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                ) {
                    drawLine(
                        color = Color.White,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Add more accounts button (clickable)
                Text(
                    text = "Add more accounts",
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onAddAccountsClick()
                        }
                        .padding(vertical = 12.dp)
                )
            }
        }
    }
}
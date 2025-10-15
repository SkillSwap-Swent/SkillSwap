package com.swent.skillswap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.swent.skillswap.ui.theme.SkillSwapAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkillSwapAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProfileTestScreen()
                }
            }
        }
    }
}

@Composable
fun ProfileTestScreen() {
    Text(
        text =
            "Profile Screen Test\n\nTo test the profile screen:\n1. Open ProfileScreen.kt\n2. Use Android Studio Preview\n3. Or create a separate test project",
        fontSize = 16.sp,
        modifier = Modifier.fillMaxSize()
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileTestPreview() {
    SkillSwapAppTheme { ProfileTestScreen() }
}

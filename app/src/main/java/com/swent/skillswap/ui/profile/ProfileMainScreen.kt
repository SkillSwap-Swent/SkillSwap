package com.swent.skillswap.ui.profile

import androidx.compose.runtime.*
import com.swent.skillswap.model.tags.SkillTag

enum class ProfileScreenType {
    MAIN,
    SKILLS,
    EDIT_SKILLS
}

@Composable
fun ProfileMainScreen(
    userSkills: Set<SkillTag> = emptySet(),
    onSkillsUpdated: (Set<SkillTag>) -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf(ProfileScreenType.MAIN) }
    var currentUserSkills by remember { mutableStateOf(userSkills) }

    when (currentScreen) {
        ProfileScreenType.MAIN -> {
            ProfileScreen(
                userSkills = currentUserSkills,
                onSkillsClick = { currentScreen = ProfileScreenType.EDIT_SKILLS }
            )
        }
        ProfileScreenType.SKILLS -> {
            MySkillsScreen(onBackClick = { currentScreen = ProfileScreenType.MAIN })
        }
        ProfileScreenType.EDIT_SKILLS -> {
            SkillsEditScreen(
                currentSkills = currentUserSkills,
                onBackClick = { currentScreen = ProfileScreenType.MAIN },
                onSkillsUpdated = { updatedSkills ->
                    currentUserSkills = updatedSkills
                    onSkillsUpdated(updatedSkills)
                }
            )
        }
    }
}

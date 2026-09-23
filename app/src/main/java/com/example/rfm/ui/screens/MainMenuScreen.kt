package com.example.rfm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rfm.ui.theme.*

@Composable
fun MainMenuScreen(
    hasExistingSave: Boolean,
    onNewCareer: () -> Unit,
    onContinueCareer: () -> Unit,
    onLoadSave: () -> Unit,
    onQuickMatch: () -> Unit,
    onOpenModMenu: () -> Unit,
    onToggleKeypad: () -> Unit,
    onToggleSound: () -> Unit,
    isKeypadOn: Boolean,
    isSoundOn: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(RfmNavyDark, RfmPitchDark)
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title Header
            Text(
                text = "REAL FOOTBALL",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "MANAGER 2013",
                color = RfmGold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            Text(
                text = "MAIN MENU",
                color = RfmTextSecondary,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Menu Options
            if (hasExistingSave) {
                MenuButton(
                    title = "CONTINUE CAREER",
                    subtitle = "Resume your existing manager season",
                    isPrimary = true,
                    onClick = onContinueCareer,
                    tag = "menu_continue_button"
                )
            }

            MenuButton(
                title = "NEW CAREER",
                subtitle = "Start fresh with any club in 5 European leagues",
                isPrimary = !hasExistingSave,
                onClick = onNewCareer,
                tag = "menu_new_career_button"
            )

            MenuButton(
                title = "QUICK EXHIBITION",
                subtitle = "Manchester United vs Real Madrid instant match",
                isPrimary = false,
                onClick = onQuickMatch,
                tag = "menu_quick_match_button"
            )

            MenuButton(
                title = "LOAD / SAVE SLOTS",
                subtitle = "Manage multiple save states & autosave",
                isPrimary = false,
                onClick = onLoadSave,
                tag = "menu_load_save_button"
            )

            MenuButton(
                title = "⚡ MOD MENU (SANDBOX)",
                subtitle = "Unlimited Money, 99 Stats, No Fatigue, Unlocks",
                isPrimary = false,
                accentColor = RfmAmber,
                onClick = onOpenModMenu,
                tag = "menu_mod_sandbox_button"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Controls & Audio Quick Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onToggleKeypad,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(
                        text = if (isKeypadOn) "🎮 Keypad: ON" else "📱 Keypad: OFF",
                        fontSize = 11.sp
                    )
                }

                OutlinedButton(
                    onClick = onToggleSound,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(
                        text = if (isSoundOn) "🔊 Sound: ON" else "🔇 Sound: OFF",
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuButton(
    title: String,
    subtitle: String,
    isPrimary: Boolean,
    accentColor: Color? = null,
    onClick: () -> Unit,
    tag: String
) {
    val containerColor = when {
        accentColor != null -> RfmNavyCard
        isPrimary -> RfmPitchGreen
        else -> RfmNavyCard
    }
    val borderColor = accentColor ?: if (isPrimary) RfmNeonGreen else RfmNavyBorder

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag(tag)
    ) {
        Column {
            Text(
                text = title,
                color = accentColor ?: if (isPrimary) Color.White else RfmGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = subtitle,
                color = RfmTextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

package com.example.rfm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rfm.model.GameState
import com.example.rfm.navigation.RfmScreen
import com.example.rfm.ui.theme.*

@Composable
fun RfmTopBar(
    gameState: GameState,
    currentScreen: RfmScreen,
    onNavigateHome: () -> Unit,
    onOpenModMenu: () -> Unit,
    onToggleKeypad: () -> Unit,
    onToggleSound: () -> Unit
) {
    val userTeam = gameState.getUserTeam()
    val activeMods = gameState.modSettings.activeModCount()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(RfmNavyDark, RfmNavySurface)
                )
            )
            .border(width = 1.dp, color = RfmNavyBorder)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Team Name & Season
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentScreen != RfmScreen.DASHBOARD && currentScreen != RfmScreen.MAIN_MENU) {
                    IconButton(
                        onClick = onNavigateHome,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("nav_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = RfmGold
                        )
                    }
                }

                Column {
                    Text(
                        text = userTeam?.name ?: "RFM 2013",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "Matchday ${gameState.currentMatchDay}/${gameState.totalMatchDays} • 2012/13",
                        color = RfmTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Right Quick Actions: MOD MENU, KEYPAD, SOUND
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // MOD MENU BUTTON
                Box(
                    modifier = Modifier
                        .background(
                            color = if (activeMods > 0) RfmGold else RfmNavyCard,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (activeMods > 0) RfmAmber else RfmNavyBorder,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onOpenModMenu() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("mod_menu_trigger_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "⚡ MOD",
                            color = if (activeMods > 0) Color.Black else RfmGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (activeMods > 0) {
                            Text(
                                text = "($activeMods)",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // Sound Toggle
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("toggle_sound_button")
                ) {
                    Text(
                        text = if (gameState.isSoundEnabled) "🔊" else "🔇",
                        fontSize = 14.sp
                    )
                }

                // Virtual D-pad Toggle
                IconButton(
                    onClick = onToggleKeypad,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("toggle_keypad_button")
                ) {
                    Text(
                        text = if (gameState.isRetroKeypadEnabled) "🎮" else "📱",
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Subheader status bar with Finances & Board Confidence
        if (userTeam != null && currentScreen != RfmScreen.MAIN_MENU && currentScreen != RfmScreen.SPLASH) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Funds: €${userTeam.balanceEuro / 1_000_000}M",
                    color = RfmNeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Transfer: €${userTeam.transferBudgetEuro / 1_000_000}M",
                    color = RfmGold,
                    fontSize = 11.sp
                )
                Text(
                    text = "Board: ${gameState.boardConfidence}%",
                    color = if (gameState.boardConfidence >= 70) RfmNeonGreen else RfmWarningYellow,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun RfmTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(RfmNavyDark, RfmNavySurface)
                )
            )
            .border(width = 1.dp, color = RfmNavyBorder)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                color = RfmGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = subtitle,
                color = RfmTextSecondary,
                fontSize = 10.sp
            )
        }
        OutlinedButton(
            onClick = onBack,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, RfmNavyBorder)
        ) {
            Text("Back", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}


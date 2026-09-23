package com.example.rfm.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rfm.audio.RfmAudioEngine
import com.example.rfm.model.GameState
import com.example.rfm.model.Team
import com.example.ui.theme.*

@Composable
fun FinancesTab(
    gameState: GameState,
    onFinancesUpdated: () -> Unit
) {
    val userTeam = gameState.getUserTeam() ?: return
    var financeMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
            .testTag("finances_tab_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Balance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = RfmNavyCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(RfmGold, RfmNeonGreen)))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("OVERALL CLUB BALANCE", color = RfmTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("€${userTeam.balanceEuro / 1_000_000}M", color = RfmGold, fontSize = 22.sp, fontWeight = FontWeight.Black)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Weekly Wages", color = RfmTextMuted, fontSize = 11.sp)
                        Text("€${userTeam.players.sumOf { it.wageWeeklyEuro } / 1000}k / wk", color = RfmDangerRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Matchday Revenue", color = RfmTextMuted, fontSize = 11.sp)
                        Text("€${userTeam.stadiumCapacity * 45 / 1000}k / match", color = RfmNeonGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Facilities Upgrades
        Text("CLUB INFRASTRUCTURE & FACILITIES", color = RfmGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

        // 1. Stadium
        FacilityUpgradeCard(
            title = "Stadium Capacity",
            subtitle = "${userTeam.stadiumName} • Level ${userTeam.stadiumLevel} (${userTeam.stadiumCapacity} seats)",
            costEuro = if (gameState.modSettings.unlimitedClubResources) 0 else 15_000_000L,
            currentLevel = userTeam.stadiumLevel,
            maxLevel = 5,
            tag = "btn_upgrade_stadium",
            onUpgrade = {
                val cost = if (gameState.modSettings.unlimitedClubResources) 0 else 15_000_000L
                if (gameState.modSettings.unlimitedMoney || userTeam.balanceEuro >= cost) {
                    RfmAudioEngine.playClickSound()
                    userTeam.balanceEuro -= cost
                    userTeam.stadiumLevel++
                    userTeam.stadiumCapacity += 10_000
                    financeMessage = "Stadium expanded! Capacity increased by 10,000."
                    onFinancesUpdated()
                } else {
                    financeMessage = "Insufficient balance for stadium expansion."
                }
            }
        )

        // 2. Training Ground
        FacilityUpgradeCard(
            title = "Training Ground",
            subtitle = "Level ${userTeam.trainingFacilityLevel} • Sharpens drill development speed",
            costEuro = if (gameState.modSettings.unlimitedClubResources) 0 else 8_000_000L,
            currentLevel = userTeam.trainingFacilityLevel,
            maxLevel = 5,
            tag = "btn_upgrade_training",
            onUpgrade = {
                val cost = if (gameState.modSettings.unlimitedClubResources) 0 else 8_000_000L
                if (gameState.modSettings.unlimitedMoney || userTeam.balanceEuro >= cost) {
                    RfmAudioEngine.playClickSound()
                    userTeam.balanceEuro -= cost
                    userTeam.trainingFacilityLevel++
                    financeMessage = "Training facility upgraded to Level ${userTeam.trainingFacilityLevel}."
                    onFinancesUpdated()
                } else {
                    financeMessage = "Insufficient balance for facility upgrade."
                }
            }
        )

        // 3. Medical Center
        FacilityUpgradeCard(
            title = "Medical Center",
            subtitle = "Level ${userTeam.medicalFacilityLevel} • Faster player injury recovery",
            costEuro = if (gameState.modSettings.unlimitedClubResources) 0 else 6_000_000L,
            currentLevel = userTeam.medicalFacilityLevel,
            maxLevel = 5,
            tag = "btn_upgrade_medical",
            onUpgrade = {
                val cost = if (gameState.modSettings.unlimitedClubResources) 0 else 6_000_000L
                if (gameState.modSettings.unlimitedMoney || userTeam.balanceEuro >= cost) {
                    RfmAudioEngine.playClickSound()
                    userTeam.balanceEuro -= cost
                    userTeam.medicalFacilityLevel++
                    financeMessage = "Medical center upgraded to Level ${userTeam.medicalFacilityLevel}."
                    onFinancesUpdated()
                } else {
                    financeMessage = "Insufficient balance for medical upgrade."
                }
            }
        )

        // 4. Youth Academy
        FacilityUpgradeCard(
            title = "Youth Academy",
            subtitle = "Level ${userTeam.youthFacilityLevel} • Produces elite young talents",
            costEuro = if (gameState.modSettings.unlimitedClubResources) 0 else 7_000_000L,
            currentLevel = userTeam.youthFacilityLevel,
            maxLevel = 5,
            tag = "btn_upgrade_youth",
            onUpgrade = {
                val cost = if (gameState.modSettings.unlimitedClubResources) 0 else 7_000_000L
                if (gameState.modSettings.unlimitedMoney || userTeam.balanceEuro >= cost) {
                    RfmAudioEngine.playClickSound()
                    userTeam.balanceEuro -= cost
                    userTeam.youthFacilityLevel++
                    financeMessage = "Youth academy upgraded to Level ${userTeam.youthFacilityLevel}."
                    onFinancesUpdated()
                } else {
                    financeMessage = "Insufficient balance for academy upgrade."
                }
            }
        )

        if (financeMessage != null) {
            Text(
                text = financeMessage!!,
                color = RfmNeonGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FacilityUpgradeCard(
    title: String,
    subtitle: String,
    costEuro: Long,
    currentLevel: Int,
    maxLevel: Int,
    tag: String,
    onUpgrade: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = RfmNavyCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(RfmNavyBorder, RfmNavyBorder)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = RfmTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(subtitle, color = RfmTextSecondary, fontSize = 11.sp)
            }

            if (currentLevel < maxLevel) {
                Button(
                    onClick = onUpgrade,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag(tag)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("€${costEuro / 1_000_000}M", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text("MAX", color = RfmGold, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

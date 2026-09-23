package com.example.rfm.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rfm.audio.RfmAudioEngine
import com.example.rfm.model.GameState
import com.example.rfm.model.ModSettings
import com.example.ui.theme.*

@Composable
fun ModMenuDialog(
    gameState: GameState,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    val mod = gameState.modSettings

    var unlimitedMoney by remember { mutableStateOf(mod.unlimitedMoney) }
    var unlimitedTransferBudget by remember { mutableStateOf(mod.unlimitedTransferBudget) }
    var unlimitedClubResources by remember { mutableStateOf(mod.unlimitedClubResources) }

    var maxPlayerStats by remember { mutableStateOf(mod.maxPlayerStats) }
    var maxPlayerDevelopment by remember { mutableStateOf(mod.maxPlayerDevelopment) }
    var noPlayerFatigue by remember { mutableStateOf(mod.noPlayerFatigue) }
    var noPlayerInjury by remember { mutableStateOf(mod.noPlayerInjury) }

    var unlimitedTraining by remember { mutableStateOf(mod.unlimitedTraining) }
    var instantTraining by remember { mutableStateOf(mod.instantTraining) }
    var maxClubReputation by remember { mutableStateOf(mod.maxClubReputation) }
    var maxManagerReputation by remember { mutableStateOf(mod.maxManagerReputation) }

    var unlockContent by remember { mutableStateOf(mod.unlockContent) }
    var disableResourceConsumption by remember { mutableStateOf(mod.disableResourceConsumption) }
    var disableFinancialPenalties by remember { mutableStateOf(mod.disableFinancialPenalties) }
    var disableFatigueEffects by remember { mutableStateOf(mod.disableFatigueEffects) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(8.dp)
                .testTag("mod_menu_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = RfmNavyDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RfmGold))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = RfmGold,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "MOD",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "MOD MENU",
                            color = RfmTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            RfmAudioEngine.playClickSound()
                            onDismiss()
                        },
                        modifier = Modifier.testTag("close_mod_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = RfmTextSecondary)
                    }
                }

                Text(
                    text = "Personal Testing Mode: Configure game parameters and instant cheats.",
                    color = RfmTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                HorizontalDivider(color = RfmNavyBorder)

                // Scrollable Toggles
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    // Category: Economy
                    ModSectionHeader(title = "ECONOMY")
                    ModToggleRow(
                        title = "Unlimited Money",
                        subtitle = "Sets club balance to €999,999,999",
                        checked = unlimitedMoney,
                        onCheckedChange = { unlimitedMoney = it },
                        tag = "toggle_unlimited_money"
                    )
                    ModToggleRow(
                        title = "Unlimited Transfer Budget",
                        subtitle = "Sets transfer budget to €500,000,000",
                        checked = unlimitedTransferBudget,
                        onCheckedChange = { unlimitedTransferBudget = it },
                        tag = "toggle_unlimited_transfer"
                    )
                    ModToggleRow(
                        title = "Unlimited Club Resources",
                        subtitle = "Facility upgrade costs reduced to €0",
                        checked = unlimitedClubResources,
                        onCheckedChange = { unlimitedClubResources = it },
                        tag = "toggle_unlimited_resources"
                    )

                    // Category: Player
                    ModSectionHeader(title = "PLAYERS")
                    ModToggleRow(
                        title = "Max Player Stats",
                        subtitle = "Squad match performance rated at 99 OVR",
                        checked = maxPlayerStats,
                        onCheckedChange = { maxPlayerStats = it },
                        tag = "toggle_max_stats"
                    )
                    ModToggleRow(
                        title = "Max Player Development",
                        subtitle = "Accelerates weekly growth potential to maximum",
                        checked = maxPlayerDevelopment,
                        onCheckedChange = { maxPlayerDevelopment = it },
                        tag = "toggle_max_dev"
                    )
                    ModToggleRow(
                        title = "No Player Fatigue",
                        subtitle = "Locks squad condition permanently at 100%",
                        checked = noPlayerFatigue,
                        onCheckedChange = { noPlayerFatigue = it },
                        tag = "toggle_no_fatigue"
                    )
                    ModToggleRow(
                        title = "No Player Injury",
                        subtitle = "Prevents all in-match and training injuries",
                        checked = noPlayerInjury,
                        onCheckedChange = { noPlayerInjury = it },
                        tag = "toggle_no_injury"
                    )

                    // Category: Management
                    ModSectionHeader(title = "MANAGEMENT & PROGRESSION")
                    ModToggleRow(
                        title = "Unlimited Training",
                        subtitle = "Train drills without weekly fatigue cooldown",
                        checked = unlimitedTraining,
                        onCheckedChange = { unlimitedTraining = it },
                        tag = "toggle_unlimited_training"
                    )
                    ModToggleRow(
                        title = "Instant Training / Development",
                        subtitle = "Apply attribute upgrades immediately",
                        checked = instantTraining,
                        onCheckedChange = { instantTraining = it },
                        tag = "toggle_instant_training"
                    )
                    ModToggleRow(
                        title = "Maximum Club Reputation",
                        subtitle = "Set club prestige to 5 stars (attracts all stars)",
                        checked = maxClubReputation,
                        onCheckedChange = { maxClubReputation = it },
                        tag = "toggle_max_club_rep"
                    )
                    ModToggleRow(
                        title = "Maximum Manager Reputation",
                        subtitle = "Set manager rating to 100% board approval",
                        checked = maxManagerReputation,
                        onCheckedChange = { maxManagerReputation = it },
                        tag = "toggle_max_manager_rep"
                    )

                    // Category: Unlocks & Gameplay
                    ModSectionHeader(title = "UNLOCKS & GAMEPLAY RULES")
                    ModToggleRow(
                        title = "Unlock All Content",
                        subtitle = "Unlock all scenarios, tournaments and clubs",
                        checked = unlockContent,
                        onCheckedChange = { unlockContent = it },
                        tag = "toggle_unlock_all"
                    )
                    ModToggleRow(
                        title = "Disable Resource Consumption",
                        subtitle = "Weekly maintenance and wages do not deduct funds",
                        checked = disableResourceConsumption,
                        onCheckedChange = { disableResourceConsumption = it },
                        tag = "toggle_disable_consumption"
                    )
                    ModToggleRow(
                        title = "Disable Financial Penalties",
                        subtitle = "No board fines or debt sanctions",
                        checked = disableFinancialPenalties,
                        onCheckedChange = { disableFinancialPenalties = it },
                        tag = "toggle_disable_penalties"
                    )
                    ModToggleRow(
                        title = "Disable Fatigue Effects",
                        subtitle = "Match performance will not drop when tired",
                        checked = disableFatigueEffects,
                        onCheckedChange = { disableFatigueEffects = it },
                        tag = "toggle_disable_fatigue_fx"
                    )
                }

                HorizontalDivider(color = RfmNavyBorder)

                // Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            RfmAudioEngine.playClickSound()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mod_cancel_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RfmTextSecondary)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            RfmAudioEngine.playClickSound()
                            // Commit to modSettings
                            mod.unlimitedMoney = unlimitedMoney
                            mod.unlimitedTransferBudget = unlimitedTransferBudget
                            mod.unlimitedClubResources = unlimitedClubResources
                            mod.maxPlayerStats = maxPlayerStats
                            mod.maxPlayerDevelopment = maxPlayerDevelopment
                            mod.noPlayerFatigue = noPlayerFatigue
                            mod.noPlayerInjury = noPlayerInjury
                            mod.unlimitedTraining = unlimitedTraining
                            mod.instantTraining = instantTraining
                            mod.maxClubReputation = maxClubReputation
                            mod.maxManagerReputation = maxManagerReputation
                            mod.unlockContent = unlockContent
                            mod.disableResourceConsumption = disableResourceConsumption
                            mod.disableFinancialPenalties = disableFinancialPenalties
                            mod.disableFatigueEffects = disableFatigueEffects

                            // If unlimited money active, apply to actual user team balance
                            val userTeam = gameState.getUserTeam()
                            if (userTeam != null) {
                                if (unlimitedMoney) userTeam.balanceEuro = 999_999_999L
                                if (unlimitedTransferBudget) userTeam.transferBudgetEuro = 500_000_000L
                                if (maxClubReputation) userTeam.reputationStars = 5
                                if (noPlayerFatigue) userTeam.players.forEach { it.condition = 100 }
                                if (noPlayerInjury) userTeam.players.forEach { it.injuryWeeks = 0 }
                            }
                            if (maxManagerReputation) {
                                gameState.boardConfidence = 100
                                gameState.managerReputation = 100
                            }

                            onApply()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mod_apply_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = RfmGold, contentColor = Color.Black)
                    ) {
                        Text("Apply & Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModSectionHeader(title: String) {
    Text(
        text = title,
        color = RfmGold,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun ModToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(RfmNavyCard, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = RfmTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                color = RfmTextMuted,
                fontSize = 11.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = {
                RfmAudioEngine.playClickSound()
                onCheckedChange(it)
            },
            modifier = Modifier.testTag(tag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = RfmNeonGreen,
                uncheckedThumbColor = RfmTextSecondary,
                uncheckedTrackColor = RfmNavySurface
            )
        )
    }
}

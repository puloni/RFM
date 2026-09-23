package com.example.rfm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.rfm.model.ModSettings
import com.example.rfm.ui.theme.*

@Composable
fun ModMenuDialog(
    initialSettings: ModSettings,
    onDismiss: () -> Unit,
    onApplySettings: (ModSettings) -> Unit
) {
    // Local editable copy
    val state = remember { mutableStateOf(initialSettings.copy()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = RfmNavyDark),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .border(2.dp, RfmGold, RoundedCornerShape(12.dp))
                .testTag("mod_menu_dialog")
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
                    Column {
                        Text(
                            text = "⚡ RFM 2013 MOD MENU",
                            color = RfmGold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Private Testing Sandbox & Cheats",
                            color = RfmTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    // Quick Toggle All
                    TextButton(
                        onClick = {
                            val target = state.value.activeModCount() == 0
                            state.value = ModSettings(
                                unlimitedMoney = target,
                                unlimitedTransferBudget = target,
                                unlimitedClubResources = target,
                                maxPlayerStats = target,
                                maxPlayerDevelopment = target,
                                noPlayerFatigue = target,
                                noPlayerInjury = target,
                                unlimitedTraining = target,
                                instantTraining = target,
                                maxClubReputation = target,
                                maxManagerReputation = target,
                                unlockContent = target,
                                disableResourceConsumption = target,
                                disableFinancialPenalties = target,
                                disableFatigueEffects = target
                            )
                        }
                    ) {
                        Text(
                            text = if (state.value.activeModCount() == 0) "ALL ON" else "ALL OFF",
                            color = RfmAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Divider(color = RfmNavyBorder, modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable List of Mod options
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. ECONOMY
                    ModSectionHeader(title = "💰 ECONOMY & CLUB FUNDS")
                    ModSwitchItem(
                        title = "Unlimited Club Balance",
                        subtitle = "Instantly sets funds to €999,999,999",
                        checked = state.value.unlimitedMoney,
                        onCheckedChange = { state.value = state.value.copy(unlimitedMoney = it) }
                    )
                    ModSwitchItem(
                        title = "Unlimited Transfer Budget",
                        subtitle = "Grants €500M transfer fee & €10M wage limit",
                        checked = state.value.unlimitedTransferBudget,
                        onCheckedChange = { state.value = state.value.copy(unlimitedTransferBudget = it) }
                    )
                    ModSwitchItem(
                        title = "Unlimited Club Resources",
                        subtitle = "Instantly maxes stadium & facility levels (Lvl 5)",
                        checked = state.value.unlimitedClubResources,
                        onCheckedChange = { state.value = state.value.copy(unlimitedClubResources = it) }
                    )

                    // 2. PLAYERS
                    ModSectionHeader(title = "⚽ PLAYER ATTRIBUTES & FITNESS")
                    ModSwitchItem(
                        title = "Max Player Stats (99 All)",
                        subtitle = "All squad members boosted to 99 overall rating",
                        checked = state.value.maxPlayerStats,
                        onCheckedChange = { state.value = state.value.copy(maxPlayerStats = it) }
                    )
                    ModSwitchItem(
                        title = "Max Player Development",
                        subtitle = "Doubles weekly training progression yields",
                        checked = state.value.maxPlayerDevelopment,
                        onCheckedChange = { state.value = state.value.copy(maxPlayerDevelopment = it) }
                    )
                    ModSwitchItem(
                        title = "No Player Fatigue",
                        subtitle = "Condition remains locked at 100% during matches",
                        checked = state.value.noPlayerFatigue,
                        onCheckedChange = { state.value = state.value.copy(noPlayerFatigue = it) }
                    )
                    ModSwitchItem(
                        title = "No Player Injury",
                        subtitle = "Completely prevents in-match & training injuries",
                        checked = state.value.noPlayerInjury,
                        onCheckedChange = { state.value = state.value.copy(noPlayerInjury = it) }
                    )

                    // 3. MANAGEMENT
                    ModSectionHeader(title = "📋 CLUB MANAGEMENT & REPUTATION")
                    ModSwitchItem(
                        title = "Instant Training Completion",
                        subtitle = "Bypasses weekly delays for instant skill boost",
                        checked = state.value.instantTraining,
                        onCheckedChange = { state.value = state.value.copy(instantTraining = it) }
                    )
                    ModSwitchItem(
                        title = "Unlimited Training Sessions",
                        subtitle = "Allows multiple drills per match cycle",
                        checked = state.value.unlimitedTraining,
                        onCheckedChange = { state.value = state.value.copy(unlimitedTraining = it) }
                    )
                    ModSwitchItem(
                        title = "Max Club Reputation (5 Stars)",
                        subtitle = "Unlocks elite transfer target interest",
                        checked = state.value.maxClubReputation,
                        onCheckedChange = { state.value = state.value.copy(maxClubReputation = it) }
                    )
                    ModSwitchItem(
                        title = "Max Manager Reputation & Board Trust",
                        subtitle = "Locks board confidence at 100%",
                        checked = state.value.maxManagerReputation,
                        onCheckedChange = { state.value = state.value.copy(maxManagerReputation = it) }
                    )

                    // 4. GAMEPLAY RULES
                    ModSectionHeader(title = "⚙️ GAMEPLAY RULES & BYPASSES")
                    ModSwitchItem(
                        title = "Unlock All Content",
                        subtitle = "Instant access to all leagues & historical teams",
                        checked = state.value.unlockContent,
                        onCheckedChange = { state.value = state.value.copy(unlockContent = it) }
                    )
                    ModSwitchItem(
                        title = "Disable Financial Penalties",
                        subtitle = "Ignore deficit limits and overspending checks",
                        checked = state.value.disableFinancialPenalties,
                        onCheckedChange = { state.value = state.value.copy(disableFinancialPenalties = it) }
                    )
                    ModSwitchItem(
                        title = "Disable Fatigue Effects",
                        subtitle = "Players do not suffer performance drop at low stamina",
                        checked = state.value.disableFatigueEffects,
                        onCheckedChange = { state.value = state.value.copy(disableFatigueEffects = it) }
                    )

                    // 5. RETRO AESTHETICS
                    ModSectionHeader(title = "📺 VINTAGE J2ME / CRT DISPLAY")
                    ModSwitchItem(
                        title = "CRT Retro Scanlines",
                        subtitle = "Authentic Samsung GT-S8000 touch LCD pixel scanline overlay",
                        checked = state.value.crtRetroFilter,
                        onCheckedChange = { state.value = state.value.copy(crtRetroFilter = it) }
                    )
                }

                Divider(color = RfmNavyBorder, modifier = Modifier.padding(vertical = 8.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onApplySettings(state.value) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apply_mods_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RfmGold,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Apply Cheats", fontWeight = FontWeight.Bold)
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
        color = RfmAmber,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun ModSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RfmNavyCard, RoundedCornerShape(8.dp))
            .border(1.dp, if (checked) RfmGold else RfmNavyBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = title,
                color = if (checked) Color.White else RfmTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = RfmTextMuted,
                fontSize = 10.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = RfmGold,
                checkedTrackColor = RfmNavyBorder,
                uncheckedThumbColor = RfmTextMuted,
                uncheckedTrackColor = RfmNavyDark
            )
        )
    }
}

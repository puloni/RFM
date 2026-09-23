package com.example.rfm.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rfm.audio.RfmAudioEngine
import com.example.rfm.model.Player
import com.example.rfm.model.Team
import com.example.ui.theme.*

@Composable
fun SquadTab(
    userTeam: Team,
    onPlayerAction: () -> Unit
) {
    var selectedPlayer by remember { mutableStateOf<Player?>(null) }
    var swapTargetPlayer by remember { mutableStateOf<Player?>(null) }
    var showPlayerDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("squad_tab_screen")
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SQUAD MANAGEMENT",
                    color = RfmGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${userTeam.players.size} Players • Team OVR: ${userTeam.calculateTeamStrength()}",
                    color = RfmTextSecondary,
                    fontSize = 11.sp
                )
            }

            // Auto-Pick Button
            Button(
                onClick = {
                    RfmAudioEngine.playClickSound()
                    autoPickBestXI(userTeam)
                    onPlayerAction()
                },
                modifier = Modifier.testTag("btn_auto_pick_xi"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp), tint = RfmNeonGreen)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Auto Pick XI", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Squad List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 1. Starting XI Header
            item {
                SectionHeader(title = "STARTING XI (${userTeam.getStartingXI().size}/11)")
            }

            items(userTeam.getStartingXI()) { player ->
                PlayerRowCard(
                    player = player,
                    userTeam = userTeam,
                    isStarting = true,
                    onClick = {
                        RfmAudioEngine.playClickSound()
                        selectedPlayer = player
                        showPlayerDialog = true
                    }
                )
            }

            // 2. Substitutes Header
            item {
                SectionHeader(title = "SUBSTITUTES (${userTeam.getSubstitutes().size}/7)")
            }

            items(userTeam.getSubstitutes()) { player ->
                PlayerRowCard(
                    player = player,
                    userTeam = userTeam,
                    isStarting = false,
                    onClick = {
                        RfmAudioEngine.playClickSound()
                        selectedPlayer = player
                        showPlayerDialog = true
                    }
                )
            }

            // 3. Reserves Header
            val reserves = userTeam.getReserves()
            if (reserves.isNotEmpty()) {
                item {
                    SectionHeader(title = "RESERVES (${reserves.size})")
                }
                items(reserves) { player ->
                    PlayerRowCard(
                        player = player,
                        userTeam = userTeam,
                        isStarting = false,
                        onClick = {
                            RfmAudioEngine.playClickSound()
                            selectedPlayer = player
                            showPlayerDialog = true
                        }
                    )
                }
            }
        }
    }

    // Player Management Dialog
    if (showPlayerDialog && selectedPlayer != null) {
        val player = selectedPlayer!!
        AlertDialog(
            onDismissRequest = { showPlayerDialog = false },
            containerColor = RfmNavyDark,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(getPositionColor(player.position.roleCategory), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = player.position.label,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                    Column {
                        Text(
                            text = player.name,
                            color = RfmGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${player.nationality} • Age: ${player.age} • OVR: ${player.overall}",
                            color = RfmTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Attributes Row
                    Text("Attributes:", color = RfmTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RfmNavyCard, RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatPill("PAC", player.pace)
                        StatPill("SHO", player.shooting)
                        StatPill("PAS", player.passing)
                        StatPill("DRI", player.dribbling)
                        StatPill("DEF", player.defending)
                        StatPill("PHY", player.physical)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Condition: ${player.condition}% • Morale: ${player.morale}%",
                        color = RfmTextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Value: €${player.valueEuro / 1_000_000}M • Wage: €${player.wageWeeklyEuro / 1000}k/wk",
                        color = RfmTextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = RfmNavyBorder)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Tactical Roles Buttons
                    Button(
                        onClick = {
                            RfmAudioEngine.playClickSound()
                            userTeam.captainPlayerId = player.id
                            showPlayerDialog = false
                            onPlayerAction()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_set_captain"),
                        colors = ButtonDefaults.buttonColors(containerColor = RfmNavyCard),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = RfmGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Appoint Captain", color = RfmGold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            RfmAudioEngine.playClickSound()
                            userTeam.penaltyTakerId = player.id
                            showPlayerDialog = false
                            onPlayerAction()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_set_penalties"),
                        colors = ButtonDefaults.buttonColors(containerColor = RfmNavyCard),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = RfmNeonGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Appoint Penalty Taker", color = RfmNeonGreen, fontSize = 12.sp)
                    }

                    // Toggle Starting / Sub
                    Button(
                        onClick = {
                            RfmAudioEngine.playClickSound()
                            if (player.isStarting) {
                                player.isStarting = false
                                player.isSubstitute = true
                            } else {
                                player.isStarting = true
                                player.isSubstitute = false
                            }
                            showPlayerDialog = false
                            onPlayerAction()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_toggle_starting"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (player.isStarting) RfmWarningYellow.copy(alpha = 0.2f) else RfmPitchGreen
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (player.isStarting) "Move to Bench" else "Promote to Starting XI",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlayerDialog = false }) {
                    Text("Close", color = RfmGold)
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = RfmGold,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun PlayerRowCard(
    player: Player,
    userTeam: Team,
    isStarting: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("player_row_${player.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = RfmNavyCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isStarting) listOf(RfmNavyBorder, RfmNavyBorder) else listOf(Color.Transparent, Color.Transparent)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Pos + Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(getPositionColor(player.position.roleCategory), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.position.label,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.name,
                            color = RfmTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (userTeam.captainPlayerId == player.id) {
                            Text(
                                text = " (C)",
                                color = RfmGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Text(
                        text = "${player.nationality} • ${player.age} yrs",
                        color = RfmTextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            // Stats / Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Condition bar
                Text(
                    text = "${player.condition}%",
                    color = if (player.condition > 80) RfmNeonGreen else if (player.condition > 60) RfmWarningYellow else RfmDangerRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 12.dp)
                )

                // Overall badge
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(RfmNavySurface, CircleShape)
                        .border(1.dp, RfmGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${player.overall}",
                        color = RfmGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = RfmTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text("$value", color = RfmTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

private fun getPositionColor(category: String): Color {
    return when (category) {
        "Goalkeeper" -> Color(0xFFF59E0B)
        "Defender" -> Color(0xFF2563EB)
        "Midfielder" -> Color(0xFF10B981)
        "Forward" -> Color(0xFFDC2626)
        else -> Color(0xFF6B7280)
    }
}

private fun autoPickBestXI(team: Team) {
    // Reset starting status
    team.players.forEach {
        it.isStarting = false
        it.isSubstitute = false
    }

    // Best GK
    val bestGk = team.players.filter { it.position == com.example.rfm.model.PlayerPosition.GK }.maxByOrNull { it.overall }
    bestGk?.isStarting = true

    // Best 4 Defenders
    val defs = team.players.filter { it.position.roleCategory == "Defender" && !it.isStarting }
        .sortedByDescending { it.overall }.take(4)
    defs.forEach { it.isStarting = true }

    // Best 4 Midfielders
    val mids = team.players.filter { it.position.roleCategory == "Midfielder" && !it.isStarting }
        .sortedByDescending { it.overall }.take(4)
    mids.forEach { it.isStarting = true }

    // Best 2 Forwards
    val fwds = team.players.filter { it.position.roleCategory == "Forward" && !it.isStarting }
        .sortedByDescending { it.overall }.take(2)
    fwds.forEach { it.isStarting = true }

    // Best 7 remaining as substitutes
    val remaining = team.players.filter { !it.isStarting }.sortedByDescending { it.overall }.take(7)
    remaining.forEach { it.isSubstitute = true }
}

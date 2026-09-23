package com.example.rfm.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rfm.model.Player
import com.example.rfm.model.PlayerPosition
import com.example.rfm.model.Team
import com.example.rfm.model.TrainingFocus
import com.example.rfm.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

enum class SquadFilter {
    ALL, STARTING, BENCH, INJURED
}

@Composable
fun SquadScreen(
    userTeam: Team,
    onRenewContract: (Player) -> Unit = {},
    onPhysioTreat: (Player) -> Unit = {},
    onBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(SquadFilter.ALL) }
    var selectedPlayerId by remember {
        mutableStateOf(userTeam.getStartingXI().firstOrNull()?.id ?: userTeam.players.firstOrNull()?.id)
    }
    var squadChangeTrigger by remember { mutableStateOf(0) }

    val allPlayers = userTeam.players
    val displayedPlayers = remember(allPlayers, selectedFilter, squadChangeTrigger) {
        when (selectedFilter) {
            SquadFilter.ALL -> allPlayers
            SquadFilter.STARTING -> allPlayers.filter { it.isStarting }
            SquadFilter.BENCH -> allPlayers.filter { it.isSubstitute }
            SquadFilter.INJURED -> allPlayers.filter { it.isInjured || it.isSuspended || it.grievanceStatus != null }
        }
    }

    val selectedPlayer = remember(allPlayers, selectedPlayerId, squadChangeTrigger) {
        allPlayers.find { it.id == selectedPlayerId } ?: allPlayers.firstOrNull()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("squad_screen")
    ) {
        // TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SQUAD MANAGEMENT",
                    color = RfmGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${userTeam.name} • ${allPlayers.size} players",
                    color = RfmTextSecondary,
                    fontSize = 10.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {
                        val sorted = userTeam.players.sortedByDescending { it.overall }
                        userTeam.players.forEach {
                            it.isStarting = false
                            it.isSubstitute = false
                        }
                        sorted.take(11).forEach { it.isStarting = true }
                        sorted.drop(11).take(7).forEach { it.isSubstitute = true }
                        squadChangeTrigger++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RfmNavyCard),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("⚡ AUTO-PICK XI", color = RfmGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = RfmNavySurface),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("BACK", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // LANDSCAPE SPLIT VIEW: LEFT (Squad List 44%) & RIGHT (Player Profile, Radar & Training 56%)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // LEFT COLUMN: SQUAD LIST (44%)
            Column(
                modifier = Modifier
                    .weight(0.44f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Filter Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    SquadFilter.values().forEach { filter ->
                        val isSelected = selectedFilter == filter
                        val label = when (filter) {
                            SquadFilter.ALL -> "All (${allPlayers.size})"
                            SquadFilter.STARTING -> "XI (${allPlayers.count { it.isStarting }})"
                            SquadFilter.BENCH -> "Bench (${allPlayers.count { it.isSubstitute }})"
                            SquadFilter.INJURED -> "Alerts (${allPlayers.count { it.isInjured || it.isSuspended || it.grievanceStatus != null }})"
                        }
                        Surface(
                            color = if (isSelected) RfmPitchGreen else RfmNavyCard,
                            shape = RoundedCornerShape(3.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) RfmGold else RfmNavyBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedFilter = filter }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else RfmTextSecondary,
                                fontSize = 8.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                // Players List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    items(displayedPlayers) { player ->
                        val isSelected = selectedPlayer?.id == player.id
                        SquadListItemRow(
                            player = player,
                            isSelected = isSelected,
                            onClick = { selectedPlayerId = player.id }
                        )
                    }
                }
            }

            // RIGHT COLUMN: FULL PLAYER PROFILE, ATTRIBUTES RADAR & TRAINING (56%)
            Column(
                modifier = Modifier
                    .weight(0.56f)
                    .fillMaxHeight()
                    .background(RfmNavySurface, RoundedCornerShape(6.dp))
                    .border(1.dp, RfmNavyBorder, RoundedCornerShape(6.dp))
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedPlayer != null) {
                    PlayerProfileDeck(
                        player = selectedPlayer,
                        onRenewContract = {
                            onRenewContract(selectedPlayer)
                            squadChangeTrigger++
                        },
                        onPhysioTreat = {
                            onPhysioTreat(selectedPlayer)
                            squadChangeTrigger++
                        },
                        onToggleStatus = {
                            when {
                                selectedPlayer.isStarting -> {
                                    selectedPlayer.isStarting = false
                                    selectedPlayer.isSubstitute = true
                                }
                                selectedPlayer.isSubstitute -> {
                                    selectedPlayer.isSubstitute = false
                                }
                                else -> {
                                    selectedPlayer.isStarting = true
                                    selectedPlayer.isSubstitute = false
                                }
                            }
                            squadChangeTrigger++
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Select a player from the list to view profile", color = RfmTextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SquadListItemRow(
    player: Player,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) RfmAmber.copy(alpha = 0.22f) else RfmNavyCard,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) RfmAmber else RfmNavyBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Position badge
                Surface(
                    color = when (player.position.roleCategory) {
                        "Goalkeeper" -> Color(0xFFD32F2F)
                        "Defender" -> Color(0xFF1976D2)
                        "Midfielder" -> Color(0xFF388E3C)
                        else -> Color(0xFFF57C00)
                    },
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(
                        text = player.position.label,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = player.name,
                            color = if (isSelected) RfmGold else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (player.isInjured) {
                            Text("🏥", fontSize = 8.sp)
                        }
                        if (player.isSuspended) {
                            Text("🟥", fontSize = 8.sp)
                        }
                        if (player.grievanceStatus != null) {
                            Text("⚠️", fontSize = 8.sp)
                        }
                    }
                    Text(
                        text = "${if (player.isStarting) "Starting XI" else if (player.isSubstitute) "Bench" else "Reserve"} • ${player.condition}% fit",
                        color = if (player.condition >= 80) RfmNeonGreen else RfmAmber,
                        fontSize = 7.5.sp
                    )
                }
            }

            // OVR Badge
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(if (isSelected) RfmGold else RfmNavySurface, RoundedCornerShape(3.dp))
                    .border(0.5.dp, RfmGold, RoundedCornerShape(3.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${player.overall}",
                    color = if (isSelected) Color.Black else RfmGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun PlayerProfileDeck(
    player: Player,
    onRenewContract: () -> Unit,
    onPhysioTreat: () -> Unit,
    onToggleStatus: () -> Unit
) {
    // 1. Header: Name, Position, Age, OVR & Potential
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = player.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Surface(
                    color = when (player.position.roleCategory) {
                        "Goalkeeper" -> Color(0xFFD32F2F)
                        "Defender" -> Color(0xFF1976D2)
                        "Midfielder" -> Color(0xFF388E3C)
                        else -> Color(0xFFF57C00)
                    },
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Text(
                        text = "${player.position.roleCategory} (${player.position.label})",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = "Age ${player.age} • ${player.nationality} • Form: ${player.morale}% • Fit: ${player.condition}%",
                color = RfmTextSecondary,
                fontSize = 9.5.sp
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Overall
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("OVR", color = RfmTextSecondary, fontSize = 7.5.sp)
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(RfmGold, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${player.overall}", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
            }
            // Potential
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("POT", color = RfmTextSecondary, fontSize = 7.5.sp)
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(RfmNavyCard, RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF64B5F6), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${player.potential}", color = Color(0xFF64B5F6), fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }

    // 2. ATTRIBUTES RADAR & 6-PACK GRID
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Radar Chart (Canvas polygon)
        Box(
            modifier = Modifier
                .size(110.dp)
                .background(RfmNavyDark, RoundedCornerShape(6.dp))
                .border(1.dp, RfmNavyBorder, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            AttributesRadarChart(player = player)
        }

        // 6-pack key attributes
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AttributeTile("PAC", player.pace, Modifier.weight(1f))
                AttributeTile("SHO", player.shooting, Modifier.weight(1f))
                AttributeTile("PAS", player.passing, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AttributeTile("DRI", player.dribbling, Modifier.weight(1f))
                AttributeTile("DEF", player.defending, Modifier.weight(1f))
                AttributeTile("PHY", player.physical, Modifier.weight(1f))
            }
        }
    }

    // 3. INDIVIDUAL TRAINING FOCUS & XP
    var currentTrainingFocus by remember { mutableStateOf(player.trainingFocus) }
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "INDIVIDUAL TRAINING: ${currentTrainingFocus.icon} ${currentTrainingFocus.label}",
                color = RfmAmber,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "XP: ${player.trainingProgressPoints}/100",
                color = RfmGold,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LinearProgressIndicator(
            progress = { (player.trainingProgressPoints % 100) / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = RfmGold,
            trackColor = RfmNavyBorder
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(TrainingFocus.values()) { focus ->
                val isSelected = currentTrainingFocus == focus
                Surface(
                    color = if (isSelected) RfmPitchGreen else RfmNavyCard,
                    shape = RoundedCornerShape(3.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) RfmGold else RfmNavyBorder),
                    modifier = Modifier.clickable {
                        currentTrainingFocus = focus
                        player.trainingFocus = focus
                    }
                ) {
                    Text(
                        text = "${focus.icon} ${focus.label}",
                        color = if (isSelected) Color.White else RfmTextSecondary,
                        fontSize = 8.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }

    // 4. CONTRACT, VALUE & STATS
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Value: €${player.valueEuro / 1_000_000}M", color = RfmNeonGreen, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
            Text("Wage: €${player.wageWeeklyEuro / 1_000}k/wk", color = RfmGold, fontSize = 9.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Contract: ${player.contractYears}y left", color = Color.White, fontSize = 9.5.sp)
            Text("Apps: ${player.matchesPlayed} • G: ${player.goalsScored} • A: ${player.assists}", color = RfmTextSecondary, fontSize = 9.sp)
        }
    }

    // Alerts: Injured or Grievance
    if (player.isInjured) {
        Surface(
            color = Color(0xFFD32F2F).copy(alpha = 0.2f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🏥 Injured (${player.injuryType.ifEmpty { "Knock" }}): ${player.injuryWeeks} weeks remaining",
                color = RfmDangerRed,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(4.dp)
            )
        }
    }

    if (player.grievanceStatus != null) {
        Surface(
            color = RfmAmber.copy(alpha = 0.2f),
            border = androidx.compose.foundation.BorderStroke(1.dp, RfmAmber),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚠️ Grievance: ${player.grievanceStatus}",
                    color = RfmAmber,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = {
                        player.resolveGrievance("Talked with manager")
                        player.morale = (player.morale + 15).coerceAtMost(100)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RfmAmber),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(22.dp)
                ) {
                    Text("1-on-1 Talk", color = Color.Black, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // 5. ACTION BUTTONS: STATUS TOGGLE, CONTRACT RENEW, PHYSIO
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Button(
            onClick = onToggleStatus,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen),
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            Text(
                text = when {
                    player.isStarting -> "→ Move to Bench"
                    player.isSubstitute -> "→ Move to Reserves"
                    else -> "★ Promote to Starting XI"
                },
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = onRenewContract,
            modifier = Modifier.weight(0.7f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            Text("Renew (+2y)", fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }

        if (player.isInjured) {
            Button(
                onClick = onPhysioTreat,
                modifier = Modifier.weight(0.6f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Text("Physio (-1w)", fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AttributeTile(label: String, value: Int, modifier: Modifier = Modifier) {
    Surface(
        color = RfmNavyCard,
        shape = RoundedCornerShape(3.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, RfmNavyBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = RfmTextSecondary, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "$value",
                color = if (value >= 85) RfmGold else if (value >= 70) RfmNeonGreen else Color.White,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

/**
 * Visual Attributes Radar Chart (Hexagon spider web)
 */
@Composable
private fun AttributesRadarChart(player: Player) {
    val stats = listOf(
        Pair("PAC", player.pace),
        Pair("SHO", player.shooting),
        Pair("PAS", player.passing),
        Pair("DRI", player.dribbling),
        Pair("DEF", player.defending),
        Pair("PHY", player.physical)
    )

    Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
        val cx = size.width / 2
        val cy = size.height / 2
        val radius = size.minDimension / 2 * 0.85f

        // Draw web rings (3 rings: 33%, 66%, 100%)
        val rings = listOf(0.33f, 0.66f, 1.0f)
        rings.forEach { ringScale ->
            val ringPath = Path()
            for (i in 0 until 6) {
                val angle = Math.toRadians((i * 60 - 90).toDouble())
                val px = (cx + radius * ringScale * cos(angle)).toFloat()
                val py = (cy + radius * ringScale * sin(angle)).toFloat()
                if (i == 0) ringPath.moveTo(px, py) else ringPath.lineTo(px, py)
            }
            ringPath.close()
            drawPath(ringPath, color = Color(0x33FFFFFF), style = Stroke(width = 1f))
        }

        // Draw radial spokes
        for (i in 0 until 6) {
            val angle = Math.toRadians((i * 60 - 90).toDouble())
            val px = (cx + radius * cos(angle)).toFloat()
            val py = (cy + radius * sin(angle)).toFloat()
            drawLine(color = Color(0x22FFFFFF), start = Offset(cx, cy), end = Offset(px, py), strokeWidth = 1f)
        }

        // Draw Player Attribute polygon
        val statPath = Path()
        stats.forEachIndexed { i, (_, value) ->
            val fraction = (value / 100f).coerceIn(0.15f, 1.0f)
            val angle = Math.toRadians((i * 60 - 90).toDouble())
            val px = (cx + radius * fraction * cos(angle)).toFloat()
            val py = (cy + radius * fraction * sin(angle)).toFloat()
            if (i == 0) statPath.moveTo(px, py) else statPath.lineTo(px, py)
        }
        statPath.close()

        // Fill and stroke
        drawPath(statPath, color = RfmGold.copy(alpha = 0.35f))
        drawPath(statPath, color = RfmGold, style = Stroke(width = 2f))
    }
}

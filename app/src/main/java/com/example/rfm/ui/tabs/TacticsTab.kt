package com.example.rfm.ui.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rfm.audio.RfmAudioEngine
import com.example.rfm.model.*
import com.example.ui.theme.*

@Composable
fun TacticsTab(
    userTeam: Team,
    onTacticsChanged: () -> Unit
) {
    var selectedFormation by remember { mutableStateOf(userTeam.formation) }
    var selectedMentality by remember { mutableStateOf(userTeam.mentality) }
    var selectedPassing by remember { mutableStateOf(userTeam.passingStyle) }
    var selectedPressing by remember { mutableStateOf(userTeam.pressingStyle) }
    var selectedTackling by remember { mutableStateOf(userTeam.tacklingStyle) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
            .testTag("tactics_tab_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Ratings Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = RfmNavyCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(RfmGold, RfmNeonGreen)))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                TacticsRatingBadge("ATTACK", userTeam.calculateAttackRating(), RfmDangerRed)
                TacticsRatingBadge("MIDFIELD", userTeam.calculateMidfieldRating(), RfmNeonGreen)
                TacticsRatingBadge("DEFENSE", userTeam.calculateDefenseRating(), Color(0xFF3B82F6))
            }
        }

        // 2D Tactical Pitch Canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = RfmPitchDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(RfmPitchLine, RfmPitchLine)))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    // Grass stripes
                    val stripeW = w / 6f
                    for (i in 0 until 6) {
                        if (i % 2 == 0) {
                            drawRect(
                                color = Color(0x15FFFFFF),
                                topLeft = Offset(i * stripeW, 0f),
                                size = Size(stripeW, h)
                            )
                        }
                    }
                    // Outer border
                    drawRect(
                        color = Color(0x88FFFFFF),
                        topLeft = Offset(8f, 8f),
                        size = Size(w - 16f, h - 16f),
                        style = Stroke(width = 2f)
                    )
                    // Halfway line
                    drawLine(
                        color = Color(0x88FFFFFF),
                        start = Offset(w / 2f, 8f),
                        end = Offset(w / 2f, h - 8f),
                        strokeWidth = 2f
                    )
                    // Center circle
                    drawCircle(
                        color = Color(0x88FFFFFF),
                        radius = 28f,
                        center = Offset(w / 2f, h / 2f),
                        style = Stroke(width = 2f)
                    )
                    // Penalty boxes
                    drawRect(
                        color = Color(0x88FFFFFF),
                        topLeft = Offset(8f, h * 0.25f),
                        size = Size(w * 0.15f, h * 0.5f),
                        style = Stroke(width = 2f)
                    )
                    drawRect(
                        color = Color(0x88FFFFFF),
                        topLeft = Offset(w - 8f - w * 0.15f, h * 0.25f),
                        size = Size(w * 0.15f, h * 0.5f),
                        style = Stroke(width = 2f)
                    )
                }

                // Render Player Tokens on Pitch
                val startingPlayers = userTeam.getStartingXI()
                startingPlayers.forEachIndexed { index, player ->
                    val pos = getPitchPosition(index, startingPlayers.size, selectedFormation)
                    Box(
                        modifier = Modifier
                            .offset(x = (pos.first * 3.0).dp, y = (pos.second * 1.5).dp)
                            .size(24.dp)
                            .background(Color(userTeam.primaryColorHex), CircleShape)
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = player.position.label,
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Formation Selection
        TacticSelectorRow(
            title = "Formation",
            currentValue = selectedFormation.label,
            options = Formation.values().map { it.label },
            tag = "selector_formation",
            onSelected = { label ->
                val f = Formation.values().first { it.label == label }
                selectedFormation = f
                userTeam.formation = f
                onTacticsChanged()
            }
        )

        // Mentality Selection
        TacticSelectorRow(
            title = "Team Mentality",
            currentValue = selectedMentality.label,
            options = Mentality.values().map { it.label },
            tag = "selector_mentality",
            onSelected = { label ->
                val m = Mentality.values().first { it.label == label }
                selectedMentality = m
                userTeam.mentality = m
                onTacticsChanged()
            }
        )

        // Passing Style
        TacticSelectorRow(
            title = "Passing Style",
            currentValue = selectedPassing.label,
            options = PassingStyle.values().map { it.label },
            tag = "selector_passing",
            onSelected = { label ->
                val p = PassingStyle.values().first { it.label == label }
                selectedPassing = p
                userTeam.passingStyle = p
                onTacticsChanged()
            }
        )

        // Pressing Style
        TacticSelectorRow(
            title = "Pressing Intensity",
            currentValue = selectedPressing.label,
            options = PressingStyle.values().map { it.label },
            tag = "selector_pressing",
            onSelected = { label ->
                val pr = PressingStyle.values().first { it.label == label }
                selectedPressing = pr
                userTeam.pressingStyle = pr
                onTacticsChanged()
            }
        )

        // Tackling Style
        TacticSelectorRow(
            title = "Tackling Style",
            currentValue = selectedTackling.label,
            options = TacklingStyle.values().map { it.label },
            tag = "selector_tackling",
            onSelected = { label ->
                val t = TacklingStyle.values().first { it.label == label }
                selectedTackling = t
                userTeam.tacklingStyle = t
                onTacticsChanged()
            }
        )
    }
}

@Composable
private fun TacticsRatingBadge(label: String, rating: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = RfmTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(text = "$rating", color = color, fontSize = 20.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun TacticSelectorRow(
    title: String,
    currentValue: String,
    options: List<String>,
    tag: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RfmNavyCard, RoundedCornerShape(8.dp))
            .border(1.dp, RfmNavyBorder, RoundedCornerShape(8.dp))
            .clickable { expanded = true }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(tag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = title, color = RfmTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = currentValue, color = RfmGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = RfmTextSecondary)

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(RfmNavySurface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = if (option == currentValue) RfmGold else RfmTextPrimary,
                            fontWeight = if (option == currentValue) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        RfmAudioEngine.playClickSound()
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun getPitchPosition(index: Int, total: Int, formation: Formation): Pair<Float, Float> {
    // 0 = GK
    if (index == 0) return Pair(10f, 48f)
    val spread = (index) * (90f / total) + 5f
    val xBase = when {
        index in 1..4 -> 28f
        index in 5..8 -> 55f
        else -> 78f
    }
    return Pair(xBase, spread)
}

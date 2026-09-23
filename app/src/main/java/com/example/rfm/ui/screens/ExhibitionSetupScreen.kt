package com.example.rfm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SportsSoccer
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
fun ExhibitionSetupScreen(
    gameState: GameState,
    onBack: () -> Unit,
    onStartMatch: (homeTeam: Team, awayTeam: Team) -> Unit
) {
    val allTeams = remember { gameState.leagues.flatMap { it.teams } }
    var selectedHomeTeamId by remember { mutableStateOf(allTeams[0].id) }
    var selectedAwayTeamId by remember { mutableStateOf(if (allTeams.size > 1) allTeams[1].id else allTeams[0].id) }

    val homeTeam = allTeams.find { it.id == selectedHomeTeamId } ?: allTeams.first()
    val awayTeam = allTeams.find { it.id == selectedAwayTeamId } ?: allTeams.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .testTag("exhibition_setup_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RfmNavySurface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    RfmAudioEngine.playClickSound()
                    onBack()
                },
                modifier = Modifier.testTag("exhibition_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = RfmTextPrimary)
            }
            Text(
                text = "EXHIBITION MATCH",
                color = RfmGold,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Versus Preview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = RfmNavyCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(RfmGold, RfmNeonGreen)))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(homeTeam.primaryColorHex), CircleShape)
                            .border(2.dp, Color(homeTeam.secondaryColorHex), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(homeTeam.shortName, color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                    Text(homeTeam.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                    Text("OVR: ${homeTeam.calculateTeamStrength()}", color = RfmNeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Text("VS", color = RfmGold, fontWeight = FontWeight.Black, fontSize = 24.sp)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(awayTeam.primaryColorHex), CircleShape)
                            .border(2.dp, Color(awayTeam.secondaryColorHex), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(awayTeam.shortName, color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                    Text(awayTeam.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                    Text("OVR: ${awayTeam.calculateTeamStrength()}", color = RfmNeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Team Pickers
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Home selection list
            Column(modifier = Modifier.weight(1f)) {
                Text("SELECT HOME", color = RfmGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(allTeams) { team ->
                        val isSelected = team.id == selectedHomeTeamId
                        TeamMiniCard(team = team, isSelected = isSelected) {
                            RfmAudioEngine.playClickSound()
                            selectedHomeTeamId = team.id
                        }
                    }
                }
            }

            // Away selection list
            Column(modifier = Modifier.weight(1f)) {
                Text("SELECT AWAY", color = RfmNeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(allTeams) { team ->
                        val isSelected = team.id == selectedAwayTeamId
                        TeamMiniCard(team = team, isSelected = isSelected) {
                            RfmAudioEngine.playClickSound()
                            selectedAwayTeamId = team.id
                        }
                    }
                }
            }
        }

        // Kickoff Button
        Button(
            onClick = {
                RfmAudioEngine.playWhistleShort()
                onStartMatch(homeTeam, awayTeam)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .height(50.dp)
                .testTag("btn_start_exhibition_match"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RfmGold, contentColor = Color.Black)
        ) {
            Icon(Icons.Default.SportsSoccer, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("KICK OFF EXHIBITION MATCH", fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
    }
}

@Composable
private fun TeamMiniCard(team: Team, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) RfmNavyCard else RfmNavySurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (isSelected) Brush.horizontalGradient(listOf(RfmGold, RfmNeonGreen)) else Brush.horizontalGradient(listOf(RfmNavyBorder, RfmNavyBorder))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(team.primaryColorHex), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(team.shortName.take(2), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = team.shortName,
                color = if (isSelected) RfmGold else RfmTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

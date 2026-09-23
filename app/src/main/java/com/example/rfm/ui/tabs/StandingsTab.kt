package com.example.rfm.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.rfm.model.TeamStanding
import com.example.ui.theme.*

@Composable
fun StandingsTab(
    gameState: GameState
) {
    val userLeague = gameState.getUserLeague() ?: gameState.leagues.first()
    var selectedView by remember { mutableStateOf("TABLE") } // TABLE, SCORERS, FIXTURES

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(12.dp)
            .testTag("standings_tab_screen")
    ) {
        // Toggle Buttons (Table, Scorers, Fixtures)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("TABLE", "SCORERS", "FIXTURES").forEach { view ->
                val isSelected = selectedView == view
                Button(
                    onClick = {
                        RfmAudioEngine.playClickSound()
                        selectedView = view
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) RfmPitchGreen else RfmNavyCard
                    ),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text(
                        text = view,
                        color = if (isSelected) Color.White else RfmTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        when (selectedView) {
            "TABLE" -> {
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RfmNavySurface, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#", color = RfmTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                    Text("CLUB", color = RfmTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("PL", color = RfmTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
                    Text("W", color = RfmTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                    Text("D", color = RfmTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                    Text("L", color = RfmTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                    Text("GD", color = RfmTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                    Text("PTS", color = RfmGold, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(28.dp))
                }

                // Table Rows
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(userLeague.standings) { index, standing ->
                        val isUser = standing.teamId == gameState.userTeamId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isUser) RfmGold.copy(alpha = 0.15f) else RfmNavyCard)
                                .border(
                                    0.5.dp,
                                    if (isUser) RfmGold else RfmNavyBorder
                                )
                                .padding(horizontal = 8.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = if (index < 4) RfmNeonGreen else RfmTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(20.dp)
                            )
                            Text(
                                text = standing.teamName,
                                color = if (isUser) RfmGold else RfmTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isUser) FontWeight.Black else FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Text("${standing.played}", color = RfmTextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp))
                            Text("${standing.won}", color = RfmTextSecondary, fontSize = 11.sp, modifier = Modifier.width(20.dp))
                            Text("${standing.drawn}", color = RfmTextSecondary, fontSize = 11.sp, modifier = Modifier.width(20.dp))
                            Text("${standing.lost}", color = RfmTextSecondary, fontSize = 11.sp, modifier = Modifier.width(20.dp))
                            Text("${standing.goalDifference}", color = RfmTextSecondary, fontSize = 11.sp, modifier = Modifier.width(28.dp))
                            Text("${standing.points}", color = if (isUser) RfmGold else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(28.dp))
                        }
                    }
                }
            }

            "SCORERS" -> {
                // Top Scorers across league teams
                val topScorers = userLeague.teams.flatMap { it.players }
                    .sortedByDescending { it.goalsScored }
                    .take(15)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(topScorers) { index, player ->
                        val team = userLeague.teams.find { it.players.any { p -> p.id == player.id } }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            colors = CardDefaults.cardColors(containerColor = RfmNavyCard),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(RfmNavyBorder, RfmNavyBorder)))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${index + 1}.", color = RfmGold, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
                                    Column {
                                        Text(player.name, color = RfmTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${team?.name ?: ""} • ${player.position.label}", color = RfmTextSecondary, fontSize = 10.sp)
                                    }
                                }
                                Text("${player.goalsScored} Goals", color = RfmNeonGreen, fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            "FIXTURES" -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(gameState.fixtures) { fixture ->
                        val homeTeam = gameState.getTeamById(fixture.homeTeamId)
                        val awayTeam = gameState.getTeamById(fixture.awayTeamId)
                        val isUserMatch = fixture.homeTeamId == gameState.userTeamId || fixture.awayTeamId == gameState.userTeamId

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUserMatch) RfmGold.copy(alpha = 0.1f) else RfmNavyCard
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = if (isUserMatch) Brush.horizontalGradient(listOf(RfmGold, RfmGoldDark)) else Brush.horizontalGradient(listOf(RfmNavyBorder, RfmNavyBorder))
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("MD ${fixture.round}", color = RfmTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                                Text(
                                    text = "${homeTeam?.shortName ?: "HOM"} ${if (fixture.isPlayed) "${fixture.homeScore} - ${fixture.awayScore}" else "vs"} ${awayTeam?.shortName ?: "AWY"}",
                                    color = if (isUserMatch) RfmGold else RfmTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    color = if (fixture.isPlayed) RfmPitchGreen else RfmNavySurface,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (fixture.isPlayed) "PLAYED" else "UPCOMING",
                                        color = if (fixture.isPlayed) Color.White else RfmTextSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.rfm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rfm.model.GameState
import com.example.rfm.navigation.RfmScreen
import com.example.rfm.ui.components.RfmTopBar

@Composable
fun TrophyCabinetScreen(
    gameState: GameState,
    onNavigate: (RfmScreen) -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val userTeam = gameState.getUserTeam()

    // Calculated career statistics
    val playedFixtures = gameState.fixtures.filter { it.isPlayed }
    val userFixtures = playedFixtures.filter { it.homeTeamId == gameState.userTeamId || it.awayTeamId == gameState.userTeamId }
    val wins = userFixtures.count { fix ->
        (fix.homeTeamId == gameState.userTeamId && (fix.homeScore ?: 0) > (fix.awayScore ?: 0)) ||
        (fix.awayTeamId == gameState.userTeamId && (fix.awayScore ?: 0) > (fix.homeScore ?: 0))
    }
    val draws = userFixtures.count { fix -> (fix.homeScore ?: 0) == (fix.awayScore ?: 0) }
    val losses = userFixtures.size - wins - draws
    val winRate = if (userFixtures.isNotEmpty()) ((wins.toFloat() / userFixtures.size) * 100).toInt() else 0

    // Top Goalscorer across league
    val allPlayers = gameState.leagues.flatMap { it.teams }.flatMap { it.players }
    val topScorer = allPlayers.maxByOrNull { it.goalsScored }
    val topAssister = allPlayers.maxByOrNull { it.assists }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1923))
    ) {
        RfmTopBar(
            title = "TROPHY CABINET",
            subtitle = "Silverware & Manager Records",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Trophy Showcase
            Text(
                "TROPHY SHOWCASE",
                color = Color(0xFFFFD700),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TrophyCard(
                    title = "Domestic League",
                    icon = "🏆",
                    count = if (wins >= 15) 1 else 0,
                    modifier = Modifier.weight(1f)
                )
                TrophyCard(
                    title = "Champions Cup",
                    icon = "🌟",
                    count = if (wins >= 25) 1 else 0,
                    modifier = Modifier.weight(1f)
                )
                TrophyCard(
                    title = "National Cup",
                    icon = "🥇",
                    count = if (wins >= 8) 1 else 0,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Career Record Card
            Text(
                "CAREER MILESTONES (${gameState.managerName})",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2833)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    RecordStatRow("Current Club", userTeam?.name ?: "N/A")
                    RecordStatRow("Matches Managed", "${userFixtures.size}")
                    RecordStatRow("Record (W - D - L)", "$wins - $draws - $losses")
                    RecordStatRow("Win Ratio", "$winRate%")
                    RecordStatRow("Board Confidence", "${gameState.boardConfidence}%")
                    RecordStatRow("Manager Reputation", "${gameState.managerReputation} / 100")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Season Individual Accolades
            Text(
                "INDIVIDUAL HONORS (2012/13)",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2833)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👟", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("GOLDEN BOOT LEADER", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${topScorer?.name ?: "N/A"} (${topScorer?.goalsScored ?: 0} Goals)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎯", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("MOST ASSISTS", color = Color(0xFF64B5F6), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${topAssister?.name ?: "N/A"} (${topAssister?.assists ?: 0} Assists)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrophyCard(title: String, icon: String, count: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .border(
                1.dp,
                if (count > 0) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f),
                RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (count > 0) Color(0xFF263228) else Color(0xFF1E2833)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                title,
                color = if (count > 0) Color(0xFFFFD700) else Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "x$count",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RecordStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.LightGray, fontSize = 12.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

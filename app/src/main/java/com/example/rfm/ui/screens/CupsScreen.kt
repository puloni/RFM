package com.example.rfm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rfm.model.CupCompetition
import com.example.rfm.model.CupMatch
import com.example.rfm.model.GameState
import com.example.rfm.ui.theme.*

@Composable
fun CupsScreen(
    gameState: GameState,
    onSimulateCupRound: (CupCompetition) -> Unit,
    onBack: () -> Unit
) {
    var selectedCupIndex by remember { mutableStateOf(0) }
    val competitions = gameState.cupCompetitions

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(12.dp)
            .testTag("cups_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CUP TOURNAMENTS",
                    color = RfmGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Knockout Brackets & Continental Glory",
                    color = RfmTextSecondary,
                    fontSize = 11.sp
                )
            }
            TextButton(onClick = onBack) {
                Text("Back", color = RfmTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (competitions.isNotEmpty()) {
            TabRow(
                selectedTabIndex = selectedCupIndex,
                containerColor = RfmNavySurface,
                contentColor = RfmGold
            ) {
                competitions.forEachIndexed { index, comp ->
                    Tab(
                        selected = selectedCupIndex == index,
                        onClick = { selectedCupIndex = index },
                        text = {
                            Text(
                                text = "${comp.trophyIcon} ${comp.name}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val currentCup = competitions.getOrNull(selectedCupIndex)
            if (currentCup != null) {
                // Cup Info Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(RfmNavyCard)
                        .border(1.dp, RfmGold, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = currentCup.name,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentCup.isCompleted) "🏆 Winner: ${gameState.getTeamById(currentCup.winnerTeamId ?: "")?.name ?: "Crown Decided"}"
                                else "Prize Purse: €${currentCup.prizeMoneyEuro / 1_000_000}M",
                                color = RfmNeonGreen,
                                fontSize = 11.sp
                            )
                        }

                        if (!currentCup.isCompleted) {
                            Button(
                                onClick = { onSimulateCupRound(currentCup) },
                                colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Simulate Round", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Match List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentCup.matches) { match ->
                        val home = gameState.getTeamById(match.homeTeamId)
                        val away = gameState.getTeamById(match.awayTeamId)
                        CupMatchCard(
                            match = match,
                            homeTeamName = home?.name ?: match.homeTeamId,
                            awayTeamName = away?.name ?: match.awayTeamId,
                            userTeamId = gameState.userTeamId
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No cup competitions scheduled.", color = RfmTextSecondary)
            }
        }
    }
}

@Composable
private fun CupMatchCard(
    match: CupMatch,
    homeTeamName: String,
    awayTeamName: String,
    userTeamId: String
) {
    val isUserMatch = match.homeTeamId == userTeamId || match.awayTeamId == userTeamId

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(RfmNavyCard)
            .border(
                1.dp,
                if (isUserMatch) RfmGold else RfmNavyBorder,
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = match.roundName.uppercase(),
                color = RfmAmber,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = homeTeamName,
                    color = if (match.homeTeamId == userTeamId) RfmGold else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = if (match.isPlayed) {
                        "${match.homeScore ?: 0} - ${match.awayScore ?: 0}" +
                            (if (match.penaltiesHome != null) " (${match.penaltiesHome}-${match.penaltiesAway} pen)" else "")
                    } else "vs",
                    color = if (match.isPlayed) RfmNeonGreen else RfmTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Text(
                    text = awayTeamName,
                    color = if (match.awayTeamId == userTeamId) RfmGold else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

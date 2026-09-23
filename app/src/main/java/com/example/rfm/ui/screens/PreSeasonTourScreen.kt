package com.example.rfm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rfm.model.GameState
import com.example.rfm.model.PreSeasonTourFixture
import com.example.rfm.ui.theme.*

@Composable
fun PreSeasonTourScreen(
    gameState: GameState,
    onPlayTourMatch: (PreSeasonTourFixture) -> Unit,
    onSimulateTourMatch: (PreSeasonTourFixture) -> Unit,
    onBack: () -> Unit
) {
    val userTeam = gameState.getUserTeam()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(12.dp)
            .testTag("pre_season_tour_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PRE-SEASON GLOBAL TOURS",
                    color = RfmGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Commercial Friendlies, Match Sharpness & Squad Preparation",
                    color = RfmTextSecondary,
                    fontSize = 11.sp
                )
            }
            TextButton(onClick = onBack) {
                Text("Back", color = RfmTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Commercial Tour Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, RfmAmber, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = RfmNavySurface),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "🌍 2012/13 SUMMER WORLD PREPARATION TOUR", color = RfmAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Touring international markets boosts squad conditioning, tests tactical readiness, and earns lucrative commercial appearance bonuses directly into your club balance.",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "TOUR FIXTURES & RESULTS", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(gameState.preSeasonFixtures) { tourMatch ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (tourMatch.isPlayed) RfmNavyBorder else RfmGold, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = RfmNavyCard),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🚩 ${tourMatch.tourLocation.uppercase()}",
                                color = RfmAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "+€${tourMatch.commercialBonusEuro / 1_000}k Revenue",
                                color = RfmNeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Teams & Score
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${userTeam?.shortName ?: "User"} vs ${tourMatch.opponentName}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (tourMatch.isPlayed) {
                                Text(
                                    text = "${tourMatch.userScore} - ${tourMatch.opponentScore}",
                                    color = RfmGold,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            } else {
                                Text(
                                    text = "Scheduled",
                                    color = RfmTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Actions for unplayed matches
                        if (!tourMatch.isPlayed) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onPlayTourMatch(tourMatch) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("Play Match", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { onSimulateTourMatch(tourMatch) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = RfmNavySurface),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("Quick Sim", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RfmGold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.rfm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import com.example.rfm.model.Fixture
import com.example.rfm.model.GameState
import com.example.rfm.navigation.RfmScreen
import com.example.rfm.ui.theme.*

@Composable
fun DashboardScreen(
    gameState: GameState,
    onNavigate: (RfmScreen) -> Unit,
    onStartMatch: (Fixture) -> Unit,
    onAnswerPressConference: (Int, Fixture) -> Unit = { _, f -> onStartMatch(f) },
    onAdvanceToNextSeason: () -> Unit = {}
) {
    val userTeam = gameState.getUserTeam()
    val nextFixture = gameState.getNextFixtureForUser()
    val opponentTeam = if (nextFixture != null) {
        val oppId = if (nextFixture.homeTeamId == gameState.userTeamId) nextFixture.awayTeamId else nextFixture.homeTeamId
        gameState.getTeamById(oppId)
    } else null

    var showPressConferenceDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("dashboard_screen"),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // LEFT COLUMN (46%): MATCHDAY HERO, BOARD CONFIDENCE, TRANSFERS ALERT
        Column(
            modifier = Modifier
                .weight(0.46f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // NEXT MATCH HERO CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, RfmGold, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = RfmNavySurface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NEXT FIXTURE • MATCHDAY ${gameState.currentMatchDay}",
                        color = RfmAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (nextFixture != null && opponentTeam != null && userTeam != null) {
                        val isHome = nextFixture.homeTeamId == userTeam.id
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TeamMatchBadge(name = userTeam.name, isHome = isHome)
                            Text(
                                text = "VS",
                                color = RfmGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            TeamMatchBadge(name = opponentTeam.name, isHome = !isHome)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isHome) "Home @ ${userTeam.stadiumName}" else "Away @ ${opponentTeam.stadiumName}",
                                color = RfmTextSecondary,
                                fontSize = 10.sp
                            )
                            Text(" • ", color = RfmTextSecondary, fontSize = 10.sp)
                            Text(
                                text = "${nextFixture.weather.icon} ${nextFixture.weather.label} (${nextFixture.weather.tempCelsius}°C)",
                                color = RfmGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showPressConferenceDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .testTag("play_match_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RfmPitchGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            Text(
                                text = "▶ ENTER MATCHDAY (PRESS & KICK-OFF)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.5.sp
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🏆 SEASON ${gameState.currentSeasonYear}/${gameState.currentSeasonYear + 1} COMPLETED!",
                                color = RfmGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Trophies awarded and prize money ready.",
                                color = RfmNeonGreen,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = onAdvanceToNextSeason,
                                colors = ButtonDefaults.buttonColors(containerColor = RfmAmber),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "ADVANCE TO NEXT SEASON (${gameState.currentSeasonYear + 1}) >>",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // BOARD CONFIDENCE & OBJECTIVE
            if (userTeam != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RfmNavyBorder, RoundedCornerShape(6.dp)),
                    colors = CardDefaults.cardColors(containerColor = RfmNavySurface),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "BOARD STATUS: ${userTeam.seasonObjective.label}",
                                color = RfmAmber,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Confidence: ${gameState.boardConfidence}% • Reputation: ${gameState.managerReputation}%",
                                color = if (gameState.boardConfidence >= 60) RfmNeonGreen else RfmDangerRed,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    if (gameState.boardConfidence >= 75) RfmPitchGreen
                                    else if (gameState.boardConfidence >= 50) Color(0xFFE65100)
                                    else RfmDangerRed,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (gameState.boardConfidence >= 75) "SECURE" else if (gameState.boardConfidence >= 50) "SATISFIED" else "AT RISK",
                                color = Color.White,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // PENDING TRANSFER BIDS ALERT
            val pendingBids = gameState.activeTransferOffers.filter { it.isIncomingToUser && it.status == "PENDING" }
            if (pendingBids.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RfmGold, RoundedCornerShape(6.dp))
                        .clickable { onNavigate(RfmScreen.TRANSFERS) },
                    colors = CardDefaults.cardColors(containerColor = RfmNavyCard),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🚨", fontSize = 16.sp)
                            Column {
                                Text(
                                    text = "INCOMING TRANSFER BID (${pendingBids.size})",
                                    color = RfmGold,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                                val topBid = pendingBids.first()
                                Text(
                                    text = "${topBid.fromTeamName} offered €${topBid.offerAmountEuro / 1_000_000}M for ${topBid.playerName}!",
                                    color = Color.White,
                                    fontSize = 9.5.sp
                                )
                            }
                        }
                        Text(
                            text = "Review >",
                            color = RfmNeonGreen,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // INBOX & CLUB NEWS
            Text(
                text = "INBOX & CLUB NEWS",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                gameState.newsInbox.take(3).forEach { news ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(RfmNavyCard)
                            .border(1.dp, if (news.isImportant) RfmGold else RfmNavyBorder, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = news.title,
                                    color = if (news.isImportant) RfmGold else Color.White,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = news.dateString,
                                    color = RfmTextMuted,
                                    fontSize = 8.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = news.content,
                                color = RfmTextSecondary,
                                fontSize = 9.sp,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }

        // RIGHT COLUMN (54%): MANAGEMENT HUB GRID
        Column(
            modifier = Modifier
                .weight(0.54f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "MANAGEMENT HUB",
                color = RfmAmber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DashboardHubButton(
                    title = "SQUAD",
                    subtitle = "${userTeam?.players?.size ?: 0} Players",
                    icon = "⚽",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(RfmScreen.SQUAD) },
                    tag = "hub_squad_button"
                )
                DashboardHubButton(
                    title = "TACTICS",
                    subtitle = "${userTeam?.formation?.label} • ${userTeam?.mentality?.label}",
                    icon = "📋",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(RfmScreen.TACTICS) },
                    tag = "hub_tactics_button"
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DashboardHubButton(
                    title = "TRANSFERS",
                    subtitle = "€${(userTeam?.transferBudgetEuro ?: 0) / 1_000_000}M Budget",
                    icon = "🔄",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(RfmScreen.TRANSFERS) },
                    tag = "hub_transfers_button"
                )
                DashboardHubButton(
                    title = "TRAINING",
                    subtitle = gameState.currentTrainingFocus,
                    icon = "🏋️",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(RfmScreen.TRAINING) },
                    tag = "hub_training_button"
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DashboardHubButton(
                    title = "FINANCES",
                    subtitle = "Facilities & Stadium",
                    icon = "🏛️",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(RfmScreen.FINANCES) },
                    tag = "hub_finances_button"
                )
                DashboardHubButton(
                    title = "TABLE",
                    subtitle = "Standings & Stats",
                    icon = "📊",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(RfmScreen.TABLE) },
                    tag = "hub_table_button"
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DashboardHubButton(
                    title = "FIXTURES",
                    subtitle = "Season Schedule",
                    icon = "📅",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(RfmScreen.FIXTURES) },
                    tag = "hub_fixtures_button"
                )
                DashboardHubButton(
                    title = "SAVE / LOAD",
                    subtitle = "Slots & Autosave",
                    icon = "💾",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(RfmScreen.LOAD_SAVE) },
                    tag = "hub_save_button"
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DashboardHubButton(
                    title = "CUPS",
                    subtitle = "${gameState.cupCompetitions.size} Tournaments",
                    icon = "🏆",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(RfmScreen.CUPS) },
                    tag = "hub_cups_button"
                )
                DashboardHubButton(
                    title = "ACADEMY",
                    subtitle = "${gameState.youthAcademyProspects.size} Prospects & Scouts",
                    icon = "⭐",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(RfmScreen.YOUTH_ACADEMY) },
                    tag = "hub_academy_button"
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DashboardHubButton(
                    title = "STAFF",
                    subtitle = "${gameState.backroomStaff.size} Backroom Staff",
                    icon = "👔",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(RfmScreen.STAFF_MANAGEMENT) },
                    tag = "hub_staff_button"
                )
                DashboardHubButton(
                    title = "TROPHIES",
                    subtitle = "Cabinet & Records",
                    icon = "🥇",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(RfmScreen.TROPHY_CABINET) },
                    tag = "hub_trophies_button"
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DashboardHubButton(
                    title = "PRE-SEASON",
                    subtitle = "${gameState.preSeasonFixtures.count { !it.isPlayed }} Tours",
                    icon = "🌍",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(RfmScreen.PRE_SEASON_TOUR) },
                    tag = "hub_tours_button"
                )
                DashboardHubButton(
                    title = "EXHIBITION",
                    subtitle = "Quick Friendly Match",
                    icon = "⚡",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(RfmScreen.EXHIBITION_SETUP) },
                    tag = "hub_exhibition_button"
                )
            }
        }
    }

    if (showPressConferenceDialog && nextFixture != null && opponentTeam != null) {
        Dialog(onDismissRequest = { showPressConferenceDialog = false }) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = RfmNavyDark),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .border(2.dp, RfmGold, RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📰 PRE-MATCH PRESS CONFERENCE",
                        color = RfmGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "The Press asks: \"Manager ${gameState.managerName}, what is your game plan against ${opponentTeam.name}?\"",
                        color = Color.White,
                        fontSize = 11.sp
                    )

                    Button(
                        onClick = {
                            showPressConferenceDialog = false
                            onAnswerPressConference(0, nextFixture)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text("1. \"We will attack from the first whistle!\"", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showPressConferenceDialog = false
                            onAnswerPressConference(1, nextFixture)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text("2. \"They are dangerous; discipline is key.\"", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showPressConferenceDialog = false
                            onAnswerPressConference(2, nextFixture)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RfmNavyCard),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text("3. \"No comments. We do our talking on the pitch.\"", fontSize = 10.sp, color = RfmTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamMatchBadge(name: String, isHome: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isHome) "(Home)" else "(Away)",
            color = RfmTextSecondary,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun DashboardHubButton(
    title: String,
    subtitle: String,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(RfmNavyCard)
            .border(1.dp, RfmNavyBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(8.dp)
            .testTag(tag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 17.sp, modifier = Modifier.padding(end = 6.dp))
            Column {
                Text(
                    text = title,
                    color = RfmGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = RfmTextSecondary,
                    fontSize = 8.5.sp,
                    maxLines = 1
                )
            }
        }
    }
}

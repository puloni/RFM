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
import com.example.rfm.model.GameState
import com.example.rfm.model.Player
import com.example.rfm.model.YouthProspect
import com.example.rfm.ui.theme.*

@Composable
fun YouthAcademyScreen(
    gameState: GameState,
    onPromoteProspect: (YouthProspect) -> Unit,
    onSendScout: (region: String, cost: Long) -> Unit,
    onSignScoutedPlayer: (Player) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val userTeam = gameState.getUserTeam()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(12.dp)
            .testTag("youth_academy_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ACADEMY & SCOUTING",
                    color = RfmGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${userTeam?.name} • Youth Facility Level ${userTeam?.youthFacilityLevel ?: 1}",
                    color = RfmTextSecondary,
                    fontSize = 11.sp
                )
            }
            TextButton(onClick = onBack) {
                Text("Back", color = RfmTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = RfmNavySurface,
            contentColor = RfmGold
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Youth Academy (${gameState.youthAcademyProspects.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Scouting Network (${gameState.scoutedPlayers.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedTab == 0) {
            // YOUTH ACADEMY PROSPECTS
            if (gameState.youthAcademyProspects.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No youth prospects currently available.\nUpgrade your Youth Facility in Finances to recruit new talents!",
                        color = RfmTextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(gameState.youthAcademyProspects) { prospect ->
                        YouthProspectCard(
                            prospect = prospect,
                            onPromote = { onPromoteProspect(prospect) }
                        )
                    }
                }
            }
        } else {
            // SCOUTING NETWORK
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "DISPATCH REGIONAL SCOUTS",
                        color = RfmAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScoutMissionButton(
                            region = "Western Europe",
                            cost = 1_000_000L,
                            flag = "🇪🇺",
                            modifier = Modifier.weight(1f),
                            onSendScout = { onSendScout("Western Europe", 1_000_000L) }
                        )
                        ScoutMissionButton(
                            region = "South America",
                            cost = 1_500_000L,
                            flag = "🇧🇷",
                            modifier = Modifier.weight(1f),
                            onSendScout = { onSendScout("South America", 1_500_000L) }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScoutMissionButton(
                            region = "Eastern Europe",
                            cost = 800_000L,
                            flag = "🌍",
                            modifier = Modifier.weight(1f),
                            onSendScout = { onSendScout("Eastern Europe", 800_000L) }
                        )
                        ScoutMissionButton(
                            region = "Africa",
                            cost = 700_000L,
                            flag = "🌍",
                            modifier = Modifier.weight(1f),
                            onSendScout = { onSendScout("Africa", 700_000L) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "SCOUTED TALENTS REPORT (${gameState.scoutedPlayers.size})",
                        color = RfmGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (gameState.scoutedPlayers.isEmpty()) {
                    item {
                        Text(
                            "No scouted players yet. Dispatch scouts above to discover talents worldwide!",
                            color = RfmTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                } else {
                    items(gameState.scoutedPlayers) { player ->
                        ScoutedPlayerCard(
                            player = player,
                            onSign = { onSignScoutedPlayer(player) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YouthProspectCard(
    prospect: YouthProspect,
    onPromote: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(RfmNavyCard)
            .border(1.dp, RfmNavyBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = prospect.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⭐ ${prospect.specialty}",
                        color = RfmAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "${prospect.position.roleCategory} (${prospect.position.label}) • Age ${prospect.age} • ${prospect.nationality}",
                    color = RfmTextSecondary,
                    fontSize = 11.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text("OVR: ${prospect.overall}", color = RfmGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("POTENTIAL: ${prospect.potential}+", color = RfmNeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text("Wage: €${prospect.wageWeeklyEuro / 1000}k/wk", color = RfmTextSecondary, fontSize = 11.sp)
                }
            }

            Button(
                onClick = onPromote,
                colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Promote", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ScoutMissionButton(
    region: String,
    cost: Long,
    flag: String,
    modifier: Modifier = Modifier,
    onSendScout: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(RfmNavySurface)
            .border(1.dp, RfmGold, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$flag $region", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("Fee: €${cost / 1_000_000.0}M", color = RfmAmber, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onSendScout,
                colors = ButtonDefaults.buttonColors(containerColor = RfmNavyCard),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Text("Dispatch Scout", fontSize = 10.sp, color = RfmGold)
            }
        }
    }
}

@Composable
private fun ScoutedPlayerCard(
    player: Player,
    onSign: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(RfmNavyCard)
            .border(1.dp, RfmNavyBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${player.position.roleCategory} (${player.position.label}) • Age ${player.age} • ${player.nationality}",
                    color = RfmTextSecondary,
                    fontSize = 11.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text("OVR: ${player.overall}", color = RfmGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("POT: ${player.potential}+", color = RfmNeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text("Val: €${player.valueEuro / 1_000_000}M", color = RfmTextSecondary, fontSize = 11.sp)
                }
            }

            Button(
                onClick = onSign,
                colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Sign", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

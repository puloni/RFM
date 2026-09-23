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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rfm.model.League
import com.example.rfm.model.Team
import com.example.rfm.ui.theme.*

@Composable
fun TeamSelectScreen(
    leagues: List<League>,
    onTeamSelected: (teamId: String, managerName: String) -> Unit,
    onBack: () -> Unit
) {
    var selectedLeagueIndex by remember { mutableStateOf(0) }
    var selectedTeamId by remember { mutableStateOf("man_utd") }
    var managerName by remember { mutableStateOf("Sir Manager") }

    val currentLeague = leagues.getOrNull(selectedLeagueIndex) ?: leagues.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(12.dp)
            .testTag("team_select_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SELECT YOUR CLUB",
                color = RfmGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            TextButton(onClick = onBack) {
                Text("Back", color = RfmTextSecondary)
            }
        }

        // Manager Name Field
        OutlinedTextField(
            value = managerName,
            onValueChange = { managerName = it },
            label = { Text("Manager Name", color = RfmTextSecondary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = RfmGold,
                unfocusedBorderColor = RfmNavyBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .testTag("manager_name_input")
        )

        // League Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedLeagueIndex,
            containerColor = RfmNavySurface,
            contentColor = RfmGold,
            edgePadding = 4.dp
        ) {
            leagues.forEachIndexed { index, league ->
                Tab(
                    selected = selectedLeagueIndex == index,
                    onClick = {
                        selectedLeagueIndex = index
                        selectedTeamId = league.teams.firstOrNull()?.id ?: ""
                    },
                    text = {
                        Text(
                            text = league.name,
                            fontWeight = if (selectedLeagueIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Teams in selected league
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(currentLeague.teams) { team ->
                TeamSelectionCard(
                    team = team,
                    isSelected = team.id == selectedTeamId,
                    onSelect = { selectedTeamId = team.id }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Confirm Button
        Button(
            onClick = { onTeamSelected(selectedTeamId, managerName) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("confirm_team_selection_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = RfmPitchGreen,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "APPOINT AS MANAGER & BEGIN SEASON",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun TeamSelectionCard(
    team: Team,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) RfmGold else RfmNavyBorder
    val bgColor = if (isSelected) RfmNavyCard else RfmNavySurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(12.dp)
            .testTag("team_card_${team.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = team.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Stadium: ${team.stadiumName} (${team.stadiumCapacity} seats)",
                    color = RfmTextSecondary,
                    fontSize = 11.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "Rep: ${"★".repeat(team.reputationStars)}",
                        color = RfmAmber,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Balance: €${team.balanceEuro / 1_000_000}M",
                        color = RfmNeonGreen,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Transfer: €${team.transferBudgetEuro / 1_000_000}M",
                        color = RfmGold,
                        fontSize = 11.sp
                    )
                }
            }

            if (isSelected) {
                Text(
                    text = "SELECTED",
                    color = RfmGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

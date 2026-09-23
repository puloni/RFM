package com.example.rfm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.rfm.ui.theme.*

@Composable
fun TableScreen(
    leagues: List<League>,
    userTeamId: String,
    onBack: () -> Unit
) {
    var selectedLeagueIndex by remember { mutableStateOf(0) }
    val currentLeague = leagues.getOrNull(selectedLeagueIndex) ?: leagues.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(10.dp)
            .testTag("table_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LEAGUE STANDINGS",
                color = RfmGold,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )
            TextButton(onClick = onBack) {
                Text("Back", color = RfmTextSecondary)
            }
        }

        // League Switcher Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedLeagueIndex,
            containerColor = RfmNavySurface,
            contentColor = RfmGold,
            edgePadding = 4.dp
        ) {
            leagues.forEachIndexed { index, l ->
                Tab(
                    selected = selectedLeagueIndex == index,
                    onClick = { selectedLeagueIndex = index },
                    text = { Text(text = l.name, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RfmNavyCard, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "#", color = RfmTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
            Text(text = "CLUB", color = RfmTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(text = "P", color = RfmTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
            Text(text = "W", color = RfmTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
            Text(text = "D", color = RfmTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
            Text(text = "L", color = RfmTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
            Text(text = "GD", color = RfmTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp))
            Text(text = "PTS", color = RfmGold, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(32.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Standings Rows
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(currentLeague.standings) { index, s ->
                val isUserTeam = s.teamId == userTeamId
                val posColor = when (index) {
                    0 -> Color(0xFFFFD700) // Champion
                    in 1..3 -> Color(0xFF00E676) // Champions League
                    4 -> Color(0xFF00E5FF) // Europa League
                    else -> Color.White
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isUserTeam) RfmPitchGreen.copy(alpha = 0.5f) else RfmNavySurface)
                        .border(1.dp, if (isUserTeam) RfmGold else RfmNavyBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "${index + 1}", color = posColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
                        Text(text = s.teamName, color = if (isUserTeam) RfmGold else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1)
                        Text(text = "${s.played}", color = RfmTextSecondary, fontSize = 11.sp, modifier = Modifier.width(28.dp))
                        Text(text = "${s.won}", color = RfmTextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp))
                        Text(text = "${s.drawn}", color = RfmTextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp))
                        Text(text = "${s.lost}", color = RfmTextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp))
                        Text(text = "${s.goalDifference}", color = if (s.goalDifference >= 0) RfmNeonGreen else RfmDangerRed, fontSize = 11.sp, modifier = Modifier.width(32.dp))
                        Text(text = "${s.points}", color = RfmGold, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(32.dp))
                    }
                }
            }
        }
    }
}

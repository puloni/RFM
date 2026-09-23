package com.example.rfm.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.rfm.model.*

@Composable
fun MatchAnalyticsDialog(
    fixture: Fixture,
    homeTeam: Team,
    awayTeam: Team,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Overview & xG, 1: Shot Map, 2: Player Ratings

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141E28))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MATCH ANALYTICS",
                            color = Color(0xFFFFD700),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${homeTeam.shortName} ${fixture.homeScore ?: 0} - ${fixture.awayScore ?: 0} ${awayTeam.shortName} (${fixture.weather.icon} ${fixture.weather.label})",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Text("✕", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("STATISTICS", "SHOT MAP", "RATINGS").forEachIndexed { index, tabTitle ->
                        val isSelected = selectedTab == index
                        Button(
                            onClick = { selectedTab = index },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .padding(horizontal = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color(0xFF1976D2) else Color(0xFF263238),
                                contentColor = if (isSelected) Color.White else Color.LightGray
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(tabTitle, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> MatchStatsTab(fixture, homeTeam, awayTeam)
                        1 -> ShotMapTab(fixture, homeTeam, awayTeam)
                        2 -> PlayerRatingsTab(fixture, homeTeam, awayTeam)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("CLOSE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MatchStatsTab(fixture: Fixture, homeTeam: Team, awayTeam: Team) {
    val stats = fixture.stats
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // xG Comparison Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A38))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${homeTeam.shortName} xG: ${stats.homeXg}",
                        color = Color(0xFF64B5F6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text("EXPECTED GOALS", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${awayTeam.shortName} xG: ${stats.awayXg}",
                        color = Color(0xFFFF8A80),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                val totalXg = (stats.homeXg + stats.awayXg).coerceAtLeast(0.1f)
                val homeFraction = (stats.homeXg / totalXg).coerceIn(0.1f, 0.9f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(Color.Black, RoundedCornerShape(5.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(homeFraction)
                            .background(Color(0xFF1976D2), RoundedCornerShape(topStart = 5.dp, bottomStart = 5.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f - homeFraction)
                            .background(Color(0xFFD32F2F), RoundedCornerShape(topEnd = 5.dp, bottomEnd = 5.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Man of the Match Banner
        fixture.manOfTheMatchName?.let { motmName ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E3B1C))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⭐", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("PLAYER OF THE MATCH", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(motmName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Detailed Match Metrics Comparison
        StatComparisonRow("Possession", "${stats.homePossession}%", "${stats.awayPossession}%", stats.homePossession > stats.awayPossession)
        StatComparisonRow("Total Shots", "${stats.homeShots}", "${stats.awayShots}", stats.homeShots > stats.awayShots)
        StatComparisonRow("Shots on Target", "${stats.homeShotsOnTarget}", "${stats.awayShotsOnTarget}", stats.homeShotsOnTarget > stats.awayShotsOnTarget)
        StatComparisonRow("Pass Accuracy", "${stats.homePassAccuracy}%", "${stats.awayPassAccuracy}%", stats.homePassAccuracy > stats.awayPassAccuracy)
        StatComparisonRow("Tackle Success", "${stats.homeTackleSuccessRate}%", "${stats.awayTackleSuccessRate}%", stats.homeTackleSuccessRate > stats.awayTackleSuccessRate)
        StatComparisonRow("Corner Kicks", "${stats.homeCorners}", "${stats.awayCorners}", stats.homeCorners > stats.awayCorners)
        StatComparisonRow("Fouls Committed", "${stats.homeFouls}", "${stats.awayFouls}", false)
        StatComparisonRow("Yellow Cards", "${stats.homeYellowCards}", "${stats.awayYellowCards}", false)
    }
}

@Composable
fun StatComparisonRow(label: String, homeVal: String, awayVal: String, isHomeBetter: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2833)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                homeVal,
                color = if (isHomeBetter) Color(0xFF64B5F6) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(label, color = Color.LightGray, fontSize = 12.sp)
            Text(
                awayVal,
                color = if (!isHomeBetter) Color(0xFFFF8A80) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun ShotMapTab(fixture: Fixture, homeTeam: Team, awayTeam: Team) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Shot Locations & Expected Goals (xG)",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendItem("Goal", Color(0xFF4CAF50))
            LegendItem("Saved", Color(0xFF2196F3))
            LegendItem("Woodwork", Color(0xFFFF9800))
            LegendItem("Off Target", Color(0xFF9E9E9E))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2D Pitch Canvas for Shot Map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF2E7D32), RoundedCornerShape(8.dp))
                .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Pitch lines
                drawLine(Color.White.copy(alpha = 0.5f), Offset(0f, h / 2), Offset(w, h / 2), strokeWidth = 2f)
                drawCircle(Color.White.copy(alpha = 0.5f), radius = w * 0.15f, center = Offset(w / 2, h / 2), style = Stroke(width = 2f))

                // Penalty boxes
                drawRect(Color.White.copy(alpha = 0.5f), topLeft = Offset(w * 0.2f, 0f), size = Size(w * 0.6f, h * 0.18f), style = Stroke(width = 2f))
                drawRect(Color.White.copy(alpha = 0.5f), topLeft = Offset(w * 0.2f, h * 0.82f), size = Size(w * 0.6f, h * 0.18f), style = Stroke(width = 2f))

                // Render shots
                fixture.shotMap.forEach { shot ->
                    val shotColor = when (shot.outcome) {
                        ShotOutcome.GOAL -> Color(0xFF4CAF50)
                        ShotOutcome.SAVED -> Color(0xFF2196F3)
                        ShotOutcome.WOODWORK -> Color(0xFFFF9800)
                        ShotOutcome.OFF_TARGET, ShotOutcome.BLOCKED -> Color(0xFFE0E0E0)
                    }
                    val radius = (shot.xG * 18f).coerceIn(6f, 18f)
                    val px = shot.pitchX * w
                    val py = shot.pitchY * h

                    drawCircle(shotColor, radius = radius, center = Offset(px, py))
                    drawCircle(Color.Black, radius = radius, center = Offset(px, py), style = Stroke(width = 2f))
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = Color.LightGray, fontSize = 10.sp)
    }
}

@Composable
fun PlayerRatingsTab(fixture: Fixture, homeTeam: Team, awayTeam: Team) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Text("${homeTeam.name} Ratings", color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        homeTeam.getStartingXI().forEach { p ->
            val rating = fixture.playerRatings[p.id] ?: p.matchRating
            PlayerRatingRow(p, rating, isMotm = p.id == fixture.manOfTheMatchId)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("${awayTeam.name} Ratings", color = Color(0xFFFF8A80), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        awayTeam.getStartingXI().forEach { p ->
            val rating = fixture.playerRatings[p.id] ?: p.matchRating
            PlayerRatingRow(p, rating, isMotm = p.id == fixture.manOfTheMatchId)
        }
    }
}

@Composable
fun PlayerRatingRow(player: Player, rating: Float, isMotm: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = if (isMotm) Color(0xFF2E3B1C) else Color(0xFF1E2833)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    player.position.label,
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(36.dp)
                )
                Text(
                    player.name + if (isMotm) " ⭐ MOTM" else "",
                    color = if (isMotm) Color(0xFFFFD700) else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            // Rating pill
            val badgeColor = when {
                rating >= 8.0f -> Color(0xFF2E7D32)
                rating >= 6.8f -> Color(0xFF1565C0)
                rating >= 5.8f -> Color(0xFFF57F17)
                else -> Color(0xFFC62828)
            }
            Box(
                modifier = Modifier
                    .background(badgeColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    String.format("%.1f", rating),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

package com.example.rfm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.rfm.model.BackroomStaff
import com.example.rfm.model.GameState
import com.example.rfm.ui.theme.*

@Composable
fun StaffManagementScreen(
    gameState: GameState,
    onUpgradeStaff: (BackroomStaff) -> Unit,
    onHireStaff: (BackroomStaff) -> Unit,
    onBack: () -> Unit
) {
    val userTeam = gameState.getUserTeam()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(12.dp)
            .testTag("staff_management_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BACKROOM STAFF MANAGEMENT",
                    color = RfmGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Club Balance: €${(userTeam?.balanceEuro ?: 0) / 1_000_000}M • Weekly Staff Wages: €${gameState.backroomStaff.sumOf { it.weeklySalaryEuro } / 1000}k/wk",
                    color = RfmTextSecondary,
                    fontSize = 11.sp
                )
            }
            TextButton(onClick = onBack) {
                Text("Back", color = RfmTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Staff List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(gameState.backroomStaff) { staff ->
                val upgradeCost = staff.upgradeCostEuro
                val canUpgrade = staff.level < 5 && (userTeam?.balanceEuro ?: 0L) >= upgradeCost

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RfmNavyBorder, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = RfmNavySurface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(RfmNavyCard, CircleShape)
                                        .border(1.dp, RfmGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (staff.role) {
                                            "Assistant Manager" -> "👔"
                                            "Head Scout" -> "🔍"
                                            "Chief Physio" -> "🏥"
                                            else -> "💪"
                                        },
                                        fontSize = 18.sp
                                    )
                                }
                                Column {
                                    Text(
                                        text = staff.name,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = staff.role,
                                        color = RfmAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Star Rating Badge
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(5) { i ->
                                    Text(
                                        text = if (i < staff.level) "★" else "☆",
                                        color = if (i < staff.level) RfmGold else RfmTextSecondary,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }

                        // Specialty & Impact Description
                        Text(
                            text = staff.perkDescription,
                            color = RfmNeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Wage & Upgrade Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Wage: €${staff.weeklySalaryEuro / 1000}k/wk",
                                color = RfmTextSecondary,
                                fontSize = 11.sp
                            )

                            if (staff.level < 5) {
                                Button(
                                    onClick = { onUpgradeStaff(staff) },
                                    enabled = canUpgrade,
                                    colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Upgrade (Level ${staff.level + 1}) • €${upgradeCost / 1_000}k",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(RfmGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("MAX LEVEL (WORLD CLASS)", color = RfmGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

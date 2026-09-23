package com.example.rfm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rfm.model.SponsorContract
import com.example.rfm.model.Team
import com.example.rfm.model.TicketPriceTier
import com.example.rfm.ui.theme.*

@Composable
fun FinancesScreen(
    userTeam: Team,
    onUpgradeFacility: (type: String) -> Boolean,
    onSetTicketPriceTier: (TicketPriceTier) -> Unit = {},
    onSignSponsor: (category: String, SponsorContract) -> Unit = { _, _ -> },
    onBack: () -> Unit
) {
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var selectedTier by remember { mutableStateOf(userTeam.ticketPriceTier) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(12.dp)
            .testTag("finances_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CLUB INFRASTRUCTURE",
                    color = RfmGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Finances & Stadium Facilities",
                    color = RfmTextSecondary,
                    fontSize = 11.sp
                )
            }
            TextButton(onClick = onBack) {
                Text("Back", color = RfmTextSecondary)
            }
        }

        if (feedbackMessage != null) {
            Text(
                text = feedbackMessage!!,
                color = RfmNeonGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Balance Card
            Card(
                colors = CardDefaults.cardColors(containerColor = RfmNavySurface),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, RfmNavyBorder, RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "TREASURY & BUDGETS", color = RfmAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Overall Balance:", color = Color.White, fontSize = 13.sp)
                        Text(text = "€${userTeam.balanceEuro / 1_000_000}M", color = RfmNeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Transfer Budget:", color = Color.White, fontSize = 13.sp)
                        Text(text = "€${userTeam.transferBudgetEuro / 1_000_000}M", color = RfmGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Weekly Wage Bill:", color = Color.White, fontSize = 13.sp)
                        Text(text = "€${userTeam.wageBudgetEuroWeekly / 1_000}k/wk", color = RfmTextSecondary, fontSize = 13.sp)
                    }
                }
            }

            // TICKET PRICING STRATEGY
            Card(
                colors = CardDefaults.cardColors(containerColor = RfmNavySurface),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, RfmNavyBorder, RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "MATCHDAY TICKET PRICING", color = RfmAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TicketPriceTier.values().forEach { tier ->
                            val isSelected = selectedTier == tier
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelected) RfmNavyCard else RfmNavyDark, RoundedCornerShape(6.dp))
                                    .border(1.dp, if (isSelected) RfmGold else RfmNavyBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        selectedTier = tier
                                        userTeam.ticketPriceTier = tier
                                        onSetTicketPriceTier(tier)
                                        feedbackMessage = "Ticket price set to ${tier.label}!"
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = tier.name.take(4),
                                        color = if (isSelected) RfmGold else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "€${tier.priceEuro}",
                                        color = if (isSelected) RfmNeonGreen else RfmTextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                    val estAttendance = (userTeam.stadiumCapacity * 0.85f * selectedTier.attendanceMultiplier).toInt().coerceAtMost(userTeam.stadiumCapacity)
                    val estIncome = estAttendance.toLong() * selectedTier.priceEuro
                    Text(
                        text = "Est. Attendance: ${estAttendance} / ${userTeam.stadiumCapacity} • Est. Income: €${estIncome / 1_000}k / match",
                        color = RfmTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            // COMMERCIAL SPONSORSHIPS
            Card(
                colors = CardDefaults.cardColors(containerColor = RfmNavySurface),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, RfmNavyBorder, RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "COMMERCIAL SPONSORSHIPS", color = RfmAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Shirt: ${userTeam.shirtSponsor.sponsorName}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "€${userTeam.shirtSponsor.weeklyIncomeEuro / 1_000}k/wk (${userTeam.shirtSponsor.contractDurationYears} yrs remaining)", color = RfmTextSecondary, fontSize = 10.sp)
                        }
                        Button(
                            onClick = {
                                val newSponsor = SponsorContract("Shirt Sponsor", "Fly Emirates Global", 420_000L, 4)
                                onSignSponsor("Shirt Sponsor", newSponsor)
                                feedbackMessage = "New Shirt deal signed with ${newSponsor.sponsorName}!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Renegotiate", fontSize = 10.sp)
                        }
                    }

                    Divider(color = RfmNavyBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Stadium Naming: ${userTeam.stadiumSponsor.sponsorName}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "€${userTeam.stadiumSponsor.weeklyIncomeEuro / 1_000}k/wk (${userTeam.stadiumSponsor.contractDurationYears} yrs remaining)", color = RfmTextSecondary, fontSize = 10.sp)
                        }
                        Button(
                            onClick = {
                                val newSponsor = SponsorContract("Stadium Naming", "Allianz Arena Partnership", 280_000L, 5)
                                onSignSponsor("Stadium Naming", newSponsor)
                                feedbackMessage = "New Stadium naming deal signed with ${newSponsor.sponsorName}!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Renegotiate", fontSize = 10.sp)
                        }
                    }
                }
            }

            // Facilities
            Text(text = "STADIUM & INFRASTRUCTURE UPGRADES (€5M EACH)", color = RfmAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)

            FacilityUpgradeRow(
                title = "Stadium Expansion",
                description = "${userTeam.stadiumName} (Capacity: ${userTeam.stadiumCapacity} seats)",
                level = userTeam.stadiumLevel,
                onUpgrade = {
                    val ok = onUpgradeFacility("stadium")
                    feedbackMessage = if (ok) "Stadium expanded by 5,000 seats!" else "Upgrade failed: Insufficient funds."
                }
            )

            FacilityUpgradeRow(
                title = "Training Grounds",
                description = "Modern training fields accelerate player skill progression.",
                level = userTeam.trainingFacilityLevel,
                onUpgrade = {
                    val ok = onUpgradeFacility("training")
                    feedbackMessage = if (ok) "Training facility upgraded!" else "Upgrade failed: Insufficient funds."
                }
            )

            FacilityUpgradeRow(
                title = "Medical & Rehab Center",
                description = "Specialized physio staff reduces player injury convalescence.",
                level = userTeam.medicalFacilityLevel,
                onUpgrade = {
                    val ok = onUpgradeFacility("medical")
                    feedbackMessage = if (ok) "Medical clinic upgraded!" else "Upgrade failed: Insufficient funds."
                }
            )

            FacilityUpgradeRow(
                title = "Youth Development Academy",
                description = "Attracts high-potential scouted prospects into youth ranks.",
                level = userTeam.youthFacilityLevel,
                onUpgrade = {
                    val ok = onUpgradeFacility("youth")
                    feedbackMessage = if (ok) "Youth academy upgraded!" else "Upgrade failed: Insufficient funds."
                }
            )
        }
    }
}

@Composable
private fun FacilityUpgradeRow(
    title: String,
    description: String,
    level: Int,
    onUpgrade: () -> Unit
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
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Lvl $level", color = RfmGold, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
                Text(text = description, color = RfmTextSecondary, fontSize = 10.sp)
            }

            Button(
                onClick = onUpgrade,
                colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(text = "Upgrade", fontSize = 11.sp)
            }
        }
    }
}

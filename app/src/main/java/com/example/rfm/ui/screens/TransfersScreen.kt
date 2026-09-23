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
import androidx.compose.ui.window.Dialog
import com.example.rfm.model.Player
import com.example.rfm.model.Team
import com.example.rfm.model.TransferOffer
import com.example.rfm.ui.theme.*

enum class TransferTab(val label: String) {
    TRANSFERS("Transfers"),
    FREE_AGENTS("Free Agents"),
    LOANS("Loan Deals"),
    INCOMING_BIDS("Club Bids")
}

@Composable
fun TransfersScreen(
    userTeam: Team,
    marketPlayers: List<Player>,
    freeAgents: List<Player> = emptyList(),
    loanMarket: List<Player> = emptyList(),
    incomingOffers: List<TransferOffer> = emptyList(),
    onBuyPlayer: (player: Player, fee: Long, wage: Long) -> Boolean,
    onSignFreeAgent: (player: Player, wage: Long) -> Boolean = { _, _ -> true },
    onLoanPlayer: (player: Player, loanFee: Long, wage: Long) -> Boolean = { _, _, _ -> true },
    onRespondOffer: (offer: TransferOffer, accept: Boolean, counterFee: Long?) -> Unit = { _, _, _ -> },
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(TransferTab.TRANSFERS) }
    var selectedPlayer by remember { mutableStateOf<Player?>(null) }
    var bidMessage by remember { mutableStateOf<String?>(null) }

    val currentList = when (selectedTab) {
        TransferTab.TRANSFERS -> marketPlayers
        TransferTab.FREE_AGENTS -> freeAgents
        TransferTab.LOANS -> loanMarket
        TransferTab.INCOMING_BIDS -> emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(12.dp)
            .testTag("transfers_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TRANSFER & LOAN MARKET",
                    color = RfmGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Transfer Budget: €${userTeam.transferBudgetEuro / 1_000_000}M • Club Funds: €${userTeam.balanceEuro / 1_000_000}M",
                    color = RfmNeonGreen,
                    fontSize = 11.sp
                )
            }
            TextButton(onClick = onBack) {
                Text("Back", color = RfmTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // DEADLINE DAY TICKER BANNER
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = RfmAmber.copy(alpha = 0.15f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, RfmAmber)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(color = RfmAmber, shape = RoundedCornerShape(3.dp)) {
                    Text(
                        text = "⚡ DEADLINE TICKER",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "AI Clubs active! Top clubs hunting signings. Scout & secure deals before window shut!",
                    color = Color.White,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Segmented Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(RfmNavyCard)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TransferTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) RfmPitchGreen else Color.Transparent)
                        .clickable {
                            selectedTab = tab
                            bidMessage = null
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        color = if (isSelected) Color.White else RfmTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        if (bidMessage != null) {
            Text(
                text = bidMessage!!,
                color = RfmNeonGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Players List or Incoming Bids
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedTab == TransferTab.INCOMING_BIDS) {
                if (incomingOffers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No active bids currently pending for your squad players.",
                                color = RfmTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(incomingOffers) { offer ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, RfmGold, RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = RfmNavyCard)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${offer.playerName} (${offer.playerPosition.label})",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Offer from: ${offer.fromTeamName} • Player OVR: ${offer.playerOverall}",
                                            color = RfmTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = "€${offer.offerAmountEuro / 1_000_000}M",
                                        color = RfmNeonGreen,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { onRespondOffer(offer, true, null) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen)
                                    ) {
                                        Text("Accept", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    val counterFee = (offer.offerAmountEuro * 1.25f).toLong()
                                    Button(
                                        onClick = { onRespondOffer(offer, false, counterFee) },
                                        modifier = Modifier.weight(1.2f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                                    ) {
                                        Text("Counter (€${counterFee / 1_000_000}M)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { onRespondOffer(offer, false, null) },
                                        modifier = Modifier.weight(0.8f),
                                        colors = ButtonDefaults.buttonColors(containerColor = RfmDangerRed)
                                    ) {
                                        Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (currentList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No players currently available in this category.",
                                color = RfmTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                items(currentList) { player ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(RfmNavyCard)
                        .border(1.dp, RfmNavyBorder, RoundedCornerShape(8.dp))
                        .clickable { selectedPlayer = player }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "[${player.position.label}]",
                                    color = RfmAmber,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = player.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Age ${player.age} • ${player.nationality} • Ovr ${player.overall} • Pot ${player.potential}",
                                color = RfmTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            when (selectedTab) {
                                TransferTab.TRANSFERS -> {
                                    Text(
                                        text = "€${player.valueEuro / 1_000_000}M",
                                        color = RfmGold,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "€${player.wageWeeklyEuro / 1_000}k/wk",
                                        color = RfmNeonGreen,
                                        fontSize = 10.sp
                                    )
                                }
                                TransferTab.FREE_AGENTS -> {
                                    Text(
                                        text = "FREE (Bosman)",
                                        color = RfmNeonGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "€${player.wageWeeklyEuro / 1_000}k/wk",
                                        color = RfmGold,
                                        fontSize = 10.sp
                                    )
                                }
                                TransferTab.LOANS -> {
                                    val loanFee = (player.valueEuro * 0.10).toLong().coerceAtLeast(500_000L)
                                    Text(
                                        text = "Loan Fee: €${loanFee / 1_000_000}M",
                                        color = Color(0xFF64B5F6),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "€${player.wageWeeklyEuro / 1_000}k/wk (1 Season)",
                                        color = RfmNeonGreen,
                                        fontSize = 10.sp
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}

    // Bid / Sign / Loan Confirmation Dialog
    selectedPlayer?.let { player ->
        Dialog(onDismissRequest = { selectedPlayer = null }) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = RfmNavyDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, RfmGold, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val title = when (selectedTab) {
                        TransferTab.TRANSFERS -> "SUBMIT TRANSFER OFFER"
                        TransferTab.FREE_AGENTS -> "SIGN FREE AGENT (BOSMAN)"
                        TransferTab.LOANS -> "SEASON LOAN AGREEMENT"
                        else -> "CONFIRM TRANSACTION"
                    }

                    Text(text = title, color = RfmGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Player: ${player.name} (${player.position.label}, Ovr ${player.overall}, Pot ${player.potential})", color = Color.White, fontSize = 12.sp)

                    when (selectedTab) {
                        TransferTab.TRANSFERS -> {
                            Text(text = "Transfer Fee: €${player.valueEuro / 1_000_000}M", color = RfmGold, fontSize = 13.sp)
                            Text(text = "Weekly Wage: €${player.wageWeeklyEuro / 1_000}k/wk (3 Years)", color = RfmNeonGreen, fontSize = 13.sp)
                        }
                        TransferTab.FREE_AGENTS -> {
                            Text(text = "Transfer Fee: €0 (Free Bosman Transfer)", color = RfmNeonGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Weekly Wage: €${player.wageWeeklyEuro / 1_000}k/wk (2 Years)", color = RfmGold, fontSize = 13.sp)
                        }
                        TransferTab.LOANS -> {
                            val loanFee = (player.valueEuro * 0.10).toLong().coerceAtLeast(500_000L)
                            Text(text = "Loan Fee: €${loanFee / 1_000_000}M", color = Color(0xFF64B5F6), fontSize = 13.sp)
                            Text(text = "Weekly Wage: €${player.wageWeeklyEuro / 1_000}k/wk (1 Year Loan)", color = RfmNeonGreen, fontSize = 13.sp)
                        }
                        else -> {}
                    }

                    Divider(color = RfmNavyBorder)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val success = when (selectedTab) {
                                    TransferTab.TRANSFERS -> onBuyPlayer(player, player.valueEuro, player.wageWeeklyEuro)
                                    TransferTab.FREE_AGENTS -> onSignFreeAgent(player, player.wageWeeklyEuro)
                                    TransferTab.LOANS -> {
                                        val loanFee = (player.valueEuro * 0.10).toLong().coerceAtLeast(500_000L)
                                        onLoanPlayer(player, loanFee, player.wageWeeklyEuro)
                                    }
                                    else -> false
                                }

                                bidMessage = if (success) {
                                    when (selectedTab) {
                                        TransferTab.TRANSFERS -> "Contract Signed! ${player.name} has joined the squad."
                                        TransferTab.FREE_AGENTS -> "Free Agent Secured! ${player.name} signed on a free transfer."
                                        TransferTab.LOANS -> "Loan Deal Finalized! ${player.name} arrives for the season."
                                        else -> "Transaction completed."
                                    }
                                } else {
                                    "Offer Rejected: Insufficient club transfer/budget funds."
                                }
                                selectedPlayer = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen)
                        ) {
                            Text(
                                text = when (selectedTab) {
                                    TransferTab.TRANSFERS -> "Sign Player"
                                    TransferTab.FREE_AGENTS -> "Sign Free Agent"
                                    TransferTab.LOANS -> "Confirm Loan"
                                    else -> "Confirm"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { selectedPlayer = null },
                            modifier = Modifier.weight(0.6f)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

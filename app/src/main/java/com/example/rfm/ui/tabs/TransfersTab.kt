package com.example.rfm.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rfm.audio.RfmAudioEngine
import com.example.rfm.model.GameState
import com.example.rfm.model.Player
import com.example.rfm.model.PlayerPosition
import com.example.ui.theme.*

@Composable
fun TransfersTab(
    gameState: GameState,
    onTransferCompleted: () -> Unit
) {
    val userTeam = gameState.getUserTeam() ?: return
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedPlayerToSign by remember { mutableStateOf<Player?>(null) }
    var showBidDialog by remember { mutableStateOf(false) }
    var bidFeedbackMessage by remember { mutableStateOf<String?>(null) }

    // Aggregate market players (Free agents + scouting pool from other clubs)
    val availablePlayers = remember(selectedCategory, gameState.transferMarket.size) {
        val pool = mutableListOf<Player>()
        pool.addAll(gameState.transferMarket)
        // Add non-user team stars as scouted targets
        for (league in gameState.leagues) {
            for (team in league.teams) {
                if (team.id != userTeam.id) {
                    team.players.take(2).forEach { pool.add(it) }
                }
            }
        }
        if (selectedCategory == "ALL") pool
        else pool.filter { it.position.roleCategory.uppercase().startsWith(selectedCategory) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(12.dp)
            .testTag("transfers_tab_screen")
    ) {
        // Budget Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = RfmNavyCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(RfmGold, RfmNeonGreen)))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TRANSFER BUDGET", color = RfmTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("€${userTeam.transferBudgetEuro / 1_000_000}M", color = RfmGold, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("WAGE BUDGET", color = RfmTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("€${userTeam.wageBudgetEuroWeekly / 1_000}k / wk", color = RfmNeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Category Filter Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val categories = listOf("ALL", "GOALKEEPER", "DEFENDER", "MIDFIELDER", "FORWARD")
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Button(
                    onClick = {
                        RfmAudioEngine.playClickSound()
                        selectedCategory = cat
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) RfmPitchGreen else RfmNavyCard
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (cat == "ALL") "All Positions" else cat.take(3),
                        color = if (isSelected) Color.White else RfmTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Market Player List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(availablePlayers) { player ->
                MarketPlayerCard(
                    player = player,
                    onSignClick = {
                        RfmAudioEngine.playClickSound()
                        selectedPlayerToSign = player
                        showBidDialog = true
                    }
                )
            }
        }
    }

    // Sign Player Dialog
    if (showBidDialog && selectedPlayerToSign != null) {
        val player = selectedPlayerToSign!!
        var offeredFeeMillion by remember { mutableStateOf((player.valueEuro / 1_000_000).coerceAtLeast(1).toString()) }

        AlertDialog(
            onDismissRequest = { showBidDialog = false },
            containerColor = RfmNavyDark,
            title = {
                Text(
                    text = "Transfer Bid: ${player.name}",
                    color = RfmGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Position: ${player.position.label} • Age: ${player.age} • OVR: ${player.overall}",
                        color = RfmTextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Market Valuation: €${player.valueEuro / 1_000_000}M",
                        color = RfmTextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Weekly Wage: €${player.wageWeeklyEuro / 1000}k / wk",
                        color = RfmTextSecondary,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = offeredFeeMillion,
                        onValueChange = { offeredFeeMillion = it },
                        label = { Text("Transfer Offer (€ Millions)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = RfmTextPrimary,
                            unfocusedTextColor = RfmTextPrimary,
                            focusedBorderColor = RfmGold,
                            unfocusedBorderColor = RfmNavyBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_transfer_fee")
                    )

                    if (bidFeedbackMessage != null) {
                        Text(
                            text = bidFeedbackMessage!!,
                            color = RfmNeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val feeM = offeredFeeMillion.toLongOrNull() ?: (player.valueEuro / 1_000_000)
                        val totalCost = feeM * 1_000_000L

                        val canAfford = gameState.modSettings.unlimitedTransferBudget ||
                                gameState.modSettings.unlimitedMoney ||
                                userTeam.transferBudgetEuro >= totalCost

                        if (canAfford) {
                            RfmAudioEngine.playWhistleShort()
                            if (!gameState.modSettings.unlimitedTransferBudget && !gameState.modSettings.unlimitedMoney) {
                                userTeam.transferBudgetEuro -= totalCost
                            }
                            // Add player copy to user squad
                            val signed = player.copyPlayer()
                            signed.isStarting = false
                            signed.isSubstitute = true
                            userTeam.players.add(signed)
                            gameState.transferMarket.remove(player)

                            showBidDialog = false
                            onTransferCompleted()
                        } else {
                            bidFeedbackMessage = "Insufficient transfer budget!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RfmGold, contentColor = Color.Black),
                    modifier = Modifier.testTag("btn_confirm_bid")
                ) {
                    Text("Confirm Signing", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBidDialog = false }) {
                    Text("Cancel", color = RfmTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun MarketPlayerCard(
    player: Player,
    onSignClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("market_player_${player.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = RfmNavyCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(RfmNavyBorder, RfmNavyBorder)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(RfmNavySurface, CircleShape)
                        .border(1.dp, RfmGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.position.label,
                        color = RfmGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = player.name,
                        color = RfmTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${player.nationality} • Age: ${player.age} • OVR: ${player.overall}",
                        color = RfmTextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Val: €${player.valueEuro / 1_000_000}M • Wage: €${player.wageWeeklyEuro / 1000}k/wk",
                        color = RfmNeonGreen,
                        fontSize = 10.sp
                    )
                }
            }

            Button(
                onClick = onSignClick,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.testTag("btn_sign_${player.id}")
            ) {
                Text("Sign", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

package com.example.rfm.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.SportsSoccer
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
import com.example.rfm.model.Team
import com.example.ui.theme.*

@Composable
fun TrainingTab(
    gameState: GameState,
    onTrainingCompleted: () -> Unit
) {
    val userTeam = gameState.getUserTeam() ?: return
    var trainingStatusMessage by remember { mutableStateOf<String?>(null) }

    val trainingDrills = listOf(
        "Tactical Mastery" to "Improves squad passing and positioning cohesion (+Form)",
        "Attacking & Finishing" to "Sharpens striker shot accuracy and dribbling (+Shooting)",
        "Defensive Organization" to "Boosts backline tackling and aerial dominance (+Defending)",
        "Physical & Conditioning" to "Recharges player stamina and recovery (+Condition)"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
            .testTag("training_tab_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Active Focus Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = RfmNavyCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(RfmGold, RfmNeonGreen)))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("CURRENT REGIME", color = RfmTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(gameState.currentTrainingFocus, color = RfmGold, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Text(
                    "Training Facility Level: ${userTeam.trainingFacilityLevel} / 5",
                    color = RfmTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Drills Selection
        Text("SELECT TEAM DRILL", color = RfmGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

        trainingDrills.forEach { (drill, desc) ->
            val isCurrent = gameState.currentTrainingFocus == drill
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        RfmAudioEngine.playClickSound()
                        gameState.currentTrainingFocus = drill
                    }
                    .testTag("drill_$drill"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) RfmPitchGreen.copy(alpha = 0.5f) else RfmNavyCard
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = if (isCurrent) Brush.horizontalGradient(listOf(RfmNeonGreen, RfmPitchLine)) else Brush.horizontalGradient(listOf(RfmNavyBorder, RfmNavyBorder))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(drill, color = RfmTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(desc, color = RfmTextSecondary, fontSize = 11.sp)
                    }
                    if (isCurrent) {
                        Surface(
                            color = RfmNeonGreen,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "ACTIVE",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Run Drill Button
        Button(
            onClick = {
                RfmAudioEngine.playWhistleShort()
                // Boost squad attributes
                userTeam.players.forEach { p ->
                    when (gameState.currentTrainingFocus) {
                        "Attacking & Finishing" -> p.shooting = (p.shooting + 1).coerceAtMost(99)
                        "Defensive Organization" -> p.defending = (p.defending + 1).coerceAtMost(99)
                        "Physical & Conditioning" -> p.condition = (p.condition + 10).coerceAtMost(100)
                        else -> {
                            p.passing = (p.passing + 1).coerceAtMost(99)
                            p.form = (p.form + 1).coerceAtMost(10)
                        }
                    }
                    if (gameState.modSettings.noPlayerFatigue) {
                        p.condition = 100
                    }
                }
                trainingStatusMessage = "Training completed! Squad attributes and form sharpened."
                onTrainingCompleted()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_run_drill"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RfmGold, contentColor = Color.Black)
        ) {
            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("RUN INTENSIVE DRILL SESSION", fontWeight = FontWeight.Black, fontSize = 13.sp)
        }

        if (trainingStatusMessage != null) {
            Text(
                text = trainingStatusMessage!!,
                color = RfmNeonGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

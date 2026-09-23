package com.example.rfm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.rfm.ui.theme.*

@Composable
fun TrainingScreen(
    gameState: GameState,
    onRunTraining: (focus: String) -> Unit,
    onBack: () -> Unit
) {
    val regimens = listOf(
        "Balanced Training" to "Improves overall sharpness and general form.",
        "Attacking & Finishing" to "Enhances shooting attributes for forwards and wingers.",
        "Defending & Tactics" to "Strengthens defensive positioning and tackling for defenders.",
        "Passing & Vision" to "Sharpens short & long range passing distribution.",
        "Physical Fitness" to "Boosts stamina and resilience against fatigue.",
        "Rest & Recovery" to "Rejuvenates tired players, restoring condition and team morale."
    )

    var selectedFocus by remember { mutableStateOf(gameState.currentTrainingFocus) }
    var trainingFeedback by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(12.dp)
            .testTag("training_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TRAINING GROUND",
                    color = RfmGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Weekly Regimen & Player Development",
                    color = RfmTextSecondary,
                    fontSize = 11.sp
                )
            }
            TextButton(onClick = onBack) {
                Text("Back", color = RfmTextSecondary)
            }
        }

        if (gameState.modSettings.instantTraining || gameState.modSettings.maxPlayerDevelopment) {
            Text(
                text = "⚡ MOD ACTIVE: INSTANT TRAINING & 2X DEVELOPMENT BOOST",
                color = RfmAmber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (trainingFeedback != null) {
            Text(
                text = trainingFeedback!!,
                color = RfmNeonGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Regimens List
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            regimens.forEach { (title, desc) ->
                val isSelected = selectedFocus == title
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) RfmNavyCard else RfmNavySurface)
                        .border(1.dp, if (isSelected) RfmGold else RfmNavyBorder, RoundedCornerShape(8.dp))
                        .clickable { selectedFocus = title }
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = title,
                            color = if (isSelected) RfmGold else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = desc,
                            color = RfmTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Run Training Button
        Button(
            onClick = {
                onRunTraining(selectedFocus)
                trainingFeedback = "Training completed for focus: $selectedFocus. Squad attributes updated!"
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("run_training_button"),
            colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "EXECUTE TRAINING DRILL",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

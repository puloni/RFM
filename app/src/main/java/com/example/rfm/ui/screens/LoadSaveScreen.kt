package com.example.rfm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.rfm.ui.theme.*

@Composable
fun LoadSaveScreen(
    onSaveSlot: (slot: String) -> Boolean,
    onLoadSlot: (slot: String) -> Boolean,
    hasSaveCheck: (slot: String) -> Boolean,
    onBack: () -> Unit
) {
    val slots = listOf("autosave", "slot_1", "slot_2", "slot_3")
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RfmNavyDark)
            .padding(12.dp)
            .testTag("load_save_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SAVE & LOAD MANAGER",
                    color = RfmGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Multiple career save states & persistent backups",
                    color = RfmTextSecondary,
                    fontSize = 11.sp
                )
            }
            TextButton(onClick = onBack) {
                Text("Back", color = RfmTextSecondary)
            }
        }

        if (statusMessage != null) {
            Text(
                text = statusMessage!!,
                color = RfmNeonGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            slots.forEach { slot ->
                val hasSave = hasSaveCheck(slot)
                val label = when (slot) {
                    "autosave" -> "AUTO-SAVE SLOT"
                    "slot_1" -> "SAVE SLOT 1"
                    "slot_2" -> "SAVE SLOT 2"
                    else -> "SAVE SLOT 3"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(RfmNavyCard)
                        .border(1.dp, if (hasSave) RfmGold else RfmNavyBorder, RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (hasSave) "Status: Saved Career Data Available" else "Status: Empty Slot",
                                color = if (hasSave) RfmNeonGreen else RfmTextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Save Button
                            Button(
                                onClick = {
                                    val ok = onSaveSlot(slot)
                                    statusMessage = if (ok) "Game successfully saved to $label!" else "Save failed."
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RfmPitchGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Save", fontSize = 11.sp)
                            }

                            // Load Button
                            OutlinedButton(
                                onClick = {
                                    val ok = onLoadSlot(slot)
                                    statusMessage = if (ok) "Game loaded from $label!" else "Could not load save."
                                },
                                enabled = hasSave,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RfmGold),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Load", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

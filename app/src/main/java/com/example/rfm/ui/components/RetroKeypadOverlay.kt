package com.example.rfm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rfm.ui.theme.*

/**
 * Virtual Samsung GT-S8000 (Jet) retro hardware keypad overlay.
 * Recreates the J2ME phone experience with Soft Keys, D-Pad, OK, and numeric dialer shortcuts.
 */
@Composable
fun RetroKeypadOverlay(
    onKeyPress: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RfmNavyDark)
            .border(width = 1.dp, color = RfmNavyBorder)
            .padding(8.dp)
            .testTag("retro_keypad_overlay"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Phone Model Badge
        Text(
            text = "SAMSUNG GT-S8000 • JET CONTROLS",
            color = RfmTextMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Soft Keys Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(RfmNavyCard)
                    .border(1.dp, RfmNavyBorder, RoundedCornerShape(6.dp))
                    .clickable { onKeyPress("SOFT_LEFT") }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("key_soft_left")
            ) {
                Text(text = "[ BACK ]", color = RfmGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(RfmNavyCard)
                    .border(1.dp, RfmNavyBorder, RoundedCornerShape(6.dp))
                    .clickable { onKeyPress("SOFT_RIGHT") }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("key_soft_right")
            ) {
                Text(text = "[ MODS ]", color = RfmAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // D-Pad + OK Center
        Box(
            modifier = Modifier
                .size(130.dp)
                .background(RfmNavySurface, CircleShape)
                .border(2.dp, RfmNavyBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // UP
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
                    .size(36.dp)
                    .clickable { onKeyPress("UP") }
                    .testTag("key_dpad_up"),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = RfmGold)
            }

            // DOWN
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(36.dp)
                    .clickable { onKeyPress("DOWN") }
                    .testTag("key_dpad_down"),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = RfmGold)
            }

            // LEFT
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .size(36.dp)
                    .clickable { onKeyPress("LEFT") }
                    .testTag("key_dpad_left"),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = RfmGold)
            }

            // RIGHT
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
                    .size(36.dp)
                    .clickable { onKeyPress("RIGHT") }
                    .testTag("key_dpad_right"),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = RfmGold)
            }

            // OK / SELECT
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(RfmGold, CircleShape)
                    .clickable { onKeyPress("OK") }
                    .testTag("key_dpad_ok"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OK",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }
    }
}

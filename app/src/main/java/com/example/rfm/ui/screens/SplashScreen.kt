package com.example.rfm.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rfm.ui.theme.*

@Composable
fun SplashScreen(
    onContinue: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(RfmNavyDark, RfmPitchDark, Color(0xFF031207))
                )
            )
            .clickable { onContinue() }
            .testTag("splash_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        // Background Stadium Floodlight canvas effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            // Draw pitch stripes
            val stripeHeight = height / 14
            for (i in 0 until 14) {
                if (i % 2 == 0) {
                    drawRect(
                        color = Color(0x1000E676),
                        topLeft = Offset(0f, i * stripeHeight),
                        size = androidx.compose.ui.geometry.Size(width, stripeHeight)
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            // Gameloft style badge
            Card(
                colors = CardDefaults.cardColors(containerColor = RfmNavyCard),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "GAMELOFT J2ME CLASSIC PORT",
                    color = RfmGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Game Logo Display
            Text(
                text = "REAL FOOTBALL",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Text(
                text = "MANAGER 2013",
                color = RfmGold,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )

            Text(
                text = "Samsung GT-S8000 Edition • Android Reconstructed",
                color = RfmTextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Pulsing "Press Any Key / Tap to Start"
            Card(
                colors = CardDefaults.cardColors(containerColor = RfmPitchGreen),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.testTag("splash_start_button")
            ) {
                Text(
                    text = "TAP TO START",
                    color = Color.White.copy(alpha = alpha),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Features: Real 2012/13 Database • 2D Engine • Built-in Mod Menu",
                color = RfmTextMuted,
                fontSize = 10.sp
            )
        }
    }
}

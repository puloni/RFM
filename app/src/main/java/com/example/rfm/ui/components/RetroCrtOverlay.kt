package com.example.rfm.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

/**
 * Renders an authentic retro J2ME scanline / CRT pixel grid overlay
 * when the CRT Retro Filter is enabled.
 */
@Composable
fun RetroCrtOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val h = size.height
        val w = size.width
        val scanlineSpacing = 4f

        var y = 0f
        while (y < h) {
            drawLine(
                color = Color.Black.copy(alpha = 0.12f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.2f
            )
            y += scanlineSpacing
        }

        // Faint greenish tint vignette
        drawRect(
            color = Color(0x0A00FF66)
        )
    }
}

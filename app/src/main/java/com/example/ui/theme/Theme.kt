package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RfmColorScheme = darkColorScheme(
    primary = RfmGold,
    onPrimary = Color.Black,
    secondary = RfmNeonGreen,
    onSecondary = Color.Black,
    tertiary = RfmAmber,
    background = RfmNavyDark,
    onBackground = RfmTextPrimary,
    surface = RfmNavySurface,
    onSurface = RfmTextPrimary,
    surfaceVariant = RfmNavyCard,
    onSurfaceVariant = RfmTextSecondary,
    outline = RfmNavyBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = RfmColorScheme,
        typography = Typography,
        content = content
    )
}


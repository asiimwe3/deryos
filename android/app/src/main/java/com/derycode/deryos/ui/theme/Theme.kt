package com.derycode.deryos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** DERYCODE POWERED palette: deep navy, electric blue, white, silver. */
val DeryNavy = Color(0xFF050A18)
val DeryNavyRaised = Color(0xFF0A1229)
val DeryBlue = Color(0xFF2F6BFF)
val DeryBlueBright = Color(0xFF4F8CFF)
val DerySilver = Color(0xFFC7D2E5)
val DeryWhite = Color(0xFFF7FAFF)
val DeryDim = Color(0xFF8497B8)

private val DeryColors = darkColorScheme(
    primary = DeryBlue,
    onPrimary = DeryWhite,
    secondary = DeryBlueBright,
    background = DeryNavy,
    onBackground = DeryWhite,
    surface = DeryNavyRaised,
    onSurface = DerySilver
)

@Composable
fun DeryOSTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DeryColors, content = content)
}

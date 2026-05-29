package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonYellow,
    onPrimary = JetBlack,
    secondary = NeonTeal,
    onSecondary = JetBlack,
    tertiary = NeonOrange,
    background = DarkSlateBg,
    onBackground = Color.White,
    surface = DarkGreySurface,
    onSurface = Color.White,
    surfaceVariant = BorderGrey,
    onSurfaceVariant = AccentGrey,
    error = Color(0xFFFF5252)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // FamPay runs natively in deep dark neon skins for high teen engagement
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

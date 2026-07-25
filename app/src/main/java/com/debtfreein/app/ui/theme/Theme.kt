package com.debtfreein.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Premium Sleek Dark Mode Color Palette
val DeepDarkBlue = Color(0xFF0B0F17)
val CardSurface = Color(0xFF161E2E)
val EmeraldGreen = Color(0xFF10B981) // High interest payoff indication
val ElectricBlue = Color(0xFF3B82F6) // Interactive buttons / indicators
val AmberGold = Color(0xFFF59E0B) // Warnings / high APR warnings
val CrimsonRed = Color(0xFFEF4444) // Debt levels / errors
val TextPrimary = Color(0xFFF9FAFB)
val TextSecondary = Color(0xFF9CA3AF)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldGreen,
    secondary = ElectricBlue,
    tertiary = AmberGold,
    background = DeepDarkBlue,
    surface = CardSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = CrimsonRed
)

@Composable
fun DebtFreeInTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Force dark theme for maximum premium dashboard aesthetics as requested
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}

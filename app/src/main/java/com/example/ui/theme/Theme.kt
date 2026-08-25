package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryEmerald,
    onPrimary = Color.Black,
    primaryContainer = PrimaryEmerald.copy(alpha = 0.2f),
    onPrimaryContainer = PrimaryEmerald,
    secondary = PrimaryTeal,
    onSecondary = Color.Black,
    secondaryContainer = PrimaryTeal.copy(alpha = 0.2f),
    onSecondaryContainer = PrimaryTeal,
    tertiary = AccentIndigo,
    background = DarkBackground1,
    onBackground = TextPrimary,
    surface = SlateCardSurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateCardElevated,
    onSurfaceVariant = TextSecondary,
    outline = GlassCardBorder,
    error = ExpenseRed,
    onError = Color.White
)

@Composable
fun SpendWiseTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

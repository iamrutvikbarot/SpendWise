package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = Color.White,
    primaryContainer = SecondaryTeal,
    onPrimaryContainer = PrimaryTealDark,
    secondary = PrimaryTeal,
    onSecondary = Color.White,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = Surface, // For cards
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    error = ExpenseRed,
    onError = Color.White
)

@Composable
fun SpendWiseTheme(
    darkTheme: Boolean = false, // Force light theme based on design
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

package com.spendsplit.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppleMinimalDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF4F4F6),
    onPrimary = Color(0xFF121214),
    secondary = Color(0xFFA1A1AA),
    onSecondary = Color(0xFF121214),
    background = Color(0xFF121214),
    onBackground = Color(0xFFF4F4F6),
    surface = Color(0xFF1C1C1F),
    onSurface = Color(0xFFF4F4F6),
    surfaceVariant = Color(0xFF28282C),
    onSurfaceVariant = Color(0xFFA1A1AA),
    outline = Color(0xFF333338),
    outlineVariant = Color(0xFF242428)
)

private val AppleMinimalLightColorScheme = lightColorScheme(
    primary = Color(0xFF191817),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF6B655F),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFBF8F3), // Warm soft porcelain/beige
    onBackground = Color(0xFF191817),
    surface = Color(0xFFFFFFFF),     // Pure clean white card surface
    onSurface = Color(0xFF191817),   // Sharp high-contrast text on card
    surfaceVariant = Color(0xFFF3EFE7), // Muted warm linen for inputs & pills
    onSurfaceVariant = Color(0xFF6B655F), // Legible dark slate secondary text
    outline = Color(0xFFE8E2D6),     // Subtle hairline stone border
    outlineVariant = Color(0xFFEFE9DE)
)

@Composable
fun SpendSplitTheme(
    darkTheme: Boolean = false, // Defaults to clean white/beige Apple aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AppleMinimalDarkColorScheme else AppleMinimalLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

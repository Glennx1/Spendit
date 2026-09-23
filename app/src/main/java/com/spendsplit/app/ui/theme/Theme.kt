package com.spendsplit.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppleMinimalDarkColorScheme = darkColorScheme(
    primary = DarkTextPrimary,
    onPrimary = DarkBackground,
    secondary = DarkTextSecondary,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkMutedSurface,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder.copy(alpha = 0.5f)
)

private val AppleMinimalLightColorScheme = lightColorScheme(
    primary = AccentBlack,
    onPrimary = Color.White,
    secondary = CharcoalSecondary,
    onSecondary = Color.White,
    background = BeigeBackground,
    onBackground = ObsidianBlack,
    surface = CardSurface,
    onSurface = ObsidianBlack,
    surfaceVariant = MutedSurface,
    onSurfaceVariant = CharcoalSecondary,
    outline = CardBorder,
    outlineVariant = CardBorderSubtle
)

@Composable
fun SpendSplitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve crafted Apple Minimalist styling
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AppleMinimalDarkColorScheme else AppleMinimalLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

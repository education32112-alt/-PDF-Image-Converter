package com.educationwithhassan.pdfimageconverter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = CardWhite,
    primaryContainer = LightBg,
    secondary = CyanAccent,
    background = LightBg,
    surface = CardWhite,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
    error = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = RoyalBlue,
    onPrimary = CardWhite,
    secondary = CyanAccent,
    background = DarkBg,
    surface = DarkCard,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    error = ErrorRed
)

@Composable
fun PDFImageConverterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

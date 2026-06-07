package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AuctionDarkColorScheme = darkColorScheme(
    primary = RoyalBlue,
    onPrimary = TextPrimary,
    secondary = DarkNavy,
    onSecondary = TextPrimary,
    tertiary = Gold,
    onTertiary = DarkNavy,
    background = MainBg,
    onBackground = TextPrimary,
    surface = CardBg,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceBg,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force modern dark mode
    dynamicColor: Boolean = false, // Disable dynamic colors to keep royal blue dark design
    content: @Composable () -> Unit,
) {
    // We strictly use our beautifully tailored dark color scheme matching specification.
    MaterialTheme(
        colorScheme = AuctionDarkColorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.solitaire_compose.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun Solitaire_ComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// --- Card Sizing (larger than normal for elderly users) ---
val CardWidth   = 90.dp
val CardHeight = 90.dp    // ← reduced from 105.dp (shorter tableau cards)
val CardRadius  = 8.dp      // Rounded corners
val CardOverlap = 30.dp     // How much tableau cards overlap vertically
val TopRowCardWidth  = 55.dp
val TopRowCardHeight = 78.dp

// --- Font Sizes ---
val RankFontSize  = 22.sp
val SuitFontSize  = 18.sp

// --- Colors ---
val TableGreen    = Color(0xFF35654D)   // Classic felt green background
val CardWhite     = Color(0xFFFFFDF5)   // Slightly warm white for cards
val CardRed       = Color(0xFFCC1100)   // Clear, bold red for Hearts/Diamonds
val CardBlack     = Color(0xFF000000)   // Deep black for Clubs/Spades
val SelectedGold  = Color(0xFFFFD700)   // Highlight color when a card is tapped
val PileOutline   = Color(0x55FFFFFF)   // Faint white border for empty pile slots
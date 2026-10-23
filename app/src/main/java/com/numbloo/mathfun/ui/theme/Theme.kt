package com.numbloo.mathfun.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object NumColors {
    val Cream = Color(0xFFFFF6E5)
    val Ink = Color(0xFF3B2F63)
    val InkSoft = Color(0xFF7A7099)
    val Sun = Color(0xFFFFC93C)
    val Grass = Color(0xFF4CB944)
    val Sky = Color(0xFFBDE7FF)
    val Card = Color(0xFFFFFFFF)
    val Locked = Color(0xFFCFC8DD)
    val Good = Color(0xFF34B865)
    val Bad = Color(0xFFFF6B6B)
    val Lily = Color(0xFF7ED957)

    /** Pip's colours. The first six are free, the rest are won in the closet. */
    val PipPalette = listOf(
        Color(0xFF7ED957), // lime
        Color(0xFF4FC3F7), // sky
        Color(0xFFFF9F43), // tangerine
        Color(0xFFFF6FA8), // bubblegum
        Color(0xFFB388FF), // grape
        Color(0xFFFFD54F), // banana
        Color(0xFF5EE6C0), // mint (unlockable)
        Color(0xFFE53958), // ruby (unlockable)
        Color(0xFFF2B705), // gold (unlockable)
        Color(0xFF3D4A9E), // midnight (unlockable)
        Color(0xFFFF7F6E), // coral
        Color(0xFFC7A6F2), // lavender
        Color(0xFF26A69A), // teal
        Color(0xFF43A047), // forest
        Color(0xFFFFB38A), // peach
        Color(0xFF8D6E63), // cocoa
        Color(0xFFB0BEC5), // silver
        Color(0xFFB3F0FF), // diamond (Pro superstar)
        Color(0xFFFF6D00), // blaze (Addition master)
        Color(0xFF1E88E5), // ocean (Subtraction master)
        Color(0xFF5E35B1), // galaxy (Multiplication master)
        Color(0xFFD81B60), // cherry (Division master)
        Color(0xFFFF6FA8), // rainbow (drawn as a rainbow; this is its base)
    )

    /** The colours of the rainbow, for the Numbloo Legend prize. */
    val Rainbow = listOf(
        Color(0xFFFF5252), Color(0xFFFF9800), Color(0xFFFFEB3B), Color(0xFF66BB6A),
        Color(0xFF42A5F5), Color(0xFFAB47BC), Color(0xFFFF5252),
    )

    fun pip(index: Int): Color = PipPalette[((index % PipPalette.size) + PipPalette.size) % PipPalette.size]
}

fun Color.darken(f: Float = 0.25f): Color =
    Color(red * (1f - f), green * (1f - f), blue * (1f - f), alpha)

fun Color.lighten(f: Float = 0.3f): Color =
    Color(red + (1f - red) * f, green + (1f - green) * f, blue + (1f - blue) * f, alpha)

@Composable
fun NumblooTheme(content: @Composable () -> Unit) {
    val scheme = lightColorScheme(
        primary = NumColors.Ink,
        onPrimary = Color.White,
        secondary = NumColors.Sun,
        background = NumColors.Cream,
        onBackground = NumColors.Ink,
        surface = Color.White,
        onSurface = NumColors.Ink,
    )
    MaterialTheme(colorScheme = scheme, content = content)
}

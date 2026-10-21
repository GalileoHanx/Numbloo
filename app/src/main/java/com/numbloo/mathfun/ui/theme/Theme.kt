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

    /** Pip comes in six flavours, one per player. */
    val PipPalette = listOf(
        Color(0xFF7ED957), // lime
        Color(0xFF4FC3F7), // sky
        Color(0xFFFF9F43), // tangerine
        Color(0xFFFF6FA8), // bubblegum
        Color(0xFFB388FF), // grape
        Color(0xFFFFD54F), // banana
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

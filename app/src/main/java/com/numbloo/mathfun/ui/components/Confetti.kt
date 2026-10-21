package com.numbloo.mathfun.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

private class Piece(
    val x: Float,
    val speed: Float,
    val sway: Float,
    val phase: Float,
    val rot: Float,
    val spinDir: Float,
    val color: Color,
)

private val confettiColors = listOf(
    Color(0xFFFF6FA8), Color(0xFFFFC93C), Color(0xFF4FC3F7),
    Color(0xFF7ED957), Color(0xFFB388FF), Color(0xFFFF8A3D),
)

/** A burst of falling confetti each time [trigger] changes (0 = none). */
@Composable
fun ConfettiBurst(trigger: Int, modifier: Modifier = Modifier) {
    if (trigger <= 0) return
    val progress = remember(trigger) { Animatable(0f) }
    val pieces = remember(trigger) {
        val r = Random(trigger * 7919L)
        List(70) {
            Piece(
                x = r.nextFloat(),
                speed = 0.7f + r.nextFloat() * 0.6f,
                sway = 1f + r.nextFloat() * 2f,
                phase = r.nextFloat() * 6.28f,
                rot = r.nextFloat() * 360f,
                spinDir = if (r.nextBoolean()) 1f else -1f,
                color = confettiColors[r.nextInt(confettiColors.size)],
            )
        }
    }
    LaunchedEffect(trigger) {
        progress.animateTo(1f, tween(1900, easing = LinearEasing))
    }
    Canvas(modifier.fillMaxSize()) {
        val p = progress.value
        if (p < 1f) {
            val fade = if (p > 0.7f) 1f - (p - 0.7f) / 0.3f else 1f
            for (c in pieces) {
                val x = c.x * size.width + sin(p * c.sway * 6.28f + c.phase) * 40f
                val y = -40f + p * c.speed * (size.height + 120f)
                rotate(c.rot + p * 720f * c.spinDir, Offset(x, y)) {
                    drawRect(
                        color = c.color,
                        topLeft = Offset(x - 10f, y - 5f),
                        size = Size(20f, 10f),
                        alpha = fade.coerceIn(0f, 1f),
                    )
                }
            }
        }
    }
}

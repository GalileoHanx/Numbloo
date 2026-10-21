package com.numbloo.mathfun.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.numbloo.mathfun.model.Hat
import com.numbloo.mathfun.ui.theme.NumColors
import com.numbloo.mathfun.ui.theme.darken
import com.numbloo.mathfun.ui.theme.lighten
import kotlin.math.abs
import kotlin.math.cos

enum class PipMood { HAPPY, WOW, OOPS, CHEW, THINK }

/**
 * Pip: a hungry, goofy blob who eats numbers.
 * Drawn entirely in code, so it costs nothing to ship and scales to any size.
 */
@Composable
fun Pip(
    modifier: Modifier = Modifier,
    mood: PipMood = PipMood.HAPPY,
    color: Color = NumColors.PipPalette[0],
    hat: Hat = Hat.NONE,
) {
    val inf = rememberInfiniteTransition(label = "pip")
    val bounce by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(if (mood == PipMood.HAPPY) 380 else 750, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "bounce",
    )
    val blink by inf.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes<Float> {
                durationMillis = 3400
                1f at 0
                1f at 3100
                0.1f at 3220
                1f at 3400
            }
        ),
        label = "blink",
    )
    val fast by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(150, easing = LinearEasing), RepeatMode.Reverse),
        label = "fast",
    )
    val spin by inf.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing)),
        label = "spin",
    )

    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            drawPip(mood, color, hat, bounce, blink, fast, spin)
        }
        if (mood == PipMood.OOPS) {
            Text(
                "hic!",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .graphicsLayer { rotationZ = 14f; translationY = -fast * 6f },
                color = NumColors.Ink,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
            )
        }
    }
}

private fun DrawScope.drawPip(
    mood: PipMood,
    color: Color,
    hat: Hat,
    bounce: Float,
    blink: Float,
    fast: Float,
    spin: Float,
) {
    val w = size.width
    val h = size.height
    val shake = if (mood == PipMood.OOPS) (fast - 0.5f) * w * 0.04f else 0f
    val jump = if (mood == PipMood.HAPPY) bounce * h * 0.04f else 0f
    val squash = bounce * 0.05f
    val rx = w * 0.40f * (1f + squash)
    val ry = h * 0.32f * (1f - squash)
    val cx = w / 2f + shake
    val cy = h * 0.93f - ry - jump
    val outline = color.darken(0.4f)
    val stroke = w * 0.022f

    // Shadow
    drawOval(
        Color.Black.copy(alpha = 0.10f),
        topLeft = Offset(w / 2f - rx * 0.8f, h * 0.92f),
        size = Size(rx * 1.6f, h * 0.05f),
    )
    // Feet
    drawOval(color.darken(0.2f), Offset(cx - rx * 0.6f, cy + ry * 0.72f), Size(rx * 0.42f, ry * 0.36f))
    drawOval(color.darken(0.2f), Offset(cx + rx * 0.18f, cy + ry * 0.72f), Size(rx * 0.42f, ry * 0.36f))

    // Body
    val bodyTopLeft = Offset(cx - rx, cy - ry)
    val bodySize = Size(rx * 2f, ry * 2f)
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(color.lighten(0.45f), color),
            center = Offset(cx - rx * 0.3f, cy - ry * 0.4f),
            radius = rx * 1.3f,
        ),
        topLeft = bodyTopLeft,
        size = bodySize,
    )
    drawOval(outline, bodyTopLeft, bodySize, style = Stroke(width = stroke))
    // Belly shine
    drawOval(
        Color.White.copy(alpha = 0.22f),
        Offset(cx - rx * 0.42f, cy + ry * 0.12f),
        Size(rx * 0.84f, ry * 0.6f),
    )

    // Eyes
    val eyeY = cy - ry * 0.18f
    val er = rx * 0.21f
    for (s in listOf(-1f, 1f)) {
        val ex = cx + s * rx * 0.36f
        if (mood == PipMood.OOPS) {
            val tipX = ex - s * er * 0.4f
            val armX = ex + s * er * 0.5f
            val p = Path().apply {
                moveTo(armX, eyeY - er * 0.55f)
                lineTo(tipX, eyeY)
                lineTo(armX, eyeY + er * 0.55f)
            }
            drawPath(p, NumColors.Ink, style = Stroke(width = stroke * 1.3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        } else {
            val eh = er * 2f * blink
            drawOval(Color.White, Offset(ex - er, eyeY - eh / 2f), Size(er * 2f, eh))
            drawOval(outline, Offset(ex - er, eyeY - eh / 2f), Size(er * 2f, eh), style = Stroke(width = stroke * 0.6f))
            if (blink > 0.4f) {
                val pr = er * if (mood == PipMood.WOW) 0.38f else 0.52f
                val look = when (mood) {
                    PipMood.THINK -> Offset(er * 0.35f, -er * 0.35f)
                    else -> Offset(0f, er * 0.12f)
                }
                val pc = Offset(ex, eyeY) + look
                drawCircle(NumColors.Ink, radius = pr, center = pc)
                drawCircle(Color.White, radius = pr * 0.35f, center = pc + Offset(-pr * 0.3f, -pr * 0.35f))
            }
        }
    }

    // Cheeks
    for (s in listOf(-1f, 1f)) {
        drawCircle(
            Color(0xFFFF8FA3).copy(alpha = 0.55f),
            radius = rx * 0.12f,
            center = Offset(cx + s * rx * 0.64f, cy + ry * 0.14f),
        )
    }

    // Mouth
    val my = cy + ry * 0.3f
    val mouthColor = Color(0xFF7A1F3D)
    when (mood) {
        PipMood.HAPPY -> {
            drawArc(
                mouthColor, startAngle = 0f, sweepAngle = 180f, useCenter = true,
                topLeft = Offset(cx - rx * 0.28f, my - ry * 0.22f),
                size = Size(rx * 0.56f, ry * 0.48f),
            )
            drawOval(Color(0xFFFF7B9C), Offset(cx - rx * 0.12f, my + ry * 0.06f), Size(rx * 0.24f, ry * 0.12f))
        }
        PipMood.WOW -> drawOval(mouthColor, Offset(cx - rx * 0.1f, my - ry * 0.1f), Size(rx * 0.2f, ry * 0.28f))
        PipMood.CHEW -> {
            val open = ry * (0.06f + fast * 0.26f)
            drawOval(mouthColor, Offset(cx - rx * 0.2f, my - open / 2f), Size(rx * 0.4f, open))
        }
        PipMood.THINK -> drawLine(
            outline, Offset(cx - rx * 0.12f, my), Offset(cx + rx * 0.14f, my - ry * 0.05f),
            strokeWidth = stroke, cap = StrokeCap.Round,
        )
        PipMood.OOPS -> {
            val p = Path().apply {
                moveTo(cx - rx * 0.24f, my)
                lineTo(cx - rx * 0.12f, my - ry * 0.07f)
                lineTo(cx, my)
                lineTo(cx + rx * 0.12f, my - ry * 0.07f)
                lineTo(cx + rx * 0.24f, my)
            }
            drawPath(p, outline, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    drawHat(hat, cx, cy - ry, rx, ry, h, spin)
}

private fun DrawScope.drawHat(hat: Hat, cx: Float, top: Float, rx: Float, ry: Float, h: Float, spin: Float) {
    val ink = NumColors.Ink
    when (hat) {
        Hat.NONE -> {
            // A little sprout
            drawLine(Color(0xFF3E8E2F), Offset(cx, top + ry * 0.04f), Offset(cx, top - h * 0.07f), strokeWidth = rx * 0.06f)
            rotate(-30f, Offset(cx, top - h * 0.07f)) {
                drawOval(Color(0xFF5DBB4A), Offset(cx - rx * 0.32f, top - h * 0.1f), Size(rx * 0.32f, rx * 0.16f))
            }
            rotate(30f, Offset(cx, top - h * 0.07f)) {
                drawOval(Color(0xFF5DBB4A), Offset(cx, top - h * 0.1f), Size(rx * 0.32f, rx * 0.16f))
            }
        }
        Hat.PARTY -> {
            val base = top + ry * 0.12f
            val apex = Offset(cx + rx * 0.08f, top - h * 0.24f)
            val p = Path().apply {
                moveTo(cx - rx * 0.3f, base)
                lineTo(apex.x, apex.y)
                lineTo(cx + rx * 0.3f, base)
                close()
            }
            drawPath(p, Color(0xFFFF6FA8))
            drawPath(p, ink, style = Stroke(width = rx * 0.03f, join = StrokeJoin.Round))
            for (k in 1..3) {
                val t = k / 4f
                drawCircle(Color(0xFFFFE066), radius = rx * 0.045f, center = Offset(cx + (apex.x - cx) * t, base + (apex.y - base) * t))
            }
            drawCircle(Color(0xFFFFC93C), radius = rx * 0.09f, center = apex)
        }
        Hat.CROWN -> {
            val base = top + ry * 0.15f
            val hh = h * 0.16f
            val l = cx - rx * 0.42f
            val r = cx + rx * 0.42f
            val p = Path().apply {
                moveTo(l, base)
                lineTo(l, base - hh)
                lineTo(l + (r - l) * 0.25f, base - hh * 0.55f)
                lineTo(cx, base - hh * 1.15f)
                lineTo(l + (r - l) * 0.75f, base - hh * 0.55f)
                lineTo(r, base - hh)
                lineTo(r, base)
                close()
            }
            drawPath(p, Color(0xFFFFC93C))
            drawPath(p, Color(0xFFB8860B), style = Stroke(width = rx * 0.03f, join = StrokeJoin.Round))
            drawCircle(Color(0xFFFF4D6D), radius = rx * 0.06f, center = Offset(cx, base - hh * 0.35f))
            drawCircle(Color(0xFF4FC3F7), radius = rx * 0.05f, center = Offset(l + (r - l) * 0.2f, base - hh * 0.25f))
            drawCircle(Color(0xFF4FC3F7), radius = rx * 0.05f, center = Offset(l + (r - l) * 0.8f, base - hh * 0.25f))
        }
        Hat.WIZARD -> {
            val base = top + ry * 0.1f
            val apex = Offset(cx + rx * 0.35f, top - h * 0.3f)
            val p = Path().apply {
                moveTo(cx - rx * 0.32f, base)
                lineTo(apex.x, apex.y)
                lineTo(cx + rx * 0.32f, base)
                close()
            }
            drawPath(p, Color(0xFF6A4BC4))
            drawOval(Color(0xFF4B3391), Offset(cx - rx * 0.55f, base - ry * 0.08f), Size(rx * 1.1f, ry * 0.2f))
            drawCircle(Color(0xFFFFE066), radius = rx * 0.07f, center = Offset(cx + rx * 0.05f, base - h * 0.1f))
            drawCircle(Color(0xFFFFE066), radius = rx * 0.04f, center = Offset(cx + rx * 0.2f, base - h * 0.18f))
        }
        Hat.PROPELLER -> {
            val base = top + ry * 0.18f
            val capR = rx * 0.45f
            drawArc(
                Color(0xFFFF4D6D), startAngle = 180f, sweepAngle = 90f, useCenter = true,
                topLeft = Offset(cx - capR, base - capR), size = Size(capR * 2f, capR * 2f),
            )
            drawArc(
                Color(0xFF4FC3F7), startAngle = 270f, sweepAngle = 90f, useCenter = true,
                topLeft = Offset(cx - capR, base - capR), size = Size(capR * 2f, capR * 2f),
            )
            val stickTop = base - capR - h * 0.06f
            drawLine(ink, Offset(cx, base - capR), Offset(cx, stickTop), strokeWidth = rx * 0.04f)
            val blade = abs(cos(Math.toRadians(spin.toDouble())).toFloat()) * rx * 0.5f + rx * 0.05f
            drawOval(Color(0xFFFFC93C), Offset(cx - blade, stickTop - rx * 0.05f), Size(blade * 2f, rx * 0.1f))
            drawCircle(ink, radius = rx * 0.04f, center = Offset(cx, stickTop))
        }
    }
}

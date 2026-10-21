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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.numbloo.mathfun.model.Eyes
import com.numbloo.mathfun.model.Flag
import com.numbloo.mathfun.model.Glasses
import com.numbloo.mathfun.model.Hair
import com.numbloo.mathfun.model.Hat
import com.numbloo.mathfun.model.Job
import com.numbloo.mathfun.model.Mouth
import com.numbloo.mathfun.model.Neck
import com.numbloo.mathfun.model.Profile
import com.numbloo.mathfun.ui.theme.NumColors
import com.numbloo.mathfun.ui.theme.darken
import com.numbloo.mathfun.ui.theme.lighten
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

enum class PipMood { HAPPY, WOW, OOPS, CHEW, THINK }

/** Everything Pip is wearing. */
data class PipLook(
    val color: Color = NumColors.PipPalette[0],
    val hat: Hat = Hat.NONE,
    val glasses: Glasses = Glasses.NONE,
    val neck: Neck = Neck.NONE,
    val flag: Flag = Flag.NONE,
    val hair: Hair = Hair.NONE,
    val job: Job = Job.NONE,
    val eyes: Eyes = Eyes.NORMAL,
    val mouth: Mouth = Mouth.NORMAL,
)

fun Profile.pipLook(): PipLook =
    PipLook(NumColors.pip(colorIndex), hat, glasses, neck, flag, hair, job, eyes, mouth)

/**
 * Pip: a hungry, goofy blob who eats numbers.
 * Drawn entirely in code, so it costs nothing to ship and scales to any size.
 * [animated] = false draws a still Pip (used for the many small previews in the closet).
 */
@Composable
fun Pip(
    modifier: Modifier = Modifier,
    mood: PipMood = PipMood.HAPPY,
    color: Color = NumColors.PipPalette[0],
    hat: Hat = Hat.NONE,
    glasses: Glasses = Glasses.NONE,
    neck: Neck = Neck.NONE,
    flag: Flag = Flag.NONE,
    hair: Hair = Hair.NONE,
    job: Job = Job.NONE,
    eyes: Eyes = Eyes.NORMAL,
    mouth: Mouth = Mouth.NORMAL,
    animated: Boolean = true,
) {
    val look = PipLook(color, hat, glasses, neck, flag, hair, job, eyes, mouth)
    val inf = rememberInfiniteTransition(label = "pip")
    val bounce = if (!animated) 0f else inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(if (mood == PipMood.HAPPY) 380 else 750, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "bounce",
    ).value
    val blink = if (!animated) 1f else inf.animateFloat(
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
    ).value
    val fast = if (!animated) 0.5f else inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(150, easing = LinearEasing), RepeatMode.Reverse),
        label = "fast",
    ).value
    val spin = if (!animated) 30f else inf.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing)),
        label = "spin",
    ).value

    BoxWithConstraints(modifier) {
        val boxW = maxWidth
        val boxH = maxHeight
        Canvas(Modifier.fillMaxSize()) {
            drawPip(mood, look, bounce, blink, fast, spin)
        }
        if (flag != Flag.NONE) {
            Text(
                flag.emoji,
                fontSize = (boxW.value * 0.24f).sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = -(boxW * 0.07f), y = boxH * 0.01f)
                    .graphicsLayer { rotationZ = if (animated) (fast - 0.5f) * 6f else 0f },
            )
        }
        if (mood == PipMood.OOPS) {
            Text(
                "hic!",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .graphicsLayer { rotationZ = -14f; translationY = -fast * 6f },
                color = NumColors.Ink,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
            )
        }
    }
}

/** Pip dressed the way this player chose. */
@Composable
fun ProfilePip(p: Profile, modifier: Modifier = Modifier, mood: PipMood = PipMood.HAPPY) {
    LookPip(p.pipLook(), modifier, mood)
}

@Composable
fun LookPip(look: PipLook, modifier: Modifier = Modifier, mood: PipMood = PipMood.HAPPY, animated: Boolean = true) {
    Pip(
        modifier, mood, look.color, look.hat, look.glasses, look.neck,
        look.flag, look.hair, look.job, look.eyes, look.mouth, animated,
    )
}

private fun DrawScope.drawPip(
    mood: PipMood,
    look: PipLook,
    bounce: Float,
    blink: Float,
    fast: Float,
    spin: Float,
) {
    val color = look.color
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
    // Flag pole (the flag itself is an emoji placed on top)
    if (look.flag != Flag.NONE) {
        drawLine(
            Color(0xFF8D6E63), Offset(cx + rx * 0.82f, cy + ry * 0.15f), Offset(w * 0.93f, h * 0.05f),
            strokeWidth = w * 0.025f, cap = StrokeCap.Round,
        )
    }
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
    // Hand holding the flag
    if (look.flag != Flag.NONE) {
        drawCircle(color.darken(0.1f), radius = rx * 0.13f, center = Offset(cx + rx * 0.86f, cy + ry * 0.12f))
    }

    drawHair(look.hair, cx, cy, rx, ry)

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
            drawEye(look.eyes, s, ex, eyeY, er, blink, mood, outline, color, stroke)
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
        PipMood.HAPPY -> drawHappyMouth(look.mouth, cx, my, rx, ry, mouthColor, outline, stroke)
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
    if (look.mouth == Mouth.MUSTACHE) {
        val brown = Color(0xFF4E342E)
        for (s in listOf(-1f, 1f)) {
            rotate(s * -12f, Offset(cx, my - ry * 0.14f)) {
                drawOval(brown, Offset(if (s < 0) cx - rx * 0.3f else cx, my - ry * 0.2f), Size(rx * 0.3f, ry * 0.13f))
            }
        }
    }

    drawNeck(look.neck, cx, cy, rx, ry)
    if (look.job == Job.DOCTOR) drawStethoscope(cx, cy, rx, ry)
    drawGlasses(look.glasses, cx, eyeY, rx * 0.36f, er)
    if (look.job != Job.NONE) {
        drawJob(look.job, cx, cy, rx, ry, h)
    } else if (!(look.hat == Hat.NONE && look.hair != Hair.NONE)) {
        drawHat(look.hat, cx, cy - ry, rx, ry, h, spin)
    }
}

/** A point on Pip's body outline at [deg] degrees (270 = the very top). */
private fun edge(cx: Float, cy: Float, rx: Float, ry: Float, deg: Float, scale: Float = 1f): Offset {
    val a = Math.toRadians(deg.toDouble())
    return Offset(cx + rx * scale * cos(a).toFloat(), cy + ry * scale * sin(a).toFloat())
}

private fun DrawScope.drawHair(hair: Hair, cx: Float, cy: Float, rx: Float, ry: Float) {
    when (hair) {
        Hair.NONE -> {}
        Hair.SPIKY, Hair.MOHAWK -> {
            val mohawk = hair == Hair.MOHAWK
            val from = if (mohawk) 250f else 205f
            val to = if (mohawk) 290f else 335f
            val spikes = if (mohawk) 4 else 6
            val reach = if (mohawk) 1.75f else 1.4f
            val p = Path()
            val step = (to - from) / spikes
            val start = edge(cx, cy, rx, ry, from, 0.92f)
            p.moveTo(start.x, start.y)
            for (k in 0 until spikes) {
                val tip = edge(cx, cy, rx, ry, from + step * (k + 0.5f), reach)
                val base = edge(cx, cy, rx, ry, from + step * (k + 1), 0.92f)
                p.lineTo(tip.x, tip.y)
                p.lineTo(base.x, base.y)
            }
            p.close()
            val c = if (mohawk) Color(0xFFE91E63) else Color(0xFF5D4037)
            drawPath(p, c)
            drawPath(p, c.darken(0.3f), style = Stroke(width = rx * 0.025f, join = StrokeJoin.Round))
        }
        Hair.CURLY -> {
            val c = Color(0xFF3E2723)
            var deg = 198f
            while (deg <= 342f) {
                drawCircle(c, radius = rx * 0.19f, center = edge(cx, cy, rx, ry, deg, 0.98f))
                deg += 18f
            }
            for (d in listOf(235f, 270f, 305f)) {
                drawCircle(c, radius = rx * 0.17f, center = edge(cx, cy, rx, ry, d, 1.12f))
            }
            for (d in listOf(250f, 290f)) {
                drawCircle(c.lighten(0.12f), radius = rx * 0.07f, center = edge(cx, cy, rx, ry, d, 1.1f))
            }
        }
        Hair.PIGTAILS -> {
            val c = Color(0xFFD84315)
            // fringe
            val f = Path()
            val a = edge(cx, cy, rx, ry, 215f)
            val b = edge(cx, cy, rx, ry, 325f)
            f.moveTo(a.x, a.y)
            f.cubicTo(cx - rx * 0.4f, cy - ry * 1.25f, cx + rx * 0.4f, cy - ry * 1.25f, b.x, b.y)
            f.cubicTo(cx + rx * 0.3f, cy - ry * 0.75f, cx - rx * 0.3f, cy - ry * 0.75f, a.x, a.y)
            drawPath(f, c)
            for (s in listOf(-1f, 1f)) {
                val puff = Offset(cx + s * rx * 1.02f, cy - ry * 0.62f)
                drawCircle(c, radius = rx * 0.24f, center = puff)
                drawCircle(c.darken(0.25f), radius = rx * 0.24f, center = puff, style = Stroke(width = rx * 0.025f))
                val bow = Offset(cx + s * rx * 0.82f, cy - ry * 0.78f)
                drawCircle(Color(0xFFFF6FA8), radius = rx * 0.08f, center = bow)
                drawCircle(Color(0xFFFF6FA8), radius = rx * 0.06f, center = bow + Offset(-rx * 0.09f, -rx * 0.05f))
                drawCircle(Color(0xFFFF6FA8), radius = rx * 0.06f, center = bow + Offset(rx * 0.09f, -rx * 0.05f))
            }
        }
        Hair.SWOOP -> {
            val c = Color(0xFF6D4C41)
            val p = Path()
            val a = edge(cx, cy, rx, ry, 195f)
            val b = edge(cx, cy, rx, ry, 345f)
            p.moveTo(a.x, a.y)
            p.cubicTo(cx - rx * 0.9f, cy - ry * 1.35f, cx + rx * 0.6f, cy - ry * 1.5f, b.x, b.y)
            p.cubicTo(cx + rx * 0.5f, cy - ry * 0.8f, cx - rx * 0.1f, cy - ry * 0.85f, cx - rx * 0.55f, cy - ry * 0.45f)
            p.close()
            drawPath(p, c)
            drawPath(p, c.darken(0.3f), style = Stroke(width = rx * 0.025f, join = StrokeJoin.Round))
            drawLine(Color.White.copy(alpha = 0.3f), Offset(cx - rx * 0.4f, cy - ry * 0.95f), Offset(cx + rx * 0.2f, cy - ry * 1.08f), strokeWidth = rx * 0.05f, cap = StrokeCap.Round)
        }
    }
}

private fun heartPath(c: Offset, r: Float): Path = Path().apply {
    moveTo(c.x, c.y + r * 0.9f)
    cubicTo(c.x - r * 1.4f, c.y - r * 0.1f, c.x - r * 0.7f, c.y - r * 1.2f, c.x, c.y - r * 0.4f)
    cubicTo(c.x + r * 0.7f, c.y - r * 1.2f, c.x + r * 1.4f, c.y - r * 0.1f, c.x, c.y + r * 0.9f)
    close()
}

private fun DrawScope.drawEye(
    style: Eyes,
    side: Float,
    ex: Float,
    eyeY: Float,
    er: Float,
    blink: Float,
    mood: PipMood,
    outline: Color,
    body: Color,
    stroke: Float,
) {
    if (style == Eyes.HEARTS) {
        val p = heartPath(Offset(ex, eyeY), er * 1.05f * blink.coerceAtLeast(0.3f))
        drawPath(p, Color(0xFFE53958))
        drawCircle(Color.White.copy(alpha = 0.7f), radius = er * 0.18f, center = Offset(ex - er * 0.35f, eyeY - er * 0.3f))
        return
    }
    if (style == Eyes.WINK && side > 0f) {
        drawArc(
            NumColors.Ink, startAngle = 200f, sweepAngle = 140f, useCenter = false,
            topLeft = Offset(ex - er * 0.8f, eyeY - er * 0.3f), size = Size(er * 1.6f, er * 1.2f),
            style = Stroke(width = stroke * 1.2f, cap = StrokeCap.Round),
        )
        return
    }
    val eh = er * 2f * blink
    drawOval(Color.White, Offset(ex - er, eyeY - eh / 2f), Size(er * 2f, eh))
    drawOval(outline, Offset(ex - er, eyeY - eh / 2f), Size(er * 2f, eh), style = Stroke(width = stroke * 0.6f))
    if (blink <= 0.4f) return
    val look = when {
        mood == PipMood.THINK -> Offset(er * 0.35f, -er * 0.35f)
        style == Eyes.SLEEPY -> Offset(0f, er * 0.35f)
        else -> Offset(0f, er * 0.12f)
    }
    val pc = Offset(ex, eyeY) + look
    when (style) {
        Eyes.STARRY -> {
            val star = starPath(pc, er * 0.62f, er * 0.28f)
            drawPath(star, Color(0xFFFFC93C))
            drawPath(star, Color(0xFFB8860B), style = Stroke(width = er * 0.08f, join = StrokeJoin.Round))
        }
        Eyes.SPARKLY -> {
            val pr = er * 0.64f
            drawCircle(NumColors.Ink, radius = pr, center = pc)
            drawCircle(Color.White, radius = pr * 0.36f, center = pc + Offset(-pr * 0.3f, -pr * 0.35f))
            drawCircle(Color.White, radius = pr * 0.18f, center = pc + Offset(pr * 0.35f, pr * 0.3f))
        }
        else -> {
            val pr = er * if (mood == PipMood.WOW) 0.38f else 0.52f
            drawCircle(NumColors.Ink, radius = pr, center = pc)
            drawCircle(Color.White, radius = pr * 0.35f, center = pc + Offset(-pr * 0.3f, -pr * 0.35f))
        }
    }
    if (style == Eyes.SLEEPY) {
        // Droopy eyelid
        drawArc(
            body, startAngle = 180f, sweepAngle = 180f, useCenter = true,
            topLeft = Offset(ex - er * 1.05f, eyeY - eh / 2f - er * 0.05f), size = Size(er * 2.1f, eh * 1.05f),
        )
        drawLine(outline, Offset(ex - er, eyeY), Offset(ex + er, eyeY), strokeWidth = stroke * 0.8f, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawHappyMouth(
    style: Mouth,
    cx: Float,
    my: Float,
    rx: Float,
    ry: Float,
    mouthColor: Color,
    outline: Color,
    stroke: Float,
) {
    val left = cx - rx * 0.28f
    val top = my - ry * 0.22f
    val mw = rx * 0.56f
    val mh = ry * 0.48f
    if (style == Mouth.TONGUE) {
        drawArc(
            outline, startAngle = 10f, sweepAngle = 160f, useCenter = false,
            topLeft = Offset(left, top), size = Size(mw, mh),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        drawOval(Color(0xFFFF7B9C), Offset(cx - rx * 0.05f, my + ry * 0.12f), Size(rx * 0.22f, ry * 0.3f))
        drawLine(Color(0xFFE0557A), Offset(cx + rx * 0.06f, my + ry * 0.15f), Offset(cx + rx * 0.06f, my + ry * 0.33f), strokeWidth = stroke * 0.5f)
        return
    }
    drawArc(mouthColor, startAngle = 0f, sweepAngle = 180f, useCenter = true, topLeft = Offset(left, top), size = Size(mw, mh))
    drawOval(Color(0xFFFF7B9C), Offset(cx - rx * 0.12f, my + ry * 0.06f), Size(rx * 0.24f, ry * 0.12f))
    when (style) {
        Mouth.TOOTHY -> {
            drawRect(Color.White, Offset(cx - rx * 0.1f, my + ry * 0.01f), Size(rx * 0.09f, ry * 0.11f))
            drawRect(Color.White, Offset(cx + rx * 0.01f, my + ry * 0.01f), Size(rx * 0.09f, ry * 0.11f))
        }
        Mouth.FANGS -> {
            for (s in listOf(-1f, 1f)) {
                val x = cx + s * rx * 0.17f
                val p = Path().apply {
                    moveTo(x - rx * 0.05f, my + ry * 0.01f)
                    lineTo(x + rx * 0.05f, my + ry * 0.01f)
                    lineTo(x, my + ry * 0.12f)
                    close()
                }
                drawPath(p, Color.White)
            }
        }
        else -> {}
    }
}

private fun DrawScope.drawStethoscope(cx: Float, cy: Float, rx: Float, ry: Float) {
    val grey = Color(0xFF607D8B)
    drawArc(
        grey, startAngle = 25f, sweepAngle = 130f, useCenter = false,
        topLeft = Offset(cx - rx * 0.8f, cy - ry * 0.05f), size = Size(rx * 1.6f, ry * 0.95f),
        style = Stroke(width = rx * 0.05f, cap = StrokeCap.Round),
    )
    val end = Offset(cx + rx * 0.35f, cy + ry * 0.82f)
    drawCircle(Color(0xFFB0BEC5), radius = rx * 0.1f, center = end)
    drawCircle(grey, radius = rx * 0.1f, center = end, style = Stroke(width = rx * 0.03f))
}

private fun DrawScope.drawJob(job: Job, cx: Float, cy: Float, rx: Float, ry: Float, h: Float) {
    val top = cy - ry
    when (job) {
        Job.NONE -> {}
        Job.CHEF -> {
            val white = Color(0xFFFDFDFD)
            val edgeC = Color(0xFFB0BEC5)
            for (k in listOf(-1f, 0f, 1f)) {
                drawCircle(white, radius = rx * 0.24f, center = Offset(cx + k * rx * 0.26f, top - h * 0.1f))
                drawCircle(edgeC, radius = rx * 0.24f, center = Offset(cx + k * rx * 0.26f, top - h * 0.1f), style = Stroke(width = rx * 0.02f))
            }
            drawRect(white, Offset(cx - rx * 0.42f, top - h * 0.06f), Size(rx * 0.84f, ry * 0.32f))
            drawRect(edgeC, Offset(cx - rx * 0.42f, top - h * 0.06f), Size(rx * 0.84f, ry * 0.32f), style = Stroke(width = rx * 0.02f))
        }
        Job.DOCTOR -> {
            drawLine(Color(0xFF455A64), edge(cx, cy, rx, ry, 215f, 0.95f), edge(cx, cy, rx, ry, 325f, 0.95f), strokeWidth = rx * 0.06f)
            val mirror = Offset(cx, top + ry * 0.12f)
            drawCircle(Color(0xFFE0E0E0), radius = rx * 0.17f, center = mirror)
            drawCircle(Color(0xFF90A4AE), radius = rx * 0.17f, center = mirror, style = Stroke(width = rx * 0.035f))
            drawCircle(Color.White, radius = rx * 0.05f, center = mirror + Offset(-rx * 0.05f, -rx * 0.05f))
        }
        Job.FIREFIGHTER, Job.BUILDER, Job.PILOT -> {
            val main = when (job) {
                Job.FIREFIGHTER -> Color(0xFFD32F2F)
                Job.BUILDER -> Color(0xFFFFC107)
                else -> Color(0xFF283593)
            }
            val domeR = rx * 0.62f
            val base = top + ry * 0.2f
            drawArc(main, 180f, 180f, true, Offset(cx - domeR, base - domeR * 0.9f), Size(domeR * 2f, domeR * 1.8f))
            // brim
            val brimColor = if (job == Job.PILOT) Color(0xFF111111) else main.darken(0.15f)
            drawOval(brimColor, Offset(cx - rx * 0.8f, base - ry * 0.08f), Size(rx * 1.6f, ry * 0.18f))
            when (job) {
                Job.FIREFIGHTER -> {
                    drawCircle(Color(0xFFFFC93C), radius = rx * 0.13f, center = Offset(cx, base - domeR * 0.45f))
                    drawCircle(Color(0xFFB8860B), radius = rx * 0.13f, center = Offset(cx, base - domeR * 0.45f), style = Stroke(width = rx * 0.025f))
                }
                Job.BUILDER -> drawLine(main.darken(0.2f), Offset(cx, base - domeR * 0.9f), Offset(cx, base), strokeWidth = rx * 0.08f)
                else -> {
                    val gold = Color(0xFFFFC93C)
                    val c = Offset(cx, base - domeR * 0.45f)
                    drawOval(gold, Offset(c.x - rx * 0.32f, c.y - rx * 0.04f), Size(rx * 0.26f, rx * 0.08f))
                    drawOval(gold, Offset(c.x + rx * 0.06f, c.y - rx * 0.04f), Size(rx * 0.26f, rx * 0.08f))
                    drawCircle(gold, radius = rx * 0.07f, center = c)
                }
            }
        }
        Job.ASTRONAUT -> {
            val c = Offset(cx, cy - ry * 0.05f)
            val r = rx * 1.12f
            drawCircle(Color(0xFFBDE7FF).copy(alpha = 0.22f), radius = r, center = c)
            drawCircle(Color(0xFFCFD8DC), radius = r, center = c, style = Stroke(width = rx * 0.07f))
            drawArc(
                Color.White.copy(alpha = 0.7f), 200f, 50f, false,
                Offset(c.x - r * 0.8f, c.y - r * 0.8f), Size(r * 1.6f, r * 1.6f),
                style = Stroke(width = rx * 0.06f, cap = StrokeCap.Round),
            )
            drawLine(Color(0xFF90A4AE), Offset(c.x + r * 0.5f, c.y - r * 0.85f), Offset(c.x + r * 0.62f, c.y - r * 1.12f), strokeWidth = rx * 0.04f)
            drawCircle(Color(0xFFFF4D6D), radius = rx * 0.06f, center = Offset(c.x + r * 0.62f, c.y - r * 1.12f))
        }
    }
}

private fun starPath(c: Offset, outer: Float, inner: Float): Path = Path().apply {
    for (k in 0 until 10) {
        val r = if (k % 2 == 0) outer else inner
        val a = Math.toRadians((-90.0 + k * 36.0))
        val x = c.x + r * cos(a).toFloat()
        val y = c.y + r * kotlin.math.sin(a).toFloat()
        if (k == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private fun DrawScope.drawGlasses(g: Glasses, cx: Float, eyeY: Float, dx: Float, er: Float) {
    if (g == Glasses.NONE) return
    val ink = NumColors.Ink
    val bridgeY = eyeY - er * 0.2f
    when (g) {
        Glasses.SHADES -> {
            for (s in listOf(-1f, 1f)) {
                val ex = cx + s * dx
                drawOval(Color(0xEE1E1B2E), Offset(ex - er * 1.15f, eyeY - er * 0.85f), Size(er * 2.3f, er * 1.7f))
                drawLine(Color.White.copy(alpha = 0.5f), Offset(ex - er * 0.6f, eyeY - er * 0.35f), Offset(ex - er * 0.15f, eyeY - er * 0.6f), strokeWidth = er * 0.18f, cap = StrokeCap.Round)
            }
            drawLine(Color(0xFF1E1B2E), Offset(cx - dx + er, bridgeY), Offset(cx + dx - er, bridgeY), strokeWidth = er * 0.25f)
        }
        Glasses.SPECS -> {
            for (s in listOf(-1f, 1f)) {
                drawCircle(ink, radius = er * 1.2f, center = Offset(cx + s * dx, eyeY), style = Stroke(width = er * 0.25f))
            }
            drawLine(ink, Offset(cx - dx + er * 1.2f, bridgeY), Offset(cx + dx - er * 1.2f, bridgeY), strokeWidth = er * 0.25f)
        }
        Glasses.STARS -> {
            for (s in listOf(-1f, 1f)) {
                val c = Offset(cx + s * dx, eyeY)
                val p = starPath(c, er * 1.5f, er * 0.75f)
                drawPath(p, Color(0xCCFF6FA8))
                drawPath(p, Color(0xFFC2185B), style = Stroke(width = er * 0.15f, join = StrokeJoin.Round))
            }
            drawLine(Color(0xFFC2185B), Offset(cx - dx + er * 0.9f, bridgeY), Offset(cx + dx - er * 0.9f, bridgeY), strokeWidth = er * 0.2f)
        }
        Glasses.NONE -> {}
    }
}

private fun DrawScope.drawNeck(n: Neck, cx: Float, cy: Float, rx: Float, ry: Float) {
    val y = cy + ry * 0.66f
    when (n) {
        Neck.BOW_TIE -> {
            val w = rx * 0.26f
            val hgt = ry * 0.2f
            val left = Path().apply {
                moveTo(cx, y)
                lineTo(cx - w, y - hgt)
                lineTo(cx - w, y + hgt)
                close()
            }
            val right = Path().apply {
                moveTo(cx, y)
                lineTo(cx + w, y - hgt)
                lineTo(cx + w, y + hgt)
                close()
            }
            val red = Color(0xFFE53958)
            drawPath(left, red)
            drawPath(right, red)
            drawPath(left, red.darken(0.35f), style = Stroke(width = rx * 0.025f, join = StrokeJoin.Round))
            drawPath(right, red.darken(0.35f), style = Stroke(width = rx * 0.025f, join = StrokeJoin.Round))
            drawCircle(red.darken(0.2f), radius = hgt * 0.45f, center = Offset(cx, y))
        }
        Neck.SCARF -> {
            val band = Color(0xFF4FC3F7)
            drawArc(
                band, startAngle = 20f, sweepAngle = 140f, useCenter = false,
                topLeft = Offset(cx - rx * 0.92f, cy - ry * 0.25f),
                size = Size(rx * 1.84f, ry * 1.0f),
                style = Stroke(width = ry * 0.2f, cap = StrokeCap.Round),
            )
            // Hanging end with stripes
            val tx = cx + rx * 0.38f
            val ty = cy + ry * 0.62f
            drawRect(band, Offset(tx, ty), Size(rx * 0.2f, ry * 0.42f))
            drawRect(Color.White.copy(alpha = 0.7f), Offset(tx, ty + ry * 0.12f), Size(rx * 0.2f, ry * 0.06f))
            drawRect(Color.White.copy(alpha = 0.7f), Offset(tx, ty + ry * 0.26f), Size(rx * 0.2f, ry * 0.06f))
        }
        Neck.NONE -> {}
    }
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

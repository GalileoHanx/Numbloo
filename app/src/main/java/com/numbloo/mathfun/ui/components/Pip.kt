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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
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
import com.numbloo.mathfun.model.Pet
import com.numbloo.mathfun.model.PipColors
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
    val pet: Pet = Pet.NONE,
    val rainbow: Boolean = false,
)

fun Profile.pipLook(): PipLook =
    PipLook(
        NumColors.pip(colorIndex), hat, glasses, neck, flag, hair, job, eyes, mouth, pet,
        rainbow = colorIndex == PipColors.RAINBOW,
    )

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
    pet: Pet = Pet.NONE,
    rainbow: Boolean = false,
) {
    val look = PipLook(color, hat, glasses, neck, flag, hair, job, eyes, mouth, pet, rainbow)
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
        if (pet != Pet.NONE) {
            Text(
                pet.emoji,
                fontSize = (boxW.value * 0.26f).sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = -(boxW * 0.05f))
                    .graphicsLayer { translationY = if (animated) -bounce * 10f else 0f },
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
        pet = look.pet,
        rainbow = look.rainbow,
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
    if (look.neck == Neck.CAPE) drawCape(cx, cy, rx, ry)
    // Feet
    drawOval(color.darken(0.2f), Offset(cx - rx * 0.6f, cy + ry * 0.72f), Size(rx * 0.42f, ry * 0.36f))
    drawOval(color.darken(0.2f), Offset(cx + rx * 0.18f, cy + ry * 0.72f), Size(rx * 0.42f, ry * 0.36f))

    // Body
    val bodyTopLeft = Offset(cx - rx, cy - ry)
    val bodySize = Size(rx * 2f, ry * 2f)
    val bodyBrush = if (look.rainbow) {
        Brush.sweepGradient(NumColors.Rainbow, center = Offset(cx, cy))
    } else {
        Brush.radialGradient(
            colors = listOf(color.lighten(0.45f), color),
            center = Offset(cx - rx * 0.3f, cy - ry * 0.4f),
            radius = rx * 1.3f,
        )
    }
    drawOval(brush = bodyBrush, topLeft = bodyTopLeft, size = bodySize)
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

    val headCloth = look.job == Job.NONE && (look.hat == Hat.GHUTRA || look.hat == Hat.SHEMAGH || look.hat == Hat.HIJAB)
    if (!headCloth) drawHair(look.hair, cx, cy, rx, ry)

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
    if (look.mouth == Mouth.BEARD) {
        val brown = Color(0xFF6D4C41)
        var deg = 20f
        while (deg <= 160f) {
            drawCircle(brown, rx * 0.13f, edge(cx, cy + ry * 0.22f, rx * 0.55f, ry * 0.48f, deg))
            deg += 14f
        }
        drawCircle(brown, rx * 0.16f, Offset(cx, cy + ry * 0.78f))
    }
    if (look.mouth == Mouth.CAT) {
        for (s in listOf(-1f, 1f)) {
            for (k in -1..1) {
                drawLine(
                    outline,
                    Offset(cx + s * rx * 0.45f, my - ry * 0.04f + k * ry * 0.06f),
                    Offset(cx + s * rx * 0.82f, my - ry * 0.08f + k * ry * 0.12f),
                    strokeWidth = stroke * 0.5f,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
    if (look.mouth == Mouth.MUSTACHE || look.mouth == Mouth.BEARD) {
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
        Hair.RAINBOW -> {
            val from = 205f
            val to = 335f
            val spikes = 6
            val step = (to - from) / spikes
            val band = Path()
            val s0 = edge(cx, cy, rx, ry, from, 0.92f)
            band.moveTo(s0.x, s0.y)
            for (k in 0..spikes) {
                val pnt = edge(cx, cy, rx, ry, from + step * k, 0.92f)
                band.lineTo(pnt.x, pnt.y)
            }
            band.close()
            drawPath(band, Color(0xFF7E57C2))
            for (k in 0 until spikes) {
                val a = edge(cx, cy, rx, ry, from + step * k, 0.92f)
                val tip = edge(cx, cy, rx, ry, from + step * (k + 0.5f), 1.45f)
                val b = edge(cx, cy, rx, ry, from + step * (k + 1), 0.92f)
                val p = Path().apply {
                    moveTo(a.x, a.y)
                    lineTo(tip.x, tip.y)
                    lineTo(b.x, b.y)
                    close()
                }
                drawPath(p, NumColors.Rainbow[k % 6])
            }
        }
        Hair.LONG -> {
            val c = Color(0xFFF4C542)
            for (side in listOf(-1f, 1f)) {
                val x = if (side < 0f) cx - rx * 1.1f else cx + rx * 0.8f
                drawRoundRect(c, Offset(x, cy - ry * 0.75f), Size(rx * 0.3f, ry * 1.45f), CornerRadius(rx * 0.15f, rx * 0.15f))
            }
            drawArc(c, 180f, 180f, true, Offset(cx - rx * 1.08f, cy - ry * 1.14f), Size(rx * 2.16f, ry * 1.0f))
            drawArc(
                c.darken(0.2f), 180f, 180f, false, Offset(cx - rx * 1.08f, cy - ry * 1.14f), Size(rx * 2.16f, ry * 1.0f),
                style = Stroke(width = rx * 0.025f),
            )
        }
        Hair.BUN -> {
            val c = Color(0xFF6D4C41)
            drawArc(c, 180f, 180f, true, Offset(cx - rx * 0.95f, cy - ry * 1.06f), Size(rx * 1.9f, ry * 0.9f))
            val bun = Offset(cx, cy - ry * 1.18f)
            drawCircle(c, radius = rx * 0.3f, center = bun)
            drawCircle(c.darken(0.25f), radius = rx * 0.3f, center = bun, style = Stroke(width = rx * 0.025f))
            drawRect(Color(0xFFFF6FA8), Offset(cx - rx * 0.22f, cy - ry * 1.0f), Size(rx * 0.44f, ry * 0.07f))
        }
        Hair.AFRO -> {
            val c = Color(0xFF3E2723)
            var deg = 190f
            while (deg <= 350f) {
                drawCircle(c, radius = rx * 0.3f, center = edge(cx, cy, rx, ry, deg, 1.05f))
                deg += 16f
            }
            for (d in listOf(225f, 255f, 285f, 315f)) {
                drawCircle(c, radius = rx * 0.32f, center = edge(cx, cy, rx, ry, d, 1.28f))
            }
            drawCircle(c, radius = rx * 0.34f, center = edge(cx, cy, rx, ry, 270f, 1.45f))
        }
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
    if (style == Eyes.ROBOT) {
        val lit = er * 1.5f * blink.coerceAtLeast(0.15f)
        drawRoundRect(Color(0xFF263238), Offset(ex - er * 1.05f, eyeY - er * 0.85f), Size(er * 2.1f, er * 1.7f), CornerRadius(er * 0.3f, er * 0.3f))
        drawRoundRect(Color(0xFF00E5FF), Offset(ex - er * 0.7f, eyeY - lit / 2f), Size(er * 1.4f, lit), CornerRadius(er * 0.2f, er * 0.2f))
        drawRect(Color.White.copy(alpha = 0.6f), Offset(ex - er * 0.55f, eyeY - lit / 2f + er * 0.1f), Size(er * 0.35f, er * 0.16f))
        return
    }
    if (style == Eyes.GOOGLY) {
        val gr = er * 1.25f
        drawCircle(Color.White, gr, Offset(ex, eyeY))
        drawCircle(outline, gr, Offset(ex, eyeY), style = Stroke(width = stroke * 0.6f))
        val off = if (side < 0f) Offset(-gr * 0.3f, gr * 0.35f) else Offset(gr * 0.35f, -gr * 0.25f)
        drawCircle(NumColors.Ink, gr * 0.45f, Offset(ex, eyeY) + off)
        return
    }
    if (style == Eyes.SPIRAL) {
        drawCircle(Color.White, er, Offset(ex, eyeY))
        drawCircle(outline, er, Offset(ex, eyeY), style = Stroke(width = stroke * 0.6f))
        val sp = Path()
        val turns = 2.5f
        val stepsN = 40
        for (k in 0..stepsN) {
            val t = k / stepsN.toFloat()
            val ang = t * turns * 2f * Math.PI.toFloat() * side
            val rr = er * 0.85f * t
            val x = ex + rr * cos(ang.toDouble()).toFloat()
            val y = eyeY + rr * sin(ang.toDouble()).toFloat()
            if (k == 0) sp.moveTo(x, y) else sp.lineTo(x, y)
        }
        drawPath(sp, NumColors.Ink, style = Stroke(width = er * 0.14f, cap = StrokeCap.Round))
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
    if (style == Eyes.LASHES) {
        for (k in -1..1) {
            val sx = ex + k * er * 0.5f
            val sy = eyeY - eh / 2f + abs(k) * er * 0.15f
            drawLine(
                NumColors.Ink, Offset(sx, sy), Offset(sx + k * er * 0.25f + side * er * 0.15f, sy - er * 0.42f),
                strokeWidth = er * 0.13f, cap = StrokeCap.Round,
            )
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
    if (style == Mouth.CAT) {
        val p = Path().apply {
            moveTo(cx - rx * 0.2f, my)
            cubicTo(cx - rx * 0.15f, my + ry * 0.12f, cx - rx * 0.03f, my + ry * 0.12f, cx, my)
            cubicTo(cx + rx * 0.03f, my + ry * 0.12f, cx + rx * 0.15f, my + ry * 0.12f, cx + rx * 0.2f, my)
        }
        drawPath(p, outline, style = Stroke(width = stroke, cap = StrokeCap.Round))
        val nose = Path().apply {
            moveTo(cx - rx * 0.06f, my - ry * 0.1f)
            lineTo(cx + rx * 0.06f, my - ry * 0.1f)
            lineTo(cx, my - ry * 0.02f)
            close()
        }
        drawPath(nose, Color(0xFFFF7B9C))
        return
    }
    if (style == Mouth.GRIN) {
        drawArc(mouthColor, 0f, 180f, true, Offset(cx - rx * 0.4f, my - ry * 0.24f), Size(rx * 0.8f, ry * 0.56f))
        drawRect(Color.White, Offset(cx - rx * 0.34f, my + ry * 0.04f), Size(rx * 0.68f, ry * 0.08f))
        drawOval(Color(0xFFFF7B9C), Offset(cx - rx * 0.14f, my + ry * 0.16f), Size(rx * 0.28f, ry * 0.1f))
        return
    }
    drawArc(mouthColor, startAngle = 0f, sweepAngle = 180f, useCenter = true, topLeft = Offset(left, top), size = Size(mw, mh))
    drawOval(Color(0xFFFF7B9C), Offset(cx - rx * 0.12f, my + ry * 0.06f), Size(rx * 0.24f, ry * 0.12f))
    when (style) {
        Mouth.TOOTHY -> {
            drawRect(Color.White, Offset(cx - rx * 0.1f, my + ry * 0.01f), Size(rx * 0.09f, ry * 0.11f))
            drawRect(Color.White, Offset(cx + rx * 0.01f, my + ry * 0.01f), Size(rx * 0.09f, ry * 0.11f))
        }
        Mouth.BUNNY -> {
            drawRoundRect(Color.White, Offset(cx - rx * 0.11f, my + ry * 0.01f), Size(rx * 0.1f, ry * 0.17f), CornerRadius(rx * 0.02f, rx * 0.02f))
            drawRoundRect(Color.White, Offset(cx + rx * 0.01f, my + ry * 0.01f), Size(rx * 0.1f, ry * 0.17f), CornerRadius(rx * 0.02f, rx * 0.02f))
            drawOval(Color(0xFFFF7B9C), Offset(cx - rx * 0.06f, my - ry * 0.15f), Size(rx * 0.12f, ry * 0.08f))
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
        Job.POLICE -> {
            val navy = Color(0xFF1A237E)
            val base = top + ry * 0.2f
            drawRoundRect(navy, Offset(cx - rx * 0.62f, base - h * 0.13f), Size(rx * 1.24f, h * 0.13f), CornerRadius(rx * 0.2f, rx * 0.2f))
            drawRect(Color.White, Offset(cx - rx * 0.62f, base - h * 0.05f), Size(rx * 1.24f, h * 0.02f))
            drawOval(Color(0xFF111111), Offset(cx - rx * 0.66f, base - ry * 0.06f), Size(rx * 1.32f, ry * 0.16f))
            drawPath(starPath(Offset(cx, base - h * 0.09f), rx * 0.12f, rx * 0.055f), Color(0xFFFFC93C))
        }
        Job.ARTIST -> {
            val beret = Color(0xFFC62828)
            rotate(-12f, Offset(cx, top)) {
                drawOval(beret, Offset(cx - rx * 0.7f, top - ry * 0.32f), Size(rx * 1.4f, ry * 0.5f))
                drawOval(beret.darken(0.2f), Offset(cx - rx * 0.7f, top - ry * 0.32f), Size(rx * 1.4f, ry * 0.5f), style = Stroke(width = rx * 0.025f))
            }
            drawLine(beret.darken(0.3f), Offset(cx + rx * 0.05f, top - ry * 0.3f), Offset(cx + rx * 0.08f, top - ry * 0.45f), strokeWidth = rx * 0.05f, cap = StrokeCap.Round)
            // a paintbrush behind the ear
            drawLine(Color(0xFF8D6E63), Offset(cx + rx * 0.7f, cy - ry * 0.75f), Offset(cx + rx * 1.0f, cy - ry * 0.15f), strokeWidth = rx * 0.05f, cap = StrokeCap.Round)
            drawCircle(Color(0xFF42A5F5), rx * 0.06f, Offset(cx + rx * 0.68f, cy - ry * 0.8f))
        }
        Job.FARMER -> {
            val straw = Color(0xFFE8C468)
            val base = top + ry * 0.15f
            drawOval(straw, Offset(cx - rx * 1.05f, base - ry * 0.14f), Size(rx * 2.1f, ry * 0.3f))
            drawArc(straw, 180f, 180f, true, Offset(cx - rx * 0.5f, base - h * 0.15f), Size(rx * 1.0f, h * 0.3f))
            drawRect(Color(0xFFD32F2F), Offset(cx - rx * 0.5f, base - h * 0.035f), Size(rx * 1.0f, h * 0.03f))
            drawOval(straw.darken(0.25f), Offset(cx - rx * 1.05f, base - ry * 0.14f), Size(rx * 2.1f, ry * 0.3f), style = Stroke(width = rx * 0.02f))
        }
        Job.KNIGHT -> {
            val steel = Color(0xFFB0BEC5)
            val base = top + ry * 0.3f
            drawOval(Color(0xFFE53935), Offset(cx - rx * 0.12f, base - ry * 1.25f), Size(rx * 0.24f, ry * 0.55f))
            drawArc(steel, 180f, 180f, true, Offset(cx - rx * 0.78f, base - ry * 0.75f), Size(rx * 1.56f, ry * 1.5f))
            drawArc(
                steel.darken(0.3f), 180f, 180f, true, Offset(cx - rx * 0.78f, base - ry * 0.75f), Size(rx * 1.56f, ry * 1.5f),
                style = Stroke(width = rx * 0.03f),
            )
            drawRect(steel.darken(0.15f), Offset(cx - rx * 0.06f, base - ry * 0.75f), Size(rx * 0.12f, ry * 0.75f))
            drawRect(steel.darken(0.1f), Offset(cx - rx * 0.05f, base), Size(rx * 0.1f, ry * 0.25f))
        }
        Job.DETECTIVE -> {
            val tweed = Color(0xFF8D6E63)
            val base = top + ry * 0.22f
            drawArc(tweed, 180f, 180f, true, Offset(cx - rx * 0.62f, base - ry * 0.6f), Size(rx * 1.24f, ry * 1.2f))
            drawOval(tweed.darken(0.15f), Offset(cx - rx * 0.95f, base - ry * 0.1f), Size(rx * 0.5f, ry * 0.18f))
            drawOval(tweed.darken(0.15f), Offset(cx + rx * 0.45f, base - ry * 0.1f), Size(rx * 0.5f, ry * 0.18f))
            for (k in -2..2) {
                drawLine(
                    tweed.darken(0.3f), Offset(cx + k * rx * 0.2f, base - ry * 0.55f + abs(k) * ry * 0.08f), Offset(cx + k * rx * 0.2f, base),
                    strokeWidth = rx * 0.015f,
                )
            }
            drawLine(tweed.darken(0.3f), Offset(cx - rx * 0.55f, base - ry * 0.25f), Offset(cx + rx * 0.55f, base - ry * 0.25f), strokeWidth = rx * 0.015f)
            drawCircle(tweed.darken(0.35f), rx * 0.06f, Offset(cx, base - ry * 0.6f))
        }
        Job.DIVER -> {
            val eyeY = cy - ry * 0.18f
            val strap = Color(0xFF00897B)
            drawLine(strap, Offset(cx - rx * 0.98f, eyeY), Offset(cx - rx * 0.66f, eyeY), strokeWidth = ry * 0.14f)
            drawLine(strap, Offset(cx + rx * 0.66f, eyeY), Offset(cx + rx * 0.98f, eyeY), strokeWidth = ry * 0.14f)
            drawRoundRect(
                Color(0xFF80DEEA).copy(alpha = 0.45f), Offset(cx - rx * 0.66f, eyeY - ry * 0.32f), Size(rx * 1.32f, ry * 0.64f),
                CornerRadius(rx * 0.25f, rx * 0.25f),
            )
            drawRoundRect(
                strap, Offset(cx - rx * 0.66f, eyeY - ry * 0.32f), Size(rx * 1.32f, ry * 0.64f),
                CornerRadius(rx * 0.25f, rx * 0.25f), style = Stroke(width = rx * 0.07f),
            )
            val tube = Color(0xFFFFC107)
            drawLine(tube, Offset(cx + rx * 0.72f, cy + ry * 0.2f), Offset(cx + rx * 0.72f, top - ry * 0.35f), strokeWidth = rx * 0.1f, cap = StrokeCap.Round)
            drawLine(tube, Offset(cx + rx * 0.72f, cy + ry * 0.2f), Offset(cx + rx * 0.3f, cy + ry * 0.32f), strokeWidth = rx * 0.1f, cap = StrokeCap.Round)
        }
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
        Glasses.HEART_SHADES -> {
            for (s in listOf(-1f, 1f)) {
                val p = heartPath(Offset(cx + s * dx, eyeY), er * 1.35f)
                drawPath(p, Color(0xDDE53958))
                drawPath(p, Color(0xFF8E0038), style = Stroke(width = er * 0.15f, join = StrokeJoin.Round))
            }
            drawLine(Color(0xFF8E0038), Offset(cx - dx + er * 0.9f, bridgeY), Offset(cx + dx - er * 0.9f, bridgeY), strokeWidth = er * 0.2f)
        }
        Glasses.GOGGLES -> {
            val rim = Color(0xFF546E7A)
            drawLine(Color(0xFF37474F), Offset(cx - dx * 2.6f, eyeY), Offset(cx + dx * 2.6f, eyeY), strokeWidth = er * 0.45f)
            for (s in listOf(-1f, 1f)) {
                val c = Offset(cx + s * dx, eyeY)
                drawCircle(Color(0x6680DEEA), radius = er * 1.3f, center = c)
                drawCircle(rim, radius = er * 1.3f, center = c, style = Stroke(width = er * 0.38f))
            }
        }
        Glasses.MONOCLE -> {
            val gold = Color(0xFFFFC93C)
            val c = Offset(cx + dx, eyeY)
            drawCircle(Color(0x33FFFFFF), radius = er * 1.25f, center = c)
            drawCircle(gold, radius = er * 1.25f, center = c, style = Stroke(width = er * 0.22f))
            drawLine(gold, Offset(c.x + er * 0.9f, c.y + er * 0.9f), Offset(c.x + er * 1.4f, c.y + er * 4f), strokeWidth = er * 0.1f)
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
        Neck.TIE -> {
            val c = Color(0xFF1565C0)
            val blade = Path().apply {
                moveTo(cx - rx * 0.06f, y + ry * 0.06f)
                lineTo(cx + rx * 0.06f, y + ry * 0.06f)
                lineTo(cx + rx * 0.11f, y + ry * 0.24f)
                lineTo(cx, y + ry * 0.32f)
                lineTo(cx - rx * 0.11f, y + ry * 0.24f)
                close()
            }
            val knot = Path().apply {
                moveTo(cx - rx * 0.09f, y - ry * 0.06f)
                lineTo(cx + rx * 0.09f, y - ry * 0.06f)
                lineTo(cx + rx * 0.06f, y + ry * 0.07f)
                lineTo(cx - rx * 0.06f, y + ry * 0.07f)
                close()
            }
            drawPath(blade, c)
            drawPath(knot, c.darken(0.2f))
            drawLine(Color.White.copy(alpha = 0.5f), Offset(cx - rx * 0.08f, y + ry * 0.12f), Offset(cx + rx * 0.09f, y + ry * 0.2f), strokeWidth = rx * 0.03f)
        }
        Neck.BANDANA -> {
            val red = Color(0xFFE53935)
            val p = Path().apply {
                moveTo(cx - rx * 0.55f, y - ry * 0.12f)
                lineTo(cx + rx * 0.55f, y - ry * 0.12f)
                lineTo(cx, y + ry * 0.3f)
                close()
            }
            drawPath(p, red)
            drawPath(p, red.darken(0.3f), style = Stroke(width = rx * 0.02f, join = StrokeJoin.Round))
            for ((dx, dy) in listOf(-0.3f to -0.04f, 0.3f to -0.04f, 0f to 0.14f, -0.12f to 0.03f, 0.12f to 0.03f)) {
                drawCircle(Color.White, rx * 0.035f, Offset(cx + dx * rx, y + dy * ry))
            }
        }
        Neck.LEI -> {
            val petals = listOf(Color(0xFFFF6FA8), Color(0xFFFFC93C), Color(0xFFFF8A65), Color(0xFFBA68C8))
            var k = 0
            var deg = 15f
            while (deg <= 165f) {
                val c = edge(cx, cy + ry * 0.12f, rx * 0.82f, ry * 0.56f, deg)
                drawCircle(petals[k % petals.size], rx * 0.1f, c)
                drawCircle(Color(0xFFFFF59D), rx * 0.035f, c)
                k++
                deg += 15f
            }
        }
        Neck.BRONZE_MEDAL, Neck.SILVER_MEDAL, Neck.GOLD_MEDAL -> {
            val metal = when (n) {
                Neck.BRONZE_MEDAL -> Color(0xFFCD7F32)
                Neck.SILVER_MEDAL -> Color(0xFFC0C0C0)
                else -> Color(0xFFFFC93C)
            }
            val c = Offset(cx, cy + ry * 0.8f)
            drawLine(Color(0xFF1E88E5), Offset(cx - rx * 0.6f, cy + ry * 0.45f), c, strokeWidth = rx * 0.1f)
            drawLine(Color(0xFFE53935), Offset(cx + rx * 0.6f, cy + ry * 0.45f), c, strokeWidth = rx * 0.1f)
            drawCircle(metal, rx * 0.17f, c)
            drawCircle(metal.darken(0.3f), rx * 0.17f, c, style = Stroke(width = rx * 0.03f))
            drawPath(starPath(c, rx * 0.1f, rx * 0.045f), metal.darken(0.25f))
        }
        Neck.CAPE -> {
            val gold = Color(0xFFFFC93C)
            drawLine(Color(0xFFE53935), Offset(cx - rx * 0.62f, cy + ry * 0.45f), Offset(cx + rx * 0.62f, cy + ry * 0.45f), strokeWidth = rx * 0.08f)
            drawCircle(gold, rx * 0.08f, Offset(cx - rx * 0.62f, cy + ry * 0.45f))
            drawCircle(gold, rx * 0.08f, Offset(cx + rx * 0.62f, cy + ry * 0.45f))
        }
        Neck.NONE -> {}
    }
}

/** A superhero cape, drawn behind Pip. */
private fun DrawScope.drawCape(cx: Float, cy: Float, rx: Float, ry: Float) {
    val red = Color(0xFFE53935)
    val p = Path().apply {
        moveTo(cx - rx * 0.7f, cy - ry * 0.2f)
        cubicTo(cx - rx * 1.15f, cy + ry * 0.3f, cx - rx * 1.2f, cy + ry * 0.8f, cx - rx * 1.15f, cy + ry * 1.02f)
        cubicTo(cx - rx * 0.4f, cy + ry * 1.15f, cx + rx * 0.4f, cy + ry * 1.15f, cx + rx * 1.15f, cy + ry * 1.02f)
        cubicTo(cx + rx * 1.2f, cy + ry * 0.8f, cx + rx * 1.15f, cy + ry * 0.3f, cx + rx * 0.7f, cy - ry * 0.2f)
        close()
    }
    drawPath(p, red)
    drawPath(p, red.darken(0.3f), style = Stroke(width = rx * 0.03f, join = StrokeJoin.Round))
}

/** Ghutra, shemagh or hijab: cloth over the head with an opening for Pip's face. */
private fun DrawScope.drawHeadCloth(
    cx: Float,
    cy: Float,
    rx: Float,
    ry: Float,
    cloth: Color,
    checks: Color?,
    agal: Boolean,
    chin: Boolean,
) {
    val bottom = if (chin) cy + ry * 0.98f else cy + ry * 0.75f
    val outer = Path().apply {
        moveTo(cx - rx * 1.08f, bottom)
        lineTo(cx - rx * 1.08f, cy - ry * 0.1f)
        cubicTo(cx - rx * 1.08f, cy - ry * 1.32f, cx + rx * 1.08f, cy - ry * 1.32f, cx + rx * 1.08f, cy - ry * 0.1f)
        lineTo(cx + rx * 1.08f, bottom)
        if (chin) cubicTo(cx + rx * 0.6f, bottom + ry * 0.12f, cx - rx * 0.6f, bottom + ry * 0.12f, cx - rx * 1.08f, bottom)
        close()
    }
    val faceBottom = if (chin) cy + ry * 0.7f else cy + ry * 0.95f
    val face = Path().apply { addOval(Rect(cx - rx * 0.8f, cy - ry * 0.62f, cx + rx * 0.8f, faceBottom)) }
    val shape = Path.combine(PathOperation.Difference, outer, face)
    drawPath(shape, cloth)
    if (checks != null) {
        clipPath(shape) {
            val step = rx * 0.18f
            var x = cx - rx * 2.4f
            while (x < cx + rx * 2.4f) {
                drawLine(checks, Offset(x, cy - ry * 1.4f), Offset(x + rx * 1.3f, cy + ry * 1.2f), strokeWidth = rx * 0.035f)
                drawLine(checks, Offset(x, cy + ry * 1.2f), Offset(x + rx * 1.3f, cy - ry * 1.4f), strokeWidth = rx * 0.035f)
                x += step
            }
        }
    }
    drawPath(shape, cloth.darken(0.25f), style = Stroke(width = rx * 0.025f, join = StrokeJoin.Round))
    if (agal) {
        val black = Color(0xFF1B1B1B)
        drawOval(black, Offset(cx - rx * 0.86f, cy - ry * 1.08f), Size(rx * 1.72f, ry * 0.36f), style = Stroke(width = rx * 0.07f))
        drawOval(black, Offset(cx - rx * 0.8f, cy - ry * 0.97f), Size(rx * 1.6f, ry * 0.3f), style = Stroke(width = rx * 0.05f))
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
        Hat.COWBOY -> {
            val brown = Color(0xFF8D5524)
            val base = top + ry * 0.12f
            drawOval(brown.darken(0.1f), Offset(cx - rx * 0.95f, base - ry * 0.12f), Size(rx * 1.9f, ry * 0.26f))
            val crown = Path().apply {
                moveTo(cx - rx * 0.42f, base)
                lineTo(cx - rx * 0.36f, base - h * 0.17f)
                cubicTo(cx - rx * 0.15f, base - h * 0.2f, cx - rx * 0.05f, base - h * 0.13f, cx, base - h * 0.15f)
                cubicTo(cx + rx * 0.05f, base - h * 0.13f, cx + rx * 0.15f, base - h * 0.2f, cx + rx * 0.36f, base - h * 0.17f)
                lineTo(cx + rx * 0.42f, base)
                close()
            }
            drawPath(crown, brown)
            drawRect(Color(0xFF3E2723), Offset(cx - rx * 0.41f, base - h * 0.045f), Size(rx * 0.82f, h * 0.03f))
        }
        Hat.PIRATE -> {
            val base = top + ry * 0.15f
            val p = Path().apply {
                moveTo(cx - rx * 0.9f, base)
                cubicTo(cx - rx * 0.6f, base - h * 0.3f, cx + rx * 0.6f, base - h * 0.3f, cx + rx * 0.9f, base)
                cubicTo(cx + rx * 0.4f, base - h * 0.06f, cx - rx * 0.4f, base - h * 0.06f, cx - rx * 0.9f, base)
                close()
            }
            drawPath(p, Color(0xFF212121))
            drawPath(p, Color(0xFFFFC93C), style = Stroke(width = rx * 0.03f))
            val sk = Offset(cx, base - h * 0.13f)
            drawLine(Color.White, sk + Offset(-rx * 0.15f, rx * 0.05f), sk + Offset(rx * 0.15f, rx * 0.19f), strokeWidth = rx * 0.035f)
            drawLine(Color.White, sk + Offset(rx * 0.15f, rx * 0.05f), sk + Offset(-rx * 0.15f, rx * 0.19f), strokeWidth = rx * 0.035f)
            drawCircle(Color.White, rx * 0.1f, sk)
            drawCircle(Color(0xFF212121), rx * 0.025f, sk + Offset(-rx * 0.04f, -rx * 0.01f))
            drawCircle(Color(0xFF212121), rx * 0.025f, sk + Offset(rx * 0.04f, -rx * 0.01f))
        }
        Hat.BEANIE -> {
            val c = Color(0xFF26A69A)
            val base = top + ry * 0.28f
            drawArc(c, 180f, 180f, true, Offset(cx - rx * 0.72f, base - ry * 0.7f), Size(rx * 1.44f, ry * 1.4f))
            drawRoundRect(c.darken(0.2f), Offset(cx - rx * 0.76f, base - ry * 0.16f), Size(rx * 1.52f, ry * 0.24f), CornerRadius(rx * 0.1f, rx * 0.1f))
            for (k in -3..3) {
                drawLine(c.darken(0.35f), Offset(cx + k * rx * 0.2f, base - ry * 0.14f), Offset(cx + k * rx * 0.2f, base + ry * 0.06f), strokeWidth = rx * 0.02f)
            }
            drawCircle(Color(0xFFFFF59D), rx * 0.14f, Offset(cx, base - ry * 0.72f))
        }
        Hat.VIKING -> {
            val steel = Color(0xFF90A4AE)
            val base = top + ry * 0.25f
            for (sd in listOf(-1f, 1f)) {
                val horn = Path().apply {
                    moveTo(cx + sd * rx * 0.5f, base - ry * 0.25f)
                    cubicTo(cx + sd * rx * 0.95f, base - ry * 0.35f, cx + sd * rx * 1.0f, base - ry * 0.75f, cx + sd * rx * 0.85f, base - ry * 1.05f)
                    cubicTo(cx + sd * rx * 0.8f, base - ry * 0.7f, cx + sd * rx * 0.7f, base - ry * 0.5f, cx + sd * rx * 0.45f, base - ry * 0.45f)
                    close()
                }
                drawPath(horn, Color(0xFFFFF3E0))
                drawPath(horn, Color(0xFFBCAAA4), style = Stroke(width = rx * 0.025f, join = StrokeJoin.Round))
            }
            drawArc(steel, 180f, 180f, true, Offset(cx - rx * 0.68f, base - ry * 0.68f), Size(rx * 1.36f, ry * 1.36f))
            drawRect(steel.darken(0.2f), Offset(cx - rx * 0.7f, base - ry * 0.1f), Size(rx * 1.4f, ry * 0.14f))
            for (k in -2..2) drawCircle(Color(0xFFECEFF1), rx * 0.03f, Offset(cx + k * rx * 0.28f, base - ry * 0.03f))
        }
        Hat.GRAD_CAP -> {
            val black = Color(0xFF212121)
            val base = top + ry * 0.2f
            drawRect(black, Offset(cx - rx * 0.4f, base - ry * 0.3f), Size(rx * 0.8f, ry * 0.32f))
            val board = Path().apply {
                moveTo(cx, base - ry * 0.62f)
                lineTo(cx + rx * 0.85f, base - ry * 0.38f)
                lineTo(cx, base - ry * 0.16f)
                lineTo(cx - rx * 0.85f, base - ry * 0.38f)
                close()
            }
            drawPath(board, black)
            drawPath(board, Color(0xFF424242), style = Stroke(width = rx * 0.02f))
            val gold = Color(0xFFFFC93C)
            drawCircle(gold, rx * 0.04f, Offset(cx, base - ry * 0.39f))
            drawLine(gold, Offset(cx, base - ry * 0.39f), Offset(cx + rx * 0.55f, base - ry * 0.3f), strokeWidth = rx * 0.025f)
            drawLine(gold, Offset(cx + rx * 0.55f, base - ry * 0.3f), Offset(cx + rx * 0.58f, base + ry * 0.05f), strokeWidth = rx * 0.025f)
            drawRoundRect(gold, Offset(cx + rx * 0.53f, base + ry * 0.02f), Size(rx * 0.1f, ry * 0.16f), CornerRadius(rx * 0.03f, rx * 0.03f))
        }
        Hat.TOP_HAT -> {
            val black = Color(0xFF212121)
            val base = top + ry * 0.15f
            drawOval(black, Offset(cx - rx * 0.72f, base - ry * 0.1f), Size(rx * 1.44f, ry * 0.22f))
            drawRect(black, Offset(cx - rx * 0.45f, base - h * 0.24f), Size(rx * 0.9f, h * 0.24f))
            drawOval(Color(0xFF424242), Offset(cx - rx * 0.45f, base - h * 0.24f - ry * 0.06f), Size(rx * 0.9f, ry * 0.12f))
            drawRect(Color(0xFFD32F2F), Offset(cx - rx * 0.45f, base - h * 0.06f), Size(rx * 0.9f, h * 0.04f))
        }
        Hat.GHUTRA -> drawHeadCloth(cx, top + ry, rx, ry, Color(0xFFFAFAFA), null, agal = true, chin = false)
        Hat.SHEMAGH -> drawHeadCloth(cx, top + ry, rx, ry, Color(0xFFFAFAFA), Color(0xFFD32F2F), agal = true, chin = false)
        Hat.HIJAB -> drawHeadCloth(cx, top + ry, rx, ry, Color(0xFFF48FB1), null, agal = false, chin = true)
    }
}

package com.numbloo.mathfun.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numbloo.mathfun.ui.theme.NumColors
import com.numbloo.mathfun.ui.theme.darken
import kotlinx.coroutines.delay

/** Chunky, friendly text used everywhere. */
@Composable
fun FunText(
    text: String,
    size: TextUnit = 20.sp,
    color: Color = NumColors.Ink,
    modifier: Modifier = Modifier,
    weight: FontWeight = FontWeight.ExtraBold,
    align: TextAlign = TextAlign.Center,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size,
        fontWeight = weight,
        textAlign = align,
        lineHeight = size * 1.2f,
        maxLines = maxLines,
    )
}

/** A squishy card that shrinks a little when pressed, with a darker "lip" underneath. */
@Composable
fun BouncyBox(
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    corner: Dp = 22.dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, label = "press")
    val base = if (enabled) color else NumColors.Locked
    val shape = RoundedCornerShape(corner)
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .background(base.darken(0.22f))
            .padding(bottom = 5.dp)
            .clip(shape)
            .background(base),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
fun BouncyButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    fontSize: TextUnit = 22.sp,
    height: Dp = 64.dp,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    BouncyBox(color = color, modifier = modifier.height(height), enabled = enabled, onClick = onClick) {
        FunText(text, fontSize, textColor, Modifier.padding(horizontal = 12.dp), maxLines = 2)
    }
}

/** Round white button for icons such as back and repeat-voice. */
@Composable
fun RoundButton(emoji: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    BouncyBox(color = Color.White, modifier = modifier.size(54.dp), corner = 27.dp, onClick = onClick) {
        Text(emoji, fontSize = 24.sp)
    }
}

/** Top bar: back button, title and an optional thing on the right. */
@Composable
fun TopBar(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    color: Color = NumColors.Ink,
    right: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) RoundButton("⬅️", onClick = onBack) else Spacer(Modifier.size(54.dp))
        FunText(title, 24.sp, color, Modifier.weight(1f).padding(horizontal = 8.dp), maxLines = 1)
        Row(verticalAlignment = Alignment.CenterVertically) { right() }
    }
}

/** Speech bubble with a little tail pointing left at Pip. */
@Composable
fun SpeechBubble(text: String, modifier: Modifier = Modifier, size: TextUnit = 18.sp) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(width = 12.dp, height = 18.dp)) {
            val p = Path().apply {
                moveTo(this@Canvas.size.width + 2f, 0f)
                lineTo(0f, this@Canvas.size.height / 2f)
                lineTo(this@Canvas.size.width + 2f, this@Canvas.size.height)
                close()
            }
            drawPath(p, Color.White)
        }
        Box(
            Modifier
                .weight(1f)
                .heightIn(min = 52.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            FunText(text, size, NumColors.Ink, align = TextAlign.Start, weight = FontWeight.Bold)
        }
    }
}

/** ★★☆ style rating. */
@Composable
fun StarsRow(stars: Int, max: Int = 3, size: TextUnit = 28.sp, modifier: Modifier = Modifier) {
    Row(modifier) {
        for (i in 1..max) {
            Text(
                "★",
                fontSize = size,
                color = if (i <= stars) NumColors.Sun else NumColors.Locked,
            )
        }
    }
}

/** Little dots showing question progress. */
@Composable
fun ProgressDots(total: Int, done: Int, color: Color, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        for (i in 0 until total) {
            val d = when {
                i < done -> 14.dp
                i == done -> 18.dp
                else -> 12.dp
            }
            Box(
                Modifier
                    .size(d)
                    .clip(CircleShape)
                    .background(if (i <= done) color else NumColors.Locked)
            )
        }
    }
}

/**
 * Shows [count] emoji in rows of [perRow].
 * [faded] greys out some items; [label] can put a small number under an item.
 */
@Composable
fun EmojiGrid(
    count: Int,
    emoji: String,
    modifier: Modifier = Modifier,
    perRow: Int = 5,
    size: TextUnit = 30.sp,
    faded: (Int) -> Boolean = { false },
    label: (Int) -> String? = { null },
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        (0 until count).chunked(perRow.coerceAtLeast(1)).forEach { row ->
            Row(horizontalArrangement = Arrangement.Center) {
                row.forEach { i ->
                    Column(
                        Modifier.padding(horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            emoji,
                            fontSize = size,
                            modifier = Modifier.alpha(if (faded(i)) 0.2f else 1f),
                        )
                        val l = label(i)
                        if (l != null) FunText(l, 13.sp, NumColors.InkSoft)
                    }
                }
            }
        }
    }
}

/** Pops its content in with a springy bounce after [delayMs]. */
@Composable
fun PopIn(delayMs: Int = 0, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val s = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        s.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 300f))
    }
    Box(modifier.graphicsLayer { scaleX = s.value; scaleY = s.value }) { content() }
}

/** Gentle pulsing used to point at the right answer when a hint is needed. */
@Composable
fun pulseScale(active: Boolean): Float {
    val inf = rememberInfiniteTransition(label = "pulse")
    val v by inf.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulseV",
    )
    return if (active) v else 1f
}

/** Soft polka-dot background. */
@Composable
fun FunBackground(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawCircle(tint.copy(alpha = 0.10f), radius = w * 0.45f, center = Offset(w * 0.95f, h * 0.05f))
        drawCircle(tint.copy(alpha = 0.08f), radius = w * 0.35f, center = Offset(0f, h * 0.55f))
        drawCircle(tint.copy(alpha = 0.07f), radius = w * 0.30f, center = Offset(w * 0.9f, h * 0.95f))
        drawCircle(NumColors.Sun.copy(alpha = 0.10f), radius = w * 0.08f, center = Offset(w * 0.2f, h * 0.15f))
    }
}

/** A white rounded panel. */
@Composable
fun Panel(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .background(color)
            .border(2.dp, NumColors.Ink.copy(alpha = 0.06f), RoundedCornerShape(24.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

/** Small coloured tag like "Start here!". */
@Composable
fun Badge(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        FunText(text, 12.sp, Color.White)
    }
}

/** Vertical gap. */
@Composable
fun Gap(h: Dp = 12.dp) {
    Spacer(Modifier.height(h).width(1.dp))
}

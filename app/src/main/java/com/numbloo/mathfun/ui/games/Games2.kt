package com.numbloo.mathfun.ui.games

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numbloo.mathfun.audio.Sfx
import com.numbloo.mathfun.logic.Question
import com.numbloo.mathfun.model.Topic
import com.numbloo.mathfun.ui.components.BouncyButton
import com.numbloo.mathfun.ui.components.EmojiGrid
import com.numbloo.mathfun.ui.components.FunText
import com.numbloo.mathfun.ui.components.Pip
import com.numbloo.mathfun.ui.components.PipMood
import com.numbloo.mathfun.ui.components.PopIn
import com.numbloo.mathfun.ui.components.pulseScale
import com.numbloo.mathfun.ui.screens.AnswerFn
import com.numbloo.mathfun.ui.screens.GameCtx
import com.numbloo.mathfun.ui.theme.NumColors
import com.numbloo.mathfun.ui.theme.darken
import com.numbloo.mathfun.ui.theme.lighten
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

/* ===================================================================== SHARED BITS */

/** A row of numbered pads (from..to) with a little rider standing on one of them. */
@Composable
private fun PadLine(
    from: Int,
    to: Int,
    at: Int,
    rider: String,
    wrong: List<Int>,
    glowAt: Int,
    glow: Float,
    enabled: Boolean,
    flipRider: Boolean = false,
    padColor: Color = NumColors.Lily,
    onTap: (Int) -> Unit,
) {
    Row(Modifier.fillMaxWidth()) {
        for (n in from..to) {
            val isWrong = n in wrong
            val isGlow = n == glowAt
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (at == n) rider else " ",
                    fontSize = 22.sp,
                    modifier = Modifier.graphicsLayer { scaleX = if (flipRider) -1f else 1f },
                )
                Box(
                    Modifier
                        .padding(1.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .graphicsLayer {
                            val s = if (isGlow) glow else 1f
                            scaleX = s
                            scaleY = s
                        }
                        .clip(CircleShape)
                        .background(
                            when {
                                isWrong -> NumColors.Locked
                                isGlow -> NumColors.Sun
                                else -> padColor
                            }
                        )
                        .clickable(enabled = enabled) { onTap(n) },
                    contentAlignment = Alignment.Center,
                ) {
                    FunText("$n", 13.sp, Color.White)
                }
            }
        }
    }
}

/** A big round number, used by the machines. */
@Composable
private fun NumberBall(text: String, color: Color, modifier: Modifier = Modifier, ball: Dp = 76.dp) {
    Box(
        modifier
            .size(ball)
            .clip(CircleShape)
            .background(color)
            .border(4.dp, color.darken(0.25f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        FunText(text, (ball.value * if (text.length >= 3) 0.3f else 0.4f).sp, Color.White)
    }
}

/* ===================================================================== ADDITION */

/** Count the dots on both halves of a domino. */
@Composable
fun DominoDotsGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    GameColumn {
        PopIn {
            Domino(q.a, q.b, ctx.color)
        }
        if (attempts >= 1 || ctx.guided) {
            FunText("${q.a} + ${q.b} = ?", 36.sp, ctx.color)
            FunText("Count one side, then keep counting the other!", 16.sp, NumColors.InkSoft)
        } else {
            FunText("How many dots in all?", 28.sp, ctx.color)
        }
        ChoiceRow(
            q.choices, q.answer, attempts, ctx, onAnswer,
            correctLine = "Dot-tastic! ${q.a} and ${q.b} make ${q.answer}!",
        )
    }
}

@Composable
private fun Domino(a: Int, b: Int, dot: Color) {
    Canvas(Modifier.size(width = 250.dp, height = 125.dp)) {
        val w = size.width
        val h = size.height
        val corner = CornerRadius(h * 0.16f, h * 0.16f)
        drawRoundRect(Color.Black.copy(alpha = 0.08f), topLeft = Offset(6f, 10f), size = size, cornerRadius = corner)
        drawRoundRect(Color.White, cornerRadius = corner)
        drawRoundRect(NumColors.Ink, cornerRadius = corner, style = Stroke(width = h * 0.04f))
        drawLine(NumColors.Ink, Offset(w / 2f, h * 0.14f), Offset(w / 2f, h * 0.86f), strokeWidth = h * 0.03f)
        drawDots(a, Offset(0f, 0f), Size(w / 2f, h), dot)
        drawDots(b, Offset(w / 2f, 0f), Size(w / 2f, h), dot)
    }
}

private fun DrawScope.drawDots(n: Int, origin: Offset, area: Size, color: Color) {
    val lo = 0.27f
    val hi = 0.73f
    val mid = 0.5f
    val corners = listOf(lo to lo, hi to lo, lo to hi, hi to hi)
    val pts: List<Pair<Float, Float>> = when (n) {
        1 -> listOf(mid to mid)
        2 -> listOf(lo to lo, hi to hi)
        3 -> listOf(lo to lo, mid to mid, hi to hi)
        4 -> corners
        5 -> corners + (mid to mid)
        else -> corners + (lo to mid) + (hi to mid)
    }
    val r = min(area.width, area.height) * 0.09f
    for ((x, y) in pts) {
        drawCircle(color, radius = r, center = Offset(origin.x + area.width * x, origin.y + area.height * y))
    }
}

/** Snail crawls forward on the number line. Tap where it stops. */
@Composable
fun SnailRaceGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val lo = max(0, q.a - (10 - q.b) / 2).let { if (it + 10 < q.answer) q.answer - 10 else it }
    var snailAt by remember { mutableIntStateOf(q.a) }
    var busy by remember { mutableStateOf(false) }
    val wrongPads = remember { mutableStateListOf<Int>() }
    val scope = rememberCoroutineScope()
    val glow = pulseScale(attempts >= 2)

    GameColumn {
        FunText("${q.a} + ${q.b} = ?", 46.sp, ctx.color)
        FunText("Crawl forward ${q.b}:  " + "➡️".repeat(q.b), 18.sp, NumColors.InkSoft)
        PadLine(
            from = lo,
            to = lo + 10,
            at = snailAt,
            rider = "🐌",
            wrong = wrongPads,
            glowAt = if (attempts >= 2) q.answer else -1,
            glow = glow,
            enabled = !busy,
            flipRider = true,
            padColor = Color(0xFF8BC34A),
        ) { n ->
            if (n == q.answer) {
                busy = true
                scope.launch {
                    while (snailAt < q.answer) {
                        delay(380)
                        snailAt++
                        ctx.sfx(Sfx.TAP)
                    }
                    delay(250)
                    onAnswer(true, "Slime-tastic! Snail stopped at ${q.answer}!")
                }
            } else {
                if (n !in wrongPads) wrongPads.add(n)
                onAnswer(false, "Hmm, Snail won't stop at $n. Count forward ${q.b}!")
            }
        }
        if (attempts >= 1) {
            BouncyButton("👀  Show me the crawl", Color.White, Modifier.fillMaxWidth(), textColor = NumColors.Ink, enabled = !busy) {
                busy = true
                scope.launch {
                    snailAt = q.a
                    for (k in 1..q.b) {
                        delay(500)
                        snailAt = q.a + k
                        ctx.sfx(Sfx.TAP)
                        ctx.say("${q.a + k}")
                    }
                    delay(1200)
                    snailAt = q.a
                    busy = false
                    ctx.say("Now tap where Snail stopped!", queue = true)
                }
            }
        }
    }
}

/** Something is hiding in a gift box: a + ? = total (or a ÷ ? = result at higher levels). */
@Composable
fun MysteryBoxGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val total = q.result
    var opened by remember { mutableStateOf(false) }
    val inf = rememberInfiniteTransition(label = "box")
    val wiggle by inf.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(260), RepeatMode.Reverse),
        label = "wiggle",
    )
    val pictures = ctx.beginner && q.topic == Topic.ADD && total <= 20
    val sym = q.topic.symbol

    GameColumn {
        FunText("${q.a} $sym ? = $total", if (total >= 100 || q.a >= 100) 38.sp else 44.sp, ctx.color)
        if (pictures) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiGrid(q.a, "🍎", size = 24.sp)
                FunText("+", 30.sp, ctx.color, Modifier.padding(horizontal = 6.dp))
                if (opened) {
                    PopIn { EmojiGrid(q.b, "🍏", size = 24.sp) }
                } else {
                    Text("🎁", fontSize = 56.sp, modifier = Modifier.graphicsLayer { rotationZ = wiggle })
                }
            }
            FunText("Now Pip has $total apples:", 16.sp, NumColors.InkSoft)
            EmojiGrid(
                count = total,
                emoji = "🍎",
                size = 22.sp,
                faded = { i -> attempts >= 1 && i < q.a },
                label = { i -> if (attempts >= 1 && i >= q.a) "${i - q.a + 1}" else null },
            )
            FunText("How many were in the box?", 18.sp, NumColors.InkSoft)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NumberBall("${q.a}", ctx.color, ball = 62.dp)
                FunText(" $sym ", 28.sp, ctx.color)
                Box(contentAlignment = Alignment.Center) {
                    if (opened) {
                        PopIn { NumberBall("${q.b}", NumColors.Good, ball = 62.dp) }
                    } else {
                        Text("🎁", fontSize = 46.sp, modifier = Modifier.graphicsLayer { rotationZ = wiggle })
                    }
                }
                FunText(" = ", 28.sp, ctx.color)
                NumberBall("$total", NumColors.Sun, ball = 62.dp)
            }
            FunText("What number is hiding in the box?", 18.sp, NumColors.InkSoft)
            if (attempts >= 1) {
                val hint = when (q.topic) {
                    Topic.DIV -> "Think: $total × ? = ${q.a}"
                    Topic.SUB -> "Think: ${q.a} − $total = ?"
                    else -> "Think: $total − ${q.a} = ?"
                }
                FunText(hint, 20.sp, ctx.color)
            }
        }
        ChoiceRow(
            q.choices, q.answer, attempts, ctx, onAnswer,
            correctLine = "Ta-da! ${q.b} was hiding in the box!",
            onCorrect = { opened = true },
        )
    }
}

/* ===================================================================== SUBTRACTION */

/** Pop some bubbles yourself, then count the ones left. */
@Composable
fun BubblePopGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val popped = remember { mutableStateListOf<Int>() }
    val done = popped.size >= q.b
    val remaining = (0 until q.a).filter { it !in popped }

    GameColumn {
        FunText(if (!done) "Pop ${q.b} bubbles!" else "${q.a} − ${q.b} = ?", 32.sp, ctx.color)
        FunText("Popped: ${popped.size} / ${q.b}", 18.sp, NumColors.InkSoft)
        (0 until q.a).chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { i ->
                    val isPopped = i in popped
                    val label = if (done && attempts >= 1 && !isPopped) "${remaining.indexOf(i) + 1}" else null
                    Bubble(isPopped, label, enabled = !done && !isPopped) {
                        popped.add(i)
                        ctx.sfx(Sfx.POP)
                    }
                }
            }
        }
        if (done) {
            FunText("How many bubbles are left?", 18.sp, NumColors.InkSoft)
            ChoiceRow(
                q.choices, q.answer, attempts, ctx, onAnswer,
                correctLine = "Bubble-licious! ${q.answer} bubbles left!",
            )
        }
    }
}

@Composable
private fun Bubble(popped: Boolean, label: String?, enabled: Boolean, onTap: () -> Unit) {
    val scale by animateFloatAsState(if (popped) 0.55f else 1f, label = "bubble")
    Box(
        Modifier
            .size(62.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            if (popped) {
                drawCircle(NumColors.Locked.copy(alpha = 0.6f), radius = r * 0.9f, style = Stroke(width = r * 0.08f))
            } else {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color(0xFFBDE7FF), Color(0xFF7FC8F8)),
                        center = Offset(r * 0.7f, r * 0.6f),
                        radius = r * 1.3f,
                    ),
                    radius = r * 0.95f,
                )
                drawCircle(Color(0xFF4FA3D9), radius = r * 0.95f, style = Stroke(width = r * 0.06f))
                drawCircle(Color.White, radius = r * 0.16f, center = Offset(r * 0.62f, r * 0.55f))
            }
        }
        if (popped) FunText("💥", 18.sp)
        if (label != null) FunText(label, 18.sp, NumColors.Ink)
    }
}

/** Two towers: how many more blocks does the tall one have? */
@Composable
fun TowerCompareGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val tall = q.a
    val short = q.b
    GameColumn {
        FunText("How many more blocks?", 28.sp, ctx.color)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(48.dp)) {
            Tower(tall, Color(0xFFFF8A3D), highlightFrom = if (attempts >= 1) short else Int.MAX_VALUE)
            Tower(short, Color(0xFF4FC3F7))
        }
        FunText("$tall − $short = ?", 36.sp, ctx.color)
        ChoiceRow(
            q.choices, q.answer, attempts, ctx, onAnswer,
            correctLine = "Block-buster! The tall tower has ${q.answer} more!",
        )
    }
}

@Composable
private fun Tower(n: Int, color: Color, highlightFrom: Int = Int.MAX_VALUE) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for (i in n - 1 downTo 0) {
            val hl = i >= highlightFrom
            PopIn((n - 1 - i) * 60) {
                Box(
                    Modifier
                        .padding(1.dp)
                        .size(width = 58.dp, height = 24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (hl) NumColors.Sun else color)
                        .border(2.dp, (if (hl) NumColors.Sun else color).darken(0.2f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (hl) FunText("${i - highlightFrom + 1}", 12.sp, NumColors.Ink)
                }
            }
        }
        FunText("$n", 22.sp, NumColors.Ink)
    }
}

/** Bunnies vanish into a magic hat. How many hid? (a − ? = left) */
@Composable
fun MagicHatGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val hid = q.b
    val left = q.a - q.b
    var hidden by remember { mutableIntStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    val big = q.a > 20
    LaunchedEffect(Unit) {
        delay(1500)
        if (big) {
            hidden = hid
            ctx.sfx(Sfx.WHOOSH)
        } else {
            repeat(hid) {
                hidden++
                ctx.sfx(Sfx.WHOOSH)
                delay(500)
            }
        }
    }
    val done = hidden >= hid
    val inf = rememberInfiniteTransition(label = "hat")
    val wiggle by inf.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(200), RepeatMode.Reverse),
        label = "hatWiggle",
    )

    GameColumn {
        Box(contentAlignment = Alignment.TopCenter) {
            Text(
                "🎩",
                fontSize = 66.sp,
                modifier = Modifier.graphicsLayer { rotationZ = if (done) 0f else wiggle },
            )
        }
        if (revealed) {
            PopIn {
                if (big) FunText("🐰 × $hid jumped out!", 26.sp, NumColors.Good) else EmojiGrid(hid, "🐰", size = 26.sp)
            }
        }
        if (big) {
            FunText("🐰 × ${q.a - hidden}", 40.sp, NumColors.Ink)
        } else {
            EmojiGrid(q.a - hidden, "🐰", size = 30.sp)
        }
        if (done) {
            FunText("${q.a} − ? = $left", 40.sp, ctx.color)
            FunText("There were ${q.a} bunnies. Now there are $left. How many hid?", 17.sp, NumColors.InkSoft)
            if (attempts >= 1) {
                val hint = if (hid <= 12) "Count up from $left: " + ((left + 1)..q.a).joinToString(", ") else "Think: ${q.a} − $left = ?"
                FunText(hint, 16.sp, ctx.color)
            }
            ChoiceRow(
                q.choices, q.answer, attempts, ctx, onAnswer,
                correctLine = "Abracadabra! ${q.answer} bunnies were in the hat!",
                onCorrect = { revealed = true },
            )
        } else {
            FunText(if (hidden == 0) "Watch the magic hat..." else "Abracadabra!", 20.sp, NumColors.InkSoft)
        }
    }
}

/* ===================================================================== MULTIPLICATION */

/** Rows of seeds bloom into flowers: an array. */
@Composable
fun FlowerGardenGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val rows = q.a
    val cols = q.b
    val bloomed = remember { mutableStateListOf<Boolean>().apply { repeat(rows) { add(false) } } }
    val all = bloomed.all { it }

    GameColumn {
        FunText(if (!all) "Tap each row to make it bloom!" else "$rows rows of $cols flowers", 22.sp, ctx.color)
        for (r in 0 until rows) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF8D6E63).copy(alpha = 0.22f))
                    .clickable(enabled = !bloomed[r]) {
                        bloomed[r] = true
                        ctx.sfx(Sfx.POP)
                    }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(cols) {
                    Text(if (bloomed[r]) "🌷" else "🌱", fontSize = 30.sp, modifier = Modifier.padding(horizontal = 2.dp))
                }
                if (all && attempts >= 1) FunText("  ${(r + 1) * cols}", 16.sp, ctx.color)
            }
        }
        if (all) {
            FunText(List(rows) { "$cols" }.joinToString(" + "), 20.sp, NumColors.InkSoft)
            FunText("$rows × $cols = ?", 38.sp, ctx.color)
            ChoiceRow(
                q.choices, q.answer, attempts, ctx, onAnswer,
                correctLine = "Flower power! $rows times $cols is ${q.answer}!",
            )
        }
    }
}

/** Every ladybug has the same number of spots. */
@Composable
fun LadybugSpotsGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    GameColumn {
        FunText("Each ladybug has ${q.b} spots", 22.sp, ctx.color)
        (0 until q.a).chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { i ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PopIn(i * 200) { Ladybug(q.b, Modifier.size(86.dp)) }
                        FunText(if (attempts >= 1) "${(i + 1) * q.b}" else " ", 15.sp, ctx.color)
                    }
                }
            }
        }
        FunText("${q.a} × ${q.b} = ?", 38.sp, ctx.color)
        ChoiceRow(
            q.choices, q.answer, attempts, ctx, onAnswer,
            correctLine = "Spot on! ${q.a} ladybugs have ${q.answer} spots!",
        )
    }
}

@Composable
private fun Ladybug(spots: Int, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h * 0.58f
        val r = w * 0.34f
        val ink = NumColors.Ink
        // legs
        for (k in -1..1) {
            val y = cy + k * r * 0.5f
            drawLine(ink, Offset(cx - r * 0.8f, y), Offset(cx - r * 1.25f, y + k * r * 0.2f), strokeWidth = w * 0.03f)
            drawLine(ink, Offset(cx + r * 0.8f, y), Offset(cx + r * 1.25f, y + k * r * 0.2f), strokeWidth = w * 0.03f)
        }
        // head + eyes
        drawCircle(ink, radius = r * 0.45f, center = Offset(cx, cy - r * 0.95f))
        drawCircle(Color.White, radius = r * 0.11f, center = Offset(cx - r * 0.18f, cy - r * 1.05f))
        drawCircle(Color.White, radius = r * 0.11f, center = Offset(cx + r * 0.18f, cy - r * 1.05f))
        // shell
        drawCircle(Color(0xFFE53935), radius = r, center = Offset(cx, cy))
        drawLine(ink, Offset(cx, cy - r), Offset(cx, cy + r), strokeWidth = w * 0.025f)
        drawCircle(Color.White.copy(alpha = 0.35f), radius = r * 0.18f, center = Offset(cx - r * 0.55f, cy - r * 0.55f))
        val spotPos = listOf(
            -0.45f to -0.35f, 0.45f to -0.35f,
            -0.55f to 0.2f, 0.55f to 0.2f,
            -0.3f to 0.62f, 0.3f to 0.62f,
            -0.15f to 0.05f, 0.15f to 0.05f,
        )
        for (k in 0 until min(spots, spotPos.size)) {
            val (dx, dy) = spotPos[k]
            drawCircle(ink, radius = r * 0.15f, center = Offset(cx + dx * r, cy + dy * r))
        }
    }
}

/** Shared "number machine": put a number in, crank, guess what comes out. */
@Composable
fun NumberMachineGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn, divide: Boolean) {
    var phase by remember { mutableIntStateOf(0) } // 0 = ready, 1 = working, 2 = asking
    var solved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val inf = rememberInfiniteTransition(label = "machine")
    val spin by inf.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "gear",
    )
    val shake by inf.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(70), RepeatMode.Reverse),
        label = "shake",
    )
    val drop by animateFloatAsState(if (phase >= 1) 1f else 0f, tween(500), label = "drop")
    val opSymbol = if (divide) "÷" else "×"
    val op = "$opSymbol ${q.b}"

    GameColumn {
        NumberBall(
            "${q.a}",
            NumColors.Grass,
            Modifier.graphicsLayer {
                translationY = drop * 60.dp.toPx()
                alpha = 1f - drop
            },
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .graphicsLayer { translationX = if (phase == 1) shake * 6.dp.toPx() else 0f }
                .clip(RoundedCornerShape(30.dp))
                .background(ctx.color)
                .border(5.dp, ctx.color.darken(0.25f), RoundedCornerShape(30.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚙️", fontSize = 40.sp, modifier = Modifier.graphicsLayer { rotationZ = if (phase == 1) spin else 0f })
                FunText("  $op  ", 50.sp, Color.White)
                Text("⚙️", fontSize = 40.sp, modifier = Modifier.graphicsLayer { rotationZ = if (phase == 1) -spin else 0f })
            }
        }
        NumberBall(
            when {
                solved -> "${q.answer}"
                phase >= 2 -> "?"
                else -> "…"
            },
            if (solved) NumColors.Good else NumColors.Sun,
        )
        when (phase) {
            0 -> BouncyButton("⚙️  Crank it!", NumColors.Ink, Modifier.fillMaxWidth(), height = 70.dp) {
                phase = 1
                ctx.sfx(Sfx.WHOOSH)
                scope.launch {
                    delay(1200)
                    phase = 2
                    ctx.sfx(Sfx.POP)
                }
            }
            1 -> FunText("Whirr... clank... bzzt!", 22.sp, NumColors.InkSoft)
            else -> {
                FunText("${q.a} $op = ?", 36.sp, ctx.color)
                if (attempts >= 1) {
                    val hint = when {
                        divide && q.answer <= 10 -> "Count by ${q.b}s to ${q.a}: " + (1..q.answer).joinToString(", ") { "${it * q.b}" }
                        divide -> "Think: ${q.b} × ? = ${q.a}"
                        q.a <= 10 -> List(q.a) { "${q.b}" }.joinToString(" + ")
                        else -> "Think: ${q.b} × ${q.a}"
                    }
                    FunText(hint, 16.sp, NumColors.InkSoft)
                }
                ChoiceRow(
                    q.choices, q.answer, attempts, ctx, onAnswer,
                    correctLine = "Ding ding! The machine says ${q.answer}!",
                    onCorrect = { solved = true },
                )
            }
        }
    }
}

/* ===================================================================== DIVISION */

/** Gloop eats the same number of cookies per bite. How many bites? */
@Composable
fun MonsterMunchGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val total = q.a
    val bite = q.b
    var bites by remember { mutableIntStateOf(0) }
    var chewing by remember { mutableStateOf(false) }
    LaunchedEffect(bites) {
        if (bites > 0) {
            chewing = true
            delay(600)
            chewing = false
        }
    }
    val left = total - bites * bite

    GameColumn {
        FunText("Gloop eats $bite cookies per bite!", 22.sp, ctx.color)
        Pip(
            Modifier
                .size(140.dp)
                .clickable(enabled = left > 0) {
                    bites++
                    ctx.sfx(Sfx.CHOMP)
                },
            mood = when {
                chewing -> PipMood.CHEW
                left == 0 -> PipMood.HAPPY
                else -> PipMood.WOW
            },
            color = Color(0xFF9C6ADE),
        )
        FunText(if (left > 0) "Tap Gloop to take a bite!" else "Burp! All gone!", 18.sp, NumColors.InkSoft)
        Box(Modifier.heightIn(min = 40.dp), contentAlignment = Alignment.Center) {
            if (left > 0) EmojiGrid(left, "🍪", size = 28.sp, perRow = 6)
        }
        if (bites > 0) {
            EmojiGrid(
                count = bites,
                emoji = "😋",
                size = 24.sp,
                perRow = 6,
                label = { i -> if (left == 0 && attempts >= 1) "${i + 1}" else null },
            )
        }
        if (left == 0) {
            FunText("$total ÷ $bite = ?", 38.sp, ctx.color)
            FunText("How many bites did Gloop take?", 17.sp, NumColors.InkSoft)
            ChoiceRow(
                q.choices, q.answer, attempts, ctx, onAnswer,
                correctLine = "Munch-tastic! ${q.answer} bites!",
            )
        }
    }
}

/** Sort cupcakes onto trays with the same number on each. How many trays? */
@Composable
fun CupcakeTraysGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val total = q.a
    val per = q.b
    var sorted by remember { mutableStateOf(false) }

    GameColumn {
        FunText("$total cupcakes, $per on each tray", 22.sp, ctx.color)
        if (!sorted) {
            EmojiGrid(total, "🧁", size = 28.sp, perRow = 6)
            BouncyButton("✨  Sort onto trays!", ctx.color, Modifier.fillMaxWidth(), height = 68.dp) {
                sorted = true
                ctx.sfx(Sfx.WHOOSH)
            }
        } else {
            for (t in 0 until q.answer) {
                PopIn(t * 220) {
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ctx.color.lighten(0.75f))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(per) { Text("🧁", fontSize = 26.sp) }
                        if (attempts >= 1) FunText("   tray ${t + 1}", 14.sp, NumColors.InkSoft)
                    }
                }
            }
            FunText("$total ÷ $per = ?", 38.sp, ctx.color)
            FunText("How many trays did we fill?", 17.sp, NumColors.InkSoft)
            ChoiceRow(
                q.choices, q.answer, attempts, ctx, onAnswer,
                correctLine = "Sweet! ${q.answer} trays of cupcakes!",
            )
        }
    }
}

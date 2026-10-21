package com.numbloo.mathfun.ui.games

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numbloo.mathfun.audio.Sfx
import com.numbloo.mathfun.logic.Question
import com.numbloo.mathfun.model.Topic
import com.numbloo.mathfun.ui.components.BouncyButton
import com.numbloo.mathfun.ui.components.EmojiGrid
import com.numbloo.mathfun.ui.components.FunText
import com.numbloo.mathfun.ui.components.GroupsVisual
import com.numbloo.mathfun.ui.components.LookPip
import com.numbloo.mathfun.ui.components.PipMood
import com.numbloo.mathfun.ui.components.ShareVisual
import com.numbloo.mathfun.ui.components.pulseScale
import com.numbloo.mathfun.ui.screens.AnswerFn
import com.numbloo.mathfun.ui.screens.GameCtx
import com.numbloo.mathfun.ui.theme.NumColors
import com.numbloo.mathfun.ui.theme.darken
import com.numbloo.mathfun.ui.theme.lighten
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

/* ===================================================================== ADDITION */

/** Tap the tree to drop apples into Pip's mouth, then press Done. Teaches counting on. */
@Composable
fun FeedPipGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    var fed by remember { mutableIntStateOf(0) }
    var chewTick by remember { mutableIntStateOf(0) }
    var chewing by remember { mutableStateOf(false) }
    LaunchedEffect(chewTick) {
        if (chewTick > 0) {
            chewing = true
            delay(450)
            chewing = false
        }
    }
    val numberHint = attempts >= 1

    GameColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EmojiGrid(q.a, "🍎", size = 26.sp, label = { i -> if (numberHint) "${i + 1}" else null })
            FunText("+", 34.sp, ctx.color, Modifier.padding(horizontal = 8.dp))
            EmojiGrid(q.b, "🍏", size = 26.sp, label = { i -> if (numberHint) "${q.a + i + 1}" else null })
        }
        FunText("${q.a} + ${q.b}", 34.sp, ctx.color)

        Row(verticalAlignment = Alignment.CenterVertically) {
            LookPip(
                ctx.look,
                Modifier.size(130.dp),
                mood = if (chewing) PipMood.CHEW else PipMood.WOW,
            )
            Spacer(Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FunText("Pip ate", 16.sp, NumColors.InkSoft)
                FunText("$fed", 56.sp, ctx.color)
                FunText("🍎".repeat(minOf(fed, 10)), 14.sp)
            }
        }

        BouncyButton("🌳  Drop an apple!", NumColors.Grass, Modifier.fillMaxWidth(), height = 70.dp) {
            if (fed < 25) {
                fed++
                chewTick++
                ctx.sfx(Sfx.CHOMP)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BouncyButton("↩ Undo", Color.White, Modifier.weight(1f), textColor = NumColors.Ink) {
                if (fed > 0) fed--
                ctx.sfx(Sfx.TAP)
            }
            BouncyButton("✅ Done!", ctx.color, Modifier.weight(1f)) {
                when {
                    fed == q.answer -> onAnswer(true, "Burp! Exactly ${q.answer} apples. Yum-tastic!")
                    fed > q.answer -> {
                        onAnswer(false, "Too many! Pip's tummy went BLOOP. Let's start again.")
                        fed = 0
                    }
                    else -> onAnswer(false, "Pip's tummy is still rumbling! Feed him more.")
                }
            }
        }
    }
}

private enum class BalloonState { FLOATING, DEFLATED, POPPED }

private val balloonColors = listOf(
    Color(0xFFFF4D6D), Color(0xFF4FC3F7), Color(0xFFFFC93C), Color(0xFF7ED957), Color(0xFFB388FF),
)

/** Pop the balloon with the right answer. */
@Composable
fun BalloonPopGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val order = remember { q.choices.shuffled() }
    val states = remember { mutableStateListOf<BalloonState>().apply { repeat(order.size) { add(BalloonState.FLOATING) } } }
    val glow = pulseScale(attempts >= 2)
    val showObjects = ctx.beginner && q.topic == Topic.ADD && q.answer <= 10 && (attempts >= 1 || ctx.age <= 5 || ctx.guided)

    GameColumn {
        FunText("${q.equation} = ?", if (q.result >= 100) 40.sp else 48.sp, ctx.color)
        if (showObjects) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiGrid(q.a, "🍓", size = 22.sp)
                FunText("+", 26.sp, ctx.color, Modifier.padding(horizontal = 6.dp))
                EmojiGrid(q.b, "🍓", size = 22.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            order.forEachIndexed { i, value ->
                Balloon(
                    value = value,
                    color = balloonColors[i % balloonColors.size],
                    index = i,
                    state = states[i],
                    scale = if (attempts >= 2 && value == q.answer) glow else 1f,
                ) {
                    if (states[i] == BalloonState.FLOATING) {
                        if (value == q.answer) {
                            states[i] = BalloonState.POPPED
                            ctx.sfx(Sfx.POP)
                            onAnswer(true, "POP! ${q.equation} = ${q.answer}!")
                        } else {
                            states[i] = BalloonState.DEFLATED
                            onAnswer(false, null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Balloon(
    value: Int,
    color: Color,
    index: Int,
    state: BalloonState,
    scale: Float,
    onTap: () -> Unit,
) {
    val inf = rememberInfiniteTransition(label = "balloon$index")
    val bob by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100 + index * 230, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob",
    )
    val popScale by animateFloatAsState(
        when (state) {
            BalloonState.POPPED -> 1.7f
            BalloonState.DEFLATED -> 0.72f
            BalloonState.FLOATING -> 1f
        },
        label = "popScale",
    )
    val popAlpha by animateFloatAsState(if (state == BalloonState.POPPED) 0f else 1f, tween(350), label = "popAlpha")
    val body = if (state == BalloonState.DEFLATED) NumColors.Locked else color
    val interaction = remember { MutableInteractionSource() }

    Box(contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .graphicsLayer {
                    translationY = -bob * 16.dp.toPx()
                    scaleX = popScale * scale
                    scaleY = popScale * scale
                    alpha = popAlpha
                }
                .clickable(interactionSource = interaction, indication = null, onClick = onTap),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(width = 78.dp, height = 96.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    drawOval(body, Offset(0f, 0f), Size(w, h * 0.92f))
                    drawOval(body.darken(0.25f), Offset(0f, 0f), Size(w, h * 0.92f), style = Stroke(width = w * 0.03f))
                    drawOval(Color.White.copy(alpha = 0.45f), Offset(w * 0.18f, h * 0.12f), Size(w * 0.22f, h * 0.2f))
                    val knot = Path().apply {
                        moveTo(w * 0.5f, h * 0.9f)
                        lineTo(w * 0.42f, h)
                        lineTo(w * 0.58f, h)
                        close()
                    }
                    drawPath(knot, body.darken(0.25f))
                }
                FunText("$value", if (value >= 100) 24.sp else 32.sp, Color.White, Modifier.padding(bottom = 8.dp))
            }
            Canvas(Modifier.size(width = 16.dp, height = 46.dp)) {
                val p = Path().apply {
                    moveTo(size.width / 2f, 0f)
                    cubicTo(0f, size.height * 0.3f, size.width, size.height * 0.6f, size.width / 2f, size.height)
                }
                drawPath(p, NumColors.InkSoft, style = Stroke(width = 3f))
            }
        }
        if (state == BalloonState.POPPED) {
            FunText("POP!", 30.sp, NumColors.Bad)
        }
    }
}

/* ===================================================================== SUBTRACTION */

/** Froggy hops back on a number line. Tap the lily pad where he lands. */
@Composable
fun FrogHopGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    // Show 11 lily pads around the hop (0..10 for small numbers).
    val lo = max(0, minOf(q.a, q.answer) - (10 - q.b) / 2).let { if (it + 10 < q.a) q.a - 10 else it }
    val hi = lo + 10
    var frogAt by remember { mutableIntStateOf(q.a) }
    var busy by remember { mutableStateOf(false) }
    val wrongPads = remember { mutableStateListOf<Int>() }
    val scope = rememberCoroutineScope()
    val glow = pulseScale(attempts >= 2)

    GameColumn {
        FunText("${q.a} − ${q.b} = ?", 46.sp, ctx.color)
        FunText("Hop back ${q.b}:  " + "🐾".repeat(q.b), 20.sp, NumColors.InkSoft)
        Spacer(Modifier.height(6.dp))

        Row(Modifier.fillMaxWidth()) {
            for (n in lo..hi) {
                val isWrong = n in wrongPads
                val isGlow = attempts >= 2 && n == q.answer
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(if (frogAt == n) "🐸" else " ", fontSize = 22.sp)
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
                                    else -> NumColors.Lily
                                }
                            )
                            .clickable(enabled = !busy) {
                                if (n == q.answer) {
                                    busy = true
                                    scope.launch {
                                        while (frogAt > q.answer) {
                                            delay(330)
                                            frogAt--
                                            ctx.sfx(Sfx.HOP)
                                        }
                                        delay(200)
                                        onAnswer(true, "Ribbit! Froggy landed on ${q.answer}!")
                                    }
                                } else {
                                    if (n !in wrongPads) wrongPads.add(n)
                                    onAnswer(false, "Ribbit? Froggy won't land on $n. Count the hops back!")
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        FunText("$n", 13.sp, Color.White)
                    }
                }
            }
        }

        if (attempts >= 1) {
            BouncyButton("👀  Show me the hops", Color.White, Modifier.fillMaxWidth(), textColor = NumColors.Ink, enabled = !busy) {
                busy = true
                scope.launch {
                    frogAt = q.a
                    for (k in 1..q.b) {
                        delay(450)
                        frogAt = q.a - k
                        ctx.sfx(Sfx.HOP)
                        ctx.say("$k")
                    }
                    delay(1200)
                    frogAt = q.a
                    busy = false
                    ctx.say("Now tap where Froggy landed!")
                }
            }
        }
    }
}

/** Squirrel steals some acorns; how many are left? */
@Composable
fun SneakySquirrelGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    var grabbed by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        delay(1400)
        repeat(q.b) {
            grabbed++
            ctx.sfx(Sfx.CHOMP)
            delay(500)
        }
    }
    val done = grabbed >= q.b
    val inf = rememberInfiniteTransition(label = "squirrel")
    val wiggle by inf.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(220), RepeatMode.Reverse),
        label = "wiggle",
    )
    val left = q.a - q.b

    GameColumn {
        Text(
            "🐿️",
            fontSize = 60.sp,
            modifier = Modifier.graphicsLayer { rotationZ = if (done) 0f else wiggle },
        )
        EmojiGrid(
            count = q.a,
            emoji = "🌰",
            size = 32.sp,
            faded = { i -> i >= q.a - grabbed },
            label = { i -> if (done && attempts >= 1 && i < left) "${i + 1}" else null },
        )
        if (done) {
            FunText("${q.a} − ${q.b} = ?", 42.sp, ctx.color)
            FunText("How many acorns are left?", 18.sp, NumColors.InkSoft)
            ChoiceRow(q.choices, q.answer, attempts, ctx, onAnswer, correctLine = "Yes! ${q.answer} acorns left. Nice counting!")
        } else {
            FunText(if (grabbed == 0) "Uh-oh... here comes Sneaky Squirrel!" else "Grab! Grab!", 20.sp, NumColors.InkSoft)
        }
    }
}

/* ===================================================================== MULTIPLICATION */

/** Fill each plate with the same number of cookies, then count them all. */
@Composable
fun CookiePlatesGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val groups = q.a
    val per = q.b
    val filled = remember { mutableStateListOf<Int>().apply { repeat(groups) { add(0) } } }
    val allFull = filled.all { it >= per }

    GameColumn {
        FunText(
            if (!allFull) "Put $per cookies on each plate!" else "$groups plates of $per cookies",
            22.sp,
            ctx.color,
        )
        (0 until groups).chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { g ->
                    val full = filled[g] >= per
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(98.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(4.dp, if (full) NumColors.Good else ctx.color.lighten(0.4f), CircleShape)
                                .clickable(enabled = !full) {
                                    filled[g] = filled[g] + 1
                                    ctx.sfx(if (filled[g] >= per) Sfx.POP else Sfx.TAP)
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (filled[g] == 0) {
                                FunText("tap!", 16.sp, NumColors.InkSoft)
                            } else {
                                EmojiGrid(filled[g], "🍪", perRow = 3, size = 18.sp)
                            }
                        }
                        val sub = if (allFull && attempts >= 1) "= ${(g + 1) * per}" else "${filled[g]}/$per"
                        FunText(sub, 15.sp, if (full) NumColors.Good else NumColors.InkSoft)
                    }
                }
            }
        }
        if (allFull) {
            FunText(List(groups) { "$per" }.joinToString(" + "), 22.sp, NumColors.InkSoft)
            FunText("$groups × $per = ?", 40.sp, ctx.color)
            ChoiceRow(q.choices, q.answer, attempts, ctx, onAnswer, correctLine = "Cookie-tastic! $groups times $per is ${q.answer}!")
        }
    }
}

/** Skip-counting: find the missing number, then watch the kangaroo bounce. */
@Composable
fun KangarooJumpsGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    var solved by remember { mutableStateOf(false) }
    var roo by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val showSteps = attempts >= 1 || ctx.age <= 6

    GameColumn {
        FunText("Count by ${q.step}s!", 30.sp, ctx.color)
        Row(Modifier.fillMaxWidth()) {
            q.sequence.forEachIndexed { i, v ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (roo == i) "🦘" else " ", fontSize = 30.sp)
                    val missing = i == q.missingIndex && !solved
                    Box(
                        Modifier
                            .padding(3.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (missing) NumColors.Sun else ctx.color),
                        contentAlignment = Alignment.Center,
                    ) {
                        FunText(if (missing) "?" else "$v", 24.sp, Color.White)
                    }
                    if (showSteps && i < q.sequence.lastIndex) {
                        FunText("+${q.step} ➜", 12.sp, NumColors.InkSoft)
                    } else {
                        FunText(" ", 12.sp)
                    }
                }
            }
        }
        FunText("Which number goes in the ? box", 18.sp, NumColors.InkSoft, weight = FontWeight.Bold)
        ChoiceRow(
            q.choices, q.answer, attempts, ctx, onAnswer,
            correctLine = "Boing! It's ${q.answer}!",
            onCorrect = {
                solved = true
                scope.launch {
                    for (i in 1..q.sequence.lastIndex) {
                        delay(280)
                        roo = i
                        ctx.sfx(Sfx.HOP)
                    }
                }
            },
        )
    }
}

/* ===================================================================== DIVISION */

private val friendFaces = listOf("🐻", "🐰", "🐼", "🐨", "🦊")

/** Deal pizza slices one at a time to friends, round and round. */
@Composable
fun PizzaShareGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val total = q.a
    val friends = q.b
    val got = remember { mutableStateListOf<Int>().apply { repeat(friends) { add(0) } } }
    var dealt by remember { mutableIntStateOf(0) }
    val left = total - dealt
    val glowFriend = pulseScale(attempts >= 2)

    GameColumn {
        FunText("$total slices for $friends friends", 22.sp, ctx.color)
        Box(Modifier.heightIn(min = 50.dp), contentAlignment = Alignment.Center) {
            if (left > 0) {
                EmojiGrid(left, "🍕", size = 30.sp, perRow = 6)
            } else {
                FunText("All shared! 🎉", 24.sp, NumColors.Good)
            }
        }
        BouncyButton("🍕  Deal a slice!", ctx.color, Modifier.fillMaxWidth(), enabled = left > 0) {
            val f = dealt % friends
            got[f] = got[f] + 1
            dealt++
            ctx.sfx(Sfx.POP)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            for (f in 0 until friends) {
                Column(
                    Modifier.graphicsLayer {
                        val s = if (f == 0 && left == 0) glowFriend else 1f
                        scaleX = s
                        scaleY = s
                    },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(friendFaces[f % friendFaces.size], fontSize = 38.sp)
                    Box(
                        Modifier
                            .size(width = 72.dp, height = 64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(3.dp, ctx.color.lighten(0.4f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmojiGrid(got[f], "🍕", perRow = 3, size = 15.sp)
                    }
                    FunText("${got[f]}", 18.sp, ctx.color)
                }
            }
        }
        if (left == 0) {
            FunText("$total ÷ $friends = ?", 40.sp, ctx.color)
            FunText("How many slices does each friend get?", 17.sp, NumColors.InkSoft)
            ChoiceRow(q.choices, q.answer, attempts, ctx, onAnswer, correctLine = "Fair and square! Everyone gets ${q.answer}!")
        }
    }
}

/** Pack eggs into boxes; how many boxes did we fill? */
@Composable
fun PackBoxesGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val total = q.a
    val per = q.b
    var boxes by remember { mutableIntStateOf(0) }
    val left = total - boxes * per

    GameColumn {
        FunText("$total eggs. $per fit in each box.", 22.sp, ctx.color)
        Box(Modifier.heightIn(min = 50.dp), contentAlignment = Alignment.Center) {
            if (left > 0) {
                EmojiGrid(left, "🥚", size = 28.sp, perRow = 6)
            } else {
                FunText("All packed! 🎉", 24.sp, NumColors.Good)
            }
        }
        BouncyButton("📦  Pack a box!", ctx.color, Modifier.fillMaxWidth(), enabled = left >= per) {
            boxes++
            ctx.sfx(Sfx.POP)
        }
        if (boxes > 0) {
            EmojiGrid(
                count = boxes,
                emoji = "📦",
                size = 34.sp,
                perRow = 5,
                label = { i -> if (left == 0 && attempts >= 1) "box ${i + 1}" else "$per 🥚" },
            )
        }
        if (left == 0) {
            FunText("$total ÷ $per = ?", 40.sp, ctx.color)
            FunText("How many boxes did we fill?", 17.sp, NumColors.InkSoft)
            ChoiceRow(q.choices, q.answer, attempts, ctx, onAnswer, correctLine = "Egg-cellent! ${q.answer} boxes!")
        }
    }
}

/* ===================================================================== QUIZ */

/** Plain questions with picture help when needed. Also used for lesson practice. */
@Composable
fun QuizGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val showVisual = ctx.beginner && (ctx.guided || attempts >= 1 || (ctx.age <= 5 && q.answer <= 10))

    GameColumn {
        FunText("${q.equation} = ?", if (q.result >= 100 || q.a >= 100) 42.sp else 54.sp, ctx.color)
        if (showVisual) {
            when (q.topic) {
                Topic.ADD -> if (q.a + q.b <= 20) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EmojiGrid(q.a, "🍓", size = 22.sp)
                        FunText("+", 26.sp, ctx.color, Modifier.padding(horizontal = 6.dp))
                        EmojiGrid(q.b, "🫐", size = 22.sp)
                    }
                }
                Topic.SUB -> if (q.a <= 20) {
                    EmojiGrid(q.a, "🍪", size = 26.sp, faded = { i -> i >= q.a - q.b })
                }
                Topic.MUL -> if (q.answer <= 30) {
                    GroupsVisual(q.a, q.b, "⭐", ctx.color, itemSize = 16.sp)
                }
                Topic.DIV -> if (q.a <= 30) {
                    ShareVisual(q.a, q.b, "🍓", ctx.color)
                }
            }
        }
        ChoiceRow(q.choices, q.answer, attempts, ctx, onAnswer)
    }
}

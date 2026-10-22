package com.numbloo.mathfun.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numbloo.mathfun.audio.Sfx
import com.numbloo.mathfun.logic.Question
import com.numbloo.mathfun.ui.components.BouncyButton
import com.numbloo.mathfun.ui.components.EmojiGrid
import com.numbloo.mathfun.ui.components.FunText
import com.numbloo.mathfun.ui.components.LookPip
import com.numbloo.mathfun.ui.components.PipMood
import com.numbloo.mathfun.ui.components.pulseScale
import com.numbloo.mathfun.ui.screens.AnswerFn
import com.numbloo.mathfun.ui.screens.GameCtx
import com.numbloo.mathfun.ui.theme.NumColors
import com.numbloo.mathfun.ui.theme.lighten
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/** Big "6 + 3 = ?" header that shrinks a little for 3-digit numbers. */
@Composable
private fun EquationHeader(q: Question, color: Color) {
    val big = q.a >= 100 || q.result >= 100
    FunText("${q.equation} = ?", if (big) 34.sp else 42.sp, color)
}

/* ===================================================================== ADDITION: Fruit Basket */

private val fruitEmoji = listOf("🍎", "🍊", "🍐", "🍉", "🍇", "🍓", "🍌", "🍑")

/** Six fruit tiles: ids 0 and 1 are the question's numbers, the rest are helpers. */
private fun basketTiles(q: Question): List<Pair<Int, Int>> {
    val t = q.result
    val values = mutableListOf(q.a, q.b)
    var guard = 0
    while (values.size < 6 && guard < 200) {
        guard++
        val c = if (t <= 20) {
            Random.nextInt(1, max(2, t))
        } else {
            when (Random.nextInt(4)) {
                0 -> Random.nextInt(1, 10)
                1 -> Random.nextInt(1, max(2, t / 10)) * 10
                2 -> q.a + Random.nextInt(-5, 6)
                else -> q.b + Random.nextInt(-5, 6)
            }
        }
        if (c in 1 until t) values.add(c)
    }
    while (values.size < 6) values.add(1)
    return values.mapIndexed { i, v -> i to v }.shuffled()
}

@Composable
private fun FruitTile(emoji: String, value: Int, color: Color) {
    Column(
        Modifier
            .size(width = 78.dp, height = 66.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(3.dp, color.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(emoji, fontSize = 22.sp)
        FunText("$value", if (value >= 100) 17.sp else 20.sp, NumColors.Ink)
    }
}

/** Drag fruit into the basket until it holds exactly the target number. */
@Composable
fun FillBasketGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val target = q.result
    val tiles = remember { basketTiles(q) }
    val inBasket = remember { mutableStateListOf<Int>() }
    val basket = remember { Holder(Rect.Zero) }
    var done by remember { mutableStateOf(false) }
    val glow = pulseScale(attempts >= 2 && !done)

    fun valueOf(id: Int): Int = tiles.first { it.first == id }.second
    val total = inBasket.sumOf { valueOf(it) }

    fun dropped(id: Int): Boolean {
        if (done) return false
        inBasket.add(id)
        ctx.sfx(Sfx.POP)
        val sum = inBasket.sumOf { valueOf(it) }
        if (sum == target) {
            done = true
            onAnswer(true, "Yum! " + inBasket.joinToString(" + ") { "${valueOf(it)}" } + " = $target!")
        } else if (sum > target) {
            inBasket.removeAt(inBasket.lastIndex)
            onAnswer(false, "Too heavy! That makes $sum, more than $target. The basket tipped over!")
        }
        return true
    }

    PlayColumn {
        FunText("Make exactly $target!", 30.sp, ctx.color)
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .dropZone(basket)
                .clip(RoundedCornerShape(28.dp))
                .background(if (done) NumColors.Good.copy(alpha = 0.2f) else ctx.color.copy(alpha = 0.12f))
                .border(3.dp, ctx.color.copy(alpha = 0.5f), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🧺", fontSize = 64.sp)
                Row(horizontalArrangement = Arrangement.Center) {
                    inBasket.toList().forEach { id ->
                        Box(
                            Modifier
                                .padding(3.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .clickable(enabled = !done) {
                                    inBasket.remove(id)
                                    ctx.sfx(Sfx.TAP)
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            FunText(fruitEmoji[id % fruitEmoji.size] + valueOf(id), 16.sp, NumColors.Ink)
                        }
                    }
                }
                FunText("$total / $target", 28.sp, if (done) NumColors.Good else ctx.color)
                if (attempts >= 1 && !done && total < target) {
                    FunText("${target - total} more to go!", 16.sp, NumColors.InkSoft)
                }
            }
        }
        FunText(
            "Drag fruit into the basket. Tap fruit in the basket to take it out.",
            13.sp,
            NumColors.InkSoft,
            weight = FontWeight.Bold,
        )
        tiles.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { (id, value) ->
                    key(id) {
                        if (id in inBasket) {
                            Box(Modifier.size(width = 78.dp, height = 66.dp))
                        } else {
                            val isGlow = attempts >= 2 && (id == 0 || id == 1) && !done
                            DragItem(
                                modifier = Modifier.graphicsLayer {
                                    val s = if (isGlow) glow else 1f
                                    scaleX = s
                                    scaleY = s
                                },
                                enabled = !done,
                                onDrop = { p -> if (basket.v.contains(p)) dropped(id) else false },
                            ) {
                                FruitTile(fruitEmoji[id % fruitEmoji.size], value, ctx.color)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

/* ===================================================================== ADDITION: Star Catcher */

private class Faller(val value: Int, var x: Float, var y: Float, val speed: Float)

/** Stars with numbers fall from the sky. Slide Pip under the right one. */
@Composable
fun StarCatcherGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val answer by rememberUpdatedState(onAnswer)
    val tries by rememberUpdatedState(attempts)
    val stars = remember { ArrayList<Faller>() }
    var frame by remember { mutableLongStateOf(0L) }
    var catcherX by remember { mutableFloatStateOf(0.5f) }
    var done by remember { mutableStateOf(false) }
    val wrongs = remember { q.choices.filter { it != q.answer } }
    val baseSpeed = when {
        ctx.age <= 5 -> 0.12f
        ctx.beginner -> 0.15f
        else -> 0.18f
    }

    LaunchedEffect(Unit) {
        var spawn = 0.3f
        var sinceCorrect = 0
        gameLoop { dt ->
            if (!done) {
                spawn -= dt
                if (spawn <= 0f && stars.size < 4) {
                    spawn = 1.4f
                    val v = if (sinceCorrect >= 2 || Random.nextInt(3) == 0) q.answer else wrongs.random()
                    sinceCorrect = if (v == q.answer) 0 else sinceCorrect + 1
                    var x = 0.12f + Random.nextFloat() * 0.76f
                    val prev = stars.lastOrNull()
                    if (prev != null && abs(prev.x - x) < 0.22f) {
                        x = if (prev.x > 0.5f) prev.x - 0.35f else prev.x + 0.35f
                    }
                    val slow = if (tries >= 2 && v == q.answer) 0.7f else 1f
                    stars.add(Faller(v, x.coerceIn(0.1f, 0.9f), -0.08f, baseSpeed * slow * (0.9f + Random.nextFloat() * 0.3f)))
                }
                val iter = stars.iterator()
                while (iter.hasNext()) {
                    val s = iter.next()
                    s.y += s.speed * dt
                    val caught = !done && s.y in 0.74f..0.9f && abs(s.x - catcherX) < 0.14f
                    if (caught) {
                        iter.remove()
                        if (s.value == q.answer) {
                            done = true
                            ctx.sfx(Sfx.POP)
                            answer(true, "Caught it! ${q.equation} = ${q.answer}!")
                        } else {
                            answer(false, "Oops, that star was ${s.value}. Catch the right one!")
                        }
                    } else if (s.y > 1.05f) {
                        iter.remove()
                    }
                }
            }
            frame++
        }
    }

    PlayColumn {
        EquationHeader(q, ctx.color)
        FunText("Drag Pip to catch the right star!", 15.sp, NumColors.InkSoft, weight = FontWeight.Bold)
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF1B2A5C), Color(0xFF3B4FA0))))
                .pointerInput(Unit) {
                    detectTapGestures { p -> catcherX = (p.x / size.width).coerceIn(0.1f, 0.9f) }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, amount ->
                        change.consume()
                        catcherX = (catcherX + amount.x / size.width).coerceIn(0.1f, 0.9f)
                    }
                },
        ) {
            val w = maxWidth
            val h = maxHeight
            if (frame >= 0L) {
                for (k in 0 until 14) {
                    Text(
                        "✦",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.offset(x = w * ((k * 37 % 100) / 100f), y = h * ((k * 61 % 90) / 100f)),
                    )
                }
                stars.forEach { s ->
                    val big = tries >= 2 && s.value == q.answer
                    Box(
                        Modifier
                            .offset(x = w * s.x - 34.dp, y = h * s.y - 34.dp)
                            .size(68.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("⭐", fontSize = if (big) 60.sp else 52.sp)
                        FunText("${s.value}", if (s.value >= 100) 14.sp else 19.sp, NumColors.Ink)
                    }
                }
            }
            Box(
                Modifier
                    .offset(x = w * catcherX - 46.dp, y = h * 0.83f - 46.dp)
                    .size(92.dp),
            ) {
                LookPip(ctx.look, Modifier.fillMaxSize(), mood = if (done) PipMood.HAPPY else PipMood.WOW)
            }
        }
    }
}

/* ===================================================================== SUBTRACTION: Mole Whack */

private class Hole {
    var value: Int? = null
    var timeLeft = 0f
    var bonked = false
}

/** Moles pop up holding numbers. Bonk the one with the answer. */
@Composable
fun MoleWhackGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val answer by rememberUpdatedState(onAnswer)
    val tries by rememberUpdatedState(attempts)
    val holes = remember { List(9) { Hole() } }
    var frame by remember { mutableLongStateOf(0L) }
    var done by remember { mutableStateOf(false) }
    val wrongs = remember { q.choices.filter { it != q.answer } }
    val stay = when {
        ctx.age <= 5 -> 2.8f
        ctx.beginner -> 2.3f
        else -> 2.0f
    }

    LaunchedEffect(Unit) {
        var spawn = 0.4f
        var sinceCorrect = 0
        gameLoop { dt ->
            if (!done) {
                for (h in holes) {
                    if (h.value != null) {
                        h.timeLeft -= dt
                        if (h.timeLeft <= 0f) {
                            h.value = null
                            h.bonked = false
                        }
                    }
                }
                spawn -= dt
                val up = holes.count { it.value != null }
                if (spawn <= 0f && up < 3) {
                    spawn = 0.85f
                    val free = holes.filter { it.value == null }
                    if (free.isNotEmpty()) {
                        val correctUp = holes.any { it.value == q.answer && !it.bonked }
                        val v = if (!correctUp && (sinceCorrect >= 2 || Random.nextInt(3) == 0)) q.answer else wrongs.random()
                        sinceCorrect = if (v == q.answer) 0 else sinceCorrect + 1
                        val h = free.random()
                        h.value = v
                        h.bonked = false
                        h.timeLeft = stay * (if (tries >= 2 && v == q.answer) 1.6f else 1f)
                    }
                }
            }
            frame++
        }
    }

    fun whack(h: Hole) {
        val v = h.value ?: return
        if (done || h.bonked) return
        h.bonked = true
        h.timeLeft = 0.6f
        ctx.sfx(Sfx.POP)
        if (v == q.answer) {
            done = true
            answer(true, "Bonk! ${q.equation} = $v!")
        } else {
            answer(false, "Ouch! That mole had $v. Whack the right one!")
        }
    }

    PlayColumn {
        EquationHeader(q, ctx.color)
        FunText("Whack the mole holding the answer!", 15.sp, NumColors.InkSoft, weight = FontWeight.Bold)
        if (frame >= 0L) {
            Column(
                Modifier
                    .weight(1f)
                    .aspectRatio(0.9f, matchHeightConstraintsFirst = true)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF8BC34A)),
            ) {
                for (r in 0 until 3) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) {
                        for (c in 0 until 3) {
                            val h = holes[r * 3 + c]
                            MoleHole(
                                value = h.value,
                                bonked = h.bonked,
                                hint = tries >= 2 && h.value == q.answer && !h.bonked && !done,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize(),
                            ) { whack(h) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoleHole(value: Int?, bonked: Boolean, hint: Boolean, modifier: Modifier, onWhack: () -> Unit) {
    val rise by animateFloatAsState(if (value != null) 1f else 0f, tween(180), label = "rise")
    val glow = pulseScale(hint)
    Box(
        modifier
            .padding(4.dp)
            .clickable(enabled = value != null && !bonked, onClick = onWhack),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val hh = size.height
            drawOval(Color(0xFF8D6E63), Offset(w * 0.04f, hh * 0.66f), Size(w * 0.92f, hh * 0.3f))
            drawOval(Color(0xFF3E2723), Offset(w * 0.14f, hh * 0.71f), Size(w * 0.72f, hh * 0.19f))
            if (rise > 0.02f) {
                val top = hh * 0.80f - rise * hh * 0.5f
                clipRect(bottom = hh * 0.81f) {
                    drawRoundRect(
                        Color(0xFF9C6B4E),
                        Offset(w * 0.24f, top),
                        Size(w * 0.52f, hh * 0.6f),
                        CornerRadius(w * 0.26f, w * 0.26f),
                    )
                    val ey = top + hh * 0.14f
                    val ink = NumColors.Ink
                    if (bonked) {
                        for (ex in listOf(w * 0.4f, w * 0.6f)) {
                            val d = w * 0.04f
                            drawLine(ink, Offset(ex - d, ey - d), Offset(ex + d, ey + d), strokeWidth = w * 0.025f)
                            drawLine(ink, Offset(ex - d, ey + d), Offset(ex + d, ey - d), strokeWidth = w * 0.025f)
                        }
                    } else {
                        drawCircle(ink, w * 0.035f, Offset(w * 0.4f, ey))
                        drawCircle(ink, w * 0.035f, Offset(w * 0.6f, ey))
                    }
                    drawCircle(Color(0xFFFF8FA3), w * 0.055f, Offset(w * 0.5f, top + hh * 0.23f))
                    drawRect(Color.White, Offset(w * 0.47f, top + hh * 0.28f), Size(w * 0.06f, hh * 0.05f))
                }
            }
        }
        if (value != null) {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        alpha = rise
                        scaleX = glow
                        scaleY = glow
                    }
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (bonked) NumColors.Locked else Color.White)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                FunText("$value", if (value >= 100) 16.sp else 20.sp, NumColors.Ink)
            }
        }
        if (bonked) FunText("💥", 26.sp)
    }
}

/* ===================================================================== SUBTRACTION: Mail Sorter */

private class Letter(val id: Int, val text: String, val answer: Int)

/** Three letters with different answers, and the mailboxes for them. */
private fun mailSet(q: Question): Pair<List<Letter>, List<Int>> {
    val answers = ArrayList<Int>()
    answers.add(q.answer)
    q.choices.filter { it != q.answer && it > 0 }.shuffled().forEach { if (answers.size < 3) answers.add(it) }
    var extra = q.answer + 2
    while (answers.size < 3) {
        if (extra !in answers) answers.add(extra)
        extra++
    }
    val letters = answers.mapIndexed { i, ans ->
        if (i == 0) {
            Letter(0, q.equation, ans)
        } else {
            val n = max(1, q.b + Random.nextInt(-2, 3))
            Letter(i, "${ans + n} − $n", ans)
        }
    }
    return letters.shuffled() to answers.sorted()
}

/** Drag each letter into the mailbox with its answer. */
@Composable
fun MailboxGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val setup = remember { mailSet(q) }
    val letters = setup.first
    val boxes = setup.second
    val zones = remember { boxes.associateWith { Holder(Rect.Zero) } }
    val delivered = remember { mutableStateListOf<Int>() }
    var holding by remember { mutableStateOf<Int?>(null) }
    var done by remember { mutableStateOf(false) }
    val glow = pulseScale(attempts >= 2 && holding != null)

    fun drop(letter: Letter, p: Offset): Boolean {
        holding = null
        if (done) return false
        val box = zones.entries.firstOrNull { it.value.v.contains(p) }?.key ?: return false
        return if (box == letter.answer) {
            delivered.add(letter.id)
            ctx.sfx(Sfx.POP)
            if (delivered.size == letters.size) {
                done = true
                onAnswer(true, "All letters delivered! ${q.equation} = ${q.answer}!")
            }
            true
        } else {
            onAnswer(false, "${letter.text} isn't $box. Try another mailbox!")
            false
        }
    }

    PlayColumn {
        FunText("Post each letter in the right mailbox!", 20.sp, ctx.color)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            boxes.forEach { ans ->
                val count = letters.count { it.answer == ans && it.id in delivered }
                val hint = attempts >= 2 && holding == ans
                Column(
                    Modifier
                        .dropZone(zones.getValue(ans))
                        .graphicsLayer {
                            val s = if (hint) glow else 1f
                            scaleX = s
                            scaleY = s
                        }
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (hint) NumColors.Sun.copy(alpha = 0.45f) else Color.White)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(ctx.color),
                        contentAlignment = Alignment.Center,
                    ) {
                        FunText("$ans", if (ans >= 100) 15.sp else 21.sp, Color.White)
                    }
                    Text(if (count > 0) "📬" else "📪", fontSize = 48.sp)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        if (attempts >= 1 && !done) {
            FunText("Work out each letter, then find its number!", 14.sp, NumColors.InkSoft, weight = FontWeight.Bold)
        }
        letters.forEach { l ->
            key(l.id) {
                if (l.id in delivered) {
                    Box(Modifier.height(60.dp))
                } else {
                    DragItem(
                        enabled = !done,
                        onDragStart = { holding = l.answer },
                        onDrop = { p -> drop(l, p) },
                    ) {
                        Row(
                            Modifier
                                .width(230.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFFF8E1))
                                .border(2.dp, Color(0xFFE0C080), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text("✉️", fontSize = 26.sp)
                            FunText(l.text, 22.sp, NumColors.Ink)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}

/* ===================================================================== MULTIPLICATION: Tile Painter */

/** Drag across a grid to paint a rectangle with exactly the target number of tiles. */
@Composable
fun TilePainterGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val target = q.result
    val n = max(6, max(q.a, q.b)).coerceAtMost(10)
    val answer by rememberUpdatedState(onAnswer)
    var start by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var end by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var done by remember { mutableStateOf(false) }

    val s0 = start
    val e0 = end
    val rows = if (s0 != null && e0 != null) abs(s0.first - e0.first) + 1 else 0
    val cols = if (s0 != null && e0 != null) abs(s0.second - e0.second) + 1 else 0

    PlayColumn {
        FunText("Paint $target tiles in a rectangle!", 24.sp, ctx.color)
        FunText(
            if (rows > 0) "$rows × $cols = ${rows * cols}" else "Drag your finger across the grid",
            if (rows > 0) 30.sp else 16.sp,
            if (rows > 0 && rows * cols == target) NumColors.Good else NumColors.Ink,
        )
        if (!done && attempts == 1) {
            FunText("Think: what times what makes $target?", 15.sp, NumColors.InkSoft, weight = FontWeight.Bold)
        }
        if (!done && attempts >= 2) {
            FunText("Try ${q.a} rows of ${q.b}!", 15.sp, NumColors.InkSoft, weight = FontWeight.Bold)
        }
        Canvas(
            Modifier
                .weight(1f)
                .aspectRatio(1f, matchHeightConstraintsFirst = true)
                .clip(RoundedCornerShape(16.dp))
                .background(ctx.color.copy(alpha = 0.15f))
                .pointerInput(Unit) {
                    fun cellAt(p: Offset): Pair<Int, Int> {
                        val c = (p.x / (size.width / n.toFloat())).toInt().coerceIn(0, n - 1)
                        val r = (p.y / (size.height / n.toFloat())).toInt().coerceIn(0, n - 1)
                        return r to c
                    }
                    detectDragGestures(
                        onDragStart = { p ->
                            if (!done) {
                                start = cellAt(p)
                                end = cellAt(p)
                            }
                        },
                        onDragEnd = {
                            val s = start
                            val e = end
                            if (!done && s != null && e != null) {
                                val r = abs(s.first - e.first) + 1
                                val c = abs(s.second - e.second) + 1
                                if (r * c == target) {
                                    done = true
                                    ctx.sfx(Sfx.POP)
                                    answer(true, "$r rows of $c! $r × $c = $target!")
                                } else {
                                    answer(false, "That's $r × $c = ${r * c} tiles. We need $target!")
                                    start = null
                                    end = null
                                }
                            }
                        },
                        onDragCancel = {
                            if (!done) {
                                start = null
                                end = null
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            if (!done) end = cellAt(change.position)
                        },
                    )
                },
        ) {
            val cell = size.width / n
            val s = start
            val e = end
            val r0 = if (s != null && e != null) min(s.first, e.first) else -1
            val r1 = if (s != null && e != null) max(s.first, e.first) else -2
            val c0 = if (s != null && e != null) min(s.second, e.second) else -1
            val c1 = if (s != null && e != null) max(s.second, e.second) else -2
            for (r in 0 until n) {
                for (c in 0 until n) {
                    val painted = r in r0..r1 && c in c0..c1
                    val hint = attempts >= 2 && !done && r < q.a && c < q.b
                    drawRoundRect(
                        color = when {
                            painted && done -> NumColors.Good
                            painted -> ctx.color
                            hint -> NumColors.Sun.copy(alpha = 0.45f)
                            else -> Color.White
                        },
                        topLeft = Offset(c * cell + cell * 0.06f, r * cell + cell * 0.06f),
                        size = Size(cell * 0.88f, cell * 0.88f),
                        cornerRadius = CornerRadius(cell * 0.18f, cell * 0.18f),
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

/* ===================================================================== MULTIPLICATION: Go Fishing */

private class Fish(val value: Int, val lane: Int, var x: Float, val speed: Float, val dir: Float, val emoji: String)

/** Fish swim past with numbers. Tap the one with the answer to hook it. */
@Composable
fun GoFishingGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val answer by rememberUpdatedState(onAnswer)
    val tries by rememberUpdatedState(attempts)
    val base = when {
        ctx.age <= 5 -> 0.07f
        ctx.beginner -> 0.09f
        else -> 0.11f
    }
    val fish = remember {
        val emojis = listOf("🐟", "🐠", "🐡", "🐟")
        q.choices.shuffled().mapIndexed { i, v ->
            Fish(
                value = v,
                lane = i,
                x = Random.nextFloat(),
                speed = base * (0.8f + Random.nextFloat() * 0.5f),
                dir = if (i % 2 == 0) 1f else -1f,
                emoji = emojis[i % emojis.size],
            )
        }
    }
    var frame by remember { mutableLongStateOf(0L) }
    var hooked by remember { mutableStateOf<Fish?>(null) }
    var done by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        gameLoop { dt ->
            if (!done) {
                for (f in fish) {
                    if (f !== hooked) {
                        val slow = if (tries >= 2 && f.value == q.answer) 0.5f else 1f
                        f.x += f.dir * f.speed * slow * dt
                        if (f.x > 1.12f) f.x = -0.12f
                        if (f.x < -0.12f) f.x = 1.12f
                    }
                }
            }
            frame++
        }
    }

    fun cast(f: Fish) {
        if (done || hooked != null) return
        hooked = f
        ctx.sfx(Sfx.WHOOSH)
        scope.launch {
            delay(500)
            if (f.value == q.answer) {
                done = true
                answer(true, "What a catch! ${q.equation} = ${f.value}!")
            } else {
                answer(false, "That fish says ${f.value}. Splash! Back it goes.")
                delay(300)
                hooked = null
            }
        }
    }

    val laneTop = 0.3f
    val laneGap = (0.94f - laneTop) / fish.size

    PlayColumn {
        EquationHeader(q, ctx.color)
        FunText("Tap the fish with the answer!", 15.sp, NumColors.InkSoft, weight = FontWeight.Bold)
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        0f to Color(0xFFBDE7FF),
                        0.22f to Color(0xFFBDE7FF),
                        0.23f to Color(0xFF4FA3D9),
                        1f to Color(0xFF1B4F72),
                    )
                ),
        ) {
            val w = maxWidth
            val h = maxHeight
            if (frame >= 0L) {
                val hk = hooked
                Canvas(Modifier.fillMaxSize()) {
                    if (hk != null) {
                        val fx = size.width * hk.x
                        val fy = size.height * (laneTop + laneGap * (hk.lane + 0.5f))
                        drawLine(
                            Color.White,
                            Offset(size.width * 0.5f + 30f, size.height * 0.12f),
                            Offset(fx, fy),
                            strokeWidth = 4f,
                        )
                    }
                }
                Box(
                    Modifier
                        .offset(x = w * 0.5f - 45.dp, y = 0.dp)
                        .size(90.dp),
                ) {
                    LookPip(
                        ctx.look,
                        Modifier
                            .size(56.dp)
                            .align(Alignment.TopCenter),
                        mood = if (done) PipMood.HAPPY else PipMood.THINK,
                    )
                    Text("🛶", fontSize = 44.sp, modifier = Modifier.align(Alignment.BottomCenter))
                }
                fish.forEach { f ->
                    val y = h * (laneTop + laneGap * (f.lane + 0.5f))
                    val glowing = tries >= 2 && f.value == q.answer && !done
                    Box(
                        Modifier
                            .offset(x = w * f.x - 44.dp, y = y - 30.dp)
                            .size(width = 88.dp, height = 60.dp)
                            .clickable { cast(f) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            f.emoji,
                            fontSize = 42.sp,
                            modifier = Modifier.graphicsLayer { scaleX = if (f.dir > 0f) -1f else 1f },
                        )
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (glowing) NumColors.Sun else Color.White)
                                .padding(horizontal = 6.dp, vertical = 1.dp),
                        ) {
                            FunText("${f.value}", if (f.value >= 100) 13.sp else 17.sp, NumColors.Ink)
                        }
                    }
                }
            }
        }
    }
}

/* ===================================================================== DIVISION: Cookie Share */

/** Drag cookies onto friends' plates. When the jar is empty, everyone must have the same. */
@Composable
fun FairShareGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val total = q.a
    val friends = q.b
    val plates = remember { mutableStateListOf<Int>().apply { repeat(friends) { add(0) } } }
    val zones = remember { List(friends) { Holder(Rect.Zero) } }
    var done by remember { mutableStateOf(false) }
    val given = plates.sum()
    val left = total - given
    val faces = listOf("🐻", "🐰", "🐼", "🐨", "🦊")

    fun give(p: Offset): Boolean {
        if (done) return false
        val i = zones.indexOfFirst { it.v.contains(p) }
        if (i < 0) return false
        plates[i] = plates[i] + 1
        ctx.sfx(Sfx.POP)
        if (plates.sum() == total) {
            if (plates.all { it == plates[0] }) {
                done = true
                onAnswer(true, "Fair and square! Everyone gets ${plates[0]}. $total ÷ $friends = ${q.answer}!")
            } else {
                onAnswer(false, "Uh-oh, that's not fair! Tap a plate to take a cookie back.")
            }
        }
        return true
    }

    PlayColumn {
        FunText("Share $total cookies with $friends friends!", 22.sp, ctx.color)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            for (i in 0 until friends) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(faces[i % faces.size], fontSize = 36.sp)
                    Box(
                        Modifier
                            .dropZone(zones[i])
                            .size(width = 74.dp, height = 90.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (done) NumColors.Good.copy(alpha = 0.2f) else Color.White)
                            .border(3.dp, ctx.color.lighten(0.4f), RoundedCornerShape(18.dp))
                            .clickable(enabled = !done && plates[i] > 0) {
                                plates[i] = plates[i] - 1
                                ctx.sfx(Sfx.TAP)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        EmojiGrid(plates[i], "🍪", perRow = 3, size = 15.sp)
                    }
                    FunText("${plates[i]}", 20.sp, ctx.color)
                }
            }
        }
        if (!done && attempts >= 1) {
            FunText("Give one cookie to each friend, then go round again!", 14.sp, NumColors.InkSoft, weight = FontWeight.Bold)
        }
        Spacer(Modifier.weight(1f))
        if (left > 0) {
            if (left > 1) EmojiGrid(left - 1, "🍪", size = 20.sp, perRow = 8)
            key(given) {
                DragItem(enabled = !done, onDrop = { p -> give(p) }) {
                    Box(
                        Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(NumColors.Sun.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("🍪", fontSize = 48.sp)
                    }
                }
            }
            FunText("Drag a cookie to a plate • $left left", 14.sp, NumColors.InkSoft, weight = FontWeight.Bold)
        } else if (!done) {
            FunText("All cookies are on plates. Tap a plate to take one back.", 15.sp, NumColors.InkSoft, weight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
    }
}

/* ===================================================================== DIVISION: Lane Racer */

private fun gateValues(q: Question): List<Int> =
    (q.choices.filter { it != q.answer }.shuffled().take(2) + q.answer).shuffled()

/** Number gates race towards the kart. Steer into the lane with the answer. */
@Composable
fun LaneRacerGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val answer by rememberUpdatedState(onAnswer)
    val tries by rememberUpdatedState(attempts)
    var lane by remember { mutableIntStateOf(1) }
    var gateY by remember { mutableFloatStateOf(-0.12f) }
    var gate by remember { mutableStateOf(gateValues(q)) }
    var crashed by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }
    var road by remember { mutableFloatStateOf(0f) }
    val laneAnim by animateFloatAsState(lane.toFloat(), tween(160), label = "lane")
    val speed = when {
        ctx.age <= 5 -> 0.10f
        ctx.beginner -> 0.13f
        else -> 0.16f
    }
    val kartY = 0.8f

    LaunchedEffect(Unit) {
        gameLoop { dt ->
            if (!done) road = (road + dt * 1.2f) % 1f
            if (!done && !crashed) {
                val slow = if (tries >= 2) 0.6f else 1f
                gateY += speed * slow * dt
                if (gateY >= kartY - 0.06f) {
                    val v = gate[lane]
                    if (v == q.answer) {
                        done = true
                        answer(true, "Vroom! ${q.equation} = $v!")
                    } else {
                        crashed = true
                        answer(false, "Crash! $v isn't right. Pick another lane!")
                        launch {
                            delay(1100)
                            gate = gateValues(q)
                            gateY = -0.12f
                            crashed = false
                        }
                    }
                }
            }
        }
    }

    PlayColumn {
        EquationHeader(q, ctx.color)
        FunText("Tap a lane to steer into the answer!", 15.sp, NumColors.InkSoft, weight = FontWeight.Bold)
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF4A4A5A))
                .pointerInput(Unit) {
                    detectTapGestures { p ->
                        if (!done) lane = (p.x / (size.width / 3f)).toInt().coerceIn(0, 2)
                    }
                },
        ) {
            val w = maxWidth
            val h = maxHeight
            val laneW = w / 3
            Canvas(Modifier.fillMaxSize()) {
                val dash = size.height / 8f
                for (k in 1..2) {
                    val x = size.width * k / 3f
                    var y = -2f * dash + road * 2f * dash
                    while (y < size.height) {
                        drawLine(Color.White.copy(alpha = 0.7f), Offset(x, y), Offset(x, y + dash), strokeWidth = 6f)
                        y += dash * 2f
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .offset(y = h * gateY - 28.dp),
            ) {
                gate.forEachIndexed { i, v ->
                    Box(
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    done && v == q.answer -> NumColors.Good
                                    crashed && i == lane -> NumColors.Bad
                                    else -> NumColors.Sun
                                }
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        FunText("$v", if (v >= 100) 20.sp else 26.sp, NumColors.Ink)
                    }
                }
            }
            Box(
                Modifier
                    .offset(x = laneW * laneAnim + laneW / 2 - 36.dp, y = h * kartY - 36.dp)
                    .size(72.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (crashed) "💥" else "🏎️",
                    fontSize = 48.sp,
                    modifier = Modifier.graphicsLayer { rotationZ = if (crashed) 0f else 90f },
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BouncyButton("⬅️", Color.White, Modifier.weight(1f), textColor = NumColors.Ink, height = 56.dp) {
                if (!done) lane = (lane - 1).coerceAtLeast(0)
            }
            BouncyButton("➡️", Color.White, Modifier.weight(1f), textColor = NumColors.Ink, height = 56.dp) {
                if (!done) lane = (lane + 1).coerceAtMost(2)
            }
        }
    }
}

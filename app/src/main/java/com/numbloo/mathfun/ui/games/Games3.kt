package com.numbloo.mathfun.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numbloo.mathfun.audio.Sfx
import com.numbloo.mathfun.logic.Question
import com.numbloo.mathfun.model.Glasses
import com.numbloo.mathfun.model.Topic
import com.numbloo.mathfun.ui.components.BouncyBox
import com.numbloo.mathfun.ui.components.BouncyButton
import com.numbloo.mathfun.ui.components.FunText
import com.numbloo.mathfun.ui.components.LookPip
import com.numbloo.mathfun.ui.components.PipMood
import com.numbloo.mathfun.ui.components.pulseScale
import com.numbloo.mathfun.ui.screens.AnswerFn
import com.numbloo.mathfun.ui.screens.GameCtx
import com.numbloo.mathfun.ui.theme.NumColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/* ===================================================================== SNAP (pair cards) */

/** Do these two cards make the target with this operation? Order doesn't matter. */
private fun isPair(x: Int, y: Int, topic: Topic, target: Int): Boolean = when (topic) {
    Topic.ADD -> x + y == target
    Topic.SUB -> abs(x - y) == target
    Topic.MUL -> x * y == target
    Topic.DIV -> {
        val big = max(x, y)
        val small = min(x, y)
        small > 0 && big % small == 0 && big / small == target
    }
}

private fun pairText(x: Int, y: Int, topic: Topic): String = when (topic) {
    Topic.ADD -> "$x + $y"
    Topic.SUB -> "${max(x, y)} − ${min(x, y)}"
    Topic.MUL -> "$x × $y"
    Topic.DIV -> "${max(x, y)} ÷ ${min(x, y)}"
}

/** What two wrong cards actually make, said kindly. */
private fun pairResultLine(x: Int, y: Int, topic: Topic, target: Int): String {
    val text = pairText(x, y, topic)
    return when (topic) {
        Topic.ADD -> "$text makes ${x + y}, not $target. Try again!"
        Topic.SUB -> "$text makes ${abs(x - y)}, not $target. Try again!"
        Topic.MUL -> "$text makes ${x * y}, not $target. Try again!"
        Topic.DIV -> {
            val big = max(x, y)
            val small = min(x, y)
            if (small > 0 && big % small == 0) "$text makes ${big / small}, not $target. Try again!"
            else "$text doesn't share fairly. Try again!"
        }
    }
}

/** Six cards: the two that make the answer, plus four look-alikes. */
private fun snapCards(q: Question): List<Int> {
    val out = mutableListOf(q.a, q.b)
    val span = max(3, max(q.a, q.b) / 3)
    var guard = 0
    while (out.size < 6 && guard < 500) {
        guard++
        val base = if (Random.nextBoolean()) q.a else q.b
        val c = base + Random.nextInt(-span, span + 1)
        if (c <= 0 || c in out) continue
        out.add(c)
    }
    var extra = (out.maxOrNull() ?: 10) + 1
    while (out.size < 6) out.add(extra++)
    return out.shuffled()
}

/** Find two cards that make the target number. Themed per operation. */
@Composable
fun SnapGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val topic = q.topic
    val target = q.result
    val cards = remember { snapCards(q) }
    val pairA = remember(cards) { cards.indexOf(q.a) }
    val pairB = remember(cards) { cards.indices.first { cards[it] == q.b && it != pairA } }
    val selected = remember { mutableStateListOf<Int>() }
    var solved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val glow = pulseScale(attempts >= 2)

    val (title, cardEmoji) = when (topic) {
        Topic.ADD -> "Find two cards that ADD up to" to "🃏"
        Topic.SUB -> "Find two cards with a GAP of" to "🔍"
        Topic.MUL -> "Find two cards that MULTIPLY to" to "👯"
        Topic.DIV -> "Find big ÷ small that makes" to "🍰"
    }

    fun tap(i: Int) {
        if (solved || selected.size >= 2) return
        if (i in selected) {
            selected.remove(i)
            return
        }
        selected.add(i)
        ctx.sfx(Sfx.TAP)
        if (selected.size == 2) {
            val x = cards[selected[0]]
            val y = cards[selected[1]]
            scope.launch {
                delay(350)
                if (isPair(x, y, topic, target)) {
                    solved = true
                    onAnswer(true, "Snap! ${pairText(x, y, topic)} = $target!")
                } else {
                    onAnswer(false, pairResultLine(x, y, topic, target))
                    delay(500)
                    selected.clear()
                }
            }
        }
    }

    GameColumn {
        FunText("$cardEmoji  $title", 20.sp, NumColors.InkSoft)
        Box(
            Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(NumColors.Sun)
                .border(5.dp, Color(0xFFE0A800), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            FunText("$target", if (target >= 100) 38.sp else 48.sp, NumColors.Ink)
        }
        val picked = selected.map { cards[it] }
        FunText(
            when (picked.size) {
                0 -> "Tap a card!"
                1 -> "${picked[0]} ${topic.symbol} ?"
                else -> pairText(picked[0], picked[1], topic)
            },
            26.sp,
            ctx.color,
        )
        cards.indices.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { i ->
                    val isSel = i in selected
                    val isGlow = attempts >= 2 && (i == pairA || i == pairB) && !solved
                    BouncyBox(
                        color = when {
                            isSel && solved -> NumColors.Good
                            isSel -> NumColors.Ink
                            else -> Color.White
                        },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.15f)
                            .graphicsLayer {
                                val s = if (isGlow) glow else 1f
                                scaleX = s
                                scaleY = s
                            },
                        onClick = { tap(i) },
                    ) {
                        FunText(
                            "${cards[i]}",
                            if (cards[i] >= 100) 26.sp else 32.sp,
                            if (isSel) Color.White else NumColors.Ink,
                        )
                    }
                }
            }
        }
        if (attempts >= 1 && !solved) {
            val hint = when (topic) {
                Topic.ADD -> "Pick one card, then look for $target minus that card."
                Topic.SUB -> "Pick a big card, then look for one that is $target smaller."
                Topic.MUL -> "Pick a card, then think: card × ? = $target"
                Topic.DIV -> "Pick a small card, then look for small × $target."
            }
            FunText(hint, 16.sp, NumColors.InkSoft, weight = FontWeight.Bold)
        }
    }
}

/* ===================================================================== SILLY PIP (spot the mistake) */

/** Professor Pip writes an answer. Sometimes he's right, sometimes he's being silly. */
@Composable
fun SillyPipGame(q: Question, attempts: Int, ctx: GameCtx, onAnswer: AnswerFn) {
    val pipRight = remember { Random.nextBoolean() }
    val shown = remember { if (pipRight) q.answer else q.choices.filter { it != q.answer }.random() }
    var phase by remember { mutableIntStateOf(0) } // 0 = judge, 1 = fix Pip's mistake
    var mood by remember { mutableStateOf(PipMood.THINK) }

    fun judge(saysRight: Boolean) {
        if (phase != 0) return
        if (saysRight == pipRight) {
            if (pipRight) {
                mood = PipMood.HAPPY
                onAnswer(true, "Yes! Pip got it right this time. ${q.equation} = ${q.answer}!")
            } else {
                phase = 1
                mood = PipMood.OOPS
                ctx.sfx(Sfx.POP)
                ctx.say("You caught me! Silly Pip! What's the real answer?")
            }
        } else {
            mood = PipMood.WOW
            onAnswer(
                false,
                if (pipRight) "Hmm, check again. Pip was actually right!" else "Look closer. Is ${q.equation} really $shown?",
            )
        }
    }

    GameColumn {
        LookPip(ctx.look.copy(glasses = Glasses.SPECS), Modifier.size(100.dp), mood = mood)
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF2E5E4E))
                .border(6.dp, Color(0xFF8D6E63), RoundedCornerShape(20.dp))
                .padding(vertical = 22.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val big = q.result >= 100 || q.a >= 100
                FunText("${q.equation} = ", if (big) 32.sp else 40.sp, Color.White)
                Box(contentAlignment = Alignment.Center) {
                    FunText("$shown", if (big) 32.sp else 40.sp, if (phase == 1) Color(0xFFFF8A80) else Color(0xFFFFF59D))
                    if (phase == 1) FunText("✕", if (big) 40.sp else 48.sp, Color(0xFFFF5252))
                }
            }
        }
        if (phase == 0) {
            FunText("Is Professor Pip right?", 22.sp, ctx.color)
            if (attempts >= 1) {
                FunText("Work it out yourself first: ${q.equation} = ?", 16.sp, NumColors.InkSoft, weight = FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BouncyButton("👍 Right!", NumColors.Good, Modifier.weight(1f), height = 76.dp, fontSize = 24.sp) {
                    judge(true)
                }
                BouncyButton("🤪 Silly!", NumColors.Bad, Modifier.weight(1f), height = 76.dp, fontSize = 24.sp) {
                    judge(false)
                }
            }
        } else {
            FunText("Silly Pip! What's the real answer?", 20.sp, ctx.color)
            ChoiceRow(
                q.choices, q.answer, attempts, ctx, onAnswer,
                correctLine = "Thanks for fixing my mistake! ${q.equation} = ${q.answer}!",
                onCorrect = { mood = PipMood.HAPPY },
            )
        }
    }
}

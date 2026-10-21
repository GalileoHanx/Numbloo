package com.numbloo.mathfun.logic

import com.numbloo.mathfun.model.GameType
import com.numbloo.mathfun.model.Topic
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * One math question.
 * - ADD/SUB: a (+/−) b
 * - MUL: a groups of b
 * - DIV: a shared into b equal parts (answer = a / b)
 * - Skip counting: [sequence] with one [missingIndex], counting by [step]
 */
data class Question(
    val topic: Topic,
    val a: Int,
    val b: Int,
    val answer: Int,
    val choices: List<Int>,
    val sequence: List<Int> = emptyList(),
    val missingIndex: Int = -1,
    val step: Int = 0,
) {
    val equation: String get() = "$a ${topic.symbol} $b"
    val spokenEquation: String get() = "$a ${topic.spokenOp} $b"
}

/**
 * Makes questions that fit the child's age and current difficulty tier.
 * Tier 1 = tiny numbers (confidence!), tier 2 = up to 10, tier 3 = up to 20.
 */
object QuestionGen {

    private val rnd = Random.Default

    fun make(game: GameType, topic: Topic, tier: Int, age: Int): Question {
        val t = tier.coerceIn(1, 3)
        val n = if (age <= 5) 3 else 4 // number of answer buttons
        return when (game) {
            GameType.FEED_PIP -> add(t, n, maxSum = 10)
            GameType.BALLOON_POP -> add(t, n, maxSum = 20)
            GameType.FROG_HOP -> sub(t, n, maxStart = 10)
            GameType.SNEAKY_SQUIRREL -> sub(t, n, maxStart = 15)
            GameType.COOKIE_PLATES -> mul(t, n, maxGroups = 5, maxPer = 5)
            GameType.KANGAROO_JUMPS -> skip(t, n)
            GameType.PIZZA_SHARE -> div(t, n, maxDivisor = 4, maxTotal = 12)
            GameType.PACK_BOXES -> div(t, n, maxDivisor = 5, maxTotal = 15)
            GameType.DOMINO_DOTS -> domino(t, n)
            GameType.SNAIL_RACE -> add(t, n, maxSum = 10)
            GameType.MYSTERY_BOX -> hideB(add(t, n, maxSum = 10), n)
            GameType.BUBBLE_POP -> sub(t, n, maxStart = 12)
            GameType.TOWER_COMPARE -> sub(t, n, maxStart = 10)
            GameType.MAGIC_HAT -> hideB(sub(t, n, maxStart = 10), n)
            GameType.FLOWER_GARDEN -> mul(t, n, maxGroups = 5, maxPer = 5)
            GameType.LADYBUG_SPOTS -> mul(t, n, maxGroups = 5, maxPer = 5)
            GameType.TIMES_MACHINE -> mul(t, n, maxGroups = 5, maxPer = 10)
            GameType.MONSTER_MUNCH -> div(t, n, maxDivisor = 5, maxTotal = 15)
            GameType.CUPCAKE_TRAYS -> div(t, n, maxDivisor = 5, maxTotal = 20)
            GameType.DIVIDE_MACHINE -> div(t, n, maxDivisor = 5, maxTotal = 30)
            GameType.QUIZ -> when (topic) {
                Topic.ADD -> add(t, n, maxSum = 20)
                Topic.SUB -> sub(t, n, maxStart = 20)
                Topic.MUL -> mul(t, n, maxGroups = 5, maxPer = 10)
                Topic.DIV -> div(t, n, maxDivisor = 5, maxTotal = 50)
            }
        }
    }

    /** A fresh question that is not the same as [previous]. */
    fun makeDifferent(game: GameType, topic: Topic, tier: Int, age: Int, previous: Question?): Question {
        var q = make(game, topic, tier, age)
        var tries = 0
        while (previous != null && tries < 8 && q.a == previous.a && q.b == previous.b && q.answer == previous.answer) {
            q = make(game, topic, tier, age)
            tries++
        }
        return q
    }

    /** Same numbers, but the hidden one is b ("3 + ? = 7", "7 − ? = 4"). */
    private fun hideB(q: Question, n: Int): Question {
        val shown = if (q.topic == Topic.ADD) q.a + q.b else q.answer
        return q.copy(
            answer = q.b,
            choices = choices(q.b, n, listOf(q.b + 1, q.b - 1, q.b + 2, q.b - 2, q.a, shown)),
        )
    }

    /** Two domino halves, each 1..6 dots. */
    private fun domino(t: Int, n: Int): Question {
        val (lo, hi) = when (t) {
            1 -> 2 to 5
            2 -> 4 to 9
            else -> 7 to 12
        }
        var a = 1
        var b = 1
        var tries = 0
        do {
            a = rnd.nextInt(1, 7)
            b = rnd.nextInt(1, 7)
            tries++
        } while ((a + b < lo || a + b > hi) && tries < 200)
        val total = a + b
        return Question(
            Topic.ADD, a, b, total,
            choices(total, n, listOf(total + 1, total - 1, total + 2, total - 2, max(a, b))),
        )
    }

    private fun add(t: Int, n: Int, maxSum: Int): Question {
        val (lo, hi) = when (t) {
            1 -> 2 to 5
            2 -> 4 to 10
            else -> 9 to 20
        }
        val top = max(lo, min(hi, maxSum))
        val total = rnd.nextInt(lo, top + 1)
        val a = rnd.nextInt(1, total) // 1..total-1
        val b = total - a
        return Question(
            Topic.ADD, a, b, total,
            choices(total, n, listOf(total + 1, total - 1, total + 2, total - 2, max(a, b), total + 10)),
        )
    }

    private fun sub(t: Int, n: Int, maxStart: Int): Question {
        val (lo, hi) = when (t) {
            1 -> 3 to 5
            2 -> 5 to 10
            else -> 10 to 20
        }
        val top = max(lo, min(hi, maxStart))
        val a = rnd.nextInt(lo, top + 1)
        val b = rnd.nextInt(1, a) // 1..a-1, never a negative answer
        val ans = a - b
        return Question(
            Topic.SUB, a, b, ans,
            choices(ans, n, listOf(ans + 1, ans - 1, ans + 2, a + b, b, ans - 2)),
        )
    }

    private fun mul(t: Int, n: Int, maxGroups: Int, maxPer: Int): Question {
        val groups = when (t) {
            1 -> rnd.nextInt(2, 4)
            else -> rnd.nextInt(2, 6)
        }.coerceAtMost(maxGroups)
        val per = when (t) {
            1 -> rnd.nextInt(1, 4)
            2 -> listOf(2, 5, 10).filter { it <= maxPer }.random(rnd)
            else -> rnd.nextInt(2, maxPer + 1)
        }
        val ans = groups * per
        return Question(
            Topic.MUL, groups, per, ans,
            choices(ans, n, listOf(ans + per, ans - per, groups + per, ans + 1, ans - 1)),
        )
    }

    private fun div(t: Int, n: Int, maxDivisor: Int, maxTotal: Int): Question {
        var d = 2
        var q = 1
        var tries = 0
        do {
            d = when (t) {
                1 -> 2
                2 -> rnd.nextInt(2, 4)
                else -> rnd.nextInt(2, maxDivisor + 1)
            }.coerceAtMost(maxDivisor)
            q = when (t) {
                1 -> rnd.nextInt(1, 4)
                2 -> rnd.nextInt(1, 6)
                else -> rnd.nextInt(2, 11)
            }
            tries++
        } while (d * q > maxTotal && tries < 50)
        if (d * q > maxTotal) {
            d = 2
            q = 2
        }
        val total = d * q
        return Question(
            Topic.DIV, total, d, q,
            choices(q, n, listOf(q + 1, q - 1, q + 2, d, total - d)),
        )
    }

    private fun skip(t: Int, n: Int): Question {
        val step = when (t) {
            1 -> listOf(1, 2).random(rnd)
            2 -> listOf(2, 5, 10).random(rnd)
            else -> listOf(3, 4, 5, 10).random(rnd)
        }
        val start = step * rnd.nextInt(0, if (t == 1) 2 else 4)
        val seq = List(5) { start + it * step }
        val missing = rnd.nextInt(1, 5)
        val ans = seq[missing]
        return Question(
            Topic.MUL, step, 0, ans,
            choices(ans, n, listOf(ans + 1, ans - 1, ans + step, ans - step, ans + 2)),
            sequence = seq, missingIndex = missing, step = step,
        )
    }

    /** The answer plus believable wrong answers, sorted small to big. */
    private fun choices(answer: Int, n: Int, candidates: List<Int>): List<Int> {
        val set = LinkedHashSet<Int>()
        set.add(answer)
        for (c in candidates.shuffled(rnd)) {
            if (set.size >= n) break
            if (c >= 0 && c != answer) set.add(c)
        }
        var spread = 1
        while (set.size < n) {
            val c = answer + if (rnd.nextBoolean()) spread else -spread
            if (c >= 0) set.add(c)
            spread++
        }
        return set.toList().sorted()
    }
}

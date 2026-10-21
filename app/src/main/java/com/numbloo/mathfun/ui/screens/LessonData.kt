package com.numbloo.mathfun.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numbloo.mathfun.model.Level
import com.numbloo.mathfun.model.Topic
import com.numbloo.mathfun.ui.components.ArrayVisual
import com.numbloo.mathfun.ui.components.BigEquation
import com.numbloo.mathfun.ui.components.ColumnMath
import com.numbloo.mathfun.ui.components.FunText
import com.numbloo.mathfun.ui.components.Gap
import com.numbloo.mathfun.ui.components.GroupsVisual
import com.numbloo.mathfun.ui.components.HopLine
import com.numbloo.mathfun.ui.components.MiniNumberLine
import com.numbloo.mathfun.ui.components.PipMood
import com.numbloo.mathfun.ui.components.PoppingItems
import com.numbloo.mathfun.ui.components.ShareVisual
import com.numbloo.mathfun.ui.components.TenFrame
import com.numbloo.mathfun.ui.components.TensOnes
import com.numbloo.mathfun.ui.theme.NumColors

/** One step of a lesson: what Pip says + a picture. */
class LessonCard(
    val text: String,
    val mood: PipMood = PipMood.HAPPY,
    val visual: @Composable (Color) -> Unit,
)

/** A short lesson. Show it, name it, give a trick, then practise. */
class LessonDef(
    val id: String,
    val title: String,
    val emoji: String,
    val cards: List<LessonCard>,
)

/** Several equations stacked. */
@Composable
private fun Eqs(color: Color, vararg lines: String, size: TextUnit = 34.sp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        lines.forEach { line ->
            BigEquation(line, color, size)
            Gap(6.dp)
        }
    }
}

private fun turn(): LessonCard = LessonCard("Your turn! Let's try 3 together.", PipMood.WOW) {
    Text("🙌", fontSize = 90.sp)
}

private fun lesson(id: String, title: String, emoji: String, vararg cards: LessonCard): LessonDef =
    LessonDef(id, title, emoji, cards.toList() + turn())

/** Three lessons for every operation at every level (36 in all). */
object Lessons {

    fun forTopic(topic: Topic, level: Level): List<LessonDef> = when (level) {
        Level.BEGINNER -> beginner(topic)
        Level.INTERMEDIATE -> intermediate(topic)
        Level.PRO -> pro(topic)
    }

    /* ------------------------------------------------------------ BEGINNER */

    private fun beginner(topic: Topic): List<LessonDef> = when (topic) {
        Topic.ADD -> listOf(
            lesson(
                "1", "Putting together", "🍎",
                LessonCard("Hi! I'm Pip and I LOVE snacks. Adding means putting things together!") { c -> BigEquation("+", c, 120.sp) },
                LessonCard("Look! Pip has 2 apples.") { PoppingItems(2, "🍎", size = 52.sp) },
                LessonCard("Then a friend brings 3 more apples. Yum!", PipMood.WOW) { c ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PoppingItems(2, "🍎", size = 40.sp)
                        FunText(" + ", 40.sp, c)
                        PoppingItems(3, "🍎", startDelay = 400, size = 40.sp)
                    }
                },
                LessonCard("Put them together and count: 1, 2, 3, 4, 5. Five apples!") { PoppingItems(5, "🍎", size = 44.sp, numbered = true) },
                LessonCard("We write it like this: 2 plus 3 equals 5. The plus sign means put together!") { c -> BigEquation("2 + 3 = 5", c) },
            ),
            lesson(
                "2", "Counting on", "🐌",
                LessonCard("Here's a speedy trick called counting on! Keep the big number in your head.") { c -> BigEquation("5 + 3", c) },
                LessonCard("Think 5. Then count 3 more: 6, 7, 8!", PipMood.WOW) { c -> HopLine(5, 1, 3, c) },
                LessonCard("So 5 + 3 = 8. No need to count all the apples from 1!") { c -> BigEquation("5 + 3 = 8", c) },
                LessonCard("Try 2 + 6. Turn it around and start with 6: 7, 8. That's 8!") { c ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BigEquation("2 + 6 = 6 + 2", c, 34.sp)
                        Gap(10.dp)
                        HopLine(6, 1, 2, c)
                    }
                },
            ),
            lesson(
                "3", "Ten friends", "🔟",
                LessonCard("Ten is Pip's favourite number. Some numbers are best friends that make 10!") { c -> BigEquation("10", c, 110.sp) },
                LessonCard("7 and 3 make 10. See? They fill the whole ten frame!", PipMood.WOW) { c -> TenFrame(7, 3, c) },
                LessonCard("6 and 4 make 10 too!") { c -> TenFrame(6, 4, c) },
                LessonCard("Ten friends: 1 and 9, 2 and 8, 3 and 7, 4 and 6, 5 and 5. Learn them and you'll be super fast!") { c ->
                    Eqs(c, "1 + 9", "2 + 8", "3 + 7", "4 + 6", "5 + 5", size = 26.sp)
                },
            ),
        )
        Topic.SUB -> listOf(
            lesson(
                "1", "Taking away", "🍪",
                LessonCard("Subtracting means taking some away. Pip has 5 cookies.") { PoppingItems(5, "🍪", size = 46.sp) },
                LessonCard("Chomp chomp! Pip eats 2 of them. Oops!", PipMood.CHEW) { PoppingItems(5, "🍪", size = 46.sp, crossFrom = 3) },
                LessonCard("How many are left? Count them: 1, 2, 3. Three cookies left!") { PoppingItems(3, "🍪", size = 46.sp, numbered = true) },
                LessonCard("We write 5 minus 2 equals 3. The minus sign means take away.") { c -> BigEquation("5 − 2 = 3", c) },
            ),
            lesson(
                "2", "Counting back", "🐸",
                LessonCard("Froggy's trick: start at the big number and hop back!") { c -> BigEquation("7 − 3", c) },
                LessonCard("Start at 7. Hop back 3: 6, 5, 4. Froggy lands on 4!", PipMood.WOW) { c -> MiniNumberLine(7, 3, c) },
                LessonCard("So 7 − 3 = 4.") { c -> BigEquation("7 − 3 = 4", c) },
                LessonCard("Try 6 − 2. Hop back: 5, 4. That's 4!") { c -> MiniNumberLine(6, 2, c) },
            ),
            lesson(
                "3", "How many more?", "🍓",
                LessonCard("Subtracting can also find the difference: how many MORE?") { c -> BigEquation("−", c, 120.sp) },
                LessonCard("Pip has 5 strawberries. Gloop has 3 grapes. Line them up!") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PoppingItems(5, "🍓", size = 40.sp)
                        PoppingItems(3, "🍇", size = 40.sp, startDelay = 600)
                    }
                },
                LessonCard("Pip has 2 extra! So 5 − 3 = 2.", PipMood.WOW) { c -> BigEquation("5 − 3 = 2", c) },
                LessonCard("Counting up works too: from 3 to 5 is 2 hops!") { c -> HopLine(3, 1, 2, c) },
            ),
        )
        Topic.MUL -> listOf(
            lesson(
                "1", "Equal groups", "🍪",
                LessonCard("Multiplying is a super speedy way to add equal groups!") { c -> BigEquation("×", c, 120.sp) },
                LessonCard("Here are 3 plates. Each plate has 2 cookies.") { c -> GroupsVisual(3, 2, "🍪", c) },
                LessonCard("Count by twos: 2, 4, 6. That's 6 cookies!", PipMood.WOW) { c -> GroupsVisual(3, 2, "🍪", c, runningTotals = true) },
                LessonCard("2 plus 2 plus 2 makes 6. That's the same as 3 times 2!") { c -> Eqs(c, "2 + 2 + 2 = 6", "3 × 2 = 6") },
                LessonCard("The times sign means groups of. So 3 times 2 means 3 groups of 2.") { c ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BigEquation("3 × 2", c)
                        FunText("3 groups of 2", 24.sp, NumColors.InkSoft)
                    }
                },
            ),
            lesson(
                "2", "Skip counting", "🦘",
                LessonCard("Kangaroos don't count 1, 2, 3. They jump! Let's count by 2s.") { c -> HopLine(0, 2, 4, c) },
                LessonCard("Count by 5s: 5, 10, 15, 20. Like counting fingers on hands!", PipMood.WOW) { c -> HopLine(5, 5, 3, c) },
                LessonCard("Count by 10s: 10, 20, 30, 40!") { c -> HopLine(10, 10, 3, c) },
                LessonCard("Skip counting helps with times: 3 jumps of 2 is 2, 4, 6. So 3 × 2 = 6!") { c ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HopLine(2, 2, 2, c)
                        Gap(10.dp)
                        BigEquation("3 × 2 = 6", c, 36.sp)
                    }
                },
            ),
            lesson(
                "3", "Rows and columns", "🌷",
                LessonCard("When things sit in neat rows, it's called an array.") { c -> ArrayVisual(3, 4, "🌷", c) },
                LessonCard("3 rows of 4 flowers. Count by 4s: 4, 8, 12!", PipMood.WOW) { c -> ArrayVisual(3, 4, "🌷", c, rowTotals = true) },
                LessonCard("So 3 × 4 = 12.") { c -> BigEquation("3 × 4 = 12", c) },
                LessonCard("Turn the garden sideways: 4 rows of 3. Still 12! So 4 × 3 = 12 too.") { c -> ArrayVisual(4, 3, "🌷", c) },
            ),
        )
        Topic.DIV -> listOf(
            lesson(
                "1", "Sharing fairly", "🍓",
                LessonCard("Dividing means sharing fairly, so everyone gets the same amount!") { c -> BigEquation("÷", c, 120.sp) },
                LessonCard("Here are 6 strawberries and 2 hungry friends.") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PoppingItems(6, "🍓", size = 40.sp, perRow = 6)
                        Gap(10.dp)
                        Text("🐻   🐰", fontSize = 44.sp)
                    }
                },
                LessonCard("Give one to each friend, again and again, until they're all gone.", PipMood.WOW) { c -> ShareVisual(6, 2, "🍓", c) },
                LessonCard("Each friend gets 3! So 6 divided by 2 equals 3.") { c -> BigEquation("6 ÷ 2 = 3", c) },
            ),
            lesson(
                "2", "Making groups", "📦",
                LessonCard("Dividing can also mean making groups. Here are 12 eggs.") { PoppingItems(12, "🥚", size = 34.sp, perRow = 6) },
                LessonCard("Each box holds 3 eggs. Let's pack them!", PipMood.WOW) { c -> GroupsVisual(4, 3, "🥚", c) },
                LessonCard("We filled 4 boxes! So 12 ÷ 3 = 4.") { c -> BigEquation("12 ÷ 3 = 4", c) },
                LessonCard("Ask: how many 3s fit into 12? Count by 3s: 3, 6, 9, 12. Four jumps!") { c -> HopLine(3, 3, 3, c) },
            ),
            lesson(
                "3", "Undoing times", "🔁",
                LessonCard("Times and divide are a team. 3 groups of 4 make 12.") { c -> GroupsVisual(3, 4, "🍪", c) },
                LessonCard("Now share 12 back into 3 groups. Each gets 4! So 12 ÷ 3 = 4.", PipMood.WOW) { c -> ShareVisual(12, 3, "🍪", c) },
                LessonCard("If you know 3 × 4 = 12, you also know 12 ÷ 3 = 4 and 12 ÷ 4 = 3!") { c -> Eqs(c, "3 × 4 = 12", "12 ÷ 3 = 4", "12 ÷ 4 = 3") },
                LessonCard("Stuck on a divide? Think of a times fact! 5 × 2 = 10, so 10 ÷ 2 = 5.", PipMood.THINK) { c -> Eqs(c, "5 × 2 = 10", "10 ÷ 2 = 5") },
            ),
        )
    }

    /* ------------------------------------------------------------ INTERMEDIATE */

    private fun intermediate(topic: Topic): List<LessonDef> = when (topic) {
        Topic.ADD -> listOf(
            lesson(
                "1", "Tens and ones", "🧱",
                LessonCard("Big numbers are made of tens and ones. 23 is 2 tens and 3 ones!") { c -> TensOnes(23, c) },
                LessonCard("To add 23 + 4, keep the 2 tens and add the ones: 3 + 4 = 7.", PipMood.WOW) { c -> ColumnMath(23, 4, "+", 27, c) },
                LessonCard("So 23 + 4 = 27. Only the ones changed!") { c -> BigEquation("23 + 4 = 27", c) },
                LessonCard("Try 41 + 6. Ones: 1 + 6 = 7. The tens stay 4. That's 47!") { c -> TensOnes(47, c) },
            ),
            lesson(
                "2", "Adding tens", "🔟",
                LessonCard("Adding tens is easy if you know your small sums. 3 + 2 = 5.") { c -> BigEquation("3 + 2 = 5", c) },
                LessonCard("So 3 tens plus 2 tens make 5 tens. 30 + 20 = 50!", PipMood.WOW) { c -> Eqs(c, "3 + 2 = 5", "30 + 20 = 50") },
                LessonCard("Adding 10? Just add 1 to the tens. Hop by 10s: 34, 44, 54!") { c -> HopLine(34, 10, 2, c) },
                LessonCard("So 25 + 30 means add 3 tens to 25. That's 55!") { c -> BigEquation("25 + 30 = 55", c) },
            ),
            lesson(
                "3", "Bridge through 10", "🌉",
                LessonCard("Adding past 10? Build a bridge! Let's do 8 + 5.") { c -> BigEquation("8 + 5", c) },
                LessonCard("8 needs 2 more to make 10. So split 5 into 2 and 3.", PipMood.WOW) { c -> TenFrame(8, 2, c) },
                LessonCard("8 + 2 = 10, then 10 + 3 = 13. So 8 + 5 = 13!") { c -> Eqs(c, "8 + 2 = 10", "10 + 3 = 13") },
                LessonCard("Same trick for 27 + 5: 27 + 3 = 30, then 30 + 2 = 32!") { c -> Eqs(c, "27 + 3 = 30", "30 + 2 = 32") },
            ),
        )
        Topic.SUB -> listOf(
            lesson(
                "1", "Taking away tens", "🔟",
                LessonCard("You know 7 − 3 = 4. So 70 − 30 = 40! Just think in tens.") { c -> Eqs(c, "7 − 3 = 4", "70 − 30 = 40") },
                LessonCard("Taking away 10? Take 1 from the tens: 56 − 10 = 46.", PipMood.WOW) { c -> TensOnes(46, c) },
                LessonCard("And 48 − 20: 4 tens take away 2 tens is 2 tens. That's 28!") { c -> BigEquation("48 − 20 = 28", c) },
                LessonCard("The ones stay the same when you take away tens!") { c -> ColumnMath(48, 20, "−", 28, c) },
            ),
            lesson(
                "2", "Fact families", "👨‍👩‍👧",
                LessonCard("Numbers live in families! 3, 4 and 7 are one family.") { c -> BigEquation("3  •  4  •  7", c) },
                LessonCard("Adding: 3 + 4 = 7 and 4 + 3 = 7.") { c -> Eqs(c, "3 + 4 = 7", "4 + 3 = 7") },
                LessonCard("Taking away: 7 − 4 = 3 and 7 − 3 = 4!", PipMood.WOW) { c -> Eqs(c, "7 − 4 = 3", "7 − 3 = 4") },
                LessonCard("So if you know 8 + 9 = 17, you also know 17 − 9 = 8!") { c -> Eqs(c, "8 + 9 = 17", "17 − 9 = 8") },
            ),
            lesson(
                "3", "Bridge back through 10", "🌉",
                LessonCard("Let's take away across 10. Try 13 − 5.") { c -> BigEquation("13 − 5", c) },
                LessonCard("First hop back to 10: 13 − 3 = 10. We still need to take away 2 more.", PipMood.WOW) { c -> BigEquation("13 − 3 = 10", c, 40.sp) },
                LessonCard("Then 10 − 2 = 8. So 13 − 5 = 8!") { c -> Eqs(c, "10 − 2 = 8", "13 − 5 = 8") },
                LessonCard("Same for 42 − 7: 42 − 2 = 40, then 40 − 5 = 35!") { c -> Eqs(c, "42 − 2 = 40", "40 − 5 = 35") },
            ),
        )
        Topic.MUL -> listOf(
            lesson(
                "1", "Table patterns", "🔎",
                LessonCard("Times tables have secret patterns! Times 10 just adds a zero.") { c -> BigEquation("7 × 10 = 70", c) },
                LessonCard("Times 5 always ends in 0 or 5: 5, 10, 15, 20, 25.", PipMood.WOW) { c -> HopLine(5, 5, 4, c) },
                LessonCard("Times 5 is half of times 10! 6 × 10 = 60, so 6 × 5 = 30.") { c -> Eqs(c, "6 × 10 = 60", "6 × 5 = 30") },
                LessonCard("Times 9 trick: the digits add up to 9! 9 × 3 = 27, and 2 + 7 = 9.") { c -> Eqs(c, "9 × 3 = 27", "2 + 7 = 9") },
            ),
            lesson(
                "2", "Doubling", "✌️",
                LessonCard("Times 2 means double! 6 × 2 = 12.") { c -> BigEquation("6 × 2 = 12", c) },
                LessonCard("Times 4 is double, then double again: 6 × 2 = 12, 12 × 2 = 24. So 6 × 4 = 24!", PipMood.WOW) { c ->
                    Eqs(c, "6 × 2 = 12", "12 × 2 = 24", "6 × 4 = 24")
                },
                LessonCard("Times 8 is double, double, double! 3 × 8: 6, 12, 24.") { c -> BigEquation("3 × 8 = 24", c) },
                LessonCard("Doubling makes big tables feel small!") { c -> GroupsVisual(2, 4, "⭐", c) },
            ),
            lesson(
                "3", "Turn-around facts", "🔄",
                LessonCard("Multiplying works both ways: 3 × 7 is the same as 7 × 3!") { c -> ArrayVisual(3, 7, "⭐", c) },
                LessonCard("So if you know 7 × 3 = 21, you also know 3 × 7 = 21.", PipMood.WOW) { c -> Eqs(c, "7 × 3 = 21", "3 × 7 = 21") },
                LessonCard("Times 1 keeps a number the same: 8 × 1 = 8.") { c -> BigEquation("8 × 1 = 8", c) },
                LessonCard("Times 0 is always 0. Zero plates of cookies means no cookies!", PipMood.OOPS) { c -> BigEquation("9 × 0 = 0", c) },
            ),
        )
        Topic.DIV -> listOf(
            lesson(
                "1", "Use your tables", "📚",
                LessonCard("To divide, use times tables backwards. 24 ÷ 6 = ?") { c -> BigEquation("24 ÷ 6 = ?", c) },
                LessonCard("Ask: 6 times what makes 24? 6, 12, 18, 24. That's 4 jumps!", PipMood.THINK) { c -> HopLine(6, 6, 3, c) },
                LessonCard("6 × 4 = 24, so 24 ÷ 6 = 4!") { c -> Eqs(c, "6 × 4 = 24", "24 ÷ 6 = 4") },
                LessonCard("Try 35 ÷ 5: 5 × 7 = 35, so the answer is 7!") { c -> Eqs(c, "5 × 7 = 35", "35 ÷ 5 = 7") },
            ),
            lesson(
                "2", "Divide by 10 and 5", "🔟",
                LessonCard("Dividing by 10? Just take away a zero! 70 ÷ 10 = 7.") { c -> BigEquation("70 ÷ 10 = 7", c) },
                LessonCard("Why? Because 7 groups of 10 make 70.", PipMood.WOW) { c -> TensOnes(70, c) },
                LessonCard("Dividing by 5? Count by 5s to 30: 5, 10, 15, 20, 25, 30. Six jumps!") { c -> BigEquation("30 ÷ 5 = 6", c) },
                LessonCard("Or double it, then divide by 10: 30 × 2 = 60, and 60 ÷ 10 = 6!") { c -> Eqs(c, "30 × 2 = 60", "60 ÷ 10 = 6") },
            ),
            lesson(
                "3", "Halving", "✂️",
                LessonCard("Dividing by 2 means cutting in half. Half of 8 pizzas is 4!") { c -> ShareVisual(8, 2, "🍕", c) },
                LessonCard("So 8 ÷ 2 = 4, and 18 ÷ 2 = 9.") { c -> Eqs(c, "8 ÷ 2 = 4", "18 ÷ 2 = 9") },
                LessonCard("Dividing by 4? Halve it, then halve again! 20, 10, 5. So 20 ÷ 4 = 5.", PipMood.WOW) { c -> BigEquation("20 ÷ 4 = 5", c) },
                LessonCard("Half of 40 is 20 and half of 6 is 3, so half of 46 is 23!") { c -> BigEquation("46 ÷ 2 = 23", c) },
            ),
        )
    }

    /* ------------------------------------------------------------ PRO */

    private fun pro(topic: Topic): List<LessonDef> = when (topic) {
        Topic.ADD -> listOf(
            lesson(
                "1", "Two-digit adding", "📝",
                LessonCard("Adding two big numbers? Line them up: tens under tens, ones under ones.") { c -> ColumnMath(24, 35, "+", null, c) },
                LessonCard("Add the ones first: 4 + 5 = 9.") { c -> BigEquation("4 + 5 = 9", c) },
                LessonCard("Then the tens: 2 + 3 = 5 tens. So 24 + 35 = 59!", PipMood.WOW) { c -> ColumnMath(24, 35, "+", 59, c) },
                LessonCard("Check it: 25 + 35 = 60, so 59 makes sense!") { c -> Eqs(c, "24 + 35 = 59", "25 + 35 = 60") },
            ),
            lesson(
                "2", "Carrying", "🎒",
                LessonCard("What if the ones make more than 9? Let's try 27 + 15.") { c -> ColumnMath(27, 15, "+", null, c) },
                LessonCard("Ones: 7 + 5 = 12. That's 1 ten and 2 ones. Write the 2 and carry the 1 ten!", PipMood.WOW) { c ->
                    ColumnMath(27, 15, "+", null, c, note = "1 ")
                },
                LessonCard("Tens: 2 + 1 + the carried 1 makes 4. So 27 + 15 = 42!") { c -> ColumnMath(27, 15, "+", 42, c, note = "1 ") },
                LessonCard("Carrying is just trading 10 ones for 1 ten. Pip trades snacks the same way!") { c -> TensOnes(42, c) },
            ),
            lesson(
                "3", "Smart shortcuts", "🧠",
                LessonCard("Pros use shortcuts! 39 is almost 40. Let's do 39 + 25.") { c -> BigEquation("39 + 25", c) },
                LessonCard("Add 40 instead: 40 + 25 = 65. Then take back the extra 1. That's 64!", PipMood.WOW) { c -> Eqs(c, "40 + 25 = 65", "65 − 1 = 64") },
                LessonCard("Doubles are speedy too: 35 + 35 = 70, so 35 + 36 = 71!") { c -> Eqs(c, "35 + 35 = 70", "35 + 36 = 71") },
                LessonCard("Look for friendly numbers. 98 + 7? 98 + 2 = 100, then 5 more is 105!") { c -> Eqs(c, "98 + 2 = 100", "100 + 5 = 105") },
            ),
        )
        Topic.SUB -> listOf(
            lesson(
                "1", "Two-digit take-away", "📝",
                LessonCard("Line up big numbers: tens under tens, ones under ones.") { c -> ColumnMath(58, 23, "−", null, c) },
                LessonCard("Ones first: 8 − 3 = 5.") { c -> BigEquation("8 − 3 = 5", c) },
                LessonCard("Then the tens: 5 − 2 = 3. So 58 − 23 = 35!", PipMood.WOW) { c -> ColumnMath(58, 23, "−", 35, c) },
                LessonCard("Check by adding back: 35 + 23 = 58. Yes!") { c -> BigEquation("35 + 23 = 58", c) },
            ),
            lesson(
                "2", "Borrowing", "🤝",
                LessonCard("Try 42 − 17. Ones: 2 − 7? Not enough ones!", PipMood.OOPS) { c -> ColumnMath(42, 17, "−", null, c) },
                LessonCard("Borrow 1 ten from the 4 tens. Now there are 3 tens and 12 ones.", PipMood.WOW) { c -> BigEquation("42 = 30 + 12", c, 36.sp) },
                LessonCard("Ones: 12 − 7 = 5. Tens: 3 − 1 = 2. So 42 − 17 = 25!") { c -> ColumnMath(42, 17, "−", 25, c) },
                LessonCard("Check: 25 + 17 = 42. Borrowing works!") { c -> BigEquation("25 + 17 = 42", c) },
            ),
            lesson(
                "3", "Count up to subtract", "🛒",
                LessonCard("Shopkeepers count up! Try 61 − 58.") { c -> BigEquation("61 − 58", c) },
                LessonCard("The numbers are close, so count up from 58: 59, 60, 61. That's 3 hops!", PipMood.WOW) { c -> HopLine(58, 1, 3, c) },
                LessonCard("So 61 − 58 = 3. Super fast!") { c -> BigEquation("61 − 58 = 3", c) },
                LessonCard("Try 103 − 97: 97 to 100 is 3, then 3 more to 103. That's 6!") { c -> BigEquation("103 − 97 = 6", c) },
            ),
        )
        Topic.MUL -> listOf(
            lesson(
                "1", "Multiplying tens", "🔟",
                LessonCard("You know 3 × 2 = 6. So 3 × 20 = 60! Just think in tens.") { c -> Eqs(c, "3 × 2 = 6", "3 × 20 = 60") },
                LessonCard("3 groups of 2 tens make 6 tens, which is 60.", PipMood.WOW) { c -> TensOnes(60, c) },
                LessonCard("Try 4 × 30: 4 × 3 = 12, so 4 × 30 = 120!") { c -> Eqs(c, "4 × 3 = 12", "4 × 30 = 120") },
                LessonCard("Find the small fact, then add the zero: 5 × 4 = 20, so 5 × 40 = 200.") { c -> Eqs(c, "5 × 4 = 20", "5 × 40 = 200") },
            ),
            lesson(
                "2", "Split the number", "✂️",
                LessonCard("Big times? Split the big number! Let's do 14 × 3.") { c -> BigEquation("14 × 3", c) },
                LessonCard("14 is 10 and 4. Do each part: 10 × 3 = 30 and 4 × 3 = 12.", PipMood.WOW) { c -> Eqs(c, "10 × 3 = 30", "4 × 3 = 12") },
                LessonCard("Add the parts: 30 + 12 = 42. So 14 × 3 = 42!") { c -> Eqs(c, "30 + 12 = 42", "14 × 3 = 42") },
                LessonCard("Try 23 × 4: 20 × 4 = 80, 3 × 4 = 12, and 80 + 12 = 92!") { c -> BigEquation("23 × 4 = 92", c) },
            ),
            lesson(
                "3", "Nearly ten", "🎯",
                LessonCard("Times 9 is nearly times 10! Let's do 9 × 6.") { c -> BigEquation("9 × 6", c) },
                LessonCard("10 × 6 = 60. But we only want 9 sixes, so take one 6 away.", PipMood.THINK) { c -> BigEquation("10 × 6 = 60", c, 40.sp) },
                LessonCard("60 − 6 = 54. So 9 × 6 = 54!", PipMood.WOW) { c -> Eqs(c, "60 − 6 = 54", "9 × 6 = 54") },
                LessonCard("Same for 19 × 3: 20 × 3 = 60, then 60 − 3 = 57!") { c -> Eqs(c, "20 × 3 = 60", "19 × 3 = 57") },
            ),
        )
        Topic.DIV -> listOf(
            lesson(
                "1", "Dividing tens", "🔟",
                LessonCard("You know 6 ÷ 3 = 2. So 60 ÷ 3 = 20!") { c -> Eqs(c, "6 ÷ 3 = 2", "60 ÷ 3 = 20") },
                LessonCard("6 tens shared by 3 is 2 tens each.", PipMood.WOW) { c -> TensOnes(60, c) },
                LessonCard("Try 80 ÷ 4: 8 ÷ 4 = 2, so 80 ÷ 4 = 20!") { c -> Eqs(c, "8 ÷ 4 = 2", "80 ÷ 4 = 20") },
                LessonCard("And 150 ÷ 5: 15 ÷ 5 = 3, so the answer is 30!") { c -> Eqs(c, "15 ÷ 5 = 3", "150 ÷ 5 = 30") },
            ),
            lesson(
                "2", "Split to divide", "✂️",
                LessonCard("Big divide? Split the big number into easy parts. Try 84 ÷ 4.") { c -> BigEquation("84 ÷ 4", c) },
                LessonCard("84 is 80 and 4. 80 ÷ 4 = 20 and 4 ÷ 4 = 1.", PipMood.WOW) { c -> Eqs(c, "80 ÷ 4 = 20", "4 ÷ 4 = 1") },
                LessonCard("Add the parts: 20 + 1 = 21. So 84 ÷ 4 = 21!") { c -> BigEquation("84 ÷ 4 = 21", c) },
                LessonCard("Try 72 ÷ 3: 60 ÷ 3 = 20 and 12 ÷ 3 = 4. That's 24!") { c -> Eqs(c, "60 ÷ 3 = 20", "12 ÷ 3 = 4", "72 ÷ 3 = 24") },
            ),
            lesson(
                "3", "Check with times", "✅",
                LessonCard("Pros always check! Is 96 ÷ 4 = 24 right?", PipMood.THINK) { c -> BigEquation("96 ÷ 4 = 24", c) },
                LessonCard("Multiply back: 24 × 4. 20 × 4 = 80 and 4 × 4 = 16.") { c -> Eqs(c, "20 × 4 = 80", "4 × 4 = 16") },
                LessonCard("80 + 16 = 96. It matches, so we're right!", PipMood.WOW) { c -> BigEquation("24 × 4 = 96", c) },
                LessonCard("Estimate first: 96 is near 100, and 100 ÷ 4 = 25. So 24 makes sense!") { c -> BigEquation("100 ÷ 4 = 25", c, 40.sp) },
            ),
        )
    }
}

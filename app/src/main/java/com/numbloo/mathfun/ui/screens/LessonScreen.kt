package com.numbloo.mathfun.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numbloo.mathfun.AppState
import com.numbloo.mathfun.audio.Sfx
import com.numbloo.mathfun.model.Profile
import com.numbloo.mathfun.model.Topic
import com.numbloo.mathfun.ui.components.BigEquation
import com.numbloo.mathfun.ui.components.BouncyButton
import com.numbloo.mathfun.ui.components.FunBackground
import com.numbloo.mathfun.ui.components.FunText
import com.numbloo.mathfun.ui.components.Gap
import com.numbloo.mathfun.ui.components.GroupsVisual
import com.numbloo.mathfun.ui.components.MiniNumberLine
import com.numbloo.mathfun.ui.components.Panel
import com.numbloo.mathfun.ui.components.Pip
import com.numbloo.mathfun.ui.components.PipMood
import com.numbloo.mathfun.ui.components.ProfilePip
import com.numbloo.mathfun.ui.components.PopIn
import com.numbloo.mathfun.ui.components.PoppingItems
import com.numbloo.mathfun.ui.components.ProgressDots
import com.numbloo.mathfun.ui.components.RoundButton
import com.numbloo.mathfun.ui.components.ShareVisual
import com.numbloo.mathfun.ui.components.SpeechBubble
import com.numbloo.mathfun.ui.components.TopBar
import com.numbloo.mathfun.ui.theme.NumColors

/** One step of a lesson: what Pip says + a picture. */
class LessonCard(
    val text: String,
    val mood: PipMood = PipMood.HAPPY,
    val visual: @Composable (Color) -> Unit,
)

/** Short, silly lessons. Show first, then name it, then practice. */
object Lessons {
    fun cards(topic: Topic): List<LessonCard> = when (topic) {
        Topic.ADD -> listOf(
            LessonCard("Hi! I'm Pip and I LOVE snacks. Adding means putting things together!") { c ->
                BigEquation("+", c, 120.sp)
            },
            LessonCard("Look! Pip has 2 apples.") {
                PoppingItems(2, "🍎", size = 52.sp)
            },
            LessonCard("Then a friend brings 3 more apples. Yum!", PipMood.WOW) { c ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PoppingItems(2, "🍎", size = 40.sp)
                    FunText(" + ", 40.sp, c)
                    PoppingItems(3, "🍎", startDelay = 400, size = 40.sp)
                }
            },
            LessonCard("Put them together and count: 1, 2, 3, 4, 5. Five apples!") {
                PoppingItems(5, "🍎", size = 44.sp, numbered = true)
            },
            LessonCard("We write it like this: 2 plus 3 equals 5. The plus sign means put together!") { c ->
                BigEquation("2 + 3 = 5", c)
            },
            LessonCard("Now it's your turn! Let's try 3 together.", PipMood.WOW) {
                Text("🙌", fontSize = 90.sp)
            },
        )

        Topic.SUB -> listOf(
            LessonCard("Subtracting means taking some away. Pip has 5 cookies.") {
                PoppingItems(5, "🍪", size = 46.sp)
            },
            LessonCard("Chomp chomp! Pip eats 2 of them. Oops!", PipMood.CHEW) {
                PoppingItems(5, "🍪", size = 46.sp, crossFrom = 3)
            },
            LessonCard("How many are left? Count them: 1, 2, 3. Three cookies left!") {
                PoppingItems(3, "🍪", size = 46.sp, numbered = true)
            },
            LessonCard("We write 5 minus 2 equals 3. The minus sign means take away.") { c ->
                BigEquation("5 − 2 = 3", c)
            },
            LessonCard("Another trick: start at 5 and hop back 2 on the number line. 4, 3!", PipMood.WOW) { c ->
                MiniNumberLine(5, 2, c)
            },
            LessonCard("Your turn! Let's try 3 together.", PipMood.WOW) {
                Text("🙌", fontSize = 90.sp)
            },
        )

        Topic.MUL -> listOf(
            LessonCard("Multiplying is a super speedy way to add equal groups!") { c ->
                BigEquation("×", c, 120.sp)
            },
            LessonCard("Here are 3 plates. Each plate has 2 cookies.") { c ->
                GroupsVisual(3, 2, "🍪", c)
            },
            LessonCard("Count by twos: 2, 4, 6. That's 6 cookies!", PipMood.WOW) { c ->
                GroupsVisual(3, 2, "🍪", c, runningTotals = true)
            },
            LessonCard("2 plus 2 plus 2 makes 6. That's the same as 3 times 2!") { c ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BigEquation("2 + 2 + 2 = 6", c, 40.sp)
                    Gap(8.dp)
                    BigEquation("3 × 2 = 6", c, 48.sp)
                }
            },
            LessonCard("The times sign means groups of. So 3 times 2 means 3 groups of 2.") { c ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BigEquation("3 × 2", c)
                    FunText("3 groups of 2", 24.sp, NumColors.InkSoft)
                }
            },
            LessonCard("Your turn! Let's try 3 together.", PipMood.WOW) {
                Text("🙌", fontSize = 90.sp)
            },
        )

        Topic.DIV -> listOf(
            LessonCard("Dividing means sharing fairly, so everyone gets the same amount!") { c ->
                BigEquation("÷", c, 120.sp)
            },
            LessonCard("Here are 6 strawberries and 2 hungry friends.") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PoppingItems(6, "🍓", size = 40.sp, perRow = 6)
                    Gap(10.dp)
                    Text("🐻   🐰", fontSize = 44.sp)
                }
            },
            LessonCard("Give one to each friend, again and again, until they're all gone.", PipMood.WOW) { c ->
                ShareVisual(6, 2, "🍓", c)
            },
            LessonCard("Each friend gets 3! So 6 divided by 2 equals 3.") { c ->
                BigEquation("6 ÷ 2 = 3", c)
            },
            LessonCard("Psst, a secret! Dividing is multiplying backwards. 2 groups of 3 make 6.", PipMood.THINK) { c ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BigEquation("2 × 3 = 6", c, 40.sp)
                    FunText("⬇️", 30.sp)
                    BigEquation("6 ÷ 2 = 3", c, 40.sp)
                }
            },
            LessonCard("Your turn! Let's try 3 together.", PipMood.WOW) {
                Text("🙌", fontSize = 90.sp)
            },
        )
    }
}

@Composable
fun LessonScreen(
    app: AppState,
    profile: Profile,
    topic: Topic,
    onPractice: () -> Unit,
    onBack: () -> Unit,
) {
    val cards = remember(topic) { Lessons.cards(topic) }
    var index by remember { mutableIntStateOf(0) }
    val card = cards[index]
    val last = index == cards.lastIndex

    LaunchedEffect(index) { app.say(card.text) }

    Box(Modifier.fillMaxSize()) {
        FunBackground(topic.color)
        Column(Modifier.fillMaxSize()) {
            TopBar("Learn: ${topic.title}", onBack = onBack, color = topic.color) {
                RoundButton("🔊") { app.say(card.text) }
            }
            ProgressDots(cards.size, index, topic.color, Modifier.align(Alignment.CenterHorizontally))
            Gap(10.dp)

            AnimatedContent(
                targetState = index,
                transitionSpec = {
                    (slideInHorizontally(tween(260)) { it / 3 } + fadeIn(tween(260))) togetherWith fadeOut(tween(120))
                },
                label = "card",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) { i ->
                Panel(Modifier.fillMaxSize()) {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        cards[i].visual(topic.color)
                    }
                }
            }

            Gap(10.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfilePip(profile, Modifier.size(96.dp), mood = card.mood)
                key(index) {
                    PopIn(modifier = Modifier.weight(1f)) {
                        SpeechBubble(card.text, Modifier.fillMaxWidth(), size = 17.sp)
                    }
                }
            }
            Gap(10.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                if (index > 0) {
                    BouncyButton("⬅", Color.White, textColor = NumColors.Ink, modifier = Modifier.width(80.dp)) {
                        app.play(Sfx.TAP)
                        index--
                    }
                    Spacer(Modifier.width(12.dp))
                }
                BouncyButton(
                    text = if (last) "Let's try! 🚀" else "Next ➜",
                    color = topic.color,
                    modifier = Modifier.weight(1f),
                ) {
                    if (last) {
                        app.play(Sfx.CELEBRATE)
                        onPractice()
                    } else {
                        app.play(Sfx.WHOOSH)
                        index++
                    }
                }
            }
            Gap(14.dp)
        }
    }
}

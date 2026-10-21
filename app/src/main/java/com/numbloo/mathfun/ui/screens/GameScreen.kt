package com.numbloo.mathfun.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numbloo.mathfun.AppState
import com.numbloo.mathfun.audio.Sfx
import com.numbloo.mathfun.logic.Question
import com.numbloo.mathfun.logic.QuestionGen
import com.numbloo.mathfun.model.GameResult
import com.numbloo.mathfun.model.GameType
import com.numbloo.mathfun.model.Hat
import com.numbloo.mathfun.model.Profile
import com.numbloo.mathfun.model.Quips
import com.numbloo.mathfun.model.Topic
import com.numbloo.mathfun.ui.components.ConfettiBurst
import com.numbloo.mathfun.ui.components.FunBackground
import com.numbloo.mathfun.ui.components.Pip
import com.numbloo.mathfun.ui.components.PipMood
import com.numbloo.mathfun.ui.components.ProgressDots
import com.numbloo.mathfun.ui.components.RoundButton
import com.numbloo.mathfun.ui.components.SpeechBubble
import com.numbloo.mathfun.ui.games.BalloonPopGame
import com.numbloo.mathfun.ui.games.CookiePlatesGame
import com.numbloo.mathfun.ui.games.FeedPipGame
import com.numbloo.mathfun.ui.games.FrogHopGame
import com.numbloo.mathfun.ui.games.KangarooJumpsGame
import com.numbloo.mathfun.ui.games.PackBoxesGame
import com.numbloo.mathfun.ui.games.PizzaShareGame
import com.numbloo.mathfun.ui.games.QuizGame
import com.numbloo.mathfun.ui.games.SneakySquirrelGame
import com.numbloo.mathfun.ui.theme.NumColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

/** What every mini-game gets: colours, Pip's look, sounds and voice. */
class GameCtx(
    private val app: AppState,
    val color: Color,
    val age: Int,
    val pipColor: Color,
    val hat: Hat,
    val guided: Boolean,
) {
    fun sfx(s: Sfx) = app.play(s)
    fun say(text: String) = app.say(text)
}

/** Called by a game when the child answers. [line] lets a game say something special. */
typealias AnswerFn = (correct: Boolean, line: String?) -> Unit

fun instructionFor(game: GameType, q: Question): String = when (game) {
    GameType.FEED_PIP -> "Feed Pip ${q.a} + ${q.b} apples! Tap the tree, then press Done."
    GameType.BALLOON_POP -> "Pop the balloon that answers ${q.a} + ${q.b}!"
    GameType.FROG_HOP -> "Froggy is on ${q.a}. He hops back ${q.b}. Tap where he lands!"
    GameType.SNEAKY_SQUIRREL -> "${q.a} acorns... Sneaky Squirrel grabs ${q.b}! How many are left?"
    GameType.COOKIE_PLATES -> "Put ${q.b} cookies on each plate. Tap the plates!"
    GameType.KANGAROO_JUMPS -> "Kangaroo jumps ${q.step} at a time! Which number is missing?"
    GameType.PIZZA_SHARE -> "Share ${q.a} pizza slices with ${q.b} friends. Tap Deal!"
    GameType.PACK_BOXES -> "${q.a} eggs. Each box holds ${q.b}. Tap Pack!"
    GameType.QUIZ -> "What is ${q.spokenEquation}?"
}

@Composable
fun GameScreen(
    app: AppState,
    profile: Profile,
    topic: Topic,
    game: GameType,
    guided: Boolean,
    onExit: () -> Unit,
    onFinished: (GameResult) -> Unit,
) {
    val total = when {
        guided -> 3
        game == GameType.QUIZ -> 8
        else -> 5
    }
    val startTier = remember { if (guided) 1 else profile.stats(topic).tier }
    val ctx = remember {
        GameCtx(app, topic.color, profile.age, NumColors.pip(profile.colorIndex), profile.hat, guided)
    }
    val scope = rememberCoroutineScope()

    var tier by remember { mutableIntStateOf(startTier) }
    var index by remember { mutableIntStateOf(0) }
    var question by remember { mutableStateOf(QuestionGen.make(game, topic, startTier, profile.age)) }
    var attempts by remember { mutableIntStateOf(0) }
    var firstTry by remember { mutableIntStateOf(0) }
    var mistakes by remember { mutableIntStateOf(0) }
    var wrongStreak by remember { mutableIntStateOf(0) }
    var locked by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var mood by remember { mutableStateOf(PipMood.THINK) }
    var line by remember { mutableStateOf("") }
    var confetti by remember { mutableIntStateOf(0) }

    val instruction = instructionFor(game, question)

    LaunchedEffect(index) {
        mood = PipMood.THINK
        line = instruction
        app.say(instruction)
    }

    fun finish() {
        if (finished) return
        finished = true
        val stars = when {
            mistakes == 0 -> 3
            mistakes <= max(1, total / 4) -> 2
            else -> 1
        }
        val result = app.recordResult(profile.id, topic, game, guided, stars, firstTry, total)
        onFinished(result)
    }

    val onAnswer: AnswerFn = { correct, custom ->
        if (!locked && !finished) {
            if (correct) {
                locked = true
                if (attempts == 0) {
                    firstTry++
                    wrongStreak = 0
                }
                app.play(Sfx.CORRECT)
                confetti++
                mood = PipMood.HAPPY
                line = custom ?: Quips.correct.random()
                app.say(line)
                scope.launch {
                    delay(1700)
                    if (index + 1 >= total) {
                        finish()
                    } else {
                        // Two shaky questions in a row? Make the next one gentler.
                        if (wrongStreak >= 2 && tier > 1) {
                            tier--
                            wrongStreak = 0
                        }
                        question = QuestionGen.makeDifferent(game, topic, tier, profile.age, question)
                        attempts = 0
                        index++
                        locked = false
                    }
                }
            } else {
                if (attempts == 0) {
                    mistakes++
                    wrongStreak++
                }
                attempts++
                app.play(Sfx.WRONG)
                mood = PipMood.OOPS
                line = custom ?: if (attempts >= 2) Quips.hint else Quips.wrong.random()
                app.say(line)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        FunBackground(topic.color)
        Column(Modifier.fillMaxSize()) {
            // Top: close, progress, repeat voice
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundButton("✖️", onClick = onExit)
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ProgressDots(total, index, topic.color)
                }
                RoundButton("🔊") { app.say(line.ifEmpty { instruction }) }
            }
            // Pip and what he says
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Pip(Modifier.size(78.dp), mood = mood, color = ctx.pipColor, hat = ctx.hat)
                Spacer(Modifier.width(4.dp))
                SpeechBubble(line, Modifier.weight(1f), size = 16.sp)
            }
            // The game itself (fresh state for each question)
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                key(index) {
                    when (game) {
                        GameType.FEED_PIP -> FeedPipGame(question, attempts, ctx, onAnswer)
                        GameType.BALLOON_POP -> BalloonPopGame(question, attempts, ctx, onAnswer)
                        GameType.FROG_HOP -> FrogHopGame(question, attempts, ctx, onAnswer)
                        GameType.SNEAKY_SQUIRREL -> SneakySquirrelGame(question, attempts, ctx, onAnswer)
                        GameType.COOKIE_PLATES -> CookiePlatesGame(question, attempts, ctx, onAnswer)
                        GameType.KANGAROO_JUMPS -> KangarooJumpsGame(question, attempts, ctx, onAnswer)
                        GameType.PIZZA_SHARE -> PizzaShareGame(question, attempts, ctx, onAnswer)
                        GameType.PACK_BOXES -> PackBoxesGame(question, attempts, ctx, onAnswer)
                        GameType.QUIZ -> QuizGame(question, attempts, ctx, onAnswer)
                    }
                }
            }
        }
        ConfettiBurst(confetti, Modifier.fillMaxSize())
    }
}

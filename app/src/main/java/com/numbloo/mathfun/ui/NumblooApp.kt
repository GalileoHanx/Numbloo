package com.numbloo.mathfun.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.numbloo.mathfun.AppState
import com.numbloo.mathfun.model.GameResult
import com.numbloo.mathfun.model.GameType
import com.numbloo.mathfun.model.Topic
import com.numbloo.mathfun.ui.screens.ClosetScreen
import com.numbloo.mathfun.ui.screens.GameScreen
import com.numbloo.mathfun.ui.screens.LessonScreen
import com.numbloo.mathfun.ui.screens.LevelScreen
import com.numbloo.mathfun.ui.screens.NewProfileScreen
import com.numbloo.mathfun.ui.screens.ParentGateScreen
import com.numbloo.mathfun.ui.screens.ParentsScreen
import com.numbloo.mathfun.ui.screens.ProfilesScreen
import com.numbloo.mathfun.ui.screens.ResultsScreen
import com.numbloo.mathfun.ui.screens.StickerBookScreen
import com.numbloo.mathfun.ui.screens.TopicHubScreen
import com.numbloo.mathfun.ui.screens.TopicMapScreen
import com.numbloo.mathfun.ui.theme.NumColors

sealed interface Screen {
    data object Profiles : Screen
    data object NewProfile : Screen
    data object Levels : Screen
    data object TopicMap : Screen
    data class TopicHub(val topic: Topic) : Screen
    data class Lesson(val topic: Topic) : Screen
    data class Play(val topic: Topic, val game: GameType, val guided: Boolean = false, val round: Long = System.nanoTime()) : Screen
    data class Results(val result: GameResult) : Screen
    data object StickerBook : Screen
    data object Closet : Screen
    data object ParentGate : Screen
    data object Parents : Screen
}

/** A tiny back-stack. */
class Navigator(start: Screen) {
    val stack = mutableStateListOf(start)
    val current: Screen get() = stack.last()

    fun push(s: Screen) {
        stack.add(s)
    }

    fun pop() {
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    fun replace(s: Screen) {
        stack[stack.lastIndex] = s
    }

    fun popTo(predicate: (Screen) -> Boolean) {
        while (stack.size > 1 && !predicate(stack.last())) stack.removeAt(stack.lastIndex)
    }

    fun reset(s: Screen) {
        stack.clear()
        stack.add(s)
    }
}

@Composable
fun NumblooApp(app: AppState) {
    val nav = remember { Navigator(Screen.Profiles) }
    BackHandler(enabled = nav.stack.size > 1) {
        app.hush()
        nav.pop()
    }

    Box(Modifier.fillMaxSize().background(NumColors.Cream)) {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            AnimatedContent(
                targetState = nav.current,
                transitionSpec = {
                    (fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.96f)) togetherWith fadeOut(tween(150))
                },
                label = "nav",
            ) { screen ->
                val profile = app.current
                val needsProfile = screen !is Screen.Profiles && screen !is Screen.NewProfile &&
                    screen !is Screen.ParentGate && screen !is Screen.Parents
                if (needsProfile && profile == null) {
                    ProfilesScreen(
                        app = app,
                        onPick = { id -> app.select(id); nav.reset(Screen.Profiles); nav.push(Screen.Levels) },
                        onNew = { nav.reset(Screen.Profiles); nav.push(Screen.NewProfile) },
                        onParents = { nav.push(Screen.ParentGate) },
                    )
                    return@AnimatedContent
                }
                when (screen) {
                    Screen.Profiles -> ProfilesScreen(
                        app = app,
                        onPick = { id -> app.select(id); nav.push(Screen.Levels) },
                        onNew = { nav.push(Screen.NewProfile) },
                        onParents = { nav.push(Screen.ParentGate) },
                    )

                    Screen.NewProfile -> NewProfileScreen(
                        app = app,
                        onCreated = { p -> app.select(p.id); nav.replace(Screen.Levels) },
                        onBack = { nav.pop() },
                    )

                    Screen.Levels -> LevelScreen(
                        app = app,
                        profile = profile!!,
                        onBeginner = { nav.push(Screen.TopicMap) },
                        onStickers = { nav.push(Screen.StickerBook) },
                        onCloset = { nav.push(Screen.Closet) },
                        onSwitchPlayer = { app.hush(); app.select(null); nav.reset(Screen.Profiles) },
                    )

                    Screen.TopicMap -> TopicMapScreen(
                        app = app,
                        profile = profile!!,
                        onTopic = { t -> nav.push(Screen.TopicHub(t)) },
                        onBack = { nav.pop() },
                    )

                    is Screen.TopicHub -> TopicHubScreen(
                        app = app,
                        profile = profile!!,
                        topic = screen.topic,
                        onLesson = { nav.push(Screen.Lesson(screen.topic)) },
                        onGame = { g -> nav.push(Screen.Play(screen.topic, g)) },
                        onBack = { nav.pop() },
                    )

                    is Screen.Lesson -> LessonScreen(
                        app = app,
                        profile = profile!!,
                        topic = screen.topic,
                        onPractice = { nav.replace(Screen.Play(screen.topic, GameType.QUIZ, guided = true)) },
                        onBack = { nav.pop() },
                    )

                    is Screen.Play -> GameScreen(
                        app = app,
                        profile = profile!!,
                        topic = screen.topic,
                        game = screen.game,
                        guided = screen.guided,
                        onExit = { app.hush(); nav.pop() },
                        onFinished = { r: GameResult -> nav.replace(Screen.Results(r)) },
                    )

                    is Screen.Results -> ResultsScreen(
                        app = app,
                        profile = profile!!,
                        result = screen.result,
                        onAgain = {
                            val r = screen.result
                            nav.replace(Screen.Play(r.topic, r.game, guided = false))
                        },
                        onDone = { app.hush(); nav.pop() },
                    )

                    Screen.StickerBook -> StickerBookScreen(
                        app = app,
                        profile = profile!!,
                        onBack = { nav.pop() },
                    )

                    Screen.Closet -> ClosetScreen(
                        app = app,
                        profile = profile!!,
                        onBack = { nav.pop() },
                    )

                    Screen.ParentGate -> ParentGateScreen(
                        app = app,
                        onPass = { nav.replace(Screen.Parents) },
                        onBack = { nav.pop() },
                    )

                    Screen.Parents -> ParentsScreen(
                        app = app,
                        onBack = { nav.pop() },
                    )
                }
            }
        }
    }
}

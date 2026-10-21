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
import com.numbloo.mathfun.model.Level
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

@Composable
fun LessonScreen(
    app: AppState,
    profile: Profile,
    topic: Topic,
    level: Level,
    lessonIndex: Int,
    onPractice: (lessonId: String) -> Unit,
    onBack: () -> Unit,
) {
    val lessonList = remember(topic, level) { Lessons.forTopic(topic, level) }
    val lesson = lessonList[lessonIndex.coerceIn(0, lessonList.lastIndex)]
    val cards = lesson.cards
    var index by remember { mutableIntStateOf(0) }
    val card = cards[index]
    val last = index == cards.lastIndex

    LaunchedEffect(index) { app.say(card.text) }

    Box(Modifier.fillMaxSize()) {
        FunBackground(topic.color)
        Column(Modifier.fillMaxSize()) {
            TopBar("${lesson.emoji} ${lesson.title}", onBack = onBack, color = topic.color) {
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
                        onPractice(lesson.id)
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

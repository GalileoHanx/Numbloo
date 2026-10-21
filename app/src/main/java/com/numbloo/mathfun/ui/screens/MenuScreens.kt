package com.numbloo.mathfun.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numbloo.mathfun.AppState
import com.numbloo.mathfun.audio.Sfx
import com.numbloo.mathfun.model.GameType
import com.numbloo.mathfun.model.Hat
import com.numbloo.mathfun.model.Level
import com.numbloo.mathfun.model.Profile
import com.numbloo.mathfun.model.Topic
import com.numbloo.mathfun.ui.components.Badge
import com.numbloo.mathfun.ui.components.BouncyBox
import com.numbloo.mathfun.ui.components.BouncyButton
import com.numbloo.mathfun.ui.components.FunBackground
import com.numbloo.mathfun.ui.components.FunText
import com.numbloo.mathfun.ui.components.Gap
import com.numbloo.mathfun.ui.components.Pip
import com.numbloo.mathfun.ui.components.PipMood
import com.numbloo.mathfun.ui.components.SpeechBubble
import com.numbloo.mathfun.ui.components.StarsRow
import com.numbloo.mathfun.ui.components.TopBar
import com.numbloo.mathfun.ui.theme.NumColors

/* ---------------------------------------------------------------- Levels */

@Composable
fun LevelScreen(
    app: AppState,
    profile: Profile,
    onBeginner: () -> Unit,
    onStickers: () -> Unit,
    onSwitchPlayer: () -> Unit,
) {
    var line by remember { mutableStateOf("Hi ${profile.name}! Pick your level.") }
    var mood by remember { mutableStateOf(PipMood.HAPPY) }
    LaunchedEffect(Unit) { app.say(line) }

    Box(Modifier.fillMaxSize()) {
        FunBackground(NumColors.Sun)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                BouncyButton(
                    "👋 Switch",
                    Color.White,
                    textColor = NumColors.InkSoft,
                    fontSize = 15.sp,
                    height = 46.dp,
                    modifier = Modifier.width(120.dp),
                ) { onSwitchPlayer() }
                Spacer(Modifier.weight(1f))
                BouncyButton(
                    "🏷️ Stickers",
                    Color.White,
                    textColor = NumColors.Ink,
                    fontSize = 15.sp,
                    height = 46.dp,
                    modifier = Modifier.width(140.dp),
                ) { app.play(Sfx.TAP); onStickers() }
            }
            Gap(10.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pip(Modifier.size(120.dp), mood = mood, color = NumColors.pip(profile.colorIndex), hat = profile.hat)
                SpeechBubble(line, Modifier.weight(1f))
            }
            Gap(6.dp)
            HatProgress(profile)
            Gap(18.dp)

            Level.entries.forEach { level ->
                LevelCard(level) {
                    if (level.unlocked) {
                        app.play(Sfx.TAP)
                        onBeginner()
                    } else {
                        app.play(Sfx.WRONG)
                        mood = PipMood.OOPS
                        line = "Shh! ${level.title} land is still being built. Let's become Beginner champs first!"
                        app.say(line)
                    }
                }
                Gap(14.dp)
            }
        }
    }
}

@Composable
private fun HatProgress(profile: Profile) {
    val next = Hat.next(profile.totalStars)
    val text = if (next == null) {
        "⭐ ${profile.totalStars} stars — Pip has every hat!"
    } else {
        "⭐ ${profile.totalStars} stars — ${next.starsNeeded - profile.totalStars} more for Pip's ${next.label}!"
    }
    FunText(text, 15.sp, NumColors.InkSoft, weight = FontWeight.Bold)
}

@Composable
private fun LevelCard(level: Level, onClick: () -> Unit) {
    val color = when (level) {
        Level.BEGINNER -> NumColors.Grass
        Level.INTERMEDIATE -> Color(0xFFFF8A3D)
        Level.PRO -> Color(0xFF6A4BC4)
    }
    BouncyBox(
        color = if (level.unlocked) color else NumColors.Locked,
        modifier = Modifier.fillMaxWidth().height(108.dp),
        onClick = onClick,
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(level.emoji, fontSize = 46.sp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                FunText(level.title, 26.sp, Color.White, align = TextAlign.Start)
                FunText(level.blurb, 14.sp, Color.White, align = TextAlign.Start, weight = FontWeight.Bold)
            }
            Text(if (level.unlocked) "▶" else "🔒", fontSize = 28.sp, color = Color.White)
        }
    }
}

/* ---------------------------------------------------------------- Topic map */

@Composable
fun TopicMapScreen(
    app: AppState,
    profile: Profile,
    onTopic: (Topic) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) { app.say("Welcome to Beginner Island! Pick a topic.") }
    Column(Modifier.fillMaxSize()) {
        TopBar("Beginner Island 🏝️", onBack = onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pip(Modifier.size(90.dp), mood = PipMood.HAPPY, color = NumColors.pip(profile.colorIndex), hat = profile.hat)
                SpeechBubble(
                    if (profile.age <= 5) "Start with Addition, it's yummy!" else "Pick any topic. I'll explain first!",
                    Modifier.weight(1f),
                )
            }
            Gap(12.dp)
            Topic.entries.toList().chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    pair.forEach { t ->
                        TopicCard(t, profile, Modifier.weight(1f)) {
                            app.play(Sfx.TAP)
                            onTopic(t)
                        }
                    }
                }
                Gap(14.dp)
            }
        }
    }
}

@Composable
private fun TopicCard(topic: Topic, profile: Profile, modifier: Modifier, onClick: () -> Unit) {
    val stars = profile.topicStars(topic)
    val maxStars = (topic.games.size + 1) * 3
    val challenge = profile.age <= 5 && (topic == Topic.MUL || topic == Topic.DIV)
    BouncyBox(color = topic.color, modifier = modifier.height(200.dp), onClick = onClick) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            FunText(topic.symbol, 54.sp, Color.White)
            Text(topic.emoji, fontSize = 30.sp)
            FunText(topic.title, 18.sp, Color.White, maxLines = 1)
            FunText("⭐ $stars / $maxStars", 14.sp, Color.White)
            if (challenge) {
                Gap(4.dp)
                Badge("Big-kid challenge!", Color.Black.copy(alpha = 0.25f))
            }
        }
    }
}

/* ---------------------------------------------------------------- Topic hub */

@Composable
fun TopicHubScreen(
    app: AppState,
    profile: Profile,
    topic: Topic,
    onLesson: () -> Unit,
    onGame: (GameType) -> Unit,
    onBack: () -> Unit,
) {
    val stats = profile.stats(topic)
    LaunchedEffect(Unit) {
        app.say(if (!stats.lessonDone) "${topic.tagline} Tap Learn with Pip to start!" else "Pick a game!")
    }
    Box(Modifier.fillMaxSize()) {
        FunBackground(topic.color)
        Column(Modifier.fillMaxSize()) {
            TopBar(topic.title, onBack = onBack, color = topic.color)
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pip(Modifier.size(90.dp), mood = PipMood.HAPPY, color = NumColors.pip(profile.colorIndex), hat = profile.hat)
                    SpeechBubble(topic.tagline, Modifier.weight(1f))
                }
                Gap(6.dp)
                FunText("Puzzle level: ${stats.tierName}", 14.sp, NumColors.InkSoft, weight = FontWeight.Bold)
                Gap(12.dp)

                HubCard(
                    emoji = "📖",
                    title = "Learn with Pip",
                    blurb = "A quick, silly lesson + 3 practice tries",
                    color = topic.color,
                    badge = if (stats.lessonDone) "Done ✓" else "Start here!",
                    badgeColor = if (stats.lessonDone) NumColors.Good else Color(0xFFFF4D6D),
                    stars = -1,
                ) { app.play(Sfx.TAP); onLesson() }
                Gap(16.dp)
                FunText("Games 🎮", 22.sp, NumColors.Ink, Modifier.fillMaxWidth(), align = TextAlign.Start)
                Gap(8.dp)
                (topic.games + GameType.QUIZ).forEach { g ->
                    HubCard(
                        emoji = g.emoji,
                        title = g.title,
                        blurb = g.blurb,
                        color = Color.White,
                        textColor = NumColors.Ink,
                        stars = stats.bestStars[g.id] ?: 0,
                    ) { app.play(Sfx.TAP); onGame(g) }
                    Gap(12.dp)
                }
                Gap(12.dp)
            }
        }
    }
}

@Composable
private fun HubCard(
    emoji: String,
    title: String,
    blurb: String,
    color: Color,
    stars: Int,
    textColor: Color = Color.White,
    badge: String? = null,
    badgeColor: Color = NumColors.Good,
    onClick: () -> Unit,
) {
    BouncyBox(color = color, modifier = Modifier.fillMaxWidth().height(96.dp), onClick = onClick) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(emoji, fontSize = 40.sp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FunText(title, 21.sp, textColor, align = TextAlign.Start, maxLines = 1)
                    if (badge != null) {
                        Spacer(Modifier.width(8.dp))
                        Badge(badge, badgeColor)
                    }
                }
                FunText(blurb, 13.sp, textColor.copy(alpha = 0.8f), align = TextAlign.Start, weight = FontWeight.Bold, maxLines = 2)
            }
            if (stars >= 0) StarsRow(stars, size = 20.sp)
        }
    }
}

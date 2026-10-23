package com.numbloo.mathfun.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numbloo.mathfun.AppState
import com.numbloo.mathfun.audio.Sfx
import com.numbloo.mathfun.model.Curriculum
import com.numbloo.mathfun.model.GameType
import com.numbloo.mathfun.model.Rewards
import com.numbloo.mathfun.model.Level
import com.numbloo.mathfun.model.Profile
import com.numbloo.mathfun.model.Topic
import com.numbloo.mathfun.ui.components.Badge
import com.numbloo.mathfun.ui.components.BouncyBox
import com.numbloo.mathfun.ui.components.BouncyButton
import com.numbloo.mathfun.ui.components.FunBackground
import com.numbloo.mathfun.ui.components.FunText
import com.numbloo.mathfun.ui.components.Gap
import com.numbloo.mathfun.ui.components.PipMood
import com.numbloo.mathfun.ui.components.ProfilePip
import com.numbloo.mathfun.ui.components.SpeechBubble
import com.numbloo.mathfun.ui.components.StarsRow
import com.numbloo.mathfun.ui.components.TopBar
import com.numbloo.mathfun.ui.theme.NumColors

fun levelColor(level: Level): Color = when (level) {
    Level.BEGINNER -> NumColors.Grass
    Level.INTERMEDIATE -> Color(0xFFFF8A3D)
    Level.PRO -> Color(0xFF6A4BC4)
}

/* ---------------------------------------------------------------- Levels */

@Composable
fun LevelScreen(
    app: AppState,
    profile: Profile,
    onLevel: (Level) -> Unit,
    onStickers: () -> Unit,
    onCloset: () -> Unit,
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
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BouncyButton(
                    "👋 Switch",
                    Color.White,
                    textColor = NumColors.InkSoft,
                    fontSize = 14.sp,
                    height = 48.dp,
                    modifier = Modifier.weight(1f),
                ) { onSwitchPlayer() }
                BouncyButton(
                    "🏷️ Stickers",
                    Color.White,
                    textColor = NumColors.Ink,
                    fontSize = 14.sp,
                    height = 48.dp,
                    modifier = Modifier.weight(1f),
                ) { app.play(Sfx.TAP); onStickers() }
                BouncyButton(
                    if (profile.closetOpen) "👕 Closet" else "👕 🔒",
                    if (profile.closetOpen) Color(0xFFB388FF) else Color.White,
                    textColor = if (profile.closetOpen) Color.White else NumColors.InkSoft,
                    fontSize = 14.sp,
                    height = 48.dp,
                    modifier = Modifier.weight(1f),
                ) {
                    if (profile.closetOpen) {
                        app.play(Sfx.TAP)
                        onCloset()
                    } else {
                        app.play(Sfx.WRONG)
                        mood = PipMood.OOPS
                        line = "My closet is locked! Win any game with 3 stars to open it."
                        app.say(line)
                    }
                }
            }
            Gap(10.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfilePip(profile, Modifier.size(120.dp), mood = mood)
                SpeechBubble(line, Modifier.weight(1f))
            }
            Gap(6.dp)
            HatProgress(profile)
            Gap(18.dp)

            Level.entries.forEach { level ->
                val unlocked = profile.levelUnlocked(level)
                val openCount = Topic.entries.count { profile.topicUnlocked(it, level) }
                val sub = when {
                    level == Level.BEGINNER -> level.blurb
                    unlocked -> "$openCount of 4 operations open • ${level.blurb}"
                    else -> "Get every ⭐ in a ${level.previous?.title} operation to open"
                }
                LevelCard(level, unlocked, sub) {
                    if (unlocked) {
                        app.play(Sfx.TAP)
                        onLevel(level)
                    } else {
                        app.play(Sfx.WRONG)
                        mood = PipMood.OOPS
                        line = "${level.title} is locked! Collect all the stars in one ${level.previous?.title} operation, like Addition, to open it."
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
    val got = profile.collectedStars()
    val total = Rewards.totalStars()
    val next = Rewards.nextByStars(got)
    val text = if (next == null) {
        "⭐ $got / $total stars — every star prize won!"
    } else {
        "⭐ $got / $total stars — ${next.first - got} more for ${next.second.first().label}!"
    }
    FunText(text, 15.sp, NumColors.InkSoft, weight = FontWeight.Bold)
}

@Composable
private fun LevelCard(level: Level, unlocked: Boolean, sub: String, onClick: () -> Unit) {
    BouncyBox(
        color = if (unlocked) levelColor(level) else NumColors.Locked,
        modifier = Modifier.fillMaxWidth().height(112.dp),
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
                FunText(sub, 13.sp, Color.White, align = TextAlign.Start, weight = FontWeight.Bold, maxLines = 2)
            }
            Text(if (unlocked) "▶" else "🔒", fontSize = 28.sp, color = Color.White)
        }
    }
}

/* ---------------------------------------------------------------- Topic map */

@Composable
fun TopicMapScreen(
    app: AppState,
    profile: Profile,
    level: Level,
    onTopic: (Topic) -> Unit,
    onBack: () -> Unit,
) {
    var line by remember {
        mutableStateOf(
            when {
                level != Level.BEGINNER -> "Welcome to ${level.title} Island! Bigger numbers live here."
                profile.age <= 5 -> "Start with Addition, it's yummy!"
                else -> "Pick any operation. I'll explain first!"
            }
        )
    }
    var mood by remember { mutableStateOf(PipMood.HAPPY) }
    LaunchedEffect(Unit) { app.say(line) }
    Column(Modifier.fillMaxSize()) {
        TopBar("${level.title} Island 🏝️", onBack = onBack, color = levelColor(level))
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfilePip(profile, Modifier.size(90.dp), mood = mood)
                SpeechBubble(line, Modifier.weight(1f))
            }
            Gap(12.dp)
            Topic.entries.toList().chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    pair.forEach { t ->
                        val open = profile.topicUnlocked(t, level)
                        TopicCard(t, level, profile, open, Modifier.weight(1f)) {
                            if (open) {
                                app.play(Sfx.TAP)
                                onTopic(t)
                            } else {
                                app.play(Sfx.WRONG)
                                mood = PipMood.OOPS
                                line = "Locked! Get every star in ${level.previous?.title} ${t.title} to open this one."
                                app.say(line)
                            }
                        }
                    }
                }
                Gap(14.dp)
            }
        }
    }
}

@Composable
private fun TopicCard(topic: Topic, level: Level, profile: Profile, open: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val stars = profile.topicStars(topic, level)
    val maxStars = Curriculum.maxStars(topic, level)
    val challenge = level == Level.BEGINNER && profile.age <= 5 && (topic == Topic.MUL || topic == Topic.DIV)
    BouncyBox(color = if (open) topic.color else NumColors.Locked, modifier = modifier.height(206.dp), onClick = onClick) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            FunText(topic.symbol, 54.sp, Color.White)
            Text(if (open) topic.emoji else "🔒", fontSize = 30.sp)
            FunText(topic.title, 18.sp, Color.White, maxLines = 1)
            if (open) {
                FunText("⭐ $stars / $maxStars", 14.sp, Color.White)
                if (stars >= maxStars && level.next != null) Badge("${level.next?.title} open!", Color.Black.copy(alpha = 0.25f))
            } else {
                FunText("All ⭐ in ${level.previous?.title} first", 12.sp, Color.White)
            }
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
    level: Level,
    onLesson: (Int) -> Unit,
    onGame: (GameType) -> Unit,
    onBack: () -> Unit,
) {
    val stats = profile.stats(topic, level)
    val lessons = remember(topic, level) { Lessons.forTopic(topic, level) }
    val stars = profile.topicStars(topic, level)
    val maxStars = Curriculum.maxStars(topic, level)
    LaunchedEffect(Unit) {
        app.say(if (!stats.lessonDone) "${topic.tagline} Start with a lesson!" else "Pick a lesson or a game!")
    }
    Box(Modifier.fillMaxSize()) {
        FunBackground(topic.color)
        Column(Modifier.fillMaxSize()) {
            TopBar("${level.emoji} ${topic.title}", onBack = onBack, color = topic.color)
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfilePip(profile, Modifier.size(90.dp))
                    SpeechBubble(topic.tagline, Modifier.weight(1f))
                }
                Gap(6.dp)
                FunText("${level.title} • Puzzle level: ${stats.tierName}", 14.sp, NumColors.InkSoft, weight = FontWeight.Bold)
                Gap(8.dp)
                LevelProgress(stars, maxStars, level, topic)
                Gap(14.dp)

                FunText("Lessons 📖", 22.sp, NumColors.Ink, Modifier.fillMaxWidth(), align = TextAlign.Start)
                Gap(8.dp)
                lessons.forEachIndexed { i, l ->
                    val done = l.id in stats.lessons
                    val firstNotDone = lessons.indexOfFirst { it.id !in stats.lessons }
                    HubCard(
                        emoji = l.emoji,
                        title = "${i + 1}. ${l.title}",
                        blurb = "A quick, silly lesson + 3 practice tries",
                        color = topic.color,
                        badge = when {
                            done -> "Done ✓"
                            i == firstNotDone -> "Next!"
                            else -> null
                        },
                        badgeColor = if (done) NumColors.Good else Color(0xFFFF4D6D),
                        stars = -1,
                        height = 84,
                    ) { app.play(Sfx.TAP); onLesson(i) }
                    Gap(10.dp)
                }
                Gap(10.dp)
                FunText("Games 🎮", 22.sp, NumColors.Ink, Modifier.fillMaxWidth(), align = TextAlign.Start)
                Gap(8.dp)
                Curriculum.starGames(topic, level).forEach { g ->
                    val best = stats.bestStars[g.id] ?: 0
                    HubCard(
                        emoji = g.emoji,
                        title = g.title,
                        blurb = g.blurb,
                        color = Color.White,
                        textColor = NumColors.Ink,
                        badge = if (g in GameType.ADDED_V4 && best == 0) "New!" else null,
                        badgeColor = Color(0xFFFF4D6D),
                        stars = best,
                    ) { app.play(Sfx.TAP); onGame(g) }
                    Gap(12.dp)
                }
                Gap(12.dp)
            }
        }
    }
}

/** "⭐ 14 / 24 — get them all to open Intermediate" with a little bar. */
@Composable
private fun LevelProgress(stars: Int, maxStars: Int, level: Level, topic: Topic) {
    val next = level.next
    val text = when {
        next == null -> "⭐ $stars / $maxStars — Pro champion stars!"
        stars >= maxStars -> "⭐ $stars / $maxStars — ${next.title} ${topic.title} is open! 🎉"
        else -> "⭐ $stars / $maxStars — get them all to open ${next.title} ${topic.title}"
    }
    Column(Modifier.fillMaxWidth()) {
        FunText(text, 14.sp, NumColors.Ink, weight = FontWeight.Bold, align = TextAlign.Start)
        Gap(4.dp)
        Box(
            Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(NumColors.Locked.copy(alpha = 0.6f))
        ) {
            val frac = if (maxStars == 0) 0f else (stars.toFloat() / maxStars).coerceIn(0f, 1f)
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(frac)
                    .clip(RoundedCornerShape(7.dp))
                    .background(NumColors.Sun)
            )
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
    height: Int = 96,
    onClick: () -> Unit,
) {
    BouncyBox(color = color, modifier = Modifier.fillMaxWidth().height(height.dp), onClick = onClick) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(emoji, fontSize = 36.sp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FunText(title, 20.sp, textColor, Modifier.weight(1f, fill = false), align = TextAlign.Start, maxLines = 1)
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

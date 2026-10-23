package com.numbloo.mathfun.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numbloo.mathfun.AppState
import com.numbloo.mathfun.audio.Sfx
import com.numbloo.mathfun.model.GameResult
import com.numbloo.mathfun.model.Level
import com.numbloo.mathfun.model.Profile
import com.numbloo.mathfun.model.Stickers
import com.numbloo.mathfun.ui.components.BouncyButton
import com.numbloo.mathfun.ui.components.ConfettiBurst
import com.numbloo.mathfun.ui.components.FunBackground
import com.numbloo.mathfun.ui.components.FunText
import com.numbloo.mathfun.ui.components.Gap
import com.numbloo.mathfun.ui.components.Panel
import com.numbloo.mathfun.ui.components.Pip
import com.numbloo.mathfun.ui.components.PipMood
import com.numbloo.mathfun.ui.components.ProfilePip
import com.numbloo.mathfun.ui.components.PopIn
import com.numbloo.mathfun.ui.components.StarsRow
import com.numbloo.mathfun.ui.components.TopBar
import com.numbloo.mathfun.ui.theme.NumColors

@Composable
fun ResultsScreen(
    app: AppState,
    profile: Profile,
    result: GameResult,
    onAgain: () -> Unit,
    onDone: () -> Unit,
) {
    val headline = when (result.stars) {
        3 -> "WOW, ${profile.name}! Three stars!"
        2 -> "Great job, ${profile.name}!"
        else -> "You did it, ${profile.name}! Practice makes Pip proud!"
    }
    LaunchedEffect(Unit) {
        app.play(Sfx.CELEBRATE)
        val extra = buildString {
            if (result.sticker != null) append(" You got a new sticker: ${result.sticker.name}!")
            if (result.newHat != null) append(" And Pip got a ${result.newHat.label}!")
            if (result.levelUnlocked != null) append(" WOW! ${result.levelUnlocked.title} ${result.topic.title} is now open!")
            result.trophies.forEach { append(" You won the $it trophy!") }
            if (result.closetOpened) append(" Pip's closet is now open!")
            else if (result.unlocked.isNotEmpty()) append(" New things in Pip's closet!")
        }
        app.say(headline + extra, queue = true)
    }

    Box(Modifier.fillMaxSize()) {
        FunBackground(result.topic.color)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Gap(10.dp)
            ProfilePip(profile, Modifier.size(180.dp))
            FunText(headline, 26.sp, NumColors.Ink)
            Gap(8.dp)
            PopIn(300) { StarsRow(result.stars, size = 56.sp) }
            FunText(
                "${result.correctFirstTry} of ${result.total} right on the first try",
                16.sp,
                NumColors.InkSoft,
                weight = FontWeight.Bold,
            )
            Gap(16.dp)

            if (result.sticker != null) {
                PopIn(700) {
                    Panel(Modifier.fillMaxWidth()) {
                        FunText("New sticker!", 18.sp, result.topic.color)
                        Text(result.sticker.emoji, fontSize = 64.sp)
                        FunText(result.sticker.name, 20.sp)
                    }
                }
                Gap(12.dp)
            }
            val opened = result.levelUnlocked
            if (opened != null) {
                PopIn(500) {
                    Panel(Modifier.fillMaxWidth(), color = levelColor(opened).copy(alpha = 0.3f)) {
                        FunText("${opened.emoji} ${opened.title} ${result.topic.title} is open!", 20.sp)
                        FunText("You got every star! Bigger numbers are waiting.", 15.sp, NumColors.InkSoft)
                    }
                }
                Gap(12.dp)
            }
            if (result.closetOpened) {
                PopIn(900) {
                    Panel(Modifier.fillMaxWidth(), color = NumColors.Grass.copy(alpha = 0.25f)) {
                        FunText("👕 Pip's Closet is open!", 20.sp)
                        FunText("Three stars! Now you can dress up Pip from the level menu.", 15.sp, NumColors.InkSoft)
                    }
                }
                Gap(12.dp)
            }
            result.trophies.forEach { name ->
                PopIn(800) {
                    Panel(Modifier.fillMaxWidth(), color = NumColors.Sun.copy(alpha = 0.45f)) {
                        FunText("🏆 Trophy won!", 20.sp)
                        FunText(name, 18.sp, NumColors.Ink)
                    }
                }
                Gap(12.dp)
            }
            if (result.unlocked.isNotEmpty()) {
                PopIn(1100) {
                    Panel(Modifier.fillMaxWidth(), color = Color(0xFFB388FF).copy(alpha = 0.25f)) {
                        FunText("✨ New in Pip's Closet", 18.sp)
                        FunText(result.unlocked.joinToString(", "), 16.sp, NumColors.InkSoft)
                    }
                }
                Gap(12.dp)
            }
            if (result.newHat != null) {
                PopIn(1000) {
                    Panel(Modifier.fillMaxWidth(), color = NumColors.Sun.copy(alpha = 0.35f)) {
                        FunText("🎩 Pip unlocked the ${result.newHat.label}!", 18.sp)
                    }
                }
                Gap(12.dp)
            }
            when {
                result.tierChange > 0 -> FunText("Level up! 🚀 Trickier puzzles next time.", 16.sp, NumColors.Good)
                result.tierChange < 0 -> FunText("Next round is warm-up mode 💪", 16.sp, NumColors.InkSoft)
            }
            Gap(20.dp)

            if (result.guided) {
                BouncyButton("Play the games! 🎮", result.topic.color, Modifier.fillMaxWidth(), height = 70.dp) {
                    app.play(Sfx.TAP)
                    onDone()
                }
            } else {
                BouncyButton("Play again 🔁", result.topic.color, Modifier.fillMaxWidth(), height = 70.dp) {
                    app.play(Sfx.TAP)
                    onAgain()
                }
                Gap(12.dp)
                BouncyButton("More games 🎮", Color.White, Modifier.fillMaxWidth(), textColor = NumColors.Ink) {
                    app.play(Sfx.TAP)
                    onDone()
                }
            }
            Gap(20.dp)
        }
        ConfettiBurst(1, Modifier.fillMaxSize())
    }
}

@Composable
fun StickerBookScreen(app: AppState, profile: Profile, onBack: () -> Unit) {
    val owned = profile.stickers.toSet()
    var level by remember { mutableStateOf(Level.BEGINNER) }
    val stickers = Stickers.forLevel(level)
    val have = stickers.count { it.id in owned }
    LaunchedEffect(Unit) {
        app.say("You have $have ${level.title} stickers! Win more by playing games.")
    }
    Column(Modifier.fillMaxSize()) {
        TopBar("Sticker Book", onBack = onBack)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Level.entries.forEach { l ->
                val selected = l == level
                BouncyButton(
                    text = "${l.emoji} ${l.title}",
                    color = if (selected) NumColors.Ink else Color.White,
                    textColor = if (selected) Color.White else NumColors.Ink,
                    fontSize = 13.sp,
                    height = 50.dp,
                    modifier = Modifier.weight(1f),
                ) {
                    level = l
                    app.play(Sfx.TAP)
                }
            }
        }
        Gap(8.dp)
        FunText(
            if (profile.levelUnlocked(level)) "$have / ${stickers.size} collected"
            else "🔒 Unlocks when ${level.title} opens — get every ⭐ in a ${level.previous?.title} operation!",
            17.sp,
            NumColors.InkSoft,
            Modifier.fillMaxWidth(),
        )
        Gap(10.dp)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            stickers.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { s ->
                        val got = s.id in owned
                        Column(
                            Modifier
                                .weight(1f)
                                .aspectRatio(0.85f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (got) Color.White else NumColors.Locked.copy(alpha = 0.5f)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(if (got) s.emoji else if (profile.levelUnlocked(level)) "❓" else "🔒", fontSize = 36.sp)
                            FunText(if (got) s.name else "???", 12.sp, NumColors.InkSoft, maxLines = 1)
                        }
                    }
                    repeat(4 - row.size) { Box(Modifier.weight(1f)) }
                }
                Gap(10.dp)
            }
        }
    }
}

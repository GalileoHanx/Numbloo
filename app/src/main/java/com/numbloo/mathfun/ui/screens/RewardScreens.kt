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
        }
        app.say(headline + extra)
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
            Pip(
                Modifier.size(180.dp),
                mood = PipMood.HAPPY,
                color = NumColors.pip(profile.colorIndex),
                hat = profile.hat,
            )
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
    LaunchedEffect(Unit) {
        app.say("You have ${profile.stickers.size} stickers! Win more by playing games.")
    }
    val owned = profile.stickers.toSet()
    Column(Modifier.fillMaxSize()) {
        TopBar("Sticker Book", onBack = onBack)
        FunText(
            "${owned.size} / ${Stickers.all.size} collected",
            18.sp,
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
            Stickers.all.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { s ->
                        val have = s.id in owned
                        Column(
                            Modifier
                                .weight(1f)
                                .aspectRatio(0.85f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (have) Color.White else NumColors.Locked.copy(alpha = 0.5f)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(if (have) s.emoji else "❓", fontSize = 36.sp)
                            FunText(if (have) s.name else "???", 12.sp, NumColors.InkSoft, maxLines = 1)
                        }
                    }
                    repeat(4 - row.size) { Box(Modifier.weight(1f)) }
                }
                Gap(10.dp)
            }
        }
    }
}

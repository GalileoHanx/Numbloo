package com.numbloo.mathfun.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numbloo.mathfun.AppState
import com.numbloo.mathfun.audio.Sfx
import com.numbloo.mathfun.model.Eyes
import com.numbloo.mathfun.model.Flag
import com.numbloo.mathfun.model.Glasses
import com.numbloo.mathfun.model.Hair
import com.numbloo.mathfun.model.Hat
import com.numbloo.mathfun.model.Job
import com.numbloo.mathfun.model.Mouth
import com.numbloo.mathfun.model.Neck
import com.numbloo.mathfun.model.Pet
import com.numbloo.mathfun.model.PipColors
import com.numbloo.mathfun.model.Profile
import com.numbloo.mathfun.model.Reward
import com.numbloo.mathfun.model.Rewards
import com.numbloo.mathfun.model.Shelf
import com.numbloo.mathfun.model.Unlock
import com.numbloo.mathfun.ui.components.BouncyBox
import com.numbloo.mathfun.ui.components.BouncyButton
import com.numbloo.mathfun.ui.components.FunBackground
import com.numbloo.mathfun.ui.components.FunText
import com.numbloo.mathfun.ui.components.Gap
import com.numbloo.mathfun.ui.components.LookPip
import com.numbloo.mathfun.ui.components.PipLook
import com.numbloo.mathfun.ui.components.PipMood
import com.numbloo.mathfun.ui.components.ProfilePip
import com.numbloo.mathfun.ui.components.SpeechBubble
import com.numbloo.mathfun.ui.components.TopBar
import com.numbloo.mathfun.ui.components.pipLook
import com.numbloo.mathfun.ui.theme.NumColors

/** Puts a closet item on Pip. */
private fun wear(p: Profile, r: Reward): Profile = when (val v = r.value) {
    is Int -> p.copy(colorIndex = v)
    is Flag -> p.copy(flag = v)
    is Hair -> p.copy(hair = v)
    is Job -> p.copy(job = v)
    is Eyes -> p.copy(eyes = v)
    is Mouth -> p.copy(mouth = v)
    is Hat -> p.copy(hatChoice = v.name, job = Job.NONE)
    is Glasses -> p.copy(glasses = v)
    is Neck -> p.copy(neck = v)
    is Pet -> p.copy(pet = v)
    else -> p
}

/** Is Pip wearing this item right now? */
private fun wearing(p: Profile, r: Reward): Boolean = when (val v = r.value) {
    is Int -> p.colorIndex == v
    is Flag -> p.flag == v
    is Hair -> p.hair == v
    is Job -> p.job == v
    is Eyes -> p.eyes == v
    is Mouth -> p.mouth == v
    is Hat -> p.hat == v && p.job == Job.NONE
    is Glasses -> p.glasses == v
    is Neck -> p.neck == v
    is Pet -> p.pet == v
    else -> false
}

/** Pip's Closet: opens after the first 3-star game. Every few stars, and every trophy, unlocks more. */
@Composable
fun ClosetScreen(app: AppState, profile: Profile, onBack: () -> Unit) {
    var line by remember { mutableStateOf("Ooh, my closet! Every star you collect opens something new.") }
    var mood by remember { mutableStateOf(PipMood.HAPPY) }
    var shelf by remember { mutableStateOf<Shelf?>(Shelf.HATS) } // null = trophy room
    LaunchedEffect(Unit) { app.say(line) }

    val base = profile.pipLook()
    val collected = profile.collectedStars()
    val total = Rewards.totalStars()
    val next = Rewards.nextByStars(collected)
    val trophiesWon = Rewards.trophies.count { profile.meets(it) }

    fun tap(r: Reward) {
        if (profile.owns(r)) {
            app.updateProfile(profile.id) { wear(it, r) }
            app.play(Sfx.POP)
            mood = PipMood.HAPPY
            line = listOf("Looking fabulous!", "Ooh, so fancy!", "I love it!", "Pip-tastic style!", "Wow, is that me?").random()
        } else {
            app.play(Sfx.WRONG)
            mood = PipMood.OOPS
            line = when (val u = r.unlock) {
                is Unlock.Stars -> "Not yet! Collect ${u.n - collected} more stars to unlock ${r.label}."
                else -> "That's a trophy prize! ${Rewards.trophyGoal(u).replace("⭐", "star")} to win ${r.label}."
            }
        }
        app.say(line)
    }

    Box(Modifier.fillMaxSize()) {
        FunBackground(Color(0xFFB388FF))
        Column(Modifier.fillMaxSize()) {
            TopBar("Pip's Closet 👕", onBack = onBack)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfilePip(profile, Modifier.size(130.dp), mood = mood)
                SpeechBubble(line, Modifier.weight(1f), size = 15.sp)
            }
            // Star progress
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                FunText(
                    "⭐ $collected / $total stars collected   🏆 $trophiesWon / ${Rewards.trophies.size}",
                    15.sp, NumColors.Ink, align = TextAlign.Start,
                )
                Gap(4.dp)
                ProgressBar(collected, total, NumColors.Sun)
                Gap(4.dp)
                FunText(
                    if (next == null) {
                        "Every star prize is yours! Win the trophies for the rest."
                    } else {
                        "Next at ${next.first} ⭐ (${next.first - collected} more): " + next.second.joinToString(" + ") { it.label }
                    },
                    13.sp, NumColors.InkSoft, align = TextAlign.Start, weight = FontWeight.Bold,
                )
            }
            Gap(8.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val tabs: List<Shelf?> = listOf<Shelf?>(null) + Shelf.entries
                tabs.forEach { sh ->
                    val sel = sh == shelf
                    val owned = if (sh == null) trophiesWon else Rewards.onShelf(sh).count { profile.owns(it) }
                    val count = if (sh == null) Rewards.trophies.size else Rewards.onShelf(sh).size
                    BouncyButton(
                        (sh?.title ?: "🏆 Trophies") + "  $owned/$count",
                        if (sel) NumColors.Ink else Color.White,
                        Modifier.width(150.dp),
                        textColor = if (sel) Color.White else NumColors.Ink,
                        fontSize = 13.sp,
                        height = 48.dp,
                    ) {
                        shelf = sh
                        app.play(Sfx.TAP)
                    }
                }
            }
            Gap(10.dp)
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                val sh = shelf
                if (sh == null) {
                    Rewards.trophies.forEach { t ->
                        TrophyCard(profile, t)
                        Gap(10.dp)
                    }
                } else {
                    Rewards.onShelf(sh).chunked(3).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { r ->
                                ClosetTile(
                                    r = r,
                                    base = base,
                                    owned = profile.owns(r),
                                    selected = wearing(profile, r),
                                    lockText = Rewards.lockText(profile, r),
                                    modifier = Modifier.weight(1f),
                                ) { tap(r) }
                            }
                            repeat(3 - row.size) { Box(Modifier.weight(1f)) }
                        }
                        Gap(10.dp)
                    }
                }
                Gap(20.dp)
            }
        }
    }
}

@Composable
private fun ProgressBar(value: Int, max: Int, color: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(NumColors.Locked.copy(alpha = 0.6f))
    ) {
        val frac = if (max <= 0) 0f else (value.toFloat() / max).coerceIn(0f, 1f)
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(frac)
                .clip(RoundedCornerShape(6.dp))
                .background(color)
        )
    }
}

/** What an item looks like on its tile. */
@Composable
private fun RewardPreview(r: Reward, base: PipLook) {
    val pipSize = Modifier.size(66.dp)
    when (val v = r.value) {
        is Int -> {
            val swatch = Modifier
                .size(50.dp)
                .clip(CircleShape)
            if (v == PipColors.RAINBOW) {
                Box(swatch.background(Brush.sweepGradient(NumColors.Rainbow)))
            } else {
                Box(swatch.background(NumColors.pip(v)))
            }
        }
        is Flag -> if (v == Flag.NONE) LookPip(base.copy(flag = v), pipSize, animated = false) else Text(v.emoji, fontSize = 44.sp)
        is Pet -> if (v == Pet.NONE) LookPip(base.copy(pet = v), pipSize, animated = false) else Text(v.emoji, fontSize = 44.sp)
        is Hair -> LookPip(base.copy(hair = v, hat = Hat.NONE, job = Job.NONE), pipSize, animated = false)
        is Job -> LookPip(base.copy(job = v), pipSize, animated = false)
        is Eyes -> LookPip(base.copy(eyes = v, glasses = Glasses.NONE, job = Job.NONE), pipSize, animated = false)
        is Mouth -> LookPip(base.copy(mouth = v), pipSize, animated = false)
        is Hat -> LookPip(base.copy(hat = v, job = Job.NONE, hair = Hair.NONE), pipSize, animated = false)
        is Glasses -> LookPip(base.copy(glasses = v, job = Job.NONE), pipSize, animated = false)
        is Neck -> LookPip(base.copy(neck = v), pipSize, animated = false)
        else -> {}
    }
}

@Composable
private fun ClosetTile(
    r: Reward,
    base: PipLook,
    owned: Boolean,
    selected: Boolean,
    lockText: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val trophyPrize = r.unlock !is Unlock.Stars && r.unlock != Unlock.Free
    BouncyBox(
        color = when {
            owned -> Color.White
            trophyPrize -> NumColors.Sun.copy(alpha = 0.35f)
            else -> NumColors.Locked.copy(alpha = 0.55f)
        },
        modifier = modifier
            .aspectRatio(0.8f)
            .then(
                if (selected) Modifier.border(4.dp, NumColors.Ink, RoundedCornerShape(22.dp))
                else Modifier
            ),
        onClick = onClick,
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(contentAlignment = Alignment.Center) {
                RewardPreview(r, base)
                if (!owned) FunText(if (trophyPrize) "🏆" else "🔒", 26.sp)
            }
            FunText(r.label, 12.sp, NumColors.Ink, maxLines = 1)
            when {
                !owned -> FunText(lockText, 10.sp, NumColors.InkSoft, weight = FontWeight.Bold, maxLines = 2)
                selected -> FunText("Wearing ✓", 11.sp, NumColors.Good)
            }
        }
    }
}

@Composable
private fun TrophyCard(profile: Profile, t: Unlock) {
    val won = profile.meets(t)
    val (got, need) = Rewards.trophyProgress(profile, t)
    val prizes = Rewards.prizesFor(t)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (won) NumColors.Sun.copy(alpha = 0.35f) else Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(if (won) "🏆" else "🎯", fontSize = 36.sp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            FunText(Rewards.trophyName(t), 17.sp, NumColors.Ink, align = TextAlign.Start, maxLines = 1)
            FunText(Rewards.trophyGoal(t), 12.sp, NumColors.InkSoft, align = TextAlign.Start, weight = FontWeight.Bold)
            Gap(4.dp)
            ProgressBar(got, need, if (won) NumColors.Good else NumColors.Sun)
            Gap(2.dp)
            FunText(
                (if (won) "Won! " else "⭐ $got / $need  •  ") + "Prize: " + prizes.joinToString(" + ") { prizeName(it) },
                12.sp, NumColors.Ink, align = TextAlign.Start, weight = FontWeight.Bold,
            )
        }
    }
}

private fun prizeName(r: Reward): String = when (val v = r.value) {
    is Pet -> "${v.emoji} ${v.label}"
    else -> "${r.label} ${r.shelf.title.substringBefore(' ')}"
}

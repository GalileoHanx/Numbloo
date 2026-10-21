package com.numbloo.mathfun.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.numbloo.mathfun.model.Glasses
import com.numbloo.mathfun.model.Hat
import com.numbloo.mathfun.model.Neck
import com.numbloo.mathfun.model.PipColors
import com.numbloo.mathfun.model.Profile
import com.numbloo.mathfun.ui.components.BouncyBox
import com.numbloo.mathfun.ui.components.FunBackground
import com.numbloo.mathfun.ui.components.FunText
import com.numbloo.mathfun.ui.components.Gap
import com.numbloo.mathfun.ui.components.Pip
import com.numbloo.mathfun.ui.components.PipMood
import com.numbloo.mathfun.ui.components.ProfilePip
import com.numbloo.mathfun.ui.components.SpeechBubble
import com.numbloo.mathfun.ui.components.TopBar
import com.numbloo.mathfun.ui.theme.NumColors

/** One thing Pip can wear. */
private class ClosetItem(
    val label: String,
    val unlocked: Boolean,
    val lockText: String,
    val selected: Boolean,
    val preview: @Composable () -> Unit,
    val apply: (Profile) -> Profile,
)

/** Pip's Closet: opens after the first 3-star game; more items unlock with more 3-star wins. */
@Composable
fun ClosetScreen(app: AppState, profile: Profile, onBack: () -> Unit) {
    var line by remember { mutableStateOf("Ooh, my closet! Tap something for me to wear.") }
    var mood by remember { mutableStateOf(PipMood.HAPPY) }
    LaunchedEffect(Unit) { app.say(line) }

    val gold = profile.goldWins
    val pc = NumColors.pip(profile.colorIndex)

    val colours = PipColors.names.indices.map { i ->
        val need = PipColors.goldNeeded[i]
        ClosetItem(
            label = PipColors.names[i],
            unlocked = gold >= need,
            lockText = "${need - gold} more 3⭐ games",
            selected = profile.colorIndex == i,
            preview = {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(NumColors.pip(i))
                )
            },
            apply = { it.copy(colorIndex = i) },
        )
    }
    val hats = Hat.entries.map { h ->
        ClosetItem(
            label = h.label,
            unlocked = profile.totalStars >= h.starsNeeded,
            lockText = "${h.starsNeeded - profile.totalStars} more ⭐",
            selected = profile.hat == h,
            preview = { Pip(Modifier.size(64.dp), color = pc, hat = h) },
            apply = { it.copy(hatChoice = h.name) },
        )
    }
    val glasses = Glasses.entries.map { g ->
        ClosetItem(
            label = g.label,
            unlocked = gold >= g.goldNeeded,
            lockText = "${g.goldNeeded - gold} more 3⭐ games",
            selected = profile.glasses == g,
            preview = { Pip(Modifier.size(64.dp), color = pc, glasses = g) },
            apply = { it.copy(glasses = g) },
        )
    }
    val necks = Neck.entries.map { n ->
        ClosetItem(
            label = n.label,
            unlocked = gold >= n.goldNeeded,
            lockText = "${n.goldNeeded - gold} more 3⭐ games",
            selected = profile.neck == n,
            preview = { Pip(Modifier.size(64.dp), color = pc, neck = n) },
            apply = { it.copy(neck = n) },
        )
    }

    fun tap(item: ClosetItem) {
        if (item.unlocked) {
            app.updateProfile(profile.id, item.apply)
            app.play(Sfx.POP)
            mood = PipMood.HAPPY
            line = listOf("Looking fabulous!", "Ooh, so fancy!", "I love it!", "Pip-tastic style!").random()
        } else {
            app.play(Sfx.WRONG)
            mood = PipMood.OOPS
            line = "Not yet! Win ${item.lockText} to unlock ${item.label}."
        }
        app.say(line)
    }

    Box(Modifier.fillMaxSize()) {
        FunBackground(Color(0xFFB388FF))
        Column(Modifier.fillMaxSize()) {
            TopBar("Pip's Closet 👕", onBack = onBack)
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfilePip(profile, Modifier.size(150.dp), mood = mood)
                    SpeechBubble(line, Modifier.weight(1f), size = 16.sp)
                }
                FunText(
                    "3-star wins: $gold  •  Stars: ${profile.totalStars}",
                    15.sp,
                    NumColors.InkSoft,
                    weight = FontWeight.Bold,
                )
                Gap(10.dp)
                ClosetSection("🎨 Colours", colours, ::tap)
                ClosetSection("🎩 Hats (earned with stars)", hats, ::tap)
                ClosetSection("🕶️ Glasses", glasses, ::tap)
                ClosetSection("🎀 Neckwear", necks, ::tap)
                Gap(20.dp)
            }
        }
    }
}

@Composable
private fun ClosetSection(title: String, items: List<ClosetItem>, onTap: (ClosetItem) -> Unit) {
    FunText(title, 20.sp, NumColors.Ink, Modifier.fillMaxWidth(), align = TextAlign.Start)
    Gap(8.dp)
    items.chunked(3).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            row.forEach { item ->
                BouncyBox(
                    color = if (item.unlocked) Color.White else NumColors.Locked.copy(alpha = 0.55f),
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.9f)
                        .then(
                            if (item.selected) Modifier.border(4.dp, NumColors.Ink, RoundedCornerShape(22.dp))
                            else Modifier
                        ),
                    onClick = { onTap(item) },
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            item.preview()
                            if (!item.unlocked) FunText("🔒", 26.sp)
                        }
                        FunText(item.label, 12.sp, NumColors.Ink, maxLines = 1)
                        if (!item.unlocked) {
                            FunText(item.lockText, 10.sp, NumColors.InkSoft, weight = FontWeight.Bold, maxLines = 2)
                        } else if (item.selected) {
                            FunText("Wearing ✓", 11.sp, NumColors.Good)
                        }
                    }
                }
            }
            repeat(3 - row.size) { Box(Modifier.weight(1f)) }
        }
        Gap(10.dp)
    }
    Gap(6.dp)
}

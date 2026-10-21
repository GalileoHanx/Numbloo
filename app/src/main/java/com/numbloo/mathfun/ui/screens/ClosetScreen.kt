package com.numbloo.mathfun.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import com.numbloo.mathfun.model.PipColors
import com.numbloo.mathfun.model.Profile
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

/** One thing Pip can wear. */
private class ClosetItem(
    val label: String,
    val unlocked: Boolean,
    val lockText: String,
    val selected: Boolean,
    val preview: @Composable () -> Unit,
    val apply: (Profile) -> Profile,
)

private enum class Shelf(val title: String) {
    COLOURS("🎨 Colours"),
    FLAGS("🚩 Flags"),
    HAIR("💇 Hair"),
    JOBS("👷 Jobs"),
    EYES("👀 Eyes"),
    MOUTH("👄 Mouth"),
    HATS("🎩 Hats"),
    GLASSES("🕶️ Glasses"),
    NECK("🎀 Neck"),
}

/** Pip's Closet: opens after the first 3-star game; more items unlock with more 3-star wins. */
@Composable
fun ClosetScreen(app: AppState, profile: Profile, onBack: () -> Unit) {
    var line by remember { mutableStateOf("Ooh, my closet! Pick a shelf, then tap something for me to wear.") }
    var mood by remember { mutableStateOf(PipMood.HAPPY) }
    var shelf by remember { mutableStateOf(Shelf.FLAGS) }
    LaunchedEffect(Unit) { app.say(line) }

    val gold = profile.goldWins
    val base = profile.pipLook()

    /** A still preview of Pip wearing one changed thing. */
    fun preview(look: PipLook): @Composable () -> Unit = {
        LookPip(look, Modifier.size(66.dp), animated = false)
    }

    fun flagPreview(emoji: String): @Composable () -> Unit = {
        Text(emoji, fontSize = 44.sp)
    }

    fun goldLock(need: Int) = "${need - gold} more 3⭐ wins"

    val items: List<ClosetItem> = when (shelf) {
        Shelf.COLOURS -> PipColors.names.indices.map { i ->
            ClosetItem(
                label = PipColors.names[i],
                unlocked = gold >= PipColors.goldNeeded[i],
                lockText = goldLock(PipColors.goldNeeded[i]),
                selected = profile.colorIndex == i,
                preview = {
                    Box(
                        Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(NumColors.pip(i))
                    )
                },
                apply = { it.copy(colorIndex = i) },
            )
        }
        Shelf.FLAGS -> Flag.entries.map { f ->
            ClosetItem(
                label = f.label,
                unlocked = gold >= f.goldNeeded,
                lockText = goldLock(f.goldNeeded),
                selected = profile.flag == f,
                preview = if (f == Flag.NONE) preview(base.copy(flag = f)) else flagPreview(f.emoji),
                apply = { it.copy(flag = f) },
            )
        }
        Shelf.HAIR -> Hair.entries.map { h ->
            ClosetItem(h.label, gold >= h.goldNeeded, goldLock(h.goldNeeded), profile.hair == h,
                preview(base.copy(hair = h, hat = Hat.NONE, job = Job.NONE))) { it.copy(hair = h) }
        }
        Shelf.JOBS -> Job.entries.map { j ->
            ClosetItem(j.label, gold >= j.goldNeeded, goldLock(j.goldNeeded), profile.job == j,
                preview(base.copy(job = j))) { it.copy(job = j) }
        }
        Shelf.EYES -> Eyes.entries.map { e ->
            ClosetItem(e.label, gold >= e.goldNeeded, goldLock(e.goldNeeded), profile.eyes == e,
                preview(base.copy(eyes = e, glasses = Glasses.NONE))) { it.copy(eyes = e) }
        }
        Shelf.MOUTH -> Mouth.entries.map { m ->
            ClosetItem(m.label, gold >= m.goldNeeded, goldLock(m.goldNeeded), profile.mouth == m,
                preview(base.copy(mouth = m))) { it.copy(mouth = m) }
        }
        Shelf.HATS -> Hat.entries.map { h ->
            ClosetItem(h.label, profile.totalStars >= h.starsNeeded, "${h.starsNeeded - profile.totalStars} more ⭐",
                profile.hat == h && profile.job == Job.NONE,
                preview(base.copy(hat = h, job = Job.NONE, hair = Hair.NONE))) { it.copy(hatChoice = h.name, job = Job.NONE) }
        }
        Shelf.GLASSES -> Glasses.entries.map { g ->
            ClosetItem(g.label, gold >= g.goldNeeded, goldLock(g.goldNeeded), profile.glasses == g,
                preview(base.copy(glasses = g))) { it.copy(glasses = g) }
        }
        Shelf.NECK -> Neck.entries.map { n ->
            ClosetItem(n.label, gold >= n.goldNeeded, goldLock(n.goldNeeded), profile.neck == n,
                preview(base.copy(neck = n))) { it.copy(neck = n) }
        }
    }

    fun tap(item: ClosetItem) {
        if (item.unlocked) {
            app.updateProfile(profile.id, item.apply)
            app.play(Sfx.POP)
            mood = PipMood.HAPPY
            line = listOf("Looking fabulous!", "Ooh, so fancy!", "I love it!", "Pip-tastic style!", "Wow, is that me?").random()
        } else {
            app.play(Sfx.WRONG)
            mood = PipMood.OOPS
            line = "Not yet! Get ${item.lockText} to unlock ${item.label}."
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
                ProfilePip(profile, Modifier.size(140.dp), mood = mood)
                SpeechBubble(line, Modifier.weight(1f), size = 15.sp)
            }
            FunText(
                "3-star wins: $gold  •  Stars: ${profile.totalStars}",
                14.sp,
                NumColors.InkSoft,
                Modifier.fillMaxWidth(),
                weight = FontWeight.Bold,
            )
            Gap(8.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Shelf.entries.forEach { sh ->
                    val sel = sh == shelf
                    BouncyButton(
                        sh.title,
                        if (sel) NumColors.Ink else Color.White,
                        Modifier.width(118.dp),
                        textColor = if (sel) Color.White else NumColors.Ink,
                        fontSize = 14.sp,
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
                items.chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { item ->
                            ClosetTile(item, Modifier.weight(1f)) { tap(item) }
                        }
                        repeat(3 - row.size) { Box(Modifier.weight(1f)) }
                    }
                    Gap(10.dp)
                }
                Gap(20.dp)
            }
        }
    }
}

@Composable
private fun ClosetTile(item: ClosetItem, modifier: Modifier, onClick: () -> Unit) {
    BouncyBox(
        color = if (item.unlocked) Color.White else NumColors.Locked.copy(alpha = 0.55f),
        modifier = modifier
            .aspectRatio(0.85f)
            .then(
                if (item.selected) Modifier.border(4.dp, NumColors.Ink, RoundedCornerShape(22.dp))
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

package com.numbloo.mathfun.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numbloo.mathfun.AppState
import com.numbloo.mathfun.audio.Sfx
import com.numbloo.mathfun.model.Profile
import com.numbloo.mathfun.model.Topic
import com.numbloo.mathfun.ui.components.BouncyButton
import com.numbloo.mathfun.ui.components.FunText
import com.numbloo.mathfun.ui.components.Gap
import com.numbloo.mathfun.ui.components.Panel
import com.numbloo.mathfun.ui.components.Pip
import com.numbloo.mathfun.ui.components.PipMood
import com.numbloo.mathfun.ui.components.TopBar
import com.numbloo.mathfun.ui.theme.NumColors
import kotlin.random.Random

/** Grown-ups type a number written in words. Easy for adults, hard for little ones. */
@Composable
fun ParentGateScreen(app: AppState, onPass: () -> Unit, onBack: () -> Unit) {
    val target = remember { Random.nextInt(120, 990) }
    var typed by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TopBar("Grown-ups only", onBack = onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Pip(Modifier.size(110.dp), mood = PipMood.THINK)
            Gap(10.dp)
            FunText("Please type this number in digits:", 18.sp, NumColors.InkSoft, weight = FontWeight.Bold)
            Gap(8.dp)
            FunText(numberToWords(target), 26.sp, NumColors.Ink)
            Gap(16.dp)
            OutlinedTextField(
                value = typed,
                onValueChange = { v ->
                    typed = v.filter { it.isDigit() }.take(4)
                    wrong = false
                },
                singleLine = true,
                textStyle = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            if (wrong) {
                Gap(6.dp)
                FunText("Not quite, try again.", 16.sp, NumColors.Bad)
            }
            Gap(16.dp)
            BouncyButton("Enter", NumColors.Ink, Modifier.fillMaxWidth()) {
                if (typed == target.toString()) {
                    app.play(Sfx.TAP)
                    onPass()
                } else {
                    wrong = true
                    app.play(Sfx.WRONG)
                }
            }
        }
    }
}

fun numberToWords(n: Int): String {
    val ones = listOf(
        "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
        "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
        "seventeen", "eighteen", "nineteen",
    )
    val tens = listOf("", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety")
    fun under100(x: Int): String = when {
        x < 20 -> ones[x]
        x % 10 == 0 -> tens[x / 10]
        else -> tens[x / 10] + "-" + ones[x % 10]
    }
    if (n < 100) return under100(n)
    val h = n / 100
    val r = n % 100
    val rest = if (r == 0) "" else " and " + under100(r)
    return ones[h] + " hundred" + rest
}

@Composable
fun ParentsScreen(app: AppState, onBack: () -> Unit) {
    var confirm by remember { mutableStateOf<Pair<String, Profile>?>(null) }

    Column(Modifier.fillMaxSize()) {
        TopBar("Grown-ups", onBack = onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Panel(Modifier.fillMaxWidth()) {
                FunText("Settings", 20.sp, NumColors.Ink, Modifier.fillMaxWidth(), align = TextAlign.Start)
                Gap(6.dp)
                SettingRow("Read-aloud voice", app.voiceOn) { app.setVoice(it) }
                SettingRow("Sound effects", app.soundOn) { app.setSound(it) }
                Gap(4.dp)
                BouncyButton("🔊 Test the voice", Color.White, Modifier.fillMaxWidth(), textColor = NumColors.Ink, fontSize = 16.sp, height = 50.dp) {
                    app.say("Hello! I'm Pip. Let's do some math!")
                }
                Gap(8.dp)
                FunText(
                    "The voice uses your phone's built-in text-to-speech, so it works offline. " +
                        "If you hear nothing, check Settings › Accessibility › Text-to-speech on your phone. " +
                        "Numbloo has no internet, camera or microphone access, and all progress stays on this phone.",
                    13.sp,
                    NumColors.InkSoft,
                    align = TextAlign.Start,
                    weight = FontWeight.Normal,
                )
            }
            Gap(14.dp)
            FunText("Players", 20.sp, NumColors.Ink, Modifier.fillMaxWidth(), align = TextAlign.Start)
            Gap(8.dp)
            if (app.profiles.isEmpty()) {
                FunText("No players yet.", 16.sp, NumColors.InkSoft)
            }
            app.profiles.forEach { p ->
                PlayerReport(
                    p = p,
                    onAgeChange = { app.setAge(p.id, it) },
                    onReset = { confirm = "reset" to p },
                    onDelete = { confirm = "delete" to p },
                )
                Gap(12.dp)
            }
            Gap(20.dp)
        }
    }

    val c = confirm
    if (c != null) {
        val (action, p) = c
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text(if (action == "delete") "Delete ${p.name}?" else "Reset ${p.name}'s progress?") },
            text = {
                Text(
                    if (action == "delete") "This removes the player and all their stars, stickers and records."
                    else "Stars, stickers and records go back to zero. The player stays."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (action == "delete") app.deleteProfile(p.id) else app.resetProgress(p.id)
                    confirm = null
                }) { Text(if (action == "delete") "Delete" else "Reset", color = NumColors.Bad) }
            },
            dismissButton = {
                TextButton(onClick = { confirm = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SettingRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FunText(label, 17.sp, NumColors.Ink, Modifier.weight(1f), align = TextAlign.Start, weight = FontWeight.Bold)
        Switch(checked = value, onCheckedChange = onChange)
    }
}

@Composable
private fun PlayerReport(
    p: Profile,
    onAgeChange: (Int) -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit,
) {
    Panel(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Pip(Modifier.size(64.dp), color = NumColors.pip(p.colorIndex), hat = p.hat)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                FunText(p.name, 22.sp, NumColors.Ink, align = TextAlign.Start, maxLines = 1)
                FunText(
                    "⭐ ${p.totalStars} stars  •  🏷️ ${p.stickers.size} stickers  •  Pip's ${p.hat.label}",
                    13.sp,
                    NumColors.InkSoft,
                    align = TextAlign.Start,
                    weight = FontWeight.Bold,
                )
            }
        }
        Gap(8.dp)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FunText("Age", 16.sp, NumColors.Ink, Modifier.weight(1f), align = TextAlign.Start)
            BouncyButton("−", Color.White, Modifier.size(48.dp), textColor = NumColors.Ink, height = 48.dp) {
                onAgeChange(p.age - 1)
            }
            FunText("${p.age}", 22.sp, NumColors.Ink, Modifier.width(48.dp))
            BouncyButton("+", Color.White, Modifier.size(48.dp), textColor = NumColors.Ink, height = 48.dp) {
                onAgeChange(p.age + 1)
            }
        }
        Gap(8.dp)
        // Per-topic report
        Row(Modifier.fillMaxWidth()) {
            FunText("Topic", 13.sp, NumColors.InkSoft, Modifier.weight(1.6f), align = TextAlign.Start)
            FunText("Lesson", 13.sp, NumColors.InkSoft, Modifier.weight(1f))
            FunText("Games", 13.sp, NumColors.InkSoft, Modifier.weight(1f))
            FunText("Right", 13.sp, NumColors.InkSoft, Modifier.weight(1f))
            FunText("Level", 13.sp, NumColors.InkSoft, Modifier.weight(1.1f))
        }
        Topic.entries.forEach { t ->
            val s = p.stats(t)
            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                FunText("${t.symbol} ${t.title}", 14.sp, t.color, Modifier.weight(1.6f), align = TextAlign.Start, maxLines = 1)
                FunText(if (s.lessonDone) "✓" else "–", 15.sp, NumColors.Ink, Modifier.weight(1f))
                FunText("${s.gamesPlayed}", 15.sp, NumColors.Ink, Modifier.weight(1f))
                FunText(if (s.attempts == 0) "–" else "${s.accuracyPercent}%", 15.sp, NumColors.Ink, Modifier.weight(1f))
                FunText(s.tierName, 13.sp, NumColors.Ink, Modifier.weight(1.1f))
            }
        }
        Gap(10.dp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BouncyButton("Reset", Color.White, Modifier.weight(1f), textColor = NumColors.InkSoft, fontSize = 16.sp, height = 48.dp) { onReset() }
            BouncyButton("Delete", NumColors.Bad, Modifier.weight(1f), fontSize = 16.sp, height = 48.dp) { onDelete() }
        }
    }
}

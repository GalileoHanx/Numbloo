package com.numbloo.mathfun.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numbloo.mathfun.AppState
import com.numbloo.mathfun.audio.Sfx
import com.numbloo.mathfun.model.PipColors
import com.numbloo.mathfun.model.Profile
import com.numbloo.mathfun.ui.components.BouncyBox
import com.numbloo.mathfun.ui.components.BouncyButton
import com.numbloo.mathfun.ui.components.FunBackground
import com.numbloo.mathfun.ui.components.FunText
import com.numbloo.mathfun.ui.components.Gap
import com.numbloo.mathfun.ui.components.Pip
import com.numbloo.mathfun.ui.components.PipMood
import com.numbloo.mathfun.ui.components.ProfilePip
import com.numbloo.mathfun.ui.components.SpeechBubble
import com.numbloo.mathfun.ui.components.TopBar
import com.numbloo.mathfun.ui.theme.NumColors

@Composable
fun ProfilesScreen(
    app: AppState,
    onPick: (String) -> Unit,
    onNew: () -> Unit,
    onParents: () -> Unit,
) {
    LaunchedEffect(Unit) {
        app.say(if (app.profiles.isEmpty()) "Hi! I'm Pip! Let's make your player." else "Hi! I'm Pip! Who's playing today?")
    }
    Box(Modifier.fillMaxSize()) {
        FunBackground(NumColors.Grass)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Gap(8.dp)
            FunText("Numbloo", 46.sp, NumColors.Ink)
            FunText("Math snacks with Pip!", 18.sp, NumColors.InkSoft, weight = FontWeight.Bold)
            Gap(8.dp)
            Pip(Modifier.size(170.dp), mood = PipMood.HAPPY)
            Gap(8.dp)
            FunText(if (app.profiles.isEmpty()) "Let's make your player!" else "Who's playing?", 26.sp)
            Gap(16.dp)

            app.profiles.forEach { p ->
                ProfileCard(p) {
                    app.play(Sfx.TAP)
                    onPick(p.id)
                }
                Gap(12.dp)
            }

            BouncyButton(
                text = "➕  New player",
                color = NumColors.Grass,
                modifier = Modifier.fillMaxWidth(),
                height = 70.dp,
            ) {
                app.play(Sfx.TAP)
                onNew()
            }
            Gap(28.dp)
            BouncyButton(
                text = "👪  Grown-ups",
                color = Color.White,
                textColor = NumColors.InkSoft,
                fontSize = 16.sp,
                height = 48.dp,
                modifier = Modifier.width(190.dp),
            ) { onParents() }
            Gap(12.dp)
        }
    }
}

@Composable
private fun ProfileCard(p: Profile, onClick: () -> Unit) {
    BouncyBox(color = Color.White, modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfilePip(p, Modifier.size(72.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                FunText(p.name, 24.sp, NumColors.Ink, maxLines = 1)
                FunText("Age ${p.age}  •  ⭐ ${p.totalStars}  •  🏷️ ${p.stickers.size}", 15.sp, NumColors.InkSoft, weight = FontWeight.Bold)
            }
            FunText("▶", 28.sp, NumColors.Grass)
        }
    }
}

@Composable
fun NewProfileScreen(
    app: AppState,
    onCreated: (Profile) -> Unit,
    onBack: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableIntStateOf(6) }
    var color by remember { mutableIntStateOf(app.profiles.size % PipColors.FREE) }

    LaunchedEffect(Unit) { app.say("What's your name? And how old are you?") }

    Column(Modifier.fillMaxSize()) {
        TopBar("New player", onBack = onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pip(Modifier.size(110.dp), mood = PipMood.WOW, color = NumColors.pip(color))
                SpeechBubble(
                    if (name.isBlank()) "Ooh, a new friend! What's your name?" else "Nice to meet you, ${name.trim()}!",
                    Modifier.weight(1f),
                )
            }
            Gap(12.dp)
            FunText("Your name", 20.sp, align = TextAlign.Start, modifier = Modifier.fillMaxWidth())
            Gap(6.dp)
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 14) name = it },
                singleLine = true,
                textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NumColors.Ink),
                placeholder = { Text("Type a name", fontSize = 20.sp) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Gap(18.dp)
            FunText("How old are you?", 20.sp, align = TextAlign.Start, modifier = Modifier.fillMaxWidth())
            Gap(8.dp)
            listOf(4..7, 8..10).forEach { range ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (a in range) {
                        val selected = a == age
                        BouncyButton(
                            text = "$a",
                            color = if (selected) NumColors.Ink else Color.White,
                            textColor = if (selected) Color.White else NumColors.Ink,
                            fontSize = 26.sp,
                            height = 60.dp,
                            modifier = Modifier.weight(1f),
                        ) {
                            age = a
                            app.play(Sfx.TAP)
                        }
                    }
                    if (range.last == 10) Spacer(Modifier.weight(1f))
                }
                Gap(10.dp)
            }

            Gap(8.dp)
            FunText("Pick Pip's colour", 20.sp, align = TextAlign.Start, modifier = Modifier.fillMaxWidth())
            Gap(8.dp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                NumColors.PipPalette.take(PipColors.FREE).forEachIndexed { i, c ->
                    Box(
                        Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(c)
                            .then(if (i == color) Modifier.border(4.dp, NumColors.Ink, CircleShape) else Modifier)
                            .clickable {
                                color = i
                                app.play(Sfx.POP)
                            }
                    )
                }
            }

            Gap(24.dp)
            BouncyButton(
                text = "Let's go! 🚀",
                color = NumColors.Grass,
                enabled = name.isNotBlank(),
                height = 72.dp,
                fontSize = 26.sp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                app.play(Sfx.CELEBRATE)
                val p = app.addProfile(name, age, color)
                app.say("Yay! Welcome, ${p.name}!")
                onCreated(p)
            }
            Gap(24.dp)
        }
    }
}

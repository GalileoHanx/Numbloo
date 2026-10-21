package com.numbloo.mathfun.ui.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numbloo.mathfun.audio.Sfx
import com.numbloo.mathfun.ui.components.BouncyButton
import com.numbloo.mathfun.ui.components.pulseScale
import com.numbloo.mathfun.ui.screens.AnswerFn
import com.numbloo.mathfun.ui.screens.GameCtx
import com.numbloo.mathfun.ui.theme.NumColors

/** Scrollable, centred column every game sits in (keeps small phones happy). */
@Composable
fun GameColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content,
    )
}

/**
 * Big answer buttons. Wrong picks turn grey; after two misses the right one wiggles.
 */
@Composable
fun ChoiceRow(
    choices: List<Int>,
    answer: Int,
    attempts: Int,
    ctx: GameCtx,
    onAnswer: AnswerFn,
    modifier: Modifier = Modifier,
    correctLine: String? = null,
    onCorrect: () -> Unit = {},
) {
    val wrong = remember(choices) { mutableStateListOf<Int>() }
    val glow = pulseScale(attempts >= 2)
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        choices.forEach { c ->
            val isWrong = c in wrong
            val isGlow = attempts >= 2 && c == answer
            BouncyButton(
                text = c.toString(),
                color = if (isWrong) NumColors.Locked else ctx.color,
                enabled = !isWrong,
                fontSize = 34.sp,
                height = 78.dp,
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        val s = if (isGlow) glow else 1f
                        scaleX = s
                        scaleY = s
                    },
            ) {
                if (c == answer) {
                    onCorrect()
                    onAnswer(true, correctLine)
                } else {
                    ctx.sfx(Sfx.TAP)
                    wrong.add(c)
                    onAnswer(false, null)
                }
            }
        }
    }
}

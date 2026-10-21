package com.numbloo.mathfun.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numbloo.mathfun.ui.theme.NumColors

/** Emoji that pop in one after another. Items from [crossFrom] onward are "taken away". */
@Composable
fun PoppingItems(
    count: Int,
    emoji: String,
    startDelay: Int = 0,
    perRow: Int = 5,
    size: TextUnit = 34.sp,
    crossFrom: Int = Int.MAX_VALUE,
    numbered: Boolean = false,
    numberOffset: Int = 0,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        (0 until count).chunked(perRow).forEach { row ->
            Row {
                row.forEach { i ->
                    PopIn(startDelay + i * 160) {
                        Column(
                            Modifier.padding(3.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(emoji, fontSize = size, color = if (i >= crossFrom) Color.Gray else Color.Unspecified)
                                if (i >= crossFrom) FunText("✕", size * 0.9f, NumColors.Bad)
                            }
                            if (numbered) FunText("${i + 1 + numberOffset}", 16.sp, NumColors.InkSoft)
                        }
                    }
                }
            }
        }
    }
}

/** "3 + 2 = 5" in big colourful letters. */
@Composable
fun BigEquation(text: String, color: Color, size: TextUnit = 52.sp) {
    PopIn {
        FunText(text, size, color)
    }
}

/** Equal groups, e.g. 3 plates with 2 cookies each. Optional running totals under each group. */
@Composable
fun GroupsVisual(
    groups: Int,
    per: Int,
    emoji: String,
    color: Color,
    runningTotals: Boolean = false,
    itemSize: TextUnit = 22.sp,
    label: ((Int) -> String)? = null,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        (0 until groups).chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.Center) {
                row.forEach { g ->
                    PopIn(g * 220) {
                        Column(
                            Modifier.padding(5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                Modifier
                                    .widthIn(min = 76.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(color.copy(alpha = 0.15f))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                EmojiGrid(per, emoji, perRow = 3, size = itemSize)
                            }
                            when {
                                label != null -> FunText(label(g), 15.sp, color)
                                runningTotals -> FunText("${(g + 1) * per}", 18.sp, color)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** A small number line with a frog hopping back. */
@Composable
fun MiniNumberLine(from: Int, back: Int, color: Color) {
    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
        for (n in 0..from) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 2.dp)) {
                val label = when {
                    n == from -> "🐸"
                    n == from - back -> "⬇️"
                    n > from - back -> "↩️"
                    else -> ""
                }
                Text(label, fontSize = 18.sp)
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (n == from - back) color else NumColors.Lily),
                    contentAlignment = Alignment.Center,
                ) {
                    FunText("$n", 15.sp, Color.White)
                }
            }
        }
    }
}

/** Friends sharing items fairly. */
@Composable
fun ShareVisual(total: Int, friends: Int, emoji: String, color: Color) {
    val faces = listOf("🐻", "🐰", "🐼", "🐨", "🦊")
    val each = if (friends > 0) total / friends else 0
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      (0 until friends).chunked(3).forEach { row ->
       Row(horizontalArrangement = Arrangement.Center) {
        for (f in row) {
            PopIn(f * 300) {
                Column(
                    Modifier.padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(faces[f % faces.size], fontSize = 40.sp)
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(color.copy(alpha = 0.15f))
                            .padding(8.dp)
                    ) {
                        EmojiGrid(each, emoji, perRow = 3, size = 22.sp)
                    }
                    FunText("$each", 18.sp, color)
                }
            }
        }
       }
      }
    }
}

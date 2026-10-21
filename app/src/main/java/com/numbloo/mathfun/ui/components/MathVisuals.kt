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
import androidx.compose.ui.text.font.FontWeight
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

/* ---------------------------------------------------------------- More lesson pictures */

/** A ten frame: 2 rows of 5 boxes. [first] dots in one colour, then [second] in another. */
@Composable
fun TenFrame(first: Int, second: Int, color: Color, secondColor: Color = NumColors.Sun) {
    Column(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NumColors.Ink)
            .padding(4.dp),
    ) {
        for (row in 0 until 2) {
            Row {
                for (col in 0 until 5) {
                    val i = row * 5 + col
                    Box(
                        Modifier
                            .padding(2.dp)
                            .size(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                        val c = when {
                            i < first -> color
                            i < first + second -> secondColor
                            else -> null
                        }
                        if (c != null) {
                            PopIn(i * 90) {
                                Box(
                                    Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(c)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Tens rods and ones cubes, e.g. 23 = 2 tens and 3 ones. */
@Composable
fun TensOnes(n: Int, color: Color, label: Boolean = true) {
    val tens = n / 10
    val ones = n % 10
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
            for (t in 0 until tens) {
                PopIn(t * 150) {
                    Column(Modifier.padding(horizontal = 3.dp)) {
                        for (k in 0 until 10) {
                            Box(
                                Modifier
                                    .padding(0.5.dp)
                                    .size(width = 18.dp, height = 11.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(color)
                            )
                        }
                    }
                }
            }
            if (tens > 0 && ones > 0) Box(Modifier.size(14.dp))
            Column {
                (0 until ones).chunked(2).forEach { pair ->
                    Row {
                        pair.forEach { k ->
                            PopIn(tens * 150 + k * 100) {
                                Box(
                                    Modifier
                                        .padding(1.dp)
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(NumColors.Sun)
                                )
                            }
                        }
                    }
                }
            }
        }
        if (label) FunText("$tens tens and $ones ones = $n", 18.sp, NumColors.InkSoft)
    }
}

/** A sum written in columns, with an optional little carried/borrowed note on top. */
@Composable
fun ColumnMath(top: Int, bottom: Int, op: String, result: Int?, color: Color, note: String? = null) {
    val mono = androidx.compose.ui.text.font.FontFamily.Monospace
    val width = maxOf(top.toString().length, bottom.toString().length, (result ?: 0).toString().length) + 2
    fun pad(s: String) = s.padStart(width, ' ')
    PopIn {
        Column(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.End,
        ) {
            if (note != null) {
                Text(pad(note), fontFamily = mono, fontSize = 20.sp, color = NumColors.Bad, fontWeight = FontWeight.Bold)
            }
            Text(pad("$top"), fontFamily = mono, fontSize = 38.sp, color = NumColors.Ink, fontWeight = FontWeight.Bold)
            Text(op + pad("$bottom").drop(1), fontFamily = mono, fontSize = 38.sp, color = NumColors.Ink, fontWeight = FontWeight.Bold)
            Box(
                Modifier
                    .padding(vertical = 2.dp)
                    .size(width = (width * 24).dp, height = 4.dp)
                    .background(NumColors.Ink)
            )
            Text(
                pad(result?.toString() ?: "?"),
                fontFamily = mono,
                fontSize = 38.sp,
                color = color,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/** Rows and columns of things, e.g. 3 rows of 4. */
@Composable
fun ArrayVisual(rows: Int, cols: Int, emoji: String, color: Color, rowTotals: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for (r in 0 until rows) {
            PopIn(r * 200) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(cols) { Text(emoji, fontSize = 28.sp, modifier = Modifier.padding(2.dp)) }
                    if (rowTotals) FunText("  ${(r + 1) * cols}", 18.sp, color)
                }
            }
        }
    }
}

/** Number line with forward hops of [by], [steps] times, starting at [from]. */
@Composable
fun HopLine(from: Int, by: Int, steps: Int, color: Color) {
    val end = from + by * steps
    val labels = (0..steps).map { from + it * by }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            labels.forEachIndexed { i, v ->
                PopIn(i * 250) {
                    Box(
                        Modifier
                            .padding(2.dp)
                            .size(if (v >= 100) 46.dp else 40.dp)
                            .clip(CircleShape)
                            .background(if (v == end) color else NumColors.Lily),
                        contentAlignment = Alignment.Center,
                    ) {
                        FunText("$v", if (v >= 100) 13.sp else 15.sp, Color.White)
                    }
                }
                if (i < labels.lastIndex) FunText("➜", 14.sp, NumColors.InkSoft)
            }
        }
        FunText("+$by each hop", 16.sp, NumColors.InkSoft)
    }
}

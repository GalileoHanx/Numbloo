package com.numbloo.mathfun.ui.games

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Holds a value without causing the screen to redraw (used for measured positions). */
class Holder<T>(var v: T)

/** Remembers where this element is on screen so things can be dropped on it. */
fun Modifier.dropZone(holder: Holder<Rect>): Modifier =
    this.onGloballyPositioned { holder.v = it.boundsInRoot() }

/** A game area that never scrolls, so dragging and steering never fight with scrolling. */
@Composable
fun PlayColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

/**
 * Runs [step] once per screen frame with the seconds since the last frame,
 * until the game leaves the screen. Used by the arcade games.
 */
suspend fun gameLoop(step: (dt: Float) -> Unit) {
    var last = withFrameNanos { it }
    while (true) {
        val now = withFrameNanos { it }
        val dt = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.05f)
        last = now
        step(dt)
    }
}

/**
 * Something the child can pick up and drag. When let go, [onDrop] gets the
 * on-screen centre of the item. Return true if it landed somewhere useful;
 * otherwise the item springs back home.
 */
@Composable
fun DragItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDragStart: () -> Unit = {},
    onDrop: (Offset) -> Boolean,
    content: @Composable () -> Unit,
) {
    val home = remember { Holder(Rect.Zero) }
    var drag by remember { mutableStateOf(Offset.Zero) }
    var dragging by remember { mutableStateOf(false) }
    val dropFn by rememberUpdatedState(onDrop)
    val startFn by rememberUpdatedState(onDragStart)
    val scope = rememberCoroutineScope()

    Box(
        modifier
            .zIndex(if (dragging) 10f else 1f)
            .onGloballyPositioned { home.v = it.boundsInRoot() }
            .offset { IntOffset(drag.x.roundToInt(), drag.y.roundToInt()) }
            .graphicsLayer {
                val s = if (dragging) 1.18f else 1f
                scaleX = s
                scaleY = s
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragStart = {
                        dragging = true
                        startFn()
                    },
                    onDragEnd = {
                        dragging = false
                        val from = drag
                        val accepted = dropFn(home.v.center + from)
                        if (accepted) {
                            drag = Offset.Zero
                        } else {
                            scope.launch {
                                animate(1f, 0f, animationSpec = tween(260)) { v, _ -> drag = from * v }
                            }
                        }
                    },
                    onDragCancel = {
                        dragging = false
                        drag = Offset.Zero
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        drag += amount
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

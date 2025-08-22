package com.warh.commons.scroll_utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Velocity

class HideOnScrollState internal constructor(
    val connection: NestedScrollConnection,
    private val maxHeightPxState: MutableState<Int>,
    private val offsetPxState: MutableState<Float>,
    private val settlingState: MutableState<Boolean>,
) {
    val offsetY: Float get() = offsetPxState.value
    val isSettling: Boolean get() = settlingState.value

    fun setMeasuredHeight(px: Int) { maxHeightPxState.value = px }
}

@Composable
fun rememberHideOnScrollState(): HideOnScrollState {
    val maxHeightPx = remember { mutableIntStateOf(0) }
    val offsetPx = remember { mutableFloatStateOf(0f) }
    val settling = remember { mutableStateOf(false) }

    val conn = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val dy = consumed.y
                val max = maxHeightPx.intValue.toFloat()
                offsetPx.floatValue = (offsetPx.floatValue - dy).coerceIn(0f, max)
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val max = maxHeightPx.intValue.toFloat()
                val mid = max / 2f
                settling.value = true
                offsetPx.floatValue = if (offsetPx.floatValue > mid) max else 0f
                settling.value = false
                return Velocity.Zero
            }
        }
    }
    return HideOnScrollState(conn, maxHeightPx, offsetPx, settling)
}

fun Modifier.collapseByPx(cutPx: Int, minHeightPx: Int = 0): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val cut = cutPx.coerceIn(0, placeable.height - minHeightPx)
        val newH = placeable.height - cut
        layout(placeable.width, newH) {
            placeable.placeRelative(0, 0)
        }
    }
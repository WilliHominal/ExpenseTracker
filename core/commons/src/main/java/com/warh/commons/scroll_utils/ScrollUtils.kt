package com.warh.commons.scroll_utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Velocity
import kotlin.math.roundToInt

class HideOnScrollState internal constructor(
    val connection: NestedScrollConnection,
    private val maxHeightPxState: MutableState<Int>,
    private val offsetPxState: MutableState<Float>,
) {
    val offsetY: Float get() = offsetPxState.value
    fun setMeasuredHeight(px: Int) { maxHeightPxState.value = px }
}

@Composable
fun rememberHideOnScrollState(): HideOnScrollState {
    val maxHeightPx = remember { mutableIntStateOf(0) }
    val offsetPx = remember { mutableFloatStateOf(0f) }

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
                offsetPx.floatValue = if (offsetPx.floatValue > mid) max else 0f
                return Velocity.Zero
            }
        }
    }
    return HideOnScrollState(conn, maxHeightPx, offsetPx)
}

fun Modifier.collapseBy(offsetY: Float, minHeightPx: Int = 0): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val cut = offsetY.roundToInt().coerceIn(0, placeable.height - minHeightPx)
        val newH = placeable.height - cut
        layout(placeable.width, newH) {
            placeable.placeRelative(0, 0)
        }
    }
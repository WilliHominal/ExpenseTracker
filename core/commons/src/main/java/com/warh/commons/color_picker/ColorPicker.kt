package com.warh.commons.color_picker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.warh.commons.R

@Composable
fun RingColorPickerDialog(
    initial: Color,
    onCancel: () -> Unit,
    onPick: (Color) -> Unit
) {
    val hsvInit = FloatArray(3).also { android.graphics.Color.colorToHSV(initial.toArgb(), it) }
    var hue by remember { mutableFloatStateOf(hsvInit[0]) }

    val preview = Color.hsv(hue, 1f, 1f)

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = { TextButton(onClick = { onPick(preview) }) { Text(stringResource(R.string.color_picker_ok)) } },
        dismissButton = { TextButton(onClick = onCancel) { Text(stringResource(R.string.color_picker_cancel)) } },
        title = { Text(stringResource(R.string.color_picker_title)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ColorHueWheel(
                    hue = hue,
                    onHueChange = { hue = it },
                    ringWidth = 28.dp,
                    diameter = 220.dp,
                    previewColor = preview
                )
            }
        }
    )
}

@Composable
private fun ColorHueWheel(
    hue: Float,
    onHueChange: (Float) -> Unit,
    ringWidth: Dp,
    diameter: Dp,
    previewColor: Color,
    modifier: Modifier = Modifier
) {
    val ringPx = with(LocalDensity.current) { ringWidth.toPx() }

    val sweepBrush = remember {
        val colorStops = (0..360).map { deg ->
            (deg / 360f) to Color.hsv(deg.toFloat(), 1f, 1f)
        }.toTypedArray()
        Brush.sweepGradient(colorStops = colorStops)
    }

    Canvas(
        modifier
            .size(diameter)
            .pointerInput(Unit) {
                fun handle(pos: Offset) {
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)
                    val outerR = kotlin.math.min(this.size.width, this.size.height) / 2f
                    val innerR = outerR - ringPx
                    val v = pos - center
                    if (v.getDistance() < innerR * 0.65f) return
                    val angle = Math.toDegrees(kotlin.math.atan2(v.y.toDouble(), v.x.toDouble()))
                    val deg = ((angle + 360.0) % 360.0).toFloat()
                    onHueChange(deg)
                }

                detectDragGestures(
                    onDragStart = { offset -> handle(offset) },
                    onDrag = { change, _ ->
                        handle(change.position)
                        change.consume()
                    }
                )
            }
    ) {
        val outerR = kotlin.math.min(size.width, size.height) / 2f - 4.dp.toPx()
        val innerR = outerR - ringPx

        drawCircle(
            brush = sweepBrush,
            radius = (innerR + outerR) / 2f,
            style = Stroke(width = ringPx)
        )

        drawCircle(color = previewColor, radius = innerR * 0.72f)

        val rad = Math.toRadians(hue.toDouble()).toFloat()
        val cx0 = size.width / 2f
        val cy0 = size.height / 2f
        val cx = cx0 + kotlin.math.cos(rad) * (innerR + outerR) / 2f
        val cy = cy0 + kotlin.math.sin(rad) * (innerR + outerR) / 2f

        drawCircle(color = Color.White, radius = 8.dp.toPx(), center = Offset(cx, cy))
        drawCircle(color = Color.Black, radius = 5.dp.toPx(), center = Offset(cx, cy))
    }
}

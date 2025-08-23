// DonutChart.kt
package com.warh.commons.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.warh.designsystem.charts.DonutChartDesign
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

data class DonutSlice(val key: String, val ratio: Float)

@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    stroke: Dp = DonutChartDesign.Dimensions.Stroke,
    gutter: Dp = DonutChartDesign.Dimensions.Gutter,
    gapDeg: Float = DonutChartDesign.Dimensions.GAP_DEG,
    selectedIndex: Int?,
    onSelectedIndexChange: (Int?) -> Unit,
    lastTapAngle: Float?,
    onLastTapAngleChange: (Float?) -> Unit,
    colorFor: (String) -> Color = DonutChartDesign.Colors.colorFor(),
    outlineColor: Color = DonutChartDesign.Colors.outline(),
    labelColor: Color = DonutChartDesign.Colors.label()
) {
    if (slices.isEmpty()) return
    val hasMultiple = slices.size > 1
    val progress = remember { Animatable(DonutChartDesign.Angles.ZERO) }
    LaunchedEffect(slices) {
        progress.snapTo(DonutChartDesign.Angles.ZERO)
        progress.animateTo(
            DonutChartDesign.Angles.FULL,
            tween(DonutChartDesign.Motion.ANIMATE_MS, DonutChartDesign.Motion.ANIMATE_DELAY, FastOutSlowInEasing)
        )
    }

    val density = LocalDensity.current
    val strokePx = with(density) { stroke.toPx() }
    val gutterPx = with(density) { gutter.toPx() }
    val lineW      = with(density) { DonutChartDesign.Dimensions.LineWidthDp.toPx() }
    val rayStartPx = with(density) { DonutChartDesign.Dimensions.RayStartDp.toPx() }
    val rayEndPx   = with(density) { DonutChartDesign.Dimensions.RayEndDp.toPx() }
    val elbowDxPx  = with(density) { DonutChartDesign.Dimensions.RayElbowDxDp.toPx() }
    val labelPx    = with(density) { DonutChartDesign.Typography.LabelSp.toPx() }
    val labelMargin= with(density) { DonutChartDesign.Dimensions.LabelMarginDp.toPx() }

    val labelPaint = remember(labelColor, labelPx) {
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = labelColor.toArgb()
            textSize = labelPx
        }
    }

    Canvas(
        modifier = modifier
            .height(DonutChartDesign.Layout.canvasHeight())
            .pointerInput(slices) {
            detectTapGestures { offset ->
                val w = size.width.toFloat()
                val h = size.height.toFloat()
                val cx = w / 2f
                val cy = h / 2f
                val ringDiameter = min(w, h - gutterPx * 2f)
                val outerR = ringDiameter / 2f
                val innerR = (outerR - strokePx).coerceAtLeast(0f)
                val dx = offset.x - cx
                val dy = offset.y - cy
                val r = hypot(dx, dy)
                if (r in innerR..outerR) {
                    var deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    if (deg < 0) deg += 360f
                    var acc = 0f
                    var found: Int? = null
                    for (i in slices.indices) {
                        val sweep = slices[i].ratio * 360f
                        if (deg < acc + sweep) { found = i; break }
                        acc += sweep
                    }
                    onSelectedIndexChange(found)
                    onLastTapAngleChange(deg)
                } else {
                    onSelectedIndexChange(null)
                    onLastTapAngleChange(null)
                }
            }
        }
    ) {
        val ringDiameter = min(size.width, size.height - gutterPx * 2f)
        val outerR = ringDiameter / 2f
        val radiusCenter = (outerR - strokePx / 2f).coerceAtLeast(0f)
        val arcSize = androidx.compose.ui.geometry.Size(radiusCenter * 2f, radiusCenter * 2f)
        val topLeft = Offset(center.x - radiusCenter, center.y - radiusCenter)

        var start = 0f
        var acc = 0f

        slices.forEachIndexed { idx, s ->
            val full = (s.ratio * 360f).coerceAtMost(360f)
            val remaining = (progress.value - acc).coerceAtLeast(0f)
            val sweep = remaining.coerceIn(0f, (full - if (hasMultiple) gapDeg else 0f).coerceAtLeast(0f))
            if (sweep > 0f) {
                val sel = idx == selectedIndex
                val width = strokePx * if (sel) DonutChartDesign.Motion.SELECTED_STROKE_GAIN else 1f
                if (!hasMultiple && sweep >= DonutChartDesign.Limits.ALMOST_FULL_DEG) {
                    drawCircle(
                        color = colorFor(s.key),
                        radius = radiusCenter,
                        style = Stroke(width = width, cap = DonutChartDesign.StrokeStyle.Cap)
                    )
                } else {
                    drawArc(
                        color = colorFor(s.key),
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = width, cap = DonutChartDesign.StrokeStyle.Cap)
                    )
                }
            }
            start += full
            acc += full
        }

        selectedIndex?.let { idx ->
            var startSel = 0f
            for (i in 0 until idx) startSel += (slices[i].ratio * 360f)
            val fullSel = (slices[idx].ratio * 360f)
            val sweepSel = (fullSel - if (hasMultiple) gapDeg else 0f).coerceAtLeast(0f)
            if (sweepSel > DonutChartDesign.Limits.MIN_LEGEND_SWEEP) {
                val anchorDeg = lastTapAngle?.let { tap ->
                    val end = startSel + sweepSel
                    if (tap in startSel..end) tap else startSel + sweepSel / 2f
                } ?: (startSel + sweepSel / 2f)

                val rad = Math.toRadians(anchorDeg.toDouble())
                val rayStart = outerR + rayStartPx
                val rayEnd   = outerR + rayEndPx

                val xEdge = center.x + outerR * cos(rad).toFloat()
                val yEdge = center.y + outerR * sin(rad).toFloat()
                val xRay  = center.x + rayStart * cos(rad).toFloat()
                val yRay  = center.y + rayStart * sin(rad).toFloat()
                val xRayE = center.x + rayEnd   * cos(rad).toFloat()
                val yRayE = center.y + rayEnd   * sin(rad).toFloat()

                drawLine(outlineColor, Offset(xEdge, yEdge), Offset(xRay, yRay), lineW)
                val toRight = cos(rad) >= 0
                val hx2 = if (toRight) xRayE + elbowDxPx else xRayE - elbowDxPx
                drawLine(outlineColor, Offset(xRay, yRay),   Offset(xRayE, yRayE), lineW)
                drawLine(outlineColor, Offset(xRayE, yRayE), Offset(hx2,  yRayE),  lineW)

                val legend = DonutChartDesign.Formatting.legend(slices[idx].ratio, slices[idx].key)
                val textW = labelPaint.measureText(legend)
                val xText = if (toRight) min(hx2 + labelMargin, size.width - textW - labelMargin)
                            else         max(hx2 - textW - labelMargin, labelMargin)
                val yText = (yRayE + labelPaint.textSize / 3f)
                    .coerceIn(labelPaint.textSize, size.height - labelMargin)
                drawContext.canvas.nativeCanvas.drawText(legend, xText, yText, labelPaint)
            }
        }
    }
}
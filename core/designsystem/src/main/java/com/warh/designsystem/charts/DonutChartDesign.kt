package com.warh.designsystem.charts

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object DonutChartDesign {
    object Dimensions {
        const val GAP_DEG: Float = 2.4f
        val Stroke: Dp = 32.dp
        val Gutter: Dp = 32.dp
        val RayStartDp: Dp = 2.dp
        val RayEndDp: Dp = 14.dp
        val RayElbowDxDp: Dp = 18.dp
        val LineWidthDp: Dp = 1.dp
        val LabelMarginDp: Dp = 4.dp
    }

    object Layout {
        private val RingHeight: Dp = 200.dp
        fun canvasHeight(gutters: Int = 2): Dp = RingHeight + Dimensions.Gutter * gutters
    }

    object Motion {
        const val ANIMATE_MS: Int = 1200
        const val ANIMATE_DELAY: Int = 80
        const val SELECTED_STROKE_GAIN: Float = 1.18f
    }

    object Typography {
        val LabelSp = 12.sp
    }

    object StrokeStyle {
        val Cap = StrokeCap.Butt
    }

    object Limits {
        const val ALMOST_FULL_DEG = 359.8f
        const val MIN_LEGEND_SWEEP = 1f
    }

    object Angles {
        const val FULL: Float = 360f
        const val ZERO: Float = 0f
    }

    object Colors {
        @Composable fun outline(): Color = MaterialTheme.colorScheme.outlineVariant
        @Composable fun label(): Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
        @Composable
        fun palette(): List<Color> = with(MaterialTheme.colorScheme) {
            listOf(primary, secondary, tertiary, primaryContainer, secondaryContainer, tertiaryContainer, inversePrimary, surfaceTint)
        }
        @Composable
        fun colorFor(): (String) -> Color {
            val p = palette()
            return remember(p) { { key -> p[(key.hashCode().ushr(1)) % p.size] } }
        }
    }

    object Formatting {
        private const val PERCENT_DECIMALS = 1
        fun legend(ratio: Float, key: String): String =
            "${"%.${PERCENT_DECIMALS}f".format(ratio * 100f)}%   $key"
    }
}
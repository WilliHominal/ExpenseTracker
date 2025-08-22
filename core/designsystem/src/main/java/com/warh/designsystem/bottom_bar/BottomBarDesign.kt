package com.warh.designsystem.bottom_bar

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object BottomBarDesign {

    object Sizes {
        val RowHeight   = 40.dp
        val PillWidth   = 52.dp
        val PillHeight  = 28.dp
        val IconButton  = 40.dp
        val CornerRadius   = 16.dp

        val PadStartEnd = 8.dp
        val PadTop      = 2.dp
        val PadBottom   = 2.dp
    }

    object Alpha {
        const val UNSELECTED_ICON: Float = 0.64f
        const val PILL_INITIAL_ALPHA: Float = 0.9f
        const val PILL_SCALE_INITIAL: Float = 0.8f
        const val PILL_SCALE_PEAK: Float = 1.2f
        const val PILL_SCALE_FINAL: Float = 1f
    }

    object Anim {
        const val SLIDE_MS: Int = 420
        const val PILL_SCALE_MS: Int = SLIDE_MS / 2
        val slideEasing = FastOutSlowInEasing
    }

    object Colors {
        @Composable
        fun containerColor(): Color = MaterialTheme.colorScheme.primaryContainer

        @Composable
        fun contentColor(): Color = MaterialTheme.colorScheme.onPrimaryContainer

        @Composable
        fun selectedIconColor(): Color = MaterialTheme.colorScheme.primary

        @Composable
        fun unselectedIconColor(): Color = MaterialTheme.colorScheme.onPrimary.copy(alpha = Alpha.UNSELECTED_ICON)

        @Composable
        fun indicatorColor(): Color = MaterialTheme.colorScheme.secondaryContainer
    }

    object Shapes {
        val indicatorShape = RoundedCornerShape(Sizes.CornerRadius)
    }

    object Calculations {
        fun calculatePillTargetX(
            selectedIndex: Int,
            slots: Int,
            contentWidthPx: Int,
            pillWidthPx: Int
        ): Int {
            if (contentWidthPx <= 0 || slots == 0) return 0
            val slotW = contentWidthPx / slots
            return (slotW * selectedIndex) + (slotW - pillWidthPx) / 2
        }
    }
}
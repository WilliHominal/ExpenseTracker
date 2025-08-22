package com.warh.designsystem.bottom_bar

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object BottomBarDesign {

    object Sizes {
        val RowHeight   = 40.dp   // antes 48.dp
        val PillWidth   = 52.dp   // un toque más angosto si querés
        val PillHeight  = 28.dp   // antes 36.dp
        val IconButton  = 40.dp   // antes 48.dp
        val CornerRadius   = 16.dp

        val PadStartEnd = 8.dp
        val PadTop      = 2.dp    // antes 8.dp
        val PadBottom   = 2.dp    // antes 4.dp
    }

    object Alpha {
        const val UNSELECTED_ICON: Float = 0.64f
    }

    object Anim {
        const val SLIDE_MS: Int = 220
        val slideEasing = FastOutSlowInEasing
    }

    object Colors {
        @Composable fun containerColor(): Color         = MaterialTheme.colorScheme.primaryContainer
        @Composable fun contentColor(): Color           = MaterialTheme.colorScheme.onPrimaryContainer
        @Composable fun selectedIconColor(): Color      = MaterialTheme.colorScheme.primary
        @Composable fun unselectedIconColor(): Color    = MaterialTheme.colorScheme.onPrimary.copy(alpha = Alpha.UNSELECTED_ICON)
        @Composable fun indicatorColor(): Color         = MaterialTheme.colorScheme.secondaryContainer
    }

    object Shapes {
        val indicatorShape = RoundedCornerShape(Sizes.CornerRadius)
    }
}
package com.warh.designsystem.bottom_bar

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object BottomBarDesign {
    private const val DISABLED_ALPHA = 0.38f
    private const val UNSELECTED_ALPHA = 0.72f

    @Composable
    fun containerColor(): Color = MaterialTheme.colorScheme.surfaceContainer

    @Composable
    fun contentColor(): Color = MaterialTheme.colorScheme.onPrimary.copy(alpha = UNSELECTED_ALPHA)

    @Composable
    fun itemColors(): NavigationBarItemColors {
        val cs = MaterialTheme.colorScheme
        val unselected = cs.surfaceVariant.copy(alpha = UNSELECTED_ALPHA)
        val disabled   = cs.onPrimary.copy(alpha = DISABLED_ALPHA)

        return NavigationBarItemDefaults.colors(
            indicatorColor      = cs.secondaryContainer,
            selectedIconColor   = cs.primary,
            selectedTextColor   = cs.onPrimary,
            unselectedIconColor = unselected,
            unselectedTextColor = unselected,
            disabledIconColor   = disabled,
            disabledTextColor   = disabled
        )
    }
}
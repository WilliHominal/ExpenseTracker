package com.warh.commons.bottom_bar

import androidx.compose.runtime.staticCompositionLocalOf
import com.warh.commons.scroll_utils.HideOnScrollState

val LocalBottomBarBehavior = staticCompositionLocalOf<HideOnScrollState?> { null }
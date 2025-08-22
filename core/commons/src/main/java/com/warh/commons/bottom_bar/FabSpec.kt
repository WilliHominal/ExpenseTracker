package com.warh.commons.bottom_bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
data class FabSpec(
    val visible: Boolean,
    val onClick: () -> Unit,
    val content: @Composable () -> Unit
)
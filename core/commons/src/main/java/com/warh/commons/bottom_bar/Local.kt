package com.warh.commons.bottom_bar

import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.staticCompositionLocalOf

@OptIn(ExperimentalMaterial3Api::class)
val LocalBottomBarBehavior = staticCompositionLocalOf<BottomAppBarScrollBehavior?> { null }
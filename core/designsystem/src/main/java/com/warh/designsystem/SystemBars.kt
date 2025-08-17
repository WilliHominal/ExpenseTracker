package com.warh.designsystem

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

@Composable
fun SyncSystemBarsWithTheme(
    navColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as ComponentActivity

        val status = MaterialTheme.colorScheme.primary
        val statusArgb = status.toArgb()
        val navArgb = navColor.toArgb()

        SideEffect {
            val statusStyle = SystemBarStyle.auto(
                lightScrim = statusArgb,
                darkScrim = statusArgb
            )
            val navStyle = SystemBarStyle.auto(
                lightScrim = navArgb,
                darkScrim = navArgb
            )
            activity.enableEdgeToEdge(
                statusBarStyle = statusStyle,
                navigationBarStyle = navStyle
            )
        }
    }
}

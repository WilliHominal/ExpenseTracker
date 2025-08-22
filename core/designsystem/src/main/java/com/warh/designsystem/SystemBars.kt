package com.warh.designsystem

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

private fun isGestureNavigation(view: android.view.View): Boolean {
    val insets = ViewCompat.getRootWindowInsets(view) ?: return false
    val g = insets.getInsets(WindowInsetsCompat.Type.systemGestures())
    return (g.left > 0 || g.right > 0 || g.bottom > 0)
}

@Composable
fun SyncSystemBarsWithTheme(
    statusColor: Color = MaterialTheme.colorScheme.primary,
    navColor: Color = MaterialTheme.colorScheme.primary
) {
    val view = LocalView.current
    if (view.isInEditMode) return

    val activity = view.context as ComponentActivity

    val statusArgb = statusColor.toArgb()
    val navArgb    = navColor.toArgb()

    val statusWantsLightIcons = statusColor.luminance() < 0.5f
    val navWantsLightIcons    = navColor.luminance() < 0.5f

    val gestures = isGestureNavigation(view)

    SideEffect {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.window.isNavigationBarContrastEnforced = false
        }

        val statusStyle =
            if (statusWantsLightIcons) SystemBarStyle.dark(statusArgb)
            else                        SystemBarStyle.light(statusArgb, statusArgb)

        val navStyle =
            if (gestures) {
                val transparent = Color.Transparent.toArgb()
                if (navWantsLightIcons) SystemBarStyle.dark(transparent)
                else                     SystemBarStyle.light(transparent, transparent)
            } else {
                if (navWantsLightIcons) SystemBarStyle.dark(navArgb)
                else                     SystemBarStyle.light(navArgb, navArgb)
            }

        activity.enableEdgeToEdge(
            statusBarStyle = statusStyle,
            navigationBarStyle = navStyle
        )
    }
}
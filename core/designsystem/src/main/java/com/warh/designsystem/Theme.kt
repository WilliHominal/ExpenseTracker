package com.warh.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Light = lightColorScheme(
    primary = Color(0xFFD33E4C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD8DD),
    onPrimaryContainer = Color(0xFF5F0B14),

    secondary = Color(0xFF9E4A55),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE6EA),
    onSecondaryContainer = Color(0xFF4A1820),

    tertiary = Color(0xFF56B8A6),
    onTertiary = Color(0xFF003730),
    tertiaryContainer = Color(0xFFBEF0E6),
    onTertiaryContainer = Color(0xFF0A2C26),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = Color(0xFFFFF3F5),
    onBackground = Color(0xFF201A1C),

    surface = Color(0xFFFFF7F8),
    onSurface = Color(0xFF211A1C),
    surfaceVariant = Color(0xFFF4D7DB),
    onSurfaceVariant = Color(0xFF534346),

    outline = Color(0xFF89686E),
    outlineVariant = Color(0xFFDABCC1),

    inverseSurface = Color(0xFF372E30),
    inverseOnSurface = Color(0xFFFBEDEF),
    inversePrimary = Color(0xFFFFB0B9),
    surfaceTint = Color(0xFFD33E4C),
    scrim = Color(0x66000000)
)

private val Dark = darkColorScheme(
    primary = Color(0xFF3A424E),
    onPrimary = Color(0xFFDDE3EA),
    primaryContainer = Color(0xFF2C333D),
    onPrimaryContainer = Color(0xFFDDE3EA),

    secondary = Color(0xFF3A424E),
    onSecondary = Color(0xFFE6EBF2),
    secondaryContainer = Color(0xFF343C48),
    onSecondaryContainer = Color(0xFFD8DEE6),

    tertiary = Color(0xFF3AA9A3),
    onTertiary = Color(0xFF041F1E),
    tertiaryContainer = Color(0xFF1E3B39),
    onTertiaryContainer = Color(0xFFAEE7E2),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF0E1116),
    onBackground = Color(0xFFE2E6EE),

    surface = Color(0xFF141820),
    onSurface = Color(0xFFE1E6EE),
    surfaceVariant = Color(0xFF1C222B),
    onSurfaceVariant = Color(0xFFBAC2CF),

    outline = Color(0xFF435063),
    outlineVariant = Color(0xFF303A46),

    inverseSurface = Color(0xFFE6EAF0),
    inverseOnSurface = Color(0xFF1A1F27),
    inversePrimary = Color(0xFF8BA2BF),

    surfaceTint = Color(0xFF232A33),
    scrim = Color(0x99000000)
)

@Composable
fun ExpenseTheme(
    dark: Boolean = isSystemInDarkTheme(),
    dynamic: Boolean = false,
    content: @Composable () -> Unit
) {
    val ctx = LocalContext.current
    val scheme =
        if (dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        } else {
            if (dark) Dark else Light
        }

    MaterialTheme(
        colorScheme = scheme,
        content = content
    )
}
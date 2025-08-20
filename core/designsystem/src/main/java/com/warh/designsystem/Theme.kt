package com.warh.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

val LightScheme = lightColorScheme(
    primary = ColorTokens.Light.Primary,
    onPrimary = ColorTokens.Light.OnPrimary,
    primaryContainer = ColorTokens.Light.PrimaryContainer,
    onPrimaryContainer = ColorTokens.Light.OnPrimaryContainer,

    secondary = ColorTokens.Light.Secondary,
    onSecondary = ColorTokens.Light.OnSecondary,
    secondaryContainer = ColorTokens.Light.SecondaryContainer,
    onSecondaryContainer = ColorTokens.Light.OnSecondaryContainer,

    tertiary = ColorTokens.Light.Tertiary,
    onTertiary = ColorTokens.Light.OnTertiary,
    tertiaryContainer = ColorTokens.Light.TertiaryContainer,
    onTertiaryContainer = ColorTokens.Light.OnTertiaryContainer,

    error = ColorTokens.Light.Error,
    onError = ColorTokens.Light.OnError,
    errorContainer = ColorTokens.Light.ErrorContainer,
    onErrorContainer = ColorTokens.Light.OnErrorContainer,

    background = ColorTokens.Light.Background,
    onBackground = ColorTokens.Light.OnBackground,

    surface = ColorTokens.Light.Surface,
    onSurface = ColorTokens.Light.OnSurface,
    surfaceVariant = ColorTokens.Light.SurfaceVariant,
    onSurfaceVariant = ColorTokens.Light.OnSurfaceVariant,

    outline = ColorTokens.Light.Outline,
    outlineVariant = ColorTokens.Light.OutlineVariant,

    inverseSurface = ColorTokens.Light.InverseSurface,
    inverseOnSurface = ColorTokens.Light.InverseOnSurface,
    inversePrimary = ColorTokens.Light.InversePrimary,
    surfaceTint = ColorTokens.Light.SurfaceTint,
    scrim = ColorTokens.Light.Scrim
)

val DarkScheme = darkColorScheme(
    primary = ColorTokens.Dark.Primary,
    onPrimary = ColorTokens.Dark.OnPrimary,
    primaryContainer = ColorTokens.Dark.PrimaryContainer,
    onPrimaryContainer = ColorTokens.Dark.OnPrimaryContainer,

    secondary = ColorTokens.Dark.Secondary,
    onSecondary = ColorTokens.Dark.OnSecondary,
    secondaryContainer = ColorTokens.Dark.SecondaryContainer,
    onSecondaryContainer = ColorTokens.Dark.OnSecondaryContainer,

    tertiary = ColorTokens.Dark.Tertiary,
    onTertiary = ColorTokens.Dark.OnTertiary,
    tertiaryContainer = ColorTokens.Dark.TertiaryContainer,
    onTertiaryContainer = ColorTokens.Dark.OnTertiaryContainer,

    error = ColorTokens.Dark.Error,
    onError = ColorTokens.Dark.OnError,
    errorContainer = ColorTokens.Dark.ErrorContainer,
    onErrorContainer = ColorTokens.Dark.OnErrorContainer,

    background = ColorTokens.Dark.Background,
    onBackground = ColorTokens.Dark.OnBackground,

    surface = ColorTokens.Dark.Surface,
    onSurface = ColorTokens.Dark.OnSurface,
    surfaceVariant = ColorTokens.Dark.SurfaceVariant,
    onSurfaceVariant = ColorTokens.Dark.OnSurfaceVariant,

    outline = ColorTokens.Dark.Outline,
    outlineVariant = ColorTokens.Dark.OutlineVariant,

    inverseSurface = ColorTokens.Dark.InverseSurface,
    inverseOnSurface = ColorTokens.Dark.InverseOnSurface,
    inversePrimary = ColorTokens.Dark.InversePrimary,

    surfaceTint = ColorTokens.Dark.SurfaceTint,
    scrim = ColorTokens.Dark.Scrim
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
            if (dark) DarkScheme else LightScheme
        }

    MaterialTheme(
        colorScheme = scheme,
        content = content
    )
}
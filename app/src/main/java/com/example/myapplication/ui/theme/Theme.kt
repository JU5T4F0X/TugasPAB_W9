package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SlateIndigoPrimaryDark,
    onPrimary = SlateIndigoOnPrimaryDark,
    primaryContainer = SlateIndigoContainerDark,
    onPrimaryContainer = SlateIndigoOnContainerDark,
    secondary = CampusTealSecondaryDark,
    onSecondary = CampusTealOnSecondaryDark,
    secondaryContainer = CampusTealContainerDark,
    onSecondaryContainer = CampusTealOnContainerDark,
    surface = NeutralSurfaceDark,
    onSurface = NeutralOnSurfaceDark,
    surfaceVariant = NeutralSurfaceVariantDark,
    outline = NeutralOutlineDark,
    error = ErrorRed,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = SlateIndigoPrimary,
    onPrimary = SlateIndigoOnPrimary,
    primaryContainer = SlateIndigoContainer,
    onPrimaryContainer = SlateIndigoOnContainer,
    secondary = CampusTealSecondary,
    onSecondary = CampusTealOnSecondary,
    secondaryContainer = CampusTealContainer,
    onSecondaryContainer = CampusTealOnContainer,
    surface = NeutralSurfaceLight,
    onSurface = NeutralOnSurfaceLight,
    surfaceVariant = NeutralSurfaceVariantLight,
    outline = NeutralOutlineLight,
    error = ErrorRed,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ErpPrimaryDark,
    secondary = ErpSecondaryDark,
    tertiary = ErpTertiaryDark,
    background = ErpBackgroundDark,
    surface = ErpSurfaceDark,
    onPrimary = ErpOnPrimaryDark,
    onBackground = ErpOnBackgroundDark,
    onSurface = ErpOnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = ErpPrimaryLight,
    secondary = ErpSecondaryLight,
    tertiary = ErpTertiaryLight,
    background = ErpBackgroundLight,
    surface = ErpSurfaceLight,
    onPrimary = ErpOnPrimaryLight,
    onBackground = ErpOnBackgroundLight,
    onSurface = ErpOnSurfaceLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We maintain visual consistency by applying our high-fidelity custom ERP palette
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.simplenote.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// پالت رنگ تم روشن
private val LightColors = lightColorScheme(
    primary = PrimaryBase,
    onPrimary = NeutralWhite,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,

    secondary = SecondaryBase,
    onSecondary = NeutralWhite,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = SecondaryDark,

    background = PrimaryBackground,
    onBackground = NeutralBlack,

    surface = NeutralWhite,
    onSurface = NeutralBlack,

    error = ErrorBase,
    onError = NeutralWhite
)

// پالت رنگ تم تاریک
private val DarkColors = darkColorScheme(
    primary = PrimaryBase,
    onPrimary = NeutralWhite,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = PrimaryLight,

    secondary = SecondaryBase,
    onSecondary = NeutralWhite,
    secondaryContainer = SecondaryDark,
    onSecondaryContainer = SecondaryLight,

    background = NeutralBlack,
    onBackground = NeutralWhite,

    surface = NeutralDarkGrey,
    onSurface = NeutralWhite,

    error = ErrorBase,
    onError = NeutralWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        window.statusBarColor = colorScheme.primary.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content

    )

}

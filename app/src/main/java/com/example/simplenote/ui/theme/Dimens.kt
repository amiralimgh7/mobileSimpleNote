package com.example.simplenote.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// کلاس فاصله‌ها
data class Spacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp
)

// ساخت یک CompositionLocal برای فاصله‌ها
val LocalSpacing = staticCompositionLocalOf { Spacing() }

// فراهم کردن فاصله‌ها در کل پروژه
@Composable
fun ProvideSpacing(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        value = LocalSpacing provides Spacing(),
        content = content
    )
}

// گرفتن فاصله‌ها
val spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current

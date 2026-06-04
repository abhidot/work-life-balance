package com.worklife.boundary.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Sage = Color(0xFF6B8F71)
private val SageDark = Color(0xFF4F6F55)
private val Cream = Color(0xFFF7F3EC)
private val Sand = Color(0xFFE8DFD0)
private val Ink = Color(0xFF2F3430)
private val Muted = Color(0xFF6E756F)

private val LightColors = lightColorScheme(
    primary = Sage,
    onPrimary = Color.White,
    primaryContainer = Sand,
    onPrimaryContainer = Ink,
    secondary = SageDark,
    onSecondary = Color.White,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = Muted,
    outline = Muted,
)

@Composable
fun BoundaryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = BoundaryTypography,
        content = content,
    )
}

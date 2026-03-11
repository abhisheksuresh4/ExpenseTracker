package com.example.weatherawareexpensetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    secondary = NeonYellow,
    tertiary = LimeGreen,
    background = BackgroundCream,
    surface = White,
    onPrimary = White,
    onSecondary = TextDark,
    onTertiary = White,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun WeatherAwareExpenseTrackerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

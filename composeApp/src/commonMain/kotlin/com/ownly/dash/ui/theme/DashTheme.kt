package com.ownly.dash.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DashDarkColorScheme = darkColorScheme(
    primary = DashColors.Pink,
    onPrimary = Color.White,
    background = DashColors.Background,
    onBackground = DashColors.TextPrimary,
    surface = DashColors.Surface,
    onSurface = DashColors.TextPrimary,
    surfaceVariant = DashColors.SurfaceElevated,
    onSurfaceVariant = DashColors.TextSecondary,
    error = DashColors.Danger,
    outline = DashColors.Border,
)

private val DashTypography = Typography(
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp),
    bodyMedium = TextStyle(fontSize = 13.sp),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun DashTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DashDarkColorScheme,
        typography = DashTypography,
        content = content,
    )
}

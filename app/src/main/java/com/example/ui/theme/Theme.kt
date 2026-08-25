package com.example.ui.theme

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

private val LightPinkColorScheme =
  lightColorScheme(
    primary = PinkPrimary,
    onPrimary = Color.White,
    primaryContainer = PinkContainer,
    onPrimaryContainer = OnPinkContainer,
    secondary = PinkPrimaryDark,
    onSecondary = Color.White,
    secondaryContainer = PinkContainer,
    onSecondaryContainer = OnPinkContainer,
    tertiary = PinkPrimaryLight,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceCardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    error = SafetyPinkAlert,
    onError = Color.White
  )

@Composable
fun RamisaTheme(
  darkTheme: Boolean = false, // Always enforce vibrant, clean Pink & White aesthetic
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = LightPinkColorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  RamisaTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}


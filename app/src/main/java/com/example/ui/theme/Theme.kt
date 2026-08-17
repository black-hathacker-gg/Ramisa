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

private val DarkColorScheme =
  darkColorScheme(
    primary = PinkPrimaryLight,
    onPrimary = Color(0xFF3E1029),
    primaryContainer = PinkPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = PinkPrimary,
    onSecondary = Color.White,
    secondaryContainer = PinkPrimaryDark,
    onSecondaryContainer = PinkContainer,
    tertiary = PinkPrimaryLight,
    onTertiary = Color.White,
    background = SurfaceDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceVariantDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceCardDark,
    onSurfaceVariant = TextSecondaryDark,
    error = SafetyPinkAlert,
    onError = Color.White
  )

private val LightColorScheme =
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
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Preserve high contrast safety colors by default
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  RamisaTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}


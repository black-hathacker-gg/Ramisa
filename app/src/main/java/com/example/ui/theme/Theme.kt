package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightPinkColorScheme =
  lightColorScheme(
    primary = PinkPrimary,
    onPrimary = Color.White,
    primaryContainer = PinkContainer,
    onPrimaryContainer = OnPinkContainer,
    secondary = InfoTeal,
    onSecondary = Color.White,
    secondaryContainer = InfoTealContainer,
    onSecondaryContainer = InfoTeal,
    tertiary = Color(0xFF62547A),
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceCardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = SurfaceOutline,
    outlineVariant = SurfaceOutlineSubtle,
    error = SafetyPinkAlert,
    onError = Color.White,
    errorContainer = SafetyPinkAlertContainer,
    onErrorContainer = OnSafetyRedContainer
  )

private val DarkPinkColorScheme = darkColorScheme(
  primary = Color(0xFFFBA7C9),
  onPrimary = Color(0xFF421029),
  primaryContainer = Color(0xFF382630),
  onPrimaryContainer = Color(0xFFFFD9E7),
  secondary = Color(0xFF8CD1C2),
  onSecondary = Color(0xFF00382F),
  secondaryContainer = Color(0xFF203B34),
  onSecondaryContainer = Color(0xFFB7E8DB),
  background = SurfaceDark,
  onBackground = TextPrimaryDark,
  surface = SurfaceCardDark,
  onSurface = TextPrimaryDark,
  surfaceVariant = Color(0xFF382F35),
  onSurfaceVariant = TextSecondaryDark,
  outline = Color(0xFF987F8D),
  outlineVariant = Color(0xFF4B3A43),
  error = Color(0xFFFFACB6),
  onError = Color(0xFF4B0716),
  errorContainer = Color(0xFF42252C),
  onErrorContainer = Color(0xFFFFD9DD)
)

@Composable
fun RamisaTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = if (darkTheme) DarkPinkColorScheme else LightPinkColorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  RamisaTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}


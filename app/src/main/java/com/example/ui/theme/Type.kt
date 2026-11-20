package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private fun ramisaText(size: Int, lineHeight: Int, weight: FontWeight) = TextStyle(
  fontFamily = FontFamily.Default,
  fontWeight = weight,
  fontSize = size.sp,
  lineHeight = lineHeight.sp
)

val Typography = Typography(
  bodyLarge = ramisaText(16, 24, FontWeight.Normal),
  bodyMedium = ramisaText(14, 20, FontWeight.Normal),
  titleLarge = ramisaText(22, 28, FontWeight.Medium),
  titleMedium = ramisaText(18, 24, FontWeight.Medium),
  labelLarge = ramisaText(14, 20, FontWeight.Medium)
)

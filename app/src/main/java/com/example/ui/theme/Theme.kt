package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = NavyTonal,
    secondary = SlateBlue,
    tertiary = WarmGold,
    background = DeepDarkNavy,
    surface = CharcoalDark,
    onPrimary = DeepDarkNavy,
    onSecondary = CrispWhite,
    onBackground = CrispWhite,
    onSurface = CrispWhite
  )

private val LightColorScheme =
  lightColorScheme(
    primary = DeepNavy,
    secondary = SlateBlue,
    tertiary = WarmGold,
    background = IceMist,
    surface = CrispWhite,
    onPrimary = CrispWhite,
    onSecondary = CrispWhite,
    onBackground = CharcoalText,
    onSurface = CharcoalText
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color by default to preserve the exact Navy brand
  dynamicColor: Boolean = false,
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

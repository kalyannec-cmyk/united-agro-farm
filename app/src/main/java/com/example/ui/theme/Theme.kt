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
    primary = DarkGreenPrimary,
    onPrimary = Color(0xFF003912),
    primaryContainer = DarkGreenContainer,
    onPrimaryContainer = Color(0xFFA5F4AC),
    secondary = Color(0xFFA2D39E),
    onSecondary = Color(0xFF0C3814),
    tertiary = Color(0xFFFFB74D),
    background = DarkSurface,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = Color(0xFFC2C9BD),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AgroGreenPrimary,
    onPrimary = AgroGreenOnPrimary,
    primaryContainer = AgroGreenContainer,
    onPrimaryContainer = AgroGreenOnContainer,
    secondary = AgroSecondary,
    onSecondary = Color.White,
    secondaryContainer = AgroSecondaryContainer,
    onSecondaryContainer = AgroOnSecondaryContainer,
    tertiary = AgroGoldTertiary,
    onTertiary = Color.White,
    tertiaryContainer = AgroGoldContainer,
    onTertiaryContainer = AgroOnGoldContainer,
    background = Color(0xFFF7FBF6),
    surface = AgroSurface,
    surfaceVariant = AgroSurfaceVariant,
    onBackground = AgroOnSurface,
    onSurface = AgroOnSurface,
    onSurfaceVariant = AgroOnSurfaceVariant,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Set to false to preserve United Agro Farm branded colors
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

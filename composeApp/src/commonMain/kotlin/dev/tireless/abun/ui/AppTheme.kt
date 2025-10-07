package dev.tireless.abun.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemePreference {
  SYSTEM,
  LIGHT,
  DARK,
}

private val LightColorScheme: ColorScheme =
  lightColorScheme(
    primary = Color(0xFF4A4A4A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE5E5E5),
    onPrimaryContainer = Color(0xFF1F1F1F),
    secondary = Color(0xFF6B6B6B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDDDDDD),
    onSecondaryContainer = Color(0xFF232323),
    tertiary = Color(0xFF878787),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE6E6E6),
    onTertiaryContainer = Color(0xFF1A1A1A),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1C1C1C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1C1C),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF464646),
    outline = Color(0xFF939393),
    outlineVariant = Color(0xFFD5D5D5),
    inverseSurface = Color(0xFF2F2F2F),
    inverseOnSurface = Color(0xFFEAEAEA),
    surfaceTint = Color.Transparent,
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFEAEAEA),
    surfaceContainerLowest = Color(0xFFF5F5F5),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF6F6F6),
    surfaceContainerHighest = Color(0xFFF0F0F0),
  )

private val DarkColorScheme: ColorScheme =
  darkColorScheme(
    primary = Color(0xFFBEBEBE),
    onPrimary = Color(0xFF121212),
    primaryContainer = Color(0xFF353535),
    onPrimaryContainer = Color(0xFFE7E7E7),
    secondary = Color(0xFFA0A0A0),
    onSecondary = Color(0xFF151515),
    secondaryContainer = Color(0xFF3C3C3C),
    onSecondaryContainer = Color(0xFFDFDFDF),
    tertiary = Color(0xFF8A8A8A),
    onTertiary = Color(0xFF151515),
    tertiaryContainer = Color(0xFF353535),
    onTertiaryContainer = Color(0xFFE0E0E0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111111),
    onBackground = Color(0xFFE6E6E6),
    surface = Color(0xFF181818),
    onSurface = Color(0xFFE6E6E6),
    surfaceVariant = Color(0xFF323232),
    onSurfaceVariant = Color(0xFFB9B9B9),
    outline = Color(0xFF646464),
    outlineVariant = Color(0xFF2A2A2A),
    inverseSurface = Color(0xFFE6E6E6),
    inverseOnSurface = Color(0xFF131313),
    surfaceTint = Color.Transparent,
    surfaceBright = Color(0xFF232323),
    surfaceDim = Color(0xFF0F0F0F),
    surfaceContainerLowest = Color(0xFF0F0F0F),
    surfaceContainerLow = Color(0xFF161616),
    surfaceContainer = Color(0xFF181818),
    surfaceContainerHigh = Color(0xFF1F1F1F),
    surfaceContainerHighest = Color(0xFF242424),
  )

@Composable
fun AppTheme(
  themePreference: ThemePreference = ThemePreference.SYSTEM,
  content: @Composable () -> Unit,
) {
  val useDarkTheme =
    when (themePreference) {
      ThemePreference.SYSTEM -> isSystemInDarkTheme()
      ThemePreference.LIGHT -> false
      ThemePreference.DARK -> true
    }
  val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    content = content,
  )
}

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
    primary = Color(0xFF4A5568),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = Color(0xFF1A202C),
    secondary = Color(0xFF718096),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD9DEE7),
    onSecondaryContainer = Color(0xFF1F2933),
    tertiary = Color(0xFF2D3748),
    onTertiary = Color(0xFFF7FAFC),
    tertiaryContainer = Color(0xFFCBD5E1),
    onTertiaryContainer = Color(0xFF111820),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF1A202C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A202C),
    surfaceVariant = Color(0xFFE4E7EB),
    onSurfaceVariant = Color(0xFF4A5568),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFD5DBE5),
    inverseSurface = Color(0xFF2D3542),
    inverseOnSurface = Color(0xFFE9EDF5),
  )

private val DarkColorScheme: ColorScheme =
  darkColorScheme(
    primary = Color(0xFF9CA3AF),
    onPrimary = Color(0xFF111418),
    primaryContainer = Color(0xFF374151),
    onPrimaryContainer = Color(0xFFE5E7EB),
    secondary = Color(0xFFADB4BE),
    onSecondary = Color(0xFF14181E),
    secondaryContainer = Color(0xFF3A4150),
    onSecondaryContainer = Color(0xFFDEE2E9),
    tertiary = Color(0xFF6B7280),
    onTertiary = Color(0xFFEDEEF2),
    tertiaryContainer = Color(0xFF2F3542),
    onTertiaryContainer = Color(0xFFE0E4EA),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0A0D12),
    onBackground = Color(0xFFE2E6ED),
    surface = Color(0xFF12171F),
    onSurface = Color(0xFFE2E6ED),
    surfaceVariant = Color(0xFF2C3442),
    onSurfaceVariant = Color(0xFFBEC4CE),
    outline = Color(0xFF5F6672),
    outlineVariant = Color(0xFF2A303B),
    inverseSurface = Color(0xFFE2E6ED),
    inverseOnSurface = Color(0xFF131721),
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

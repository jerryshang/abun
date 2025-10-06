package dev.tireless.abun.material

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
  DARK
}

private val LightColorScheme: ColorScheme = lightColorScheme(
  primary = Color(0xFF3F5BA9),
  onPrimary = Color(0xFFFFFFFF),
  primaryContainer = Color(0xFFDEE6FF),
  onPrimaryContainer = Color(0xFF0C224E),
  secondary = Color(0xFF5B6374),
  onSecondary = Color(0xFFFFFFFF),
  secondaryContainer = Color(0xFFDFE3ED),
  onSecondaryContainer = Color(0xFF151C2C),
  tertiary = Color(0xFF7B4FA7),
  onTertiary = Color(0xFFFFFFFF),
  tertiaryContainer = Color(0xFFEDDCFF),
  onTertiaryContainer = Color(0xFF2F114D),
  error = Color(0xFFBA1A1A),
  onError = Color(0xFFFFFFFF),
  errorContainer = Color(0xFFFFDAD6),
  onErrorContainer = Color(0xFF410002),
  background = Color(0xFFF6F7FB),
  onBackground = Color(0xFF1B1F28),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF1B1F28),
  surfaceVariant = Color(0xFFE1E4ED),
  onSurfaceVariant = Color(0xFF454C5D),
  outline = Color(0xFF747A8B),
  outlineVariant = Color(0xFFC8CDD8),
  inverseSurface = Color(0xFF2E3340),
  inverseOnSurface = Color(0xFFE7EAF3)
)

private val DarkColorScheme: ColorScheme = darkColorScheme(
  primary = Color(0xFFACC6FF),
  onPrimary = Color(0xFF002E63),
  primaryContainer = Color(0xFF1F457F),
  onPrimaryContainer = Color(0xFFD7E3FF),
  secondary = Color(0xFFBAC6E5),
  onSecondary = Color(0xFF243040),
  secondaryContainer = Color(0xFF3A475A),
  onSecondaryContainer = Color(0xFFDDE5FF),
  tertiary = Color(0xFFD5B6FF),
  onTertiary = Color(0xFF381559),
  tertiaryContainer = Color(0xFF523170),
  onTertiaryContainer = Color(0xFFEFDFFF),
  error = Color(0xFFFFB4AB),
  onError = Color(0xFF690005),
  errorContainer = Color(0xFF93000A),
  onErrorContainer = Color(0xFFFFDAD6),
  background = Color(0xFF11141C),
  onBackground = Color(0xFFE0E2EA),
  surface = Color(0xFF181C24),
  onSurface = Color(0xFFE0E2EA),
  surfaceVariant = Color(0xFF434758),
  onSurfaceVariant = Color(0xFFC3C7D6),
  outline = Color(0xFF8B90A3),
  outlineVariant = Color(0xFF414656),
  inverseSurface = Color(0xFFE5E9F3),
  inverseOnSurface = Color(0xFF1E2230)
)

@Composable
fun AppTheme(
  themePreference: ThemePreference = ThemePreference.SYSTEM,
  content: @Composable () -> Unit
) {
  val useDarkTheme = when (themePreference) {
    ThemePreference.SYSTEM -> isSystemInDarkTheme()
    ThemePreference.LIGHT -> false
    ThemePreference.DARK -> true
  }
  val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    content = content
  )
}

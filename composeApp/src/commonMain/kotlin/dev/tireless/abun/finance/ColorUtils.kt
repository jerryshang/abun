package dev.tireless.abun.finance

import androidx.compose.ui.graphics.Color

fun hexToColorOrNull(hex: String?): Color? {
  if (hex.isNullOrBlank()) return null
  val clean = hex.removePrefix("#")
  val colorLong = try {
    when (clean.length) {
      6 -> 0xFF000000 or clean.toLong(16)
      8 -> clean.toLong(16)
      else -> return null
    }
  } catch (_: Exception) {
    return null
  }
  return Color(colorLong)
}

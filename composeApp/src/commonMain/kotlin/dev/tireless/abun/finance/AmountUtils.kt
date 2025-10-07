package dev.tireless.abun.finance

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Utility functions for handling monetary amounts with precise decimal arithmetic
 *
 * All amounts are stored as INTEGER in database (actual value * 10000) to avoid
 * floating point precision loss. This supports up to 4 decimal places (.4f precision).
 *
 * Examples:
 * - 100.00 yuan -> stored as 1000000
 * - 0.0001 yuan -> stored as 1
 * - 99999.9999 yuan -> stored as 999999999
 */

/**
 * Precision multiplier: 10^4 = 10000
 * Supports up to 4 decimal places
 */
private const val AMOUNT_PRECISION = 10000L

/**
 * Convert display amount (Double) to storage amount (Long)
 * @param amount Display amount (e.g., 100.5678)
 * @return Storage amount (e.g., 1005678)
 */
fun Double.toStorageAmount(): Long = (this * AMOUNT_PRECISION).roundToLong()

/**
 * Convert storage amount (Long) to display amount (Double)
 * @param storageAmount Storage amount (e.g., 1005678)
 * @return Display amount (e.g., 100.5678)
 */
fun Long.toDisplayAmount(): Double = this.toDouble() / AMOUNT_PRECISION

/**
 * Format amount for display with 2 decimal places (standard currency format)
 * @param amount Display amount
 * @return Formatted string (e.g., "100.57")
 */
fun formatAmount(amount: Double): String {
  val isNegative = amount < 0
  val absAmount = abs(amount)
  val intPart = absAmount.toLong()
  val decPart = ((absAmount - intPart) * 100).roundToLong().toString().padStart(2, '0')
  return if (isNegative) "-$intPart.$decPart" else "$intPart.$decPart"
}

/**
 * Format storage amount for display with 2 decimal places
 * @param storageAmount Storage amount (e.g., 1005678)
 * @return Formatted string (e.g., "100.57")
 */
fun formatStorageAmount(storageAmount: Long): String = formatAmount(storageAmount.toDisplayAmount())

/**
 * Validate input string for amount (up to 4 decimal places)
 * @param input User input string
 * @return true if valid amount format
 */
fun isValidAmountInput(input: String): Boolean {
  if (input.isBlank()) return false

  // Match pattern: optional digits, optional decimal point, up to 4 decimal digits
  val pattern = Regex("^\\d*\\.?\\d{0,4}$")
  return pattern.matches(input)
}

/**
 * Parse user input to display amount
 * @param input User input string (e.g., "100.5678")
 * @return Display amount or null if invalid
 */
fun parseAmountInput(input: String): Double? {
  if (!isValidAmountInput(input)) return null
  return input.toDoubleOrNull()
}

/**
 * Parse user input directly to storage amount
 * @param input User input string (e.g., "100.5678")
 * @return Storage amount or null if invalid
 */
fun parseAmountInputToStorage(input: String): Long? {
  val displayAmount = parseAmountInput(input) ?: return null
  return displayAmount.toStorageAmount()
}

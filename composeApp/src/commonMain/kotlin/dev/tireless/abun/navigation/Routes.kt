package dev.tireless.abun.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for the entire app
 */
sealed interface Route {
  /**
   * Main tab routes (bottom navigation)
   */
  @Serializable
  data object Home : Route

  @Serializable
  data object Finance : Route

  @Serializable
  data object Timeblock : Route

  @Serializable
  data object Settings : Route

  /**
   * Finance module routes
   */
  @Serializable
  data object AccountManagement : Route

  @Serializable
  data object PriceComparison : Route

  @Serializable
  data object FutureView : Route

  @Serializable
  data class AccountDetails(val accountId: Long?) : Route

  /**
   * Timeblock module routes
   */
  @Serializable
  data object TimeCategoryManagement : Route
}

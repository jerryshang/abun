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
  data object Material : Route

  @Serializable
  data object Mental : Route

  @Serializable
  data object Time : Route

  @Serializable
  data object Settings : Route

  /**
   * Finance module routes
   */
  @Serializable
  data object AccountManagement : Route

  @Serializable
  data class AccountEdit(
    val accountId: Long? = null,
  ) : Route

  @Serializable
  data class ExpenseEdit(
    val transactionId: Long? = null,
  ) : Route

  @Serializable
  data class RevenueEdit(
    val transactionId: Long? = null,
  ) : Route

  @Serializable
  data class TransferEdit(
    val transactionId: Long? = null,
  ) : Route

  @Serializable
  data object LoanEdit : Route

  @Serializable
  data object PriceComparison : Route

  @Serializable
  data object TrialCalculator : Route

  @Serializable
  data object FutureView : Route

  @Serializable
  data class AccountDetails(
    val accountId: Long?,
  ) : Route

  /**
   * Timeblock module routes
   */
  @Serializable
  data object TimeTaskManagement : Route

  @Serializable
  data object TaskPlanner : Route
}

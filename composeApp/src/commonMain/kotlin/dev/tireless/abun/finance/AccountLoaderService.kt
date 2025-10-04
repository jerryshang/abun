package dev.tireless.abun.finance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Predefined account template types
 */
enum class AccountTemplateType {
  MINIMAL,
  STANDARD;

  fun getFileName(): String = when (this) {
    MINIMAL -> "accounts_minimal.csv"
    STANDARD -> "accounts_standard.csv"
  }
}

/**
 * Data class representing a predefined account from CSV
 */
data class PredefinedAccount(
  val name: String,
  val parentName: String,
  val currency: String,
  val isActive: Boolean,
  val isCountable: Boolean,
  val isVisible: Boolean,
  val iconName: String?,
  val colorHex: String?
)

/**
 * Service for loading predefined account templates
 */
class AccountLoaderService(
  private val accountRepository: AccountRepository,
  private val transactionRepository: TransactionRepository
) {

  /**
   * Parse CSV content into predefined accounts
   */
  private fun parseCsvContent(csvContent: String): List<PredefinedAccount> {
    val lines = csvContent.lines().filter { it.isNotBlank() }
    if (lines.isEmpty()) return emptyList()

    // Skip header line
    val dataLines = lines.drop(1)

    return dataLines.mapNotNull { line ->
      val parts = line.split(",").map { it.trim() }
      if (parts.size < 7) return@mapNotNull null

      try {
        PredefinedAccount(
          name = parts[0],
          parentName = parts[1],
          currency = parts[2],
          isActive = parts[3].toBoolean(),
          isCountable = parts[4].toBoolean(),
          isVisible = parts[5].toBoolean(),
          iconName = parts.getOrNull(6)?.takeIf { it.isNotBlank() },
          colorHex = parts.getOrNull(7)?.takeIf { it.isNotBlank() }
        )
      } catch (e: Exception) {
        null
      }
    }
  }

  /**
   * Clear all transactions and non-root accounts
   * Keeps the 5 root accounts (Asset, Liability, Equity, Revenue, Expense)
   */
  suspend fun clearAllData() = withContext(Dispatchers.IO) {
    // Get all transactions
    val allTransactions = transactionRepository.getAllTransactions()

    // Delete all transactions
    allTransactions.forEach { transaction ->
      transactionRepository.deleteTransaction(transaction.id)
    }

    // Get all accounts
    val allAccounts = accountRepository.getAllAccounts()

    // Delete all non-root accounts (keep the 5 root accounts: id 1-5)
    allAccounts
      .filter { it.id > 5 } // Root accounts have IDs 1-5
      .forEach { account ->
        accountRepository.deleteAccount(account.id)
      }
  }

  /**
   * Load predefined accounts from CSV content
   * Must be called after clearAllData()
   */
  suspend fun loadPredefinedAccounts(csvContent: String) = withContext(Dispatchers.IO) {
    val predefinedAccounts = parseCsvContent(csvContent)

    // Build a map to track parent account IDs
    val accountIdMap = mutableMapOf<String, Long>(
      "Asset" to RootAccountIds.ASSET,
      "Liability" to RootAccountIds.LIABILITY,
      "Equity" to RootAccountIds.EQUITY,
      "Revenue" to RootAccountIds.REVENUE,
      "Expense" to RootAccountIds.EXPENSE
    )

    // Create accounts in order (parents before children)
    // First pass: create top-level accounts (direct children of root accounts)
    predefinedAccounts
      .filter { it.parentName in accountIdMap.keys }
      .forEach { predefined ->
        val parentId = accountIdMap[predefined.parentName]
        if (parentId != null) {
          val accountId = accountRepository.createAccount(
            CreateAccountInput(
              name = predefined.name,
              parentId = parentId,
              currency = predefined.currency,
              isActive = predefined.isActive,
              isCountable = predefined.isCountable,
              isVisibleInUi = predefined.isVisible,
              iconName = predefined.iconName,
              colorHex = predefined.colorHex
            )
          )
          accountIdMap[predefined.name] = accountId
        }
      }

    // Second pass: create child accounts (children of non-root accounts)
    predefinedAccounts
      .filter { it.parentName !in listOf("Asset", "Liability", "Equity", "Revenue", "Expense") }
      .forEach { predefined ->
        val parentId = accountIdMap[predefined.parentName]
        if (parentId != null) {
          val accountId = accountRepository.createAccount(
            CreateAccountInput(
              name = predefined.name,
              parentId = parentId,
              currency = predefined.currency,
              isActive = predefined.isActive,
              isCountable = predefined.isCountable,
              isVisibleInUi = predefined.isVisible,
              iconName = predefined.iconName,
              colorHex = predefined.colorHex
            )
          )
          accountIdMap[predefined.name] = accountId
        }
      }
  }

  /**
   * Load predefined account template
   * Clears all data and loads the selected template
   */
  suspend fun loadTemplate(templateType: AccountTemplateType, csvContent: String) {
    clearAllData()
    loadPredefinedAccounts(csvContent)
  }
}

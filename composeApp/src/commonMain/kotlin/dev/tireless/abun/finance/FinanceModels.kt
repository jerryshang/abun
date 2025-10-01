package dev.tireless.abun.finance

/**
 * Account Types for financial accounts
 */
enum class AccountType {
  CASH,
  DEBIT_CARD,
  CREDIT_CARD,
  E_WALLET,
  INVESTMENT,
  DEBT;

  companion object {
    fun fromString(value: String): AccountType = values().find { it.name == value.uppercase() } ?: CASH
  }
}

/**
 * Transaction Types
 */
enum class TransactionType {
  EXPENSE,
  INCOME,
  TRANSFER;

  companion object {
    fun fromString(value: String): TransactionType = values().find { it.name == value.uppercase() } ?: EXPENSE
  }
}

/**
 * Category Types (for categorizing transactions)
 */
enum class CategoryType {
  EXPENSE,
  INCOME;

  companion object {
    fun fromString(value: String): CategoryType = values().find { it.name == value.uppercase() } ?: EXPENSE
  }
}

/**
 * Recurring Transaction Frequency
 */
enum class RecurringFrequency {
  DAILY,
  WEEKLY,
  MONTHLY,
  YEARLY;

  companion object {
    fun fromString(value: String): RecurringFrequency = values().find { it.name == value.uppercase() } ?: MONTHLY
  }
}

/**
 * Domain model for Account
 */
data class Account(
  val id: Long,
  val name: String,
  val type: AccountType,
  val initialBalance: Double,
  val currentBalance: Double,
  val currency: String = "CNY",
  val isActive: Boolean = true,
  val iconName: String? = null,
  val colorHex: String? = null,
  val createdAt: Long,
  val updatedAt: Long
)

/**
 * Domain model for Finance Category
 */
data class FinanceCategory(
  val id: Long,
  val name: String,
  val parentId: Long? = null,
  val type: CategoryType,
  val iconName: String? = null,
  val colorHex: String? = null,
  val isSystem: Boolean = false,
  val createdAt: Long,
  val updatedAt: Long
)

/**
 * Domain model for Transaction
 */
data class Transaction(
  val id: Long,
  val amount: Double,
  val type: TransactionType,
  val transactionDate: Long,
  val categoryId: Long? = null,
  val accountId: Long,
  val toAccountId: Long? = null,
  val transferGroupId: String? = null,
  val payee: String? = null,
  val member: String? = null,
  val notes: String? = null,
  val createdAt: Long,
  val updatedAt: Long
)

/**
 * Domain model for Tag
 */
data class FinanceTag(
  val id: Long,
  val name: String,
  val colorHex: String? = null,
  val createdAt: Long
)

/**
 * Domain model for Transaction Attachment
 */
data class TransactionAttachment(
  val id: Long,
  val transactionId: Long,
  val filePath: String,
  val fileType: String,
  val createdAt: Long
)

/**
 * Domain model for Linked Item (cross-module linking)
 */
data class TransactionLinkedItem(
  val id: Long,
  val transactionId: Long,
  val linkedItemId: Long,
  val linkedItemType: String, // 'note', 'task', 'timeblock', etc.
  val createdAt: Long
)

/**
 * Domain model for Transaction Template
 */
data class TransactionTemplate(
  val id: Long,
  val name: String,
  val amount: Double? = null,
  val type: TransactionType,
  val categoryId: Long? = null,
  val accountId: Long,
  val payee: String? = null,
  val member: String? = null,
  val notes: String? = null,
  val createdAt: Long,
  val updatedAt: Long
)

/**
 * Domain model for Recurring Transaction
 */
data class RecurringTransaction(
  val id: Long,
  val templateId: Long,
  val frequency: RecurringFrequency,
  val intervalCount: Int = 1,
  val startDate: Long,
  val endDate: Long? = null,
  val nextOccurrence: Long,
  val isActive: Boolean = true,
  val createdAt: Long,
  val updatedAt: Long
)

/**
 * Rich transaction model with category and account details
 */
data class TransactionWithDetails(
  val transaction: Transaction,
  val category: FinanceCategory? = null,
  val account: Account,
  val toAccount: Account? = null,
  val tags: List<FinanceTag> = emptyList()
)

/**
 * Category with subcategories
 */
data class CategoryWithSubcategories(
  val category: FinanceCategory,
  val subcategories: List<FinanceCategory> = emptyList()
)

/**
 * Statistics data models
 */
data class CategorySummary(
  val category: FinanceCategory,
  val total: Double,
  val transactionCount: Int = 0
)

data class PeriodSummary(
  val totalIncome: Double,
  val totalExpense: Double,
  val netAmount: Double,
  val startDate: Long,
  val endDate: Long
)

/**
 * Input models for creating/updating entities
 */
data class CreateAccountInput(
  val name: String,
  val type: AccountType,
  val initialBalance: Double = 0.0,
  val currency: String = "CNY",
  val iconName: String? = null,
  val colorHex: String? = null
)

data class UpdateAccountInput(
  val id: Long,
  val name: String,
  val type: AccountType,
  val initialBalance: Double,
  val currency: String = "CNY",
  val isActive: Boolean = true,
  val iconName: String? = null,
  val colorHex: String? = null
)

data class CreateTransactionInput(
  val amount: Double,
  val type: TransactionType,
  val transactionDate: Long,
  val categoryId: Long? = null,
  val accountId: Long,
  val toAccountId: Long? = null,
  val payee: String? = null,
  val member: String? = null,
  val notes: String? = null,
  val tagIds: List<Long> = emptyList()
)

data class UpdateTransactionInput(
  val id: Long,
  val amount: Double,
  val type: TransactionType,
  val transactionDate: Long,
  val categoryId: Long? = null,
  val accountId: Long,
  val toAccountId: Long? = null,
  val payee: String? = null,
  val member: String? = null,
  val notes: String? = null,
  val tagIds: List<Long> = emptyList()
)

data class CreateCategoryInput(
  val name: String,
  val parentId: Long? = null,
  val type: CategoryType,
  val iconName: String? = null,
  val colorHex: String? = null
)

data class CreateTagInput(
  val name: String,
  val colorHex: String? = null
)

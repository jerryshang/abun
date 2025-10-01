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
  TRANSFER,
  LOAN,
  LOAN_PAYMENT;

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
 * Transaction Group Types
 */
enum class TransactionGroupType {
  LOAN,
  INSTALLMENT,
  SPLIT,
  CUSTOM;

  companion object {
    fun fromString(value: String): TransactionGroupType = values().find { it.name == value.uppercase() } ?: CUSTOM
  }
}

/**
 * Transaction Group Status
 */
enum class GroupStatus {
  ACTIVE,
  COMPLETED,
  CANCELLED;

  companion object {
    fun fromString(value: String): GroupStatus = values().find { it.name == value.uppercase() } ?: ACTIVE
  }
}

/**
 * Loan Types
 */
enum class LoanType {
  INTEREST_FIRST, // 先息后本 (Interest first, principal last)
  EQUAL_PRINCIPAL, // 等额本金 (Equal principal)
  EQUAL_INSTALLMENT, // 等额本息 (Equal installment - principal + interest)
  INTEREST_ONLY; // 只还利息 (Interest only, no principal repayment)

  companion object {
    fun fromString(value: String): LoanType = values().find { it.name == value.uppercase() } ?: EQUAL_INSTALLMENT
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
  val groupId: Long? = null,
  val payee: String? = null,
  val member: String? = null,
  val notes: String? = null,
  val isFuture: Boolean = false,
  val isExecuted: Boolean = true,
  val loanMetadata: String? = null,
  val createdAt: Long,
  val updatedAt: Long
)

/**
 * Domain model for Transaction Group
 */
data class TransactionGroup(
  val id: Long,
  val name: String,
  val groupType: TransactionGroupType,
  val description: String? = null,
  val totalAmount: Double? = null,
  val status: GroupStatus = GroupStatus.ACTIVE,
  val createdAt: Long,
  val updatedAt: Long
)

/**
 * Loan Metadata (stored as JSON in transaction)
 */
data class LoanMetadata(
  val loanType: LoanType,
  val totalAmount: Double,
  // Annual interest rate (e.g., 0.05 for 5%)
  val interestRate: Double,
  val loanMonths: Int,
  // Day of month to pay (1-31)
  val paymentDay: Int,
  // Loan creation date
  val startDate: Long,
  // First payment date (must be at least 1 month after start)
  val firstPaymentDate: Long,
  // For specific payment calculation
  val principalAmount: Double? = null,
  // For specific payment calculation
  val interestAmount: Double? = null
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

data class CreateLoanInput(
  val amount: Double,
  val accountId: Long, // Borrow from this account
  val payee: String, // Lender name
  val loanType: LoanType,
  val interestRate: Double, // Annual interest rate (e.g., 0.05 for 5%)
  val loanMonths: Int,
  val paymentDay: Int, // Day of month to pay (1-31)
  val startDate: Long, // Loan creation date
  val notes: String? = null
)

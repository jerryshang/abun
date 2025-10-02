package dev.tireless.abun.finance

/**
 * Fixed IDs for the 5 fundamental accounts in the chart of accounts
 * These are initialized by SQLDelight and must never change
 */
object RootAccountIds {
  const val ASSET = 1L
  const val LIABILITY = 2L
  const val EQUITY = 3L
  const val REVENUE = 4L
  const val EXPENSE = 5L
}

/**
 * Account Types for Chart of Accounts (Double-Entry Accounting)
 */
enum class AccountType {
  ASSET,      // Resources owned (cash, bank, investments)
  LIABILITY,  // Debts owed (loans, credit cards)
  EQUITY,     // Owner's stake
  REVENUE,    // Income streams (salary, sales)
  EXPENSE;    // Costs incurred (food, rent, utilities)

  companion object {
    fun fromString(value: String): AccountType = values().find { it.name == value.uppercase() } ?: ASSET

    /**
     * Get the root account ID for this account type
     */
    fun AccountType.getRootId(): Long = when (this) {
      ASSET -> RootAccountIds.ASSET
      LIABILITY -> RootAccountIds.LIABILITY
      EQUITY -> RootAccountIds.EQUITY
      REVENUE -> RootAccountIds.REVENUE
      EXPENSE -> RootAccountIds.EXPENSE
    }
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
 * Transaction State
 */
enum class TransactionState {
  PLANNED, // Fixed recurring payments (rent, loan payments)
  ESTIMATED, // Variable recurring payments (utilities, electric)
  CONFIRMED; // Manually logged transactions

  companion object {
    fun fromString(value: String): TransactionState = values().find { it.name == value.uppercase() } ?: CONFIRMED
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
 * Domain model for Account (Chart of Accounts)
 * Account type is derived from hierarchy: traverse parent_id until reaching root account
 */
data class Account(
  val id: Long,
  val name: String,
  val parentId: Long? = null,           // NULL for root accounts (Asset, Liability, Equity, Revenue, Expense)
  val initialBalance: Double,
  val currentBalance: Double,
  val currency: String = "CNY",
  val isActive: Boolean = true,
  val isVisibleInUi: Boolean = true,    // Hide accounting accounts from users
  val iconName: String? = null,
  val colorHex: String? = null,
  // Day of month for credit card billing (1-31)
  val billDate: Int? = null,
  // Day of month for payment due (1-31)
  val paymentDate: Int? = null,
  // Credit limit for credit cards
  val creditLimit: Double? = null,
  val createdAt: Long,
  val updatedAt: Long
) {
  /**
   * Check if this is a root account (one of the 5 fundamental account types)
   */
  fun isRootAccount(): Boolean = parentId == null

  /**
   * Get the account type from the name if this is a root account
   */
  fun getRootAccountType(): AccountType? {
    if (!isRootAccount()) return null
    return AccountType.fromString(name)
  }
}

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
 * Domain model for Transaction (Pure Double-Entry)
 */
data class Transaction(
  val id: Long,
  val amount: Double,                   // Always positive
  val debitAccountId: Long,              // Account being debited
  val creditAccountId: Long,             // Account being credited
  val transactionDate: Long,
  val transferGroupId: String? = null,   // UUID to link transfer pairs (for UI grouping)
  val categoryId: Long? = null,          // User-facing category (for display only)
  val payee: String? = null,
  val member: String? = null,
  val notes: String? = null,
  val state: TransactionState = TransactionState.CONFIRMED,
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
  val debitAccount: Account,
  val creditAccount: Account,
  val tags: List<FinanceTag> = emptyList(),
  // Cached account types for performance (computed from hierarchy in repository)
  val debitAccountType: AccountType,
  val creditAccountType: AccountType
) {
  /**
   * Infer user-facing transaction type from debit/credit accounts
   */
  fun inferType(): TransactionType {
    return when {
      // Transfer: Both accounts are assets
      debitAccountType == AccountType.ASSET && creditAccountType == AccountType.ASSET -> TransactionType.TRANSFER
      // Expense: Debit is expense account
      debitAccountType == AccountType.EXPENSE -> TransactionType.EXPENSE
      // Income: Credit is revenue account
      creditAccountType == AccountType.REVENUE -> TransactionType.INCOME
      // Loan-related
      debitAccountType == AccountType.LIABILITY || creditAccountType == AccountType.LIABILITY -> TransactionType.LOAN
      else -> TransactionType.EXPENSE // Default fallback
    }
  }

  /**
   * Get the primary user-facing account (asset account for expense/income, source for transfer)
   */
  fun getPrimaryAccount(): Account {
    return when (inferType()) {
      TransactionType.EXPENSE -> creditAccount // The account paying (asset being credited)
      TransactionType.INCOME -> debitAccount   // The account receiving (asset being debited)
      TransactionType.TRANSFER -> creditAccount // Source account
      else -> debitAccount
    }
  }

  /**
   * Get the secondary account (null for expense/income, destination for transfer)
   */
  fun getSecondaryAccount(): Account? {
    return when (inferType()) {
      TransactionType.TRANSFER -> debitAccount // Destination account
      else -> null
    }
  }
}

/**
 * Helper extension to infer transaction type from account types
 */
fun Transaction.inferTypeFromAccountTypes(debitAccountType: AccountType, creditAccountType: AccountType): TransactionType {
  return when {
    debitAccountType == AccountType.ASSET && creditAccountType == AccountType.ASSET -> TransactionType.TRANSFER
    debitAccountType == AccountType.EXPENSE -> TransactionType.EXPENSE
    creditAccountType == AccountType.REVENUE -> TransactionType.INCOME
    debitAccountType == AccountType.LIABILITY || creditAccountType == AccountType.LIABILITY -> TransactionType.LOAN
    else -> TransactionType.EXPENSE
  }
}

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
  val parentId: Long? = null,
  val initialBalance: Double = 0.0,
  val currency: String = "CNY",
  val isVisibleInUi: Boolean = true,
  val iconName: String? = null,
  val colorHex: String? = null,
  val billDate: Int? = null,
  val paymentDate: Int? = null,
  val creditLimit: Double? = null
)

data class UpdateAccountInput(
  val id: Long,
  val name: String,
  val parentId: Long? = null,
  val initialBalance: Double,
  val currency: String = "CNY",
  val isActive: Boolean = true,
  val isVisibleInUi: Boolean = true,
  val iconName: String? = null,
  val colorHex: String? = null,
  val billDate: Int? = null,
  val paymentDate: Int? = null,
  val creditLimit: Double? = null
)

/**
 * User-facing transaction input (will be translated to debit/credit internally)
 */
data class CreateTransactionInput(
  val amount: Double,
  val type: TransactionType,         // UI-level type: EXPENSE, INCOME, TRANSFER
  val transactionDate: Long,
  val categoryId: Long? = null,      // For EXPENSE/INCOME only
  val accountId: Long,               // Primary account
  val toAccountId: Long? = null,     // For TRANSFER only
  val payee: String? = null,
  val member: String? = null,
  val notes: String? = null,
  val tagIds: List<Long> = emptyList()
)

data class UpdateTransactionInput(
  val id: Long,
  val amount: Double,
  val type: TransactionType,         // UI-level type: EXPENSE, INCOME, TRANSFER
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
  val accountId: Long, // Borrower account (where money goes)
  val lenderAccountId: Long, // Lender account (who is lending)
  val loanType: LoanType,
  val interestRate: Double, // Annual interest rate (e.g., 0.05 for 5%)
  val loanMonths: Int,
  val paymentDay: Int, // Day of month to pay (1-31)
  val startDate: Long, // Loan creation date
  val payee: String? = null,
  val notes: String? = null
)

/**
 * Loan payment breakdown
 */
data class LoanPayment(
  val principal: Double,
  val interest: Double,
  val total: Double
)

package dev.tireless.abun.finance

import kotlinx.serialization.Serializable

/**
 * Account configuration flags (bitwise)
 */
object AccountConfig {
  const val ACTIVE = 1 shl 0 // bit0: 1=active, 0=inactive
  const val COUNTABLE = 1 shl 1 // bit1: 1=include in summary, 0=not in summary
  const val VISIBLE = 1 shl 2 // bit2: 1=visible in UI, 0=invisible

  fun isActive(config: Long): Boolean = (config and ACTIVE.toLong()) != 0L

  fun isCountable(config: Long): Boolean = (config and COUNTABLE.toLong()) != 0L

  fun isVisible(config: Long): Boolean = (config and VISIBLE.toLong()) != 0L

  fun setActive(
    config: Long,
    active: Boolean,
  ): Long = if (active) config or ACTIVE.toLong() else config and ACTIVE.toLong().inv()

  fun setCountable(
    config: Long,
    countable: Boolean,
  ): Long = if (countable) config or COUNTABLE.toLong() else config and COUNTABLE.toLong().inv()

  fun setVisible(
    config: Long,
    visible: Boolean,
  ): Long = if (visible) config or VISIBLE.toLong() else config and VISIBLE.toLong().inv()
}

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
  ASSET, // Resources owned (cash, bank, investments)
  LIABILITY, // Debts owed (loans, credit cards)
  EQUITY, // Owner's stake
  REVENUE, // Income streams (salary, sales)
  EXPENSE, // Costs incurred (food, rent, utilities)
  ;

  companion object {
    fun fromString(value: String): AccountType = entries.find { it.name == value.uppercase() } ?: ASSET

    /**
     * Get the root account ID for this account type
     */
    fun AccountType.getRootId(): Long =
      when (this) {
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
@Serializable
enum class TransactionType {
  EXPENSE,
  INCOME,
  TRANSFER,
  LOAN,
  LOAN_PAYMENT,
  ;

  companion object {
    fun fromString(value: String): TransactionType = entries.find { it.name == value.uppercase() } ?: EXPENSE
  }
}

/**
 * Recurring Transaction Frequency
 */
enum class RecurringFrequency {
  DAILY,
  WEEKLY,
  MONTHLY,
  YEARLY,
  ;

  companion object {
    fun fromString(value: String): RecurringFrequency = entries.find { it.name == value.uppercase() } ?: MONTHLY
  }
}

/**
 * Transaction Group Types
 */
enum class TransactionGroupType {
  LOAN,
  INSTALLMENT,
  SPLIT,
  CUSTOM,
  ;

  companion object {
    fun fromString(value: String): TransactionGroupType = entries.find { it.name == value.uppercase() } ?: CUSTOM
  }
}

/**
 * Transaction State
 */
enum class TransactionState {
  PLANNED, // Fixed recurring payments (rent, loan payments)
  ESTIMATED, // Variable recurring payments (utilities, electric)
  CONFIRMED, // Manually logged transactions
  ;

  companion object {
    fun fromString(value: String): TransactionState = entries.find { it.name == value.uppercase() } ?: CONFIRMED
  }
}

/**
 * Loan Types
 */
enum class LoanType {
  INTEREST_FIRST, // Interest first, principal last
  EQUAL_PRINCIPAL, // Equal principal
  EQUAL_INSTALLMENT, // Equal installment - principal plus interest
  ;

  companion object {
    fun fromString(value: String): LoanType = entries.find { it.name == value.uppercase() } ?: EQUAL_INSTALLMENT
  }
}

/**
 * Domain model for Account (Chart of Accounts)
 * Account type is derived from hierarchy: traverse parent_id until reaching root account
 * Balance is calculated on-demand from transactions, not stored
 *
 * Note: creditLimit is stored as Long (actual * 10000), -1 for non-liability accounts
 */
data class Account(
  val id: Long,
  val name: String,
  val parentId: Long? = null, // NULL for root accounts (Asset, Liability, Equity, Revenue, Expense)
  val currency: String = "CNY",
  val config: Long = 7L, // Bitwise: bit0=active, bit1=countable, bit2=visible (default: all on)
  val iconName: String? = null,
  val colorHex: String? = null,
  // Day of month for credit card billing (1-28, compatible with Feb)
  val billDate: Int? = null,
  // Day of month for payment due (1-28, compatible with Feb)
  val paymentDate: Int? = null,
  // Credit limit stored as Long (actual * 10000), -1 for non-liability accounts
  val creditLimitStorage: Long = -1L,
  val createdAt: Long,
  val updatedAt: Long,
) {
  /**
   * Check if account is active
   */
  val isActive: Boolean
    get() = AccountConfig.isActive(config)

  /**
   * Check if account should be counted in summaries
   */
  val isCountable: Boolean
    get() = AccountConfig.isCountable(config)

  /**
   * Check if account is visible in UI
   */
  val isVisibleInUi: Boolean
    get() = AccountConfig.isVisible(config)

  /**
   * Get credit limit as display amount (Double)
   * Returns null if not a liability account
   */
  val creditLimit: Double?
    get() = if (creditLimitStorage < 0) null else creditLimitStorage.toDisplayAmount()

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
 * Account with calculated balance
 * Used for display purposes where balance is needed
 */
data class AccountWithBalance(
  val account: Account,
  val currentBalance: Double,
) {
  // Delegate properties
  val id get() = account.id
  val name get() = account.name
  val parentId get() = account.parentId
  val currency get() = account.currency
  val config get() = account.config
  val isActive get() = account.isActive
  val isCountable get() = account.isCountable
  val isVisibleInUi get() = account.isVisibleInUi
  val iconName get() = account.iconName
  val colorHex get() = account.colorHex
  val billDate get() = account.billDate
  val paymentDate get() = account.paymentDate
  val creditLimit get() = account.creditLimit
  val createdAt get() = account.createdAt
  val updatedAt get() = account.updatedAt
}

/**
 * Domain model for Transaction (Pure Double-Entry)
 * Note: amount is stored as Long (actual * 10000) for precision
 */
data class Transaction(
  val id: Long,
  val amountStorage: Long, // Always positive, stored as actual * 10000
  val debitAccountId: Long, // Account being debited
  val creditAccountId: Long, // Account being credited
  val transactionDate: Long,
  val transferGroupId: String? = null, // UUID to link transfer pairs (for UI grouping)
  val payee: String? = null,
  val member: String? = null,
  val notes: String? = null,
  val state: TransactionState = TransactionState.CONFIRMED,
  val createdAt: Long,
  val updatedAt: Long,
) {
  /**
   * Get amount as display value (Double)
   */
  val amount: Double
    get() = amountStorage.toDisplayAmount()
}

/**
 * Domain model for Transaction Group (Pure Virtual Grouping)
 * This is purely a logical grouping mechanism without financial meaning.
 * Total amounts and status should be calculated from member transactions.
 */
data class TransactionGroup(
  val id: Long,
  val name: String,
  val groupType: TransactionGroupType,
  val description: String? = null,
  val createdAt: Long,
  val updatedAt: Long,
)

data class TransactionGroupWithTransactions(
  val group: TransactionGroup,
  val transactions: List<TransactionWithDetails>,
)

data class TransactionDeletionContext(
  val groups: List<TransactionGroupWithTransactions>,
) {
  companion object {
    val Empty = TransactionDeletionContext(emptyList())
  }
}

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
  val interestAmount: Double? = null,
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
  val updatedAt: Long,
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
  val updatedAt: Long,
)

/**
 * Rich transaction model with account details
 */
data class TransactionWithDetails(
  val transaction: Transaction,
  val debitAccount: Account,
  val creditAccount: Account,
  // Cached account types for performance (computed from hierarchy in repository)
  val debitAccountType: AccountType,
  val creditAccountType: AccountType,
  val groups: List<TransactionGroup> = emptyList(),
) {
  /**
   * Infer user-facing transaction type from debit/credit accounts
   */
  fun inferType(): TransactionType =
    when {
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

  /**
   * Get the primary user-facing account (asset account for expense/income, source for transfer)
   */
  fun getPrimaryAccount(): Account =
    when (inferType()) {
      TransactionType.EXPENSE -> creditAccount // The account paying (asset being credited)
      TransactionType.INCOME -> debitAccount // The account receiving (asset being debited)
      TransactionType.TRANSFER -> creditAccount // Source account
      else -> debitAccount
    }

  /**
   * Get the secondary account (null for expense/income, destination for transfer)
   */
  fun getSecondaryAccount(): Account? =
    when (inferType()) {
      TransactionType.TRANSFER -> debitAccount // Destination account
      else -> null
    }
}

/**
 * Sum balances for user-visible net worth calculation.
 * Includes only active, countable asset and liability accounts.
 */
fun List<AccountWithBalance>.totalCountableBalance(): Double =
  this
    .filter { accountWithBalance ->
      accountWithBalance.isActive &&
        accountWithBalance.isCountable &&
        (accountWithBalance.parentId == RootAccountIds.ASSET || accountWithBalance.parentId == RootAccountIds.LIABILITY)
    }.sumOf { it.currentBalance }

fun TransactionWithDetails.toEditPayload(): TransactionEditPayload? {
  val transactionType = inferType()
  val transaction = transaction

  return when (transactionType) {
    TransactionType.EXPENSE ->
      TransactionEditPayload(
        id = transaction.id,
        type = transactionType,
        amount = transaction.amount,
        transactionDate = transaction.transactionDate,
        accountId = transaction.debitAccountId,
        toAccountId = transaction.creditAccountId,
        payee = transaction.payee,
        member = transaction.member,
        notes = transaction.notes,
      )

    TransactionType.INCOME ->
      TransactionEditPayload(
        id = transaction.id,
        type = transactionType,
        amount = transaction.amount,
        transactionDate = transaction.transactionDate,
        accountId = transaction.creditAccountId,
        toAccountId = transaction.debitAccountId,
        payee = transaction.payee,
        member = transaction.member,
        notes = transaction.notes,
      )

    TransactionType.TRANSFER ->
      TransactionEditPayload(
        id = transaction.id,
        type = transactionType,
        amount = transaction.amount,
        transactionDate = transaction.transactionDate,
        accountId = transaction.creditAccountId,
        toAccountId = transaction.debitAccountId,
        payee = transaction.payee,
        member = transaction.member,
        notes = transaction.notes,
      )

    TransactionType.LOAN, TransactionType.LOAN_PAYMENT -> null
  }
}

/**
 * Helper extension to infer transaction type from account types
 */
fun Transaction.inferTypeFromAccountTypes(
  debitAccountType: AccountType,
  creditAccountType: AccountType,
): TransactionType =
  when {
    debitAccountType == AccountType.ASSET && creditAccountType == AccountType.ASSET -> TransactionType.TRANSFER
    debitAccountType == AccountType.EXPENSE -> TransactionType.EXPENSE
    creditAccountType == AccountType.REVENUE -> TransactionType.INCOME
    debitAccountType == AccountType.LIABILITY || creditAccountType == AccountType.LIABILITY -> TransactionType.LOAN
    else -> TransactionType.EXPENSE
  }

/**
 * Statistics data models
 */
data class PeriodSummary(
  val totalIncome: Double,
  val totalExpense: Double,
  val netAmount: Double,
  val startDate: Long,
  val endDate: Long,
)

/**
 * Input models for creating/updating entities
 */
data class CreateAccountInput(
  val name: String,
  val parentId: Long? = null,
  val currency: String = "CNY",
  val isActive: Boolean = true,
  val isCountable: Boolean = true,
  val isVisibleInUi: Boolean = true,
  val iconName: String? = null,
  val colorHex: String? = null,
  val billDate: Int? = null,
  val paymentDate: Int? = null,
  // User provides as Double, will be converted to storage
  val creditLimit: Double? = null,
) {
  init {
    require(billDate == null || billDate in 1..28) { "billDate must be between 1 and 28" }
    require(paymentDate == null || paymentDate in 1..28) { "paymentDate must be between 1 and 28" }
  }

  /**
   * Convert credit limit to storage format
   * Returns -1 for non-liability accounts (null input)
   */
  fun getCreditLimitStorage(): Long = creditLimit?.toStorageAmount() ?: -1L
}

data class UpdateAccountInput(
  val id: Long,
  val name: String,
  val parentId: Long? = null,
  val currency: String = "CNY",
  val isActive: Boolean = true,
  val isCountable: Boolean = true,
  val isVisibleInUi: Boolean = true,
  val iconName: String? = null,
  val colorHex: String? = null,
  val billDate: Int? = null,
  val paymentDate: Int? = null,
  // User provides as Double, will be converted to storage
  val creditLimit: Double? = null,
) {
  init {
    require(billDate == null || billDate in 1..28) { "billDate must be between 1 and 28" }
    require(paymentDate == null || paymentDate in 1..28) { "paymentDate must be between 1 and 28" }
  }

  /**
   * Convert credit limit to storage format
   * Returns -1 for non-liability accounts (null input)
   */
  fun getCreditLimitStorage(): Long = creditLimit?.toStorageAmount() ?: -1L
}

/**
 * User-facing transaction input (will be translated to debit/credit internally)
 */
data class CreateTransactionInput(
  val amount: Double,
  val type: TransactionType, // UI-level type: EXPENSE, INCOME, TRANSFER
  val transactionDate: Long,
  val accountId: Long, // Primary account
  val toAccountId: Long? = null, // For TRANSFER only
  val payee: String? = null,
  val member: String? = null,
  val notes: String? = null,
)

data class SplitExpenseEntry(
  val transactionId: Long? = null,
  val categoryId: Long,
  val amount: Double,
  val notes: String? = null,
)

data class SplitExpenseDraft(
  val groupId: Long? = null,
  val transactionDate: Long,
  val totalAmount: Double,
  val paymentAccountId: Long,
  val payee: String? = null,
  val member: String? = null,
  val entries: List<SplitExpenseEntry>,
  val groupNote: String? = null,
) {
  val entriesTotal: Double = entries.sumOf { it.amount }
}

@Serializable
data class TransactionEditPayload(
  val id: Long,
  val type: TransactionType,
  val amount: Double,
  val transactionDate: Long,
  val accountId: Long,
  val toAccountId: Long? = null,
  val payee: String? = null,
  val member: String? = null,
  val notes: String? = null,
)

data class UpdateTransactionInput(
  val id: Long,
  val amount: Double,
  val type: TransactionType, // UI-level type: EXPENSE, INCOME, TRANSFER
  val transactionDate: Long,
  val accountId: Long,
  val toAccountId: Long? = null,
  val payee: String? = null,
  val member: String? = null,
  val notes: String? = null,
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
  val notes: String? = null,
)

/**
 * Loan payment breakdown
 */
data class LoanPayment(
  val principal: Double,
  val interest: Double,
  val total: Double,
)

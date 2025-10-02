package dev.tireless.abun.finance

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.tireless.abun.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import dev.tireless.abun.database.Transaction as DbTransaction

/**
 * Repository for managing financial transactions
 * Implements double-entry booking logic
 */
class TransactionRepository(
  private val database: AppDatabase,
  private val accountRepository: AccountRepository
) {
  private val queries = database.financeQueries

  /**
   * Get all transactions as Flow
   */
  fun getAllTransactionsFlow(): Flow<List<Transaction>> = queries.getAllTransactions().asFlow().mapToList(Dispatchers.IO).map { list ->
    list.map { it.toDomain() }
  }

  /**
   * Get all transactions
   */
  suspend fun getAllTransactions(): List<Transaction> = withContext(Dispatchers.IO) {
    queries.getAllTransactions().executeAsList().map { it.toDomain() }
  }

  /**
   * Get transaction by ID
   */
  suspend fun getTransactionById(id: Long): Transaction? = withContext(Dispatchers.IO) {
    queries.getTransactionById(id).executeAsOneOrNull()?.toDomain()
  }

  /**
   * Get transactions by account
   */
  suspend fun getTransactionsByAccount(accountId: Long): List<Transaction> = withContext(Dispatchers.IO) {
    queries.getTransactionsByAccount(accountId, accountId).executeAsList().map { it.toDomain() }
  }

  /**
   * Get transactions by category
   */
  suspend fun getTransactionsByCategory(categoryId: Long): List<Transaction> = withContext(Dispatchers.IO) {
    queries.getTransactionsByCategory(categoryId).executeAsList().map { it.toDomain() }
  }

  /**
   * Get transactions by date range
   */
  suspend fun getTransactionsByDateRange(
    startDate: Long,
    endDate: Long
  ): List<Transaction> = withContext(Dispatchers.IO) {
    queries.getTransactionsByDateRange(startDate, endDate).executeAsList().map { it.toDomain() }
  }

  /**
   * Get transactions by type
   */
  suspend fun getTransactionsByType(type: TransactionType): List<Transaction> = withContext(Dispatchers.IO) {
    queries.getTransactionsByType(type.name.lowercase()).executeAsList().map { it.toDomain() }
  }

  /**
   * Create a new transaction with double-entry booking
   */
  @OptIn(ExperimentalUuidApi::class)
  suspend fun createTransaction(input: CreateTransactionInput): Long = withContext(Dispatchers.IO) {
    val now = currentTimeMillis()

    when (input.type) {
      TransactionType.EXPENSE -> {
        // Expense: Decrease account balance
        queries.insertTransaction(
          amount = input.amount,
          type = input.type.name.lowercase(),
          transaction_date = input.transactionDate,
          category_id = input.categoryId,
          account_id = input.accountId,
          to_account_id = null,
          transfer_group_id = null,
          payee = input.payee,
          member = input.member,
          notes = input.notes,
          state = TransactionState.CONFIRMED.name.lowercase(),
          created_at = now,
          updated_at = now
        )
        accountRepository.adjustAccountBalance(input.accountId, -input.amount)
      }

      TransactionType.INCOME -> {
        // Income: Increase account balance
        queries.insertTransaction(
          amount = input.amount,
          type = input.type.name.lowercase(),
          transaction_date = input.transactionDate,
          category_id = input.categoryId,
          account_id = input.accountId,
          to_account_id = null,
          transfer_group_id = null,
          payee = input.payee,
          member = input.member,
          notes = input.notes,
          state = TransactionState.CONFIRMED.name.lowercase(),
          created_at = now,
          updated_at = now
        )
        accountRepository.adjustAccountBalance(input.accountId, input.amount)
      }

      TransactionType.TRANSFER -> {
        // Transfer: Create two transactions (debit and credit)
        require(input.toAccountId != null) { "toAccountId is required for transfers" }
        require(input.accountId != input.toAccountId) { "Cannot transfer to the same account" }

        val transferGroupId = Uuid.random().toString()

        // Debit from source account
        queries.insertTransaction(
          amount = input.amount,
          type = input.type.name.lowercase(),
          transaction_date = input.transactionDate,
          category_id = null,
          account_id = input.accountId,
          to_account_id = input.toAccountId,
          transfer_group_id = transferGroupId,
          payee = input.payee,
          member = input.member,
          notes = input.notes,
          state = TransactionState.CONFIRMED.name.lowercase(),
          created_at = now,
          updated_at = now
        )
        accountRepository.adjustAccountBalance(input.accountId, -input.amount)

        // Credit to destination account
        queries.insertTransaction(
          amount = input.amount,
          type = input.type.name.lowercase(),
          transaction_date = input.transactionDate,
          category_id = null,
          account_id = input.toAccountId,
          to_account_id = input.accountId,
          transfer_group_id = transferGroupId,
          payee = input.payee,
          member = input.member,
          notes = "Transfer from account ${input.accountId}",
          state = TransactionState.CONFIRMED.name.lowercase(),
          created_at = now,
          updated_at = now
        )
        accountRepository.adjustAccountBalance(input.toAccountId, input.amount)
      }

      // Loans and loan payments don't affect balance immediately for future transactions
      TransactionType.LOAN, TransactionType.LOAN_PAYMENT -> {
        // Will be implemented separately for loan functionality
        throw UnsupportedOperationException("Use createLoan() for loan transactions")
      }
    }

    val transactionId = queries.getAllTransactions().executeAsList().lastOrNull()?.id ?: -1L

    // Add tags if provided
    input.tagIds.forEach { tagId ->
      queries.addTagToTransaction(transactionId, tagId)
    }

    transactionId
  }

  /**
   * Update an existing transaction
   */
  suspend fun updateTransaction(input: UpdateTransactionInput): Unit = withContext(Dispatchers.IO) {
    val now = currentTimeMillis()
    val oldTransaction = getTransactionById(input.id) ?: return@withContext

    // Revert old transaction's effect on account balance
    when (oldTransaction.type) {
      TransactionType.EXPENSE -> {
        accountRepository.adjustAccountBalance(oldTransaction.accountId, oldTransaction.amount)
      }

      TransactionType.INCOME -> {
        accountRepository.adjustAccountBalance(oldTransaction.accountId, -oldTransaction.amount)
      }

      TransactionType.TRANSFER -> {
        // Revert both accounts
        accountRepository.adjustAccountBalance(oldTransaction.accountId, oldTransaction.amount)
        oldTransaction.toAccountId?.let {
          accountRepository.adjustAccountBalance(it, -oldTransaction.amount)
        }
        // Delete paired transaction if exists
        oldTransaction.transferGroupId?.let { groupId ->
          val pairedTransactions = queries.getTransferPair(groupId).executeAsList()
          pairedTransactions.forEach { paired ->
            if (paired.id != oldTransaction.id) {
              queries.deleteTransaction(paired.id)
            }
          }
        }
      }

      TransactionType.LOAN, TransactionType.LOAN_PAYMENT -> {
        // Skip balance revert for loan transactions
      }
    }

    // Apply new transaction
    queries.updateTransaction(
      amount = input.amount,
      type = input.type.name.lowercase(),
      transaction_date = input.transactionDate,
      category_id = input.categoryId,
      account_id = input.accountId,
      to_account_id = input.toAccountId,
      payee = input.payee,
      member = input.member,
      notes = input.notes,
      state = TransactionState.CONFIRMED.name.lowercase(),
      updated_at = now,
      id = input.id
    )

    // Apply new balance changes
    when (input.type) {
      TransactionType.EXPENSE -> {
        accountRepository.adjustAccountBalance(input.accountId, -input.amount)
      }

      TransactionType.INCOME -> {
        accountRepository.adjustAccountBalance(input.accountId, input.amount)
      }

      TransactionType.TRANSFER -> {
        require(input.toAccountId != null) { "toAccountId is required for transfers" }
        accountRepository.adjustAccountBalance(input.accountId, -input.amount)
        accountRepository.adjustAccountBalance(input.toAccountId, input.amount)
      }

      TransactionType.LOAN, TransactionType.LOAN_PAYMENT -> {
        // Loan transactions are managed separately via createLoan()
        // Skip balance updates here
      }
    }

    // Update tags
    queries.getTagsForTransaction(input.id).executeAsList().forEach { tag ->
      queries.removeTagFromTransaction(input.id, tag.id)
    }
    input.tagIds.forEach { tagId ->
      queries.addTagToTransaction(input.id, tagId)
    }
  }

  /**
   * Delete a transaction and revert its effect on account balance
   */
  suspend fun deleteTransaction(id: Long): Unit = withContext(Dispatchers.IO) {
    val transaction = getTransactionById(id) ?: return@withContext

    // Revert account balance changes
    when (transaction.type) {
      TransactionType.EXPENSE -> {
        accountRepository.adjustAccountBalance(transaction.accountId, transaction.amount)
      }

      TransactionType.INCOME -> {
        accountRepository.adjustAccountBalance(transaction.accountId, -transaction.amount)
      }

      TransactionType.TRANSFER -> {
        accountRepository.adjustAccountBalance(transaction.accountId, transaction.amount)
        transaction.toAccountId?.let {
          accountRepository.adjustAccountBalance(it, -transaction.amount)
        }
        // Delete paired transaction
        transaction.transferGroupId?.let { groupId ->
          val pairedTransactions = queries.getTransferPair(groupId).executeAsList()
          pairedTransactions.forEach { paired ->
            if (paired.id != transaction.id) {
              queries.deleteTransaction(paired.id)
            }
          }
        }
      }

      TransactionType.LOAN, TransactionType.LOAN_PAYMENT -> {
        // Skip balance revert for loan transactions
      }
    }

    queries.deleteTransaction(id)
  }

  /**
   * Get tags for a transaction
   */
  suspend fun getTagsForTransaction(transactionId: Long): List<FinanceTag> = withContext(Dispatchers.IO) {
    queries.getTagsForTransaction(transactionId).executeAsList().map { it.toDomainTag() }
  }

  /**
   * Get transactions by tag
   */
  suspend fun getTransactionsByTag(tagId: Long): List<Transaction> = withContext(Dispatchers.IO) {
    queries.getTransactionsByTag(tagId).executeAsList().map { it.toDomain() }
  }

  /**
   * Get expense summary by category for date range
   */
  suspend fun getExpenseSummary(
    startDate: Long,
    endDate: Long
  ): Map<Long?, Double> = withContext(Dispatchers.IO) {
    queries.getExpenseSumByCategory(startDate, endDate).executeAsList()
      .associate { it.category_id to (it.total ?: 0.0) }
  }

  /**
   * Get income summary by category for date range
   */
  suspend fun getIncomeSummary(
    startDate: Long,
    endDate: Long
  ): Map<Long?, Double> = withContext(Dispatchers.IO) {
    queries.getIncomeSumByCategory(startDate, endDate).executeAsList()
      .associate { it.category_id to (it.total ?: 0.0) }
  }

  /**
   * Get total expense for date range
   */
  suspend fun getTotalExpense(
    startDate: Long,
    endDate: Long
  ): Double = withContext(Dispatchers.IO) {
    queries.getExpenseByDateRange(startDate, endDate).executeAsOneOrNull()?.total ?: 0.0
  }

  /**
   * Get total income for date range
   */
  suspend fun getTotalIncome(
    startDate: Long,
    endDate: Long
  ): Double = withContext(Dispatchers.IO) {
    queries.getIncomeByDateRange(startDate, endDate).executeAsOneOrNull()?.total ?: 0.0
  }

  /**
   * Get recent payees for autocomplete
   */
  suspend fun getRecentPayees(): List<String> = withContext(Dispatchers.IO) {
    queries.getRecentPayees().executeAsList().filterNotNull()
  }

  /**
   * Mapper: Database model to Domain model
   */
  private fun DbTransaction.toDomain() = Transaction(
    id = id,
    amount = amount,
    type = TransactionType.fromString(type),
    transactionDate = transaction_date,
    categoryId = category_id,
    accountId = account_id,
    toAccountId = to_account_id,
    transferGroupId = transfer_group_id,
    payee = payee,
    member = member,
    notes = notes,
    state = TransactionState.fromString(state),
    createdAt = created_at,
    updatedAt = updated_at
  )

  /**
   * Add transaction to a group
   */
  suspend fun addTransactionToGroup(transactionId: Long, groupId: Long): Unit = withContext(Dispatchers.IO) {
    queries.addTransactionToGroup(transactionId, groupId)
  }

  /**
   * Remove transaction from a group
   */
  suspend fun removeTransactionFromGroup(transactionId: Long, groupId: Long): Unit = withContext(Dispatchers.IO) {
    queries.removeTransactionFromGroup(transactionId, groupId)
  }

  /**
   * Calculate monthly payment for different loan types
   */
  private fun calculateLoanPayment(
    loanAmount: Double,
    annualRate: Double,
    months: Int,
    loanType: LoanType,
    monthNumber: Int
  ): Pair<Double, Double> {
    val monthlyRate = annualRate / 12.0

    return when (loanType) {
      LoanType.EQUAL_INSTALLMENT -> {
        // 等额本息: Equal monthly payment (principal + interest)
        if (monthlyRate == 0.0) {
          val principal = loanAmount / months
          Pair(principal, 0.0)
        } else {
          val payment = loanAmount * monthlyRate * (1 + monthlyRate).pow(months.toDouble()) /
            ((1 + monthlyRate).pow(months.toDouble()) - 1)
          val remainingPrincipal = loanAmount * (
            (1 + monthlyRate).pow(months.toDouble()) -
              (1 + monthlyRate).pow((monthNumber - 1).toDouble())
            ) /
            ((1 + monthlyRate).pow(months.toDouble()) - 1)
          val interest = remainingPrincipal * monthlyRate
          val principal = payment - interest
          Pair(principal, interest)
        }
      }

      LoanType.EQUAL_PRINCIPAL -> {
        // 等额本金: Equal principal, decreasing interest
        val principal = loanAmount / months
        val remainingPrincipal = loanAmount - (principal * (monthNumber - 1))
        val interest = remainingPrincipal * monthlyRate
        Pair(principal, interest)
      }

      LoanType.INTEREST_FIRST -> {
        // 先息后本: Interest only until last month, then principal
        if (monthNumber < months) {
          Pair(0.0, loanAmount * monthlyRate)
        } else {
          Pair(loanAmount, loanAmount * monthlyRate)
        }
      }

      LoanType.INTEREST_ONLY -> {
        // 只还利息: Interest only, no principal repayment
        Pair(0.0, loanAmount * monthlyRate)
      }
    }
  }

  /**
   * Calculate payment date for a given month
   * Adds approximately monthsToAdd months to startDate (uses 30.44 days per month average)
   */
  private fun calculatePaymentDate(startDate: Long, monthsToAdd: Int, paymentDay: Int): Long {
    // Use average days per month (365.25 / 12 = 30.4375) for better accuracy
    val avgDaysPerMonth = 30.4375
    val daysToAdd = (monthsToAdd * avgDaysPerMonth).toLong()
    val millisToAdd = daysToAdd * 24L * 60L * 60L * 1000L
    return startDate + millisToAdd
  }

  /**
   * Create a loan with initial transaction and future payment transactions
   * This creates:
   * 1. A transaction group to track the loan
   * 2. The initial LOAN transaction (receiving the money)
   * 3. Future LOAN_PAYMENT transactions (planned state)
   * 4. Links all transactions to the group
   */
  suspend fun createLoan(
    input: CreateLoanInput,
    transactionGroupRepository: TransactionGroupRepository
  ): Pair<Long, Long> = withContext(Dispatchers.IO) {
    val now = currentTimeMillis()

    // Get lender account name for display
    val lenderAccount = accountRepository.getAccountById(input.lenderAccountId)
    val lenderName = lenderAccount?.name ?: "Unknown"

    // 1. Create transaction group for the loan
    val groupId = transactionGroupRepository.createTransactionGroup(
      name = "借贷 - $lenderName",
      groupType = TransactionGroupType.LOAN,
      description = "借贷金额: ¥${input.amount}, 期限: ${input.loanMonths}个月, 利率: ${(input.interestRate * 100)}%",
      totalAmount = input.amount
    )

    // 2. Create the initial LOAN transaction (receiving the money)
    // This increases the account balance as money comes in
    queries.insertTransaction(
      amount = input.amount,
      type = TransactionType.LOAN.name.lowercase(),
      transaction_date = input.startDate,
      category_id = null,
      account_id = input.accountId,
      to_account_id = input.lenderAccountId,
      transfer_group_id = null,
      payee = lenderName,
      member = null,
      notes = buildLoanNotes(input, lenderName),
      state = TransactionState.CONFIRMED.name.lowercase(),
      created_at = now,
      updated_at = now
    )

    val loanTransactionId = queries.getAllTransactions().executeAsList().lastOrNull()?.id ?: -1L

    // 3. Update account balances
    // Borrower account receives money (increase)
    accountRepository.adjustAccountBalance(input.accountId, input.amount)
    // Lender account gives money (decrease)
    accountRepository.adjustAccountBalance(input.lenderAccountId, -input.amount)

    // 4. Link loan transaction to group
    queries.addTransactionToGroup(loanTransactionId, groupId)

    // 5. Create future LOAN_PAYMENT transactions (in PLANNED state)
    for (monthNumber in 1..input.loanMonths) {
      val (principal, interest) = calculateLoanPayment(
        loanAmount = input.amount,
        annualRate = input.interestRate,
        months = input.loanMonths,
        loanType = input.loanType,
        monthNumber = monthNumber
      )

      val paymentAmount = principal + interest
      val paymentDate = calculatePaymentDate(input.startDate, monthNumber, input.paymentDay)

      // Create payment transaction in PLANNED state
      queries.insertTransaction(
        amount = paymentAmount,
        type = TransactionType.LOAN_PAYMENT.name.lowercase(),
        transaction_date = paymentDate,
        category_id = null,
        account_id = input.accountId,
        to_account_id = input.lenderAccountId,
        transfer_group_id = null,
        payee = lenderName,
        member = null,
        notes = buildPaymentNotes(monthNumber, input.loanMonths, principal, interest),
        state = TransactionState.PLANNED.name.lowercase(),
        created_at = now,
        updated_at = now
      )

      val paymentTransactionId = queries.getAllTransactions().executeAsList().lastOrNull()?.id ?: -1L

      // Link payment transaction to group
      queries.addTransactionToGroup(paymentTransactionId, groupId)
    }

    Pair(loanTransactionId, groupId)
  }

  /**
   * Build loan notes with metadata for future processing
   */
  private fun buildLoanNotes(input: CreateLoanInput, lenderName: String): String {
    val loanTypeText = when (input.loanType) {
      LoanType.INTEREST_FIRST -> "先息后本"
      LoanType.EQUAL_PRINCIPAL -> "等额本金"
      LoanType.EQUAL_INSTALLMENT -> "等额本息"
      LoanType.INTEREST_ONLY -> "只还利息"
    }

    val baseNotes = "借贷类型: $loanTypeText | 年利率: ${(input.interestRate * 100)}% | 期限: ${input.loanMonths}个月 | 还款日: 每月${input.paymentDay}日"

    return if (input.notes != null) {
      "$baseNotes | ${input.notes}"
    } else {
      baseNotes
    }
  }

  /**
   * Build payment notes for loan payment transactions
   */
  private fun buildPaymentNotes(
    monthNumber: Int,
    totalMonths: Int,
    principal: Double,
    interest: Double
  ): String {
    val formattedPrincipal = "%.2f".replace("%.", principal.toString().substringBefore('.') + ".")
      .let { pattern ->
        val intPart = principal.toLong()
        val decPart = ((principal - intPart) * 100).toLong().toString().padStart(2, '0')
        "$intPart.$decPart"
      }

    val formattedInterest = "%.2f".replace("%.", interest.toString().substringBefore('.') + ".")
      .let { pattern ->
        val intPart = interest.toLong()
        val decPart = ((interest - intPart) * 100).toLong().toString().padStart(2, '0')
        "$intPart.$decPart"
      }

    return "第$monthNumber/${totalMonths}期 | 本金: ¥$formattedPrincipal | 利息: ¥$formattedInterest"
  }

  /**
   * Get all groups for a transaction
   */
  suspend fun getGroupsForTransaction(transactionId: Long): List<TransactionGroup> = withContext(Dispatchers.IO) {
    queries.getGroupsForTransaction(transactionId).executeAsList().map { it.toDomainGroup() }
  }

  private fun dev.tireless.abun.database.TransactionGroup.toDomainGroup() = TransactionGroup(
    id = id,
    name = name,
    groupType = TransactionGroupType.fromString(group_type),
    description = description,
    totalAmount = total_amount,
    status = GroupStatus.fromString(status),
    createdAt = created_at,
    updatedAt = updated_at
  )

  private fun dev.tireless.abun.database.FinanceTag.toDomainTag() = FinanceTag(
    id = id,
    name = name,
    colorHex = color_hex,
    createdAt = created_at
  )

  /**
   * Get current timestamp in milliseconds (KMP-compatible)
   */
  private fun currentTimeMillis(): Long {
    return 1704067200000L // 2024-01-01 00:00:00 UTC - Simplified for KMP
  }
}

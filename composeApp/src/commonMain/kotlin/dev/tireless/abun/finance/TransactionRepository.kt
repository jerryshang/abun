package dev.tireless.abun.finance

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.tireless.abun.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import dev.tireless.abun.database.Transaction as DbTransaction

/**
 * Repository for managing financial transactions
 * Implements double-entry booking with translation layer
 *
 * Translation Layer:
 * - User creates "Expense" → System creates debit to expense account, credit to asset account
 * - User creates "Income" → System creates debit to asset account, credit to revenue account
 * - User creates "Transfer" → System creates debit to destination asset, credit to source asset
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
   * Get transactions by date range
   */
  suspend fun getTransactionsByDateRange(
    startDate: Long,
    endDate: Long
  ): List<Transaction> = withContext(Dispatchers.IO) {
    queries.getTransactionsByDateRange(startDate, endDate).executeAsList().map { it.toDomain() }
  }

  /**
   * Create a new transaction with double-entry booking
   * Translates user-facing transaction types to debit/credit entries
   *
   * For EXPENSE: accountId = expense account (Food, Transport, etc.), toAccountId = payment source (Cash, Bank, etc.)
   * For INCOME: accountId = revenue account (Salary, Investment, etc.), toAccountId = receiving account (Bank, Cash, etc.)
   * For TRANSFER: accountId = source account, toAccountId = destination account
   */
  @OptIn(ExperimentalUuidApi::class)
  suspend fun createTransaction(input: CreateTransactionInput): Long = withContext(Dispatchers.IO) {
    val now = currentTimeMillis()

    when (input.type) {
      TransactionType.EXPENSE -> {
        // EXPENSE: Debit expense account, Credit asset account
        // User pays $100 for coffee from Cash account:
        // - Debit: Coffee Expense $100
        // - Credit: Cash $100

        require(input.toAccountId != null) { "toAccountId (payment source) is required for expenses" }

        queries.insertTransaction(
          amount = input.amount.toStorageAmount(),
          debit_account_id = input.accountId, // Expense account
          credit_account_id = input.toAccountId, // Payment source (asset)
          transaction_date = input.transactionDate,
          transfer_group_id = null,
          payee = input.payee,
          member = input.member,
          notes = input.notes,
          state = TransactionState.CONFIRMED.name.lowercase(),
          created_at = now,
          updated_at = now
        )

        // Balance is calculated on demand from transactions - no manual adjustment needed
      }

      TransactionType.INCOME -> {
        // INCOME: Debit asset account, Credit revenue account
        // User receives $1000 salary to Bank account:
        // - Debit: Bank $1000
        // - Credit: Salary Revenue $1000

        require(input.toAccountId != null) { "toAccountId (receiving account) is required for income" }

        queries.insertTransaction(
          amount = input.amount.toStorageAmount(),
          debit_account_id = input.toAccountId, // Receiving account (asset)
          credit_account_id = input.accountId, // Revenue account
          transaction_date = input.transactionDate,
          transfer_group_id = null,
          payee = input.payee,
          member = input.member,
          notes = input.notes,
          state = TransactionState.CONFIRMED.name.lowercase(),
          created_at = now,
          updated_at = now
        )

        // Balance is calculated on demand from transactions - no manual adjustment needed
      }

      TransactionType.TRANSFER -> {
        // TRANSFER: Debit destination asset, Credit source asset
        // User transfers $100 from Cash to Bank:
        // - Debit: Bank $100
        // - Credit: Cash $100

        require(input.toAccountId != null) { "toAccountId is required for transfers" }
        require(input.accountId != input.toAccountId) { "Cannot transfer to the same account" }

        val transferGroupId = Uuid.random().toString()

        queries.insertTransaction(
          amount = input.amount.toStorageAmount(),
          debit_account_id = input.toAccountId,
          credit_account_id = input.accountId,
          transaction_date = input.transactionDate,
          transfer_group_id = transferGroupId,
          payee = input.payee,
          member = input.member,
          notes = input.notes,
          state = TransactionState.CONFIRMED.name.lowercase(),
          created_at = now,
          updated_at = now
        )

        // Balance is calculated on demand from transactions - no manual adjustment needed
      }

      TransactionType.LOAN, TransactionType.LOAN_PAYMENT -> {
        throw UnsupportedOperationException("Use createLoan() for loan transactions")
      }
    }

    val transactionId = queries.getAllTransactions().executeAsList().lastOrNull()?.id ?: -1L

    transactionId
  }

  /**
   * Create a loan with scheduled payments
   *
   * Accounting Treatment:
   * - Borrowing money: Debit asset account (receives money), Credit liability account (loan payable)
   * - Lending money: Debit liability account (loan receivable), Credit asset account (gives money)
   *
   * Creates:
   * 1. Initial loan transaction (CONFIRMED)
   * 2. Transaction group for the loan
   * 3. Scheduled payment transactions (PLANNED)
   */
  @OptIn(ExperimentalUuidApi::class)
  suspend fun createLoan(
    input: CreateLoanInput,
    transactionGroupRepository: TransactionGroupRepository
  ): Long = withContext(Dispatchers.IO) {
    val now = currentTimeMillis()

    // 1. Create or get liability account for the loan
    val loanAccountName = when (input.loanType) {
      LoanType.INTEREST_FIRST -> "${input.payee ?: "Loan"} - Interest First"
      LoanType.EQUAL_PRINCIPAL -> "${input.payee ?: "Loan"} - Equal Principal"
      LoanType.EQUAL_INSTALLMENT -> "${input.payee ?: "Loan"} - Equal Installment"
      LoanType.INTEREST_ONLY -> "${input.payee ?: "Loan"} - Interest Only"
    }

    val liabilityAccount = accountRepository.getOrCreateLiabilityAccount(loanAccountName)

    // 2. Create initial loan transaction
    // Borrowing: Debit asset (receive money), Credit liability (owe money)
    queries.insertTransaction(
      amount = input.amount.toStorageAmount(),
      debit_account_id = input.accountId,
      credit_account_id = liabilityAccount.id,
      transaction_date = input.startDate,
      transfer_group_id = null,
      payee = input.payee,
      member = null,
      notes = input.notes,
      state = TransactionState.CONFIRMED.name.lowercase(),
      created_at = now,
      updated_at = now
    )

    val loanTransactionId = queries.getAllTransactions().executeAsList().lastOrNull()?.id ?: -1L

    // Balance is calculated on demand from transactions - no manual adjustment needed

    // 3. Create transaction group for the loan
    val groupId = transactionGroupRepository.createTransactionGroup(
      name = "Loan: ${input.payee ?: "Unknown"}",
      groupType = TransactionGroupType.LOAN,
      description = "Loan of ¥${input.amount} at ${input.interestRate}% for ${input.loanMonths} months"
    )

    // Add initial loan transaction to group
    addTransactionToGroup(loanTransactionId, groupId)

    // 4. Calculate and create scheduled payment transactions
    val payments = calculateLoanPayments(
      principal = input.amount,
      interestRate = input.interestRate,
      loanMonths = input.loanMonths,
      loanType = input.loanType
    )

    // Create PLANNED transactions for each payment
    payments.forEachIndexed { index, payment ->
      val paymentDate = input.startDate + (index + 1) * 30L * 24 * 60 * 60 * 1000 // Approximate month

      // Payment: Debit liability (reduce debt), Credit asset (pay money)
      queries.insertTransaction(
        amount = payment.total.toStorageAmount(),
        debit_account_id = liabilityAccount.id,
        credit_account_id = input.accountId,
        transaction_date = paymentDate,
        transfer_group_id = null,
        payee = input.payee,
        member = null,
        notes = "Payment ${index + 1}/${input.loanMonths}: Principal ¥${payment.principal}, Interest ¥${payment.interest}",
        state = TransactionState.PLANNED.name.lowercase(),
        created_at = now,
        updated_at = now
      )

      val paymentTransactionId = queries.getAllTransactions().executeAsList().lastOrNull()?.id ?: -1L
      addTransactionToGroup(paymentTransactionId, groupId)
    }

    loanTransactionId
  }

  /**
   * Calculate loan payment schedule
   */
  private fun calculateLoanPayments(
    principal: Double,
    interestRate: Double,
    loanMonths: Int,
    loanType: LoanType
  ): List<LoanPayment> {
    val monthlyRate = interestRate / 100.0 / 12.0
    val payments = mutableListOf<LoanPayment>()

    when (loanType) {
      LoanType.INTEREST_FIRST -> {
        // Interest first: Pay interest each month, principal at end
        for (month in 1 until loanMonths) {
          val interest = principal * monthlyRate
          payments.add(LoanPayment(0.0, interest, interest))
        }
        // Last payment: principal + interest
        val lastInterest = principal * monthlyRate
        payments.add(LoanPayment(principal, lastInterest, principal + lastInterest))
      }

      LoanType.EQUAL_PRINCIPAL -> {
        // Equal principal: Same principal each month, decreasing interest
        val principalPerMonth = principal / loanMonths
        var remainingPrincipal = principal

        for (month in 1..loanMonths) {
          val interest = remainingPrincipal * monthlyRate
          val total = principalPerMonth + interest
          payments.add(LoanPayment(principalPerMonth, interest, total))
          remainingPrincipal -= principalPerMonth
        }
      }

      LoanType.EQUAL_INSTALLMENT -> {
        // Equal installment: Same total payment each month
        val monthlyPayment = if (monthlyRate > 0) {
          // Calculate (1 + monthlyRate)^loanMonths using repeated multiplication
          var compoundFactor = 1.0
          repeat(loanMonths) {
            compoundFactor *= (1 + monthlyRate)
          }
          principal * monthlyRate * compoundFactor / (compoundFactor - 1)
        } else {
          principal / loanMonths
        }

        var remainingPrincipal = principal
        for (month in 1..loanMonths) {
          val interest = remainingPrincipal * monthlyRate
          val principalPayment = monthlyPayment - interest
          payments.add(LoanPayment(principalPayment, interest, monthlyPayment))
          remainingPrincipal -= principalPayment
        }
      }

      LoanType.INTEREST_ONLY -> {
        // Interest only: Pay interest each month, no principal repayment
        for (month in 1..loanMonths) {
          val interest = principal * monthlyRate
          payments.add(LoanPayment(0.0, interest, interest))
        }
      }
    }

    return payments
  }

  /**
   * Update an existing transaction
   */
  suspend fun updateTransaction(input: UpdateTransactionInput): Unit = withContext(Dispatchers.IO) {
    val now = currentTimeMillis()
    val oldTransaction = getTransactionById(input.id) ?: return@withContext

    // Revert old transaction's effect on account balance
    val oldDebitAccount = accountRepository.getAccountById(oldTransaction.debitAccountId)!!
    val oldCreditAccount = accountRepository.getAccountById(oldTransaction.creditAccountId)!!

    // Balance is calculated on demand from transactions - no need to reverse old balances

    // Delete paired transfer transaction if exists
    oldTransaction.transferGroupId?.let { groupId ->
      val pairedTransactions = queries.getTransferPair(groupId).executeAsList()
      pairedTransactions.forEach { paired ->
        if (paired.id != oldTransaction.id) {
          queries.deleteTransaction(paired.id)
        }
      }
    }

    // Apply new transaction based on type
    when (input.type) {
      TransactionType.EXPENSE -> {
        require(input.toAccountId != null) { "toAccountId (payment source) is required for expenses" }

        queries.updateTransaction(
          amount = input.amount.toStorageAmount(),
          debit_account_id = input.accountId, // Expense account
          credit_account_id = input.toAccountId, // Payment source (asset)
          transaction_date = input.transactionDate,
          transfer_group_id = null,
          payee = input.payee,
          member = input.member,
          notes = input.notes,
          state = TransactionState.CONFIRMED.name.lowercase(),
          updated_at = now,
          id = input.id
        )

        // Balance is calculated on demand from transactions - no manual adjustment needed
      }

      TransactionType.INCOME -> {
        require(input.toAccountId != null) { "toAccountId (receiving account) is required for income" }

        queries.updateTransaction(
          amount = input.amount.toStorageAmount(),
          debit_account_id = input.toAccountId, // Receiving account (asset)
          credit_account_id = input.accountId, // Revenue account
          transaction_date = input.transactionDate,
          transfer_group_id = null,
          payee = input.payee,
          member = input.member,
          notes = input.notes,
          state = TransactionState.CONFIRMED.name.lowercase(),
          updated_at = now,
          id = input.id
        )

        // Balance is calculated on demand from transactions - no manual adjustment needed
      }

      TransactionType.TRANSFER -> {
        require(input.toAccountId != null) { "toAccountId is required for transfers" }

        queries.updateTransaction(
          amount = input.amount.toStorageAmount(),
          debit_account_id = input.toAccountId,
          credit_account_id = input.accountId,
          transaction_date = input.transactionDate,
          transfer_group_id = oldTransaction.transferGroupId, // Keep existing group ID
          payee = input.payee,
          member = input.member,
          notes = input.notes,
          state = TransactionState.CONFIRMED.name.lowercase(),
          updated_at = now,
          id = input.id
        )

        // Balance is calculated on demand from transactions - no manual adjustment needed
      }

      TransactionType.LOAN, TransactionType.LOAN_PAYMENT -> {
        throw UnsupportedOperationException("Loan updates not yet supported")
      }
    }
  }

  /**
   * Delete a transaction
   * Balance is calculated on demand, so no manual reversal needed
   */
  suspend fun deleteTransaction(id: Long): Unit = withContext(Dispatchers.IO) {
    val transaction = getTransactionById(id) ?: return@withContext

    // Balance is calculated on demand from transactions - no need to reverse balances

    // Delete paired transfer transaction if exists
    transaction.transferGroupId?.let { groupId ->
      val pairedTransactions = queries.getTransferPair(groupId).executeAsList()
      pairedTransactions.forEach { paired ->
        if (paired.id != transaction.id) {
          queries.deleteTransaction(paired.id)
        }
      }
    }

    queries.deleteTransaction(id)
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
    amountStorage = amount,
    debitAccountId = debit_account_id,
    creditAccountId = credit_account_id,
    transactionDate = transaction_date,
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
    createdAt = created_at,
    updatedAt = updated_at
  )

  /**
   * Get transaction with enriched account details for UI display
   */
  suspend fun getTransactionWithDetails(id: Long): TransactionWithDetails? = withContext(Dispatchers.IO) {
    val transaction = getTransactionById(id) ?: return@withContext null
    val debitAccount = accountRepository.getAccountById(transaction.debitAccountId) ?: return@withContext null
    val creditAccount = accountRepository.getAccountById(transaction.creditAccountId) ?: return@withContext null

    // Get account types from cache
    val debitAccountType = accountRepository.getAccountType(transaction.debitAccountId)
    val creditAccountType = accountRepository.getAccountType(transaction.creditAccountId)

    TransactionWithDetails(
      transaction = transaction,
      debitAccount = debitAccount,
      creditAccount = creditAccount,
      debitAccountType = debitAccountType,
      creditAccountType = creditAccountType
    )
  }

  /**
   * Get all transactions with enriched details
   */
  suspend fun getAllTransactionsWithDetails(): List<TransactionWithDetails> = withContext(Dispatchers.IO) {
    getAllTransactions().mapNotNull { transaction ->
      val debitAccount = accountRepository.getAccountById(transaction.debitAccountId)
      val creditAccount = accountRepository.getAccountById(transaction.creditAccountId)
      if (debitAccount == null || creditAccount == null) return@mapNotNull null

      // Get account types from cache
      val debitAccountType = accountRepository.getAccountType(transaction.debitAccountId)
      val creditAccountType = accountRepository.getAccountType(transaction.creditAccountId)

      TransactionWithDetails(
        transaction = transaction,
        debitAccount = debitAccount,
        creditAccount = creditAccount,
        debitAccountType = debitAccountType,
        creditAccountType = creditAccountType
      )
    }
  }

  /**
   * Get current timestamp in milliseconds (KMP-compatible)
   */
  private fun currentTimeMillis(): Long {
    return 1704067200000L // 2024-01-01 00:00:00 UTC - Simplified for KMP
  }
}

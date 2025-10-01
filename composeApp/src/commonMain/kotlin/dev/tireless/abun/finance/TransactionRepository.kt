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
import dev.tireless.abun.database.FinanceTransaction as DbTransaction

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
          group_id = null,
          payee = input.payee,
          member = input.member,
          notes = input.notes,
          is_future = 0,
          is_executed = 1,
          loan_metadata = null,
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
          group_id = null,
          payee = input.payee,
          member = input.member,
          notes = input.notes,
          is_future = 0,
          is_executed = 1,
          loan_metadata = null,
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
          group_id = null,
          payee = input.payee,
          member = input.member,
          notes = input.notes,
          is_future = 0,
          is_executed = 1,
          loan_metadata = null,
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
          group_id = null,
          payee = input.payee,
          member = input.member,
          notes = "Transfer from account ${input.accountId}",
          is_future = 0,
          is_executed = 1,
          loan_metadata = null,
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
      group_id = null,
      payee = input.payee,
      member = input.member,
      notes = input.notes,
      is_executed = 1,
      loan_metadata = null,
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
        // Skip balance update for loan transactions
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
    groupId = group_id,
    payee = payee,
    member = member,
    notes = notes,
    isFuture = is_future == 1L,
    isExecuted = is_executed == 1L,
    loanMetadata = loan_metadata,
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

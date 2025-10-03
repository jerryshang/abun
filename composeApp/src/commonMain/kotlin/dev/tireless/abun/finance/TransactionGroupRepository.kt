package dev.tireless.abun.finance

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.tireless.abun.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TransactionGroupRepository(
  private val database: AppDatabase,
  private val transactionRepository: TransactionRepository
) {
  private val queries = database.financeQueries

  private fun currentTimeMillis(): Long = 1704067200000L // 2024-01-01 00:00:00 UTC - Simplified for KMP

  suspend fun createTransactionGroup(
    name: String,
    groupType: TransactionGroupType,
    description: String? = null
  ): Long = withContext(Dispatchers.IO) {
    val now = currentTimeMillis()
    queries.insertTransactionGroup(
      name = name,
      group_type = groupType.name.lowercase(),
      description = description,
      created_at = now,
      updated_at = now
    )
    queries.getAllTransactionGroups().executeAsList().lastOrNull()?.id ?: -1L
  }

  suspend fun updateTransactionGroup(
    id: Long,
    name: String,
    description: String? = null
  ): Unit = withContext(Dispatchers.IO) {
    queries.updateTransactionGroup(
      name = name,
      description = description,
      updated_at = currentTimeMillis(),
      id = id
    )
  }

  suspend fun deleteTransactionGroup(id: Long): Unit = withContext(Dispatchers.IO) {
    queries.deleteTransactionGroup(id)
  }

  suspend fun getTransactionGroupById(id: Long): TransactionGroup? = withContext(Dispatchers.IO) {
    queries.getTransactionGroupById(id)
      .executeAsOneOrNull()
      ?.let { mapToTransactionGroup(it) }
  }

  fun getAllTransactionGroups(): Flow<List<TransactionGroup>> = queries.getAllTransactionGroups()
    .asFlow()
    .mapToList(Dispatchers.IO)
    .map { list -> list.map { mapToTransactionGroup(it) } }

  fun getTransactionGroupsByType(groupType: TransactionGroupType): Flow<List<TransactionGroup>> = queries.getTransactionGroupsByType(groupType.name.lowercase())
    .asFlow()
    .mapToList(Dispatchers.IO)
    .map { list -> list.map { mapToTransactionGroup(it) } }

  fun getTransactionsByGroup(groupId: Long): Flow<List<Transaction>> = queries.getTransactionsByGroup(groupId)
    .asFlow()
    .mapToList(Dispatchers.IO)
    .map { list -> list.map { mapToTransaction(it) } }

  // Helper to get transactions using the shared repository
  suspend fun getTransactionsInGroup(groupId: Long): List<Transaction> = withContext(Dispatchers.IO) {
    queries.getTransactionsByGroup(groupId).executeAsList().map { mapToTransaction(it) }
  }

  private fun mapToTransactionGroup(entity: dev.tireless.abun.database.TransactionGroup): TransactionGroup = TransactionGroup(
    id = entity.id,
    name = entity.name,
    groupType = TransactionGroupType.fromString(entity.group_type),
    description = entity.description,
    createdAt = entity.created_at,
    updatedAt = entity.updated_at
  )

  private fun mapToTransaction(entity: dev.tireless.abun.database.Transaction): Transaction = Transaction(
    id = entity.id,
    amountStorage = entity.amount,
    debitAccountId = entity.debit_account_id,
    creditAccountId = entity.credit_account_id,
    transactionDate = entity.transaction_date,
    transferGroupId = entity.transfer_group_id,
    payee = entity.payee,
    member = entity.member,
    notes = entity.notes,
    state = TransactionState.fromString(entity.state),
    createdAt = entity.created_at,
    updatedAt = entity.updated_at
  )
}

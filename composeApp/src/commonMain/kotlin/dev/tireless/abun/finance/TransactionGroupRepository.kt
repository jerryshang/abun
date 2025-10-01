package dev.tireless.abun.finance

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.tireless.abun.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TransactionGroupRepository(private val database: AppDatabase) {
  private val queries = database.financeQueries

  private fun currentTimeMillis(): Long = 1704067200000L // 2024-01-01 00:00:00 UTC - Simplified for KMP

  suspend fun createTransactionGroup(
    name: String,
    groupType: TransactionGroupType,
    description: String? = null,
    totalAmount: Double? = null
  ): Long = withContext(Dispatchers.IO) {
    val now = currentTimeMillis()
    queries.insertTransactionGroup(
      name = name,
      group_type = groupType.name.lowercase(),
      description = description,
      total_amount = totalAmount,
      status = GroupStatus.ACTIVE.name.lowercase(),
      created_at = now,
      updated_at = now
    )
    queries.getAllTransactionGroups().executeAsList().lastOrNull()?.id ?: -1L
  }

  suspend fun updateTransactionGroup(
    id: Long,
    name: String,
    description: String? = null,
    status: GroupStatus
  ): Unit = withContext(Dispatchers.IO) {
    queries.updateTransactionGroup(
      name = name,
      description = description,
      status = status.name.lowercase(),
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

  private fun mapToTransactionGroup(entity: dev.tireless.abun.database.TransactionGroup): TransactionGroup = TransactionGroup(
    id = entity.id,
    name = entity.name,
    groupType = TransactionGroupType.fromString(entity.group_type),
    description = entity.description,
    totalAmount = entity.total_amount,
    status = GroupStatus.fromString(entity.status),
    createdAt = entity.created_at,
    updatedAt = entity.updated_at
  )

  private fun mapToTransaction(entity: dev.tireless.abun.database.FinanceTransaction): Transaction = Transaction(
    id = entity.id,
    amount = entity.amount,
    type = TransactionType.fromString(entity.type),
    transactionDate = entity.transaction_date,
    categoryId = entity.category_id,
    accountId = entity.account_id,
    toAccountId = entity.to_account_id,
    transferGroupId = entity.transfer_group_id,
    payee = entity.payee,
    member = entity.member,
    notes = entity.notes,
    state = TransactionState.fromString(entity.state),
    createdAt = entity.created_at,
    updatedAt = entity.updated_at
  )
}

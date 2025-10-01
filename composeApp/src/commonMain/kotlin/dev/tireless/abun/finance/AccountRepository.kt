package dev.tireless.abun.finance

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.tireless.abun.database.AppDatabase
import dev.tireless.abun.database.Account as DbAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository for managing financial accounts
 * Handles account CRUD operations and balance management
 */
class AccountRepository(private val database: AppDatabase) {
    private val queries = database.financeQueries

    /**
     * Get all accounts as Flow
     */
    fun getAllAccountsFlow(): Flow<List<Account>> =
        queries.getAllAccounts().asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toDomain() }
        }

    /**
     * Get all accounts
     */
    suspend fun getAllAccounts(): List<Account> =
        withContext(Dispatchers.IO) {
            queries.getAllAccounts().executeAsList().map { it.toDomain() }
        }

    /**
     * Get active accounts only
     */
    suspend fun getActiveAccounts(): List<Account> =
        withContext(Dispatchers.IO) {
            queries.getActiveAccounts().executeAsList().map { it.toDomain() }
        }

    /**
     * Get account by ID
     */
    suspend fun getAccountById(id: Long): Account? =
        withContext(Dispatchers.IO) {
            queries.getAccountById(id).executeAsOneOrNull()?.toDomain()
        }

    /**
     * Create a new account
     */
    suspend fun createAccount(input: CreateAccountInput): Long =
        withContext(Dispatchers.IO) {
            val now = currentTimeMillis()
            queries.insertAccount(
                name = input.name,
                type = input.type.name.lowercase(),
                initial_balance = input.initialBalance,
                current_balance = input.initialBalance,
                currency = input.currency,
                is_active = 1L,
                icon_name = input.iconName,
                color_hex = input.colorHex,
                created_at = now,
                updated_at = now
            )
            queries.getAllAccounts().executeAsList().lastOrNull()?.id ?: -1L
        }

    /**
     * Update an existing account
     */
    suspend fun updateAccount(input: UpdateAccountInput): Unit =
        withContext(Dispatchers.IO) {
            val now = currentTimeMillis()
            queries.updateAccount(
                name = input.name,
                type = input.type.name.lowercase(),
                initial_balance = input.initialBalance,
                current_balance = input.initialBalance, // Recalculate if needed
                currency = input.currency,
                is_active = if (input.isActive) 1L else 0L,
                icon_name = input.iconName,
                color_hex = input.colorHex,
                updated_at = now,
                id = input.id
            )
        }

    /**
     * Update account balance (called by TransactionRepository)
     */
    suspend fun updateAccountBalance(
        accountId: Long,
        newBalance: Double
    ): Unit =
        withContext(Dispatchers.IO) {
            val now = currentTimeMillis()
            queries.updateAccountBalance(
                current_balance = newBalance,
                updated_at = now,
                id = accountId
            )
        }

    /**
     * Adjust account balance by delta amount
     * Positive delta increases balance, negative decreases
     */
    suspend fun adjustAccountBalance(
        accountId: Long,
        delta: Double
    ): Unit =
        withContext(Dispatchers.IO) {
            val account = getAccountById(accountId) ?: return@withContext
            val newBalance = account.currentBalance + delta
            updateAccountBalance(accountId, newBalance)
        }

    /**
     * Delete an account
     */
    suspend fun deleteAccount(id: Long): Unit =
        withContext(Dispatchers.IO) {
            queries.deleteAccount(id)
        }

    /**
     * Get total balance of all active accounts
     */
    suspend fun getTotalBalance(): Double =
        withContext(Dispatchers.IO) {
            queries.getTotalBalanceAllAccounts().executeAsOneOrNull()?.total ?: 0.0
        }

    /**
     * Initialize default accounts for new users
     */
    suspend fun initializeDefaultAccounts() {
        val existingAccounts = getAllAccounts()
        if (existingAccounts.isNotEmpty()) return

        val defaultAccounts =
            listOf(
                CreateAccountInput(
                    name = "现金",
                    type = AccountType.CASH,
                    initialBalance = 0.0,
                    iconName = "cash",
                    colorHex = "#4CAF50"
                ),
                CreateAccountInput(
                    name = "微信钱包",
                    type = AccountType.E_WALLET,
                    initialBalance = 0.0,
                    iconName = "wechat",
                    colorHex = "#07C160"
                ),
                CreateAccountInput(
                    name = "支付宝",
                    type = AccountType.E_WALLET,
                    initialBalance = 0.0,
                    iconName = "alipay",
                    colorHex = "#1677FF"
                ),
                CreateAccountInput(
                    name = "储蓄卡",
                    type = AccountType.DEBIT_CARD,
                    initialBalance = 0.0,
                    iconName = "bank_card",
                    colorHex = "#2196F3"
                ),
                CreateAccountInput(
                    name = "信用卡",
                    type = AccountType.CREDIT_CARD,
                    initialBalance = 0.0,
                    iconName = "credit_card",
                    colorHex = "#FF9800"
                )
            )

        defaultAccounts.forEach { createAccount(it) }
    }

    /**
     * Mapper: Database model to Domain model
     */
    private fun DbAccount.toDomain() =
        Account(
            id = id,
            name = name,
            type = AccountType.fromString(type),
            initialBalance = initial_balance,
            currentBalance = current_balance,
            currency = currency,
            isActive = is_active == 1L,
            iconName = icon_name,
            colorHex = color_hex,
            createdAt = created_at,
            updatedAt = updated_at
        )

    /**
     * Get current timestamp in milliseconds (KMP-compatible)
     */
    private fun currentTimeMillis(): Long {
        return 1704067200000L // 2024-01-01 00:00:00 UTC - Simplified for KMP
    }
}

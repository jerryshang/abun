package dev.tireless.abun.finance

import dev.tireless.abun.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import dev.tireless.abun.database.Account as DbAccount

/**
 * Repository for managing financial accounts
 * Handles account CRUD operations and balance management
 * Accounts are organized in a hierarchy with 5 root accounts (Asset, Liability, Equity, Revenue, Expense)
 */
class AccountRepository(
  private val database: AppDatabase,
) {
  private val queries = database.financeQueries

  // Cache for account type lookups
  private var accountCache: Map<Long, Account> = emptyMap()
  private var accountTypeCache: Map<Long, AccountType> = emptyMap()

  /**
   * Refresh account cache
   */
  private suspend fun refreshCache() {
    val accounts = queries.getAllAccounts().executeAsList().map { it.toDomain() }
    accountCache = accounts.associateBy { it.id }

    // Build type cache by traversing hierarchy
    accountTypeCache =
      accounts.associate { account ->
        account.id to getAccountTypeFromHierarchy(account, accounts)
      }
  }

  /**
   * Get account type by traversing hierarchy to find root account
   */
  private fun getAccountTypeFromHierarchy(
    account: Account,
    allAccounts: List<Account>,
  ): AccountType {
    if (account.isRootAccount()) {
      return account.getRootAccountType() ?: AccountType.ASSET
    }

    // Traverse up to find root
    var current = account
    while (current.parentId != null) {
      val parent = allAccounts.find { it.id == current.parentId } ?: break
      current = parent
    }

    return current.getRootAccountType() ?: AccountType.ASSET
  }

  /**
   * Get account type for a given account ID (uses cache)
   */
  suspend fun getAccountType(accountId: Long): AccountType {
    if (accountTypeCache.isEmpty()) {
      refreshCache()
    }
    return accountTypeCache[accountId] ?: AccountType.ASSET
  }

  /**
   * Get all accounts
   */
  suspend fun getAllAccounts(): List<Account> =
    withContext(Dispatchers.IO) {
      val accounts = queries.getAllAccounts().executeAsList().map { it.toDomain() }
      refreshCache() // Update cache
      accounts
    }

  /**
   * Get all accounts with calculated balances
   */
  suspend fun getAllAccountsWithBalance(): List<AccountWithBalance> =
    withContext(Dispatchers.IO) {
      val accounts = getAllAccounts()
      val asOfMillis = Clock.System.now().toEpochMilliseconds()
      accounts.map { account ->
        AccountWithBalance(
          account = account,
          currentBalance = calculateAccountBalance(account.id, asOfMillis),
        )
      }
    }

  /**
   * Get active accounts only
   */
  suspend fun getActiveAccounts(): List<Account> =
    withContext(Dispatchers.IO) {
      queries.getActiveAccounts().executeAsList().map { it.toDomain() }
    }

  /**
   * Get active accounts with calculated balances
   */
  suspend fun getActiveAccountsWithBalance(): List<AccountWithBalance> =
    withContext(Dispatchers.IO) {
      val accounts = getActiveAccounts()
      val asOfMillis = Clock.System.now().toEpochMilliseconds()
      accounts.map { account ->
        AccountWithBalance(
          account = account,
          currentBalance = calculateAccountBalance(account.id, asOfMillis),
        )
      }
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
      val now = Clock.System.now().toEpochMilliseconds()

      // Build config from boolean flags
      var config = 0L
      config = AccountConfig.setActive(config, input.isActive)
      config = AccountConfig.setCountable(config, input.isCountable)
      config = AccountConfig.setVisible(config, input.isVisibleInUi)

      queries.insertAccount(
        name = input.name,
        parent_id = input.parentId,
        currency = input.currency,
        config = config,
        icon_name = input.iconName,
        color_hex = input.colorHex,
        bill_date = input.billDate?.toLong(),
        payment_date = input.paymentDate?.toLong(),
        credit_limit = input.getCreditLimitStorage(),
        created_at = now,
        updated_at = now,
      )
      refreshCache() // Update cache after insert
      queries
        .getAllAccounts()
        .executeAsList()
        .lastOrNull()
        ?.id ?: -1L
    }

  /**
   * Update an existing account
   */
  suspend fun updateAccount(input: UpdateAccountInput): Unit =
    withContext(Dispatchers.IO) {
      val now = Clock.System.now().toEpochMilliseconds()

      // Build config from boolean flags
      var config = 0L
      config = AccountConfig.setActive(config, input.isActive)
      config = AccountConfig.setCountable(config, input.isCountable)
      config = AccountConfig.setVisible(config, input.isVisibleInUi)

      queries.updateAccount(
        name = input.name,
        parent_id = input.parentId,
        currency = input.currency,
        config = config,
        icon_name = input.iconName,
        color_hex = input.colorHex,
        bill_date = input.billDate?.toLong(),
        payment_date = input.paymentDate?.toLong(),
        credit_limit = input.getCreditLimitStorage(),
        updated_at = now,
        id = input.id,
      )
      refreshCache() // Update cache after update
    }

  /**
   * Calculate account balance on demand from transactions
   * Uses debit/credit double-entry: balance = total debits - total credits
   * Returns balance in storage format (Long), converted to display format (Double)
   */
  suspend fun calculateAccountBalance(
    accountId: Long,
    asOfMillis: Long = Clock.System.now().toEpochMilliseconds(),
  ): Double =
    withContext(Dispatchers.IO) {
      val balanceStorage =
        queries
          .calculateAccountBalance(
            accountId = accountId,
            asOfDate = asOfMillis,
          ).executeAsOne()
      balanceStorage.toDisplayAmount()
    }

  /**
   * Delete an account
   */
  suspend fun deleteAccount(id: Long): Unit =
    withContext(Dispatchers.IO) {
      queries.deleteAccount(id)
    }

  /**
   * Get or create a liability account for a loan
   * Hidden from UI, used for double-entry booking
   */
  suspend fun getOrCreateLiabilityAccount(loanName: String): Account {
    // Try to find existing liability account
    val existingAccount =
      getAllAccounts().find {
        it.name == loanName && it.parentId == RootAccountIds.LIABILITY
      }

    if (existingAccount != null) {
      return existingAccount
    }

    // Create new liability account as child of Liability root (id=2)
    val accountId =
      createAccount(
        CreateAccountInput(
          name = loanName,
          parentId = RootAccountIds.LIABILITY,
          isVisibleInUi = false,
        ),
      )

    return getAccountById(accountId)!!
  }

  /**
   * Mapper: Database model to Domain model
   */
  private fun DbAccount.toDomain() =
    Account(
      id = id,
      name = name,
      parentId = parent_id,
      currency = currency,
      config = config,
      iconName = icon_name,
      colorHex = color_hex,
      billDate = bill_date?.toInt(),
      paymentDate = payment_date?.toInt(),
      creditLimitStorage = credit_limit,
      createdAt = created_at,
      updatedAt = updated_at,
    )
}

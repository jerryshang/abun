package dev.tireless.abun.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing financial transactions
 */
class TransactionViewModel(
  private val transactionRepository: TransactionRepository,
  private val accountRepository: AccountRepository,
  private val transactionGroupRepository: TransactionGroupRepository,
) : ViewModel() {
  private val _transactions = MutableStateFlow<List<TransactionWithDetails>>(emptyList())
  val transactions: StateFlow<List<TransactionWithDetails>> = _transactions.asStateFlow()

  private val _accounts = MutableStateFlow<List<AccountWithBalance>>(emptyList())
  val accounts: StateFlow<List<AccountWithBalance>> = _accounts.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _error = MutableStateFlow<String?>(null)
  val error: StateFlow<String?> = _error.asStateFlow()

  private val _selectedAccountId = MutableStateFlow<Long?>(null)
  val selectedAccountId: StateFlow<Long?> = _selectedAccountId.asStateFlow()

  init {
    loadData()
  }

  private fun loadData() {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        _transactions.value = transactionRepository.getAllTransactionsWithDetails()
        _accounts.value = accountRepository.getActiveAccountsWithBalance()
      } catch (e: Exception) {
        _error.value = "Failed to load data: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun refreshTransactions() {
    viewModelScope.launch {
      try {
        _transactions.value = transactionRepository.getAllTransactionsWithDetails()
      } catch (e: Exception) {
        _error.value = "Failed to refresh transactions: ${e.message}"
      }
    }
  }

  fun createTransaction(input: CreateTransactionInput) {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        transactionRepository.createTransaction(input)
        refreshTransactions()
        refreshAccounts()
      } catch (e: Exception) {
        _error.value = "Failed to create transaction: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun createSplitExpense(draft: SplitExpenseDraft) {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        transactionRepository.createSplitExpense(draft)
        refreshTransactions()
        refreshAccounts()
      } catch (e: Exception) {
        _error.value = "Failed to create split expense: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun updateTransaction(input: UpdateTransactionInput) {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        transactionRepository.updateTransaction(input)
        refreshTransactions()
        refreshAccounts()
      } catch (e: Exception) {
        _error.value = "Failed to update transaction: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun updateSplitExpense(draft: SplitExpenseDraft) {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        transactionRepository.updateSplitExpense(draft)
        refreshTransactions()
        refreshAccounts()
      } catch (e: Exception) {
        _error.value = "Failed to update split expense: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun deleteTransaction(
    id: Long,
    deleteGroupIds: List<Long> = emptyList(),
  ) {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        val groupIds = deleteGroupIds.distinct()
        val transactionIdsToDelete = mutableSetOf(id)

        for (groupId in groupIds) {
          val groupTransactions = transactionGroupRepository.getTransactionsInGroup(groupId)
          groupTransactions.forEach { transaction ->
            transactionIdsToDelete += transaction.id
          }
        }

        transactionIdsToDelete.forEach { transactionId ->
          transactionRepository.deleteTransaction(transactionId)
        }

        groupIds.forEach { groupId ->
          transactionGroupRepository.deleteTransactionGroup(groupId)
        }

        refreshTransactions()
        refreshAccounts()
      } catch (e: Exception) {
        _error.value = "Failed to delete transaction: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  suspend fun getTransactionDeletionContext(transactionId: Long): TransactionDeletionContext =
    try {
      val groups = transactionRepository.getGroupsForTransaction(transactionId)
      if (groups.isEmpty()) {
        TransactionDeletionContext.Empty
      } else {
        val groupDetails =
          groups.map { group ->
            val transactions = transactionGroupRepository.getTransactionsInGroup(group.id)
            val detailedTransactions = mutableListOf<TransactionWithDetails>()
            for (transaction in transactions) {
              val detailed = transactionRepository.getTransactionWithDetails(transaction.id)
              if (detailed != null) {
                detailedTransactions += detailed
              }
            }
            TransactionGroupWithTransactions(
              group = group,
              transactions = detailedTransactions,
            )
          }
        TransactionDeletionContext(groupDetails)
      }
    } catch (e: Exception) {
      _error.value = "Failed to load transaction details: ${e.message}"
      TransactionDeletionContext.Empty
    }

  /**
   * Create a loan with scheduled payments
   */
  fun createLoan(input: CreateLoanInput) {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        transactionRepository.createLoan(input, transactionGroupRepository)
        refreshTransactions()
        refreshAccounts()
      } catch (e: Exception) {
        _error.value = "Failed to create loan: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  suspend fun getTransactionsByAccountWithDetails(accountId: Long): List<TransactionWithDetails> =
    try {
      transactionRepository.getTransactionsByAccount(accountId).mapNotNull { transaction ->
        transactionRepository.getTransactionWithDetails(transaction.id)
      }
    } catch (e: Exception) {
      _error.value = "Failed to load transactions: ${e.message}"
      emptyList()
    }

  suspend fun getTransactionsByDateRangeWithDetails(
    startDate: Long,
    endDate: Long,
  ): List<TransactionWithDetails> =
    try {
      transactionRepository.getTransactionsByDateRange(startDate, endDate).mapNotNull { transaction ->
        transactionRepository.getTransactionWithDetails(transaction.id)
      }
    } catch (e: Exception) {
      _error.value = "Failed to load transactions: ${e.message}"
      emptyList()
    }

  suspend fun getSplitExpenseDraft(transactionId: Long): SplitExpenseDraft? =
    try {
      val transaction = transactionRepository.getTransactionById(transactionId) ?: return null
      val groups = transactionRepository.getGroupsForTransaction(transactionId)
      val splitGroup = groups.firstOrNull { it.groupType == TransactionGroupType.SPLIT }

      val (entries, paymentAccountId) =
        if (splitGroup != null) {
          val groupedTransactions = transactionGroupRepository.getTransactionsInGroup(splitGroup.id)
          val paymentId =
            groupedTransactions.firstOrNull()?.creditAccountId ?: transaction.creditAccountId
          val mappedEntries =
            groupedTransactions.map { groupedTransaction ->
              SplitExpenseEntry(
                transactionId = groupedTransaction.id,
                categoryId = groupedTransaction.debitAccountId,
                amount = groupedTransaction.amount,
                notes = groupedTransaction.notes,
              )
            }
          mappedEntries to paymentId
        } else {
          listOf(
            SplitExpenseEntry(
              transactionId = transaction.id,
              categoryId = transaction.debitAccountId,
              amount = transaction.amount,
              notes = transaction.notes,
            ),
          ) to transaction.creditAccountId
        }

      val groupNote =
        splitGroup?.let { group ->
          transactionGroupRepository.getTransactionGroupById(group.id)?.description
        }

      SplitExpenseDraft(
        groupId = splitGroup?.id,
        transactionDate = transaction.transactionDate,
        totalAmount = entries.sumOf { it.amount },
        paymentAccountId = paymentAccountId,
        payee = transaction.payee,
        member = transaction.member,
        entries = entries,
        groupNote = groupNote,
      )
    } catch (e: Exception) {
      _error.value = "Failed to load split expense: ${e.message}"
      null
    }

  private fun refreshAccounts() {
    viewModelScope.launch {
      try {
        _accounts.value = accountRepository.getActiveAccountsWithBalance()
      } catch (e: Exception) {
        _error.value = "Failed to refresh accounts: ${e.message}"
      }
    }
  }

  fun clearError() {
    _error.value = null
  }

  fun setSelectedAccount(accountId: Long?) {
    _selectedAccountId.value = accountId
  }

  suspend fun getFilteredTransactions(): List<TransactionWithDetails> {
    val selectedId = _selectedAccountId.value ?: return _transactions.value
    return _transactions.value.filter {
      it.transaction.debitAccountId == selectedId || it.transaction.creditAccountId == selectedId
    }
  }
}

/**
 * ViewModel for managing accounts
 */
class AccountViewModel(
  private val accountRepository: AccountRepository,
) : ViewModel() {
  private val _accounts = MutableStateFlow<List<AccountWithBalance>>(emptyList())
  val accounts: StateFlow<List<AccountWithBalance>> = _accounts.asStateFlow()

  private val _totalBalance = MutableStateFlow(0.0)
  val totalBalance: StateFlow<Double> = _totalBalance.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _error = MutableStateFlow<String?>(null)
  val error: StateFlow<String?> = _error.asStateFlow()

  init {
    loadAccounts()
  }

  fun loadAccounts() {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        _accounts.value = accountRepository.getAllAccountsWithBalance()
        _totalBalance.value = _accounts.value.totalCountableBalance()
      } catch (e: Exception) {
        _error.value = "Failed to load accounts: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  /**
   * Get account type for a given account ID
   */
  suspend fun getAccountType(accountId: Long): AccountType = accountRepository.getAccountType(accountId)

  fun createAccount(input: CreateAccountInput) {
    viewModelScope.launch {
      try {
        _isLoading.value = true

        // If no parent is specified, use Asset root (id=1) as default for user accounts
        val finalInput =
          if (input.parentId == null) {
            input.copy(parentId = RootAccountIds.ASSET)
          } else {
            input
          }

        accountRepository.createAccount(finalInput)
        loadAccounts()
      } catch (e: Exception) {
        _error.value = "Failed to create account: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun updateAccount(input: UpdateAccountInput) {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        accountRepository.updateAccount(input)
        loadAccounts()
      } catch (e: Exception) {
        _error.value = "Failed to update account: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun deleteAccount(id: Long) {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        accountRepository.deleteAccount(id)
        loadAccounts()
      } catch (e: Exception) {
        _error.value = "Failed to delete account: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun clearError() {
    _error.value = null
  }
}

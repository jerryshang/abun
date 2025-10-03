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
  private val transactionGroupRepository: TransactionGroupRepository
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

  fun deleteTransaction(id: Long) {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        transactionRepository.deleteTransaction(id)
        refreshTransactions()
        refreshAccounts()
      } catch (e: Exception) {
        _error.value = "Failed to delete transaction: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
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

  suspend fun getTransactionsByAccountWithDetails(accountId: Long): List<TransactionWithDetails> = try {
    transactionRepository.getTransactionsByAccount(accountId).mapNotNull { transaction ->
      transactionRepository.getTransactionWithDetails(transaction.id)
    }
  } catch (e: Exception) {
    _error.value = "Failed to load transactions: ${e.message}"
    emptyList()
  }

  suspend fun getTransactionsByDateRangeWithDetails(startDate: Long, endDate: Long): List<TransactionWithDetails> = try {
    transactionRepository.getTransactionsByDateRange(startDate, endDate).mapNotNull { transaction ->
      transactionRepository.getTransactionWithDetails(transaction.id)
    }
  } catch (e: Exception) {
    _error.value = "Failed to load transactions: ${e.message}"
    emptyList()
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
  private val accountRepository: AccountRepository
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
        _totalBalance.value = _accounts.value.filter { it.isActive && it.isCountable }.sumOf { it.currentBalance }
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
        val finalInput = if (input.parentId == null) {
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

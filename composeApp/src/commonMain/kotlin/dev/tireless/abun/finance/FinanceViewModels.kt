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
  private val categoryRepository: FinanceCategoryRepository,
  private val tagRepository: FinanceTagRepository,
  private val transactionGroupRepository: TransactionGroupRepository
) : ViewModel() {

  private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
  val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

  private val _accounts = MutableStateFlow<List<Account>>(emptyList())
  val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

  private val _categories = MutableStateFlow<List<FinanceCategory>>(emptyList())
  val categories: StateFlow<List<FinanceCategory>> = _categories.asStateFlow()

  private val _tags = MutableStateFlow<List<FinanceTag>>(emptyList())
  val tags: StateFlow<List<FinanceTag>> = _tags.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _error = MutableStateFlow<String?>(null)
  val error: StateFlow<String?> = _error.asStateFlow()

  init {
    loadData()
  }

  private fun loadData() {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        _transactions.value = transactionRepository.getAllTransactions()
        _accounts.value = accountRepository.getActiveAccounts()
        _categories.value = categoryRepository.getAllCategories()
        _tags.value = tagRepository.getAllTags()
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
        _transactions.value = transactionRepository.getAllTransactions()
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

  fun getTransactionsByAccount(accountId: Long) {
    viewModelScope.launch {
      try {
        _transactions.value = transactionRepository.getTransactionsByAccount(accountId)
      } catch (e: Exception) {
        _error.value = "Failed to load transactions: ${e.message}"
      }
    }
  }

  fun getTransactionsByDateRange(startDate: Long, endDate: Long) {
    viewModelScope.launch {
      try {
        _transactions.value = transactionRepository.getTransactionsByDateRange(startDate, endDate)
      } catch (e: Exception) {
        _error.value = "Failed to load transactions: ${e.message}"
      }
    }
  }

  private fun refreshAccounts() {
    viewModelScope.launch {
      try {
        _accounts.value = accountRepository.getActiveAccounts()
      } catch (e: Exception) {
        _error.value = "Failed to refresh accounts: ${e.message}"
      }
    }
  }

  fun clearError() {
    _error.value = null
  }
}

/**
 * ViewModel for managing accounts
 */
class AccountViewModel(
  private val accountRepository: AccountRepository
) : ViewModel() {

  private val _accounts = MutableStateFlow<List<Account>>(emptyList())
  val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

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
        _accounts.value = accountRepository.getAllAccounts()
        _totalBalance.value = accountRepository.getTotalBalance()
      } catch (e: Exception) {
        _error.value = "Failed to load accounts: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun createAccount(input: CreateAccountInput) {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        accountRepository.createAccount(input)
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

/**
 * ViewModel for managing finance categories
 */
class FinanceCategoryViewModel(
  private val categoryRepository: FinanceCategoryRepository
) : ViewModel() {

  private val _categories = MutableStateFlow<List<CategoryWithSubcategories>>(emptyList())
  val categories: StateFlow<List<CategoryWithSubcategories>> = _categories.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _error = MutableStateFlow<String?>(null)
  val error: StateFlow<String?> = _error.asStateFlow()

  init {
    loadCategories()
  }

  fun loadCategories() {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        _categories.value = categoryRepository.getCategoriesWithSubcategories()
      } catch (e: Exception) {
        _error.value = "Failed to load categories: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun createCategory(input: CreateCategoryInput) {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        categoryRepository.createCategory(input)
        loadCategories()
      } catch (e: Exception) {
        _error.value = "Failed to create category: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun updateCategory(
    id: Long,
    name: String,
    parentId: Long?,
    type: CategoryType,
    iconName: String?,
    colorHex: String?
  ) {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        categoryRepository.updateCategory(id, name, parentId, type, iconName, colorHex)
        loadCategories()
      } catch (e: Exception) {
        _error.value = "Failed to update category: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun deleteCategory(id: Long) {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        categoryRepository.deleteCategory(id)
        loadCategories()
      } catch (e: Exception) {
        _error.value = "Failed to delete category: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun clearError() {
    _error.value = null
  }
}

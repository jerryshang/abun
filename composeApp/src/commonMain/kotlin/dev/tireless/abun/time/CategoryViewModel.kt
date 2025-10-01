package dev.tireless.abun.time

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tireless.abun.time.Category
import dev.tireless.abun.time.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                categoryRepository.getAllCategories().collect { categoryList ->
                    _categories.value = categoryList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load categories: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun createCategory(
        name: String,
        color: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val categoryId = categoryRepository.insertCategory(name, color)
                if (categoryId != null) {
                    onSuccess()
                } else {
                    onError("Failed to create category")
                }
            } catch (e: Exception) {
                onError("Failed to create category: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCategory(
        id: Long,
        name: String,
        color: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                categoryRepository.updateCategory(id, name, color)
                onSuccess()
            } catch (e: Exception) {
                onError("Failed to update category: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCategory(
        id: Long,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                categoryRepository.deleteCategory(id)
                onSuccess()
            } catch (e: Exception) {
                onError("Failed to delete category: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
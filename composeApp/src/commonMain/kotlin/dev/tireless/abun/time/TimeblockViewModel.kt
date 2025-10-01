package dev.tireless.abun.time

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tireless.abun.time.Category
import dev.tireless.abun.time.Task
import dev.tireless.abun.time.Timeblock
import dev.tireless.abun.time.CategoryRepository
import dev.tireless.abun.time.TaskRepository
import dev.tireless.abun.time.TimeblockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimeblockViewModel(
    private val timeblockRepository: TimeblockRepository,
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _timeblocks = MutableStateFlow<List<Timeblock>>(emptyList())
    val timeblocks: StateFlow<List<Timeblock>> = _timeblocks.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadCategories()
        loadTasks()
    }

    fun loadTimeblocks(date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                timeblockRepository.getTimeblocksByDateRange(date, date).collect { timeblockList ->
                    _timeblocks.value = timeblockList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load timeblocks: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadTasks() {
        viewModelScope.launch {
            try {
                taskRepository.getAllTasks().collect { taskList ->
                    _tasks.value = taskList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load tasks: ${e.message}"
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                categoryRepository.getAllCategories().collect { categoryList ->
                    _categories.value = categoryList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load categories: ${e.message}"
            }
        }
    }

    fun createTimeblock(
        startTime: String,
        endTime: String,
        taskId: Long,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val timeblockId = timeblockRepository.insertTimeblock(startTime, endTime, taskId)
                if (timeblockId != null) {
                    onSuccess()
                    // Reload timeblocks for the current date
                    loadTimeblocks(startTime.substring(0, 10))
                } else {
                    onError("Failed to create timeblock")
                }
            } catch (e: Exception) {
                onError("Failed to create timeblock: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createTask(
        name: String,
        description: String?,
        categoryId: Long,
        onSuccess: (Long) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val taskId = taskRepository.insertTask(name, description, categoryId)
                if (taskId != null) {
                    onSuccess(taskId)
                } else {
                    onError("Failed to create task")
                }
            } catch (e: Exception) {
                onError("Failed to create task: ${e.message}")
            }
        }
    }

    fun deleteTimeblock(id: Long) {
        viewModelScope.launch {
            try {
                timeblockRepository.deleteTimeblock(id)
                // Refresh timeblocks
                val currentTimeblocks = _timeblocks.value
                if (currentTimeblocks.isNotEmpty()) {
                    val sampleDate = currentTimeblocks.first().startTime.substring(0, 10)
                    loadTimeblocks(sampleDate)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete timeblock: ${e.message}"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
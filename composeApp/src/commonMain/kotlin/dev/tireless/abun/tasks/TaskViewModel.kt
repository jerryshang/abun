package dev.tireless.abun.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tireless.abun.tags.Tag
import dev.tireless.abun.core.time.currentInstant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class TaskBoardViewModel(
  private val repository: TaskPlannerRepository,
) : ViewModel() {
  private val _selectedDate =
    MutableStateFlow(
      currentInstant()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date,
    )
  val selectedDate: StateFlow<LocalDate> = _selectedDate

  val allTasks: StateFlow<List<Task>> =
    repository
      .observeAllTasks()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  val inboxTasks: StateFlow<List<TaskNode>> =
    repository
      .observeInbox()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  val todayTasks: StateFlow<List<TaskNode>> =
    _selectedDate
      .flatMapLatest { date -> repository.observeToday(date) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  val futureTasks: StateFlow<List<TaskNode>> =
    _selectedDate
      .flatMapLatest { date -> repository.observeFuture(date) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  val archivedTasks: StateFlow<List<TaskNode>> =
    repository
      .observeArchived()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  val tags: StateFlow<List<Tag>> =
    repository
      .availableTagsForTasks()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  fun selectDate(date: LocalDate) {
    _selectedDate.value = date
  }

  fun createTask(draft: TaskDraft): Task = repository.createTask(draft)

  fun updateTask(update: TaskUpdate): Task? = repository.updateTask(update)

  fun changeTaskState(
    taskId: Long,
    newState: TaskState,
    logInput: TaskLogInput,
  ) {
    repository.updateTaskState(TaskStateChange(taskId, newState, logInput))
  }

  fun deleteTask(taskId: Long) {
    repository.deleteTask(taskId)
  }

  fun defaultStartSlot(): LocalTime {
    val now =
      currentInstant()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .time
    val minutes = (now.minute / 15) * 15
    return LocalTime(hour = now.hour, minute = minutes)
  }
}

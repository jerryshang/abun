package dev.tireless.abun.time

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dev.tireless.abun.core.time.currentInstant
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.milliseconds

private const val FOCUS_DURATION_MILLIS = 25 * 60 * 1000L
private const val SHORT_BREAK_DURATION_MILLIS = 5 * 60 * 1000L
private const val LONG_BREAK_DURATION_MILLIS = 15 * 60 * 1000L
private const val SESSIONS_PER_LONG_BREAK = 4
private const val TICK_MILLIS = 1000L

enum class FocusStage {
  Idle,
  Focus,
  ShortBreak,
  LongBreak,
}

data class FocusTimerUiState(
  val selectedTaskId: Long? = null,
  val stage: FocusStage = FocusStage.Idle,
  val isRunning: Boolean = false,
  val remainingMillis: Long = FOCUS_DURATION_MILLIS,
  val phaseDurationMillis: Long = FOCUS_DURATION_MILLIS,
  val completedFocusSessions: Int = 0,
  val totalFocusMillis: Long = 0L,
  val isStopDialogVisible: Boolean = false,
  val isCompletingSession: Boolean = false,
  val errorMessage: String? = null,
)

private data class FocusSegment(
  val taskId: Long?,
  var durationMillis: Long,
)

class FocusTimerViewModel(
  private val timeblockRepository: TimeblockRepository,
  private val taskRepository: TaskRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(FocusTimerUiState())
  val uiState: StateFlow<FocusTimerUiState> = _uiState

  private var timerJob: Job? = null
  private val segments = mutableListOf<FocusSegment>()
  private var currentSegment: FocusSegment? = null

  fun selectTask(taskId: Long?) {
    val state = _uiState.value
    if (state.isRunning) {
      _uiState.update {
        it.copy(errorMessage = "Pause the timer before switching tasks")
      }
      return
    }

    if (state.stage == FocusStage.Focus && currentSegment != null && currentSegment?.taskId != taskId) {
      commitCurrentSegment()
    }

    _uiState.update { it.copy(selectedTaskId = taskId) }
  }

  fun startTimer() {
    val state = _uiState.value
    if (state.stage != FocusStage.Idle || state.isRunning) {
      return
    }
    segments.clear()
    currentSegment = null
    _uiState.update {
      it.copy(
        stage = FocusStage.Focus,
        isRunning = true,
        remainingMillis = FOCUS_DURATION_MILLIS,
        phaseDurationMillis = FOCUS_DURATION_MILLIS,
        completedFocusSessions = 0,
        totalFocusMillis = 0,
      )
    }
    ensureSegmentStarted()
    startTicker()
  }

  fun pauseTimer() {
    timerJob?.cancel()
    timerJob = null
    _uiState.update { it.copy(isRunning = false) }
  }

  fun resumeTimer() {
    val state = _uiState.value
    if (state.stage == FocusStage.Idle || state.isRunning) {
      return
    }
    _uiState.update { it.copy(isRunning = true) }
    ensureSegmentStarted()
    startTicker()
  }

  fun showStopDialog() {
    pauseTimer()
    _uiState.update { it.copy(isStopDialogVisible = true) }
  }

  fun dismissStopDialog() {
    _uiState.update { it.copy(isStopDialogVisible = false) }
  }

  fun confirmStop(selectedStrategy: String?, task: Task?) {
    if (_uiState.value.isCompletingSession) return

    pauseTimer()
    commitCurrentSegment()

    val segmentsSnapshot = segments.toList()
    val stopInstant = currentInstant()
    updateTotalFocusFromSegments()

    _uiState.update { it.copy(isCompletingSession = true) }

    viewModelScope.launch {
      try {
        logSegments(segmentsSnapshot, stopInstant)
        if (task != null && selectedStrategy != null && task.strategy != selectedStrategy) {
          try {
            taskRepository.updateTask(task.id, task.name, task.description, task.parentTaskId, selectedStrategy)
          } catch (error: Exception) {
            // Persist the error but continue resetting the timer
            _uiState.update {
              it.copy(errorMessage = "Failed to update task status: ${error.message}")
            }
          }
        }
      } catch (error: Exception) {
        _uiState.update {
          it.copy(errorMessage = "Failed to log focus session: ${error.message}")
        }
      } finally {
        resetTimer()
      }
    }
  }

  fun clearError() {
    _uiState.update { it.copy(errorMessage = null) }
  }

  override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
  }

  private fun startTicker() {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (true) {
        delay(TICK_MILLIS)
        val state = _uiState.value
        if (!state.isRunning) {
          break
        }

        val remaining = (state.remainingMillis - TICK_MILLIS).coerceAtLeast(0L)
        if (state.stage == FocusStage.Focus) {
          currentSegment?.durationMillis = (currentSegment?.durationMillis ?: 0L) + TICK_MILLIS
        }

        val totalFocusMillis = segments.sumOf { it.durationMillis } + (currentSegment?.durationMillis ?: 0L)

        _uiState.update {
          it.copy(
            remainingMillis = remaining,
            totalFocusMillis = totalFocusMillis,
          )
        }

        if (remaining == 0L) {
          handlePhaseCompletion()
          break
        }
      }
    }
  }

  private fun handlePhaseCompletion() {
    timerJob?.cancel()
    timerJob = null

    val state = _uiState.value
    when (state.stage) {
      FocusStage.Focus -> {
        commitCurrentSegment()
        val completed = state.completedFocusSessions + 1
        val nextStage = if (completed % SESSIONS_PER_LONG_BREAK == 0) FocusStage.LongBreak else FocusStage.ShortBreak
        val nextDuration = durationForStage(nextStage)
        val totalFocusMillis = segments.sumOf { it.durationMillis }
        _uiState.update {
          it.copy(
            stage = nextStage,
            isRunning = true,
            remainingMillis = nextDuration,
            phaseDurationMillis = nextDuration,
            completedFocusSessions = completed,
            totalFocusMillis = totalFocusMillis,
          )
        }
        startTicker()
      }
      FocusStage.ShortBreak, FocusStage.LongBreak -> {
        _uiState.update {
          it.copy(
            stage = FocusStage.Focus,
            isRunning = true,
            remainingMillis = FOCUS_DURATION_MILLIS,
            phaseDurationMillis = FOCUS_DURATION_MILLIS,
          )
        }
        ensureSegmentStarted()
        startTicker()
      }
      FocusStage.Idle -> Unit
    }
  }

  private fun ensureSegmentStarted() {
    val state = _uiState.value
    if (state.stage != FocusStage.Focus || !state.isRunning) {
      return
    }
    val currentTask = state.selectedTaskId
    if (currentSegment == null || currentSegment?.taskId != currentTask) {
      commitCurrentSegment()
      if (currentTask != null) {
        currentSegment = FocusSegment(taskId = currentTask, durationMillis = 0)
      } else {
        currentSegment = null
      }
    }
  }

  private fun commitCurrentSegment() {
    val segment = currentSegment ?: return
    if (segment.durationMillis > 0) {
      segments += segment.copy()
    }
    currentSegment = null
    updateTotalFocusFromSegments()
  }

  private suspend fun logSegments(segmentList: List<FocusSegment>, stopInstant: Instant) {
    val totalDuration = segmentList.sumOf { it.durationMillis }
    if (totalDuration <= 0) return

    var cursor = stopInstant - totalDuration.milliseconds
    val zone = TimeZone.currentSystemDefault()

    for (segment in segmentList) {
      val taskId = segment.taskId ?: continue
      if (segment.durationMillis <= 0) continue

      val startInstant = cursor
      val endInstant = cursor + segment.durationMillis.milliseconds
      val startTime = startInstant.toLocalDateTime(zone).toString()
      val endTime = endInstant.toLocalDateTime(zone).toString()
      timeblockRepository.insertTimeblock(startTime, endTime, taskId)
      cursor = endInstant
    }
  }

  private fun resetTimer() {
    segments.clear()
    currentSegment = null
    _uiState.update {
      it.copy(
        stage = FocusStage.Idle,
        isRunning = false,
        remainingMillis = FOCUS_DURATION_MILLIS,
        phaseDurationMillis = FOCUS_DURATION_MILLIS,
        totalFocusMillis = 0,
        isStopDialogVisible = false,
        isCompletingSession = false,
      )
    }
  }

  private fun durationForStage(stage: FocusStage): Long =
    when (stage) {
      FocusStage.Focus -> FOCUS_DURATION_MILLIS
      FocusStage.ShortBreak -> SHORT_BREAK_DURATION_MILLIS
      FocusStage.LongBreak -> LONG_BREAK_DURATION_MILLIS
      FocusStage.Idle -> FOCUS_DURATION_MILLIS
    }

  private fun updateTotalFocusFromSegments() {
    val total = segments.sumOf { it.durationMillis } + (currentSegment?.durationMillis ?: 0L)
    _uiState.update { it.copy(totalFocusMillis = total) }
  }
}

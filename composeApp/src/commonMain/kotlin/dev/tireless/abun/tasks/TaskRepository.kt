package dev.tireless.abun.tasks

import dev.tireless.abun.tags.TagDomain
import dev.tireless.abun.tags.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import dev.tireless.abun.core.time.currentInstant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.max

class TaskPlannerRepository(
  private val tagRepository: TagRepository,
) {
  private val tasks = MutableStateFlow(sampleTasks())
  private val logs = MutableStateFlow(sampleLogs())

  private var taskIdCounter: Long = tasks.value.maxOfOrNull { it.id }?.plus(1) ?: 1L
  private var logIdCounter: Long = 1L

  fun observeAllTasks(): StateFlow<List<Task>> = tasks

  fun observeToday(date: LocalDate): Flow<List<TaskNode>> {
    return tasks.map { list ->
      list.filter { it.plannedDate == date }
        .toHierarchy()
    }
  }

  fun observeUpcoming(after: LocalDate): Flow<List<TaskNode>> {
    return tasks.map { list ->
      list.filter { it.plannedDate > after }
        .sortedBy { it.plannedDate }
        .toHierarchy()
    }
  }

  fun observeTask(taskId: Long): Flow<Task?> = tasks.map { list -> list.find { it.id == taskId } }

  fun observeLogs(taskId: Long): Flow<List<TaskLog>> = logs.map { list -> list.filter { it.taskId == taskId } }

  fun createTask(draft: TaskDraft): Task {
    val now = currentInstant()
    val task =
      Task(
        id = nextTaskId(),
        title = draft.title.trim(),
        description = draft.description?.takeIf { it.isNotBlank() },
        estimateMinutes = max(5, draft.estimateMinutes),
        plannedDate = draft.plannedDate,
        plannedStart = draft.plannedStart,
        notBefore = draft.notBefore,
        state = TaskState.Ready,
        parentId = draft.parentId,
        tagIds = draft.tagIds,
        actualMinutes = null,
        createdAt = now,
        updatedAt = now,
      )
    tasks.value = tasks.value + task
    return task
  }

  fun updateTask(update: TaskUpdate) {
    val now = currentInstant()
    tasks.value =
      tasks.value.map { existing ->
        if (existing.id == update.id) {
          existing.copy(
            title = update.title.trim(),
            description = update.description?.takeIf { it.isNotBlank() },
            estimateMinutes = max(5, update.estimateMinutes),
            plannedDate = update.plannedDate,
            plannedStart = update.plannedStart,
            notBefore = update.notBefore,
            parentId = update.parentId,
            tagIds = update.tagIds,
            state = update.state,
            updatedAt = now,
          )
        } else {
          existing
        }
      }
  }

  fun updateTaskState(input: TaskStateChange) {
    val log = appendLog(input.taskId, input.newState, input.log)
    tasks.value =
      tasks.value.map { existing ->
        if (existing.id == input.taskId) {
          existing.copy(
            state = input.newState,
            actualMinutes = log.actualMinutes,
            updatedAt = currentInstant(),
          )
        } else {
          existing
        }
      }
  }

  fun deleteTask(taskId: Long) {
    val descendants = collectDescendantIds(taskId)
    tasks.value = tasks.value.filterNot { it.id == taskId || it.id in descendants }
    logs.value = logs.value.filterNot { it.taskId == taskId || it.taskId in descendants }
  }

  fun availableTagsForTasks() = tagRepository.observeByDomain(TagDomain.Tasks)

  private fun appendLog(taskId: Long, state: TaskState, input: TaskLogInput): TaskLog {
    val log =
      TaskLog(
        id = nextLogId(),
        taskId = taskId,
        stateAfter = state,
        startedAt = input.startedAt,
        endedAt = input.endedAt,
        actualMinutes = input.actualMinutes,
        note = input.note?.takeIf { it.isNotBlank() },
      )
    logs.value = logs.value + log
    return log
  }

  private fun collectDescendantIds(taskId: Long): Set<Long> {
    val map = tasks.value.groupBy { it.parentId }
    val result = mutableSetOf<Long>()
    fun traverse(id: Long) {
      val children = map[id] ?: return
      for (child in children) {
        result += child.id
        traverse(child.id)
      }
    }
    traverse(taskId)
    return result
  }

  private fun List<Task>.toHierarchy(): List<TaskNode> {
    val map = groupBy { it.parentId }
    fun build(parentId: Long?, depth: Int): List<TaskNode> {
      val children = map[parentId].orEmpty().sortedWith(taskSorter)
      return children.map { task ->
        val childNodes = build(task.id, depth + 1)
        val totalEstimate = childNodes.sumOf { it.totalEstimate } + task.estimateMinutes
        val totalActual =
          if (childNodes.any { it.totalActual == null }) {
            null
          } else {
            val childrenActual = childNodes.sumOf { it.totalActual ?: 0 }
            (task.actualMinutes ?: 0) + childrenActual
          }
        TaskNode(
          task = task,
          depth = depth,
          children = childNodes,
          totalEstimate = totalEstimate,
          totalActual = totalActual,
        )
      }
    }

    return build(null, 0)
  }

  private fun nextTaskId(): Long = taskIdCounter++

  private fun nextLogId(): Long = logIdCounter++

  private val taskSorter =
    compareBy<Task>({ it.plannedDate }, { it.plannedStart.hour }, { it.plannedStart.minute }, { it.id })

  private fun sampleTasks(): List<Task> {
    val nowInstant = currentInstant()
    val today = nowInstant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val tomorrow = today.plus(1, DateTimeUnit.DAY)
    val dayAfter = today.plus(2, DateTimeUnit.DAY)
    val now = nowInstant
    return listOf(
      Task(
        id = 1,
        title = "Outline Q3 OKRs",
        description = "Draft objectives for the next quarter and align with stakeholders.",
        estimateMinutes = 120,
        plannedDate = today,
        plannedStart = LocalTime(hour = 9, minute = 0),
        notBefore = today,
        state = TaskState.Ready,
        parentId = null,
        tagIds = setOf(1L),
        actualMinutes = null,
        createdAt = now,
        updatedAt = now,
      ),
      Task(
        id = 2,
        title = "Review budgeting spreadsheet",
        description = "Double-check latest spending categories and update cashflow forecast.",
        estimateMinutes = 45,
        plannedDate = today,
        plannedStart = LocalTime(hour = 11, minute = 30),
        notBefore = today,
        state = TaskState.Ready,
        parentId = null,
        tagIds = setOf(2L),
        actualMinutes = null,
        createdAt = now,
        updatedAt = now,
      ),
      Task(
        id = 3,
        title = "Prepare meeting agenda",
        description = "Summarize progress and blockers for the strategy sync.",
        estimateMinutes = 30,
        plannedDate = today,
        plannedStart = LocalTime(hour = 10, minute = 30),
        notBefore = today,
        state = TaskState.InProgress,
        parentId = 1,
        tagIds = setOf(1L),
        actualMinutes = 20,
        createdAt = now,
        updatedAt = now,
      ),
      Task(
        id = 4,
        title = "Draft project charter",
        description = "First pass on the discovery project charter.",
        estimateMinutes = 90,
        plannedDate = tomorrow,
        plannedStart = LocalTime(hour = 13, minute = 30),
        notBefore = today,
        state = TaskState.Backlog,
        parentId = null,
        tagIds = setOf(1L),
        actualMinutes = null,
        createdAt = now,
        updatedAt = now,
      ),
      Task(
        id = 5,
        title = "Read 'Deep Work' chapter 3",
        description = null,
        estimateMinutes = 40,
        plannedDate = dayAfter,
        plannedStart = LocalTime(hour = 7, minute = 30),
        notBefore = today,
        state = TaskState.Ready,
        parentId = null,
        tagIds = setOf(3L),
        actualMinutes = null,
        createdAt = now,
        updatedAt = now,
      ),
    )
  }

  private fun sampleLogs(): List<TaskLog> {
    val nowInstant = currentInstant()
    val today = nowInstant.toLocalDateTime(TimeZone.currentSystemDefault())
    return listOf(
      TaskLog(
        id = nextLogId(),
        taskId = 3,
        stateAfter = TaskState.InProgress,
        startedAt = LocalDateTime(today.date, LocalTime(hour = 10, minute = 0)),
        endedAt = LocalDateTime(today.date, LocalTime(hour = 10, minute = 20)),
        actualMinutes = 20,
        note = "Kick-off session",
      ),
    )
  }
}

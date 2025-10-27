package dev.tireless.abun.tasks

import dev.tireless.abun.core.time.currentInstant
import dev.tireless.abun.tags.TagDomain
import dev.tireless.abun.tags.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import kotlin.math.max

class TaskPlannerRepository(
  private val tagRepository: TagRepository,
) {
  private companion object {
    private const val MINUTES_PER_DAY = 24 * 60
  }

  private val tasks = MutableStateFlow<List<Task>>(emptyList())
  private val logs = MutableStateFlow<List<TaskLog>>(emptyList())

  private var taskIdCounter: Long = 1L
  private var logIdCounter: Long = 1L

  fun observeAllTasks(): StateFlow<List<Task>> = tasks

  fun observeInbox(): Flow<List<TaskNode>> =
    tasks.map { list ->
      list
        .filter { task -> !task.isArchived() && task.plannedDate == null }
        .toHierarchy(alphabeticalSorter)
    }

  fun observeToday(reference: LocalDate): Flow<List<TaskNode>> =
    tasks.map { list ->
      val active = list.filterNot { it.isArchived() }
      val todays = active.filter { it.shouldAppearToday(reference) }
      val overdue = active
        .filter { it.isOverdueOn(reference) }
      val todayNodes = todays.toHierarchy(alphabeticalSorter)
      val overdueNodes = overdue.toHierarchy(overdueSorter)
      todayNodes + overdueNodes
    }

  fun observeFuture(reference: LocalDate): Flow<List<TaskNode>> =
    tasks.map { list ->
      list
        .filter { task ->
          !task.isArchived() && task.isFutureRelativeTo(reference)
        }
        .toHierarchy(futureSorter)
    }

  fun observeArchived(): Flow<List<TaskNode>> =
    tasks.map { list ->
      list
        .filter { task -> task.isArchived() }
        .toHierarchy(archivedSorter)
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
        constraint = draft.constraint,
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
            constraint = update.constraint,
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

  private fun Task.shouldAppearToday(reference: LocalDate): Boolean {
    val scheduled = plannedDate ?: return false
    return when (constraint) {
      TaskConstraint.Exactly -> scheduled == reference
      TaskConstraint.NotBefore -> scheduled <= reference
      TaskConstraint.NotAfter -> {
        val windowStart = latestStartDate() ?: return false
        reference in windowStart..scheduled
      }
    }
  }

  private fun Task.isOverdueOn(reference: LocalDate): Boolean =
    when (constraint) {
      TaskConstraint.Exactly -> plannedDate?.let { it < reference } == true
      TaskConstraint.NotBefore -> false
      TaskConstraint.NotAfter -> plannedDate?.let { it < reference } == true
    }

  private fun Task.isFutureRelativeTo(reference: LocalDate): Boolean =
    when (constraint) {
      TaskConstraint.Exactly -> plannedDate?.let { it > reference } == true
      TaskConstraint.NotBefore -> plannedDate?.let { it > reference } == true
      TaskConstraint.NotAfter -> {
        val windowStart = latestStartDate() ?: return false
        windowStart > reference
      }
    }

  private fun Task.latestStartDate(): LocalDate? {
    val due = plannedDate ?: return null
    val spanDays = estimateDaySpan()
    return if (spanDays <= 1) {
      due
    } else {
      due.minus(spanDays - 1, DateTimeUnit.DAY)
    }
  }

  private fun Task.estimateDaySpan(): Int {
    val minutes = estimateMinutes.coerceAtLeast(1)
    val span = (minutes + MINUTES_PER_DAY - 1) / MINUTES_PER_DAY
    return span.coerceAtLeast(1)
  }

  private fun Task.actionableDate(): LocalDate? =
    when (constraint) {
      TaskConstraint.Exactly -> plannedDate
      TaskConstraint.NotBefore -> plannedDate
      TaskConstraint.NotAfter -> latestStartDate()
    }

  private fun List<Task>.toHierarchy(sorter: Comparator<Task> = taskSorter): List<TaskNode> {
    val map = groupBy { it.parentId }
    fun build(parentId: Long?, depth: Int): List<TaskNode> {
      val children = map[parentId].orEmpty().sortedWith(sorter)
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

  private val alphabeticalSorter =
    compareBy<Task> { it.title.lowercase() }
      .thenBy { it.id }

  private val overdueSorter =
    compareBy<Task> { it.plannedDate ?: LocalDate(1970, 1, 1) }
      .thenBy { it.title.lowercase() }
      .thenBy { it.id }

  private val futureSorter =
    compareBy<Task> { it.actionableDate() ?: LocalDate(9999, 12, 31) }
      .thenBy { it.plannedStart?.hour ?: Int.MAX_VALUE }
      .thenBy { it.plannedStart?.minute ?: Int.MAX_VALUE }
      .thenBy { it.title.lowercase() }
      .thenBy { it.id }

  private val archivedSorter =
    compareByDescending<Task> { it.updatedAt }.thenByDescending { it.id }

  private val taskSorter =
    compareBy<Task>(
      { it.actionableDate() == null },
      { it.actionableDate() ?: LocalDate(1970, 1, 1) },
      { it.plannedStart?.hour ?: Int.MAX_VALUE },
      { it.plannedStart?.minute ?: Int.MAX_VALUE },
      { it.id },
    )

  private fun Task.isArchived(): Boolean = state == TaskState.Done || state == TaskState.Cancelled

}

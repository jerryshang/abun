package dev.tireless.abun.tasks

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.tireless.abun.core.time.currentInstant
import dev.tireless.abun.database.AppDatabase
import dev.tireless.abun.database.SelectPlannerTaskById
import dev.tireless.abun.database.SelectPlannerTasks
import dev.tireless.abun.database.Task_log
import dev.tireless.abun.tags.TagDomain
import dev.tireless.abun.tags.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.math.max

class TaskPlannerRepository(
  private val database: AppDatabase,
  private val tagRepository: TagRepository,
) {
  private val queries = database.plannerTasksQueries

  private val tasksFlow: Flow<List<Task>> =
    combine(
      queries
        .selectPlannerTasks()
        .asFlow()
        .mapToList(Dispatchers.IO),
      queries
        .selectPlannerTaskTags()
        .asFlow()
        .mapToList(Dispatchers.IO),
    ) { tasks, tags ->
      val tagsByTask =
        tags
          .groupBy { it.task_id }
          .mapValues { entry -> entry.value.map { it.tag_id }.toSet() }
      tasks.map { row -> row.toDomain(tagsByTask[row.id] ?: emptySet()) }
    }

  fun observeAllTasks(): Flow<List<Task>> = tasksFlow

  fun observeInbox(): Flow<List<TaskNode>> =
    tasksFlow.map { list ->
      list
        .filter { task -> !task.isArchived() && task.plannedDate == null }
        .toHierarchy(alphabeticalSorter)
    }

  fun observeToday(reference: LocalDate): Flow<List<TaskNode>> =
    tasksFlow.map { list ->
      val active = list.filterNot { it.isArchived() }
      val todays = active.filter { it.shouldAppearToday(reference) }
      val overdue = active.filter { it.isOverdueOn(reference) }
      val todayNodes = todays.toHierarchy(alphabeticalSorter)
      val overdueNodes = overdue.toHierarchy(overdueSorter)
      todayNodes + overdueNodes
    }

  fun observeFuture(reference: LocalDate): Flow<List<TaskNode>> =
    tasksFlow.map { list ->
      list
        .filter { task -> !task.isArchived() && task.isFutureRelativeTo(reference) }
        .toHierarchy(futureSorter)
    }

  fun observeArchived(): Flow<List<TaskNode>> =
    tasksFlow.map { list ->
      list
        .filter { task -> task.isArchived() }
        .toHierarchy(archivedSorter)
    }

  fun observeTask(taskId: Long): Flow<Task?> =
    tasksFlow.map { list -> list.find { it.id == taskId } }

  fun observeLogs(taskId: Long): Flow<List<TaskLog>> =
    queries
      .selectPlannerTaskLogsByTaskId(taskId)
      .asFlow()
      .mapToList(Dispatchers.IO)
      .map { rows -> rows.map { it.toDomain() } }

  fun createTask(draft: TaskDraft): Task =
    database.transactionWithResult {
      val now = currentInstant()
      val normalizedTitle = draft.title.trim()
      val estimateMinutes = max(5, draft.estimateMinutes)
      queries.insertPlannerTask(
        name = normalizedTitle,
        description = draft.description?.trim()?.takeIf { it.isNotEmpty() },
        parent_id = draft.parentId,
        strategy = "plan",
        constraint = draft.constraint.toDbValue(),
        estimate_minutes = estimateMinutes.toLong(),
        planned_date = draft.plannedDate?.toString(),
        planned_start = draft.plannedStart?.toString(),
        state = TaskState.Ready.name,
        actual_minutes = null,
        created_at = now.toEpochMilliseconds(),
        updated_at = now.toEpochMilliseconds(),
      )
      val taskId = queries.lastInsertedPlannerTaskRowId().executeAsOne()
      queries.deletePlannerTaskTags(taskId)
      draft.tagIds.forEach { tagId -> queries.insertPlannerTaskTag(taskId, tagId) }
      val tagIds = queries.selectPlannerTaskTagsForTask(taskId).executeAsList().toSet()
      queries.selectPlannerTaskById(taskId).executeAsOne().toDomain(tagIds)
    }

  fun updateTask(update: TaskUpdate): Task? =
    database.transactionWithResult {
      val existing = queries.selectPlannerTaskById(update.id).executeAsOneOrNull() ?: return@transactionWithResult null
      val now = currentInstant()
      val estimateMinutes = max(5, update.estimateMinutes)
      queries.updatePlannerTask(
        name = update.title.trim(),
        description = update.description?.trim()?.takeIf { it.isNotEmpty() },
        parent_id = update.parentId,
        strategy = existing.strategy,
        constraint = update.constraint.toDbValue(),
        estimate_minutes = estimateMinutes.toLong(),
        planned_date = update.plannedDate?.toString(),
        planned_start = update.plannedStart?.toString(),
        state = update.state.name,
        updated_at = now.toEpochMilliseconds(),
        id = update.id,
      )
      queries.deletePlannerTaskTags(update.id)
      update.tagIds.forEach { tagId -> queries.insertPlannerTaskTag(update.id, tagId) }
      val tagIds = queries.selectPlannerTaskTagsForTask(existing.id).executeAsList().toSet()
      queries.selectPlannerTaskById(existing.id).executeAsOne().toDomain(tagIds)
    }

  fun updateTaskState(input: TaskStateChange) {
    database.transaction {
      val log = appendLog(input.taskId, input.newState, input.log)
      queries.updatePlannerTaskState(
        state = input.newState.name,
        actual_minutes = log.actualMinutes.toLong(),
        updated_at = currentInstant().toEpochMilliseconds(),
        id = input.taskId,
      )
    }
  }

  fun deleteTask(taskId: Long) {
    queries.deletePlannerTask(taskId)
  }

  fun availableTagsForTasks(): Flow<List<dev.tireless.abun.tags.Tag>> =
    tagRepository.observeByDomain(TagDomain.Tasks)

  private fun appendLog(
    taskId: Long,
    state: TaskState,
    input: TaskLogInput,
  ): TaskLog {
    val now = currentInstant()
    queries.insertPlannerTaskLog(
      task_id = taskId,
      state_after = state.name,
      started_at = input.startedAt.toString(),
      ended_at = input.endedAt.toString(),
      actual_minutes = input.actualMinutes.toLong(),
      note = input.note?.trim()?.takeIf { it.isNotEmpty() },
      created_at = now.toEpochMilliseconds(),
    )
    val logId = queries.lastInsertedPlannerTaskRowId().executeAsOne()
    return queries.selectPlannerTaskLogById(logId).executeAsOne().toDomain()
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
}

private fun SelectPlannerTasks.toDomain(tagIds: Set<Long>): Task =
  Task(
    id = id,
    title = name,
    description = description,
    estimateMinutes = estimate_minutes.toInt(),
    plannedDate = planned_date?.let(LocalDate::parse),
    plannedStart = planned_start?.let(LocalTime::parse),
    constraint = constraint_value.toPlannerConstraint(),
    state = TaskState.valueOf(state),
    parentId = parent_id,
    tagIds = tagIds,
    actualMinutes = actual_minutes?.toInt(),
    createdAt = Instant.fromEpochMilliseconds(created_at),
    updatedAt = Instant.fromEpochMilliseconds(updated_at),
  )

private fun SelectPlannerTaskById.toDomain(tagIds: Set<Long>): Task =
  Task(
    id = id,
    title = name,
    description = description,
    estimateMinutes = estimate_minutes.toInt(),
    plannedDate = planned_date?.let(LocalDate::parse),
    plannedStart = planned_start?.let(LocalTime::parse),
    constraint = constraint_value.toPlannerConstraint(),
    state = TaskState.valueOf(state),
    parentId = parent_id,
    tagIds = tagIds,
    actualMinutes = actual_minutes?.toInt(),
    createdAt = Instant.fromEpochMilliseconds(created_at),
    updatedAt = Instant.fromEpochMilliseconds(updated_at),
  )

private fun Task_log.toDomain(): TaskLog =
  TaskLog(
    id = id,
    taskId = task_id,
    stateAfter = TaskState.valueOf(state_after),
    startedAt = LocalDateTime.parse(started_at),
    endedAt = LocalDateTime.parse(ended_at),
    actualMinutes = actual_minutes.toInt(),
    note = note,
    createdAt = Instant.fromEpochMilliseconds(created_at),
  )

private fun TaskConstraint.toDbValue(): String =
  when (this) {
    TaskConstraint.Exactly -> "exactly"
    TaskConstraint.NotBefore -> "not_before"
    TaskConstraint.NotAfter -> "not_after"
  }

private fun String.toPlannerConstraint(): TaskConstraint =
  when (this.lowercase()) {
    "exactly" -> TaskConstraint.Exactly
    "not_before" -> TaskConstraint.NotBefore
    "not_after" -> TaskConstraint.NotAfter
    else -> runCatching { TaskConstraint.valueOf(this) }.getOrDefault(TaskConstraint.Exactly)
  }

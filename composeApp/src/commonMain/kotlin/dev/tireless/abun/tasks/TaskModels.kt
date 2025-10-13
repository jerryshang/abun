package dev.tireless.abun.tasks

import dev.tireless.abun.core.time.currentInstant
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

enum class TaskState {
  Backlog,
  Ready,
  InProgress,
  Paused,
  Blocked,
  Done,
  Cancelled,
}

data class Task(
  val id: Long,
  val title: String,
  val description: String?,
  val estimateMinutes: Int,
  val plannedDate: LocalDate,
  val plannedStart: LocalTime,
  val notBefore: LocalDate?,
  val state: TaskState,
  val parentId: Long?,
  val tagIds: Set<Long>,
  val actualMinutes: Int?,
  val createdAt: Instant,
  val updatedAt: Instant,
)

data class TaskDraft(
  val title: String,
  val description: String?,
  val estimateMinutes: Int,
  val plannedDate: LocalDate,
  val plannedStart: LocalTime,
  val notBefore: LocalDate?,
  val parentId: Long?,
  val tagIds: Set<Long>,
)

data class TaskUpdate(
  val id: Long,
  val title: String,
  val description: String?,
  val estimateMinutes: Int,
  val plannedDate: LocalDate,
  val plannedStart: LocalTime,
  val notBefore: LocalDate?,
  val parentId: Long?,
  val tagIds: Set<Long>,
  val state: TaskState,
)

data class TaskLog(
  val id: Long,
  val taskId: Long,
  val stateAfter: TaskState,
  val startedAt: LocalDateTime,
  val endedAt: LocalDateTime,
  val actualMinutes: Int,
  val note: String?,
  val createdAt: Instant = currentInstant(),
)

data class TaskNode(
  val task: Task,
  val depth: Int,
  val children: List<TaskNode>,
  val totalEstimate: Int,
  val totalActual: Int?,
)

data class TaskStateChange(
  val taskId: Long,
  val newState: TaskState,
  val log: TaskLogInput,
)

data class TaskLogInput(
  val startedAt: LocalDateTime,
  val endedAt: LocalDateTime,
  val actualMinutes: Int,
  val note: String? = null,
)

fun Task.durationOrEstimate(): Int = actualMinutes ?: estimateMinutes

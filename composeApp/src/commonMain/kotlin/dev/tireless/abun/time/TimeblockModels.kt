package dev.tireless.abun.time

import kotlinx.datetime.Instant

data class Task(
  val id: Long,
  val name: String,
  val description: String?,
  val parentTaskId: Long?,
  val strategy: String,
  val createdAt: Instant,
  val updatedAt: Instant,
  val parentTaskName: String? = null,
  val parentTaskStrategy: String? = null,
)

data class Timeblock(
  val id: Long,
  val startTime: String,
  val endTime: String,
  val taskId: Long,
  val createdAt: Instant,
  val updatedAt: Instant,
  val taskName: String? = null,
  val taskDescription: String? = null,
  val taskStrategy: String? = null,
  val taskParentName: String? = null,
  val taskParentStrategy: String? = null,
)

data class Alarm(
  val id: Long,
  val timeblockId: Long,
  val minutesBefore: Long,
  val isEnabled: Boolean,
  val createdAt: Instant,
)

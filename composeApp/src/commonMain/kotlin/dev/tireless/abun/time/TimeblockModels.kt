package dev.tireless.abun.time

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class Category
  @OptIn(ExperimentalTime::class)
  constructor(
    val id: Long,
    val name: String,
    val color: String,
    val createdAt: Instant,
    val updatedAt: Instant,
  )

data class Task
  @OptIn(ExperimentalTime::class)
  constructor(
    val id: Long,
    val name: String,
    val description: String?,
    val categoryId: Long,
    val strategy: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val categoryName: String? = null,
    val categoryColor: String? = null,
  )

data class Timeblock
  @OptIn(ExperimentalTime::class)
  constructor(
    val id: Long,
    val startTime: String,
    val endTime: String,
    val taskId: Long,
    val createdAt: Instant,
    val updatedAt: Instant,
    val taskName: String? = null,
    val taskDescription: String? = null,
    val taskStrategy: String? = null,
    val categoryName: String? = null,
    val categoryColor: String? = null,
  )

data class Alarm
  @OptIn(ExperimentalTime::class)
  constructor(
    val id: Long,
    val timeblockId: Long,
    val minutesBefore: Long,
    val isEnabled: Boolean,
    val createdAt: Instant,
  )

package dev.tireless.abun.tasks

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

private const val MINUTES_PER_DAY = 24 * 60

internal fun Task.shouldAppearToday(reference: LocalDate): Boolean {
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

internal fun Task.isOverdueOn(reference: LocalDate): Boolean =
  when (constraint) {
    TaskConstraint.Exactly -> plannedDate?.let { it < reference } == true
    TaskConstraint.NotBefore -> false
    TaskConstraint.NotAfter -> plannedDate?.let { it < reference } == true
  }

internal fun Task.isFutureRelativeTo(reference: LocalDate): Boolean =
  when (constraint) {
    TaskConstraint.Exactly -> plannedDate?.let { it > reference } == true
    TaskConstraint.NotBefore -> plannedDate?.let { it > reference } == true
    TaskConstraint.NotAfter -> {
      val windowStart = latestStartDate() ?: return false
      windowStart > reference
    }
  }

internal fun Task.latestStartDate(): LocalDate? {
  val due = plannedDate ?: return null
  val spanDays = estimateDaySpan()
  return if (spanDays <= 1) {
    due
  } else {
    due.minus(spanDays - 1, DateTimeUnit.DAY)
  }
}

internal fun Task.estimateDaySpan(): Int {
  val minutes = estimateMinutes.coerceAtLeast(1)
  val span = (minutes + MINUTES_PER_DAY - 1) / MINUTES_PER_DAY
  return span.coerceAtLeast(1)
}

internal fun Task.actionableDate(): LocalDate? =
  when (constraint) {
    TaskConstraint.Exactly -> plannedDate
    TaskConstraint.NotBefore -> plannedDate
    TaskConstraint.NotAfter -> latestStartDate()
  }

internal fun Task.isArchived(): Boolean = state == TaskState.Done || state == TaskState.Cancelled

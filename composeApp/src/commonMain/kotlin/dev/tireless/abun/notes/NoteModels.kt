package dev.tireless.abun.notes

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class Note(
  val id: Long,
  val title: String,
  val content: String,
  val tagIds: Set<Long>,
  val createdAt: Instant,
  val updatedAt: Instant,
  val pinned: Boolean = false,
)

data class NoteDraft(
  val title: String,
  val content: String,
  val tagIds: Set<Long>,
  val pinned: Boolean = false,
)

data class NoteUpdate(
  val id: Long,
  val title: String,
  val content: String,
  val tagIds: Set<Long>,
  val pinned: Boolean,
)

data class NoteSummary(
  val id: Long,
  val title: String,
  val preview: String,
  val updatedAt: Instant,
  val tagIds: Set<Long>,
  val pinned: Boolean,
)

fun Note.toSummary(): NoteSummary {
  val preview = content.lineSequence().firstOrNull()?.take(140) ?: ""
  return NoteSummary(id, title, preview, updatedAt, tagIds, pinned)
}

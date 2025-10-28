package dev.tireless.abun.notes

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.tireless.abun.core.time.currentInstant
import dev.tireless.abun.database.AppDatabase
import dev.tireless.abun.database.Note as DbNoteRow
import dev.tireless.abun.tags.TagDomain
import dev.tireless.abun.tags.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class RichNoteRepository(
  private val database: AppDatabase,
  private val tagRepository: TagRepository,
) {
  private val queries = database.noteQueries

  private val notesFlow: Flow<List<Note>> =
    combine(
      queries
        .selectAllNotes()
        .asFlow()
        .mapToList(Dispatchers.IO),
      queries
        .selectNoteTagLinks()
        .asFlow()
        .mapToList(Dispatchers.IO),
    ) { noteRows, tagLinks ->
      val tagIdsByNote =
        tagLinks
          .groupBy { it.note_id }
          .mapValues { entry -> entry.value.map { it.tag_id }.toSet() }
      noteRows.map { row -> row.toDomain(tagIdsByNote[row.id] ?: emptySet()) }
    }

  fun observeAll(): Flow<List<Note>> = notesFlow

  fun observeSummaries(): Flow<List<NoteSummary>> =
    notesFlow.map { list -> list.sortedWith(summaryComparator).map { it.toSummary() } }

  fun observeNote(id: Long): Flow<Note?> = notesFlow.map { list -> list.find { it.id == id } }

  fun observeNotesByTags(tagIds: Set<Long>): Flow<List<NoteSummary>> =
    notesFlow.map { list ->
      list.filter { note -> tagIds.isEmpty() || note.tagIds.intersect(tagIds).isNotEmpty() }
        .sortedWith(summaryComparator)
        .map { it.toSummary() }
    }

  fun searchNotes(query: String): Flow<List<NoteSummary>> {
    if (query.isBlank()) return observeSummaries()
    val normalized = query.trim().lowercase()
    return notesFlow.map { list ->
      list.filter { note ->
        note.title.lowercase().contains(normalized) ||
          note.content.lowercase().contains(normalized)
      }.sortedWith(summaryComparator)
        .map { it.toSummary() }
    }
  }

  fun availableTags(): Flow<List<dev.tireless.abun.tags.Tag>> =
    tagRepository.observeByDomain(TagDomain.Notes)

  fun createNote(draft: NoteDraft): Note =
    database.transactionWithResult {
      val now = currentInstant()
      val normalizedTitle = draft.title.ifBlank { "Untitled" }.trim()
      queries.insertNote(
        title = normalizedTitle,
        content = draft.content,
        pinned = draft.pinned.toDbBoolean(),
        created_at = now.toEpochMilliseconds(),
        updated_at = now.toEpochMilliseconds(),
      )
      val id = queries.lastInsertedNoteId().executeAsOne()
      queries.deleteNoteTags(id)
      draft.tagIds.forEach { tagId -> queries.insertNoteTag(id, tagId) }
      val tagIds = queries.selectTagIdsForNote(id).executeAsList().toSet()
      queries.selectNoteById(id).executeAsOne().toDomain(tagIds)
    }

  fun updateNote(update: NoteUpdate) {
    val now = currentInstant()
    database.transaction {
      queries.updateNote(
        title = update.title.ifBlank { "Untitled" }.trim(),
        content = update.content,
        pinned = update.pinned.toDbBoolean(),
        updated_at = now.toEpochMilliseconds(),
        id = update.id,
      )
      queries.deleteNoteTags(update.id)
      update.tagIds.forEach { tagId -> queries.insertNoteTag(update.id, tagId) }
    }
  }

  fun deleteNote(id: Long) {
    queries.deleteNote(id)
  }

  private val summaryComparator =
    compareByDescending<Note> { it.pinned }
      .thenByDescending { it.updatedAt }
      .thenBy { it.title }
}

private fun DbNoteRow.toDomain(tagIds: Set<Long>): Note =
  Note(
    id = id,
    title = title,
    content = content,
    tagIds = tagIds,
    createdAt = Instant.fromEpochMilliseconds(created_at),
    updatedAt = Instant.fromEpochMilliseconds(updated_at),
    pinned = pinned != 0L,
  )

private fun Boolean.toDbBoolean(): Long = if (this) 1 else 0

package dev.tireless.abun.notes

import dev.tireless.abun.tags.TagDomain
import dev.tireless.abun.tags.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class RichNoteRepository(
  private val tagRepository: TagRepository,
) {
  private val notes = MutableStateFlow(sampleNotes())
  private var noteIdCounter: Long = notes.value.maxOfOrNull { it.id }?.plus(1) ?: 1L

  fun observeAll(): StateFlow<List<Note>> = notes

  fun observeSummaries(): Flow<List<NoteSummary>> =
    notes.map { list ->
      list.sortedWith(summaryComparator).map { it.toSummary() }
    }

  fun observeNote(id: Long): Flow<Note?> = notes.map { list -> list.find { it.id == id } }

  fun observeNotesByTags(tagIds: Set<Long>): Flow<List<NoteSummary>> {
    return notes.map { list ->
      list.filter { note -> tagIds.isEmpty() || note.tagIds.intersect(tagIds).isNotEmpty() }
        .sortedWith(summaryComparator)
        .map { it.toSummary() }
    }
  }

  fun searchNotes(query: String): Flow<List<NoteSummary>> {
    if (query.isBlank()) return observeSummaries()
    val normalized = query.trim().lowercase()
    return notes.map { list ->
      list.filter { note ->
        note.title.lowercase().contains(normalized) ||
          note.content.lowercase().contains(normalized)
      }.sortedWith(summaryComparator)
        .map { it.toSummary() }
    }
  }

  fun availableTags(): Flow<List<dev.tireless.abun.tags.Tag>> =
    tagRepository.observeByDomain(TagDomain.Notes)

  fun createNote(draft: NoteDraft): Note {
    val now = Clock.System.now()
    val note =
      Note(
        id = nextId(),
        title = draft.title.ifBlank { "Untitled" },
        content = draft.content,
        tagIds = draft.tagIds,
        createdAt = now,
        updatedAt = now,
        pinned = draft.pinned,
      )
    notes.value = notes.value + note
    return note
  }

  fun updateNote(update: NoteUpdate) {
    val now = Clock.System.now()
    notes.value =
      notes.value.map { existing ->
        if (existing.id == update.id) {
          existing.copy(
            title = update.title.ifBlank { "Untitled" },
            content = update.content,
            tagIds = update.tagIds,
            pinned = update.pinned,
            updatedAt = now,
          )
        } else {
          existing
        }
      }
  }

  fun deleteNote(id: Long) {
    notes.value = notes.value.filterNot { it.id == id }
  }

  private fun nextId(): Long = noteIdCounter++

  private fun sampleNotes(): List<Note> {
    val nowInstant = Clock.System.now()
    return listOf(
      Note(
        id = 1,
        title = "Weekly retrospective",
        content = "## Wins\n- Wrapped up API migration\n- Cleared backlog triage\n\n## Lessons\nStay heads-down earlier in the week.",
        tagIds = setOf(1L),
        createdAt = nowInstant,
        updatedAt = nowInstant,
        pinned = true,
      ),
      Note(
        id = 2,
        title = "Budget review questions",
        content = "Remember to confirm card cashback, check subscriptions, and compare actual vs planned in YNAB.",
        tagIds = setOf(2L),
        createdAt = nowInstant,
        updatedAt = nowInstant,
      ),
      Note(
        id = 3,
        title = "Reading notes: Deep Work",
        content = "*Focus blocks should be 90 minutes.* Practice shutting down properly at the end of the day.",
        tagIds = setOf(3L),
        createdAt = nowInstant,
        updatedAt = nowInstant,
      ),
    )
  }

  private val summaryComparator =
    compareByDescending<Note> { it.pinned }
      .thenByDescending { it.updatedAt }
      .thenBy { it.title }
}

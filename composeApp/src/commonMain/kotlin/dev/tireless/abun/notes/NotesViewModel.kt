package dev.tireless.abun.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tireless.abun.tags.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class NotesViewModel(
  private val repository: RichNoteRepository,
) : ViewModel() {
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery

  private val _selectedTagIds = MutableStateFlow<Set<Long>>(emptySet())
  val selectedTagIds: StateFlow<Set<Long>> = _selectedTagIds

  private val _editingNoteId = MutableStateFlow<Long?>(null)

  val tags: StateFlow<List<Tag>> =
    repository.availableTags()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  val summaries: StateFlow<List<NoteSummary>> =
    combine(_searchQuery, _selectedTagIds) { query, tagIds -> query to tagIds }
      .flatMapLatest { (query, tagIds) ->
        when {
          query.isNotBlank() -> repository.searchNotes(query)
          tagIds.isNotEmpty() -> repository.observeNotesByTags(tagIds)
          else -> repository.observeSummaries()
        }
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  val editingNote: StateFlow<Note?> =
    _editingNoteId
      .flatMapLatest { noteId ->
        if (noteId == null) {
          flowOf(null)
        } else {
          repository.observeNote(noteId)
        }
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun toggleTagFilter(tagId: Long) {
    _selectedTagIds.value =
      _selectedTagIds.value.toMutableSet().also { set ->
        if (!set.add(tagId)) {
          set.remove(tagId)
        }
      }
  }

  fun clearFilters() {
    _searchQuery.value = ""
    _selectedTagIds.value = emptySet()
  }

  fun startEditing(noteId: Long?) {
    _editingNoteId.value = noteId
  }

  fun saveNote(
    existingId: Long?,
    draft: NoteDraft,
  ) {
    viewModelScope.launch {
      if (existingId == null) {
        repository.createNote(draft)
      } else {
        val current = editingNote.value ?: return@launch
        repository.updateNote(
          NoteUpdate(
            id = existingId,
            title = draft.title,
            content = draft.content,
            tagIds = draft.tagIds,
            pinned = draft.pinned,
          ),
        )
      }
      _editingNoteId.value = null
    }
  }

  fun deleteNote(noteId: Long) {
    repository.deleteNote(noteId)
    if (_editingNoteId.value == noteId) {
      _editingNoteId.value = null
    }
  }

  fun togglePin(noteId: Long) {
    viewModelScope.launch {
      val current = repository.observeNote(noteId).firstOrNull() ?: return@launch
      repository.updateNote(
        NoteUpdate(
          id = current.id,
          title = current.title,
          content = current.content,
          tagIds = current.tagIds,
          pinned = !current.pinned,
        ),
      )
    }
  }
}

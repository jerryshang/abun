package dev.tireless.abun.mental

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tireless.abun.database.Note as DbNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteViewModel(
  private val noteRepository: NoteRepository,
) : ViewModel() {
  private val _notes = MutableStateFlow<List<DbNote>>(emptyList())
  val notes: StateFlow<List<DbNote>> = _notes.asStateFlow()

  private val _selectedNote = MutableStateFlow<DbNote?>(null)
  val selectedNote: StateFlow<DbNote?> = _selectedNote.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  init {
    loadNotes()
  }

  fun loadNotes() {
    viewModelScope.launch {
      _isLoading.value = true
      try {
        noteRepository.getAllNotes().collect { notesList ->
          _notes.value = notesList
        }
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun createNote(
    title: String,
    content: String,
  ) {
    viewModelScope.launch {
      try {
        noteRepository.insertNote(title, content)
        loadNotes() // Refresh the list
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun updateNote(
    id: Long,
    title: String,
    content: String,
  ) {
    viewModelScope.launch {
      try {
        noteRepository.updateNote(id, title, content)
        loadNotes() // Refresh the list
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun deleteNote(id: Long) {
    viewModelScope.launch {
      try {
        noteRepository.deleteNote(id)
        loadNotes() // Refresh the list
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun selectNote(note: DbNote?) {
    _selectedNote.value = note
  }

  fun searchNotes(query: String) {
    _searchQuery.value = query
    viewModelScope.launch {
      try {
        if (query.isBlank()) {
          loadNotes()
        } else {
          noteRepository.searchNotes(query).collect { searchResults ->
            _notes.value = searchResults
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }
}

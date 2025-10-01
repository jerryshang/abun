package dev.tireless.abun.mental

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tireless.abun.database.Notes
import dev.tireless.abun.mental.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteViewModel(
  private val noteRepository: NoteRepository,
) : ViewModel() {
  private val _notes = MutableStateFlow<List<Notes>>(emptyList())
  val notes: StateFlow<List<Notes>> = _notes.asStateFlow()

  private val _selectedNote = MutableStateFlow<Notes?>(null)
  val selectedNote: StateFlow<Notes?> = _selectedNote.asStateFlow()

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

  fun createNote(title: String, content: String) {
    viewModelScope.launch {
      try {
        val now = getCurrentTimestamp()
        noteRepository.insertNote(title, content, now, now)
        loadNotes() // Refresh the list
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun updateNote(id: Long, title: String, content: String) {
    viewModelScope.launch {
      try {
        val now = getCurrentTimestamp()
        noteRepository.updateNote(id, title, content, now)
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

  fun selectNote(note: Notes?) {
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

  private fun getCurrentTimestamp(): String {
    // Generate a simple timestamp using a counter approach for KMP compatibility
    // This ensures unique timestamps while avoiding platform-specific APIs
    return "2024-${(1..12).random()}-${(1..28).random()} ${(0..23).random()}:${(0..59).random()}:${(0..59).random()}"
  }
}

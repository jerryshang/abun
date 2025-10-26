package dev.tireless.abun.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TrialCalculatorViewModel(
  private val repository: TrialCalculatorRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(TrialCalculatorUiState())
  val uiState: StateFlow<TrialCalculatorUiState> = _uiState.asStateFlow()

  private var nextId: Long = computeNextId(_uiState.value.entries)
  private var saveJob: Job? = null

  init {
    viewModelScope.launch {
      repository
        .observeEntries()
        .collect { savedEntries ->
          val targetEntries =
            if (savedEntries.isEmpty()) {
              defaultTrialCalculatorEntries()
            } else {
              savedEntries
            }
          if (_uiState.value.entries != targetEntries) {
            nextId = computeNextId(targetEntries)
            _uiState.value = _uiState.value.copy(entries = targetEntries)
          }
        }
    }
  }

  fun updateSign(
    id: Long,
    isPositive: Boolean,
  ) {
    updateEntry(id) { it.copy(isPositive = isPositive) }
  }

  fun updateAmount(
    id: Long,
    amount: String,
  ) {
    if (amount.isNotEmpty() && !isValidAmountInput(amount)) {
      return
    }
    updateEntry(id) { it.copy(amount = amount) }
  }

  fun updateNote(
    id: Long,
    note: String,
  ) {
    updateEntry(id) { it.copy(note = note) }
  }

  fun addEntryBelow(anchorId: Long) {
    mutateEntries { entries ->
      val index = entries.indexOfFirst { it.id == anchorId }
      val newEntry = TrialCalculatorEntry(id = nextId)

      if (index >= 0 && index < entries.size - 1) {
        entries.add(index + 1, newEntry)
      } else {
        entries.add(newEntry)
      }
      true
    }
  }

  fun addEntry() {
    mutateEntries { entries ->
      entries.add(TrialCalculatorEntry(id = nextId))
      true
    }
  }

  fun deleteEntry(id: Long) {
    mutateEntries { entries ->
      if (entries.size <= 1) {
        return@mutateEntries false
      }
      val removed = entries.removeAll { it.id == id }
      removed
    }
  }

  fun moveEntryUp(id: Long) {
    mutateEntries { entries ->
      val currentIndex = entries.indexOfFirst { it.id == id }
      if (currentIndex > 0) {
        entries.move(currentIndex, currentIndex - 1)
        true
      } else {
        false
      }
    }
  }

  fun moveEntryDown(id: Long) {
    mutateEntries { entries ->
      val currentIndex = entries.indexOfFirst { it.id == id }
      if (currentIndex >= 0 && currentIndex < entries.lastIndex) {
        entries.move(currentIndex, currentIndex + 1)
        true
      } else {
        false
      }
    }
  }

  fun clearAll() {
    val clearedEntries = defaultTrialCalculatorEntries()
    nextId = computeNextId(clearedEntries)
    _uiState.value = _uiState.value.copy(entries = clearedEntries)
    saveJob?.cancel()
    saveJob =
      viewModelScope.launch {
        repository.clearEntries()
      }
  }

  private inline fun mutateEntries(
    crossinline mutator: (MutableList<TrialCalculatorEntry>) -> Boolean,
  ) {
    val current = _uiState.value.entries
    val mutable = current.toMutableList()
    val changed = mutator(mutable)
    if (changed) {
      applyEntries(mutable)
    }
  }

  private inline fun updateEntry(
    id: Long,
    crossinline transform: (TrialCalculatorEntry) -> TrialCalculatorEntry,
  ) {
    mutateEntries { entries ->
      val index = entries.indexOfFirst { it.id == id }
      if (index >= 0) {
        val updated = transform(entries[index])
        if (updated != entries[index]) {
          entries[index] = updated
          true
        } else {
          false
        }
      } else {
        false
      }
    }
  }

  private fun applyEntries(entries: List<TrialCalculatorEntry>) {
    nextId = computeNextId(entries)
    _uiState.value = _uiState.value.copy(entries = entries)
    saveJob?.cancel()
    saveJob =
      viewModelScope.launch {
        repository.saveEntries(entries)
      }
  }

  private fun computeNextId(entries: List<TrialCalculatorEntry>): Long =
    (entries.maxOfOrNull { it.id } ?: -1L) + 1L

  override fun onCleared() {
    super.onCleared()
    saveJob?.cancel()
  }
}

data class TrialCalculatorUiState(
  val entries: List<TrialCalculatorEntry> = defaultTrialCalculatorEntries(),
)

private fun MutableList<TrialCalculatorEntry>.move(fromIndex: Int, toIndex: Int) {
  if (fromIndex == toIndex || fromIndex !in indices) return
  val item = removeAt(fromIndex)
  val boundedIndex = toIndex.coerceIn(0, size)
  add(boundedIndex, item)
}

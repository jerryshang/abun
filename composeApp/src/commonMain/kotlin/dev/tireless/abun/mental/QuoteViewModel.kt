package dev.tireless.abun.mental

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tireless.abun.database.Quote as DbQuote
import dev.tireless.abun.mental.QuotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuoteViewModel(
  private val quotesRepository: QuotesRepository,
) : ViewModel() {
  private val _currentQuote = MutableStateFlow<DbQuote?>(null)
  val currentQuote: StateFlow<DbQuote?> = _currentQuote.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  init {
    loadRandomQuote()
  }

  fun loadRandomQuote() {
    viewModelScope.launch {
      _isLoading.value = true
      try {
        val quote = quotesRepository.getRandomQuote()
        _currentQuote.value = quote
      } catch (e: Exception) {
        // Handle error if needed
        e.printStackTrace()
      } finally {
        _isLoading.value = false
      }
    }
  }
}

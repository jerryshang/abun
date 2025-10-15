package dev.tireless.abun.finance

import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TrialCalculatorRepository(
  private val settings: Settings,
  private val json: Json = Json {
    ignoreUnknownKeys = true
  },
) {
  private val entriesFlow = MutableStateFlow(loadSavedEntries())

  fun observeEntries(): Flow<List<TrialCalculatorEntry>> = entriesFlow.asStateFlow()

  suspend fun saveEntries(entries: List<TrialCalculatorEntry>) {
    val serialized = json.encodeToString(TrialCalculatorPayload(entries))
    withContext(Dispatchers.IO) {
      settings.putString(TRIAL_CALCULATOR_SETTINGS_KEY, serialized)
    }
    entriesFlow.value = entries
  }

  suspend fun clearEntries() {
    withContext(Dispatchers.IO) {
      settings.remove(TRIAL_CALCULATOR_SETTINGS_KEY)
    }
    entriesFlow.value = emptyList()
  }

  private fun loadSavedEntries(): List<TrialCalculatorEntry> {
    if (!settings.hasKey(TRIAL_CALCULATOR_SETTINGS_KEY)) {
      return emptyList()
    }
    val raw = settings.getString(TRIAL_CALCULATOR_SETTINGS_KEY, "")
    if (raw.isBlank()) {
      return emptyList()
    }
    return decodePayload(raw)
  }

  private fun decodePayload(raw: String): List<TrialCalculatorEntry> =
    runCatching {
      json.decodeFromString<TrialCalculatorPayload>(raw).entries
    }.getOrElse {
      emptyList()
    }
}

private const val TRIAL_CALCULATOR_SETTINGS_KEY = "trial_calculator_entries"

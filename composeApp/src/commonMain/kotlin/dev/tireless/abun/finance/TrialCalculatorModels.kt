package dev.tireless.abun.finance

import kotlinx.serialization.Serializable

@Serializable
data class TrialCalculatorEntry(
  val id: Long,
  val isPositive: Boolean = true,
  val amount: String = "",
  val note: String = "",
)

@Serializable
data class TrialCalculatorPayload(
  val entries: List<TrialCalculatorEntry> = emptyList(),
)

const val TRIAL_CALCULATOR_DEFAULT_ENTRY_COUNT = 2

fun defaultTrialCalculatorEntries(): List<TrialCalculatorEntry> =
  List(TRIAL_CALCULATOR_DEFAULT_ENTRY_COUNT) { index ->
    TrialCalculatorEntry(id = index.toLong())
  }
